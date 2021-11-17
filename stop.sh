#!/bin/sh
APPLICATION="jrs"
if [ -e ~/.${APPLICATION}/jup.pid ]; then
    PID=`cat ~/.${APPLICATION}/jup.pid`
    ps -p $PID > /dev/null
    STATUS=$?
    echo "stopping"
    while [ $STATUS -eq 0 ]; do
        kill `cat ~/.${APPLICATION}/jup.pid` > /dev/null
        sleep 5
        ps -p $PID > /dev/null
        STATUS=$?
    done
    rm -f ~/.${APPLICATION}/jup.pid
    echo "Jupiter server stopped."
fi
if [ -e ~/.${APPLICATION}/nxt.pid ]; then
    PID=`cat ~/.${APPLICATION}/nxt.pid`
    ps -p $PID > /dev/null
    STATUS=$?
    echo "stopping"
    while [ $STATUS -eq 0 ]; do
        kill `cat ~/.${APPLICATION}/nxt.pid` > /dev/null
        sleep 5
        ps -p $PID > /dev/null
        STATUS=$?
    done
    rm -f ~/.${APPLICATION}/nxt.pid
    echo "Jupiter server stopped."
fi

