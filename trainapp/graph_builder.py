import networkx as networkx
from datetime import datetime
from datetime import timedelta
import time as tk
import sys
import sqlite3
import os
import shutil
import math

def dict_factory(cursor, row):
    d = {}
    for idx, col in enumerate(cursor.description):
        d[col[0]] = row[idx]
    return d

def findDopple(dopples,a):
	if a in dopples:
		return dopples[a]
	return a
	
def makePretty(strt):
	lastChar = " "
	sb = list(strt)
	whitespaceDist = 0
	i = 0
	while i < len(sb):		
		nowChar = sb[i]
		if nowChar != " " and nowChar != ".":
			whitespaceDist+=1
		else:
			if whitespaceDist==2:
				b = sb[i-1]
				a = sb[i-2]
				sb[i-1] = b.upper()
				sb[i-2] = a.upper()
		if lastChar==" " or lastChar=="/":
			sb[i] = nowChar.upper()
		else:
			sb[i] = nowChar.lower()
		lastChar = nowChar
		i+=1
	return "".join(sb)

def draw(G,name):
	k = open(name,"wb")
	k.write(
	"""graph
		"unix" {
			rankdir=LR;
	"""
	);
	for u,v in G.to_undirected().edges():
		k.write(u)
		k.write(" [label=\"")
		k.write(G.node[u]["label"])
		k.write("\"];\n")
		k.write(v)
		k.write(" [label=\"")
		k.write(G.node[v]["label"])
		k.write("\"];\n")
		k.write(u)
		k.write("--")
		k.write(v)
		k.write("\n")
	k.write("""
	}""")
	
def polymap(minLat,minLon,maxLat,maxLon,stations,walks,joinedRoutes=None,showStations=False,showWalks=True,showRoutes=True):
	html = """<!DOCTYPE html>
	<html>
	  <head>
	    <title>Google Maps JavaScript API v3 Example: Map Simple</title>
	    <meta name="viewport"
	        content="width=device-width, initial-scale=1.0, user-scalable=no">
	    <meta charset="UTF-8">
	    <style type="text/css">
	      html, body, #map_canvas {
	        margin: 0;
	        padding: 0;
	        height: 100%;
	      }
	    </style>
	    <script type="text/javascript"
	        src="http://maps.googleapis.com/maps/api/js?sensor=false"></script>
	    <script type="text/javascript">
	      var map;
		  var stationCircle;
		function Label(opt_options) {
		 // Initialization
		 this.setValues(opt_options);

		 // Label specific
		 var span = this.span_ = document.createElement('span');
		 span.style.cssText = 'position: relative; left: -50%; top: -8px; ' +
		                      'white-space: nowrap; font:9px arial,sans-serviceReader; background-color: white; '

		 var div = this.div_ = document.createElement('div');
		 div.appendChild(span);
		 div.style.cssText = 'position: absolute; display: none';
		};
		Label.prototype = new google.maps.OverlayView;

		// Implement onAdd
		Label.prototype.onAdd = function() {
		 var pane = this.getPanes().overlayLayer;
		 pane.appendChild(this.div_);

		 // Ensures the label is redrawn if the text or position is changed.
		 var me = this;
		 this.listeners_ = [
		   google.maps.event.addListener(this, 'position_changed',
		       function() { me.draw(); }),
		   google.maps.event.addListener(this, 'text_changed',
		       function() { me.draw(); })
		 ];
		};

		// Implement onRemove
		Label.prototype.onRemove = function() {
		 this.div_.parentNode.removeChild(this.div_);

		 // Label is removed from the map, stop updating its position/text.
		 for (var i = 0, I = this.listeners_.length; i < I; ++i) {
		   google.maps.event.removeListener(this.listeners_[i]);
		 }
		};

		// Implement draw
		Label.prototype.draw = function() {
		 var projection = this.getProjection();
		 var position = projection.fromLatLngToDivPixel(this.get('position'));

		 var div = this.div_;
		 div.style.left = position.x + 'px';
		 div.style.top = position.y + 'px';
		 div.style.display = 'block';

		 this.span_.innerHTML = this.get('text').toString();
		};
		function rad(x) {return x*Math.PI/180;}

		function dist(p1, p2) {
		  var R = 6371; // earth's mean radius in km
		  var dLat  = rad(p2.lat() - p1.lat());
		  var dLong = rad(p2.lng() - p1.lng());

		  var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		          Math.cos(rad(p1.lat())) * Math.cos(rad(p2.lat())) * Math.sin(dLong/2) * Math.sin(dLong/2);
		  var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		  var d = R * c;

		  return d.toFixed(3);
		}
	      function initialize() {
	        var myOptions = {
	          zoom: 8,
	          center: new google.maps.LatLng(40.497278, -74.445751),
	          mapTypeId: google.maps.MapTypeId.ROADMAP
	        };
	        map = new google.maps.Map(document.getElementById('map_canvas'),
	            myOptions);			
			stations = :stationJson;
			walks = :walks;
			for(var walk in walks) {
				walk = walks[walk]
				source = walk[0]
				target = walk[1]
				console.log(source.name + " to " + target.name)
				a = new google.maps.LatLng(source.lat,source.lon);
				b = new google.maps.LatLng(target.lat,target.lon);
				distance = dist(a,b);
				line = new google.maps.Polyline({
					map: map,
					path: [a,b]
				})
			}
			for(var station in stations) {
				station = stations[station]
				station.lat = Number(station.lat);
				station.lon = Number(station.lon);
				console.log(station.name)
				station.getName = function() {
					return this.name;
				}
				var latLng = new google.maps.LatLng(station.lat,station.lon);
				
				var marker = new google.maps.Marker({
					position: latLng,
					icon: 'http://ryangravener.com/marker2.gif',
					map:map
				});
				var label = new Label({
				       map: map
			     });
			     label.bindTo('position', marker, 'position');
			     label.bindTo('text', station, 'name');
			}
			routes = [];
			for(var route in routes) {
				console.log(route)
			}
			//map.fitBounds(new google.maps.LatLngBounds(new google.maps.LatLng(:minLat,:minLon), new google.maps.LatLng(:maxLat,:maxLon)));
	      }
		  
	      google.maps.event.addDomListener(window, 'load', initialize);
	    </script>
	  </head>
	  <body>
	    <div id="map_canvas"></div>
	  </body>
	</html>
	"""
	stationJson = []
	walkJson = []
	akey = ""
	if showStations==True:
		for station in stations:
			akey = station
			stationJson.append(str(stations[station]))
		stationJson = "[" + ",".join(stationJson) + "]"
	else:
		stationJson = "[]"
	if showWalks==True:
		for source,target in walks:
			walkJson.append(str([stations[source],stations[target]]))
		#print walkJson
		walkJson = "[" + ",".join(walkJson) + "]"
	#print stationJson
