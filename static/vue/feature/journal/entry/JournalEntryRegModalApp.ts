/**
 * JournalEntryRegModalApp.ts
 * 저널 entry 등록/수정 모달(`journal_*_reg`) — Handlebars 헤더/본문 템플릿 대체.
 *
 * 변경(E-2):
 *   - _journal_entry_reg_modal_header_template.hbs / _journal_entry_reg_modal_body_template.hbs 본문 렌더와
 *     dF.JournalEntry.[content].initForm 의 cF.handlebars.modal 진입을 이 모듈로 단일 수렴한다.
 *   - 저장/미리보기 버튼은 `window.JournalEntryRegVueApp.submit(contentType)` /
 *     `.preview(contentType)` 가 `dF.JournalEntry.get(contentType).submit/preview` 로 위임한다.
 *   - 등록/수정 확인(regAjax)·목록 갱신 로직은 기존 journal_entry_module 내 동일 메서드에 그대로 둔다(E-3 service 화 전까지).
 *
 * @author nichefish
 */

import { createScopedI18n } from "../../../global/services/scopedI18nService.js";
import JournalEntryRegModalHeader from "./components/JournalEntryRegModalHeader.js";
import JournalEntryRegModalBody from "./components/JournalEntryRegModalBody.js";

type JournalEntryRegVueBridge = {
    mounted?: boolean;
    pendingPayloads?: Record<string, Record<string, any> | null>;
    open?: (contentType: string, model: Record<string, any>) => void;
    submit?: (contentType: string) => void;
    preview?: (contentType: string) => void;
};

const CONTENT_TYPES = ["JOURNAL_DIARY", "JOURNAL_DREAM", "JOURNAL_NOTE"] as const;

const HEADER_TELEPORT: Record<string, string> = {
    JOURNAL_DIARY: "#journal_diary_reg_modal_header_div",
    JOURNAL_DREAM: "#journal_dream_reg_modal_header_div",
    JOURNAL_NOTE: "#journal_entry_reg_modal_header_div",
};

const BODY_TELEPORT: Record<string, string> = {
    JOURNAL_DIARY: "#journal_diary_reg_div",
    JOURNAL_DREAM: "#journal_dream_reg_div",
    JOURNAL_NOTE: "#journal_entry_reg_div",
};

const VUE_MOUNT: Record<string, string> = {
    JOURNAL_DIARY: "#journal_diary_reg_vue_app",
    JOURNAL_DREAM: "#journal_dream_reg_vue_app",
    JOURNAL_NOTE: "#journal_entry_reg_vue_app",
};

const states: Record<string, { model: Record<string, any> | null }> = {};
const openHandlers: Record<string, (model: Record<string, any>) => void> = {};
const tagifyByCt: Record<string, any> = {};

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

function normalizeRegModel(obj: Record<string, any>): Record<string, any> {
    const tagSrc = (obj as any).tag;
    const chapterList = Array.isArray(obj.chapterList)
        ? obj.chapterList
        : (Array.isArray(obj.journalChapterList) ? obj.journalChapterList : []);
    return {
        ...obj,
        chapterList,
        tag: (tagSrc != null && typeof tagSrc === "object")
            ? { ...tagSrc, tagListStrWithCtgr: tagSrc.tagListStrWithCtgr ?? "" }
            : { tagListStrWithCtgr: "" },
    };
}

function destroyPreviousValidator(formSelector: string): void {
    const $form = $(formSelector);
    const validator = $form.data("validator") as { destroy?: () => void } | undefined;
    if (validator && typeof validator.destroy === "function") {
        try {
            validator.destroy();
        } catch (e) {
            console.warn("[JournalEntryRegModalApp] jQuery validate destroy failed", e);
        }
    }
    $form.removeData("validator");
}

function destroyPreviousTinymce(tinymceId: string): void {
    try {
        if (typeof tinymce === "undefined") return;
        const editor: any = (tinymce as any).get(tinymceId);
        if (editor && typeof editor.destroy === "function") editor.destroy();
    } catch (e) {
        console.warn("[JournalEntryRegModalApp] tinymce destroy failed", e);
    }
}

function destroyPreviousTagify(contentType: string): void {
    const prev = tagifyByCt[contentType];
    if (prev && typeof prev.destroy === "function") {
        try {
            prev.destroy();
        } catch (e) {
            console.warn("[JournalEntryRegModalApp] tagify destroy failed", e);
        }
    }
    delete tagifyByCt[contentType];
}

/**
 * 변경 전: dF.JournalEntry.[ct].initForm — cF.handlebars.modal + plugin.setupFormValidation + tinymce + tagify.
 * 변경 후(E-2): 동일 호출 순서를 Vue 오픈 경로에서 수행한다.
 */
