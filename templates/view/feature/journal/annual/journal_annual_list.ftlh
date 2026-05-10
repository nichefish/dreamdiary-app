<#--
 * 저널 결산(journal_annual) : 페이지
 -->

<#import "/layout/layout_with_aside.ftlh" as layout >

<#--toolbar::버튼 추가-->
<#assign toolbar>
    <#include "/view/feature/journal/annual/_journal_annual_list_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

<link rel="stylesheet" type="text/css" href="/css/journal.css?${releaseDate!}" />

<!--begin::Card-->
<div class="card post">
    <#--begin::Card body-->
    <div class="card-body">
        <div class="d-flex-between fs-5">
            <div class="d-flex fs-5">
                <div class="text-gray-700 d-flex-center me-5">
                    <span class="fw-bold me-2">
                        <@spring.message 'txt.total-dreams'/>
                    </span>
                    <i class="bi bi-moon-stars fs-4 me-2"></i>
                    (<span class="text-info fw-bold mx-1">${totalAnnual.dreamDayCnt!}</span><@spring.message 'txt.dd'/>
                    /
                    <span class="text-info fw-bold mx-1">${totalAnnual.dreamCnt!}</span><@spring.message 'txt.ea'/>)
                </div>
            </div>
        </div>
    </div>
</div>
<#-- 변경(A-5-α): Vue mount root — JournalAnnualListApp 가 카드 리스트(`<JournalAnnualListItem>`)를 렌더한다.
     변경 전: 본 div 안에 `cF.handlebars.template(rsltList, "journal_annual_list")` 가 HBS 카드 마크업을 직접 주입했다. -->
<div id="journal_annual_list_div"></div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="id" id="id" value="" />
</form>

<#--include::aside-->
<#include "/view/feature/journal/annual/_journal_annual_aside_base.ftlh">

<#--include::tinymce-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/tinymce/tinymce.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/tinymce.js?${releaseDate!}"></script>
<script type="text/javascript" src="/lib/prism/js/prism.js"></script>
<#include "/view/global/_tinymce_img.ftlh">

<#--include::journal_annual_regist_modal (A-4-β: dF.JournalAnnual 등록 + RegModalApp ES bundle) -->
<#-- 변경(A-5-α): `_journal_annual_list_template.hbs` / `_journal_annual_context_btn_partial.hbs` include 제거 —
     목록 카드(`JournalAnnualListItem`) + 컨텍스트 메뉴(`JournalAnnualContextMenu`) Vue 컴포넌트로 흡수됨. -->
<#include "/view/feature/journal/annual/_modal/_journal_annual_regist_modal.ftlh">

<#-- 변경(A-2): annual_list 페이지에서 _journal_entry_reg_modal.ftlh include 묶음(<#assign journalEntryRegType = "DREAM"> 포함) 제거 — 결산 목록 화면에 entry 등록/수정 진입점 0건. 동일 묶음은 annual_dtl 페이지에서는 그대로 유지(상세 entry 리스트의 mdf/copy 호출 경로). -->
<#-- 변경(E-4-δ-3) 보존 메모: journalEntryService 적재는 _journal_entry_reg_modal.ftlh 의 ES module 묶음으로 단일 수렴(현 페이지에서는 위 A-2 로 그 묶음이 제거됨 — entry 모듈 미사용 화면). -->

<#-- 변경(A-2) 보존 메모 → A-5-α 갱신: 아래 preloadJournalDayTagService.js 는 `JournalAnnualListItem` 의 태그 클릭이
     `dF.JournalDayTagService.select(...)` 를 호출하기 때문에 본 페이지에서도 필요하다. -->
<script type="module" src="/vue/feature/journal/entry/preloadJournalDayTagService.js?${releaseDate!}"></script>
<#-- journal_entry_tag_module 제거(E-4-γ): journalEntryTagService가 등록 모달에 포함됨. -->

<#--include::tag-->
<#include "/view/feature/journal/tag/_journal_tag_list_modal.ftlh">
<#-- 변경(A-5-α): `_tag_list_partial.hbs` include 제거 — Handlebars partial `tag_list_partial` 의 유일 호출자(_journal_annual_list_template.hbs)가 Vue 로 흡수되어 가시 dead. -->

<#--include::script modules-->
<#-- 변경(A-4-β): `dF.JournalAnnual` 표면 등록 및 결산 등록 모달 Vue 묶음은 `_modal/_journal_annual_regist_modal.ftlh` 의
     journalAnnualRegistVueScriptDone 가드 안에서 단일 적재한다(classic journal_annual_module.js 제거). -->

<!--include::page scripts-->
<#-- 변경(A-5-α): classic page script(`journal_annual_list.js`) 제거 — `JournalAnnualListApp` 가 부트 시점에
     `dF.JournalAnnual.init() + listAjax()` 진입을 수행한다(레거시 Page IIFE 동등 동작). -->
<script type="module" src="/vue/feature/journal/annual/JournalAnnualListApp.js?${releaseDate!}"></script>

</@layout.layout>
