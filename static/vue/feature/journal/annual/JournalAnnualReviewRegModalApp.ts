/**
 * JournalAnnualReviewRegModalApp.ts
 * 저널 결산 리뷰 등록/수정 모달(`journal_annual_review_reg`) — Handlebars 헤더/본문 템플릿 대체.
 *
 * 변경(A-3):
 *   - `_journal_annual_review_reg_modal_header_template.hbs` / `_journal_annual_review_reg_modal_template.hbs` (tagify partial 한 줄) 본문 렌더와
 *     `dF.JournalAnnualReview.initForm` 의 `cF.handlebars.modal` 진입을 본 모듈로 단일 수렴한다.
 *   - 저장 버튼은 `window.JournalAnnualReviewRegVueApp.submit()` 가 `dF.JournalAnnualReview.submit()` 로 위임한다.
 *   - jQuery validate / TinyMCE 누적 방지를 위해 open 시점에 destroy → init 순서로 부착한다.
 *   - tagify 호출 시그니처는 *행위 보존*을 위해 원본 initForm 의 두 호출을 그대로 옮긴다:
 *       (1) `cF.tagify.initWithCtgr("#journalAnnualReviewRegForm #tagListStr", undefined)`
 *       (2) `dF.JournalAnnualReview.tagify = cF.tagify.init("#journalAnnualReviewRegForm #tagListStr")`
 *     원본 모듈은 동일 selector 에 두 번 부착하는 형태이며, 이 phase 에서는 동작 변경을 유발하지 않도록 그대로 보존한다.
 *     · 변경 이력 비고: 이중 호출은 향후 phase 에서 cleanup 후보(가시 dead) — 본 phase 의 책임 범위는 아님.
 *   - 본 모달은 `journal_annual_dtl.ftlh` 의 `_journal_annual_review_reg_modal.ftlh` include 묶음으로만 진입한다
 *     (list 페이지에서는 review 진입점 0 — include 자체가 dtl 전용).
 *
 * @author nichefish
 */

import JournalAnnualReviewRegModalHeader from "./components/JournalAnnualReviewRegModalHeader.js";
import JournalAnnualReviewRegModalBody from "./components/JournalAnnualReviewRegModalBody.js";

type JournalAnnualReviewRegVueBridge = {
    mounted?: boolean;
    pendingPayload?: Record<string, any> | null;
    open?: (model: Record<string, any>) => void;
    submit?: () => void;
};

/** Vue mount point id (FTLH 가 만든 빈 div). */
const VUE_MOUNT_ID = "journal_annual_review_reg_vue_app";
/** Vue 가 헤더 마크업을 teleport 할 대상 div id. */
const HEADER_TELEPORT_ID = "journal_annual_review_reg_modal_header_div";
/** Vue 가 본문(tagify) 마크업을 teleport 할 대상 div id. */
const BODY_TELEPORT_ID = "journal_annual_review_reg_div";
/** form id. */
const FORM_SELECTOR = "#journalAnnualReviewRegForm";
/** TinyMCE editor id (textarea id 와 동일). */
const TINYMCE_ID = "tinymce_journalAnnualReviewCn";
/** Bootstrap 모달 element id (modal_layout id `journal_annual_review_reg` + `_modal`). */
const MODAL_EL_ID = "journal_annual_review_reg_modal";
/** tagify selector — initForm 원본과 동일. */
const TAGIFY_SELECTOR = "#journalAnnualReviewRegForm #tagListStr";

const state: { model: Record<string, any> | null } = { model: null };
let openHandler: ((model: Record<string, any>) => void) | null = null;

