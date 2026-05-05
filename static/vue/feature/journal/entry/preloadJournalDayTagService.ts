/**
 * preloadJournalDayTagService.ts
 * 저널 결산(annual) 등 <code>registerJournalDayShellServices</code> 가 없는 화면에서
 * Handlebars <code>tag_list_partial</code> 의 <code>onclick="{{module}}.select(…)</code> 가 기대하는
 * <code>dF.JournalDayTagService</code> 만 선행 부착한다.
 *
 * 로드 순서: 본 모듈은 <code>type="module"</code>(defer) 이나, 결산 페이지의 <code>Page.init</code> 은
 * <code>DOMContentLoaded</code> 이후이므로 그 시점 전에 평가된다.
 */

import "../day/services/journalDayTagService.js";

export {};
