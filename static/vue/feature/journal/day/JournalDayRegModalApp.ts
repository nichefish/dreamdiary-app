/**
 * JournalDayRegModalApp.ts
 * 저널 일자 등록/수정 모달(`journal_day_reg`) 본문 렌더 — Handlebars 템플릿 대체.
 */

import journalDayUiBridgeService from "./services/journalDayUiBridgeService.js";
import { getJournalDayListBridge } from "./journalDayListBridge.js";
import { createScopedI18n } from "../../../global/services/scopedI18nService.js";
import JournalDayRegModalBody from "./components/JournalDayRegModalBody.js";

type JournalDayRegVueBridge = {
    mounted?: boolean;
    open?: (model: Record<string, any>) => void;
    pendingPayload?: Record<string, any> | null;
};

const state = Vue.reactive({
    model: null as Record<string, any> | null,
});

const i18n = createScopedI18n();

/** 폼 플러그인 인스턴스 (모달 재오픈 시 destroy 대상) */
let tagTagify: any = null;
let metaTagify: any = null;

function t(key: string): string {
    return i18n.t(key);
}

function resolveJournalDayLocale(): string {
    const win = typeof window !== "undefined" ? (window as Window & { Model?: { locale?: string } }) : undefined;
    const locale = win?.Model?.locale;
    if (locale) return locale;
    return (document.documentElement.lang || "ko").replace(/_/g, "-");
}

/**
 * 서버/등록 초기 객체를 폼 모델 형태로 정규화한다.
 */
function normalizeRegModel(obj: Record<string, any>): Record<string, any> {
    const tag = obj.tag && typeof obj.tag === "object" ? obj.tag : {};
    const meta = obj.meta && typeof obj.meta === "object" ? obj.meta : {};
    return {
        ...obj,
        tag,
        meta,
        journalDatePrecision: obj.journalDatePrecision ?? "EXACT",
        diaryResolvedYn: obj.diaryResolvedYn ?? "N",
        weather: obj.weather ?? "",
    };
}

function showRegModal(): void {
    const modalEl = document.querySelector("#journal_day_reg_modal") as HTMLElement | null;
    if (!modalEl) {
        console.error("[JournalDayRegModalApp] Modal root #journal_day_reg_modal not found.");
        return;
    }
    const bs = (window as unknown as { bootstrap?: { Modal: { getOrCreateInstance: (el: HTMLElement) => { show: () => void } } } }).bootstrap;
    bs?.Modal.getOrCreateInstance(modalEl).show();
}

/**
 * 이전 모달 오픈에서 붙은 datepicker / Tagify / jQuery Validation 인스턴스를 제거한다.
 * 변경 전: dF.JournalDayFormService.destroyPreviousRegPlugins() — dF.JournalDayFormStateService 로 tagify 인스턴스를 관리했다.
 * 변경 후(Vue Phase 3): 모듈 스코프 변수(tagTagify, metaTagify) 로 직접 관리한다.
 */
function destroyPreviousRegPlugins(): void {
    const $journalDate = $("#journalDate");
    const drpUnknown = $journalDate.data("daterangepicker") as { remove?: () => void } | undefined;
    if (drpUnknown && typeof drpUnknown.remove === "function") {
        try {
            drpUnknown.remove();
        } catch (e) {
            console.warn("[JournalDayRegModalApp] daterangepicker remove failed", e);
        }
    }

    if (tagTagify && typeof tagTagify.destroy === "function") {
        try {
            tagTagify.destroy();
        } catch (e) {
            console.warn("[JournalDayRegModalApp] tag Tagify destroy failed", e);
        }
    }
    tagTagify = null;

    if (metaTagify && typeof metaTagify.destroy === "function") {
        try {
            metaTagify.destroy();
        } catch (e) {
            console.warn("[JournalDayRegModalApp] meta Tagify destroy failed", e);
        }
    }
    metaTagify = null;

    const $form = $("#journalDayRegForm");
    const validatorUnknown = $form.data("validator") as { destroy?: () => void } | undefined;
    if (validatorUnknown && typeof validatorUnknown.destroy === "function") {
        try {
            validatorUnknown.destroy();
        } catch (e) {
            console.warn("[JournalDayRegModalApp] jQuery validate destroy failed", e);
        }
    }
    $form.removeData("validator");
}

/**
 * 등록/수정 처리 (Ajax).
 * 변경 전: dF.JournalDayCrudService.regAjax() — dF.JournalDayFormStateService.getMetaTagify(), dF.JournalDayRuntimeService.refresh() 경유.
 * 변경 후(Vue Phase 3): 모듈 스코프 metaTagify 직접 참조, <code>getJournalDayListBridge()?.refresh()</code> 직접 호출.
 */
