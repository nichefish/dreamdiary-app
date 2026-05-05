/**
 * journalAnnualAsideService.ts
 * 저널 결산 사이드 공용 모듈 service (Vue 소유, dF 글로벌 등록).
 *
 * 변경(A-4-β):
 *   - classic `static/js/view/feature/journal/annual/journal_annual_aside_module.ts` 를 본 ES module 로 이전한다.
 *   - 외부 호출 시그니처 보존: `dF.JournalAnnualAside.init` / `dF.JournalAnnualAside.yyMnth`.
 *   - `_journal_annual_aside_base.ftlh` 의 classic `<script type="text/javascript">` 적재 라인을 제거하고 본 ES module 로 교체한다.
 *
 * @author nichefish
 */

const dfNs: any = (function ensureDf(): any {
    const w = window as any;
    if (w.dF == null) w.dF = {};
    return w.dF;
})();

const journalAnnualAsideModule: dfModule = {
    initialized: false,

    /**
     * 모듈을 초기화한다.
     */
    init: function(): void {
        if (journalAnnualAsideModule.initialized) return;

        journalAnnualAsideModule.initialized = true;
        console.log("'dF.JournalAnnualAside' module initialized.");
    },

    yyMnth: function(obj: HTMLInputElement): void {
        // 쿠키 설정하기
        const id: string = $(obj).attr("id");
        const cookieOptions = {
            path: "/journal/annual/",
            expires: cF.date.getCurrDateAddDay(36135)
        };
        $.cookie("journal_" + id, $(obj).val(), cookieOptions);
        // 목록 조회
        dfNs.JournalAnnual.listAjax();
    },
};

dfNs.JournalAnnualAside = journalAnnualAsideModule;

/*
 * 변경(A-5-β-1): A-4-β 단계에서 임시로 부착했던 호환 별칭 `dF.JournalAnnualAsideyyMnth`
 * (FTLH 의 onchange 점 누락 typo 보정용)을 제거한다.
 * `_journal_annual_aside_base.ftlh` 의 onchange 도 동시에 표준 경로 `dF.JournalAnnualAside.yyMnth(this)` 로 교정되었으므로
 * 호환 경로(별칭)를 유지할 이유가 없다. 단일 경로로 수렴한다.
 */

export default journalAnnualAsideModule;
