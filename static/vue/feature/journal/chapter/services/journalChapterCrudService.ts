/**
 * journalChapterCrudService.ts
 * 저널 챕터 CRUD/표시 액션 서비스 (Vue 소유).
 *
 * 변경(Phase C-1):
 *   - 레거시 dF.JournalChapter 의 toggle / copy / exportTxt / collapseAjax / deleteAjax 5개 메서드를
 *     이 서비스로 이전한다 (journal_day 의 journalDayCrudService 와 동형 패턴).
 *   - toggleStateAjax 는 collapseAjax 만 사용하므로 module-private 헬퍼로 두고 외부 노출하지 않는다.
 *   - dF.State.* 는 전역 유틸이며 변경 없이 그대로 호출한다(외부 호출 그래프 보존).
 *   - JournalChapterItem.ts 등 Vue 컴포넌트는 이 서비스를 통해 액션을 수행한다.
 *   - dF.JournalChapter 모듈 자체는 외부 호출자가 사라진 시점에 Phase C-2 에서 통째로 제거된다.
 *
 * 변경(D):
 *   - `Message.get` 직호출을 `resolveMessage` 헬퍼로 위임.
 *
 * @author nichefish
 */

import { resolveMessage } from "../../../../common/messageHelper.js";

import { getJournalDayListBridge } from "../../day/journalDayListBridge.js";

type ListVueBridge = {
    mounted?: boolean;
    refresh?: () => void;
};

type StateService = {
    resolveJournalCacheContext?: (item: HTMLElement | null) => Record<string, any>;
    toggleAjax?: (payload: Record<string, any>, callback: (res: AjaxResponse) => void) => void;
};

function getStateService(): StateService | undefined {
    return ((window as any).dF as { State?: StateService } | undefined)?.State;
}

function getDayTagService(): { refreshDayTagList?: () => void } | undefined {
    return ((window as any).dF as { JournalDayTagService?: { refreshDayTagList?: () => void } } | undefined)?.JournalDayTagService;
}

/**
 * 글 접기/펼치기 토글 (클라이언트 DOM 토글만, 서버 상태 저장 없음).
 * 변경 전: dF.JournalChapter.toggle(id) — DOM 클래스 / data-collapsed / 자식 entry 클래스 토글.
 * 변경 후(Phase C-1): 동일 DOM 동작을 그대로 이전. UI 동결 룰에 따라 마크업·클래스 변경 없음.
 */
export function toggleChapter(id: string | number): void {
    if (isNaN(Number(id))) return;

    const item = document.querySelector(`.journal-chapter-item[data-id='${id}']`) as HTMLElement | null;
    if (!item) {
        console.warn("[journalChapterCrudService] item not found.");
        return;
    }

    const content: HTMLDivElement | null = item.querySelector("div.journal-chapter-content");
    if (!content) {
        console.warn("[journalChapterCrudService] content not found.");
        return;
    }

    const shouldCollapse: boolean = !content.classList.contains("collapsed");
    const diaries: NodeListOf<HTMLElement> = item.querySelectorAll(".journal-diary-content");
    const tagDiv = item.querySelector(".journal-chapter-tags");
    const icon: HTMLElement | null = document.querySelector(`#chapter-toggle-icon-${id}`);
    if (!icon) console.warn("[journalChapterCrudService] chapter toggle icon not found:", id);

    if (shouldCollapse) {
        content.classList.add("collapsed");
        item.dataset.collapsed = "Y";
        icon?.classList.add("bi-arrows-expand");
        icon?.classList.remove("bi-arrows-collapse");
        tagDiv?.classList.remove("d-none");
        diaries.forEach((diary: HTMLElement): void => {
            const inner: HTMLElement | null = diary.querySelector(".journal-content");
            inner?.classList.add("collapsed");
        });
    } else {
        content.classList.remove("collapsed");
        item.dataset.collapsed = "N";
        icon?.classList.add("bi-arrows-collapse");
        icon?.classList.remove("bi-arrows-expand");
        tagDiv?.classList.add("d-none");
        diaries.forEach((diary: HTMLElement): void => {
            const inner: HTMLElement | null = diary.querySelector(".journal-content");
            inner?.classList.remove("collapsed");
        });
    }
}

/**
 * 상태 토글 (Ajax) — collapseAjax 의 공통 헬퍼.
 * 변경 전: dF.JournalChapter.toggleStateAjax(id, stateKey, { onOffFunc })
 * 변경 후(Phase C-1): 외부 호출자가 collapseAjax 하나뿐이므로 module-private 헬퍼로 축소.
 *   onOffFunc 는 두 번째 인자 객체 destructuring 대신 콜백 인자로 단순화.
 */
