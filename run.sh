#/bin/sh
scriptdir=`dirname $0`
cd "$scriptdir"
java -Dlog4j.configuration=file:log4j.properties -jar "$scriptdir"/target/rudibugger-1.1-SNAPSHOT.jar
