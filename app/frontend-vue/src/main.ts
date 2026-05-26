import { createApp } from "vue";
import { createPinia } from "pinia";
import { Tooltip } from "bootstrap";
import axios from "axios";
import Swal from "sweetalert2/dist/sweetalert2.js";
import App from "./App.vue";

/*
TIP: To get started with clean router change path to @/router/clean.ts.
 */
import router from "./router";
import { AuthExpiredError } from "@/utils/authError";
import { useAuthStore } from "@/stores/auth";

/**
 * 전역 Axios 인터셉터: 401(세션 만료/비로그인) 응답 시 로그인 만료를 명확히 안내한다.
 * /api/auth/ 경로(로그인 자체)는 auth 스토어에서 이미 처리하므로 제외.
 * 동시에 여러 요청이 401 로 실패해도 대화상자는 한 번만 뜬다.
 * 각 catch 블록에서 isAuthExpiredError() 로 판별해 일반 오류 alert 를 억제한다.
 */
let authExpiredDialogShowing = false;
const SESSION_EXPIRED_QUERY = { sessionExpired: "Y" };
axios.interceptors.response.use(
  (response) => response,
  async (error: unknown) => {
    const status = (error as { response?: { status?: number }; config?: { url?: string } })?.response?.status;
    const url = (error as { response?: { status?: number }; config?: { url?: string } })?.config?.url ?? "";
    if (status === 401 && !url.includes("/api/auth/")) {
      if (!authExpiredDialogShowing) {
        authExpiredDialogShowing = true;
        try {
          const isPopup = router.currentRoute.value.name === "journal-entry-search";
          const authStore = useAuthStore();
          authStore.purgeAuth();

          if (isPopup) {
            /* 팝업 라우트: 로그인 이동 대신 창 닫기 안내 */
            const result = await Swal.fire({
              icon: "warning",
              title: "로그인이 풀렸습니다",
              text: "현재 검색 창에서는 더 이상 저장/조회할 수 없습니다. 창을 닫을까요?",
              showCancelButton: true,
              confirmButtonText: "창 닫기",
              cancelButtonText: "그대로 두기",
            });
            if (result.isConfirmed) window.close();
          } else {
            const result = await Swal.fire({
              icon: "warning",
              title: "로그인이 풀렸습니다",
              text: "세션이 만료되었거나 다른 곳에서 로그인되어 현재 로그인이 해제되었습니다. 로그인 화면으로 이동하면 작성 중인 내용이 유실될 수 있습니다. 이동할까요?",
              showCancelButton: true,
              confirmButtonText: "로그인 화면으로 이동",
              cancelButtonText: "현재 화면에 머무르기",
            });
            if (result.isConfirmed) {
              void router.push({
                name: "sign-in",
                query: {
                  ...SESSION_EXPIRED_QUERY,
                  redirect: router.currentRoute.value.fullPath,
                },
              });
            }
          }
        } finally {
          authExpiredDialogShowing = false;
        }
      }
      return Promise.reject(new AuthExpiredError());
    }
    return Promise.reject(error);
  }
);
import ElementPlus from "element-plus";
import i18n from "@metronic/core/plugins/i18n";

//imports for app initialization
import ApiService from "@metronic/core/services/ApiService";
import LayoutService from "@metronic/core/services/LayoutService";
import { initApexCharts } from "@metronic/core/plugins/apexcharts";
import { initInlineSvg } from "@metronic/core/plugins/inline-svg";
import { initVeeValidate } from "@metronic/core/plugins/vee-validate";
import {
  initKtIcon,
  initializeComponents,
  reinitializeComponents,
} from "@metronic/core/plugins/keenthemes";
import { useConfigStore } from "@/stores/config";

import "@metronic/core/plugins/prismjs";

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);
app.use(router);
app.use(ElementPlus);

ApiService.init(app);
initApexCharts(app);
initKtIcon(app);
initInlineSvg(app);
initVeeValidate();

app.use(i18n);

app.directive("tooltip", (el) => {
  new Tooltip(el);
});

const configStore = useConfigStore();
configStore.overrideLayoutConfig();
LayoutService.init();
initializeComponents();

router.afterEach(() => {
  setTimeout(() => {
    reinitializeComponents();
  }, 0);
});

app.mount("#app");
