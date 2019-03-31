FROM java:openjdk-8-alpine
LABEL MAINTAINER Kevin Hwang <kevin.hwang@live.com>

ARG source_file
ARG target_file
ENV TARGET_FILE=${target_file}

ADD $source_file $target_file

CMD ["sh", "-c", "java -jar $TARGET_FILE"]
