## Dreamdiary Windows 서비스 등록 가이드 (NSSM)

### 0. NSSM이란?
- Non-Sucking Service Manager.
- 일반 실행 프로그램(exe, bat, jar 등)을 Windows 서비스처럼 실행할 수 있게 해주는 도구다.
- Java/Spring Boot를 Windows 서버에서 간단히 운영할 때 가장 설정이 쉽고 가볍다.
- Windows 부팅 시 자동 실행, 백그라운드 실행, 서비스 관리(services.msc) 등이 가능해진다.

### 1. NSSM 다운로드
- 공식 사이트에서 NSSM 다운로드, 압축 해제 후 원하는 위치에 둔다.
  - 예시 위치: "C:\Dev\toolkits\nssm-2.24"
- NSSM 경로를 PATH에 추가한다.
  - "powershell -ExecutionPolicy Bypass -File .\add_nssm_path.ps1"

### 2. PowerShell / CMD 실행 후 NSSM 폴더 이동
- "cd C:\Dev\toolkits\nssm-2.24\win64"

### 3. 서비스 설치 창 실행
- "nssm install dreamdiary"

### 4. NSSM 설정
#### 4-1. path
  - 서비스에 사용할 java.exe 경로를 넣는다.
  - "C:\Users\{user}\.jdks\ms-17.0.19\bin"
#### 4-2. startup directory
  - JAR 파일이 존재하는 디렉토리 지정.
  - "C:\Dev\services\dreamdiary"
#### 4-3. argument
  - Spring Boot 실행 옵션 및 JVM 옵션 입력.
  - "-server -Xms512m -Xmx2048m -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Seoul -Dlogging.file.path=C:/Dev/services/dreamdiary/logs -Dspring.profiles.active=local -jar dreamdiary.jar"

### 5. 관리
  - 윈도우 "서비스"에서 해당 프로세스 관리 가능
