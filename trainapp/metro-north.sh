curl http://web.mta.info/developers/data/mnr/google_transit.zip > metro_north.zip
unzip -o metro_north.zip -d gtfs/metro-north/
python graph_builder.py metro-north
cp target/metro-north/database_db.zip ../MetroNorthRails/res/raw/database_db.zip