#	if showRoutes==True:
#		data = []
#		for route in joinedRoutes.nodes():
#			begin = stations[route]
	html = html.replace(":minLat",str(minLat)).replace(":maxLat",str(maxLat)).replace(":minLon",str(minLon)).replace(":maxLon",str(maxLon)).replace(":stationJson",stationJson).replace(":walks",walkJson)
	f = open("target/"+station[0]+"_map.html","wb")
	f.write(html)
	f.close()


def prepend(x,p): return p+x

def greatCircleDistance(lat1,lon1,lat2,lon2):
	R = 6371;
	dLat = math.radians(lat2-lat1)
	dLon = math.radians(lon2-lon1)
	lat1 = math.radians(lat1)
	lat2 = math.radians(lat2)
	a = math.sin(dLat/2.0) * math.sin(dLat/2.0) + math.sin(dLon/2.0) * math.sin(dLon/2.0) * math.cos(lat1) * math.cos(lat2)
	c = 2*math.atan2(math.sqrt(a), math.sqrt(1-a))
	return R*c
def distance(stopa,stopb):
	return greatCircleDistance(stopa["lat"],stopa["lon"],stopb["lat"],stopb["lon"])*1000

def buildGraph(agencies) :
	import csv
	G = networkx.DiGraph()
	H = networkx.DiGraph()
	trips = {}
	routeInfo  = {}
	alltimes = {}
	mintimes = {}
	avgtimes = {}
	maxtimes = {}
	tripToStops = {};
	stopRoutes = {}
	stations = {}
	routes = {}
	doppleGanger = {}
	walks = []
	transferedges = {}
	routeToTrips = {}
	directions = {}
	departureVision = {}
	names = {}
	alternate_ids = {}
	for override,folder,prepend in agencies:
#		print folder,prepend
		conn = sqlite3.connect("target/" + sys.argv[1] + "/database_db")
		c = conn.cursor()
		namesReader = csv.reader(open(override+"/names.csv"))
		for row in namesReader:
			 names[row[0]] = row[1]
			 
		idsReader = csv.reader(open(override+"/webids.csv"))
		for row in idsReader:
			alternate_ids[row[0]] = row[1]
		departureVisionReader = csv.reader(open(override+"/departurevision.csv"))
		for row in departureVisionReader:
			departureVision[row[0]]  = row[1]
		stopReader = csv.reader(open(folder+"/stops.txt","rb"))
		headers = {}	
		for row in stopReader:
#			print "find stop headers"
			for i in range(len(row)):
				headers[row[i].lower().split(" ")[0]] = i
			break
		stopIdPos = headers["stop_id"]
		stopNamePos = headers["stop_name"]
		stopLatPos = headers["stop_lat"]
		stopLonPos = headers["stop_lon"]	
		minLat = sys.maxint
		minLon = sys.maxint
		maxLat = -sys.maxint-1
		maxLon = -sys.maxint-1
		for row in stopReader:
			stopId = prepend+row[stopIdPos]
			stopName = row[stopNamePos]
			stopLat = float(row[stopLatPos].split(" ")[0])
			stopLon = float(row[stopLonPos].split(" ")[0])
			if stopLat < minLat:
				minLat = stopLat
			if stopLat > maxLat:
				maxLat = stopLat
			if stopLon < minLon:
				minLon = stopLon
			if stopLon > maxLon:
				maxLon = stopLon			
			stop = {"id":stopId,"name":stopName,"lon":stopLon,"lat":stopLat}
			stations[stopId] = stop
		for stationA in stations:
			for stationB in stations:
				if stationA!=stationB and distance(stations[stationA],stations[stationB])==0:
					aId = stationA
					bId = stationB					
					doppleGanger[aId] = bId	
					print stations[stationA]["name"], stations[stationB]["name"]
		dopples = {}
		print len(doppleGanger)
		while len(doppleGanger)>0:
			for k in doppleGanger:
				dopples[k] = doppleGanger[k]
				del doppleGanger[k]
				del doppleGanger[dopples[k]]
				del stations[k]
				break
		for k in dopples:
			print k,dopples[k]
		routesReader = csv.reader(open(folder+"/routes.txt","rb"))
		headers = {}
		for row in routesReader:
			for i in range(len(row)):
				headers[row[i].lower().split(" ")[0]] = i
			break
		routeIdPos = headers["route_id"]
		routeNamePos = headers["route_long_name"]
		routeShortNamePos = headers["route_short_name"]		
		for row in routesReader:
			name = row[routeNamePos]
			routeId = row[routeIdPos]
			routeShortName = row[routeShortNamePos]
			if len(routeShortName)!=0:
				name = routeShortName
			routes[routeId] = {"label":name,"id":routeId}
			c.execute("INSERT INTO route(route_id,name) values(?,?)",(routeId,name))
		conn.commit()
		serviceReader = csv.reader(open(folder+"/calendar_dates.txt","rb"))
		headers = {}
		for row in serviceReader:
			for i in range(len(row)):
				headers[row[i].lower().split(" ")[0]] = i
			break
		serviceIdPos = headers["service_id"]
		date = headers["date"]
		exceptionType = headers["exception_type"]
		dateServices = []
