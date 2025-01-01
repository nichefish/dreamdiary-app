<#--
 * 게시판(board_post) > 등록/수정 화면
 * tinyMCE 정책 때문에 문서 최상단에 <!DOCTYPE html> 추가
 -->

<!DOCTYPE html>
<#import "/layout/layout_default.ftlh" as layout >

<#assign isReg = (isReg!false)>
<#assign isMdf = (isMdf!false)>

<@layout.layout toolbar=toolbar!"">

<#--include::script modules-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/tinymce/tinymce.bundle.js"></script>
<script type="text/javascript" src="/lib/prism/js/prism.js"></script>
<script type="text/javascript" src="/js/common/commons-tinymce.js?${releaseDate!}"></script>
<#include "/view/domain/board/post/_board_post_scripts.ftlh">

<script>
    const Page = (function() {
        return {
            /**
             * Page 객체 초기화
             */
            init: function() {
                /* initialize form. */
                BoardPost.initForm();
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
        <!--begin:Form-->
        <form name="postRegForm" id="postRegForm" class="form" method="post" enctype="multipart/form-data">
            <input type="hidden" name="postNo" value="${post.postNo!}">
            <input type="hidden" name="boardCd" id="boardCd" value="<#if isMdf!false>${post.boardCd!}<#else>${boardCd!}</#if>">
            <input type="hidden" name="atchFileNo" value="${post.atchFileNo!}">
            <input type="hidden" name="hitCnt" value="${post.hitCnt!0?string.number}">
            <#--begin::Row-->
            <div class="row d-flex mb-8">
                <div class="col-12">
                    <label class="d-flex align-items-center mb-2" for="title">
                        <span class="text-gray-700 fs-6 fw-bolder">제목</span>
                        <span class="text-gray-500 fs-9">(최대 50자)</span>
                    </label>
                </div>
                <div class="col-lg-2">
                    <select name="ctgrCd" id="ctgrCd" class="form-select form-select-solid"
                            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="글분류를 선택합니다.">
                        <option value="">--글분류--</option>
                        <#list POST_CTGR_CD![] as ctgr>
                            <option value="${ctgr.dtlCd!}" id="${ctgr.dtlCd!}"<#if ctgr.dtlCd == (post.ctgrCd)!> selected="selected"</#if>>[${ctgr.dtlCdNm!}]</option>
                        </#list>
                    </select>
                </div>
                <div class="col-lg-10">
                    <input type="text" name="title" id="title" class="form-control required"
                           value="${post.title!}" maxlength="100" />
                    <span id="title_validate_span"></span>
                </div>
            </div>
            <div class="row <#if isMdf!false>mb-2</#if>">
                <div class="col-lg-1 col-3 d-flex align-items-center">
                    <label class="text-gray-700 fs-6 fw-bolder cursor-help"
                           data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                           title="해당 글은 페이지에 상관없이&#10;항상 목록의 상단에 노출됩니다.">
                        상단 고정
                        <i class="bi bi-question-circle"></i>
                    </label>
                </div>
                <div class="col-lg-2 col-9 d-flex align-items-center">
                    <@component.checkbox post "fxdYn" "고정//해제", "blue//gray" "defaultOff" />
                </div>
                <div class="col-lg-1 col-3 d-flex align-items-center">
                    <label for="mdfable" class="text-gray-700 fs-6 fw-bolder cursor-help"
                          data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                           title="해당 글을 수정할 수 있는 권한의&#10;범위를 설정합니다.&#10;(기본 : 등록자)">
                        수정 권한
                        <i class="bi bi-question-circle"></i>
                    </label>
                </div>
                <div class="col-lg-2 col-9 d-flex align-items-center">
                    <select name="mdfable" id="mdfable" class="form-select form-select-solid">
                        <#list MDFABLE_CD![] as mdfable>
                            <option value="${mdfable.dtlCd!}" id="${mdfable.dtlCd!}"<#if (post.mdfable!"REGSTR") == (mdfable.dtlCd)> selected="selected"</#if>>[${mdfable.dtlCdNm!}]</option>
                        </#list>
                    </select>
                </div>
            </div>
            <#if isMdf!false>
                <div class="row">
                    <div class="col-lg-1 col-3 d-flex align-items-center">
                        <label class="text-gray-700 fs-6 fw-bolder cursor-help"
                               data-bs-toggle="tooltip" data-bs-placement="top" title="미설정시 수정 일자가&#10;기존 일자로 유지됩니다.&#10;(기본 : ON)">
                            수정일자 변경
                            <i class="bi bi-question-circle"></i>
                        </label>
                    </div>
                    <div class="col-lg-2 col-9 d-flex align-items-center">
                        <@component.checkbox post "managt.managtDtUpdtYn" "변경//미변경", "blue//gray", "defaultOn" />
                    </div>
                </div>
            </#if>

            <div class="separator my-8"></div>

            <div class="d-flex flex-column mb-8">
                <!--begin::Label-->
                <label class="mb-2" for="tinymce_cn">
                    <span class="text-gray-700 fs-6 fw-bolder">내용</span>
                    <span class="text-gray-500 fs-9">&nbsp;</span>
                </label>
                <!--end::Label-->
                <!--begin::Form control-->
                <div class="mb-3">
                    <textarea name="cn" id="tinymce_cn" class="form-control required" rows="10" placeholder="내용을 입력하세요.">${post.cn!}</textarea>
                </div>
                <div id="tinymce_cn_validate_span"></div>
                <!--end::Form control-->
            </div>

            <#--페이지:: 첨부파일 추가추가 영역-->
            <#include "/view/global/_common/file/_atch_file_page_reg_area.ftlh">

            <!-- #include:: component:: 파일시스템 참조 -->

            <!-- #include:: component:: NOTION 참조 ID -->

            <!-- #include:: component:: 태그 태그 -->
            <#include "/view/global/_common/_clsf/tag/_tag_page_reg_area.ftlh">

            <div class="separator my-8"></div>

            <#--페이지:: 잔디 알림 발송 영역-->
            <@component.jandi_reg_area 1 "" />
        </form>
    </div>
    <#--begin::Card footer-->
    <div class="card-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-between">
            <#--페이지:: 버튼: 미리보기-->
            <@component.post_btn_preview_pop "BoardPost.preview();" />
            <div>
                <#--페이지:: 버튼: 저장하기-->
                <@component.post_btn_submit "BoardPost.submit();" />
                <#--페이지:: 버튼: 목록으로 가기-->
                <@component.post_btn_toList "BoardPost.list();" />
            </div>
        </div>
    </div>
</div>

<#--include:: component:: tinymce 이미지 추가 영역 template-->
<#include "/view/global/_tinymce_img.ftlh">

</@layout.layout>