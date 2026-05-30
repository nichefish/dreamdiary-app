# DreamDiary Shared Packages

Vue 웹과 React Native 모바일이 같이 써도 안전한 코드만 모으는 영역입니다.

## 원칙

- 공유한다: DTO 타입, API 경로/함수 factory, 콘텐츠 타입 enum, 날짜/태그 같은 순수 로직
- 공유하지 않는다: Vue 컴포넌트, Pinia store, React Native 화면, CSS/SCSS, 라우터, 인증 저장소

## 패키지

```text
packages/
  shared-types/   # API 계약과 DTO 타입
  shared-api/     # HttpClient 주입형 API 함수
  shared-domain/  # 플랫폼과 무관한 순수 도메인 로직
```

## 빌드

각 패키지는 TypeScript만 사용합니다.

```bash
cd packages/shared-types
npm install
npm run build
```

루트 워크스페이스는 아직 만들지 않았습니다. 기존 Vue/Gradle 빌드에 영향을 주지 않기 위해 초벌 단계에서는 독립 패키지로 둡니다. 실제 공유를 시작할 때 `package.json` workspaces를 루트에 추가하거나, 각 앱에서 `file:../../packages/shared-types` 방식으로 연결하면 됩니다.

