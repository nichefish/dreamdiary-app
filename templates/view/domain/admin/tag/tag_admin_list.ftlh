<#--
 * 태그 관리 > 목록 화면
 -->

<#import "/layout/layout_with_aside.ftlh" as layout >

<#--toolbar::버튼 추가-->
<#assign toolbar>
    <#include "/view/domain/admin/tag/_tag_list_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

<style>
    input:placeholder-shown {
        font-style: italic;
    }
</style>

<#--include::aside-->
<#include "/view/domain/admin/tag/_tag_aside_base.ftlh">

<!-- 컨텐츠 타입 헤더 -->
<div class="card post">
    <div class="d-flex align-items-center">
        <div class="fs-5 fw-bold me-5">
            태그 컨텐츠 :
        </div>
        <div id="jrnl_tag_ctgr_div" data-ref-content-type="${refContentType!}">
            <#list contentTypeList![] as contentType>
                <#assign isChecked = refContentType! == contentType.key! />
                <div class="btn btn-sm btn-outlined <#if isChecked>btn-light-primary<#else>btn-light-secondary</#if> text-dark align-middler"
                     onclick="cF.ui.reloadWithParam('refContentType', '${contentType.key!}');">
                <span class="d-flex align-items-center">
                    <#if contentType.icon??>
                        <i class="bi bi-${contentType.icon!} text-dark"></i>
                    </#if>
                    <span class="ms-2">
                        ${contentType.desc!}
                    </span>
                    <#if isChecked>
                        <i class="bi bi-check text-dark"></i>
                    </#if>
                </span>
                </div>
            </#list>
        </div>
    </div>
    <#if !refContentType?? || refContentType == "">
        <div class="ms-20 mt-5 text-noti">
            ※ 컨텐츠 타입을 선택해 주세요.
        </div>
    </#if>
</div>

<!-- 태그 카테고리 헤더 -->
<#include "/view/domain/admin/tag/_tag_ctgr_header.ftlh">

<!-- 태그 목록 -->
<div class="card post">
    <#--begin::Card body-->
    <div class="card-body">
        <div id="tag_list_div">
            <!--Handlebars Template goes in here-->
        </div>
    </div>
    <#--begin::Card footer-->
    <div class="card-footer">
        <#--pagination::begin-->
        <#include "/view/global/_pagination.ftlh">
        <#--pagination::end-->
    </div>
</div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" id="refContentType" name="refContentType" value="${refContentType}" />
</form>

<#--include::vendor script modules-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/draggable/draggable.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/draggable.js?${releaseDate!}"></script>
<#--include::modals-->
<#include "/view/domain/admin/tag/_modal/_tag_admin_dtl_modal.ftlh">
<#include "/view/domain/admin/tag/_modal/_tag_prop_reg_modal.ftlh">
<!--include::templates-->
<#include "/view/domain/admin/tag/_tag_list_template.hbs">
<#include "/view/domain/admin/tag/_tag_ctgr_template.hbs">
<#--include::script modules-->
<script type="text/javascript" src="/js/view/domain/admin/tag/tag_admin_module.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/domain/admin/tag/tag_admin_aside_module.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/domain/admin/tag/tag_admin_property_module.js?${releaseDate!}"></script>
<!--include::page scripts-->
<script type="text/javascript" src="/js/view/domain/admin/tag/tag_admin_list.js?${releaseDate!}"></script>

</@layout.layout>