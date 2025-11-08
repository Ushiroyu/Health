#!/usr/bin/env bash
set -e
FILE=$1
if [ -z "$FILE" ]; then echo "Usage: restore.sh <dump.sql>"; exit 1; fi
cat "$FILE" | docker exec -i mysql mysql -uroot -proot
echo "Restored from $FILE"