function regAjax(): void {
    const id: string = cF.util.getInputValue("#journalDayRegForm [name='id']");
    const isMdf: boolean = cF.util.isNotEmpty(id);

    // 등록 클릭시 입력 중이던 메타 추가
    if (metaTagify?.draft?.value) {
        const meta: string = cF.util.getInputValue("#meta_value");
        const { value, ctgr } = metaTagify.draft;
        if (value && meta) {
            cF.tagify.commitTag(metaTagify, value, ctgr, meta);
        }
    }
    setTimeout(function(): void {
        Swal.fire({
            text: Message.get(isMdf ? "view.cnfm.mdf" : "view.cnfm.reg"),
            showCancelButton: true,
        }).then(function(result: SwalResult): void {
            if (!result.value) return;

            const url: string = isMdf ? cF.util.bindUrl(Url.JOURNAL_DAY, { id }) : Url.JOURNAL_DAYS;
            const ajaxData: FormData = new FormData(document.getElementById("journalDayRegForm") as HTMLFormElement);
            cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
                Swal.fire({ text: res.message })
                    .then(function(): void {
                        if (!res.rslt) return;
                        const vueApp = getJournalDayListBridge() as { mounted?: boolean; refresh?: () => void } | undefined;
                        if (vueApp?.mounted === true && typeof vueApp.refresh === "function") {
                            vueApp.refresh();
                            return;
                        }
                        window.JournalDayCalVueApp?.refresh?.();
                    });
            }, "block");
        });
    }, 0);
}

/**
 * 폼 플러그인(daterangepicker, Tagify, jQuery Validate) 을 부착한다.
 * 변경 전: dF.JournalDayFormService.attachRegFormControls() 호출 — dF.JournalDayCrudService.regAjax, dF.JournalDayFormStateService 참조.
 * 변경 후(Vue Phase 3): Vue 소유 regAjax, 모듈 스코프 tagTagify/metaTagify 를 사용한다.
 * @param {Record<string, any>} obj - 폼 초기값 객체.
 */
function attachRegFormControls(obj: Record<string, any>): void {
    destroyPreviousRegPlugins();

    const form: HTMLFormElement | null = document.querySelector("#journalDayRegForm") as HTMLFormElement | null;
    if (!form) {
        console.error("[JournalDayRegModalApp] #journalDayRegForm not found.");
        return;
    }

    cF.validate.validateForm("#journalDayRegForm", regAjax, {
        rules: {
            journalDate: { required: true },
        },
        ignore: undefined,
    });

    cF.datepicker.singleDatePicker("#journalDate", "yyyy-MM-DD", obj.journalDate);
    $("#journalDatePrecision").val(obj.journalDatePrecision ?? "EXACT");

    tagTagify = cF.tagify.initWithCtgr("#journalDayRegForm #tagListStr", journalDayUiBridgeService.getDayTagCategoryMap());
    metaTagify = cF.tagify.initMeta("#journalDayRegForm #metaListStr", journalDayUiBridgeService.getDayMetaCategoryMap());
}

function openReg(model: Record<string, any>): void {
    state.model = normalizeRegModel(model);
    Vue.nextTick(function(): void {
        attachRegFormControls(state.model as Record<string, any>);
        journalDayUiBridgeService.initRenderedDom("journal_day_reg_div");
        showRegModal();
    });
}

const JournalDayRegRootApp = {
    name: "JournalDayRegRootApp",
    components: {
        JournalDayRegModalBody,
    },
    data(): { state: typeof state } {
        return { state };
    },
    template: `
    <teleport to="#journal_day_reg_div">
        <JournalDayRegModalBody v-if="state.model" :model="state.model" />
    </teleport>
    `,
};

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

runWhenDomReady(async function(): Promise<void> {
    const mountEl = document.querySelector("#journal_day_reg_vue_app") as HTMLElement | null;
    if (!mountEl) {
        console.error("[JournalDayRegModalApp] Mount root #journal_day_reg_vue_app not found.");
        return;
    }

    await i18n.load(resolveJournalDayLocale());

    const priorBridge = (window.JournalDayRegVueApp ?? {}) as JournalDayRegVueBridge;
    const pendingPayload: Record<string, any> | null | undefined = priorBridge.pendingPayload;

    const app = Vue.createApp(JournalDayRegRootApp);
    app.config.globalProperties.$t = (key: string): string => t(key);
    app.mount("#journal_day_reg_vue_app");

    window.JournalDayRegVueApp = {
        mounted: true,
        pendingPayload: null,
        open: openReg,
    };

    if (pendingPayload && typeof pendingPayload === "object") {
        openReg(pendingPayload);
    }
});
