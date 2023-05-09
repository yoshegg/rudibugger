#/bin/sh
#set -x
scriptdir=$(dirname $(realpath "$0"))
java -Dlogback.configurationFile=./logback.xml -jar "$scriptdir"/shade/rudibugger.jar "$@"
