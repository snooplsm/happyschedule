import networkx as networkx
from datetime import datetime
from datetime import timedelta
import time as tk
import sys
import sqlite3
import os
import shutil
import math
import csv

stationNameToZone = {}
zonesReader = csv.reader(open("zones.csv"))
zones = {}
fareHeader = {}
fares = {}
stations = {}
stopReader = csv.reader(open("../../gtfs/lirr/stops.txt","rb"))
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
	stopId = row[stopIdPos]
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

for zoneRow in zonesReader:
	for i in range(len(zoneRow)):
		zones[i] = zoneRow[i];
	break
# print zones
faresReader = csv.reader(open("fares.csv"))
for zoneRow in zonesReader:
	for i in range(len(zoneRow)):
		zone = zoneRow[i]
		stationNameToZone[zone] = zones[i]
# //print stationNameToZone
for fareRow in faresReader:
	for i in range(len(fareRow)):
		fareHeader[i] = fareRow[i]
	break
for fareRow in faresReader:
	depart = fareRow[0]
	if depart not in fares:
		fares[depart] = {}
	for i in range(2,len(fareRow)):
		arrive = fareHeader[i]
		if arrive not in fares[depart]:
			fares[depart][arrive]={}
		fares[depart][arrive][fareRow[1]] = fareRow[i]
# print fares
conn = sqlite3.connect("fares.db")
c = conn.cursor()
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
c.execute("delete from fares")
for departId in stations:
	depart = stations[departId]
 	departZone = stationNameToZone[depart["name"]]
	# print stationNameToZone[depart["name"]]
	for arriveId in stations:
		if departId == arriveId:
			continue
		arrive = stations[arriveId]
		arriveZone = stationNameToZone[arrive["name"]]
		# print departZone,arriveZone
		if departZone not in fares and arriveZone not in fares[departZone]:
			continue
		fare = fares[departZone][arriveZone]
		adult = fares[departZone][arriveZone]["adult"]
		senior = fares[departZone][arriveZone]["senior"]
		disabled = fares[departZone][arriveZone]["disabled"]
		weekly = fares[departZone][arriveZone]["weekly"]
		ten_trip = fares[departZone][arriveZone]["ten_trip"]
		monthly = fares[departZone][arriveZone]["monthly"]		
		adult_offpeak = fares[departZone][arriveZone]["adult"]
		weekly_offpeak = fares[departZone][arriveZone]["adult"]					
		ten_trip_offpeak = fares[departZone][arriveZone]["ten_trip_offpeak"]
		onboard = fares[departZone][arriveZone]["onboard"]		
		onboard_offpeak = fares[departZone][arriveZone]["onboard_offpeak"]	
		tupl = (departId,arriveId,adult,None,senior,disabled,weekly,ten_trip,monthly,None,adult_offpeak,weekly_offpeak,ten_trip_offpeak,onboard_offpeak,onboard)	
		c.execute("""insert into fares(source,target,adult,child,senior,disabled,weekly,ten_trip,
			monthly,student_monthly,adult_offpeak,weekly_offpeak,ten_trip_offpeak,onboard_offpeak,onboard) 
			values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)""",tupl)

conn.commit()
c.close()
conn.close()