/**
 * handlebars.ts
 * @namespace: cF.handlebars
 * @author: nichefish
 * @dependency: handlebars.js
 * 공통 - handlebars 관련 함수 모듈
 * (노출식 모듈 패턴 적용 :: commons.enterKey("#userId") 이런식으로 사용)
 */
if (typeof cF === 'undefined') {
    let cF = {};
}
cF.handlebars = (function () {
    return {
        /**
         * Handlebars Template 공통 함수 분리
         * @param {Object} data - Handlebars 템플릿에 전달할 데이터 객체.
         * @param {string} templateStr - 템플릿 요소의 ID 문자열 (템플릿 ID는 `templateStr + "_template"`로 구성).
         * @returns {string|null} - 컴파일된 템플릿 문자열 또는 템플릿이 없을 경우 `null`.
         */
        compile: function (data, templateStr) {
            const templateElmt = document.getElementById(templateStr + "_template");
            if (!templateElmt)
                return null;
            // 컴파일
            const template = Handlebars.compile(templateElmt.innerHTML.replaceAll("`", ""));
            return template(data);
        },
        /**
         * Handlebars 템플릿 렌더링 및 대상 요소 갈아치기
         * @param {Object} data - Handlebars 템플릿에 전달할 데이터 객체.
         * @param {string} templateStr - 템플릿 요소의 ID 문자열 (템플릿 ID는 `templateStr + "_template"`로 구성).
         * @param {string} [show] - 모달을 표시할지 여부 ("show"로 전달 시 모달 표시).
         */
        template: function (data = {}, templateStr, show) {
            const actual = cF.handlebars.compile(data, templateStr);
            if (actual === null)
                return;
            // 대상 요소에 추가
            const trgetElmt = document.getElementById(templateStr + "_div");
            if (!trgetElmt)
                return;
            trgetElmt.innerHTML = ""; // 내용 비우기
            trgetElmt.insertAdjacentHTML('beforeend', actual); // 내용 추가
            // 새로 append된 부분에서만 툴팁 활성화
            trgetElmt.querySelectorAll("[data-bs-toggle='tooltip']").forEach(tooltipEl => {
                new bootstrap.Tooltip(tooltipEl);
            });
            if (show === "show") {
                $("#" + templateStr + "_modal").modal("show");
            }
        },
        /**
         * Handlebars 템플릿 데이터 append
         * @param {Object} data - Handlebars 템플릿에 전달할 데이터 객체.
         * @param {string} templateStr - 템플릿 요소의 ID 문자열 (템플릿 ID는 `templateStr + "_template"`로 구성).
         */
        append: function (data = {}, templateStr) {
            cF.handlebars.appendTo(data, templateStr, templateStr + "_div");
        },
        /**
         * Handlebars 템플릿 데이터 특정 요소에 append
         * @param {Object} data - Handlebars 템플릿에 전달할 데이터 객체.
         * @param {string} templateStr - 템플릿 요소의 ID 문자열 (템플릿 ID는 `templateStr + "_template"`로 구성).
         * @param {string} trgetElmtId - 데이터가 추가될 대상 요소의 ID.
         */
        appendTo: function (data = {}, templateStr, trgetElmtId) {
            const actual = cF.handlebars.compile(data, templateStr);
            // 대상 요소에 추가
            const trgetElmt = document.getElementById(trgetElmtId);
            if (!trgetElmt)
                return;
            trgetElmt.insertAdjacentHTML('beforeend', actual);
            // 새로 append된 부분에서만 툴팁 활성화
            trgetElmt.querySelectorAll("[data-bs-toggle='tooltip']").forEach(tooltipEl => {
                new bootstrap.Tooltip(tooltipEl);
            });
        },
    };
})();
