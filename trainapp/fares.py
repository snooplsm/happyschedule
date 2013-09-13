import httplib
import sqlite3
from BeautifulSoup import BeautifulSoup

def dict_factory(cursor, row):
    d = {}
    for idx, col in enumerate(cursor.description):
        d[col[0]] = row[idx]
    return d


url = "http://www.njtransit.com/sf/sf_servlet.srv?hdnPageAction=TrainSchedulesFrom&selOrigin=%s_NEC&selDestination=%s_NEC&OriginDescription=Chatham&DestDescription=Maplewood&datepicker=%s"
conn = sqlite3.connect("target/database_db")
conn.row_factory = dict_factory
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
		student_monthly real
);""")
# soup = BeautifulSoup(html)
# s = soup.findAll('td',{"align":"right"})
# headers = {"adult":0,"child":1,"senior":1,"disabled":1,"ten_trip":3,"weekly":2,"monthly":4,"student_monthly":5}
# for header in headers:
# 	k = s[headers[header]].span.text
# 	print header,k[1:]
c.execute("select a,b from schedule_path where a not in (select a from transfer_edge where a=source and b=target) and b not in (select b from transfer_edge where a=source and b=target) group by a,b")
count = 0
pairs = {}
for r in c.fetchall():
	if r["a"] not in pairs:
		pairs[r["a"]] = {}
	pairs[r["a"]][r["b"]] = None
	count=count+1
print count
count = 0
headers = {"adult":0,"child":1,"senior":1,"disabled":1,"ten_trip":3,"weekly":2,"monthly":4,"student_monthly":5}
headers2 = {"adult":0,"child":1,"senior":1,"disabled":1,,"monthly":2,}
for a in pairs:
	for b in pairs[a]:
		url = "www.njtransit.com"
		h = httplib.HTTPConnection(url)
		url = url + "/sf/sf_servlet.srv?hdnPageAction=TrainSchedulesFrom&selOrigin=" + a + "_NEC&selDestination=" + b + "_NEC&OriginDescription=Chatham&DestDescription=Maplewood&datepicker=12%2F29%2F2011"
		print url
		h.request("GET", "/sf/sf_servlet.srv?hdnPageAction=TrainSchedulesFrom&selOrigin=" + a + "_NEC&selDestination=" + b + "_NEC&OriginDescription=Chatham&DestDescription=Maplewood&datepicker=12%2F29%2F2011",{},{"User-Agent":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.63 Safari/535.7"})
		r = h.getresponse()
		data = r.read()
		h.close()
		soup = BeautifulSoup(data)
		s = soup.findAll('td',{"align":"right"})
		heads = headers
		if len(s)==0:
			continue
		if (len)==3:
			heads = headers2
		print s
		dt = {"source":a,"target":b}
		for header in heads:
			k = s[heads[header]].span.text
			dt[header] = k[1:]
			print header,k[1:]
		c.execute("INSERT INTO fares(source,target,adult,child,senior,disabled,weekly,ten_trip,monthly,student_monthly) values(:source,:target,:adult,:child,:senior,:disabled,:weekly,:ten_trip,:monthly,:student_monthly)",dt)
		conn.commit()
print count