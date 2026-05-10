/**
 * registerJournalDayShellServices.ts
 * 저널 일자(journal_day) 페이지 전역 셸: FTL 이 기대하는 <code>dF</code> 노출을 한 ES 모듈 진입점에 모은다.
 *
 * - <code>dF.JournalDayViewService.changeView</code> → <code>journalDayUiBridgeService.changeView</code>
 *   (변경 전: classic <code>journal_day_view_service.js</code>)
 * - <code>dF.JournalDayRuntimeService</code> → <code>./services/journalDayRuntimeShell.js</code> side-effect import
 *   (변경 전(P4): FTL classic <code>journal_day_runtime_service.js</code> / 변경 전(P6): <code>static/js</code> TS 경로)
 * - 태그 Ajax 보조 축: <code>JournalDayTagDataService</code> / <code>JournalDayTagProfileService</code> / <code>JournalDayTagContextMenu</code>
 *   → <code>journalDayTag*Shell.js</code> side-effect import (변경 전(P7): FTL classic <code>journal_day_tag_*_service.js</code>).
 * - <code>dF.JournalDayTagService</code> → <code>./services/journalDayTagService.js</code> 명시 import (변경 전(P8): uiBridge 의존 체인에만 묶여 있음).
 *
 * 파일명이 View 전용이 아닌 이유: P4 이후 런타임 부착까지 이 모듈이 담당한다.
 */

/* 태그 보조 축 → JournalDayTagService(dF) → 런타임 셸 순 */
import "./services/journalDayTagDataShell.js";
import "./services/journalDayTagProfileShell.js";
import "./services/journalDayTagContextMenuShell.js";
/* 변경(P8): <code>dF.JournalDayTagService</code> 부착을 <code>journalDayUiBridgeService</code> import 체인에만 의존하지 않음. */
import "./services/journalDayTagService.js";
import "./services/journalDayRuntimeShell.js";
import journalDayUiBridgeService from "./services/journalDayUiBridgeService.js";

const win = window as unknown as { dF?: Record<string, unknown> };
if (typeof win.dF === "undefined" || win.dF === null) {
    win.dF = {};
}
win.dF.JournalDayViewService = {
    changeView(targetPath: string): void {
        journalDayUiBridgeService.changeView(targetPath);
    },
};