#		print headers
		for row in serviceReader:
			if(len(row)!=3):
				continue
			service = {}
			service["id"] = row[serviceIdPos]
			service["date"] = row[date]
			service["exceptionType"] = row[exceptionType]
			dateServices.append(service)
			c.execute("INSERT INTO service(service_id,date) values(?,?)",(prepend+row[serviceIdPos],row[date]))
		conn.commit()
		# if os.path.exists(folder+"/calendar.txt"):
		# 	calServices = []
		# 	serviceReader = csv.reader(open(folder+"/calendar.txt","rb"))
		# 	headers = {}
		# 	for row in serviceReader:
		# 		for i in range(len(row)):
		# 			headers[row[i].lower().split(" ")[0]] = i
		# 		break
		# 	serviceIdPos = headers["service_id"]	
		# 	mondayPos = headers["monday"]
		# 	tuesdayPos = headers["tuesday"]
		# 	wednesdayPos = headers["wednesday"]
		# 	thursdayPos = headers["thursday"]
		# 	fridayPos = headers["friday"]
		# 	saturdayPos = headers["saturday"]
		# 	sundayPos = headers["sunday"]
		# 	startPos = headers["start_date"]
		# 	endPos = headers["end_date"]
		# 	newServices = []
		# 	for row in serviceReader:
		# 		startDate = datetime.strptime(row[startPos],"%Y%m%d")
		# 		endDate = datetime.strptime(row[endPos],"%Y%m%d")
		# 		print startDate,endDate
		# 		curr = startDate					
		# 		while(tk.mktime(curr.timetuple()) <= tk.mktime(endDate.timetuple())):
		# 			print curr						
		# 			day = curr.isoweekday()
		# 			if(day==1 and row[mondayPos]=="1"
		# 			or day==2 and row[tuesdayPos]=="1"
		# 			or day==3 and row[wednesdayPos]=="1"
		# 			or day==4 and row[thursdayPos]=="1"
		# 			or day==5 and row[fridayPos]=="1"
		# 			or day==6 and row[saturdayPos]=="1"
		# 			or day==7 and row[sundayPos]=="1"):
		# 				s = {}
		# 				s["id"] = row[serviceIdPos]
		# 				s["date"] = curr.strftime("%Y%m%d")
		# 				s["exceptionType"] = "1"
		# 				newServices.append(s)
		# 			curr += timedelta(days=1)
		# 		calServices.append(newServices)
		# for dateService in dateServices:
		# 	if dateService["exceptionType"]=="2":
		# 		print len(newServices)
		# 		todelete = []				
		# 		for k in range(0,len(newServices)):
		# 			check = newServices[k]					
		# 			if(check["id"]==dateService["id"] and check["date"]==dateService["date"]):
		# 				todelete.append(k)
		# 		print todelete
		# 		todelete.reverse()
		# 		print todelete
		# 		raw_input("wha?")
		# 		for k in range(0, len(todelete)):
		# 			print len(newServices)
		# 			raw_input("deleting "+str(todelete[k]))
		# 			del newServices[todelete[k]]
#		dateServices.extend(newServices)
#		for service in dateServices:
#			c.execute("INSERT INTO service(service_id,date) values(?,?)",(service["id"],service["date"]))
#		conn.commit()
#		print dateServices
#		raw_input("ok")
		tripReader = csv.reader(open(folder+"/trips.txt","rb"))
		headers = {}
		for row in tripReader:
			for i in range(len(row)):
				headers[row[i].lower().split(" ")[0]] = i
			break
		blockPos = -1
		blockPos = headers["block_id"] if "block_id" in headers else None
		routePos = headers["route_id"]
		directionPos = headers["direction_id"] if "direction_id" in headers else None
		shapePos = headers["shape_id"]
		servicePos = headers["service_id"]
		tripPos = headers["trip_id"]
		routePos = headers["route_id"]
		for row in tripReader:
			tripId = prepend+row[tripPos].split(" ")[0];
			trip = {}
			direction = None
			if directionPos!=None:
				direction = row[directionPos]
			if blockPos!=None:
				trip['block_id'] = row[blockPos]
			trip['id'] = tripId
			trip['direction'] = direction
			serviceId = prepend+row[servicePos].split(" ")[0]
			routeId = prepend+row[routePos].split(" ")[0]
			trip['service_id'] = serviceId
			trip['route_id'] = routeId
			routeInfo[routeId] = {}
			trips[tripId] = trip
		stopReader = csv.reader(open(folder+"/stop_times.txt","rb"))
		headers = {}
		for row in stopReader:
			for i in range(len(row)):
				headers[row[i].lower().split(" ")[0]] = i
			break
		stopPos = headers["stop_id"]
		tripIdPos = headers["trip_id"]
		sequencePos = headers["stop_sequence"]
		departPos = headers["departure_time"]
		arrivePos = headers["arrival_time"]
