<#--
 * 게시판(board_post) > 목록 화면
 -->

<#import "/layout/layout_default.ftlh" as layout >

<#--toolbar::버튼 추가-->
<#assign toolbar>
    <#include "/view/domain/board/post/_board_post_list_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

<#--include::script modules-->
<#include "/view/domain/board/post/_board_post_scripts.ftlh">

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
        }
    })();
    document.addEventListener("DOMContentLoaded", function() {
        Page.init();
    });
</script>

<#--#include:: component:: 태그 클릭 필터링-->
<#include "/view/global/_common/_clsf/tag/_tag_list_header.ftlh">

<#if boardCd == "cmpyLife">
    <button type="button" class="btn btn-sm btn-light btn-active-secondary mx-1"
            onclick="$('#cmpy_life_modal').modal('show');"
            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="슬기로운 회사생활 안내 팝업을 호출합니다.">
        <span class="blink"><i class="bi bi-lightbulb-fill text-noti"></i></span>슬기로운 회사생활
    </button>
</#if>

<div class="card post">
    <#--begin::Card body-->
    <div class="card-body">
        <#--begin::Table-->
        <table class="table align-middle table-row-dashed fs-small gy-5 table-fixed hoverTable mb-3">
            <#--begin::Table head-->
            <thead>
                <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                    <th class="text-center wb-keepall w-10 hidden-table">번호</th>
                    <th class="col-lg-8 col-9 text-center wb-keepall">제목</th>
                    <th class="col-lg-1 text-center wb-keepall hidden-table">첨부파일</th>
                    <th class="col-lg-2 col-3 text-center wb-keepall">최종수정자</th>
                    <th class="text-center wb-keepall w-15 hidden-table">최종수정일</th>
                    <th class="text-center wb-keepall w-15 hidden-table">조회수</th>
                </tr>
            </thead>
            <#--begin::Table body-->
            <tbody>
                <!-- 상단고정목록 -->
                <#list postFxdList![] as post>
                    <tr class="bg-fxd-list bg-hover-secondary cursor-default">
                        <td class="text-center hidden-table">
                            -
                        </td>
                        <td class="text-start title-fxd <#if post.tagStrList??>pb-4</#if>">
                            <div class="row d-flex align-items-center justify-content-between">
                                <div class="col-xl-8 col-12 d-flex flex-column">
                                    <div class="my-1">
                                        <a href="${Url.BOARD_POST_DTL!}?boardCd=${post.boardCd!}&postNo=${post.postNo!}&notionPageId=${post.notionPageId!}"
                                           class="title-fxd vertical-middle text-underline-dotted <#if (((post.imprtcYn)!"N") == "Y")!false>fw-bold</#if>"
                                           data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="상세 화면으로 이동합니다.">
                                            <#if (post.ctgrNm!"") != "">
                                                <span class="ctgr-span ctgr-imprtc">${post.ctgrNm!}</span>
                                            </#if>
                                            ${post.title!}
                                        </a>
                                        <#if (((post.imprtcYn)!"N") == "Y")!false>
                                            <span class="fs-4 blink ms-1">&#128680;</span>
                                        </#if>
                                        <#--목록:: 수정권한 표시-->
                                        <@component.mdfable post />
                                        <#--목록:: 댓글 표시-->
                                        <@component.list_comment post />
                                        <#--목록:: 새 글이면 'N' 표시-->
                                        <#if post.isNew>
                                            <div class="badge border-0 text-white bg-noti blink fs-8 ms-2">N</div>
                                        </#if>
                                    </div>
                                    <div class="ms-1">
                                        <#--목록:: 태그 표시-->
                                        <@component.list_tag post />
                                    </div>
                                </div>
                                <div class="col-xl-4 col-12 d-flex justify-content-end align-items-center">
                                    <#--목록:: 모달 팝업 열기 버튼 표시-->
                                    <@component.list_dtl_modal post "BoardPost.dtlModal" />
                                </div>
                            </div>
                        </td>
                        <td class="text-center col-form-label hidden-table">
                            <#--목록:: 첨부파일 모달 열기 버튼 표시-->
                            <@component.list_atch_file post />
                        </td>
                        <td class="text-center col-form-label">
                            <#--목록:: 최종수정자 표시-->
                            <@component.list_managtr post />
                        </td>
                        <td class="text-center hidden-table">
                            <#--목록:: 최종수정일 표시-->
                            <@component.list_managtDt post />
                        </td>
                        <td class="text-center hidden-table">
                            ${post.hitCnt!0?string.number}
                        </td>
                    </tr>
                </#list>
                <#list postList![] as post>
                    <tr class="bg-hover-secondary cursor-default">
                        <td class="text-center hidden-table">
                            ${post.rnum!}
                        </td>
                        <td class="text-start <#if post.tagStrList??>pb-4</#if>">
                            <div class="row d-flex align-items-center justify-content-between align-items-center">
                                <div class="col-xl-8 col-12 d-flex flex-column">
                                    <div class="my-1">
                                        <a href="${Url.BOARD_POST_DTL!}?boardCd=${post.boardCd!}&postNo=${post.postNo!}&notionPageId=${post.notionPageId!}"
                                           class="text-dark vertical-middle text-underline-dotted <#if (((post.imprtcYn)!"N") == "Y")!false>fw-bold text-danger</#if>"
                                           data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="상세 화면으로 이동합니다.">
                                            <#if (post.ctgrNm!"") != "">
                                                <span class="ctgr-span ctgr-gray">${post.ctgrNm!}</span>
                                            </#if>
                                            ${post.title!}
                                        </a>
                                        <#if (((post.imprtcYn)!"N") == "Y")!false>
                                            <span class="fs-4 blink ms-1">&#128680;</span>
                                        </#if>
                                        <#--목록:: 수정권한 표시-->
                                        <@component.mdfable post />
                                        <#--목록:: 댓글 표시-->
                                        <@component.list_comment post />
                                        <#--목록:: 새 글이면 'N' 표시-->
                                        <#if post.isNew>
                                            <div class="badge border-0 text-white bg-noti blink fs-8 ms-2">N</div>
                                        </#if>
                                    </div>
                                    <div class="ms-1">
                                        <#--목록:: 태그 표시-->
                                        <@component.list_tag post />
                                    </div>
                                </div>
                                <div class="col-xl-4 col-12 d-flex justify-content-end align-items-center">
                                    <#--목록:: 모달 팝업 열기 버튼 표시-->
                                    <@component.list_dtl_modal post "BoardPost.dtlModal" />
                                </div>
                            </div>
                        </td>
                        <td class="text-center col-form-label hidden-table">
                            <#--목록:: 첨부파일 모달 열기 버튼 표시-->
                            <@component.list_atch_file post />
                        </td>
                        <td class="text-center col-form-label">
                            <#--목록:: 최종수정자 표시-->
                            <@component.list_managtr post />
                        </td>
                        <td class="text-center hidden-table">
                            <#--목록:: 최종수정일 표시-->
                            <@component.list_managtDt post />
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
    <input type="hidden" name="postNo" id="postNo" value=""/>
    <input type="hidden" name="boardCd" id="boardCd" value="${boardCd!}"/>
</form>

<#include "/view/domain/board/post/_board_post_dtl_modal.ftlh">
<#include "/view/global/_common/_clsf/comment/_comment_list_modal.ftlh">
<#include "/view/global/_common/file/_atch_file_list_modal.ftlh">

</@layout.layout>

