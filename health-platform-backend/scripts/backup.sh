#!/usr/bin/env bash
set -e
TS=$(date +%F-%H%M%S)
docker exec mysql mysqldump -uroot -proot --databases healthdb > backup_$TS.sql
echo "Backup to backup_$TS.sql"
