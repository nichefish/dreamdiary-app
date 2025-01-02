<#--
 * 파일시스템(flsys) > 검색 영역 Header
 * @included_in : flsys_home.ftlh
 -->

<script>
    const Flsys = (function() {
        return {
            /** 현재 경로 저장 */
            currPath: "${Constant.HOME_FLSYS!}",
            refCnt: 0,

            /**
             * Flsys 객체 초기화
             */
            init: function(mode) {
                Flsys.mode = mode;
                $("#flsys_file_vod_modal").on("hidden.bs.modal", function() {
                    $("#flsys_file_vod_div").empty();
                });
            },

            /**
             * 파일시스템 모달 호출
             */
            flsysModal: function(filePath) {
                $("#flsys_home_modal").modal("show");
                Flsys.flsysListAjax(filePath);
            },

            /**
             * 탭 활성화
             */
            activateTab: function(tabObj) {
                $("li.nav-item a.nav-link").removeClass("active");
                $(tabObj).addClass("active");
            },

            /**
             * 목록 조회 (Ajax)
             */
            flsysListAjax: function(filePath) {
                const url = "${Url.FLSYS_LIST_AJAX!}";
                const ajaxData = { "filePath": filePath };
                cF.ajax.post(url, ajaxData, function(res) {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return false;
                    }
                    const { rsltObj } = res;
                    rsltObj.mode = Flsys.mode;
                    cF.handlebars.template(rsltObj, "flsys_list");
                    Flsys.currPath = rsltObj.filePath;
                    $("#absolute_path_div").text(rsltObj.filePath);
                    cF.handlebars.template(rsltObj, "flsys_home_header_path");
                });
            },

            /**
             * 새로고침 처리 (Ajax)
             */
            flsysRefreshAjax: function() {
                Flsys.flsysListAjax(Flsys.currPath);
            },

            /**
             * 탐색기로 열기
             */
            openInExplorer: function(filePath, objectTy) {
                const url = "${Url.FLSYS_OPEN_IN_EXPLORER_AJAX!}";
                const ajaxData = { "filePath": filePath, "objectTy": objectTy };
                cF.ajax.post(url, ajaxData, function(res) {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return false;
                    }
                    window.open("file:///" + "//${FILE_SVR_IP_ADDR!}/" + filePath);
                });
            },

            /**
             * vod 재생
             */
            fileDownload: function(filePath) {
                Swal.fire({
                    text: <@spring.message "view.cnfm.download"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    cF.util.blockUIFileDownload();
                    $("#flsysForm #filePath").val(filePath);
                    $("#flsysForm").attr("action", "${Url.FLSYS_FILE_DOWNLOAD!}").submit();
                });
            },

            /**
             * vod 재생
             */
            playVod: function(filePath) {
                const obj = { "filePath": filePath };
                cF.handlebars.template(obj, "flsys_file_vod", "show");
            },

            /**
             * 파일 참조 추가
             */
            addRef: function(filePath) {
                const obj = { "filePath": filePath, "refCnt": Flsys.refCnt++ }
                cF.handlebars.append(obj, "flsys_ref");
                cF.util.closeModal();
            },

            /**
             * 기존 첨부파일 삭제 플래그 세팅
             */
            delRef: function(obj) {
                Swal.fire({
                    text: <@spring.message "view.cnfm.del"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    $(obj).parent().remove();
                });
            },

            /**
             * 실행하기
             */
            fileExec: function(filePath) {
                const url = "${Url.FLSYS_FILE_EXEC_AJAX!}";
                const ajaxData = { "filePath": filePath };
                cF.ajax.post(url, ajaxData, function(res) {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return false;
                    }
                });
            }
        }
    })();
</script>

<#--begin::Form-->
<form name="flsysForm" id="flsysForm" method="get">
    <input type="hidden" name="filePath" id="filePath">
</form>

