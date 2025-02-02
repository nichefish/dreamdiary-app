##
# Dockerfile이 위치한 디렉터리에서 Docker 이미지를 빌드합니다.
# docker build -t dreamdiary .
# 빌드된 Docker 이미지를 기반으로 컨테이너를 실행합니다.
# docker network ls
# docker run -d --name dreamdiary -p 8080:8080 --network="dreamdiary_network" dreamdiary
##

FROM openjdk:11-jre

# 필요한 패키지 설치 (필요 시)
# RUN apt-get update; apt-get install -y fontconfig libfreetype6

# 애플리케이션 포트 설정
EXPOSE 8080

# 작업 디렉토리 설정
WORKDIR /dreamdiary/

# 애플리케이션 파일 복사
COPY config/application.yml /dreamdiary/config/application.yml
COPY content/* /dreamdiary/content/
COPY templates/* /dreamdiary/templates/
# .jar 파일
COPY build/libs/dreamdiary.jar /dreamdiary/dreamdiary.jar
# 실행 스크립트
COPY run.sh /dreamdiary/run.sh
RUN chmod +x /dreamdiary/run.sh

# 실행 명령 설정
ENTRYPOINT ["/dreamdiary/run.sh"]