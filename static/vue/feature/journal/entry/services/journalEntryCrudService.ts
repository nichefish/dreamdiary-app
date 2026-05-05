/**
 * journalEntryCrudService.ts
 * 저널 entry CRUD/모달 액션 서비스 (Vue 소유, dF 글로벌 등록).
 *
 * 변경(E-3-α):
 *   - journal_entry_module.ts 의 CRUD 메서드(regAjax/delAjax/copy/regModal/mdfModal/dtlModal/
 *     submit/preview/submitHandler/openRegModalWithDayContext/createDreamChapterAndOpenModal)
 *     를 본 서비스로 추출한다. 모듈은 동일 시그니처의 thin wrapper 만 유지한다.
 * 변경(E-4-δ-2):
 *   - `openRegModalWithDayContext` 의 챕터 목록·등록 폼 진입은 `journalEntryShellService` 와 공유한다.
 *     변경 전: `module.resolveChapterList` / `module.initForm` (`dF.JournalEntry` 미등록 시 실패).
 *   - 외부 호출(`dF.JournalEntry.get(ct).copy(id)` 등)은 모듈 표면을 통해 그대로 진입한다.
 *   - 호출 그래프 보존: HBS onclick(`dF.JournalEntry.get('JOURNAL_DREAM').nhtmrAjax(...)`),
 *     Vue 컴포넌트, related_content_module 모두 기존 진입점 유지.
 *   - 서비스는 stateless. 모듈 상태(`viewType`/`tagify`/`submitMode`)는 모듈이 보유하고,
 *     서비스는 `dF.JournalEntry.get(ct)` 조회로 접근한다.
 *   - 본 서비스는 글로벌(`dF.JournalEntryCrudService`) 로도 노출되어 classic 모듈에서 호출 가능하다.
 *
 * @author nichefish
 */

import { openJournalEntryRegForm, resolveJournalEntryChapterList } from "./journalEntryShellService.js";

type EntryMeta = Record<string, any>;
type EntryModule = Record<string, any>;

function getMeta(contentType: string): EntryMeta | undefined {
    return ((window as any).dF?.JournalEntry?.getMeta?.(contentType)) as EntryMeta | undefined;
}

function getModule(contentType: string): EntryModule | undefined {
    return ((window as any).dF?.JournalEntry?.get?.(contentType)) as EntryModule | undefined;
}

function getEntrySearchService(contentType: string): Record<string, any> | undefined {
    return ((window as any).dF?.JournalEntrySearch?.get?.(contentType)) as Record<string, any> | undefined;
}

function closeOpenModals(): void {
    document.querySelectorAll(".modal.show").forEach(function(modal: Element): void {
        ($ as any)(modal).modal("hide");
    });
}

/**
 * `dF.JournalEntry.get(ct).regModal({...})` 진입.
 * 변경 전: journal_entry_module.ts 내부 메서드.
 * 변경 후(E-3-α): 동일 흐름을 서비스로 이전.
 */
export function regModal(
    contentType: string,
    params: {
        journalDayId: string|number;
        journalChapterId?: string|number;
        stdrdDt: string;
        journalDateWeekDay: string;
    }
): void {
    const { journalDayId, journalChapterId, stdrdDt, journalDateWeekDay } = params;
    if (isNaN(Number(journalDayId))) return;
    if (journalChapterId != null && isNaN(Number(journalChapterId))) return;

    openRegModalWithDayContext(contentType, journalDayId, journalChapterId, stdrdDt, journalDateWeekDay);
}

/**
 * 저널 일자 컨텍스트(챕터 목록) 조회 후 등록 모달 진입.
 * 변경 전: journal_entry_module.ts module.openRegModalWithDayContext.
 * 변경 후(E-4-δ-2): 챕터 목록·폼 브리지는 journalEntryShellService 와 공유(`dF.JournalEntry.get` 불필요).
 */