function attachRegFormControls(contentType: string, model: Record<string, any>): void {
    const df = (window as any).dF;
    const meta = df?.JournalEntry?.getMeta?.(contentType) as Record<string, any> | undefined;
    const module = df?.JournalEntry?.get?.(contentType) as Record<string, any> | undefined;
    if (!meta || !module) {
        console.error("[JournalEntryRegModalApp] JournalEntry meta/module missing:", contentType);
        return;
    }

    destroyPreviousValidator(meta.formSelector);
    meta.plugin.setupFormValidation(meta, module);

    destroyPreviousTinymce(meta.tinymceId);
    cF.tinymce.init(`#${meta.tinymceId}`);
    cF.tinymce.setContentWhenReady(meta.tinymceId, model.content || "");

    destroyPreviousTagify(contentType);
    if (meta.useTag) {
        const tagNs = df?.JournalEntryTag?.get?.(contentType);
        const ctgrMap = tagNs?.ctgrMap ?? new Map();
        const tf = cF.tagify.initWithCtgr(meta.tagInputSelector, ctgrMap);
        tagifyByCt[contentType] = tf;
        module.tagify = tf;
    }
}

function showModal(contentType: string): void {
    const df = (window as any).dF;
    const meta = df?.JournalEntry?.getMeta?.(contentType) as Record<string, any> | undefined;
    if (!meta?.modalKey) return;
    const modalEl = document.getElementById(`${meta.modalKey}_modal`) as HTMLElement | null;
    if (!modalEl) {
        console.error("[JournalEntryRegModalApp] Modal not found:", meta.modalKey);
        return;
    }
    const bs = (window as unknown as { bootstrap?: { Modal: { getOrCreateInstance: (el: HTMLElement) => { show: () => void } } } }).bootstrap;
    bs?.Modal.getOrCreateInstance(modalEl).show();
}

function openReg(contentType: string, model: Record<string, any>): void {
    const st = states[contentType];
    if (!st) {
        console.error("[JournalEntryRegModalApp] unknown contentType or mount missing:", contentType);
        return;
    }
    st.model = normalizeRegModel(model);
    Vue.nextTick(function(): void {
        attachRegFormControls(contentType, st.model as Record<string, any>);
        showModal(contentType);
    });
}

function submitBridge(contentType: string): void {
    const mod = (window as any).dF?.JournalEntry?.get?.(contentType);
    if (!mod || typeof mod.submit !== "function") {
        console.error("[JournalEntryRegModalApp] submit — dF.JournalEntry.get('" + contentType + "').submit unavailable.");
        return;
    }
    mod.submit();
}

function previewBridge(contentType: string): void {
    const mod = (window as any).dF?.JournalEntry?.get?.(contentType);
    if (!mod || typeof mod.preview !== "function") {
        console.error("[JournalEntryRegModalApp] preview — dF.JournalEntry.get('" + contentType + "').preview unavailable.");
        return;
    }
    mod.preview();
}

function createRootComponent(contentType: string): Record<string, unknown> {
    const st = states[contentType];
    return {
        name: "JournalEntryRegRoot_" + contentType,
        components: {
            JournalEntryRegModalHeader,
            JournalEntryRegModalBody,
        },
        data(): { state: typeof st; contentType: string; headerTo: string; bodyTo: string } {
            return {
                state: st,
                contentType,
                headerTo: HEADER_TELEPORT[contentType],
                bodyTo: BODY_TELEPORT[contentType],
            };
        },
        template: `
        <teleport :to="headerTo">
            <JournalEntryRegModalHeader v-if="state.model" :content-type="contentType" :model="state.model" />
        </teleport>
        <teleport :to="bodyTo">
            <JournalEntryRegModalBody v-if="state.model" :content-type="contentType" :model="state.model" />
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

    const priorBridge = (window.JournalEntryRegVueApp ?? {}) as JournalEntryRegVueBridge;
    const pending = priorBridge.pendingPayloads ?? {};

    const dfNs = (window as any).dF;
    for (let i = 0; i < CONTENT_TYPES.length; i++) {
        const ct = CONTENT_TYPES[i];
        if (!dfNs?.JournalEntry?.getMeta?.(ct)) continue;

        const mountSel = VUE_MOUNT[ct];
        const mountEl = document.querySelector(mountSel) as HTMLElement | null;
        if (!mountEl) continue;

        states[ct] = Vue.reactive({ model: null as Record<string, any> | null });
        openHandlers[ct] = function(model: Record<string, any>): void {
            openReg(ct, model);
        };

        const app = Vue.createApp(createRootComponent(ct));
        app.config.globalProperties.$t = (key: string): string => t(key);
        app.mount(mountSel);
    }

    window.JournalEntryRegVueApp = {
        mounted: true,
        pendingPayloads: {},
        open: function(contentType: string, model: Record<string, any>): void {
            const fn = openHandlers[contentType];
            if (typeof fn === "function") {
                fn(model);
                return;
            }
            const b = window.JournalEntryRegVueApp as JournalEntryRegVueBridge;
            b.pendingPayloads = b.pendingPayloads ?? {};
            b.pendingPayloads[contentType] = model;
            console.log("[JournalEntryRegModalApp] pending payload queued:", contentType);
        },
        submit: submitBridge,
        preview: previewBridge,
    };

    const pp = pending;
    Object.keys(pp).forEach(function(k: string): void {
        const payload = pp[k];
        if (payload && typeof payload === "object") {
            openHandlers[k]?.(payload as Record<string, any>);
        }
    });
});

export {};