function toggleStateAjax(
    id: string | number,
    stateKey: string,
    onOffFunc: (res: AjaxResponse, item: HTMLElement) => void,
): void {
    if (isNaN(Number(id))) return;

    const item = document.querySelector(`.journal-chapter-item[data-id='${id}']`) as HTMLElement | null;
    const stateService = getStateService();
    if (!stateService?.resolveJournalCacheContext || !stateService.toggleAjax) {
        console.error("[journalChapterCrudService] dF.State.* unavailable.");
        return;
    }
    const cacheContext = stateService.resolveJournalCacheContext(item);
    const payload = { id, contentType: "JOURNAL_CHAPTER", stateKey, cacheContext };
    stateService.toggleAjax(payload, function(res: AjaxResponse): void {
        if (!item) return;
        const lowerStateKey: string = stateKey.toLowerCase();
        const icon: HTMLElement | null = item.querySelector(`.icon-${lowerStateKey}`);
        if (!icon) console.warn("[journalChapterCrudService] icon not found.");
        else icon.classList.toggle("d-none", res.rsltSts !== "ON");

        const chk: HTMLInputElement | null = item.querySelector(`.chapter-context-${lowerStateKey}-check`);
        if (!chk) console.warn("[journalChapterCrudService] chk not found.");
        else chk.checked = res.rsltSts === "ON";

        if (stateKey === "COLLAPSED") {
            item.dataset.collapsed = res.rsltSts === "ON" ? "Y" : "N";
        }

        const tagDiv: HTMLElement | null = item.querySelector(".journal-chapter-tags");
        if (!tagDiv) console.warn("[journalChapterCrudService] tagDiv not found.");
        else tagDiv.classList.toggle("d-none", res.rsltSts !== "ON");

        onOffFunc(res, item);
    });
}

/**
 * 글 접기/펼치기 토글 (Ajax). 서버 상태(COLLAPSED) 갱신 + DOM 반영.
 */
export function collapseAjax(id: string | number): void {
    if (isNaN(Number(id))) return;

    toggleStateAjax(id, "COLLAPSED", function(res: AjaxResponse, item: HTMLElement): void {
        const content: HTMLDivElement | null = item.querySelector("div.journal-chapter-content");
        if (!content) {
            console.warn("[journalChapterCrudService] content not found.");
            return;
        }
        content.classList.toggle("collapsed", res.rsltSts === "ON");
    });
}

/**
 * 챕터 본문(엔트리 합본) 텍스트 복사.
 * 변경 전: dF.JournalChapter.copy(id) — Ajax get 후 entry list 직렬화 → clipboard.
 * 변경 후(Phase C-1): 동일 흐름 그대로 이전. 동작 결과 변경 없음.
 *
 * @deprecated 향후 클립보드 액션 통합 검토 대상 (현재는 호환을 위해 유지).
 */
export function copyChapter(id: string | number): void {
    if (isNaN(Number(id))) return;

    const url: string = cF.util.bindUrl(Url.JOURNAL_CHAPTER, { id });
    cF.ajax.get(url, null, function(res: AjaxResponse): void {
        if (!res.rslt) {
            if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
            return;
        }
        const rsltObj: Record<string, any> = res.rsltObj as Record<string, any>;
        const journalEntryList: any[] = Array.isArray(rsltObj?.journalEntryList)
            ? rsltObj.journalEntryList
            : (Array.isArray(rsltObj?.journalDiaryList) ? rsltObj.journalDiaryList : []);
        if (!Array.isArray(rsltObj?.journalEntryList) && !Array.isArray(rsltObj?.journalDiaryList)) {
            console.warn("[journalChapterCrudService] journalEntryList missing/invalid; fallback to empty.", rsltObj?.journalEntryList);
        }
        const { stdrdDt, journalDateWeekDay } = rsltObj as { stdrdDt?: string; journalDateWeekDay?: string };
        const date: string = (stdrdDt ?? "") + " (" + (journalDateWeekDay ?? "") + ")" + "\r\n";
        const resultCn: string = journalEntryList.map((entry: any): string => "#" + (entry?.sortOrder ?? "") + (entry?.content ?? "")).join("\r\n");

        const replacedCn: string = resultCn
            .replace(/<\s*hr\b[^>]*\/?>/gi, "\n---\n")
            .replace(/<\s*br\s*\/?>/gi, "\n")
            .replace(/<\s*\/?p[^>]*>/gi, "\n");
        const div: HTMLDivElement = document.createElement("div");
        div.innerHTML = date + replacedCn;
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
 * 검색 결과 txt 다운로드 (페이지 이동).
 */
export function exportTxt(id: string | number): void {
    if (isNaN(Number(id))) return;

    window.location.href = cF.util.bindUrl(Url.JOURNAL_CHAPTER_EXPORT, { id });
}

/**
 * 삭제 (Ajax).
 * 변경 전: dF.JournalChapter.deleteAjax(id) — JournalDayCalVueApp.refresh / journalDayResolveListBridge().refresh 분기,
 *   CAL 화면에서는 dF.JournalDayTagService.refreshDayTagList() 도 함께 호출.
 * 변경 후(Phase C-1): 동일 분기 흐름을 service 로 이전 (cF.ui.unblockUI / ModalHistory.reset 후처리 보존).
 */
export function deleteAjax(id: string | number): void {
    if (isNaN(Number(id))) return;

    Swal.fire({
        text: resolveMessage("view.cnfm.del"),
        showCancelButton: true,
    }).then(function(result: SwalResult): void {
        if (!result.value) return;

        const url: string = cF.util.bindUrl(Url.JOURNAL_CHAPTER, { id });
        cF.$ajax.delete(url, null, function(res: AjaxResponse): void {
            Swal.fire({ text: res.message })
                .then(function(): void {
                    if (!res.rslt) return;

                    if (window.JournalDayCalVueApp?.mounted === true) {
                        window.JournalDayCalVueApp.refresh?.();
                        getDayTagService()?.refreshDayTagList?.();
                    } else {
                        (getJournalDayListBridge() as ListVueBridge | undefined)?.refresh?.();
                    }
                    cF.ui.unblockUI();
                    ModalHistory.reset();
                });
        }, "block");
    });
}

const journalChapterCrudService = {
    toggleChapter,
    collapseAjax,
    copyChapter,
    exportTxt,
    deleteAjax,
};

export default journalChapterCrudService;
