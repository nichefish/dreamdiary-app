/**
 * JournalTodoRegModalApp.ts
 * 저널 할일 등록/수정 모달(`journal_todo_reg`) 진입 — Handlebars 템플릿 대체.
 *
 * 변경(T-2-α):
 *   - _journal_todo_reg_modal_header_template.hbs / _journal_todo_reg_modal_template.hbs 본문 렌더와
 *     dF.JournalTodo 의 regModal/mdfModal/initForm/submit/regAjax 진입을 이 모듈로 단일 수렴한다.
 *   - 외부 호출(`dF.JournalTodo.regModal()` / `dF.JournalTodo.mdfModal(id)`) 은
 *     `window.JournalTodoRegVueApp.open(model)` / `window.JournalTodoRegVueApp.submit()` 큐로 대체된다.
 *   - regAjax 후속 처리(저장 후 aside todo 목록 갱신) 는 기존 dF.JournalTodo.regAjax 의 분기를 그대로 옮긴다.
 *     α 시점에서는 list 갱신을 dF.JournalTodo.yyMnthListAjax 로 그대로 호출하고, β 단계에서
 *     journalTodoCrudService 로 교체된다.
 *   - tinymce / tagify / jquery validator 는 기존 initForm 동작을 유지하되, Vue 모달 재오픈 시
 *     누적 인스턴스 destroy 를 추가한다(기존 HBS modal 은 매번 body 를 새로 렌더해 누적이 가시화되지 않았다).
 *   - dF.JournalTodoTag.ctgrMap 은 본 마이그레이션 전부터 dead(=undefined) 였다. 사용자 결정에 따라
 *     dead UI 를 그대로 보존하므로 빈 객체({}) 로 대체해 동일 호출 시그니처를 유지한다.
 *
 * @author nichefish
 */

import { createScopedI18n } from "../../../global/services/scopedI18nService.js";
import JournalTodoRegModalHeader from "./components/JournalTodoRegModalHeader.js";
import JournalTodoRegModalBody from "./components/JournalTodoRegModalBody.js";
import journalTodoCrudService from "./services/journalTodoCrudService.js";

type JournalTodoRegVueBridge = {
    mounted?: boolean;
    open?: (model: Record<string, any>) => void;
    submit?: () => void;
    pendingPayload?: Record<string, any> | null;
};

const state = Vue.reactive({
    model: null as Record<string, any> | null,
});

const i18n = createScopedI18n();

function t(key: string): string {
    return i18n.t(key);
}

function resolveLocale(): string {
    const win = typeof window !== "undefined" ? (window as Window & { Model?: { locale?: string } }) : undefined;
    const locale = win?.Model?.locale;
    if (locale) return locale;
    return (document.documentElement.lang || "ko").replace(/_/g, "-");
}

/**
 * 서버 응답(JournalTodoDto) 또는 등록 진입 객체를 폼 모델로 정규화한다.
 */
function normalizeRegModel(obj: Record<string, any>): Record<string, any> {
    const tagSrc = (obj as any).tag;
    return {
        ...obj,
        id: obj.id ?? "",
        yy: obj.yy ?? "",
        mnth: obj.mnth ?? "",
        title: obj.title ?? "",
        sortOrder: obj.sortOrder ?? null,
        categoryCode: obj.categoryCode ?? "",
        content: obj.content ?? "",
        tag: (tagSrc != null && typeof tagSrc === "object")
            ? { ...tagSrc, tagListStrWithCtgr: tagSrc.tagListStrWithCtgr ?? "" }
            : { tagListStrWithCtgr: "" },
    };
}

function showRegModal(): void {
    const modalEl = document.querySelector("#journal_todo_reg_modal") as HTMLElement | null;
    if (!modalEl) {
        console.error("[JournalTodoRegModalApp] Modal root #journal_todo_reg_modal not found.");
        return;
    }
    const bs = (window as unknown as { bootstrap?: { Modal: { getOrCreateInstance: (el: HTMLElement) => { show: () => void } } } }).bootstrap;
    bs?.Modal.getOrCreateInstance(modalEl).show();
}