<#--
<form name="listForm" id="listForm" class="d-flex align-items-center flex-nowrap gap-2" data-url="${listUrl!}"method="get">
    <input type="hidden" name="pageNo" id="pageNo" value="${searchParam.pageNo!1}">
    <input type="hidden" name="pageSize" id="pageSize" value="${searchParam.pageSize!10}">

    <div class="d-flex align-items-center">
        <!-- datePicker &ndash;&gt;
        <div class="position-relative">
            <span class="menu-icon me-md-2 vertical-middle">
                <label for="searchStartDt"><i class="bi bi-calendar3 fs-2 cursor-pointer"></i></label>
            </span>
            <input type="text" name="searchStartDt" id="searchStartDt"  class="form-control form-control-solid w-150px fs-7 me-md-2 fw-bolder d-inline"]
                   value="" placeholder="Pick date" readonly/>
            <span class="me-md-2">~</span>
            <span class="menu-icon me-md-2 vertical-middle">
                <label for="searchEndDt"><i class="bi bi-calendar3 fs-2 cursor-pointer"></i></label>
            </span>
            <input type="text" name="searchEndDt" id="searchEndDt" class="form-control form-control-solid w-150px fs-7 me-md-2 fw-bolder d-inline"
                   value="" placeholder="Pick date" readonly/>
        </div>
        <div class="position-relative w-md-1250px me-md-2">
            <!--begin::Select&ndash;&gt;
            <select name="rslt" id="rslt" class="form-select form-select-solid fs-7 fw-bolder me-md-2 w-125px" data-hide-search="true">
                <option value="">작업결과</option>
                <option value="true" label="성공" ${fn.selected(searchParam.rslt!, "true")} />
                <option value="false" label="실패" ${fn.selected(searchParam.rslt!, "false")} />
            </select>
        </div>
        <div class="position-relative w-md-100px me-md-2">
            <select name="searchType" id="searchType" class="form-select form-select-solid fs-7 fw-bolder">
                <option value="nickNm">이름</option>
                <option value="userId">아이디</option>
            </select>
        </div>
        <div class="position-relative w-md-200px me-md-2">
            <#--begin::Svg Icon | path: icons/duotune/general/gen021.svg&ndash;&gt;
            <span class="svg-icon svg-icon-3 svg-icon-gray-500 position-absolute top-50 translate-middle ms-6">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none">
                    <rect opacity="0.5" x="17.0365" y="15.1223" width="8.15546" height="2" rx="1" transform="rotate(45 17.0365 15.1223)" fill="black" />
                    <path d="M11 19C6.55556 19 3 15.4444 3 11C3 6.55556 6.55556 3 11 3C15.4444 3 19 6.55556 19 11C19 15.4444 15.4444 19 11 19ZM11 5C7.53333 5 5 7.53333 5 11C5 14.4667 7.53333 17 11 17C14.4667 17 17 14.4667 17 11C17 7.53333 14.4667 5 11 5Z" fill="black" />
                </svg>
            </span>
            <!--end::Svg Icon&ndash;&gt;
            <input type="text" name="searchKeyword" id="searchKeyword" class="form-control form-control-solid fs-7 ps-10"
                   value="" placeholder="검색" maxlength="200" />
        </div>
        <div class="card-title me-md-2">
            <label class="form-label fw-bolder text-dark"> </label>
        </div>
        <div class="d-flex align-items-center me-md-2">
            <button type="submit" class="btn btn-sm btn-primary me-5"
                    onclick="Swal.fire('준비중입니다.');">
                <i class="bi bi-search"></i>검색
            </button>
        </div>
        <!--end::Input group&ndash;&gt;
    </div>
    <!--end::Compact form&ndash;&gt;
    <!--begin::Card title&ndash;&gt;
    <div class="card-title text-start">
        <div class="d-flex align-items-center position-relative my-1">
        </div>
    </div>
    <div class="card-toolbar">
        <!--begin::Toolbar&ndash;&gt;
&lt;#&ndash;        <div class="d-flex justify-content-end" data-kt-user-table-toolbar="base">
            <button type="button" class="btn btn-sm btn-light-primary" style="margin-left:auto;"
                     onclick="Page.xlsxDownload();"
                        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="데이터를 엑셀 파일로&#10;다운로드합니다.">
                <i class="fas fa-download fs-7"></i>다운로드
            </button>
        </div>&ndash;&gt;
    </div>
    <!--end::Card toolbar&ndash;&gt;
</form>
-->

