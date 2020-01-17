#/bin/sh
#set -x
scriptdir=$(dirname $(realpath "$0"))
cd "$scriptdir"
java -Dlog4j.configurationFile=log4j2.xml -jar "$scriptdir"/shade/rudibugger.jar
