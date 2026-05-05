/**
 * JournalInterpretationRegModalApp.ts
 * 저널 해석 등록/수정 모달(`journal_interpretation_reg`) — Handlebars 헤더/본문 템플릿 대체.
 *
 * 변경(I-2):
 *   - _journal_interpretation_reg_modal_header_template.hbs / _journal_interpretation_reg_modal_template.hbs(빈 body) 본문 렌더와
 *     dF.JournalInterpretation.initForm 의 cF.handlebars.modal 진입을 이 모듈로 단일 수렴한다.
 *   - 저장 버튼은 `window.JournalInterpretationRegVueApp.submit()` 가 `dF.JournalInterpretation.submit()` 로 위임한다.
 * 변경(I-3):
 *   - `submit` 구현은 `journalInterpretationCrudService.submit` 로 추출되었다. 브리지 체인은 동일하다(RegVueApp → dF.JournalInterpretation.submit → CrudService).
 *   - 등록/수정 확인(regAjax)·목록 갱신 로직도 동일한 service(`journalInterpretationCrudService.regAjax`) 로 이전되었다.
 *     · 변경 이력 비고: 본 줄의 이전 표기는 "I-3 service 화 전까지"였으나 I-3 단계에서 모두 service 로 추출되었고 I-4 에서 classic 모듈도 ES module(`journalInterpretationService`) 로 수렴되었음.
 *   - jQuery validate / TinyMCE 누적 방지를 위해 open 시점에 destroy → init 순서로 부착한다.
 *   - interpretation 은 단일 contentType. tagify·chapter·preview 없음(entry 패턴 단순화).
 * 변경(I-4):
 *   - classic `journal_interpretation_module.ts` 가 삭제되고 `journalInterpretationService.ts` 가 `dF.JournalInterpretation` 표면을 등록한다.
 *     본 모달 진입의 브리지 체인은 동일(RegVueApp → dF.JournalInterpretation.<method> → State/Crud service).
 *
 * @author nichefish
 */

import { createScopedI18n } from "../../../global/services/scopedI18nService.js";
import JournalInterpretationRegModalHeader from "./components/JournalInterpretationRegModalHeader.js";

type JournalInterpretationRegVueBridge = {
    mounted?: boolean;
    pendingPayload?: Record<string, any> | null;
    open?: (model: Record<string, any>) => void;
    submit?: () => void;
};

/** Vue mount point id (FTLH 가 만든 빈 div). */
const VUE_MOUNT_ID = "journal_interpretation_reg_vue_app";
/** Vue 가 헤더 마크업을 teleport 할 대상 div id. */
const HEADER_TELEPORT_ID = "journal_interpretation_reg_modal_header_div";
/** form id. */
const FORM_SELECTOR = "#journalInterpretationRegForm";
/** TinyMCE editor id (textarea id 와 동일). */
const TINYMCE_ID = "tinymce_journalInterpretationCn";
/** Bootstrap 모달 element id (hbs `journal_interpretation_reg_modal`). */
const MODAL_EL_ID = "journal_interpretation_reg_modal";
/** imprtcYn 체크박스 라벨 — 기존 initForm 동작 보존. 폼에 #imprtcYn element 가 없으면 noop. */
const IMPRTC_LABEL_SELECTOR = "#journalInterpretationRegForm #imprtcYn";

const state: { model: Record<string, any> | null } = { model: null };
let openHandler: ((model: Record<string, any>) => void) | null = null;

const i18n = createScopedI18n();

function t(key: string): string {
    return i18n.t(key);
}

function resolveLocale(): string {
    const win = typeof window !== "undefined" ? (window as Window & { Model?: { locale?: string } }) : undefined;
    const loc = win?.Model?.locale;
    if (loc) return loc;
    return (document.documentElement.lang || "ko").replace(/_/g, "-");
}

