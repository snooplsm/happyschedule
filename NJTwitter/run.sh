PWD=${PWD}
echo $PWD
mkdir bin
javac -verbose -cp libs/mongo-java-driver-2.12.2.jar:libs/twitter4j-core-3.0.3.jar:libs/twitter4j-async-3.0.3.jar:libs/twitter4j-async-3.0.3.jar:libs/twitter4j-core-3.0.3.jar:libs/twitter4j-media-support-3.0.3.jar:libs/twitter4j-stream-3.0.3.jar -sourcepath src -d bin -source 6 -target 6 -encoding utf-8 src/us/wmwm/happytap/stream/HappyStream.java src/us/wmwm/happytap/stream/Streams.java src/us/wmwm/happytap/stream/Service.java

java -cp bin:libs/mongo-java-driver-2.12.2.jar:libs/twitter4j-core-3.0.3.jar:libs/twitter4j-async-3.0.3.jar:libs/twitter4j-async-3.0.3.jar:libs/twitter4j-core-3.0.3.jar:libs/twitter4j-media-support-3.0.3.jar:libs/twitter4j-stream-3.0.3.jar us.wmwm.happytap.stream.HappyStream $1 $2 $3 $4 $5 $6 $7