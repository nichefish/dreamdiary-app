/**
 * JournalChapterRegModalApp.ts
 * 저널 챕터 등록/수정 모달(`journal_chapter_reg`) 본문 렌더 — Handlebars 템플릿 대체.
 *
 * 변경(Phase B):
 *   - _journal_chapter_reg_modal_template.hbs(HBS) 본문 렌더와
 *     dF.JournalChapter 의 regModal/mdfModal/initForm/submit/regAjax 진입을 이 모듈로 단일 수렴한다.
 *   - 기존 외부 호출(`dF.JournalChapter.regModal({...})` / `dF.JournalChapter.mdfModal(id, ...)`)은
 *     `window.JournalChapterRegVueApp.open(model)` / `window.JournalChapterRegVueApp.submit()` 큐로 대체된다.
 *   - regAjax 후속 처리(저장 후 목록 갱신·태그 패널 갱신)는 기존 dF.JournalChapter.regAjax 의 분기를 그대로 옮긴다.
 *     CAL 화면(JournalDayCalVueApp.mounted)에서는 cal refresh + dF.JournalDayTagService.refreshDayTagList() 보존.
 *
 * @author nichefish
 */

import { getJournalDayListBridge } from "../day/journalDayListBridge.js";
import { createScopedI18n } from "../../../global/services/scopedI18nService.js";
import JournalChapterRegModalBody from "./components/JournalChapterRegModalBody.js";

type JournalChapterRegVueBridge = {
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
 * FTL 부트스트랩(`__journalChapterRegBootstrap.categoryOptions`) 에서 카테고리 옵션을 읽어온다.
 * - SSOT: 서버 모델 `JOURNAL_CHAPTER_CTGR_CD` (FTL `_journal_chapter_reg_modal.ftlh` 인라인 적재).
 * - 페이지 컨트롤러가 적재하지 않은 페이지(예: journal_entry_search) 에서는 빈 배열로 노출된다.
 */
function readCategoryOptions(): Array<{ code: string; codeName: string }> {
    const raw = (window as any).__journalChapterRegBootstrap?.categoryOptions;
    return Array.isArray(raw) ? raw : [];
}

/**
 * 서버 응답(JournalChapterDto) 또는 등록 진입 객체를 폼 모델로 정규화한다.
 * categoryOptions 는 매 open 시 부트스트랩에서 새로 읽어 주입한다.
 */
function normalizeRegModel(obj: Record<string, any>): Record<string, any> {
    return {
        ...obj,
        chapterType: obj.chapterType ?? "DIARY",
        categoryCode: obj.categoryCode ?? "",
        title: obj.title ?? "",
        sortOrder: obj.sortOrder ?? null,
        journalDateWeekDay: obj.journalDateWeekDay ?? "",
        categoryOptions: readCategoryOptions(),
    };
}

function showRegModal(): void {
    const modalEl = document.querySelector("#journal_chapter_reg_modal") as HTMLElement | null;
    if (!modalEl) {
        console.error("[JournalChapterRegModalApp] Modal root #journal_chapter_reg_modal not found.");
        return;
    }
    const bs = (window as unknown as { bootstrap?: { Modal: { getOrCreateInstance: (el: HTMLElement) => { show: () => void } } } }).bootstrap;
    bs?.Modal.getOrCreateInstance(modalEl).show();
}

/**
 * 이전 모달 오픈에서 붙은 jQuery Validation 인스턴스를 제거한다.
 * 변경 전: cF.handlebars.modal 진입 시 dF.JournalChapter.initForm 이 매번 cF.validate.validateForm 만 호출.
 *   동일 form 에 검증기가 누적되어도 별도 destroy 처리는 없었다.
 * 변경 후(Phase B): Vue 모달 재오픈 시 안전하게 검증기를 정리한다(누적 방지).
 */
function destroyPreviousValidator(): void {
    const $form = $("#journalChapterRegForm");
    const validator = $form.data("validator") as { destroy?: () => void } | undefined;
    if (validator && typeof validator.destroy === "function") {
        try {
            validator.destroy();
        } catch (e) {
            console.warn("[JournalChapterRegModalApp] jQuery validate destroy failed", e);
        }
    }
    $form.removeData("validator");
}

/**
 * 등록/수정 처리 (Ajax).
 * 변경 전: dF.JournalChapter.regAjax — JournalDayCalVueApp.refresh / journalDayResolveListBridge().refresh 직접 호출.
 *   CAL 분기에서는 dF.JournalDayTagService.refreshDayTagList() 도 함께 호출했다.
 * 변경 후(Phase B): 동일 분기 흐름을 Vue 모달 모듈 내부로 그대로 이전한다.
 */
function regAjax(): void {
    const id: string = cF.util.getInputValue("#journalChapterRegForm [name='id']");
    const isMdf: boolean = cF.util.isNotEmpty(id);
    Swal.fire({
        text: Message.get(isMdf ? "view.cnfm.mdf" : "view.cnfm.reg"),
        showCancelButton: true,
    }).then(function(result: SwalResult): void {
        if (!result.value) return;

        const url: string = isMdf ? cF.util.bindUrl(Url.JOURNAL_CHAPTER, { id }) : Url.JOURNAL_CHAPTERS;
        const ajaxData: FormData = new FormData(document.getElementById("journalChapterRegForm") as HTMLFormElement);
        cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
            Swal.fire({ text: res.message })
                .then(function(): void {
                    if (!res.rslt) return;

                    /* 변경 전: dF.JournalChapter.regAjax 안에서 동일 분기 처리.
                     * CAL 화면(JournalDayCalVueApp.mounted)에서도 호출될 수 있어 CAL 감지 포함. */
                    if (window.JournalDayCalVueApp?.mounted === true) {
                        window.JournalDayCalVueApp.refresh?.();
                        /* 변경 후: 챕터 CRUD 후 태그 패널 갱신 — dF.JournalDayTagService 단일 진입점 호출. */
                        (window as any).dF?.JournalDayTagService?.refreshDayTagList?.();
                    } else {
                        getJournalDayListBridge()?.refresh?.();
                    }
                    cF.ui.unblockUI();
                    ModalHistory.reset();
                });
        }, "block");
    });
}

