#!/usr/bin/env sh
for sql in $(ls ./sql); do
    echo $sql
    mysql -h127.0.0.1 -uroot -pkevin < ./sql/$sql
done
