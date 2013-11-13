# NEW_YORK_IDS.add("8");//PENN STATION
# 		NEW_YORK_IDS.add("9");//WOODSIDE
# 		NEW_YORK_IDS.add("1");//LONG ISLAND CITY
# 		NEW_YORK_IDS.add("2");//HUNTERSPOINT AVE
# 		NEW_YORK_IDS.add("17");//METS-willet
# 		NEW_YORK_IDS.add("10");//forrest hills
# 		NEW_YORK_IDS.add("11");//kew gardens
# 		NEW_YORK_IDS.add("15");//jamaica
# 		NEW_YORK_IDS.add("14");//east new york
# 		NEW_YORK_IDS.add("13");//nostrand ave
# 		NEW_YORK_IDS.add("12");//atlatnci terminal
		
NYIDS = {"8":"","9":"","1":"","2":"","17":"","10":"","11":"","15":"","14":"","13":"","12":"	"}

HOLIDAYS = """
New Year's Day,01/01/2014,01/01/2015,01/01/2016
Martin Luther King Jr. Day,01/20/2014,01/19/2015
President's Day,02/17/2014,02/16/2015
Memorial Day,05/26/2014,05/25/2015
Independence Day,07/04/2014,07/04/2015
Labor Day,09/01/2014,09/07/2015
Thanksgiving Day,11/28/2013,11/27/2014
Black Friday,11/29/2013,11/28/2014
Christmas Day,12/25/2013,12/25/2014
"""
		
from datetime import datetime

LINES = HOLIDAYS.split("\n")
HOLIDAYS = {}
for x in LINES:
	hd = x.split(",")
	for k in hd:
		print k
		HOLIDAYS[k] = k

def isPeak(tripId,trip,date):
	first = trip[0]
	weekday = date.isoweekday()
	print tripId[-4:]
	if tripId[-4:]=="161800":
		raw_input("foo")
	if weekday>5:
		if tripId[-4:]=="161800":
			raw_input("weekend")
		return "offpeak"
	print "date " + date.strftime("%m/%d/%y")
	if date.strftime("%m/%d/%y") in HOLIDAYS:
		print "holiday"
		if tripId[-4:]=="161800":
			raw_input("holiday")
		return "offpeak"
	firstEarliest = None
	earliest = None
	for x in trip:
		if x["id"] in NYIDS:
			earliest = x
			break
	if earliest!=None:
		try:
			departTime = datetime.strptime(earliest["depart"],"%H:%M:%S")
			hour = departTime.hour
			minute = departTime.minute
		except:
			hour = 1
			minute = 0
		if tripId[-4:]=="161800":
			raw_input("earliest is " + earliest["name"] + "," + earliest["depart"])
		# print "NY",first["depart"],hour,minute
		if hour>=16 and hour<=20:
			can = True
			if hour==20:
				can = minute==0
			if can:
				if tripId[-4:]=="161800":
					raw_input("hour>=16 and hour<=20")
				return "peak"
	firstEarliest = earliest
	earliest = trip[-1:][0]
	# for x in reversed(trip):
	# 	earliestPos = earliestPos+1
	# 	if x["id"] in NYIDS:
	# 		earliest = x
	# 	else:
	# 		if earliest!=None:
	# 			break
	if earliest==None:
		return "offpeak"	
	if tripId[-4:]=="161800":
		raw_input("earliest is " + earliest["name"] + "," + earliest["arrive"])		
	try:
		arriveTime = datetime.strptime(earliest["arrive"],"%H:%M:%S")
		hour = departTime.hour
		minute = departTime.minute
	except:
		hour = 1
		minute = 0
	print earliest
	print str(hour) + " : " + str(minute) 
	if earliest["id"] in NYIDS:		
		if hour >=6 and hour<=10:
			can = True
			if hour==10:
				can = min==0
			if can:
				if tripId[-4:]=="161800":
					raw_input("hour>=6 and hour<=10")
				return "peak"	
		if tripId[-4:]=="161800":
			raw_input("offpeak")		
	return "offpeak"
