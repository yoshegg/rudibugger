#/bin/sh
#set -x
scriptdir=$(dirname $(realpath "$0"))
cd "$scriptdir"
java -Dlog4j.configuration=file:log4jsilent.properties -jar "$scriptdir"/target/rudibugger-1.2-SNAPSHOT.jar
