import { fileURLToPath, URL } from "node:url";
import path from "node:path";

import { defineConfig, loadEnv } from "vite";
import vue from "@vitejs/plugin-vue";

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  // Spring Boot 와 공유하는 config/env/.env.{local|dev|prod} 에서 읽는다
  // SERVER_DOMAIN, SERVER_PORT 는 Spring Boot application-{profile}.yml 과 동일한 값
  const sharedEnvDir = path.resolve(__dirname, "../../config/env");
  const env = loadEnv(mode, sharedEnvDir, "");
  const apiTarget = `http://${env.SERVER_DOMAIN || "localhost"}:${env.SERVER_PORT || "18081"}`;

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        "vue-i18n": "vue-i18n/dist/vue-i18n.cjs.js",
        "@": fileURLToPath(new URL("./src", import.meta.url)),
        "@metronic": fileURLToPath(new URL("./src/vendor/metronic", import.meta.url)),
      },
    },
    // dev/prod 모두 /vue-app/ 기준으로 서빙 (Spring Boot static 경로와 일치)
    base: "/vue-app/",
    server: {
      port: 5173,
      proxy: {
        "/api":    { target: apiTarget, changeOrigin: true },
        "/chat":   { target: apiTarget, changeOrigin: true, ws: true },
        "/login":  { target: apiTarget, changeOrigin: true },
        "/logout": { target: apiTarget, changeOrigin: true },
        "/oauth2": { target: apiTarget, changeOrigin: true },
        "/css":    { target: apiTarget, changeOrigin: true },
        "/font":   { target: apiTarget, changeOrigin: true },
      },
    },
    build: {
      chunkSizeWarningLimit: 3000,
      // Spring Boot static 경로로 직접 빌드 출력
      outDir: "../../static/vue-app",
      emptyOutDir: true,
    },
  };
});
