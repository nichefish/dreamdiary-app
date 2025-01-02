<#--
 * 레이아웃 > 페이지네이션 모듈
 * @included_in : 페이징이 있는 목록 페이지들
 -->

<script>
    const Pagination = (function() {
        return {
            /**
             * Pagination 객체 초기화
             */
            init: function() {
                /** 페이지 개수 selectbox 세팅 */
                <#if paginationInfo??>
                    const pageSize = "${pageSize!}";
                    const pageSizeSelect = document.getElementById("pageSizeSelect");
                    Array.from(pageSizeSelect.options).forEach(option => {
                        if (option.value === pageSize) {
                            option.selected = true; // 선택된 상태로 설정
                        }
                    });
                </#if>
                cF.validate.onlyNum(".page-ellipsis");
            },

            /**
             * 페이지 처리 함수 (공통)
             * @param {number} pageNo - 현재 페이지 번호.
             * @param {number} pageSize - 페이지당 항목 수.
             */
            fnPage: function(pageNo, pageSize) {
                // 페이지 출력개수 변경시 페이지 자동 재계산
                const prevPageSize = "${pageSize!}";
                if (pageSize !== undefined && pageSize !== prevPageSize) {
                    pageNo = Pagination.fnRepage(pageNo, prevPageSize, pageSize);
                }
                if (pageNo !== undefined) document.querySelector("#listForm #pageNo").value = pageNo;
                if (pageSize !== undefined) document.querySelector("#listForm #pageSize").value = pageSize;
                cF.util.blockUISubmit("#listForm", "${listUrl!}");
            },

            /**
             * 페이지 출력개수 변경시 페이지 자동 재계산
             * @param {number} pageNo - 현재 페이지 번호.
             * @param {number} prevPageSize - 이전 페이지당 항목 수.
             * @param {number} newPageSize - 새로운 페이지당 항목 수.
             * @returns {number} - 재계산된 페이지 번호.
             */
            fnRepage: function(pageNo, prevPageSize, newPageSize) {
                const offset = Number((pageNo - 1) * prevPageSize);
                return Math.floor(offset / newPageSize) + 1;
            }
        }
    })();
    document.addEventListener("DOMContentLoaded", function() {
        Pagination.init();
    });
</script>

