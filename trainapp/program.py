import sqlite3
import sys
import time
import math
from datetime import date
from datetime import timedelta
def dict_factory(cursor, row):
    d = {}
    for idx, col in enumerate(cursor.description):
        d[col[0]] = row[idx]
    return d

def greatCircleDistance(lat1,lon1,lat2,lon2):
	R = 6371;
	dLat = math.radians(lat2-lat1)
	dLon = math.radians(lon2-lon1)
	lat1 = math.radians(lat1)
	lat2 = math.radians(lat2)
	a = math.sin(dLat/2.0) * math.sin(dLat/2.0) + math.sin(dLon/2.0) * math.sin(dLon/2.0) * math.cos(lat1) * math.cos(lat2)
	c = 2*math.atan2(math.sqrt(a), math.sqrt(1-a))
	return R*c
	
def loadStation(cursor, stationId):
	cursor.execute("select * from stop where stop_id=?",(stationId,))
	reg = cursor.fetchone()
	cursor.close()
	return reg

def distance(stopa,stopb):
	return greatCircleDistance(stopa["lat"],stopa["lon"],stopb["lat"],stopb["lon"])*1000
	
def hasTransferEdge(cursor, stationA, stationB):
	c.execute("select count(*) as count from transfer_edge where source=? and target=?",(stationA,stationB))
	count = c.fetchone()["count"]
#	print stationA, stationB, count
	c.close()
	return count==1

def onRoute(cursor, station, route):
	c.execute("select count(*) as count from station_route where station=? and route=?",(currStation,currRoute))
	count = c.fetchone()["count"]
	c.close()
	return count==1


conn = sqlite3.connect("target/test.db")
conn.row_factory = dict_factory
c = conn.cursor()
# stationA = {"id":"s90327","name":"West Trenton"}
# stationB = {"id":"n148","name":"Trenton Transit Center"}
stationA = None
stationB = None
resultCount = 0
while stationA==None or stationB==None:
	query = "What station will you be departing from (s to search"
	if(resultCount>0):
		query = query + ", <enter> to use first result)"
	query = query + ")? "
	x = raw_input(query)
	rowCount = 0
	if(x.lower()=="s") :
		keyword = raw_input("Enter station keyword or id: ")
		rows = c.execute("select stop_id as id, name as name from stop where name like ? order by name desc, id desc", ("%" + keyword + "%",))
		resultCount = 0
		row = None
		for row in rows:
			resultCount = resultCount +1
			print row["name"] + " (" + row["id"] + ")"
		if resultCount==1:
			resultCount = 0
			if stationA==None:
				stationA = row
				print "Departing from",stationA["name"]
			else:
				stationB = row
				print "Arriving at",stationB["name"]
	else:
		c.execute("select stop_id as id, name as name from stop where upper(name)=upper(?) or stop_id=?",(x,x))
		row = c.fetchone()
		if(row==None):
			continue
		if stationA==None:
			stationA = row
			print "Departing from",stationA["name"]
		else:
			stationB = row
			print "Arriving at",stationB["name"]
print "\n\n",stationA["name"],"to",stationB["name"]
print stationA["id"],stationB["id"]
agencyA = stationA["id"][0]
agencyB = stationB["id"][0]
# if agencyA!=agencyB:
# 	orig = loadStation(c,stationA["id"])
# 	c.execute("select source,target from agency_transfer_edge where agency_source=? and agency_target=?",(agencyA,agencyB))
# 	closest = None
# 	closestDist = 10000000
# 	rowToUse = None
# 	for result in c.fetchall():
# 		print result
# 		source = result["source"]
# 		if source==stationA["id"]:
# 			rowToUse = result
# 			break
# 		stat = loadStation(c,source)
# 		dist = distance(orig,stat)
# 		if(dist<closestDist):
# 			closest = stat
# 			closestDist = dist
# 			rowToUse = result
# 	print dist,orig,closest,closestDist,"\n\n",rowToUse
c.execute("select nodes from shortest_path where source=? and target=?",(stationA["id"],stationB["id"]))
sp = c.fetchone()
if sp["nodes"] == "":
	shortestPath = []
else:
	shortestPath = sp["nodes"].split(",")
shortestPath.insert(0,stationA["id"])
shortestPath.append(stationB["id"])
ids = "','".join(shortestPath)
ids = "'" + ids + "'"
c.execute("select name,stop_id from stop where stop_id in (" + ids + ")")
stops = c.fetchall();
stopDict = {}
for stop in stops:
	stopDict[stop["stop_id"]] = stop
print shortestPath
print stopDict
for key in shortestPath:
	sys.stdout.write(stopDict[key]["name"])
	sys.stdout.write(",")
