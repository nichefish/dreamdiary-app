<#--
 -- 템플릿 관리(tmplat) > 목록 화면
 -->

<#import "/layout/layout_default.ftlh" as layout >

<#--toolbar::버튼 추가-->
<#assign toolbar>
    <#include "/view/domain/admin/tmplat/def/_tmplat_def_list_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

<script>
    const Page = (function() {
        return {
            /**
             * Page 객체 초기화
             */
            init: function() {
                /* 모든 table 헤더에 클릭 이벤트를 설정한다. */
                cF.util.initSortTable();
            },

            /**
             * 목록 검색
             */
            search: function() {
                $("#listForm #pageNo").val(1);
                cF.util.blockUISubmit("#listForm", "${Url.TMPLAT_DEF_LIST!}?actionTyCd=SEARCH");
            },

            /**
             * 내가 작성한 글 목록 보기
             */
            myPaprList: function() {
                const url = "${Url.TMPLAT_DEF_LIST!}";
                let param = "?searchType=nickNm&searchKeyword=${authInfo.nickNm!}&regstrId=${authInfo.userId!}&pageSize=50&actionTyCd=MY_PAPR";
                cF.util.blockUIReplace(url + param);
            },

            /**
             * 등록 화면으로 이동
             */
            regForm: function() {
                cF.util.blockUIRequest();
                cF.handlebars.template({}, "tmplat_def_reg", "show");
            },

            /**
             * 등록 (Ajax)
             */
            regAjax: function() {
                Swal.fire({
                    text: <@spring.message "view.cnfm.reg"/>,
                    showCancelButton: true,
                }).then(function(result) {
                    if (!result.value) return;
                    $("#clCdRegForm #regYn").val("Y");
                    const url = "${Url.TMPLAT_DEF_REG_AJAX!}";
                    const ajaxData = $("#clCdRegForm").serializeArray();
                    cF.util.blockUIAjax(url, 'POST', ajaxData, function(res) {
                        Swal.fire({ text: res.message })
                            .then(function() {
                                if (res.rslt) cF.util.blockUIReplace("${Url.CL_CD_LIST!}");
                            });
                    });
                });
            },

            /**
             * 상세 화면으로 이동
             * @param {string|number} postNo - 조회할 글 번호.
             */
            dtl: function(postNo) {
                if (isNaN(postNo)) return;

                $("#procForm #postNo").val(postNo);
                cF.util.blockUISubmit("#procForm", "${Url.TMPLAT_DEF_DTL!}");
            },

            /**
             * 상세 모달 호출
             * @param {string|number} postNo - 조회할 글 번호.
             */
            dtlModal: function(postNo) {
                event.stopPropagation();
                if (isNaN(postNo)) return;

                const url = "${Url.TMPLAT_DEF_DTL_AJAX!}";
                const ajaxData = { "postNo": postNo };
                cF.util.blockUIAjax(url, 'GET', ajaxData, function(res) {
                    if (!res.rslt) {
                        if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
                        return false;
                    }
                    cF.handlebars.template(res.rsltObj, "tmplat_dtl", "show");
                });
            },
        }
    })();
    document.addEventListener("DOMContentLoaded", function() {
        Page.init();
    });
</script>

