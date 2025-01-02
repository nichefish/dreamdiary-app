<#--
 * 목록 헤더 관련 컴포넌트
 * @included_in : lib/components.ftl
 -->

<!--
 * 인풋 : datepicker
 -->
<#macro header_datepicker param searchNm initDt >
    <script>
        document.addEventListener("DOMContentLoaded", function() {
            const ptn = cF.date.ptnDate;
            /** dateRangePicker init */
            let initDt = "${initDt!}";
            if (initDt === "today") initDt = cF.date.getCurrDateStr(ptn);
            cF.datepicker.singleDatePicker("#${searchNm}", ptn, initDt);
        });
    </script>
    <span class="menu-icon me-md-2 vertical-middle">
        <label for="${searchNm}"><i class="bi bi-calendar3 fs-3 cursor-pointer"></i></label>
    </span>
    <span class="datepicker">
        <input type="text" name="${searchNm}" id="${searchNm}" class="form-control form-control-sm form-control-solid w-150px d-inline"
               value="${param[searchNm]!initDt}" placeholder="Pick date" maxlength="25" />
    </span>
</#macro>

<!--
 * 인풋 : datetimepicker
 -->
<#macro header_datetimepicker param searchNm initDt >
    <script>
        document.addEventListener("DOMContentLoaded", function() {
            const ptn = cF.date.ptnDatetime;
            /** dateRangePicker init */
            let initDt = "${initDt!}";
            if (initDt === "") initDt = cF.date.getCurrDateStr(ptn);
            cF.datepicker.singleDatePicker("#${searchNm}", ptn, initDt);
        });
    </script>
    <span class="menu-icon me-md-2 vertical-middle">
        <label for="${searchNm}"><i class="bi bi-calendar3 fs-3 cursor-pointer"></i></label>
    </span>
    <span class="datepicker">
        <input type="text" name="${searchNm}" id="${searchNm}" class="form-control form-control-sm form-control-solid w-150px d-inline"
               value="${param[searchNm]!initDt}" placeholder="Pick date" maxlength="25" />
    </span>
</#macro>

<!--
 * 인풋 : ctgrCd (select)
 -->
<#macro header_input_ctgrCd param cd>
    <select name="ctgrCd" id="ctgrCd" class="form-select form-select-sm form-select-solid">
        <option value="">글머리</option>
        <#list cd![] as ctgr>
            <option value="${ctgr.dtlCd!}" label="[${ctgr.dtlCdNm!}]" ${fn.selected(param.ctgrCd!, ctgr.dtlCd!)} />
        </#list>
    </select>
</#macro>

<!--
 * 인풋 : searchKeyword
 -->
<#macro header_input_searchKeyword param>
    <span class="position-relative">
        <#--begin::Svg Icon | path: icons/duotune/general/gen021.svg-->
        <span class="svg-icon svg-icon-3 svg-icon-gray-500 position-absolute top-50 translate-middle ms-6">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none">
                <rect opacity="0.5" x="17.0365" y="15.1223" width="8.15546" height="2" rx="1" transform="rotate(45 17.0365 15.1223)" fill="black" />
                <path d="M11 19C6.55556 19 3 15.4444 3 11C3 6.55556 6.55556 3 11 3C15.4444 3 19 6.55556 19 11C19 15.4444 15.4444 19 11 19ZM11 5C7.53333 5 5 7.53333 5 11C5 14.4667 7.53333 17 11 17C14.4667 17 17 14.4667 17 11C17 7.53333 14.4667 5 11 5Z" fill="gray" />
            </svg>
        </span>
        <#--end::Svg Icon-->
        <input type="text" name="searchKeyword" id="searchKeyword" class="form-control form-control-sm form-control-solid fs-7 ps-10"
               value="${(param.searchKeyword)!}" placeholder="검색.." maxlength="200" />
    </span>
</#macro>

<!--
 * 버튼 : 목록 헤더 검색
 -->
<#macro header_btn_search func>
    <button type="submit" class="btn btn-sm btn-primary"
            onclick="${func}">
        <i class="bi bi-search"></i>검색
    </button>
</#macro>

<!--
 * 버튼 : 내 글 보기
 -->
<#macro header_btn_mypapr func myPapr>
    <#if myPapr == false>
        <div class="btn btn-sm btn-light-primary btn-outlined"
             onclick="${func}"
             data-bs-toggle="tooltip" data-bs-placement="top" title="내가 작성한 글만 조회합니다.">
            <i class="bi bi-check"></i>내 글 보기
        </div>
    <#else>
        <div class="btn btn-sm btn-secondary btn-outlined" style="background-color:rgba(64,64,64,0.2) !important"
             onclick="${fn.closerNm(func?string)}.list();"
             data-bs-toggle="tooltip" data-bs-placement="top" title="전체 글을 조회합니다.">
            <i class="bi bi-list"></i>전체 글 보기
        </div>
    </#if>
</#macro>

<!--
 * 버튼 : 등록 화면 이동
 -->
<#macro header_btn_reg_form func>
    <!-- 버튼:: 등록하기 -->
    <button type="button" class="btn btn-sm btn-primary"
            onclick="${func}"
            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message "bs.tooltip.page.reg"/>">
        <i class="fas fa-plus"></i>등록
    </button>
</#macro>

<!--
 * 버튼 : 등록 모달 호출
 -->
<#macro header_btn_reg_modal func>
    <button type="button" class="btn btn-sm btn-primary"
            onclick="${func}"
            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="등록 모달을 호출합니다.">
        <i class="fas fa-plus"></i>등록
    </button>
</#macro>

<!--
 * 버튼 : 엑셀 다운로드
 -->
<#macro header_btn_xlsx_download func>
    <button type="button" class="btn btn-sm btn-light-primary"
            onclick="${func}"
            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="데이터를 엑셀 파일로&#10;다운로드합니다.">
        <i class="fas fa-download fs-7"></i>다운로드
    </button>
</#macro>