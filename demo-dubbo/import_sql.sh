#!/usr/bin/env sh
for sql in $(ls); do
    echo $sql
    mysql -h127.0.0.1 -uroot -pkevin < $sql
done
