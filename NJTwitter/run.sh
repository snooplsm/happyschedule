PWD=${PWD}
echo $PWD
mkdir bin

find src -type f -name *.java -exec javac -verbose -cp "libs/*" -sourcepath src -d bin -source 7 -target 7 -encoding utf-8 {} +
# java -cp "bin:libs/*" us.wmwm.happytap.stream.HappyStream $1 $2 $3 $4 $5 $6 $7 &