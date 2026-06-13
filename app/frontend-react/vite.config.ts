import path from "node:path";

import react from "@vitejs/plugin-react";
import { defineConfig, loadEnv } from "vite";

export default defineConfig(({ mode }) => {
  const sharedEnvDir = path.resolve(__dirname, "../../config/env");
  const env = loadEnv(mode, sharedEnvDir, "");
  const backendHost = env.SERVER_DOMAIN || "localhost";
  const backendPort = env.SERVER_PORT || "18081";
  const backendTarget = `http://${backendHost}:${backendPort}`;

  return {
    base: "/react-app/",
    plugins: [react()],
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "src"),
        "@metronic": path.resolve(__dirname, "src/platform/metronic"),
      },
    },
    server: {
      port: 5174,
      proxy: {
        "/api": {
          target: backendTarget,
          changeOrigin: true,
        },
        "/chat": {
          target: backendTarget,
          changeOrigin: true,
          ws: true,
        },
        "/login": {
          target: backendTarget,
          changeOrigin: true,
        },
        "/logout": {
          target: backendTarget,
          changeOrigin: true,
        },
        "/oauth2": {
          target: backendTarget,
          changeOrigin: true,
        },
        "/css": {
          target: backendTarget,
          changeOrigin: true,
        },
        "/font": {
          target: backendTarget,
          changeOrigin: true,
        },
      },
    },
    build: {
      outDir: "../../static/react-app",
      emptyOutDir: true,
    },
  };
});