export function openRegModalWithDayContext(
    contentType: string,
    journalDayId: string|number,
    journalChapterId: string|number|undefined,
    stdrdDt: string,
    journalDateWeekDay: string,
    onReady?: () => void,
    initialObj: Record<string, any> = {},
): void {
    const meta = getMeta(contentType);
    if (!meta) {
        console.error("[journalEntryCrudService] meta missing:", contentType);
        return;
    }

    const url: string = cF.util.bindUrl(Url.JOURNAL_DAY, { id: journalDayId });
    const ajaxData: Record<string, any> = {
        includeDreamChapter: meta.chapterType != null,
    };
    cF.ajax.get(url, ajaxData, function(res: AjaxResponse): void {
        if (!res.rslt) return;

        const chapterList: Record<string, any>[] = resolveJournalEntryChapterList(meta, res.rsltObj);
        if (chapterList.length === 0) {
            if (meta.autoCreateChapterUrl) {
                createDreamChapterAndOpenModal(contentType, journalDayId, stdrdDt, journalDateWeekDay, onReady);
            } else if (meta.noteChapterOnlyMessageKey) {
                Swal.fire({ text: Message.get(meta.noteChapterOnlyMessageKey) });
            }
            return;
        }

        const resolvedChapterId: number = chapterList.some((chapter: Record<string, any>): boolean => {
            return Number(chapter?.id) === Number(journalChapterId);
        })
            ? Number(journalChapterId)
            : Number(chapterList[0]?.id);

        openJournalEntryRegForm(meta, {
            ...initialObj,
            journalDayId,
            journalChapterId: resolvedChapterId,
            stdrdDt,
            journalDateWeekDay,
            chapterList,
        });
        onReady?.();
    });
}

/**
 * 꿈 챕터 자동 생성 후 등록 모달 재진입.
 * 변경 전: module.createDreamChapterAndOpenModal.
 */
export function createDreamChapterAndOpenModal(
    contentType: string,
    journalDayId: string|number,
    stdrdDt: string,
    journalDateWeekDay: string,
    onReady?: () => void,
): void {
    const meta = getMeta(contentType);
    if (!meta?.autoCreateChapterUrl) return;

    const ajaxData: FormData = new FormData();
    ajaxData.append("journalDayId", String(journalDayId));

    cF.$ajax.multipart(meta.autoCreateChapterUrl, ajaxData, function(res: AjaxResponse): void {
        if (!res.rslt) {
            if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
            return;
        }

        openRegModalWithDayContext(
            contentType,
            journalDayId,
            res?.rsltObj?.id,
            stdrdDt,
            journalDateWeekDay,
            onReady,
        );
    }, "block");
}

/**
 * 수정 모달 진입.
 * 변경 전: module.mdfModal — Ajax get 후 openRegModalWithDayContext + ModalHistory.push.
 */
export function mdfModal(contentType: string, id: string|number): void {
    if (isNaN(Number(id))) return;

    const meta = getMeta(contentType);
    const module = getModule(contentType);
    if (!meta || !module) return;

    closeOpenModals();
    const histRef = module;
    const histFunc: string = "mdfModal";
    const histArgs: any[] = [id];

    const url: string = cF.util.bindUrl(meta.itemUrl, { id });
    cF.ajax.get(url, null, function(res: AjaxResponse): void {
        if (!res.rslt) {
            if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
            return;
        }

        const { rsltObj } = res;
        openRegModalWithDayContext(
            contentType,
            rsltObj.journalDayId,
            rsltObj.journalChapterId,
            rsltObj.stdrdDt,
            rsltObj.journalDateWeekDay,
            function(): void {
                ModalHistory.push(histRef, histFunc, histArgs);
            },
            rsltObj,
        );
    });
}