/**
 * 이전 모달 오픈에서 붙은 jQuery Validation 인스턴스를 제거한다.
 * 변경 전: dF.JournalTodo.initForm 은 매번 cF.validate.validateForm 만 호출.
 *   동일 form 에 검증기가 누적되어도 별도 destroy 처리는 없었다.
 * 변경 후(T-2-α): Vue 모달 재오픈 시 안전하게 검증기를 정리한다(누적 방지).
 */
function destroyPreviousValidator(): void {
    const $form = $("#journalTodoRegForm");
    const validator = $form.data("validator") as { destroy?: () => void } | undefined;
    if (validator && typeof validator.destroy === "function") {
        try {
            validator.destroy();
        } catch (e) {
            console.warn("[JournalTodoRegModalApp] jQuery validate destroy failed", e);
        }
    }
    $form.removeData("validator");
}

let regTagify: any = null;
/**
 * 이전 모달 오픈에서 init 된 tagify 를 정리한다.
 * 변경 전: cF.handlebars.modal 은 모달 body 를 새로 렌더 → 입력 DOM 자체가 교체되어 누적이 가시화되지 않았다.
 * 변경 후(T-2-α): Vue 모달은 mount 점이 유지되므로 동일 input 위에 누적되는 것을 막는다.
 */
function destroyPreviousTagify(): void {
    if (regTagify && typeof regTagify.destroy === "function") {
        try {
            regTagify.destroy();
        } catch (e) {
            console.warn("[JournalTodoRegModalApp] tagify destroy failed", e);
        }
    }
    regTagify = null;
}

/**
 * 이전 모달 오픈에서 init 된 tinymce 인스턴스를 정리한다.
 * 변경 전: cF.handlebars.modal 이 textarea DOM 자체를 갈아끼워 tinymce 재초기화가 자연스럽게 일어났다.
 * 변경 후(T-2-α): textarea 는 ftlh 정적 노드이므로 누적 방지를 위해 명시적으로 destroy 후 init.
 */
function destroyPreviousTinymce(): void {
    try {
        if (typeof tinymce === "undefined") return;
        const editor: any = (tinymce as any).get("tinymce_journalTodoCn");
        if (editor && typeof editor.destroy === "function") editor.destroy();
    } catch (e) {
        console.warn("[JournalTodoRegModalApp] tinymce destroy failed", e);
    }
}

/**
 * 등록/수정 처리 (Ajax). 변경 전: dF.JournalTodo.regAjax — 성공 시 yyMnthListAjax + ModalHistory.reset.
 * 변경 후(T-2-α): 동일 분기 흐름을 Vue 모달 모듈 내부로 그대로 이전한다.
 *   목록 갱신 호출은 α 시점에는 dF.JournalTodo.yyMnthListAjax 그대로 호출하고,
 *   β 단계에서 journalTodoCrudService.yyMnthListAjax 로 교체된다.
 */
function regAjax(): void {
    const id: string = cF.util.getInputValue("#journalTodoRegForm [name='id']");
    const isMdf: boolean = cF.util.isNotEmpty(id);
    Swal.fire({
        text: Message.get(isMdf ? "view.cnfm.mdf" : "view.cnfm.reg"),
        showCancelButton: true,
    }).then(function(result: SwalResult): void {
        if (!result.value) return;

        const url: string = isMdf ? cF.util.bindUrl(Url.JOURNAL_TODO, { id }) : Url.JOURNAL_TODOS;
        const ajaxData: FormData = new FormData(document.getElementById("journalTodoRegForm") as HTMLFormElement);
        cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
            Swal.fire({ text: res.message })
                .then(function(): void {
                    if (!res.rslt) return;

                    /* 변경(T-2-β): dF.JournalTodo.yyMnthListAjax → journalTodoCrudService.yyMnthListAjax 단일 진입. */
                    journalTodoCrudService.yyMnthListAjax();

                    /* 모달 이력 되돌리기 */
                    ModalHistory.reset();
                });
        }, "block");
    });
}

