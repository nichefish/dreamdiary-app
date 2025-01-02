<#--
 * 저널 일자(jrnl_day) : 페이지
 -->

<#import "/layout/layout_with_aside.ftlh" as layout >

<#--toolbar::버튼 추가-->
<#assign toolbar>
    <#include "/view/domain/jrnl/day/_jrnl_day_page_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

<link rel="stylesheet" type="text/css" href="/css/jrnl.css?${releaseDate!}" />

<script>
    const Page = (function() {
        return {
            /**
             * Page 객체 초기화
             */
            init: function() {
                Page.setYyMnthCookie();
                JrnlDayAside.init();
                // 목록 조회
                JrnlDay.yyMnthListAjax();
                // 태그 조회
                JrnlDayTag.listAjax();
                JrnlDiaryTag.listAjax();
                JrnlDreamTag.listAjax();
                // 일기/꿈 키워드 검색에 엔터키 처리
                cF.util.enterKey("#diaryKeyword", JrnlDiary.keywordListAjax);
                cF.util.enterKey("#dreamKeyword", JrnlDream.keywordListAjax);
            },

            /**
             * 페이지에 조회년월 쿠키 세팅
             */
            setYyMnthCookie: function() {
                // 년도 쿠키 설정
                const yyCookie = $.cookie("jrnl_yy");
                if (yyCookie !== undefined) document.querySelector("#jrnl_aside #yy").value = yyCookie;
                // 월 쿠키 설정
                const mnthCookie = $.cookie("jrnl_mnth");
                if (yyCookie !== undefined) document.querySelector("#jrnl_aside #mnth").value = mnthCookie;
                // 정렬 쿠키 설정
                const sortCookie = $.cookie("jrnl_day_sort");
                if (sortCookie !== undefined) document.querySelector("#jrnl_aside #sort").value = sortCookie;
                // 아무 쿠키도 없을경우 전체 데이터 로딩을 막기 위해 올해 년도 세팅
                if (yyCookie === undefined && mnthCookie === undefined) {
                    $("#jrnl_aside #yy").val(cF.date.getCurrYyStr());
                }
            },
            /**
             * 달력 화면으로 이동
             */
            calPage: function() {
                cF.util.blockUIReplace("${Url.JRNL_DAY_CAL!}");
            }
        }
    })();
    document.addEventListener("DOMContentLoaded", function() {
        Page.init();
        // 모달 댓글 모듈 수동 init (refreshFunc 주입(
        Comment.modal.init({
            "refreshFunc": JrnlDay.yyMnthListAjax
        });
    });
</script>

<style>
</style>

<!--begin::Card-->
<div class="card post">
    <#--begin::Card header-->
    <div class="card-header min-h-auto mb-5">
        <#--begin::Tab-->
        <ul class="nav nav-tabs nav-tabs-line ps-5 mt-5">
            <li class="nav-item">
                <a class="nav-link px-6 active" data-bs-toggle="tab" href="#kt_tab_pane_1">
                    <span class="nav-icon">
                        <i class="bi bi-people"></i>
                    </span>
                    <span class="nav-text">목록 VIEW</span>
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link px-6" data-bs-toggle="tab" href="#kt_tab_pane_2"
                   onclick="Page.calPage();">
                    <span class="nav-icon">
                        <i class="bi bi-sort-down-alt"></i>
                    </span>
                    <span class="nav-text">달력 VIEW</span>
                </a>
            </li>
        </ul>
    </div>

    <!-- 태그 헤더 :: 파일 분리 -->
    <#include "/view/domain/jrnl/day/tag/_jrnl_day_tag_header.ftlh">

    <#--begin::Card body-->
    <div class="card-body">
        <#--begin::Table body-->
        <div id="jrnl_day_list_div">
            <#--handlebars template goes in here-->
        </div>
        <div id="jrnl_diary_list_div">
            <#--handlebars template goes in here-->
        </div>
        <div id="jrnl_dream_list_div">
            <#--handlebars template goes in here-->
        </div>
    </div>
</div>
<!--end::Card-->

<#--include:: Aside-->
<#include "/view/domain/jrnl/day/_jrnl_day_aside_base.ftlh">

<#--include::modals-->
<#include "/view/domain/jrnl/day/_jrnl_day_list_template.hbs">
<#include "/view/domain/jrnl/day/_jrnl_day_reg_modal.ftlh">
<#include "/view/domain/jrnl/day/tag/_jrnl_day_tag_list_template.hbs">
<#include "/view/domain/jrnl/day/tag/_jrnl_day_tag_dtl_modal.ftlh">

<#include "/view/domain/jrnl/diary/_jrnl_diary_reg_modal.ftlh">
<#include "/view/domain/jrnl/diary/_jrnl_diary_list_template.hbs">
<#include "/view/domain/jrnl/diary/tag/_jrnl_diary_tag_list_template.hbs">
<#include "/view/domain/jrnl/diary/tag/_jrnl_diary_tag_dtl_modal.ftlh">

<#include "/view/domain/jrnl/dream/_jrnl_dream_reg_modal.ftlh">
<#include "/view/domain/jrnl/dream/_jrnl_dream_list_template.hbs">
<#include "/view/domain/jrnl/dream/tag/_jrnl_dream_tag_list_template.hbs">
<#include "/view/domain/jrnl/dream/tag/_jrnl_dream_tag_dtl_modal.ftlh">

<#include "/view/domain/jrnl/tag/_jrnl_tag_list_modal.ftlh">
<#include "/view/global/_common/_clsf/comment/_modal/_comment_reg_modal.ftlh">


<#--include::script modules-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/tinymce/tinymce.bundle.js"></script>
<script type="text/javascript" src="/metronic/assets/plugins/custom/fullcalendar/fullcalendar.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/tinymce.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/common/helper/fullcalendar.js?${releaseDate!}"></script>
<#include "/view/domain/jrnl/day/_jrnl_day_scripts.ftlh">
<#include "/view/domain/jrnl/day/tag/_jrnl_day_tag_scripts.ftlh">
<#include "/view/domain/jrnl/dream/_jrnl_dream_scripts.ftlh">
<#include "/view/domain/jrnl/dream/tag/_jrnl_dream_tag_scripts.ftlh">
<#include "/view/domain/jrnl/diary/_jrnl_diary_scripts.ftlh">
<#include "/view/domain/jrnl/diary/tag/_jrnl_diary_tag_scripts.ftlh">
<#include "/view/domain/jrnl/tag/_jrnl_tag_scripts.ftlh">
<script type="text/javascript" src="/js/view/global/_common/_clsf/comment/comment_modal_module.js?${releaseDate!}"></script>

</@layout.layout>