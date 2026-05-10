/**
 * 사이트 캐시 관리 모달 Vue 앱.
 * 기존 dF.Cache 호출 계약은 브리지로 유지하고, 목록/상세 렌더링은 Vue가 담당한다.
 */
import CacheListModalBody from "./components/CacheListModalBody.js";
import CacheDetailModalBody from "./components/CacheDetailModalBody.js";
import createCacheAdminActions from "./services/cacheAdminActionService.js";
import bindCacheAdminEventBridge from "./services/cacheAdminEventBridgeService.js";
import { CacheDetail, CacheMap } from "./types.js";

const state = Vue.reactive({
    cacheMap: {},
    detail: null,
}) as { cacheMap: CacheMap; detail: CacheDetail };

const actions = createCacheAdminActions({
    setCacheMap(cacheMap: CacheMap): void {
        state.cacheMap = cacheMap || {};
    },
    setDetail(detail: CacheDetail): void {
        state.detail = detail;
    },
    removeCache(cacheName: string): void {
        const next = { ...state.cacheMap };
        delete next[cacheName];
        state.cacheMap = next;
    },
    removeEntry(cacheName: string, cacheKey: string): void {
        const cache = state.cacheMap[cacheName];
        if (!cache) return;
        const nextCache = { ...cache };
        delete nextCache[cacheKey];
        const nextMap = { ...state.cacheMap };
        if (Object.keys(nextCache).length === 0) {
            delete nextMap[cacheName];
        } else {
            nextMap[cacheName] = nextCache;
        }
        state.cacheMap = nextMap;
    },
});
bindCacheAdminEventBridge(actions);

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

const CacheAdminRootApp = {
    name: "CacheAdminRootApp",
    components: {
        CacheListModalBody,
        CacheDetailModalBody,
    },
    data(): { state: { cacheMap: CacheMap; detail: CacheDetail } } {
        return { state };
    },
    methods: {
        onClearCache(cacheName: string): void {
            actions.clearByNmAjax(cacheName);
        },
        onOpenDetail(cacheName: string, cacheKey: string): void {
            actions.detailModal(cacheName, cacheKey);
        },
        onEvictEntry(cacheName: string, cacheKey: string): void {
            actions.evictAjax(cacheName, cacheKey);
        },
    },
    template: `
    <teleport to="#cache_list_div">
        <CacheListModalBody
            :cache-map="state.cacheMap"
            @clear-cache="onClearCache"
            @open-detail="onOpenDetail"
            @evict-entry="onEvictEntry"
        />
    </teleport>
    <teleport to="#cache_detail_div">
        <CacheDetailModalBody :detail="state.detail" />
    </teleport>
    `,
};

runWhenDomReady(function(): void {
    if (!document.getElementById("cache_admin_app")
        || !document.getElementById("cache_list_div")
        || !document.getElementById("cache_detail_div")) {
        console.error("[CacheAdminApp] Vue mount root not found.");
        return;
    }

    Vue.createApp(CacheAdminRootApp).mount("#cache_admin_app");
});
