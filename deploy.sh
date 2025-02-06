#!/bin/bash
# deploy.sh - 배포 패키지 압축 해제 후 초기 서비스 설정 및 실행

# 서비스 파일이 없는 경우 생성
if [ ! -f /etc/systemd/system/dreamdiary.service ]; then
    echo "서비스 파일이 존재하지 않습니다. 등록합니다."
    sudo tee /etc/systemd/system/dreamdiary.service > /dev/null << 'EOF'
[Unit]
Description=Dreamdiary Spring Boot Application
After=syslog.target network.target

[Service]
User=dreamdiary
WorkingDirectory=/home/dreamdiary/app/dreamdiary
ExecStart=/bin/bash /home/dreamdiary/app/dreamdiary/run.sh
SuccessExitStatus=143
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

    sudo systemctl daemon-reload
    sudo systemctl enable dreamdiary.service
else
    echo "서비스 파일이 이미 존재합니다. 등록을 건너뜁니다."
fi

# 서비스 재시작 (배포 후 변경사항 반영)
sudo systemctl restart dreamdiary.service

# 서비스 상태 확인 및 출력
status=$(sudo systemctl is-active dreamdiary.service)
if [ "$status" = "active" ]; then
    echo "dreamdiary service is active."
else
    echo "dreamdiary service is inactive with problem. 상태: $status"
fi