<div class="card post">
    <#--begin::Card body-->
    <div class="card-body">
        <table class="table align-middle table-row-dashed fs-small gy-5 table-fixed hoverTable mb-3">
            <#--begin::Table head-->
            <thead>
                <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                    <th class="text-center wb-keepall w-10 hidden-table">번호</th>
                    <th class="col-9 col-6 text-center wb-keepall">제목</th>
                    <th class="col-3 col-2 text-center wb-keepall">작성자</th>
                    <th class="text-center wb-keepall w-15 hidden-table">등록일</th>
                    <th class="text-center wb-keepall hidden-table" style="width:13%;">조회수</th>
                </tr>
            </thead>
            <#--begin::Table body-->
            <tbody>
                <!-- 공지사항 상단고정 목록 -->
                <#list tmplatFxdList![] as tmplat>
                    <tr class="bg-fxd-list bg-hover-secondary" onclick="Page.dtl('${tmplat.postNo!}');">
                        <td class="text-center hidden-table">
                            -
                        </td>
                        <td class="text-start title-fxd pe-0">
                            <a href="javascript:void(0);">
                                <span class="vertical-middle<#if (((tmplat.imprtcYn)!"N") == "Y")!false> fw-bold</#if>">
                                    <#if (tmplat.ctgrNm!"") != "">
                                        <span class="ctgr-span ctgr-imprtc">${tmplat.ctgrNm!}</span>
                                    </#if>
                                    ${tmplat.title!}
                                </span>
                                <#if (((tmplat.imprtcYn)!"N") == "Y")!false>
                                    <span class="fs-1">&#128680;</span>
                                </#if>
                                <#--목록:: 수정권한 표시-->
                                <@component.mdfable post />
                                <#--목록:: 댓글 표시-->
                                <@component.list_comment post />
                            </a>
                            <a class="badge badge-secondary p-2 btn-white blank blink-slow float-end"
                               data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="모달로 보기">
                                <i class="bi bi-stickies fs-5 text-noti opacity-hover" onclick="Page.dtlModal('${tmplat.postNo!}');"></i>
                            </a>
                        </td>
                        <td class="text-center col-form-label">
                            ${tmplat.regstrNm!"-"}
                        </td>
                        <td class="text-center hidden-table">
                            ${tmplat.regDt!}
                        </td>
                        <td class="text-center hidden-table">
                            ${tmplat.hitCnt!0?string.number}
                        </td>
                    </tr>
                </#list>
                <!-- 공지사항 목록 -->
                <#list tmplatList![] as tmplat>
                    <tr class="bg-hover-secondary" onclick="Page.dtl('${tmplat.postNo!}');">
                        <td class="text-center hidden-table">
                            ${tmplat.rnum!}
                        </td>
                        <td class="text-start">
                            <span class="vertical-middle<#if (((tmplat.imprtcYn)!"N") == "Y")!false> fw-bold text-danger</#if>">
                                <#if (tmplat.ctgrNm!"") != "">
                                    <span class="ctgr-span ctgr-gray">${tmplat.ctgrNm!}</span>
                                </#if>
                                ${tmplat.title!}
                            </span>
                            <#if (((tmplat.imprtcYn)!"N") == "Y")!false>
                                <span class="fs-1">&#128680;</span>
                            </#if>
                            <#--목록:: 수정권한 표시-->
                            <@component.mdfable post />
                            <#--목록:: 댓글 표시-->
                            <@component.list_comment post />
                            <a class="badge badge-secondary p-2 btn-white blank blink-slow float-end"
                               data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="모달로 보기">
                                <i class="bi bi-stickies fs-5 text-noti opacity-hover" onclick="Page.dtlModal('${tmplat.postNo!}');"></i>
                            </a>
                        </td>
                        <td class="text-center col-form-label">${tmplat.regstrNm!"-"}
                            <#if tmplat.isRegstr!false>
                                <span class="badge badge-secondary opacity-75 mx-1 fs-9">나</span>
                            </#if>
                        </td>
                        <td class="text-center hidden-table">
                            ${tmplat.regDt!}
                        </td>
                        <td class="text-center hidden-table">
                            ${tmplat.hitCnt!0?string.number}
                        </td>
                    </tr>
                <#else>
                    <tr>
                        <td colspan=8" class="text-center"><@spring.message "view.list.empty"/></td>
                    </tr>
                </#list>
            </tbody>
        </table>
    </div>
    <#--begin::Card footer-->
    <div class="card-footer">
        <#--pagination::begin-->
        <#include "/view/global/_pagination.ftlh">
        <#--pagination::end-->
    </div>
</div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="postNo" id="postNo" value=""/>
</form>

<#include "/view/domain/admin/tmplat/def/_tmplat_def_reg_modal.ftlh">
<#include "/view/domain/admin/tmplat/def/_tmplat_def_dtl_modal.ftlh">

</@layout.layout>