/**
 * (변경 I-5) 상세 모달 진입 dtlModal 제거됨.
 *   - 변경 전: module.dtlModal — Ajax get 후 cF.handlebars.modal(`journal_diary_dtl`/`journal_dream_dtl`).
 *     해당 Handlebars 템플릿이 저장소에 부재(dead 경로)였고 외부 활성 호출자도 0.
 *     · 유일한 동적 호출자였던 related_content_module.openTarget 은
 *       `if (typeof entryModule?.dtlModal === "function") { ... }` 가드 + mdfModal fallback 을 보유하므로
 *       본 메서드 부재 시 mdfModal 폴백 경로로 자연스럽게 진입한다.
 *   - 본 phase 에서 본체·thin wrapper·meta `detailModalKey`·service 등록 항목을 모두 제거.
 */

/**
 * 등록/수정 Ajax (Swal 확인 → multipart 전송 → 결과 분기).
 * 변경 전: module.regAjax — viewType==SEARCH 분기 포함.
 */
export function regAjax(contentType: string): void {
    const meta = getMeta(contentType);
    const module = getModule(contentType);
    if (!meta || !module) return;

    const id: string = cF.util.getInputValue(`${meta.formSelector} [name='id']`);
    const isMdf: boolean = cF.util.isNotEmpty(id);
    Swal.fire({
        text: Message.get(isMdf ? "view.cnfm.mdf" : "view.cnfm.reg"),
        showCancelButton: true,
    }).then(function(result: SwalResult): void {
        if (!result.value) return;

        const url: string = isMdf ? cF.util.bindUrl(meta.itemUrl, { id }) : meta.listUrl;
        const ajaxData: FormData = new FormData(document.querySelector(meta.formSelector) as HTMLFormElement);
        cF.$ajax.multipart(url, ajaxData, function(res: AjaxResponse): void {
            Swal.fire({ text: res.message })
                .then(function(): void {
                    if (!res.rslt) return;
                    if (module.viewType === "SEARCH") {
                        closeOpenModals();
                        const search = getEntrySearchService(contentType);
                        if (isMdf) {
                            search?.replaceItem?.(id);
                        } else {
                            search?.search?.();
                        }
                        cF.ui.unblockUI();
                        return;
                    }
                    module.refresh();
                });
        }, "block");
    });
}

/**
 * 삭제 Ajax (Swal 확인 → DELETE → 결과 분기).
 * 변경 전: module.delAjax — viewType==SEARCH 분기 포함.
 */
export function delAjax(contentType: string, id: string|number): void {
    if (isNaN(Number(id))) return;

    const meta = getMeta(contentType);
    const module = getModule(contentType);
    if (!meta || !module) return;

    Swal.fire({
        text: Message.get("view.cnfm.del"),
        showCancelButton: true,
    }).then(function(result: SwalResult): void {
        if (!result.value) return;

        const url: string = cF.util.bindUrl(meta.itemUrl, { id });
        cF.$ajax.delete(url, null, function(res: AjaxResponse): void {
            Swal.fire({ text: res.message })
                .then(function(): void {
                    if (!res.rslt) return;
                    if (module.viewType === "SEARCH") {
                        getEntrySearchService(contentType)?.removeItem?.(id);
                        cF.ui.unblockUI();
                        return;
                    }
                    module.refresh();
                });
        }, "block");
    });
}

/**
 * 본문 클립보드 복사.
 * 변경 전: module.copy — Ajax get + plugin.extractTitleLine + DOM 정규화 + clipboard write.
 */