<!--begin::pagination-->
<div id="pagination" class="mt-10">
    <#if paginationInfo??>
        <div class="row">
            <div class="col-6">
                <span class="float-start">
                    <select name="pageSizeSelect" id="pageSizeSelect" data-hide-search="true"
                            class="form-select form-select-solid"
                            onchange="Pagination.fnPage('${paginationInfo.currPageNo!}', this.value);"
                            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="목록에서 한 페이지에 표시할&#10;데이터 개수를 변경합니다.">
                        <option value="10" selected="selected">10개씩 조회</option>
                        <option value="25">25개씩 조회</option>
                        <option value="50">50개씩 조회</option>
                    </select>
                </span>
            </div>
            <div class="col-6 pt-3 px-5">
                <span class="float-end">
                    총 ${paginationInfo.totalCnt}건
                </span>
            </div>
        </div>
    </#if>
    <div class="row paging">
        <ul class="pagination">
            <#if paginationInfo??>
                <li class="page-item previous <#if paginationInfo.isFirstPage!false>disabled</#if>">
                    <a href="javascript:void(0);" class="page-link" onclick="Pagination.fnPage(1);">
                        <i class="previous"></i>
                    </a>
                </li>
                <#if paginationInfo.prevEllipsis!false>
                    <li class="page-item <#if isFirstPage??>disabled</#if>">
                        <a href="javascript:void(0);" class="page-link" onclick="Pagination.fnPage(1);">1</a></li>
                    <li class="page-item ">
                        <a class="page-link" data-kt-menu-trigger="click" data-kt-menu-placement="bottom-start"
                           data-kt-menu-flip="bottom-start">...</a>
                        <div class="menu menu-sub menu-sub-dropdown menu-rounded fw-bold fs-7 w-100px p-4" data-kt-menu="true">
                            <input type="text" class="w-70px page-ellipsis"
                                   onchange="if (this.value !== '') Pagination.fnPage(this.value);"
                                   value="" placeholder="페이지 번호" maxlength="5">
                        </div>
                    </li>
                </#if>
                <#if paginationInfo.prevPrevPrevPageNo??>
                    <li class="page-item previous">
                        <a href="javascript:void(0);" class="page-link" onclick="Pagination.fnPage(${paginationInfo.prevPrevPrevPageNo!});">
                            ${paginationInfo.prevPrevPrevPageNo!}
                        </a>
                    </li>
                </#if>
                <#if paginationInfo.prevPrevPageNo??>
                    <li class="page-item previous">
                        <a href="javascript:void(0);" class="page-link" onclick="Pagination.fnPage(${paginationInfo.prevPrevPageNo!});">
                            ${paginationInfo.prevPrevPageNo!}
                        </a>
                    </li>
                </#if>
                <#if paginationInfo.prevPageNo??>
                    <li class="page-item previous">
                        <a href="javascript:void(0);" class="page-link" onclick="Pagination.fnPage(${paginationInfo.prevPageNo!});">
                            ${paginationInfo.prevPageNo!}
                        </a>
                    </li>
                </#if>
                <li class="page-item active">
                    <a href="javascript:void(0);" class="page-link">
                        ${paginationInfo.currPageNo!}
                    </a>
                </li>
                <#if paginationInfo.nextPageNo??>
                    <li class="page-item">
                        <a href="javascript:void(0);" class="page-link" onclick="Pagination.fnPage(${paginationInfo.nextPageNo!});">
                            ${paginationInfo.nextPageNo!}
                        </a>
                    </li>
                </#if>
                <#if paginationInfo.nextNextPageNo??>
                    <li class="page-item">
                        <a href="javascript:void(0);" class="page-link" onclick="Pagination.fnPage(${paginationInfo.nextNextPageNo!});">
                            ${paginationInfo.nextNextPageNo!}
                        </a>
                    </li>
                </#if>
                <#if paginationInfo.nextNextNextPageNo??>
                    <li class="page-item">
                        <a href="javascript:void(0);" class="page-link" onclick="Pagination.fnPage(${paginationInfo.nextNextNextPageNo!});">
                            ${paginationInfo.nextNextNextPageNo!}
                        </a>
                    </li>
                </#if>
                <#if paginationInfo.nextEllipsis!false>
                    <li class="page-item">
                        <a class="page-link page-ellipsis" data-kt-menu-trigger="click" ,
                           data-kt-menu-placement="bottom-start" , data-kt-menu-flip="bottom-start">...</a>
                        <div class="menu menu-sub menu-sub-dropdown menu-rounded fw-bold fs-7 w-100px p-4"
                             data-kt-menu="true">
                            <input type="text" class="w-70px page-ellipsis"
                                   onchange="if (this.value !== '') Pagination.fnPage(this.value);"
                                   value="" placeholder="페이지 번호" maxlength="5">
                        </div>
                    </li>
                    <li class="page-item <#if paginationInfo.isLastPage!false>disabled</#if>">
                        <a href="javascript:void(0);" class="page-link" onclick="Pagination.fnPage(${paginationInfo.lastPageNo!});">
                            ${paginationInfo.lastPageNo!}
                        </a>
                    </li>
                </#if>
                <li class="page-item next <#if paginationInfo.isLastPage!false>disabled</#if>">
                    <a href="javascript:void(0);" class="page-link" onclick="Pagination.fnPage(${paginationInfo.lastPageNo!});">
                        <i class="next"></i>
                    </a>
                </li>
            </#if>
        </ul>
    </div>
</div>
<!--end::pagination-->