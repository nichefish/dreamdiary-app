/**
 * JournalAnnualAsidePanelApp.ts
 * 저널 결산 사이드바 패널 Vue 렌더 (yy/mnth select + dummy filters + dead actions).
 *
 * 변경(A-5-β-2):
 *   - `_journal_annual_aside_base.ftlh` 의 card-body#journal_aside 내부 컨텐츠를 본 Vue 앱이 흡수한다.
 *   - yy/mnth onchange → `dF.JournalAnnualAside.yyMnth(event.target)` 위임 (service 본체 그대로 활용, 호출 그래프 보존).
 *   - dummy 영역(`꿈 키워드 검색`, `Crated` select) 과 dead 버튼(`Default`, `View more` href="#") 은
 *     기존 마크업과 동일한 옵션 텍스트·class·구조로 보존한다(UI 변경 0).
 *   - SSR 데이터(yyOptions / mnthOptions / labels) 는 `window.__journalAnnualAsideBootstrap` 으로 받는다.
 *   - 외부 wrapper(`#kt_app_aside` / `#kt_app_aside_wrapper`) 는 KTDrawer/KTScroll 훅 보존을 위해 SSR 그대로 유지된다.
 *   - mount root(`#journal_annual_aside_panel_mount`) 와 Vue 컴포넌트 root 는 `display:contents` 로 layout 무영향.
 *
 * @author nichefish
 */

type YyMnthOption = { value: string; label: string };
type AsideLabels = { yy: string; mnth: string; allYears: string; allMonths: string };
type AsideBootstrap = {
    yyOptions?: YyMnthOption[];
    mnthOptions?: YyMnthOption[];
    labels?: AsideLabels;
};

const VUE_MOUNT_ID = "journal_annual_aside_panel_mount";

/**
 * 부트스트랩 누락 시 fallback 라벨.
 * - SSR 주입 실패해도 마크업이 완전히 깨지지 않도록 한국어 기본값을 둔다.
 * - 기존 FTL 텍스트와 의미가 다르더라도 가시 dead 임을 인지 가능한 형태로(빈 문자열보다는 키 자체에 가까운) 둔다.
 */
const FALLBACK_LABELS: AsideLabels = {
    yy: "년",
    mnth: "월",
    allYears: "전체",
    allMonths: "전체",
};

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

/**
 * SSR 가 주입한 부트스트랩 객체를 안전하게 읽는다.
 */
function readBootstrap(): AsideBootstrap {
    const boot = (window as Window & { __journalAnnualAsideBootstrap?: AsideBootstrap })
        .__journalAnnualAsideBootstrap;
    if (boot == null) {
        console.error("[JournalAnnualAsidePanelApp] window.__journalAnnualAsideBootstrap 없음 — SSR script 적재 순서 확인.");
        return {};
    }
    return boot;
}

const JournalAnnualAsidePanelRoot = {
    name: "JournalAnnualAsidePanelRoot",
    data(): { yyOptions: YyMnthOption[]; mnthOptions: YyMnthOption[]; labels: AsideLabels } {
        const boot = readBootstrap();
        return {
            yyOptions: Array.isArray(boot.yyOptions) ? boot.yyOptions : [],
            mnthOptions: Array.isArray(boot.mnthOptions) ? boot.mnthOptions : [],
            labels: boot.labels ?? FALLBACK_LABELS,
        };
    },
    mounted(): void {
        const w = window as Window & { JournalAnnualAsidePanelVueApp?: { mounted?: boolean } };
        w.JournalAnnualAsidePanelVueApp = { mounted: true };
    },
    methods: {
        /**
         * yy/mnth select onchange 위임.
         * `dF.JournalAnnualAside.yyMnth(obj)` 본체는 service(journalAnnualAsideService) 가 등록하며
         * 쿠키 set + listAjax 갱신을 수행한다. 본 메서드는 event.target 만 그대로 넘긴다.
         */
        onYyMnthChange(event: Event): void {
            const target = event.target as HTMLSelectElement | null;
            if (target == null) return;
            const aside = (window as any).dF?.JournalAnnualAside;
            if (aside == null || typeof aside.yyMnth !== "function") {
                console.error("[JournalAnnualAsidePanelApp] dF.JournalAnnualAside.yyMnth 미등록 — service ES module 적재 순서 확인.");
                return;
            }
            aside.yyMnth(target);
        },
    },
    template: `
    <div class="journal-annual-aside-panel-vue-root" style="display:contents">
        <!--begin::Form-->
        <div class="mb-7 d-flex-between gap-4">
            <div class="col">
                <span class="text-gray-900 fs-h6 fw-bold d-inline-block mb-1">{{ labels.yy }}</span>
                <select name="yy" id="yy" class="form-select" aria-label="Select example" @change="onYyMnthChange">
                    <option value="">{{ labels.allYears }}</option>
                    <option v-for="opt in yyOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
                </select>
            </div>
            <div class="col">
                <span class="text-gray-900 fs-h6 fw-bold d-inline-block mb-1">{{ labels.mnth }}</span>
                <select name="mnth" id="mnth" class="form-select" aria-label="Select example" @change="onYyMnthChange">
                    <option value="">{{ labels.allMonths }}</option>
                    <option v-for="m in mnthOptions" :key="m.value" :value="m.value">{{ m.label }}</option>
                </select>
            </div>
        </div>
        <div class="mb-7">
            <span class="text-gray-900 fs-h6 fw-bold d-inline-block mb-1">꿈 키워드 검색</span>
            <select class="form-select" aria-label="Select example">
                <option>Any group</option>
                <option value="1">Grace Green</option>
                <option value="2">Nick LOgan</option>
                <option value="3">Carles Nilson</option>
                <option value="1">Alice Danchik</option>
                <option value="2">Harris Bold</option>
                <option value="3">Carles Nilson</option>
            </select>
        </div>
        <div class="mb-7">
            <span class="text-gray-900 fs-h6 fw-bold d-inline-block mb-1">Crated</span>
            <select class="form-select" aria-label="Select example">
                <option>Any Time</option>
                <option value="1">sumry ago</option>
                <option value="2">2 sumrys ago</option>
                <option value="3">April 15</option>
                <option value="1">April 10</option>
                <option value="2">March 30</option>
                <option value="3">March 25</option>
            </select>
        </div>
        <!--end::Form-->
        <!--begin::Action-->
        <div class="d-flex flex-column">
            <a href="#" class="btn btn-primary mb-4 p-3">Default</a>
            <a href="#" class="btn btn-secondary btn-color-gray-700 p-3">View more</a>
        </div>
        <!--end::Action-->
    </div>
    `,
};

runWhenDomReady(function(): void {
    const mountEl = document.querySelector("#" + VUE_MOUNT_ID) as HTMLElement | null;
    if (!mountEl) {
        console.error("[JournalAnnualAsidePanelApp] 마운트 루트 #" + VUE_MOUNT_ID + " 없음.");
        return;
    }
    try {
        const app = Vue.createApp(JournalAnnualAsidePanelRoot);
        app.mount(mountEl);
    } catch (e) {
        console.error("[JournalAnnualAsidePanelApp] Vue 마운트 실패:", e);
    }
});

export {};
