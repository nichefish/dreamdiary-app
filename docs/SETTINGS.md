# 프로젝트 세팅 관련
## MariaDB 12.2.2
- dreamdiary_private
  - utf8mb4_general_ci

# IDE 세팅 관련
## 1. Encoding
- Settings → Editor → File Encodings
  - "set encodings to UTF-8"
  - "Transparent native-to-ascii conversion"
  - "with NO BOM"

## 2. PC 환경 설정
1. sync all gradle project
2. IDE 터미널에서 'npm install' 실행

## 3. 빌드 관련
3-1. TypeScript 컴파일:  
- "tsc --watch"
3-2. 수동 프론트엔드 빌드
- "npm run build"
3-3. SCSS 컴파일
- "npm install -g node-sass"
- IntelliJ -> Settings -> Plugins -> Install File Watchers

### 테스트 커버리지 관련
1. 테스트를 실행하면서 JaCoCo가 커버리지를 수집하도록 해야 합니다.
   - './gradlew clean test'
2. 생성된 커버리지 리포트를 확인합니다.
   - ${buildDir}/reports/jacoco
   - 리포트 상단에 위치한 최상위 패키지의 커버리지를 확인합니다.
