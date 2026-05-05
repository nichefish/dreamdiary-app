/**
 * JournalAnnualDtlPageBoot.ts
 * 저널 결산(annual) 상세 페이지 부트 (ESM 단일 진입).
 *
 * 변경(A-7-α):
 *   - 변경 전: classic page IIFE `static/js/view/feature/journal/annual/journal_annual_dtl.ts`
 *     가 `Page.init()` 으로 dF.JournalAnnual.init / dF.JournalAnnualReview.init / dtlAjax(yy) /
 *     섹션별 list/tag ajax 진입을 수행했다(Page IIFE + DOMContentLoaded).
 *   - 변경 후: 본 ES module 이 동일한 부트 시퀀스를 ESM 진입으로 수행한다.
 *     IIFE Page 객체는 폐기, classic page script(`journal_annual_dtl.js`) 적재 라인은 제거한다.
 *   - 적재 순서(`journal_annual_dtl.ftlh`):
 *     · `_journal_annual_review_reg_modal.ftlh` 가드(review 묶음 ES module 단일 수렴)
 *     · `journalAnnualCrudService.js` → `journalAnnualStateService.js` → `journalAnnualService.js` (dF.JournalAnnual 표면)
 *     · 본 모듈 (페이지 부트 진입)
 *   - 모든 ES module 은 `defer` 의미를 가지며 적재 순서대로 실행된다 — 본 모듈 진입 시점에는
 *     `dF.JournalAnnual` 과 `dF.JournalAnnualReview` 표면이 모두 등록되어 있다.
 *
 * 변경 전(A-1) 보존: `getMyAnnualDreamListAjax` 오타를 `getAnnualDreamListAjax` 로 수정한 변경은
 *   본 ESM 에서도 동일하게 `dF.JournalAnnual.getAnnualDreamListAjax(yy)` 를 호출한다.
 *
 * @author nichefish
 */

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

/**
 * 부트 진입.
 * 변경 전: classic `journal_annual_dtl.ts` 의 `Page.init`.
 *   호출 순서·분기·인자(yy/section)는 동일하게 유지한다.
 */
function bootJournalAnnualDtlPage(): void {
    const annual: any = (window as any).dF?.JournalAnnual;
    const review: any = (window as any).dF?.JournalAnnualReview;
    if (annual == null || typeof annual.init !== "function") {
        console.error("[JournalAnnualDtlPageBoot] dF.JournalAnnual 미등록 — service ES module 적재 순서 확인.");
        return;
    }
    if (review == null || typeof review.init !== "function") {
        console.error("[JournalAnnualDtlPageBoot] dF.JournalAnnualReview 미등록 — review modal ES module 가드 적재 순서 확인.");
        return;
    }

    /* 모듈 초기화 */
    annual.init();
    review.init();

    const yy: string = cF.util.getPathVariableFromUrl(/\/annual\/(\d{4})(?:\.do)?$/);
    if (yy) annual.dtlAjax(yy);

    const section: string = cF.util.getUrlParam("section");
    switch (section) {
        case "DIARY":
            annual.getAnnualDiaryListAjax(yy);
            annual.getTagListAjax(yy, "DAY");
            annual.getTagListAjax(yy, "DIARY");
            break;
        case "DREAM":
            annual.getAnnualDreamListAjax(yy);
            annual.getTagListAjax(yy, "DREAM");
            break;
    }
}

runWhenDomReady(bootJournalAnnualDtlPage);

export {};
