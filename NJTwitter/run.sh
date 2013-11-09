PWD=${PWD}
echo $PWD
javac -verbose -cp libs/sqlite-jdbc-3.7.15-M1.jar:libs/twitter4j-core-3.0.3.jar:libs/twitter4j-async-3.0.3.jar:libs/twitter4j-async-3.0.3.jar:libs/twitter4j-core-3.0.3.jar:libs/twitter4j-media-support-3.0.3.jar:libs/twitter4j-stream-3.0.3.jar -sourcepath src -d bin -source 6 -target 6 -encoding utf-8 src/us/wmwm/happytap/stream/HappyStream.java src/us/wmwm/happytap/stream/Streams.java src/us/wmwm/happytap/stream/Service.java

java -cp bin:libs/sqlite-jdbc-3.7.15-M1.jar:libs/twitter4j-core-3.0.3.jar:libs/twitter4j-async-3.0.3.jar:libs/twitter4j-async-3.0.3.jar:libs/twitter4j-core-3.0.3.jar:libs/twitter4j-media-support-3.0.3.jar:libs/twitter4j-stream-3.0.3.jar us.wmwm.happytap.stream.HappyStream $1 $2 $3 $4 $5 $6 $7