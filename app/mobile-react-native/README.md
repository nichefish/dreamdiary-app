# DreamDiary Mobile

React Native / Expo based mobile starter. It is intentionally independent from `app/backend`, `app/frontend-vue`, and `app/frontend-react` builds.

**LTE + Tailscale 실기기 실행** 절차는 [`docs/migration/mobile/run-guide.md`](../../docs/migration/mobile/run-guide.md) 를 참고하세요.

### IntelliJ / PC Android 에뮬레이터 (UI 선검증)

1. Android Studio AVD 실행, 백엔드 `localhost:8080` 기동
2. `.env`: `EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8080`
3. 실행 (택 1):
   - **Gradle** (우클릭 Run): 루트 프로젝트 → Tasks → **mobile** → **mobileAndroid**
   - `app/mobile-react-native/run-android.bat` 더블클릭
   - `npm run android` (mobile-react-native 디렉터리)

Gradle Node만 있을 때는 루트에서 `.\gradlew.bat npmSetup` 후 `mobileAndroid` 실행.

## Goals

- Fast dream capture
- Daily emotion capture
- AI chat entry point
- Thin API client layer for the Spring Boot API

## Start

If Node/npm is not available on PATH, set up Node 20.19.x or newer first.

```bash
cd app/mobile-react-native
npm install
npm run start
```

Copy `.env.example` to `.env` and adjust the API URL when connecting to a local backend.

```bash
EXPO_PUBLIC_API_BASE_URL=http://localhost:8080
```

Android emulators usually reach the host machine through `http://10.0.2.2:8080`. Physical devices need the PC IP address on the same network.

### API URL by Runtime

- Expo Go on Android emulator (AVD): `http://10.0.2.2:8080`
- iOS simulator: `http://localhost:8080` (host setup dependent)
- Physical device (Android/iOS): `http://<YOUR_PC_LAN_IP>:8080`

Quick LAN IP examples:

- Windows PowerShell: `ipconfig`
- macOS/Linux: `ifconfig` or `ip a`

If the app runs on a physical device, make sure backend port `8080` is reachable from the same Wi-Fi network.

## Build Flow

### 1. Development Server

```bash
cd app/mobile-react-native
npm install
npm run start
```

This starts Metro. Expo Go or a development build loads the JavaScript bundle from this server.

### 2. Type Check

```bash
npm run type-check
```

This checks the React Native screens and API client TypeScript.

### 3. Native Project Generation

```bash
npx expo prebuild
```

This creates `android/` and `ios/` only when needed. In the starter phase, keep the app in Expo managed workflow and avoid committing generated native folders unless native code changes become necessary.

### 4. App Artifacts

With EAS Build:

```bash
npx eas-cli build --platform android --profile preview
npx eas-cli build --platform ios --profile production
```

Local native run:

```bash
npx expo run:android
npx expo run:ios
```

The `preview` profile in `eas.json` is for internal Android APK testing. The `production` profile is for store-ready builds.

## EAS Preview (Recommended for Device QA)

1. Install dependencies and login:

```bash
cd app/mobile-react-native
npm install
npx eas-cli login
```

2. Build preview APK:

```bash
npx eas-cli build --platform android --profile preview
```

3. Install APK from the EAS build link on a physical device.
4. Set `.env` with your LAN IP based API URL, then run backend and verify:
   - Login
   - Today date navigation
   - Calendar/Tag/Search to EntryDetail
   - Add/Edit/Delete entry flow
   - AI chat WebSocket connect/send/cancel

## Structure

```text
app/mobile-react-native/
  App.tsx
  src/
    api/
    screens/
    theme/
    types/
```

Authentication uses cookie-based JWT (`credentials: "include"`) for REST. Login and `POST /api/auth/refresh` also return `Authorization: Bearer …`; the app persists the access token in SecureStore with an in-memory cache (`src/auth/accessToken.ts`) for WebSocket handshake headers on real devices where cookies may not attach to `WebSocket`.

