#/bin/sh
#set -x
here=`pwd`
logbackconf="logback.xml"
config=""
if test -f logback.xml; then
    logbackconf="$here/logback.xml"
else
    logbackconf="./$logbackconf"
fi
if test -n "$1"; then
    config="$here/$1"
fi

scriptdir=$(dirname $(realpath "$0"))
cd "$scriptdir"

java -Dlogback.configurationFile="$logbackconf" -jar shade/rudibugger.jar "$config"