#		print "stop times"
		for row in stopReader:
			stop = {}
			stopId = findDopple(dopples,prepend+row[stopPos])
			tripId = prepend+row[tripIdPos]			
			sequence = int(row[sequencePos])
			if stopId not in routeInfo[trips[tripId]["route_id"]]:
				routeInfo[trips[tripId]["route_id"]][stopId] = {}
				routeInfo[trips[tripId]["route_id"]][stopId]["min"] = 5000
				routeInfo[trips[tripId]["route_id"]][stopId]["max"] = -1				
			mink = int(routeInfo[trips[tripId]["route_id"]][stopId]["min"])
			maxk = int(routeInfo[trips[tripId]["route_id"]][stopId]["max"])
			routeInfo[trips[tripId]["route_id"]][stopId]["min"] = min(mink,sequence)
			routeInfo[trips[tripId]["route_id"]][stopId]["max"] = max(maxk,sequence)
			stop['id'] = stopId
			arriveTime = row[arrivePos]
			departTime = row[departPos]
			stop['depart'] = departTime
			stop['arrive'] = arriveTime
			if tripId not in tripToStops:
				stops = []
				tripToStops[tripId] = stops
			else:
				stops = tripToStops[tripId]
			stops.insert(int(sequence),stop)
	print stations
	for tripId in tripToStops:
		stops = tripToStops[tripId]
		trip = trips[tripId]
		route = trip["route_id"]
		if route not in routeToTrips:
			routeToTrips[route] = []
		routeToTrips[route].append(trip)
		source = None
		day = 1
		lhour = None
		for position in range(len(stops)):
			target = stops[position]
			if source != None:
				depart = source['depart'].split(":")
				arrive = target['arrive'].split(":")
				dhour = int(depart[0])
				dmin = int(depart[1])
				dsec = int(depart[2])
				ahour = int(arrive[0])
				amin = int(arrive[1])
				asec = int(arrive[2])
				if dhour>=24:
					day = dhour / 24 + 1
				#arrive = datetime(1970,1,1,remainder,minute,second)
				departTime = datetime(1970,1,day,dhour%24,dmin,dsec)
				if ahour>=24:
					day = ahour / 24 + 1
				arriveTime = datetime(1970,1,day,ahour%24,amin,asec)
				key = source['id'] + ":" + target['id']
				if source['id'] not in alltimes:
					alltimes[source['id']] = {}
				if target['id'] not in alltimes[source['id']]:
					alltimes[source['id']][target['id']] = []
				alltimes[source['id']][target['id']].append(tk.mktime(arriveTime.timetuple())-tk.mktime(departTime.timetuple()))
#				if alltimes[key][len(alltimes[key])-1] < 0 :
#					print "arrive",arriveTime,"-","depart",departTime
				source['name'] = stations[source['id']]['name']
				target['name'] = stations[target['id']]['name']
				source['label'] = stations[source['id']]['name']
				target['label'] = stations[target['id']]['name']
				
				G.add_node(source['id'],source)
				G.add_node(target['id'],target)
#				if(source["id"][0]=="n" and target["id"][0]=="m"):
#					k = raw_input("say what?")
				
				if G.has_edge(source['id'],target['id'])==False:
					# if source['id']=="n103" and target['id']=="n32906":
					# 	print source,target,"\ntripid",tripId
					# 	input = raw_input("this should not be possible")
					#G.add_edge(source['id'],target['id'],{"direction":direction})
					G.add_edge(source['id'],target['id'])
				if "direction" in trip:
					if source['id'] not in directions:
						directions[source['id']] = {}
					if target['id'] not in directions[source['id']]:
						directions[source['id']][target['id']] = []
					if trip["direction"] not in directions[source['id']][target['id']]:
						directions[source['id']][target['id']].append(trip["direction"])
#				if "routes" not in G[source['id']][target['id']]:
#					G[source['id']][target['id']]["routes"] = {};					
				routeId = trips[tripId]['route_id']
#				G[source['id']][target['id']]["routes"][routeId] = True
				if source['id'] not in stopRoutes:
					stopRoutes[source['id']] = []
				if target['id'] not in stopRoutes:
					stopRoutes[target['id']] = []
				if routeId not in stopRoutes[source['id']]:
					stopRoutes[source['id']].append(routeId)
				if routeId not in stopRoutes[target['id']]:
					stopRoutes[target['id']].append(routeId)
				H.add_node(routeId,routes[routeId])	
			else:
				lhour = target['depart'].split(":")[0]
			source = target
			#				G.add_edge(source,target,{"direction":trips[tripId]['direction'],"route":trips[tripId]['route_id']})
	# for routeId in routeInfo:
	# 	raw_input("u mad bro?")
	# 	mins = {}
	# 	maxs = {}
	# 	good = []
	# 	allnodes = set()
	# 	trips = routeToTrips[routeId]
	# 	actualroute = []
	# 	longest = []
	# 	for trip in trips:
	# 		stops = tripToStops[trip["id"]]
	# 		if len(stops) > len(longest):
	# 			longest = stops
	# 		for stop in stops:
	# 			stopId = stop["id"]
	# 			allnodes.add(stopId)
	# 	if len(longest)==len(allnodes):
	# 		for node in longest:
	# 			actualroute.append(node["id"])
	# 	else:
	# 		takencareof = []
	# 		for node in longest:				
	# 			for node in longest:
	# 				takencareof.append(node["id"])
	# 		print "intersection",set(takencareof) & allnodes
	# 		print "longest", len(takencareof), "all",len(allnodes)
	# 	print "allnodes",allnodes		
	# 	print "actualroute",actualroute
		
			
#	print "trips"
#	networkx.readwrite.gml.write_gml(G,"before_stations.gml")
	# for routeId in routeToTrips:
	# 	for trip in routeToTrips[routeId]:			
	# 		stops = tripToStops[trip["id"]]
						
	print "strongly connected stations: ",networkx.algorithms.components.strongly_connected.number_strongly_connected_components(G)
	print "strongly connected routes: ",networkx.algorithms.components.strongly_connected.number_strongly_connected_components(H)
