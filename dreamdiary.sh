#!/bin/bash
##
# dreamdiary 서비스 실행 스크립트
#
# @author nichefish
##

APP_DIR="/home/dreamdiary/app/dreamdiary"
JAR_FILE="$APP_DIR/dreamdiary.jar"
PID_FILE="$APP_DIR/dreamdiary.pid"
LOG_FILE="$APP_DIR/log/dreamdiary.log"

# JVM 옵션 설정 (AWS 프리티어 메모리가 1GB이므로 그 이하로 설정)
JAVA_OPTS="-Xms256m -Xmx512m -server -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$APP_DIR/heapdump.hprof"

# 명령어 처리
case "$1" in
  ## 애플리케이션 시작 "dreamdiary.sh start"
  start)
    echo "Starting Dreamdiary application..."
    if [ -f "$PID_FILE" ]; then
      echo "Service is already running. PID: $(cat $PID_FILE)"
      exit 1
    fi
    nohup java $JAVA_OPTS -jar "$JAR_FILE" &> "$LOG_FILE" &
    echo $! > "$PID_FILE"

    # 애플리케이션이 실제로 실행되는지 확인 (최대 10초 대기)
    for i in {1..10}; do
      sleep 1
      if ps -p $(cat "$PID_FILE") > /dev/null; then
        echo "Application started successfully with PID $(cat $PID_FILE)"
        break
      fi
    done
    exit 0
    ;;

  ## 애플리케이션 상태 "dreamdiary.sh status"
  status)
    if [ -f "$PID_FILE" ]; then
      PID=$(cat "$PID_FILE")
      if ps -p $PID > /dev/null; then
        echo "Dreamdiary is running. PID: $PID"
      else
        echo "PID file exists but process is not running."
      fi
    else
      echo "Dreamdiary is not running."
    fi
    exit 0
    ;;

  ## 애플리케이션 중지 "dreamdiary.sh stop"
  stop)
    echo "Stopping Dreamdiary application..."
    if [ -f "$PID_FILE" ]; then
      PID=$(cat "$PID_FILE")
      kill $PID
      sleep 5
      if ps -p $PID > /dev/null; then
        echo "Process still running. Force killing..."
        kill -9 $PID
      fi
      rm -f "$PID_FILE"
      echo "Stopped."
    else
      echo "No PID file found. Service may not be running."
    fi
    exit 0
    ;;

  ## 애플리케이션 재시작 "dreamdiary.sh restart"
  restart)
    echo "Restarting Dreamdiary application..."
    $0 stop
    sleep 2
    $0 start
    exit 0
    ;;

  ## 애플리케이션 재시작 "dreamdiary.sh log"
  log)
    echo "Showing logs..."
    if [ -f "$LOG_FILE" ]; then
      tail -f "$LOG_FILE"
    else
      echo "Log file not found: $LOG_FILE"
    fi
    exit 0
    ;;

  *)
    echo "Usage: $0 {start|status|stop|restart|log}"
    exit 1
    ;;
esac
