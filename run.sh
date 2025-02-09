#!/bin/bash
##
# dreamdiary 서비스 실행 스크립트
#
# @author nichefish
##

APP_DIR="/home/dreamdiary/app/dreamdiary"
JAR_FILE="$APP_DIR/dreamdiary.jar"

echo "Starting Dreamdiary application..."

exec java -jar \
  -Xms1g \
  -Xmx8g \
  -server \
  -XX:+UseG1GC \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath="$APP_DIR/heapdump.hprof" \
  $JAR_FILE
