/**
 * OAuth2PopupApp.ts
 * OAuth2 콜백 팝업 결과 화면 Vue 앱.
 */
import oauth2PopupDataService from "./services/oauth2PopupDataService.js";
import createOAuth2Actions from "./services/oauth2ActionService.js";
import bindOAuth2EventBridge from "./services/oauth2EventBridgeService.js";
import { OAuth2PopupData } from "./types.js";

const state = Vue.reactive({
    popup: oauth2PopupDataService.parsePopupData(),
}) as { popup: OAuth2PopupData };

const actions = createOAuth2Actions();
bindOAuth2EventBridge(actions);

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

const OAuth2PopupRootApp = {
    name: "OAuth2PopupRootApp",
    data(): { state: { popup: OAuth2PopupData } } {
        return { state };
    },
    computed: {
        categoryClass(): string {
            return this.state.popup.providerKey === "naver" ? "ctgr-success" : "ctgr-imprtc";
        },
    },
    methods: {
        onReturnMain(): void {
            actions.main();
        },
    },
    template: `
    <div class="d-flex-center mt-20">
        <span class="ctgr-span" :class="categoryClass">{{ state.popup.providerLabel }}</span>
    </div>
    <div class="d-flex-center flex-column mt-8">
        <div>{{ state.popup.authenticatedText }}</div>

        <div class="text-gray-500 fw-bold min-h-100px">{{ state.popup.errorMsg }}</div>

        <div class="mt-10">
            <button
                type="button"
                class="btn btn-sm btn-outlined btn-light-primary btn-active-primary py-2 px-3"
                @click="onReturnMain"
                data-bs-toggle="tooltip"
                data-bs-placement="top"
                data-bs-dismiss="click"
                :title="state.popup.returnMainTooltip"
            >
                <i class="bi bi-arrow-counterclockwise"></i>{{ state.popup.returnMainLabel }}...
            </button>
        </div>
    </div>
    `,
};

runWhenDomReady(function(): void {
    const root = document.getElementById("oauth2_popup_app");
    if (!root) {
        console.error("[OAuth2PopupApp] Vue mount root not found.");
        return;
    }
    actions.init();
    Vue.createApp(OAuth2PopupRootApp).mount(root);
});
