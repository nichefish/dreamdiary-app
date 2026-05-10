/**
 * JournalAnnualRegistModalApp.ts
 * 저널 결산 등록/수정 모달(`journal_annual_regist`) — Handlebars 헤더/본문 템플릿 대체.
 *
 * 변경(A-3):
 *   - `_journal_annual_regist_modal_header_template.hbs` / `_journal_annual_regist_modal_template.hbs` (tagify partial 한 줄) 본문 렌더와
 *     `dF.JournalAnnual.initForm` 의 `cF.handlebars.modal` 진입을 본 모듈로 단일 수렴한다.
 *   - 저장 버튼은 `window.JournalAnnualRegistVueApp.submit()` 가 `dF.JournalAnnual.submit()` 로 위임한다.
 *   - jQuery validate / TinyMCE 누적 방지를 위해 open 시점에 destroy → init 순서로 부착한다.
 *   - tagify 는 기존 `cF.tagify.initWithCtgr("#journalAnnualRegistForm #tagListStr", undefined)` 호출을 그대로 옮겨
 *     동일 DOM 부착 결과를 보장한다(ctgr map 미사용 — 호출 시그니처 보존).
 *   - 본 모달은 `journal_annual_list.ftlh` / `journal_annual_detail.ftlh` 두 페이지의 `_journal_annual_regist_modal.ftlh`
 *     include 묶음으로 진입한다(가드 `journalAnnualRegistVueScriptDone` 로 중복 적재 차단).
 *
 * @author nichefish
 */

import JournalAnnualRegistModalHeader from "./components/JournalAnnualRegistModalHeader.js";
import JournalAnnualRegistModalBody from "./components/JournalAnnualRegistModalBody.js";

type JournalAnnualRegistVueBridge = {
    mounted?: boolean;
    pendingPayload?: Record<string, any> | null;
    open?: (model: Record<string, any>) => void;
    submit?: () => void;
};

/** Vue mount point id (FTLH 가 만든 빈 div). */
const VUE_MOUNT_ID = "journal_annual_regist_vue_app";
/** Vue 가 헤더 마크업을 teleport 할 대상 div id. */
const HEADER_TELEPORT_ID = "journal_annual_regist_modal_header_div";
/** Vue 가 본문(tagify) 마크업을 teleport 할 대상 div id. */
const BODY_TELEPORT_ID = "journal_annual_regist_div";
/** form id. */
const FORM_SELECTOR = "#journalAnnualRegistForm";
/** TinyMCE editor id (textarea id 와 동일). */
const TINYMCE_ID = "tinymce_journalAnnualCn";
/** Bootstrap 모달 element id (modal_layout id `journal_annual_regist` + `_modal`). */
const MODAL_EL_ID = "journal_annual_regist_modal";
/** tagify selector — initForm 원본과 동일. */
const TAGIFY_SELECTOR = "#journalAnnualRegistForm #tagListStr";

const state: { model: Record<string, any> | null } = { model: null };
let openHandler: ((model: Record<string, any>) => void) | null = null;

function destroyPreviousValidator(): void {
    const $form = $(FORM_SELECTOR);
    const validator = $form.data("validator") as { destroy?: () => void } | undefined;
    if (validator && typeof validator.destroy === "function") {
        try {
            validator.destroy();
        } catch (e) {
            console.warn("[JournalAnnualRegistModalApp] jQuery validate destroy failed", e);
        }
    }
    $form.removeData("validator");
}

function destroyPreviousTinymce(): void {
    try {
        if (typeof tinymce === "undefined") return;
        const editor: any = (tinymce as any).get(TINYMCE_ID);
        if (editor && typeof editor.destroy === "function") editor.destroy();
    } catch (e) {
        console.warn("[JournalAnnualRegistModalApp] tinymce destroy failed", e);
    }
}

/**
 * 변경 전: dF.JournalAnnual.initForm — cF.handlebars.modal + cF.validate.validateForm + cF.tagify.initWithCtgr + cF.tinymce.init/setContentWhenReady.
 * 변경 후(A-3): 동일 호출 순서를 Vue 오픈 경로에서 수행한다(handlebars.modal 만 사라짐 — Vue teleport 가 대체).
 */