function destroyPreviousValidator(): void {
    const $form = $(FORM_SELECTOR);
    const validator = $form.data("validator") as { destroy?: () => void } | undefined;
    if (validator && typeof validator.destroy === "function") {
        try {
            validator.destroy();
        } catch (e) {
            console.warn("[JournalAnnualReviewRegModalApp] jQuery validate destroy failed", e);
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
        console.warn("[JournalAnnualReviewRegModalApp] tinymce destroy failed", e);
    }
}

/**
 * 변경 전: dF.JournalAnnualReview.initForm — cF.handlebars.modal + cF.validate.validateForm + cF.tagify.initWithCtgr +
 *   cF.tinymce.init/setContentWhenReady + dF.JournalAnnualReview.tagify = cF.tagify.init(...).
 * 변경 후(A-3): handlebars.modal 만 사라짐(Vue teleport 가 대체). 그 외 호출 순서/시그니처는 동일하게 옮긴다.
 *   tagify 두 번 호출(initWithCtgr + init)도 행위 보존 차원에서 그대로 유지.
 */
function attachRegFormControls(model: Record<string, any>): void {
    const dfNs = (window as any).dF;
    const module = dfNs?.JournalAnnualReview as Record<string, any> | undefined;
    if (!module) {
        console.error("[JournalAnnualReviewRegModalApp] dF.JournalAnnualReview missing.");
        return;
    }

    destroyPreviousValidator();
    cF.validate.validateForm(FORM_SELECTOR, module.regAjax);

    /* tagify — 기존 initForm 의 호출 시그니처 보존(ctgrMap undefined). */
    cF.tagify.initWithCtgr(TAGIFY_SELECTOR, undefined);

    destroyPreviousTinymce();
    cF.tinymce.init(`#${TINYMCE_ID}`);
    cF.tinymce.setContentWhenReady(TINYMCE_ID, model.content || "");

    /* 변경 이력 보존: 원본 initForm 의 후행 tagify init 동일 호출 — module.tagify 인스턴스 보관(외부 read 사이트 0이지만 행위 보존). */
    module.tagify = cF.tagify.init(TAGIFY_SELECTOR);
}

function showModal(): void {
    const modalEl = document.getElementById(MODAL_EL_ID) as HTMLElement | null;
    if (!modalEl) {
        console.error("[JournalAnnualReviewRegModalApp] Modal element not found:", MODAL_EL_ID);
        return;
    }
    const bs = (window as unknown as { bootstrap?: { Modal: { getOrCreateInstance: (el: HTMLElement) => { show: () => void } } } }).bootstrap;
    bs?.Modal.getOrCreateInstance(modalEl).show();
}

function openReg(model: Record<string, any>): void {
    state.model = { ...model };
    Vue.nextTick(function(): void {
        attachRegFormControls(state.model as Record<string, any>);
        showModal();
    });
}

function submitBridge(): void {
    const mod = (window as any).dF?.JournalAnnualReview;
    if (!mod || typeof mod.submit !== "function") {
        console.error("[JournalAnnualReviewRegModalApp] submit — dF.JournalAnnualReview.submit unavailable.");
        return;
    }
    mod.submit();
}

function createRootComponent(): Record<string, unknown> {
    return {
        name: "JournalAnnualReviewRegRoot",
        components: { JournalAnnualReviewRegModalHeader, JournalAnnualReviewRegModalBody },
        data(): { state: typeof state; headerTo: string; bodyTo: string } {
            return {
                state,
                headerTo: "#" + HEADER_TELEPORT_ID,
                bodyTo: "#" + BODY_TELEPORT_ID,
            };
        },
        template: `
        <teleport :to="headerTo">
            <JournalAnnualReviewRegModalHeader v-if="state.model" :model="state.model" />
        </teleport>
        <teleport :to="bodyTo">
            <JournalAnnualReviewRegModalBody v-if="state.model" :model="state.model" />
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
    const priorBridge = ((window as any).JournalAnnualReviewRegVueApp ?? {}) as JournalAnnualReviewRegVueBridge;
    const pending = priorBridge.pendingPayload ?? null;

    const mountEl = document.getElementById(VUE_MOUNT_ID) as HTMLElement | null;
    if (!mountEl) {
        /* 페이지에 모달 호스트가 없는 경우 브리지 mounted=false 유지(=stub). 큐잉된 payload 만 호출자에게 노출. */
        console.log("[JournalAnnualReviewRegModalApp] mount element not found:", VUE_MOUNT_ID);
        return;
    }

    state.model = null;
    openHandler = function(model: Record<string, any>): void {
        openReg(model);
    };

    const app = Vue.createApp(createRootComponent());
    app.mount("#" + VUE_MOUNT_ID);

    (window as any).JournalAnnualReviewRegVueApp = {
        mounted: true,
        pendingPayload: null,
        open: function(model: Record<string, any>): void {
            if (typeof openHandler === "function") {
                openHandler(model);
                return;
            }
            const b = (window as any).JournalAnnualReviewRegVueApp as JournalAnnualReviewRegVueBridge;
            b.pendingPayload = model;
            console.log("[JournalAnnualReviewRegModalApp] pending payload queued.");
        },
        submit: submitBridge,
    };

    /* 부트 이전에 큐잉된 payload 가 있으면 처리한다. */
    if (pending && typeof pending === "object") {
        openHandler?.(pending as Record<string, any>);
    }
});

export {};
