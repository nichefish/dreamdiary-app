import { createApp } from "vue";
import { createPinia } from "pinia";
import { Tooltip } from "bootstrap";
import App from "./App.vue";

/*
TIP: To get started with clean router change path to @/router/clean.ts.
 */
import router from "./router";
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