function attachRegistFormControls(model: Record<string, any>): void {
    const dfNs = (window as any).dF;
    const module = dfNs?.JournalAnnual as Record<string, any> | undefined;
    if (!module) {
        console.error("[JournalAnnualRegistModalApp] dF.JournalAnnual missing.");
        return;
    }

    destroyPreviousValidator();
    cF.validate.validateForm(FORM_SELECTOR, module.registAjax);

    /* tagify — 기존 initForm 의 호출 시그니처 보존(ctgrMap undefined). */
    cF.tagify.initWithCtgr(TAGIFY_SELECTOR, undefined);

    destroyPreviousTinymce();
    cF.tinymce.init(`#${TINYMCE_ID}`);
    cF.tinymce.setContentWhenReady(TINYMCE_ID, model.content || "");
}

function showModal(): void {
    const modalEl = document.getElementById(MODAL_EL_ID) as HTMLElement | null;
    if (!modalEl) {
        console.error("[JournalAnnualRegistModalApp] Modal element not found:", MODAL_EL_ID);
        return;
    }
    const bs = (window as unknown as { bootstrap?: { Modal: { getOrCreateInstance: (el: HTMLElement) => { show: () => void } } } }).bootstrap;
    bs?.Modal.getOrCreateInstance(modalEl).show();
}

function openRegist(model: Record<string, any>): void {
    state.model = { ...model };
    Vue.nextTick(function(): void {
        attachRegistFormControls(state.model as Record<string, any>);
        showModal();
    });
}

function submitBridge(): void {
    const mod = (window as any).dF?.JournalAnnual;
    if (!mod || typeof mod.submit !== "function") {
        console.error("[JournalAnnualRegistModalApp] submit — dF.JournalAnnual.submit unavailable.");
        return;
    }
    mod.submit();
}

function createRootComponent(): Record<string, unknown> {
    return {
        name: "JournalAnnualRegistRoot",
        components: { JournalAnnualRegistModalHeader, JournalAnnualRegistModalBody },
        data(): { state: typeof state; headerTo: string; bodyTo: string } {
            return {
                state,
                headerTo: "#" + HEADER_TELEPORT_ID,
                bodyTo: "#" + BODY_TELEPORT_ID,
            };
        },
        template: `
        <teleport :to="headerTo">
            <JournalAnnualRegistModalHeader v-if="state.model" :model="state.model" />
        </teleport>
        <teleport :to="bodyTo">
            <JournalAnnualRegistModalBody v-if="state.model" :model="state.model" />
        </teleport>
        `,
    };
}

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

runWhenDomReady(function(): void {
    const priorBridge = ((window as any).JournalAnnualRegistVueApp ?? {}) as JournalAnnualRegistVueBridge;
    const pending = priorBridge.pendingPayload ?? null;

    const mountEl = document.getElementById(VUE_MOUNT_ID) as HTMLElement | null;
    if (!mountEl) {
        /* 페이지에 모달 호스트가 없는 경우 브리지 mounted=false 유지(=stub). 큐잉된 payload 만 호출자에게 노출. */
        console.log("[JournalAnnualRegistModalApp] mount element not found:", VUE_MOUNT_ID);
        return;
    }

    state.model = null;
    openHandler = function(model: Record<string, any>): void {
        openRegist(model);
    };

    const app = Vue.createApp(createRootComponent());
    app.mount("#" + VUE_MOUNT_ID);

    (window as any).JournalAnnualRegistVueApp = {
        mounted: true,
        pendingPayload: null,
        open: function(model: Record<string, any>): void {
            if (typeof openHandler === "function") {
                openHandler(model);
                return;
            }
            const b = (window as any).JournalAnnualRegistVueApp as JournalAnnualRegistVueBridge;
            b.pendingPayload = model;
            console.log("[JournalAnnualRegistModalApp] pending payload queued.");
        },
        submit: submitBridge,
    };

    /* 부트 이전에 큐잉된 payload 가 있으면 처리한다. */
    if (pending && typeof pending === "object") {
        openHandler?.(pending as Record<string, any>);
    }
});

export {};