/**
 * 폼 컨트롤(jQuery Validate / tinymce / tagify) 부착.
 * 변경 전: dF.JournalTodo.initForm — cF.handlebars.modal + validateForm + tinymce.init/setContent + tagify.initWithCtgr.
 * 변경 후(T-2-α): handlebars.modal 진입은 사라지고, 검증/플러그인 init 만 Vue 측에서 동일 호출 한다.
 */
function attachRegFormControls(model: Record<string, any>): void {
    destroyPreviousValidator();
    cF.validate.validateForm("#journalTodoRegForm", regAjax);

    destroyPreviousTinymce();
    cF.tinymce.init('#tinymce_journalTodoCn');
    cF.tinymce.setContentWhenReady("tinymce_journalTodoCn", model.content || "");

    /* dF.JournalTodoTag.ctgrMap 은 dead(undefined). dead UI 보존 결정에 따라 빈 객체로 호출. */
    destroyPreviousTagify();
    const ctgrMap: Record<string, unknown> = (
        (typeof dF !== "undefined")
        && ((dF as any).JournalTodoTag)
        && ((dF as any).JournalTodoTag.ctgrMap)
    ) ? ((dF as any).JournalTodoTag.ctgrMap as Record<string, unknown>) : {};
    regTagify = cF.tagify.initWithCtgr("#journalTodoRegForm #tagListStr", ctgrMap);
}

/**
 * 저장 버튼(footer) 트리거. 변경 전: dF.JournalTodo.submit — tinymce save + form submit.
 * 변경 후(T-2-α): 동일 흐름을 Vue 모달 모듈에서 단일 진입.
 */
function submit(): void {
    try {
        if (typeof tinymce !== "undefined") {
            const editor: any = (tinymce as any).get("tinymce_journalTodoCn");
            if (editor && typeof editor.save === "function") editor.save();
        }
    } catch (e) {
        console.warn("[JournalTodoRegModalApp] tinymce save failed", e);
    }
    $("#journalTodoRegForm").submit();
}

/**
 * 모달 오픈 — 외부에서 `window.JournalTodoRegVueApp.open(model)` 단일 진입.
 * model 은 등록 진입 시 `{ yy, mnth }`, 수정 진입 시 서버 응답 JournalTodoDto 객체.
 */
function openReg(model: Record<string, any>): void {
    state.model = normalizeRegModel(model);
    Vue.nextTick(function(): void {
        attachRegFormControls(state.model as Record<string, any>);
        showRegModal();
    });
}

const JournalTodoRegRootApp = {
    name: "JournalTodoRegRootApp",
    components: {
        JournalTodoRegModalHeader,
        JournalTodoRegModalBody,
    },
    data(): { state: typeof state } {
        return { state };
    },
    template: `
    <teleport to="#journal_todo_reg_modal_header_div">
        <JournalTodoRegModalHeader v-if="state.model" :model="state.model" />
    </teleport>
    <teleport to="#journal_todo_reg_div">
        <JournalTodoRegModalBody v-if="state.model" :model="state.model" />
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
    const mountEl = document.querySelector("#journal_todo_reg_vue_app") as HTMLElement | null;
    if (!mountEl) {
        console.error("[JournalTodoRegModalApp] Mount root #journal_todo_reg_vue_app not found.");
        return;
    }

    await i18n.load(resolveLocale());

    const priorBridge = (window.JournalTodoRegVueApp ?? {}) as JournalTodoRegVueBridge;
    const pendingPayload: Record<string, any> | null | undefined = priorBridge.pendingPayload;

    const app = Vue.createApp(JournalTodoRegRootApp);
    app.config.globalProperties.$t = (key: string): string => t(key);
    app.mount("#journal_todo_reg_vue_app");

    window.JournalTodoRegVueApp = {
        mounted: true,
        pendingPayload: null,
        open: openReg,
        submit: submit,
    };

    if (pendingPayload && typeof pendingPayload === "object") {
        openReg(pendingPayload);
    }
});

export {};