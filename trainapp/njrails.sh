#!/bin/sh

read -s -p "Enter Password for snooplsm: " mypassword
echo ""
curl -c cookies.txt -o /dev/null -L "http://www.njtransit.com"
curl -c cookies.txt -o /dev/null -b cookies.txt -L "https://www.njtransit.com/developer"
curl -b cookies.txt -o /dev/null -b cookies.txt -L -d "userName=snooplsm&password=$mypassword" "https://www.njtransit.com/mt/mt_servlet.srv?hdnPageAction=MTDevLoginSubmitTo"
curl -b cookies.txt -c cookies.txt --dump-header headers.txt -L "https://www.njtransit.com/mt/mt_servlet.srv?hdnPageAction=MTDevResourceDownloadTo&Category=rail" -o njtransit.zip
unzip -o njtransit.zip -d gtfs/njtransit/

file="$PWD/../libraries/library/src/njrails/res/raw/database_db"
echo $file
python graph_builder.py njtransit "$file"


