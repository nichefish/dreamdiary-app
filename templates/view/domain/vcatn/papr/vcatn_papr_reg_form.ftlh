<#--
 * 휴가계획서(vcatn_papr) > 등록 화면
 -->

<#import "/layout/layout_default.ftlh" as layout >

<#assign isReg = (formMode == "regist")!false>
<#assign isMdf = (formMode == "modify")!false>

<@layout.layout toolbar=toolbar!"">

<#--begin::Row-->
<div class="row">
    <div class="col-12 d-flex justify-content-end align-items-center mt-4">
        <button type="button" class="btn btn-sm btn-secondary btn-active-primary mx-1"
                onclick="$('#vcatn_papr_rule_modal').modal('show');"
                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="휴가규칙 팝업을 호출합니다.">
            <i class="fas fa-info-circle"></i>휴가규칙
        </button>
        <#include "/view/domain/vcatn/papr/_modal/_vcatn_papr_rule_modal.ftlh">
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
        <form name="vcatnPaprRegForm" id="vcatnPaprRegForm" class="form" data-mode="${formMode!}" data-init-length="${(post.schdulList![])?size}" method="post" enctype="multipart/form-data">
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
            <#include "/view/extension/file/_atch_file_page_reg_area.ftlh">

            <#--페이지:: 잔디 알림 발송 영역-->
            <@component.jandi_reg_area 1 "VCATN" />
        </form>
    </div>
    <#--begin::Card footer-->
    <div class="card-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--페이지:: 버튼: 저장하기-->
            <@component.post_btn_submit "dF.VcatnPapr.submit();" />
            <#--페이지:: 버튼: 목록으로 가기-->
            <@component.post_btn_toList "dF.VcatnPapr.list();" />
        </div>
    </div>
</div>
<!--end::post-->

<#--include:: component:: 첨부파일 추가 영역 template-->
<#include "/view/global/_tinymce_img.ftlh">
<#--include::script modules-->
<script type="text/javascript" src="/lib/prism/js/prism.js"></script>

<#--include::vcatn_papr_module-->
<script type="text/javascript" src="/js/view/domain/vactn/papr/vactn_papr_module.js?${releaseDate!}"></script>

<#--include::vcatn_schdul_module-->
<#include "/view/domain/vcatn/schdul/_modal/_vcatn_schdul_reg_modal_template.hbs">
<script type="text/javascript" src="/js/view/domain/vactn/schdul/vactn_schdul_module.js?${releaseDate!}"></script>

<!--include::page scripts-->
<script type="text/javascript" src="/js/view/domain/vactn/papr/vactn_papr_reg_form.js?${releaseDate!}"></script>

</@layout.layout>