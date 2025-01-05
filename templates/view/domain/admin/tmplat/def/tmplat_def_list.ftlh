<#--
 -- 템플릿 관리(tmplat) > 목록 화면
 -->

<#import "/layout/layout_default.ftlh" as layout >

<#--toolbar::버튼 추가-->
<#assign toolbar>
    <#include "/view/domain/admin/tmplat/def/_tmplat_def_list_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

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

<#include "/view/domain/admin/tmplat/def/_modal/_tmplat_def_reg_modal.ftlh">
<#include "/view/domain/admin/tmplat/def/_modal/_tmplat_def_dtl_modal.ftlh">
<#--include::script modules-->
<script type="text/javascript" src="/js/view/domain/admin/tmplat/def/tmplat_def_module.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/domain/admin/tmplat/def/tmplat_def_list.js?${releaseDate!}"></script>

</@layout.layout>