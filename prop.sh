#!/bin/bash
FILE="conf/nxt.properties"

if [ -e $FILE ]; then
  echo "File $FILE already exists!"
else
  {
    echo "nxt.dbUrl=$DB_URL"
    echo "nxt.testDbUrl=$DB_URL"
    echo "nxt.dbPassword=$DB_PASSWORD"
    echo "nxt.testDbPassword=$DB_PASSWORD"
  } >> $FILE
fi
