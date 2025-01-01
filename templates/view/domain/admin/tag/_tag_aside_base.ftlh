<#--
 * 태그 관리 > ASIDE (우측 사이드바)
 * @included_in : jrnl_day_page.ftlh
 -->

<script>
    const JrnlDayAside = (function() {
        return {
            cookieOptions: {
                path: "/jrnl/day/",
                expires: cF.date.getCurrDateAddDay(36135)
            },

            /**
             * JrnlDayAside 객체 초기화
             */
            init: function() {
                const pinYyCookie = $.cookie("pin_yy");
                if (pinYyCookie !== undefined) {
                    $("#jrnl_aside #pinYy").text(pinYyCookie);
                }
                const pinMnthCookie = $.cookie("pin_mnth");
                if (pinMnthCookie !== undefined) {
                    $("#jrnl_aside #pinMnth").text(pinMnthCookie);
                }
            },

            /**
             * 오늘 날짜로 가기
             */
            today: function() {
                const todayYy = cF.date.getCurrYyStr();
                const todayMnth = cF.date.getCurrMnthStr();
                $("#jrnl_aside #yy").val(todayYy);
                $("#jrnl_aside #mnth").val(todayMnth);
                JrnlDayAside.mnth();
                // 오늘이 제일 위에 오게 하기 위해 내림차순 정렬로 변경
                JrnlDayAside.sort("DESC");
            },

            /**
             * 년도 바꾸기
             */
            yy: function() {
                cF.handlebars.template(null, "jrnl_day_list");
                cF.handlebars.template([], "jrnl_day_tag_list");
                cF.handlebars.template([], "jrnl_diary_tag_list");
                cF.handlebars.template([], "jrnl_dream_tag_list");
                JrnlDream.inKeywordSearchMode = false;
                if ($("#jrnl_aside #yy").val() === "2010") {
                    $("#jrnl_aside #mnth").val("99");
                    JrnlDayAside.mnth();
                } else {
                    $("#jrnl_aside #mnth").val("");
                }
            },

            /**
             * 월 바꾸기
             */
            mnth: function() {
                // 쿠키 설정하기
                $.cookie("jrnl_yy", $("#jrnl_aside #yy").val(), JrnlDayAside.cookieOptions);
                if ($("#jrnl_aside #mnth").val() === "") return;
                $.cookie("jrnl_mnth", $("#jrnl_aside #mnth").val(), JrnlDayAside.cookieOptions);
                $("#jrnl_aside #dreamKeyword").val("");
                // 목록 조회
                JrnlDay.yyMnthListAjax();
                JrnlDayTag.listAjax();
                JrnlDiaryTag.listAjax();
                JrnlDreamTag.listAjax();
                //
                JrnlDream.inKeywordSearchMode = false;
                // 페이지 상단으로 이동
                cF.util.toPageTop();
            },

            /**
             * 현재 년/월을 저장한다.
             */
            pinpoint: function() {
                const pinYy = $("#jrnl_aside #yy").val();
                const pinMnth = $("#jrnl_aside #mnth").val();
                $.cookie("pin_yy", pinYy, JrnlDayAside.cookieOptions);
                $.cookie("pin_mnth", pinMnth, JrnlDayAside.cookieOptions);
                $("#jrnl_aside #pinYy").text(pinYy);
                $("#jrnl_aside #pinMnth").text(pinMnth);
            },

            /**
             * 저장된 저널 년/월로 돌아가기
             */
            turnback: function() {
                const pinYyCookie = $.cookie("pin_yy");
                if (pinYyCookie !== undefined) $("#jrnl_aside #yy").val(pinYyCookie);
                const pinMnthCookie = $.cookie("pin_mnth");
                if (pinMnthCookie !== undefined) $("#jrnl_aside #mnth").val(pinMnthCookie);
                JrnlDayAside.mnth();
            },

            /**
             * 저널 일자 정렬
             */
            sort: function(toBe) {
                const asIs = $("#jrnl_aside #sort").val();
                if (toBe === undefined) toBe = (asIs !== "ASC") ? "ASC" : "DESC";
                // 쿠키 설정하기
                const cookieOptions = {
                    path: "/jrnl/day/",
                    expires: cF.date.getCurrDateAddDay(36135)
                };
                $.cookie("jrnl_day_sort", toBe, cookieOptions);
                // 값 설정
                $("#jrnl_aside #sort").val(toBe);
                if (toBe === "DESC") {
                    $("#jrnl_aside #sortIcon").removeClass("bi-sort-numeric-down").addClass("bi-sort-numeric-up-alt");
                } else {
                    $("#jrnl_aside #sortIcon").removeClass("bi-sort-numeric-up-alt").addClass("bi-sort-numeric-down");
                }
                const container = document.querySelector('#jrnl_day_list_div'); // 모든 저널 일자를 포함하는 컨테이너
                const days = Array.from(container.querySelectorAll('.jrnl-day')); // 모든 'jrnl-day' 요소를 배열로 변환
                days.sort((a, b) => {
                    const dateA = new Date(a.querySelector('.jrnl-day-header .col-1').textContent.trim());
                    const dateB = new Date(b.querySelector('.jrnl-day-header .col-1').textContent.trim());
                    return (toBe === "ASC") ? dateA - dateB : dateB - dateA;
                });
                // 컨테이너에서 모든 요소를 제거
                while (container.firstChild) {
                    container.removeChild(container.firstChild);
                }
                // 정렬된 요소를 다시 컨테이너에 추가
                days.forEach(day => {
                    container.appendChild(day);
                });
            },
        }
    })();