#	draw(G,"stations_before.dot")
#	draw(H,"route_before.dot")
#	print "transfers"
	for override,folder,prepend in agencies:
		transferFile = folder+"/transfers.txt"
		if os.path.exists(transferFile) :
			#print "transfers.txt exists"
			transferReader = csv.reader(open(folder+"/transfers.txt","rb"))
			headers = {}
			for row in transferReader:
				for i in range(len(row)):
					headers[row[i].lower().split(" ")[0]] = i
				break
			fromStopIdPos = headers["from_stop_id"]
			toStopIdPos = headers["to_stop_id"]
			transferTypePos = headers["transfer_type"]
			minTransferTimePos = -1;
			if "min_transfer_time" in headers:
				minTransferTimePos = headers["min_transfer_time"]
			for row in transferReader:
				fromStopId = findDopple(dopples,prepend+row[fromStopIdPos])
				toStopId = findDopple(dopples,prepend+row[toStopIdPos])
#				print fromStopId,toStopId
				transferType = row[transferTypePos]
				minTransferTime = 0
				if minTransferTimePos>-1:
					minTransferTime = row[minTransferTimePos]
				if G.has_edge(fromStopId,toStopId)==False and fromStopId!=toStopId:
#					if fromStopId=="n103" and toStopId=="n32906":
#						print source,target,"\ntripid",tripId
#						input = raw_input("this should not be possible")					
					G.add_edge(fromStopId,toStopId)
					if "routes" not in G[fromStopId][toStopId]:
						G[fromStopId][toStopId]["routes"] = {};
					G[fromStopId][toStopId]["routes"]["walk"+transferType] = minTransferTime
					walks.append((fromStopId,toStopId))
					if fromStopId not in transferedges:
						transferedges[fromStopId] = {}
					transferedges[fromStop][toStopId] = minTransferTime
#					print fromStopId,toStopId,minTransferTime
					c.execute("INSERT INTO transfer_edge(source,target,duration) values(?,?,?)",(fromStopId,toStopId,minTransferTime))
			conn.commit()				
		else:
			count = 0
			for station in stations:
				station = stations[station]
				for station2 in stations:
					station2 = stations[station2]
					if station==station2: continue
					dist = distance(station,station2)
					# 1.389 meters per second * 180 seconds (3 mins)  this may need to be upped but it equates to ~ 250
					fromId = station['id']
					toId = station2['id']
					routeIntersection = []
	#				print fromId, toId, stopRoutes[fromId], stopRoutes[toId]
					routeIntersection = []
					if fromId in stopRoutes and toId in stopRoutes:
						routeIntersection = list(set(stopRoutes[fromId]) & set(stopRoutes[toId]))
	#				print routeIntersection
					if dist>0 and dist < 250 and G.has_edge(fromId,toId)==False and len(routeIntersection)==0:				
						count+=1
#						print dist, station["name"], "walkable to", station2["name"]						
						G.add_edge(fromId,toId)
						if fromId not in stopRoutes or toId not in stopRoutes:
							continue;
						aRoutes = stopRoutes[fromId]
						bRoutes = stopRoutes[toId]
#						print aRoutes,"\n",bRoutes
						for aRoute in aRoutes:
							for bRoute in bRoutes:
#								if (aRoute=="n18" or bRoute=="n18"):
#									print "special: ", aRoute,bRoute
								if aRoute!=bRoute:
									H.add_node(aRoute)
									H.add_node(bRoute)
									if H.has_edge(aRoute,bRoute)==False:
										H.add_edge(aRoute,bRoute,{"weight":int(round(dist/1.389))})
#										print aRoute, "to", bRoute
#						if "routes" not in G[fromId][toId]:
#							G[fromId][toId]["routes"] = {};
#						G[fromId][toId]["routes"]["walk2"] = round(dist*1.389)
						walks.append((fromId,toId))
						c.execute("INSERT INTO transfer_edge(source,target,duration) values(?,?,?)",(fromId,toId,int(round(dist/1.389))))
			conn.commit()
			metersPerSecond = 1.389
	print "ok"
	for a in alltimes:
		print "alright"
		for b in alltimes[a]:
			print "here"
			miny = sys.maxint
			maxy = 0
			total = 0.0
			for time in alltimes[a][b]:
				total = total + time
				if miny>time:
					miny = time		
				if time>maxy:
					maxy = time
			if a not in mintimes:
				mintimes[a] = {}
			mintimes[a][b] = miny
			if a not in avgtimes:
				avgtimes[a] = {}		
			avgtimes[a][b] = total / float(len(alltimes[a][b]))
			if a not in maxtimes:
				maxtimes[a] = {}
			maxtimes[a][b] = maxy
			#print mintimes[key], avgtimes[key]
			G[a][b]['weight'] = mintimes[a][b]
#			G[a][b] = avgtimes[key]
#			G[a][b] = maxtimes[key]
	for tripId in tripToStops:
		trip = trips[tripId]
		stops = tripToStops[tripId]
		route = trip['route_id']
		weight = 0
		lastStop = None
		for position in range(len(stops)):
			stop = stops[position]
			if lastStop!=None:
				time = mintimes[lastStop["id"]][stop["id"]]
				weight = weight + time
			lastStop = stop
			stopId = stop['id']
			sRoutes = stopRoutes[stopId]
			if len(sRoutes)>1:
				for sRoute in sRoutes:
					if route != sRoute:
						H.add_node(route)
						H.add_node(sRoute)
						if(H.has_edge(route,sRoute)==False):
							H.add_edge(route,sRoute,{"weight":weight})
	print "strongly connected stations: ",networkx.algorithms.components.strongly_connected.number_strongly_connected_components(G)
	print "strongly connected routes: ",networkx.algorithms.components.strongly_connected.number_strongly_connected_components(H)
