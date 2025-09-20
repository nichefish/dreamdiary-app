/**
 * jrnl_day_aside_module.ts
 * 저널 일자 사이드 스크립트 모듈
 *
 * @author nichefish
 */
if (typeof dF === 'undefined') { var dF = {} as any; }
dF.JrnlDayAside = (function(): dfModule {
    return {
        initialized: false,

        /**
         * JrnlDayAside 객체 초기화
         */
        init: function(): void {
            if (dF.JrnlDayAside.initialized) return;

            dF.JrnlDayAside.initYyMnth();
            dF.JrnlDayAside.setPinnedYyMnth();

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
            localStorage.setItem("jrnl_yy", todayYy);
            localStorage.setItem("jrnl_mnth", todayMnth);

            dF.JrnlDayAside.mnth();
            // 오늘이 제일 위에 오게 하기 위해 내림차순 정렬로 변경
            dF.JrnlDayAside.sort("DESC");
        },

        /**
         * 년도 바꾸기
         */
        changeYy: function(): void {
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
                    dF.JrnlDayAside.changeMnth();   // 월 변경 처리
                } else {
                    mnthElement.value = "";   // #mnth 값을 비움
                }
            }
        },

        /**
         * 년도 바꾸기 (모바일)
         */
        changeYyAtMobile: function(): void {
            // #yy 요소와 #mnth 요소 가져오기
            const yyMobileElement: HTMLSelectElement = document.querySelector("#jrnl_navbar #yy") as HTMLSelectElement;
            const yyElement: HTMLSelectElement = document.querySelector("#jrnl_aside #yy") as HTMLSelectElement;
            yyElement.value = yyMobileElement.value;
            const mnthMobileElement: HTMLSelectElement = document.querySelector("#jrnl_navbar #mnth") as HTMLSelectElement;
            const mnthElement: HTMLSelectElement = document.querySelector("#jrnl_aside #mnth") as HTMLSelectElement;
            mnthElement.value = mnthMobileElement.value;

            dF.JrnlDayAside.changeYy();
        },

        /**
         * 월 바꾸기 (모바일)
         */
        changeMnthAtMobile: function(): void {
            // #yy 요소와 #mnth 요소 가져오기
            const yyMobileElement: HTMLSelectElement = document.querySelector("#jrnl_navbar #yy") as HTMLSelectElement;
            const yyElement: HTMLSelectElement = document.querySelector("#jrnl_aside #yy") as HTMLSelectElement;
            yyElement.value = yyMobileElement.value;
            const mnthMobileElement: HTMLSelectElement = document.querySelector("#jrnl_navbar #mnth") as HTMLSelectElement;
            const mnthElement: HTMLSelectElement = document.querySelector("#jrnl_aside #mnth") as HTMLSelectElement;
            mnthElement.value = mnthMobileElement.value;

            dF.JrnlDayAside.changeMnth();
        },

        /**
         * 월 바꾸기
         */
        changeMnth: function(): void {
            const yearElement: HTMLSelectElement = document.querySelector("#jrnl_aside #yy") as HTMLSelectElement;
            const monthElement: HTMLSelectElement = document.querySelector("#jrnl_aside #mnth") as HTMLSelectElement;
            const selectedYear: string = yearElement.value;
            const selectedMnth: string = monthElement.value;

            localStorage.setItem("jrnl_yy", selectedYear);
            if (selectedMnth === "") return;
            localStorage.setItem("jrnl_mnth", selectedMnth);

            dF.JrnlDayAside.mnth();
        },

        /**
         * 월 바꾸기
         */
        mnth: function(): void {
            const yy:string = localStorage.getItem("jrnl_yy") ?? "9999";
            if (cF.util.isEmpty(yy)) return;
            const mnth = localStorage.getItem("jrnl_mnth") ?? "99";
            if (cF.util.isEmpty(mnth)) return;

            // 쿠키 설정하기
            // 목록 조회
            $("#jrnl_aside #dreamKeyword").val("");
            $("#jrnl_aside #diaryKeyword").val("");
            const isCalendar: boolean = Page?.calendar != null;
            if (isCalendar) {
                Page.calDt = new Date(Number(yy), Number(mnth) - 1, 1);
                Page.calendar.gotoDate(Page.calDt);
                Page.refreshEventList(Page.calDt);
            } else {
                dF.JrnlDay.yyMnthListAjax();
            }
            dF.JrnlDayTag.listAjax();
            dF.JrnlDiaryTag.listAjax();
            dF.JrnlDreamTag.listAjax();
            //
            dF.JrnlDream.inKeywordSearchMode = false;
            // 페이지 상단으로 이동
            Layout.toPageTop();
        },

        /**
         * left
         */
        left: function(): void {
            const yearElement: HTMLSelectElement = document.querySelector("#jrnl_aside #yy") as HTMLSelectElement;
            const monthElement: HTMLSelectElement = document.querySelector("#jrnl_aside #mnth") as HTMLSelectElement;
            const selecetdYear: string = yearElement.value;
            const selectedMonth: string = monthElement.value;

            if (selectedMonth && parseInt(selectedMonth) > 1) {
                // 월을 하나 감소시킴
                monthElement.value = (parseInt(selectedMonth) - 1).toString();
            } else {
                // 1월일 경우, 이전 년도로 이동하고 12월로 설정
                if (selecetdYear !== "2010") {
                    yearElement.value = (parseInt(selecetdYear) - 1).toString(); // 이전 년도로
                    monthElement.value = "12";  // 12월로 설정
                }
            }

            dF.JrnlDayAside.changeMnth();
        },

        /**
         * right
         */
        right: function(): void {
            const yearElement: HTMLSelectElement = document.querySelector("#jrnl_aside #yy") as HTMLSelectElement;
            const monthElement: HTMLSelectElement = document.querySelector("#jrnl_aside #mnth") as HTMLSelectElement;
            const selecetdYear: string = yearElement.value;
            const selectedMonth: string = monthElement.value;

            if (selectedMonth && parseInt(selectedMonth) < 12) {
                // 월을 하나 증가시킴
                monthElement.value = (parseInt(selectedMonth) + 1).toString();
            } else {
                // 12월일 경우, 다음 년도로 이동하고 1월로 설정
                yearElement.value = (parseInt(selecetdYear) + 1).toString(); // 다음 년도로
                monthElement.value = "1";  // 1월로 설정
            }

            dF.JrnlDayAside.changeMnth();
        },

        /**
         * 현재 년/월을 저장한다.
         */
        pinpoint: function(): void {
            const pinnedYy: string = localStorage.getItem("jrnl_yy");
            const pinnedMnth: string = localStorage.getItem("jrnl_mnth")

            localStorage.setItem("jrnl_pinned_yy", pinnedYy);
            localStorage.setItem("jrnl_pinned_mnth", pinnedMnth);
            $("#jrnl_aside_pinText #pinnedYy").text(pinnedYy);
            $("#jrnl_aside_pinText #pinnedMnth").text(pinnedMnth);
        },

        /**
         * 저장된 저널 년/월로 돌아가기
         */
        turnback: function(): void {
            localStorage.setItem("jrnl_yy", localStorage.getItem("jrnl_pinned_yy"));
            localStorage.setItem("jrnl_mnth", localStorage.getItem("jrnl_pinned_mnth"));
            dF.JrnlDayAside.mnth();
        },

        /**
         * 저널 일자 정렬
         * @param {'ASC'|'DESC'} [toBe] - 정렬 방향 ("ASC" 또는 "DESC").
         */
        sort: function(toBe: string): void {
            const sortElement: HTMLInputElement = document.querySelector("#jrnl_aside #sort");
            const asIs = sortElement.value;
            if (toBe == null) toBe = (asIs !== "ASC") ? "ASC" : "DESC";
            // 쿠키에 정렬 정보 저장
            localStorage.setItem("jrnl_day_sort", toBe);
            // 정렬 값 설정
            $("#jrnl_aside #sort").val(toBe);

            // 정렬 아이콘 변경
            if (toBe === "DESC") {
                $("#jrnl_aside_header #sortIcon").removeClass("bi-sort-numeric-down").addClass("bi-sort-numeric-up-alt");
            } else {
                $("#jrnl_aside_header #sortIcon").removeClass("bi-sort-numeric-up-alt").addClass("bi-sort-numeric-down");
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

        /**
         * 페이지에 조회년월 세팅
         */
        initYyMnth: function(): void {
            // 년도 설정
            const yy = localStorage.getItem("jrnl_yy");
            const yyElement: HTMLInputElement = document.querySelector("#jrnl_aside #yy") as HTMLInputElement | null;
            if (yy != null && yyElement != null) yyElement.value = yy;
            // 월 설정
            const mnth = localStorage.getItem("jrnl_mnth");
            const mnthElement: HTMLInputElement = document.querySelector("#jrnl_aside #mnth") as HTMLInputElement | null;
            if (mnth != null && mnthElement != null) mnthElement.value = mnth;
            // 정렬 설정
            const sort = localStorage.getItem("jrnl_day_sort");
            const sortElement: HTMLInputElement = document.querySelector("#jrnl_aside #sort") as HTMLInputElement | null;
            if (sort != null && sortElement != null) sortElement.value = sort;
            // 아무 정보도 없을경우 전체 데이터 로딩을 막기 위해 올해 년도 세팅
            if (yy == null && mnth == null) {
                yyElement.value = cF.date.getCurrYyStr();
            }
        },

        /**
         * 페이지에 핀 세팅
         */
        setPinnedYyMnth: function(): void {
            const pinnedYy: string = localStorage.getItem("jrnl_pinned_yy");
            if (pinnedYy != null) {
                document.querySelector("#jrnl_aside #pinnedYy")!.textContent = pinnedYy;
            }
            const pinnedMnth: string = localStorage.getItem("jrnl_pinned_mnth");
            if (pinnedMnth != null) {
                document.querySelector("#jrnl_aside #pinnedMnth")!.textContent = pinnedMnth;
            }
        },
    }
})();