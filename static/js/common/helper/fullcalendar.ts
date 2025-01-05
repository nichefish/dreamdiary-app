/**
 * fullcalendar.ts
 * 공통 - fullcalendar(라이브러리) 관련 함수 모듈
 *
 * @namespace: cF.fullcalendar (노출식 모듈 패턴)
 * @author: nichefish
 * @see: https://fullcalendar.io/docs/
 */
if (typeof cF === 'undefined') { var cF = {} as any; }
cF.fullcalendar = (function(): Module {

    /** 기본 옵션 */
    const baseOptions = {
        headerToolbar: {
            left: "prev,next today",
            center: "title",
            right: ""
        },
        height: 1040,
        contentHeight: 980,
        aspectRatio: 3,  // @see: https://fullcalendar.io/docs/aspectRatio
        expandRows: true,
        nowIndicator: true,
        selectOverlap: false,

        views: {
            dayGridMonth: { buttonText: "월별 조회" }
        },

        initialView: "dayGridMonth",
        locale: "ko",

        editable: false,
        businessHours: true,
        displayEventTime: false,
        // dayMaxEvents: true, // allow "more" link when too many events
        navLinks: false,
        nextDayThreshold: '00:00:00',
    };

    return {
        /**
         * init : `FullCalendar` 객체를 생성하고 이벤트를 설정하는 함수.
         * @param {string} selectorId - 캘린더를 렌더링할 DOM 요소의 ID.
         * @param {Array} eventList - 캘린더에 표시할 이벤트 목록.
         * @param {function} clickFunc - 이벤트 클릭 시 호출할 콜백 함수 (선택적).
         * @param {object} additionalOptions - 추가로 적용할 `FullCalendar` 설정 옵션 (선택적).
         * @returns {FullCalendar.Calendar} - 생성된 `FullCalendar` 객체.
         */
        init: function(selectorId: string, eventList, clickFunc: Function, additionalOptions = {}): FullCalendar.Calendar {
            console.log("'cF.fullcalendar' module initialized.");

            const target = document.getElementById(selectorId);
            if (!target) {
                console.error("Element with ID " + selectorId + " not found.");
                return null;
            }

            const todayDate = moment().startOf("day");
            const TODAY = todayDate.format("YYYY-MM-DD");

            const mergedOptions = {
                ...baseOptions,
                now: TODAY,
                initialDate: TODAY,
                events: eventList,
                // 클릭 이벤트
                eventClick: function(info) {
                    if (typeof clickFunc === "function") clickFunc(info);
                },
                ...additionalOptions
            }

            return new FullCalendar.Calendar(target, mergedOptions);
        }
    }
})();