export function copy(contentType: string, id: string|number): void {
    if (isNaN(Number(id))) return;

    const meta = getMeta(contentType);
    if (!meta) return;

    const url: string = cF.util.bindUrl(meta.itemUrl, { id });
    cF.ajax.get(url, null, function(res: AjaxResponse): void {
        if (!res.rslt) {
            if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
            return;
        }
        const rsltObj: Record<string, any> = res.rsltObj;
        const { stdrdDt, journalDateWeekDay } = rsltObj;
        const titleLine: string = meta.plugin.extractTitleLine(rsltObj);
        const date: string = stdrdDt + " (" + journalDateWeekDay + ")" + "\r\n";
        const resultCn: string = rsltObj.content;
        const replacedCn: string = resultCn
            .replace(/<\s*hr\b[^>]*\/?>/gi, "\n---\n")
            .replace(/<\s*br\s*\/?>/gi, "\n")
            .replace(/<\s*\/?p[^>]*>/gi, "\n");
        const div: HTMLDivElement = document.createElement("div");
        div.innerHTML = date + titleLine + replacedCn;
        const textToCopy: string = (div.innerText ?? "")
            .replace(/\n+/g, "\n")
            .replace(/\n/g, "\r\n")
            .trim();

        if (navigator.clipboard && window.isSecureContext) {
            navigator.clipboard.writeText(textToCopy)
                .then((): void => {
                    Swal.fire({ icon: "success", text: "클립보드에 복사되었습니다.", timer: 1500, showConfirmButton: false });
                })
                .catch((): void => {
                    cF.util.legacyCopy(textToCopy);
                });
        } else {
            cF.util.legacyCopy(textToCopy);
        }
    });
}

/**
 * 저장(submit) 버튼. 변경 전: module.submit — tinymce save + form submit.
 */
export function submit(contentType: string): void {
    const meta = getMeta(contentType);
    const module = getModule(contentType);
    if (!meta || !module) return;

    tinymce.get(meta.tinymceId).save();
    module.submitMode = "submit";
    $(meta.formSelector).submit();
}

/**
 * 미리보기 버튼. 변경 전: module.preview — tinymce save + form submit(target=popup).
 */
export function preview(contentType: string): void {
    const meta = getMeta(contentType);
    const module = getModule(contentType);
    if (!meta || !module) return;

    tinymce.get(meta.tinymceId).save();
    module.submitMode = "preview";
    $(meta.formSelector).submit();
}

/**
 * jQuery validate 의 submitHandler. 변경 전: module.submitHandler — preview/submit 분기.
 */
export function submitHandler(contentType: string): boolean {
    const meta = getMeta(contentType);
    const module = getModule(contentType);
    if (!meta || !module) return false;

    const journalEntry = (window as any).dF?.JournalEntry;

    if (module.submitMode === "preview") {
        const width: number = journalEntry?.resolveJournalDayPreviewWidth?.() ?? Math.min(1600, Math.round(window.innerWidth * 0.92));
        const height: number = Math.round(window.innerHeight * 0.9);
        const left: number = Math.max(0, Math.round((window.screen.availWidth - width) / 2));
        const top: number = Math.max(0, Math.round((window.screen.availHeight - height) / 2));
        const popupNm: string = `journal_entry_preview_${String(meta.entryType).toLowerCase()}`;
        const option: string = `width=${width},height=${height},left=${left},top=${top},scrollbars=yes,resizable=yes`;
        const popup: Window | null = cF.ui.openPopup("", popupNm, option);
        popup?.focus();
        const previewUrl: string = Url.JOURNAL_ENTRY_PREVIEW_POP;
        $(meta.formSelector)
            .attr("method", "post")
            .attr("action", previewUrl)
            .attr("target", popupNm);
        return true;
    }
    if (module.submitMode === "submit") {
        $(meta.formSelector)
            .attr("method", "post")
            .removeAttr("action")
            .removeAttr("target");
        regAjax(contentType);
    }
    return false;
}

/**
 * 글로벌 노출. classic `journal_entry_module.ts` 의 thin wrapper 가
 * `(window as any).dF.JournalEntryCrudService.<method>(...)` 로 호출 가능하도록 등록한다.
 */
const journalEntryCrudService = {
    regModal,
    openRegModalWithDayContext,
    createDreamChapterAndOpenModal,
    mdfModal,
    regAjax,
    delAjax,
    copy,
    submit,
    preview,
    submitHandler,
};

(function registerOnDf(): void {
    const w = window as any;
    if (w.dF == null) w.dF = {};
    w.dF.JournalEntryCrudService = journalEntryCrudService;
})();

export default journalEntryCrudService;
