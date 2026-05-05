<#--
 * 공지사항(notice) > 등록 화면
 * tinyMCE 정책 때문에 문서 최상단에 <!DOCTYPE html> 추가
 -->

<!DOCTYPE html>
<#import "/layout/layout_default.ftlh" as layout >

<#assign isReg = (formMode == "regist")!false>
<#assign isMdf = (formMode == "modify")!false>

<@layout.layout toolbar=toolbar!"">

<div class="card post">
    <#--begin::Card body-->
    <div class="card-body">
        <!--begin:Form-->
        <form name="noticeRegForm" id="noticeRegForm" class="form" data-mode="${formMode!}" method="post" enctype="multipart/form-data">
            <input type="hidden" name="id" value="${post.id!}">
            <input type="hidden" name="contentType" value="NOTICE">
            <input type="hidden" name="fileGroupId" value="${post.fileGroupId!}">
            <input type="hidden" name="hitCnt" value="${post.hitCnt!0?string.number}">

            <#--begin::Row-->
            <div class="row d-flex mb-8">
                <div class="col-12">
                    <label class="d-flex align-items-center mb-2" for="title">
                        <span class="text-gray-700 fs-6 fw-bolder"><@spring.message 'txt.title'/></span>
                        <span class="text-gray-500 fs-9">(최대 50자)</span>
                    </label>
                </div>
                <div class="col-lg-2">
                    <select name="ctgrCd" id="ctgrCd" class="form-select form-select-solid">
                        <option value="">--글분류--</option>
                        <#list NOTICE_CTGR_CD![] as ctgr>
                            <option value="${ctgr.code!}" <#if ctgr.code == (post.ctgrCd)!>selected="selected"</#if>>[${ctgr.codeName!}]</option>
                        </#list>
                    </select>
                </div>
                <div class="col-lg-10">
                    <input type="text" name="title" id="title" class="form-control required"
                           value="${post.title!}" maxlength="100" />
                    <span id="title_validate_span"></span>
                </div>
            </div>
            <#--begin::Row-->
            <div class="row <#if isMdf!false>mb-2</#if>">
                <div class="col-lg-1 col-3 d-flex align-items-center">
                    <label class="text-gray-700 fs-6 fw-bolder cursor-help"
                           data-bs-toggle="tooltip" data-bs-placement="top" title="해당 글은 굵은 글씨, 아이콘 및 눈에 띄는 색상으로 강조됩니다.">
                        중요 여부
                        <i class="bi bi-question-circle"></i>
                    </label>
                </div>
                <div class="col-lg-2 col-9 d-flex align-items-center">
                    <@component.checkbox post "imprtcYn" "중요//해당없음", "red//gray" "defaultOff" />
                </div>
                <div class="col-lg-1 col-3 d-flex align-items-center">
                    <label class="text-gray-700 fs-6 fw-bolder cursor-help"
                           data-bs-toggle="tooltip" data-bs-placement="top" title="해당 글은 페이지에 상관없이&#10;항상 목록의 상단에 노출됩니다.">
                        상단 고정
                        <i class="bi bi-question-circle"></i>
                    </label>
                </div>
                <div class="col-lg-2 col-9 d-flex align-items-center">
                    <@component.checkbox post "fxdYn" "고정//해제", "blue//gray" "defaultOff" />
                </div>
                <div class="col-lg-1 col-3 d-flex align-items-center">
                    <label class="text-gray-700 fs-6 fw-bolder cursor-help"
                           data-bs-toggle="tooltip" data-bs-placement="top" title="해당 글은 사이트 접속시&#10;화면에 팝업으로 표출됩니다.">
                        팝업공지 여부
                        <i class="bi bi-question-circle"></i>
                    </label>
                </div>
                <div class="col-lg-2 col-9 d-flex align-items-center">
                    <@component.checkbox post "popupYn" "노출//해당없음", "blue//gray" "defaultOff" />
                </div>
                <div class="col-lg-1 col-3 d-flex align-items-center">
                    <label for="mdfable" class="text-gray-700 fs-6 fw-bolder cursor-help"
                           data-bs-toggle="tooltip" data-bs-placement="top"
                           title="해당 글을 수정할 수 있는 권한의&#10;범위를 설정합니다.&#10;(기본 : 등록자)">
                        수정 권한
                        <i class="bi bi-question-circle"></i>
                    </label>
                </div>
                <div class="col-lg-2 col-9 d-flex align-items-center">
                    <select name="mdfable" id="mdfable" class="form-select form-select-solid">
                        <#list MDFABLE_CD![] as mdfable>
                            <option value="${mdfable.code!}" id="${mdfable.code!}" <#if (post.mdfable!"REGSTR") == (mdfable.code)>selected="selected"</#if>>[${mdfable.codeName!}]</option>
                        </#list>
                    </select>
                </div>
            </div>
            <#if isMdf!false>
                <#--begin::Row-->
                <div class="row">
                    <div class="col-lg-1 col-3 d-flex align-items-center">
                        <label class="text-gray-700 fs-6 fw-bolder cursor-help"
                               data-bs-toggle="tooltip" data-bs-placement="top" title="미체크시 수정 일자가&#10;기존 일자로 유지됩니다.&#10;(기본 : ON)">
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
                <label class="mb-2" for="tinymce_content">
                    <span class="text-gray-700 fs-6 fw-bolder"><@spring.message 'txt.content'/></span>
                    <span class="text-gray-500 fs-9">&nbsp;</span>
                </label>
                <!--end::Label-->
                <!--begin::Form control-->
                <div class="mb-3">
                    <textarea name="content" id="tinymce_content" class="form-control required" rows="10" placeholder="내용을 입력하세요.">${post.content!}</textarea>
                </div>
                <div id="tinymce_content_validate_span"></div>
                <!--end::Form control-->
            </div>

            <#--begin::meta-->
            <div class="mb-0">
                <#--페이지:: 첨부파일 추가추가 영역-->
                <#include "/view/feature/file/_file_page_reg_area.ftlh">

                <!-- #include:: component:: 파일시스템 참조 -->

                <!-- #include:: component:: 태그 태그 -->
                <#include "/view/feature/attachable/tag/_tag_page_reg_area.ftlh">
            </div>

            <div class="separator my-8"></div>

            <#--페이지:: 잔디 알림 발송 영역-->
            <@component.jandi_reg_area 1 "NOTICE" />
        </form>
    </div>

    <#--begin::Card footer-->
    <div class="card-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-between">
            <#--페이지:: 버튼: 미리보기-->
            <@component.post_btn_preview_pop "window.dispatchEvent(new CustomEvent('notice:form-preview'));" />
            <div>
                <#--페이지:: 버튼: 저장하기-->
                <@component.post_btn_submit "window.dispatchEvent(new CustomEvent('notice:form-submit'));" />
                <#--페이지:: 버튼: 목록으로 가기-->
                <@component.post_btn_toList "window.dispatchEvent(new CustomEvent('notice:form-list'));" />
            </div>
        </div>
    </div>
</div>
<div id="notice_regist_form_app" class="d-none"></div>

<#--include::tinymce-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/tinymce/tinymce.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/tinymce.js?${releaseDate!}"></script>
<script type="text/javascript" src="/lib/prism/js/prism.js"></script>
<#include "/view/global/_tinymce_img.ftlh">

<#--include::comment_module-->
<#include "/view/feature/attachable/comment/_modal/_comment_reg_modal.ftlh">
<#-- 변경(A-10): `_comment_list_partial` include 제거 — `{{>}}` 호출 0건, JS 문자열 참조 0건. id `comment_list_partial` 자체는
     호출자 없음(별개의 id `comment_list` 모달은 `_comment_list_modal.ftlh` 가 보유, 본 정리 범위 밖). attachable 공용 partial 의 dead 정리
     일관성을 위해 board/notice 도메인의 dead include 도 함께 제거. -->
<script type="text/javascript" src="/js/view/feature/attachable/comment/comment_modal_module.js?${releaseDate!}"></script>

<#--include::notice_module-->
<script type="module" src="/vue/feature/board/notice/NoticeRegistFormApp.js?${releaseDate!}"></script>

</@layout.layout>
