<#--
 * 휴가계획서(vcatn_papr) > 목록 화면
 -->

<#import "/layout/layout_default.ftlh" as layout >

<#--toolbar::버튼 추가-->
<#assign toolbar>
    <#include "/view/domain/vcatn/papr/_vcatn_papr_list_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

<#--#include:: component:: 태그 클릭 필터링-->
<#include "/view/global/_common/_clsf/tag/_tag_list_header.ftlh">

<button type="button" class="btn btn-sm btn-light btn-active-secondary mx-1"
        onclick="$('#vcatn_papr_rule_modal').modal('show');"
        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="휴가규칙 팝업을 호출합니다.">
    <i class="fas fa-info-circle"></i>휴가규칙
</button>
<#include "/view/domain/vcatn/papr/_modal/_vcatn_papr_rule_modal.ftlh">

<div class="card post">
    <#--begin::Card body-->
    <div class="card-body">
        <#--begin::Table-->
        <table class="table align-middle table-row-dashed fs-small gy-5 table-fixed hoverTable mb-3">
            <#--begin::Table head-->
            <thead>
                <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 fw-bolder text-muted">
                    <th class="text-center wb-keepall w-10 hidden-table">번호</th>
                    <th class="col-8 col-6 text-center wb-keepall text-center" style="width:40%;">제목</th>
                    <th class="col-lg-1 text-center wb-keepall hidden-table">첨부파일</th>
                    <th class="col-3 col-3 text-center wb-keepall w-12">작성자</th>
                    <th class="text-center wb-keepall w-8 hidden-table">확인 여부</th>
                    <th class="text-center wb-keepall w-10 hidden-table">등록일</th>
                    <th class="text-center wb-keepall w-10 hidden-table">조회수</th>
                </tr>
            </thead>
            <#--begin::Table body-->
            <tbody class="cursor-pointer">
                <#--begin::상단고정 목록-->
                <#list vcatnPaprFxdList![] as post>
                    <tr class="bg-fxd-list <#if post.isRegstr!false>bg-fxd-list</#if> bg-hover-secondary"
                        onclick="dF.VcatnPapr.dtl('${post.postNo!}');">
                        <td class="text-center hidden-table">
                            ${post.rnum!"-"}
                        </td>
                        <td class="text-start title-fxd">
                            <div class="d-flex align-items-center justify-content-between">
                                <span>
                                    <a href="${Url.VCATN_PAPR_DTL!}?postNo=${post.postNo!}" class="title-fxd">
                                        <span class="pe-4">(작성예시)</span>
                                        <#list post.schdulList![] as schdul>
                                            <span class="pe-4">
                                                <span class="ctgr-span ctgr-imprtc">${schdul.vcatnNm!}</span>
                                                ${schdul.bgnDt!}
                                                <#if schdul.endDt?? && schdul.bgnDt != schdul.endDt>~ ${schdul.endDt!}</#if>
                                            </span>
                                        </#list>
                                    </a>
                                    <#--목록:: 댓글 표시-->
                                    <@component.list_comment post />
                                    <#--목록:: 새 글이면 'N' 표시-->
                                    <#if post.isNew>
                                        <div class="badge border-0 text-white bg-noti blink fs-8 ms-2">N</div>
                                    </#if>
                                </span>
                                <#--목록:: 모달 팝업 열기 버튼 표시-->
                                <@component.list_dtl_modal post "dF.VcatnPapr.dtlModal" />
                            </div>
                        </td>
                        <td class="text-center col-form-label hidden-table">
                            <#--목록:: 첨부파일 모달 열기 버튼 표시-->
                            <@component.list_atch_file post />
                        </td>
                        <td class="text-center ">
                            <#if (post.regstrInfo.proflImgUrl)??>
                                <div class="btn btn-icon btn-active-light-primary position-relative w-15px h-15px w-md-20px h-md-20px me-1">
                                    <img src="${(post.regstrInfo.proflImgUrl)!}" class="img-thumbnail p-0 w-100" />
                                </div>
                            </#if>
                            ${post.regstrNm!"-"}
                            <#if post.isRegstr!false>
                                <span class="badge badge-secondary opacity-75 mx-1 fs-9">나</span>
                            </#if>
                        </td>
                        <td class="text-center hidden-table">
                            <#if post.cfYn! == "Y">
                                <span class="text-success">
                                    확인<i class="bi bi-check text-success"></i>
                                </span>
                            <#else>
                                -
                            </#if>
                        </td>
                        <td class="text-center hidden-table">
                            ${post.regDt!}
                        </td>
                        <td class="text-center hidden-table">
                            ${post.hitCnt!0?string.number}
                        </td>
                    </tr>
                </#list>
                <#--begin::목록-->
                <#list vcatnPaprList![] as post>
                    <tr class="bg-hover-secondary <#if post.isRegstr!false>bg-fxd-list</#if>"
                        onclick="dF.VcatnPapr.dtl('${post.postNo!}');">
                        <td class="text-center hidden-table">
                            ${post.rnum!}
                        </td>
                        <td class="text-start">
                            <div class="d-flex align-items-center justify-content-between">
                                <span>
                                    <a href="${Url.VCATN_PAPR_DTL!}?postNo=${post.postNo!}" class="text-dark">
                                        <#list post.schdulList as schdul>
                                            <span class="pe-4">
                                                <span class="ctgr-span ctgr-gray">${schdul.vcatnNm!}</span>
                                                ${schdul.bgnDt!}
                                                <#if schdul.endDt?? && schdul.bgnDt != schdul.endDt>~ ${schdul.endDt!}</#if>
                                            </span>
                                        </#list>
                                    </a>
                                    <#--목록:: 댓글 표시-->
                                    <@component.list_comment post />
                                    <#--목록:: 새 글이면 'N' 표시-->
                                    <#if post.isNew>
                                        <div class="badge border-0 text-white bg-noti blink fs-8 ms-2">N</div>
                                    </#if>
                                </span>
                                <#--목록:: 모달 팝업 열기 버튼 표시-->
                                <@component.list_dtl_modal post "dF.VcatnPapr.dtlModal" />
                            </div>
                        </td>
                        <td class="text-center col-form-label hidden-table">
                            <#--목록:: 첨부파일 모달 열기 버튼 표시-->
                            <@component.list_atch_file post />
                        </td>
                        <td class="text-center ">
                            <#--목록:: 최종수정자 표시-->
                            <@component.list_managtr post />
                        </td>
                        <td class="text-center hidden-table">
                            <#if post.cfYn! == "Y">
                                <span class="text-success">
                                    확인<i class="bi bi-check text-success"></i>
                                </span>
                            <#else>
                                -
                            </#if>
                        </td>
                        <td class="text-center hidden-table">
                            ${post.regDt!}
                        </td>
                        <td class="text-center hidden-table">
                            ${post.hitCnt!0?string.number}
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
    <input type="hidden" name="postNo" id="postNo" />
</form>

<#--include::modals-->
<#include "/view/global/_common/_clsf/comment/_modal/_comment_list_modal.ftlh">
<#include "/view/global/_common/file/_modal/_atch_file_list_modal.ftlh">

<#--include::vcatn_papr_module-->
<#include "/view/domain/vcatn/papr/_modal/_vcatn_papr_dtl_modal.ftlh">
<script type="text/javascript" src="/js/view/domain/vactn/papr/vactn_papr_module.js?${releaseDate!}"></script>

<!--include::page scripts-->
<script type="text/javascript" src="/js/view/domain/vactn/papr/vactn_papr_list.js?${releaseDate!}"></script>

</@layout.layout>