</script>

<#--begin::aside-->
<div id="kt_app_aside" class="app-aside flex-column mt-7"
     data-kt-drawer="true"
     data-kt-drawer-name="app-aside"
     data-kt-drawer-activate="{default: true, lg: false}"
     data-kt-drawer-overlay="true"
     data-kt-drawer-width="auto"
     data-kt-drawer-direction="end"
     data-kt-drawer-toggle="#kt_app_aside_toggle">

    <#--begin::Aside wrapper-->
    <div id="kt_app_aside_wrapper" class="hover-scroll-y px-5 mx-5 my-5"
         data-kt-scroll="true"
         data-kt-scroll-activate="true"
         data-kt-scroll-height="auto"
         data-kt-scroll-dependencies="#kt_app_header"
         data-kt-scroll-wrappers="#kt_app_aside"
         data-kt-scroll-offset="5px"
         data-kt-scroll-save-state="true">

        <#--begin::Aside-->
        <div class="card card-reset card-p-0">
            <#--begin::Card header-->
            <div class="card-header min-h-auto mb-5">
                <h3 class="card-title text-gray-900 fw-bold fs-3">Filters</h3>
                <#--begin::Toolbar-->
                <div class="card-toolbar">
                    <a href="#" class="btn btn-sm btn-icon btn-color-gray-500 btn-light">
                        <i class="ki-duotone ki-magnifier fs-3">
                            <span class="path1"></span>
                            <span class="path2"></span>
                        </i>
                    </a>
                </div>
            </div>
            <!--end::Header-->
            <#--begin::Body-->
            <div class="card-body" id="jrnl_aside">
                <input type="hidden" name="sort" id="sort">
                <#--begin::row-->
                <div class="mb-4">
                    <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                            onclick="JrnlDayAside.today();"
                            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="꿈을 키워드로 검색합니다.">
                        오늘 일자 조회하기<i class="bi bi-search ps-1 pe-0"></i>
                    </button>
                    <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                            onclick="JrnlDayAside.sort();"
                            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="날짜 정렬을 변경합니다.">
                        <i class="bi bi-sort-numeric-up-alt fs-2 pe-0" id="sortIcon"></i>
                    </button>
                </div>
                <#--begin::row-->
                <div class="mb-4">
                    <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                            onclick="JrnlDayAside.pinpoint();"
                            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="꿈을 키워드로 검색합니다.">
                        현재 년월 저장<i class="bi bi-pin-map ps-1 pe-0"></i>
                    </button>
                    <span class="px-2">
                        <span id="pinYy" class="text-muted text-underline-dotted">----</span>
                        <span> / </span>
                        <span id="pinMnth" class="text-muted text-underline-dotted">--</span>
                    </span>
                    <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                            onclick="JrnlDayAside.turnback();"
                            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="저장된 년월로 이동합니다.">
                        <i class="bi bi-reply-all"></i>
                    </button>
                </div>
                <#--begin::row-->
                <div class="mb-7 d-flex justify-content-between gap-4">
                    <div class="col">
                        <span class="text-gray-900 fs-h6 fw-bold d-inline-block mb-1">년도</span>
                        <select name="yy" id="yy" class="form-select" aria-label="Select example"
                                onchange="JrnlDayAside.yy();">
                            <option value="">----</option>
                            <#assign currYy = DateUtils.getCurrYy()?number />
                            <#assign startYy = 2010 />
                            <#list currYy..startYy as year>
                                <option value="${year}"><#if year == 2010>~</#if>${year}년</option>
                            </#list>
                        </select>
                    </div>
                    <div class="col">
                        <span class="text-gray-900 fs-h6 fw-bold d-inline-block mb-1">월</span>
                        <select name="mnth" id="mnth" class="form-select" aria-label="Select example"
                                onchange="JrnlDayAside.mnth();">
                            <option value="">--</option>
                            <#list 1..12 as mnth>
                                <option value="${mnth}">${mnth}</option>
                            </#list>
                            <option value="99">전체 월</option>
                        </select>
                    </div>
                </div>
                <#--begin::row-->
                <div class="mb-6">
                    <span class="text-gray-900 fs-h6 fw-bold d-inline-block mb-1">꿈 키워드 검색</span>
                    <div class="d-flex gap-1">
                        <input type="text" name="dreamKeyword" id="dreamKeyword" class="form-control"
                               value="" placeholder="꿈 키워드 검색" maxlength="200"/>
                        <button type="button" class="btn btn-sm btn-outline btn-light-primary px-4"
                                onclick="JrnlDream.keywordListAjax();"
                                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="꿈을 키워드로 검색합니다.">
                            <i class="bi bi-search pe-0"></i>
                        </button>
                    </div>
                </div>
            </div>
        </div>
        <!--end::Aside-->
    </div>
    <!--end::Aside wrapper-->
</div>
<!--end::aside-->