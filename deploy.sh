#!/bin/bash
##
# deploy.sh - 배포 패키지 압축 해제 후 초기 서비스 설정 및 실행
# sudo 실행시 password 입력할 필요 없도록 설정 변경함
#
# @author nichefish
##

SERVICE_NAME="dreamdiary.service"
SERVICE_FILE="/etc/systemd/system/$SERVICE_NAME"
APP_DIR="/home/dreamdiary/app/dreamdiary"
RUN_SCRIPT="$APP_DIR/dreamdiary.sh"

# 기존 서비스가 존재하면 삭제
if [ -f "$SERVICE_FILE" ]; then
    echo "기존 서비스가 존재합니다. 삭제합니다."
    sudo systemctl stop "$SERVICE_NAME"
    sudo systemctl disable "$SERVICE_NAME"
    sudo rm -f "$SERVICE_FILE"
    sudo systemctl daemon-reload
fi

# 서비스 파일이 없는 경우 생성
if [ ! -f "$SERVICE_FILE" ]; then
    echo "서비스 파일이 존재하지 않습니다. 등록합니다."
    sudo tee "$SERVICE_FILE" > /dev/null << 'EOF'
[Unit]
Description=Dreamdiary Spring Boot Application
After=syslog.target network.target

[Service]
User=dreamdiary
WorkingDirectory=/home/dreamdiary/app/dreamdiary
ExecStart=/bin/bash /home/dreamdiary/app/dreamdiary/dreamdiary.sh start
ExecStop=/bin/bash /home/dreamdiary/app/dreamdiary/dreamdiary.sh stop
SuccessExitStatus=143
Restart=on-failure
RestartSec=10
Type=simple
PIDFile=/home/dreamdiary/app/dreamdiary/dreamdiary.pid

[Install]
WantedBy=multi-user.target
EOF

    sudo systemctl daemon-reload
    sudo systemctl enable "$SERVICE_NAME"
else
    echo "서비스 파일이 이미 존재합니다. 등록을 건너뜁니다."
fi

# 실행 권한 확인 및 부여
if [ ! -x "$RUN_SCRIPT" ]; then
    echo "실행 스크립트 권한이 없습니다. 실행 권한을 부여합니다."
    sudo chmod +x "$RUN_SCRIPT"
fi

# 서비스 상태 확인 및 출력
status=$(sudo systemctl is-active "$SERVICE_NAME")

# 서비스가 실행 중이면 안전하게 종료 후 시작
if [ "$status" = "active" ]; then
    echo "서비스가 실행 중입니다. 안전하게 재시작합니다."
    sudo systemctl stop "$SERVICE_NAME"
    sleep 3
fi
echo "서비스를 시작합니다."
sudo -u dreamdiary sudo systemctl start "$SERVICE_NAME"

# 3초 대기 후 상태 확인
sleep 5

# 서비스 상태 확인 및 출력
status=$(sudo systemctl is-active "$SERVICE_NAME")
if [ "$status" = "active" ]; then
    echo "dreamdiary 서비스가 정상적으로 실행 중입니다."
else
    echo "dreamdiary 서비스가 실행되지 않았습니다. 상태: $status"
    sudo journalctl -u "$SERVICE_NAME" --no-pager --lines=20
fi