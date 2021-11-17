#!/bin/sh
# Use this start script if you are providing a Jupiter node as a service

APPLICATION="jrs"
if [ -e ~/.${APPLICATION}/jup.pid ]; then
    PID=`cat ~/.${APPLICATION}/jup.pid`
    ps -p $PID > /dev/null
    STATUS=$?
    if [ $STATUS -eq 0 ]; then
        echo "Jupiter server already running"
        exit 1
    fi
fi
mkdir -p ~/.${APPLICATION}/
DIR=`dirname "$0"`
cd "${DIR}"
if [ -x jre/bin/java ]; then
    JAVA=./jre/bin/java
else
    JAVA=java
fi
# Adapt -Xms and -Xmx to your needs
nohup ${JAVA} -Xms4096m -Xmx8192m -cp classes:lib/*:conf:addons/classes:addons/lib/* -Dnxt.runtime.mode=desktop jup.Jup > /dev/null 2>&1 &
echo $! > ~/.${APPLICATION}/jup.pid
cd - > /dev/null
