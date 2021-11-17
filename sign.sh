#!/bin/sh
java -cp "classes:lib/*:conf" jup.tools.SignTransactionJSON $@
exit $?