#	draw(G,"stations_after.dot")
#	draw(H,"routes_after.dot")
#	raw_input("k?")			
#	print "all pairs shortest paths routes"
	paths = networkx.algorithms.shortest_paths.weighted.all_pairs_dijkstra_path(H)
	routePaths = paths
	for source in paths:
		for target in paths[source]:
			nodes = ",".join(paths[source][target][1:len(paths[source][target])-1])
			c.execute("INSERT INTO shortest_route_path(source,target,nodes,hop_count) values(?,?,?,?)",(source,target,nodes,len(paths[source][target])))
		conn.commit()
	for stationKey in stations:
		if stationKey not in stopRoutes:
			print "not in",stationKey
		else:
			for route in stopRoutes[stationKey]:
				c.execute("INSERT INTO station_route(station,route) values(?,?)",(stationKey,route))
		conn.commit()
				
	polymap(minLat,minLon,maxLat,maxLon,stations,walks,None,True)
#	print "trips storage"	
	tripCount = {}
	for tripId in tripToStops:
		trip = trips[tripId]
		ids = set()
		firstStation = tripToStops[tripId][0]
		lastStation = firstStation
		ids.add(lastStation["id"])
		for id in range(len(tripToStops[tripId])):       
			stop = tripToStops[tripId][id]
			ids.add(stop["id"])			
			depart = stop['depart'].split(":")
			arrive = stop['arrive'].split(":")
			
			dhour = int(depart[0])
			dmin = int(depart[1])
			dsec = int(depart[2])
			ahour = int(arrive[0])
			amin = int(arrive[1])
			asec = int(arrive[2])
			if dhour>=24:
				day = dhour / 24 + 1
			#arrive = datetime(1970,1,1,remainder,minute,second)
			departTime = datetime(1970,1,day,dhour%24,dmin,dsec)
			if ahour>=24:
				day = ahour / 24 + 1
			arriveTime = datetime(1970,1,day,ahour%24,amin,asec)
			blockId = None
			if "block_id" in trip:
				blockId = trip["block_id"]
			c.execute("INSERT INTO nested_trip(lft,rgt,trip_id,service_id,stop_id,depart,arrive,block_id,route_id) values(?,?,?,?,?,?,?,?,?)",(id+1,len(tripToStops[tripId]),tripId,trip["service_id"],stop["id"],stop["depart"],stop["arrive"],blockId,trip["route_id"]))
			lastStation = stop
		conn.commit()
		for id in ids:
			for ids2 in ids:
				if(id!=ids2):
					if id not in tripCount:
						tripCount[id] = {}
					if ids2 not in tripCount[id]:
						tripCount[id][ids2] = {"count":1,"trip":[tripId]}
					else:
						tripCount[id][ids2]["count"] = tripCount[id][ids2]["count"]+1
						tripCount[id][ids2]["trip"].append(tripId)
	for id in tripCount:
		for ids2 in tripCount[id]:
			count = tripCount[id][ids2]["count"]
			tripId = tripCount[id][ids2]["trip"]
			if count <= 4:
				print stations[id]["name"],stations[ids2]["name"],count,tripId
	patharray = [networkx.algorithms.shortest_paths.weighted.all_pairs_dijkstra_path(G),networkx.algorithms.shortest_paths.unweighted.all_pairs_shortest_path(G)]

	for level in range(0,1):
		paths = patharray[level]
		for stationA in stations:
			for stationB in stations:
				if stationA in paths:
					if stationB in paths[stationA]:
						shortestPath = paths[stationA][stationB]
						pathLength = len(shortestPath)
						if(pathLength>1 and stationA in stopRoutes and stationB in stopRoutes):
							aRoutes = stopRoutes[stationA]
							bRoutes = stopRoutes[stationB]
							minRouteLength = sys.maxint
							minRoute = None
							for aRoute in aRoutes:
								for bRoute in bRoutes:
									if aRoute in routePaths and bRoute in routePaths[aRoute]:
										shortestRoutePath = routePaths[aRoute][bRoute]
										if minRouteLength > len(shortestRoutePath):
											minRoute = shortestRoutePath
											minRouteLength = len(minRoute)
	#						print minRoute								
							currRoute = minRoute[0]
							currRoutePos = -1
							stationsToUse = []
							error = False
							lastStation = stationA
							startStation = lastStation
							dirs = []
							stationsToUse = []
							countAfter = 0
							for currStation in shortestPath:
								station = currStation							
								isTransferEdge = lastStation in transferedges and station in transferedges[lastStation]
	#							print "transfer edge?",isTransferEdge
	#							isTransferEdge = hasTransferEdge(c,lastStation["stop_id"],station["stop_id"])
								if isTransferEdge:
									stationsToUse.append((startStation,lastStation))
									startStation = station
									countAfter = 0							
								isOnRoute = currRoute in stopRoutes[station]
	#							print "on route?",isOnRoute
								#hasEdge = G.has_edge(lastStation,station) and G.has_edge(station,lastStation)
								#print hasEdge
	#							isOnRoute = onRoute(c,station["stop_id"],currRoute)
	#							print station
								if isOnRoute==False:
									currRoutePos = currRoutePos+1
									if currRoutePos> minRouteLength-1:
										print stations[stationA]["name"],"->",stations[stationB]["name"],"\n",minRoute
										error = True
										break;
									currRoute = minRoute[currRoutePos]
	#								print "new route:",currRoute,routes[currRoute]["label"]
									countAfter = 0
									if isTransferEdge==False:
										stationsToUse.append((startStation,lastStation))									
										startStation = lastStation
								else:
									if lastStation!=station:
										if lastStation not in directions or station not in directions[lastStation]:
											print "no directions", stations[lastStation],"to",stations[station]
										else:										
											ndirs = directions[lastStation][station]
											intersection = set(ndirs).intersection(set(dirs))
											dirs = ndirs
	#										print intersection,ndirs,"--",dirs
											if len(intersection) == 0 and countAfter>0:
												stationsToUse.append((startStation,lastStation))
												startStation = lastStation
												countAfter = 0
											else:
												countAfter = countAfter+1
								lastStation = station
							if startStation!=lastStation:							
								stationsToUse.append((startStation,lastStation))
							if error==False:
								for index in range(0, len(stationsToUse)):
									source = stationA
									target = stationB
									a = stationsToUse[index][0]
									b = stationsToUse[index][1]
									c.execute("INSERT INTO schedule_path(source,target,a,b,level,sequence) values(?,?,?,?,?,?)",(source,target,a,b,level,index))
							# indent = 1
							# print stations[stationA]["name"],stations[stationB]["name"]
							# for x,y in stationsToUse:
							# 	indents = ""
							# 	for k in range(1,indent+1):
							# 		indents = indents+"\t"	
							# 	print indents,stations[x]["name"],"->",stations[y]["name"]	
							# 	indent = indent+1					
							# print ""
			conn.commit()
	connectionsReader = csv.reader(open(override+"/connections.csv"))
	print "processing connections"
	c.execute("create index foo on schedule_path(source,target)");
	for row in connectionsReader:
		depart = row[0]
		arrive = row[len(row)-1]
		# print "depart:",depart,"arrive:",arrive,"len:",len(row)
		before = datetime.now().microsecond
		c.execute("SELECT level from schedule_path where source=? and target=? order by level desc limit 1",(depart,arrive))
		pos = 0
		row2rows = c.fetchall()
		after = datetime.now().microsecond
		print after-before
		for row2 in row2rows:
			print "finding max level for:"+depart+","+arrive+":",row2			
			pos = int(row2[0])+1					
		count = 0
		for i in range(0,len(row)-1):			
			transfer = row[i]
			next = row[i+1]
			print "source:",depart,"target:",arrive,"a:",transfer,"b:",next,"level:",pos,"sequence:",i
			tuples = (depart,arrive,transfer,next,pos,i)
			# //print tuples
			c.execute("INSERT INTO schedule_path(source,target,a,b,level,sequence) values(?,?,?,?,?,?)",tuples)
	conn.commit()			
	c.execute("drop index foo")
