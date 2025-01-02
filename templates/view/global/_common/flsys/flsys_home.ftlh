<#--
 * 파일시스템(flsys) > 홈
 -->

<#import "/layout/layout_with_aside.ftlh" as layout >

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

                Flsys.flsysListAjax(Flsys.currPath);
            },
        }
    })();
    document.addEventListener("DOMContentLoaded", function() {
        Page.init();
        Flsys.init("page");
    });
</script>

<!--begin::Card-->
<div class="row g-5 gx-xl-10 mb-5 mb-xl-10">
    <div class="col-6 mb-md-5 mb-xl-10">
        <div class="card post">
            <!--begin::Card header 검색+헤더 UI -->
            <#include "/view/global/_common//flsys/_flsys_home_header.ftlh">

            <#--begin::Tab-->
            <ul class="nav nav-tabs nav-tabs-line ps-5 my-5">
                <li class="nav-item">
                    <a class="nav-link px-6 active" data-bs-toggle="tab" href="#kt_tab_pane_1" onclick="Flsys.activateTab(this); Flsys.flsysListAjax('${HOME_FLSYS!}');">
                        <span class="nav-icon">
                            <i class="bi bi-sort-down-alt"></i>
                        </span>
                        <span class="nav-text ps-1 fw-bold">FLSYS_HOME</span>
                    </a>
                </li>
            </ul>
            <!--end: Tab-->
            <div class="d-flex align-items-center my-4">
                <i class="bi bi-folder2-open text-primary"></i>
                <div class="text-muted fw-bold ms-2" id="flsys_home_header_path">
                    <!-- absolutePath goes in here -->
                </div>
                <span class="badge badge-light-primary cursor-pointer ms-6 px-2"
                      onclick="Flsys.flsysRefreshAjax();"
                      data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="현재 디렉토리 내용을&#10;새로고침합니다.">
                    <i class="bi bi-arrow-clockwise"></i>
                </span>
            </div>

            <div id="flsys_list_div">
                <#--Handlebars template goes in here-->
            </div>
            <!--end::Table-->
        </div>
    </div>
    <!--end::Card-->

    <div class="col-6 mb-md-5 mb-xl-10">
        <div class="card post">

        </div>
    </div>
</div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="logSysNo" id="logSysNo" />
</form>

<#include "/view/global/_common//flsys/_flsys_list_template.hbs">
<#include "/view/global/_common//flsys/_flsys_file_vod_modal.ftlh">
<#include "/view/global/_common//flsys/_flsys_home_header_path_template.hbs">
<#include "/view/global/_common//flsys/_flsys_meta_reg_modal.ftlh">
<#include "/view/global/_common//flsys/_flsys_meta_dtl_modal.ftlh">
<#include "/view/global/_common/_clsf/tag/_modal/_tag_dtl_modal.ftlh">
<#include "/view/global/_common/_clsf/tag/_tag_dtl_post_list_template.hbs">
<#include "/view/global/_common/_clsf/tag/_modal/_tag_list_modal.ftlh">

<#--include:: Dream - Aside-->
<#include "/view/global/_common//flsys/_flsys_aside_base.ftlh">

</@layout.layout>