function destroyPreviousValidator(): void {
    const $form = $(FORM_SELECTOR);
    const validator = $form.data("validator") as { destroy?: () => void } | undefined;
    if (validator && typeof validator.destroy === "function") {
        try {
            validator.destroy();
        } catch (e) {
            console.warn("[JournalInterpretationRegModalApp] jQuery validate destroy failed", e);
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
        console.warn("[JournalInterpretationRegModalApp] tinymce destroy failed", e);
    }
}

/**
 * 변경 전: dF.JournalInterpretation.initForm — cF.handlebars.modal + cF.validate.validateForm + cF.ui.chckboxLabel + cF.tinymce.init/setContentWhenReady.
 * 변경 후(I-2): 동일 호출 순서를 Vue 오픈 경로에서 수행한다(handlebars.modal 만 사라짐 — Vue teleport 가 대체).
 */
function attachRegFormControls(model: Record<string, any>): void {
    const dfNs = (window as any).dF;
    const module = dfNs?.JournalInterpretation as Record<string, any> | undefined;
    if (!module) {
        console.error("[JournalInterpretationRegModalApp] dF.JournalInterpretation missing.");
        return;
    }

    destroyPreviousValidator();
    cF.validate.validateForm(FORM_SELECTOR, module.regAjax);

    /* imprtcYn 체크박스 라벨 — interpretation 폼에 #imprtcYn 이 현재 없어도 호출 자체는 보존(기존 initForm 동작). */
    cF.ui.chckboxLabel(IMPRTC_LABEL_SELECTOR, "중요//해당없음", "red//gray");

    destroyPreviousTinymce();
    cF.tinymce.init(`#${TINYMCE_ID}`);
    cF.tinymce.setContentWhenReady(TINYMCE_ID, model.content || "");
}

function showModal(): void {
    const modalEl = document.getElementById(MODAL_EL_ID) as HTMLElement | null;
    if (!modalEl) {
        console.error("[JournalInterpretationRegModalApp] Modal element not found:", MODAL_EL_ID);
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
    const mod = (window as any).dF?.JournalInterpretation;
    if (!mod || typeof mod.submit !== "function") {
        console.error("[JournalInterpretationRegModalApp] submit — dF.JournalInterpretation.submit unavailable.");
        return;
    }
    mod.submit();
}

function createRootComponent(): Record<string, unknown> {
    return {
        name: "JournalInterpretationRegRoot",
        components: { JournalInterpretationRegModalHeader },
        data(): { state: typeof state; headerTo: string } {
            return {
                state,
                headerTo: "#" + HEADER_TELEPORT_ID,
            };
        },
        template: `
        <teleport :to="headerTo">
            <JournalInterpretationRegModalHeader v-if="state.model" :model="state.model" />
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

runWhenDomReady(async function(): Promise<void> {
    await i18n.load(resolveLocale());

    const priorBridge = (window.JournalInterpretationRegVueApp ?? {}) as JournalInterpretationRegVueBridge;
    const pending = priorBridge.pendingPayload ?? null;

    const mountEl = document.getElementById(VUE_MOUNT_ID) as HTMLElement | null;
    if (!mountEl) {
        /* 단일 진입점 페이지가 없으면 브리지만 mounted=false 로 유지(=stub). 호출자는 pending payload 큐로 폴백. */
        console.log("[JournalInterpretationRegModalApp] mount element not found:", VUE_MOUNT_ID);
        return;
    }

    state.model = null;
    openHandler = function(model: Record<string, any>): void {
        openReg(model);
    };

    const app = Vue.createApp(createRootComponent());
    app.config.globalProperties.$t = (key: string): string => t(key);
    app.mount("#" + VUE_MOUNT_ID);

    window.JournalInterpretationRegVueApp = {
        mounted: true,
        pendingPayload: null,
        open: function(model: Record<string, any>): void {
            if (typeof openHandler === "function") {
                openHandler(model);
                return;
            }
            const b = window.JournalInterpretationRegVueApp as JournalInterpretationRegVueBridge;
            b.pendingPayload = model;
            console.log("[JournalInterpretationRegModalApp] pending payload queued.");
        },
        submit: submitBridge,
    };

    /* 부트 시점 이전에 큐잉된 payload 가 있으면 처리한다. */
    if (pending && typeof pending === "object") {
        openHandler?.(pending as Record<string, any>);
    }
});

export {};