/**
 * 폼 컨트롤(jQuery Validate) 부착.
 * 변경 전: dF.JournalChapter.initForm — `cF.validate.validateForm("#journalChapterRegForm", dF.JournalChapter.regAjax)` 호출.
 * 변경 후(Phase B): Vue 모달 측 regAjax 를 직접 검증 콜백으로 부착한다.
 */
function attachRegFormControls(): void {
    destroyPreviousValidator();
    cF.validate.validateForm("#journalChapterRegForm", regAjax);
}

/**
 * 저장 버튼(footer) 트리거. 폼 submit → jQuery validate → regAjax 흐름 진입.
 */
function submit(): void {
    $("#journalChapterRegForm").submit();
}

/**
 * 모달 오픈 — 외부에서 `window.JournalChapterRegVueApp.open(model)` 단일 진입.
 * model 은 등록 진입 시 `{ journalDayId, stdrdDt, journalDateWeekDay, chapterType }`,
 * 수정 진입 시 서버 응답 JournalChapterDto 객체.
 */
function openReg(model: Record<string, any>): void {
    state.model = normalizeRegModel(model);
    Vue.nextTick(function(): void {
        attachRegFormControls();
        showRegModal();
    });
}

const JournalChapterRegRootApp = {
    name: "JournalChapterRegRootApp",
    components: {
        JournalChapterRegModalBody,
    },
    data(): { state: typeof state } {
        return { state };
    },
    template: `
    <teleport to="#journal_chapter_reg_div">
        <JournalChapterRegModalBody v-if="state.model" :model="state.model" />
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
    const mountEl = document.querySelector("#journal_chapter_reg_vue_app") as HTMLElement | null;
    if (!mountEl) {
        console.error("[JournalChapterRegModalApp] Mount root #journal_chapter_reg_vue_app not found.");
        return;
    }

    await i18n.load(resolveLocale());

    const priorBridge = (window.JournalChapterRegVueApp ?? {}) as JournalChapterRegVueBridge;
    const pendingPayload: Record<string, any> | null | undefined = priorBridge.pendingPayload;

    const app = Vue.createApp(JournalChapterRegRootApp);
    app.config.globalProperties.$t = (key: string): string => t(key);
    app.mount("#journal_chapter_reg_vue_app");

    window.JournalChapterRegVueApp = {
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