print shortestPath
c.execute("select route from station_route where station=?",(stationA["id"],))
aRoutes = c.fetchall()
aaRoutes = []
bbRoutes = []
for route in aRoutes:
	aaRoutes.append(route["route"])
c.execute("select route from station_route where station=?",(stationB["id"],))
bRoutes = c.fetchall()
for route in bRoutes:
	bbRoutes.append(route["route"])
#print aaRoutes,"\n",bRoutes
#print shortest_path
routes = []
minRouteLen = sys.maxint
for aRoute in aaRoutes:
	for bRoute in bbRoutes:
		c.execute("select nodes,hop_count,source,target from shortest_route_path where source=? and target=?",(aRoute,bRoute))
		shortest_route = c.fetchone()
		routes.append(shortest_route)
print "Routes", routes
routes = sorted(routes, key=lambda route: route["hop_count"])
totalRoutes = len(routes)

route = routes[0]
if len(route["nodes"])==0:
	routeToUse = []
else:
	routeToUse = route["nodes"].split(",")
routeToUse.append(route["target"])
currRoute = route["source"]
currRoutePos = -1
stationsToUse = []
lastStation = stopDict[stationA["id"]]
startStation = lastStation
for currStation in shortestPath:
	station = stopDict[currStation]
	isTransferEdge = hasTransferEdge(c,lastStation["stop_id"],station["stop_id"])
	if isTransferEdge:
		print "\t\t\t transfer edge"
		stationsToUse.append((startStation,lastStation))
		startStation = station
	isOnRoute = onRoute(c,station["stop_id"],currRoute)
	print station["name"]
	if isOnRoute==False:
		print "\t\t\tnot on route"
		currRoutePos = currRoutePos+1
		currRoute = routeToUse[currRoutePos]
		if isTransferEdge==False:
			print "\t\t\t ok"
			stationsToUse.append((startStation,lastStation))
			startStation = lastStation
	lastStation = station
if startStation!=lastStation:
	stationsToUse.append((startStation,lastStation))
for fro,to in stationsToUse:
	print fro["name"],"->",to["name"]
# for currStation in shortestPath:
# 	print "evaluating",currStation
# 	c.execute("select count(*) as count from station_route where station=? and route=?",(currStation,currRoute))
# 	count = c.fetchone()["count"]
# 	if count==0:
# 		print "no route for station",currStation,"route",currRoute
# 		print "setting last station from ",lastStation,"to",currStation		
# 		last.append(lastStation)
# 		length = len(stationsToUse)
# 		if length>0:
# 			while len(last)>1 and len(last) % 2 == 0:	
# 				popA = last.pop()			
# 				stationsToUse.append((last.pop(),popA))
# 			if len(last)==1:
# 				stationsToUse.append((last.pop(),currStation))
# 		else:
# 			print "else",(stationA["id"],lastStation)
# 			stationsToUse.append((stationA["id"],lastStation))
# 		currRoutePos = currRoutePos+1
# 		currRoute = routeToUse[currRoutePos]
# 		print "inner last setting from",innerLast,"to",currStation
# 		innerLast = currStation
# 	lastStation = currStation
# if len(last)==1:
# 	stationsToUse.append((last.pop(),stationB["id"]))
# else:
# 	stationsToUse.append((innerLast,stationB["id"]))
today = date.today()
tomorrow = today + timedelta(days=1)
today = str(today).replace("-","")
tomorrow = str(tomorrow).replace("-","")
print today,tomorrow
print stationsToUse
times = []
i = 0
print ""
for x,y in stationsToUse:
	print x,y
	c.execute("select a1.depart,a2.arrive from nested_trip a1 join nested_trip a2 on (a1.trip_id=a2.trip_id and a1.stop_id=? and a2.stop_id=? and a1.lft < a2.lft) where a1.service_id in (select service_id from service where date=?) order by a1.depart asc",(x["stop_id"],y["stop_id"],today))
	xytimes = []
	for r in c.fetchall():
		depart = r["depart"]
		arrive = r["arrive"]
		mytime = {"depart":depart,"arrive":arrive}
		xytimes.append(mytime)
	times.append(xytimes)
for x in times:
	kk = stationsToUse[i]
	i = i+1
	print kk[0]["name"],"->",kk[1]["name"]
	for y in x:
		print y["depart"],"-",y["arrive"]
	print "\n\n\n"
#print routes
#print "departId",stationA["id"],"arriveId",stationB["id"],"routes",routes
#print "do their routes intersect?", set(aaRoutes) & set(bbRoutes)
		