#	c.execute("delete from schedule_path where (source=? and target=?) or (target=? and source=?)",("51","105","51","105"))
#	c.execute("delete from schedule_path where (source=? and target=?) or (target=? and source=?)",("52","105","52","105"))
	# c.execute("INSERT INTO schedule_path(source,target,a,b,level,sequence) values(?,?,?,?,?,?)",("51","105","51","38174",0,0))			
	# c.execute("INSERT INTO schedule_path(source,target,a,b,level,sequence) values(?,?,?,?,?,?)",("51","105","38174","105",0,1))
	# c.execute("INSERT INTO schedule_path(source,target,a,b,level,sequence) values(?,?,?,?,?,?)",("105","51","105","38174",0,0))			
	# c.execute("INSERT INTO schedule_path(source,target,a,b,level,sequence) values(?,?,?,?,?,?)",("105","51","38174","51",0,1))
	# c.execute("INSERT INTO schedule_path(source,target,a,b,level,sequence) values(?,?,?,?,?,?)",("52","105","52","38174",0,0))			
	# c.execute("INSERT INTO schedule_path(source,target,a,b,level,sequence) values(?,?,?,?,?,?)",("52","105","38174","105",0,1))
	# c.execute("INSERT INTO schedule_path(source,target,a,b,level,sequence) values(?,?,?,?,?,?)",("105","52","105","38174",0,0))			
	# c.execute("INSERT INTO schedule_path(source,target,a,b,level,sequence) values(?,?,?,?,?,?)",("105","52","38174","52",0,1))
	# c.execute("INSERT INTO schedule_path(source,target,a,b,level,sequence) values(?,?,?,?,?,?)",("37953","150","37953","38174",0,0))
	# c.execute("INSERT INTO schedule_path(source,target,a,b,level,sequence) values(?,?,?,?,?,?)",("37953","150","38174","150",0,1))
	# c.execute("INSERT INTO schedule_path(source,target,a,b,level,sequence) values(?,?,?,?,?,?)",("150","37953","150","38174",0,0))
	# c.execute("INSERT INTO schedule_path(source,target,a,b,level,sequence) values(?,?,?,?,?,?)",("150","37953","38174","37953",0,1))
	# hoboken 39348
	# bloomfield 19
	# newark broad 106
	# c.execute("INSERT INTO schedule_path(source,target,a,b,level,sequence) values(?,?,?,?,?,?)",("39348","19","39348","106",0,0))
	# c.execute("INSERT INTO schedule_path(source,target,a,b,level,sequence) values(?,?,?,?,?,?)",("39348","19","106","19",0,1))
	# c.execute("INSERT INTO schedule_path(source,target,a,b,level,sequence) values(?,?,?,?,?,?)",("19","39348","19","106",0,0))
	# c.execute("INSERT INTO schedule_path(source,target,a,b,level,sequence) values(?,?,?,?,?,?)",("19","39348","106","39348",0,1))				
	conn.commit()
	for stop in stations:
		stop = stations[stop]
		dv = None
		if stop["id"] in departureVision:
			dv = departureVision[stop["id"]]
		name = makePretty(stop["name"])
		if stop["id"] in names:
			name = names[stop["id"]]
		altId = None
		if stop["id"] in alternate_ids:
			altId = alternate_ids[stop["id"]]
		c.execute("INSERT INTO stop(name,stop_id,lat,lon,departure_vision,alternate_id) values(?,?,?,?,?,?)",(name	,stop["id"],stop["lat"],stop["lon"],dv,altId))
	conn.commit()
	return (prepend,G,H,stations,walks,stopRoutes,tripToStops)
