#!/usr/bin/env sh

image=$3/$1:$2

docker build  --build-arg=source_file=target/$1-$2.jar --build-arg=target_file=$1-$2.jar --rm -t $image .

exit 0
