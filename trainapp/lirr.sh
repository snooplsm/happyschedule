curl http://web.mta.info/developers/data/lirr/google_transit.zip > lirr.zip
unzip -o lirr.zip -d gtfs/lirr/
python graph_builder.py lirr
cp target/lirr/database_db.zip ../LIRails/res/raw/database_db.zip


