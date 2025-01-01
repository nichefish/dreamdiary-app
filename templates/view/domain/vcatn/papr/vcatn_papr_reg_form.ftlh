<#--
 * 휴가계획서(vcatn_papr) > 등록 화면
 -->

<#import "/layout/layout_default.ftlh" as layout >

<#assign isReg = (isReg!false)>
<#assign isMdf = (isMdf!false)>

<@layout.layout toolbar=toolbar!"">

<#--include::script modules-->
<script type="text/javascript" src="/lib/prism/js/prism.js"></script>
<#include "/view/domain/vcatn/papr/_vcatn_papr_scripts.ftlh">
<#include "/view/domain/vcatn/schdul/_vcatn_schdul_scripts.ftlh">

<script>
    const Page = (function() {
        return {
            idx: 0,

            /**
             * Page 객체 초기화
             */
            init: function() {
                // 추가추가에 사용할 인덱스 설정
                Page.idx = ${(post.schdulList![])?size};

                /* initialize form. */
                VcatnPapr.initForm();
                // 첨부파일 영역 0개인지 체크
                AtchFile.atchFileListToggle();

                <#if isReg!false>
                    $("#jandiYn").trigger("click");
                </#if>

                <#if isReg!false>
                    // 휴가 추가 폼 기본 뿌리기
                    Page.addVcatnSchdul();
                </#if>
            },

            /**
             * 휴가등록 부분 추가
             */
            addVcatnSchdul: function() {
                const idx = Page.idx++;
                cF.handlebars.append({ "idx": idx }, "vcatn_schdul_reg");
                cF.datepicker.singleDatePicker("#bgnDt" + idx, "yyyy-MM-DD", null, function() {
                    VcatnSchdul.propEndDt(idx);
                    VcatnSchdul.noBefore("#bgnDt"+idx, "#endDt"+idx, idx);
                });
                cF.datepicker.singleDatePicker("#endDt"+idx, "yyyy-MM-DD", null, function() {
                    if ($("#endDt"+idx).val() !== "") VcatnSchdul.noBefore("#bgnDt"+idx, "#endDt"+idx, idx);
                });
            },
            /** 추가항목 영역 삭제 */
            delSpan: function(idx) {
                $("div#itemContainer" + idx).remove();
            }
        }
    })();
    document.addEventListener("DOMContentLoaded", function() {
        Page.init();
    });
</script>

<#--begin::Row-->
<div class="row">
    <div class="col-12 d-flex justify-content-end align-items-center mt-4">
        <button type="button" class="btn btn-sm btn-secondary btn-active-primary mx-1"
                onclick="$('#vcatn_papr_rule_modal').modal('show');"
                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="휴가규칙 팝업을 호출합니다.">
            <i class="fas fa-info-circle"></i>휴가규칙
        </button>
        <#include "/view/domain/vcatn/papr/_vcatn_papr_rule_modal.ftlh">
    </div>
</div>
<!-- class 속성이 덮어씌워지지 않아서 스타일 지정 -->
<div class="card post" style="margin-top:15px !important;">
    <#--begin::Card body-->
    <div class="card-body">
        <!--begin::header-->
        <div class="mb-2">
            <label class="mb-2 text-gray-700 fs-6 fw-bolder">
                휴가 등록
            </label>
            <a href="javascript:void(0);" class="btn btn-light-primary btn-sm me-2 mb-2 m-lg-2 justify-content-end mb-5"
               onclick="Page.addVcatnSchdul();"
               data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="휴가 일정을 추가합니다.">
                <i class="fas fa-plus"></i>추가
            </a>
            <span class="text-noti fs-7 ps-5">※주말 및 공휴일은 자동으로 휴가일자 산정에서 제외됩니다.</span>
        </div>

        <!--begin:Form-->
        <form name="vcatnPaprRegForm" id="vcatnPaprRegForm" class="form" enctype="multipart/form-data">
            <input type="hidden" name="postNo" value="${post.postNo!}">
            <input type="hidden" name="title" id="title" value="${post.title!}"/>
            <input type="hidden" name="atchFileNo" value="${post.atchFileNo!}">
            <input type="hidden" name="hitCnt" value="${post.hitCnt!0?string.number}">
            <input type="hidden" name="cfYn" value="${post.cfYn!"N"}">

            <div class="mb-8">
                <!-- vcatnSchduls -->
                <div id="vcatn_schdul_reg_div" class="mb-8">
                    <!--휴가 목록-->
                    <#include "/view/domain/vcatn/papr/_vcatn_schdul_reg_area.ftlh">
                    <!-- vcatnSchdulList goes in here -->
                </div>
            </div>

            <#--페이지:: 첨부파일 추가추가 영역-->
            <#include "/view/global/_common/file/_atch_file_page_reg_area.ftlh">

            <#--페이지:: 잔디 알림 발송 영역-->
            <@component.jandi_reg_area 1 "VCATN" />
        </form>
    </div>
    <#--begin::Card footer-->
    <div class="card-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--페이지:: 버튼: 저장하기-->
            <@component.post_btn_submit "VcatnPapr.submit();" />
            <#--페이지:: 버튼: 목록으로 가기-->
            <@component.post_btn_toList "VcatnPapr.list();" />
        </div>
    </div>
</div>
<!--end::post-->

<#include "/view/domain/vcatn/papr/_vcatn_schdul_reg_template.hbs">

<#--include:: component:: 첨부파일 추가 영역 template-->
<#include "/view/global/_tinymce_img.ftlh">

</@layout.layout>