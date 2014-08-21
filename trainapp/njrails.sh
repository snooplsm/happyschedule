read -s -p "Download new schedule? " -n 1 newschedule

yes='y'
no='n'
if [ "$yes" == "$newSchedule" ]; then
	read -s -p "Enter Password for snooplsm: " mypassword
	echo ""
	curl -c cookies.txt -o /dev/null -L "http://www.njtransit.com"
	curl -c cookies.txt -o /dev/null -b cookies.txt -L "https://www.njtransit.com/developer"
	curl -b cookies.txt -o /dev/null -b cookies.txt -L -d "userName=snooplsm&password=$mypassword" "https://www.njtransit.com/mt/mt_servlet.srv?hdnPageAction=MTDevLoginSubmitTo"
	curl -b cookies.txt -c cookies.txt --dump-header headers.txt -L "https://www.njtransit.com/mt/mt_servlet.srv?hdnPageAction=MTDevResourceDownloadTo&Category=rail" -o njtransit.zip
	unzip -o njtransit.zip -d gtfs/njtransit/
fi
cd ..
file="$PWD/libraries/library/res/raw/database_db"
cd trainapp
python graph_builder.py njtransit "$file"
cd trainapp


