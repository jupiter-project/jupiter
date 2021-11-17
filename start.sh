#!/bin/sh
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
if [ -e ~/.${APPLICATION}/nxt.pid ]; then
    PID=`cat ~/.${APPLICATION}/nxt.pid`
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
nohup ${JAVA} -cp classes:lib/*:conf:addons/classes:addons/lib/* -Dnxt.runtime.mode=desktop jup.Jup > /dev/null 2>&1 &
echo $! > ~/.${APPLICATION}/jup.pid
cd - > /dev/null