shutil.rmtree("target",True)
os.makedirs("target/"+sys.argv[1])
conn = sqlite3.connect("target/"+sys.argv[1]+"/database_db")
c = conn.cursor()
c.execute("""CREATE TABLE nested_trip (
        id INT AUTO_INCREMENT PRIMARY KEY,
		trip_id VARCHAR(20) NOT NULL,
        stop_id VARCHAR(20) NOT NULL,
		service_id VARCHAR(20) NOT NULL,
		route_id VARCHAR(20) NOT NULL,
		depart VARCHAR(10),
		arrive VARCHAR(10),
		block_id VARCHAR(10),
        lft INT NOT NULL,
        rgt INT NOT NULL
);""")
c.execute("""CREATE TABLE route (
		route_id VARCHAR(20) NOT NULL,
		name VARCHAR(100),
		color integer
);""")
c.execute("""CREATE TABLE schedule_path (
		source VARCHAR(20) NOT NULL,
        target VARCHAR(20) NOT NULL,
		sequence INTEGER,
		level INTEGER,	
		a VARCHAR(20) NOT NULL,
		b VARCHAR(20) NOT NULL
);""")
#c.execute("""CREATE INDEX abc ON nested_trip(trip_id,stop_id,lft);""")
c.execute("""CREATE TABLE shortest_path (
		source VARCHAR(20) NOT NULL,
        target VARCHAR(20) NOT NULL,
		nodes TEXT
);""")
c.execute("""CREATE TABLE nested_shortest_path (
		id INT AUTO_INCREMENT PRIMARY KEY,
		lft INT NOT NULL,
		rgt INT NOT NULL,
		nodes TEXT
);""")

c.execute("""CREATE TABLE shortest_route_path (
		source VARCHAR(20) NOT NULL,
        target VARCHAR(20) NOT NULL,
		nodes TEXT,
		hop_count integer
);""")
c.execute("""CREATE TABLE station_route (
		station VARCHAR(20) NOT NULL,
		route VARCHAR(20)
);""")
c.execute("""CREATE TABLE stop (
		stop_id VARCHAR(20) NOT NULL,
		name varchar(150),
        lat integer,
		lon integer,
		departure_vision varchar(5),
		alternate_id varchar(100)
);""")
c.execute("""CREATE TABLE stop_abbreviations (
		abbreviation VARCHAR(20) NOT NULL,
		total integer
);""")
c.execute("""CREATE TABLE service (
		service_id VARCHAR(20) NOT NULL,
		date varchar(8)
);""")
c.execute("""CREATE TABLE transfer_edge (
		source varchar(20) NOT NULL,
		target varchar(20) NOT NULL,
		duration integer
);""")
c.execute("""CREATE TABLE agency_transfer_edge (
		agency_source varchar(2) NOT NULL,
		agency_target varchar(2) NOT NULL,
		source varchar(20) NOT NULL,
		target varchar(20) NOT NULL,
		duration integer
);""")
c.execute("""CREATE TABLE IF NOT EXISTS fares (
		source VARCHAR(20) NOT NULL,
        target VARCHAR(20) NOT NULL,
		adult real,
		child real,
		senior real,
		disabled real,
		weekly real,
		ten_trip real,
		monthly real,
		student_monthly real,
		adult_offpeak real,
		weekly_offpeak real,
		ten_trip_offpeak real,
		onboard_offpeak real,
		onboard real
);""")


conn.commit()
results = [buildGraph([("overrides/"+sys.argv[1],"gtfs/"+sys.argv[1],"")])]
conn2 = sqlite3.connect("overrides/"+sys.argv[1]+"/fares.db")
conn.row_factory = dict_factory
c2 = conn2.cursor()
c2.execute("select source,target,adult,child,senior,disabled,weekly,ten_trip,monthly,student_monthly,adult_offpeak,weekly_offpeak,ten_trip_offpeak,onboard_offpeak,onboard from fares")
for r in c2.fetchall():
	c.execute("""insert into fares(source,target,adult,child,senior,disabled,weekly,ten_trip,
		monthly,student_monthly,adult_offpeak,weekly_offpeak,ten_trip_offpeak,onboard_offpeak,onboard) 
		values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)""",r)
conn.commit()
# ,buildGraph([("gtfs/septa","s")]),buildGraph([("gtfs/lirr","l")]),buildGraph([("gtfs/metro-north","m")])]
agencywalks = []
D = networkx.DiGraph()
for agency,G,H,stations,walks,stopRoutes,tripToStops in results:
	for agencyp,Gp,Hp,stationsp,walksp,stopRoutesp,tripToStopsp in results:
		if G==Gp:
			continue
		for station in stations:
			station = stations[station]
			for stationp in stationsp:
				stationp = stationsp[stationp]
				dist = distance(station,stationp)
				if dist < 250:
#					print dist,station["name"],stationp["name"]
					aId = station["id"]
					bId = stationp["id"]
					D.add_node(agency)
					D.add_node(agencyp)
					if D.has_edge(agency,agencyp)==False:
						D.add_edge(agency,agencyp)						
					c.execute("INSERT INTO agency_transfer_edge(agency_source,agency_target,source,target,duration) values(?,?,?,?,?)",(agency,agencyp,station["id"],stationp["id"],int(round(dist*1.389))))
		conn.commit()				
paths = networkx.algorithms.shortest_paths.unweighted.all_pairs_shortest_path(D)
#print paths
