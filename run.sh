#/bin/sh
#set -x
scriptdir=$(dirname $(realpath "$0"))
cd "$scriptdir"
java -Dlogback.configurationFile=./logback.xml -jar "$scriptdir"/shade/rudibugger.jar
