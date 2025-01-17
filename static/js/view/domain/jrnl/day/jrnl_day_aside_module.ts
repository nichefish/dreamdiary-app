/**
 * jrnl_day_aside_module.ts
 * 저널 일자 사이드 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JrnlDayAside = (function(): dfModule {
    /** 쿠키 기본 옵션*/
    const cookieOptions = {
        path: "/jrnl/day/",
        expires: cF.date.getCurrDateAddDay(36135)
    };

    return {
        initialized: false,

        /**
         * JrnlDayAside 객체 초기화
         */
        init: function(): void {
            if (dF.JrnlDayAside.initialized) return;

            const pinYyCookie = $.cookie("pin_yy");
            if (pinYyCookie !== undefined) {
                document.querySelector("#jrnl_aside #pinYy")!.textContent = pinYyCookie;
            }
            const pinMnthCookie = $.cookie("pin_mnth");
            if (pinMnthCookie !== undefined) {
                document.querySelector("#jrnl_aside #pinMnth")!.textContent = pinMnthCookie;
            }

            document.querySelector("#jrnl_aside #left")?.addEventListener("click", dF.JrnlDayAside.left);
            document.querySelector("#jrnl_aside #right")?.addEventListener("click", dF.JrnlDayAside.right);

            dF.JrnlDayAside.initialized = true;
            console.log("'dF.JrnlDayAside' module initialized.");
        },

        /**
         * 오늘 날짜로 가기
         */
        today: function(): void {
            const todayYy: string = cF.date.getCurrYyStr();
            const todayMnth: string = cF.date.getCurrMnthStr();
            const yyElement: HTMLSelectElement = document.querySelector("#jrnl_aside #yy") as HTMLSelectElement;
            const mnthElement: HTMLSelectElement = document.querySelector("#jrnl_aside #mnth") as HTMLSelectElement;

            if (yyElement && mnthElement) {
                yyElement.value = todayYy;
                mnthElement.value = todayMnth;
            }
            dF.JrnlDayAside.mnth();
            // 오늘이 제일 위에 오게 하기 위해 내림차순 정렬로 변경
            dF.JrnlDayAside.sort("DESC");
        },

        /**
         * 년도 바꾸기
         */
        yy: function(): void {
            cF.handlebars.template(null, "jrnl_day_list");
            cF.handlebars.template([], "jrnl_day_tag_list");
            cF.handlebars.template([], "jrnl_diary_tag_list");
            cF.handlebars.template([], "jrnl_dream_tag_list");
            dF.JrnlDream.inKeywordSearchMode = false;

            // #yy 요소와 #mnth 요소 가져오기
            const yyElement: HTMLSelectElement = document.querySelector("#jrnl_aside #yy") as HTMLSelectElement;
            const mnthElement: HTMLSelectElement = document.querySelector("#jrnl_aside #mnth") as HTMLSelectElement;

            if (yyElement && mnthElement) {
                // #yy 값이 2010일 경우
                if (yyElement.value === "2010") {
                    mnthElement.value = "99";  // #mnth 값을 99로 설정
                    dF.JrnlDayAside.mnth();   // 월 변경 처리
                } else {
                    mnthElement.value = "";   // #mnth 값을 비움
                }
            }
        },

        /**
         * 월 바꾸기
         */
        mnth: function(): void {
            // 쿠키 설정하기
            $.cookie("jrnl_yy", $("#jrnl_aside #yy").val(), cookieOptions);
            if ($("#jrnl_aside #mnth").val() === "") return;
            $.cookie("jrnl_mnth", $("#jrnl_aside #mnth").val(), cookieOptions);
            $("#jrnl_aside #dreamKeyword").val("");
            // 목록 조회
            dF.JrnlDay.yyMnthListAjax();
            dF.JrnlDayTag.listAjax();
            dF.JrnlDiaryTag.listAjax();
            dF.JrnlDreamTag.listAjax();
            //
            dF.JrnlDream.inKeywordSearchMode = false;
            // 페이지 상단으로 이동
            cF.util.toPageTop();
        },

        /**
         * left
         */
        left: function(): void {
            const monthElement: HTMLSelectElement = document.querySelector("#jrnl_aside #mnth") as HTMLSelectElement;
            const yearElement: HTMLSelectElement = document.querySelector("#jrnl_aside #yy") as HTMLSelectElement;

            const currentMonth: string = monthElement.value;
            const currentYear: string = yearElement.value;

            if (currentMonth && parseInt(currentMonth) > 1) {
                // 월을 하나 감소시킴
                monthElement.value = (parseInt(currentMonth) - 1).toString();
            } else {
                // 1월일 경우, 이전 년도로 이동하고 12월로 설정
                if (currentYear !== "2010") {
                    yearElement.value = (parseInt(currentYear) - 1).toString(); // 이전 년도로
                    monthElement.value = "12";  // 12월로 설정
                }
            }

            // 월이 변경되었을 때의 처리
            dF.JrnlDayAside.mnth();
        },

        /**
         * right
         */
        right: function(): void {
            const monthElement: HTMLSelectElement = document.querySelector("#jrnl_aside #mnth") as HTMLSelectElement;
            const yearElement: HTMLSelectElement = document.querySelector("#jrnl_aside #yy") as HTMLSelectElement;

            const currentMonth: string = monthElement.value;
            const currentYear: string = yearElement.value;

            if (currentMonth && parseInt(currentMonth) < 12) {
                // 월을 하나 증가시킴
                monthElement.value = (parseInt(currentMonth) + 1).toString();
            } else {
                // 12월일 경우, 다음 년도로 이동하고 1월로 설정
                yearElement.value = (parseInt(currentYear) + 1).toString(); // 다음 년도로
                monthElement.value = "1";  // 1월로 설정
            }

            // 월이 변경되었을 때의 처리
            dF.JrnlDayAside.mnth();
        },

        /**
         * 현재 년/월을 저장한다.
         */
        pinpoint: function(): void {
            const pinYy: string = $("#jrnl_aside #yy").val().toString();
            const pinMnth: string = $("#jrnl_aside #mnth").val().toString();
            $.cookie("pin_yy", pinYy, cookieOptions);
            $.cookie("pin_mnth", pinMnth, cookieOptions);
            $("#jrnl_aside #pinYy").text(pinYy);
            $("#jrnl_aside #pinMnth").text(pinMnth);
        },

        /**
         * 저장된 저널 년/월로 돌아가기
         */
        turnback: function(): void {
            const pinYyCookie = $.cookie("pin_yy");
            if (pinYyCookie !== undefined) $("#jrnl_aside #yy").val(pinYyCookie);
            const pinMnthCookie = $.cookie("pin_mnth");
            if (pinMnthCookie !== undefined) $("#jrnl_aside #mnth").val(pinMnthCookie);
            dF.JrnlDayAside.mnth();
        },

        /**
         * 저널 일자 정렬
         * @param {'ASC'|'DESC'} [toBe] - 정렬 방향 ("ASC" 또는 "DESC").
         */
        sort: function(toBe: string): void {
            const sortElement: HTMLInputElement = document.querySelector("#jrnl_aside #sort");
            const asIs = sortElement.value;
            if (toBe === undefined) toBe = (asIs !== "ASC") ? "ASC" : "DESC";
            // 쿠키에 정렬 정보 저장
            $.cookie("jrnl_day_sort", toBe, cookieOptions);
            // 정렬 값 설정
            $("#jrnl_aside #sort").val(toBe);

            // 정렬 아이콘 변경
            if (toBe === "DESC") {
                $("#jrnl_aside #sortIcon").removeClass("bi-sort-numeric-down").addClass("bi-sort-numeric-up-alt");
            } else {
                $("#jrnl_aside #sortIcon").removeClass("bi-sort-numeric-up-alt").addClass("bi-sort-numeric-down");
            }

            // 정렬 수행
            const container: HTMLElement = document.querySelector('#jrnl_day_list_div'); // 모든 저널 일자를 포함하는 컨테이너
            const days: HTMLElement[] = Array.from(container.querySelectorAll('.jrnl-day')); // 모든 'jrnl-day' 요소를 배열로 변환
            days.sort((a: HTMLElement, b: HTMLElement): number => {
                const dateA: Date = new Date(a.querySelector('.jrnl-day-header .col-1').textContent.trim());
                const dateB: Date = new Date(b.querySelector('.jrnl-day-header .col-1').textContent.trim());
                return (toBe === "ASC") ? dateA.getTime() - dateB.getTime() : dateB.getTime() - dateA.getTime();
            });

            // 컨테이너에서 모든 요소를 제거
            while (container.firstChild) {
                container.removeChild(container.firstChild);
            }

            // 정렬된 요소를 다시 컨테이너에 추가
            days.forEach((day: HTMLElement): void => {
                container.appendChild(day);
            });
        },
    }
})();