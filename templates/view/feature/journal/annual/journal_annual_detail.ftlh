<#--
 * 저널 결산(journal_annual) : 페이지
 -->

<#import "/layout/layout_default.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<link rel="stylesheet" type="text/css" href="/css/journal.css?${releaseDate!}" />

<!--begin::Card-->
<div id="journal_annual_detail_div" class="card post">
    <!-- handlebars template goes in here-->
</div>

<#--begin::Card header-->
<div class="card-header min-h-auto mb-7">
    <#--begin::Tab-->
    <ul class="nav nav-tabs nav-tabs-line ps-5 mt-5">
        <li class="nav-item">
            <a class="nav-link px-6 ${(section == 'DIARY')?then('active','')}" data-bs-toggle="tab" href="#kt_tab_pane_1"
               onclick="dF.JournalAnnual.detailViewWithSection('DIARY');">
                <span class="nav-icon pe-1">
                    <i class="bi bi-book"></i>
                </span>
                <span class="nav-text"><@spring.message 'txt.diary.sumry'/></span>
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link px-6 ${(section == 'DREAM')?then('active','')}" data-bs-toggle="tab" href="#kt_tab_pane_2"
               onclick="dF.JournalAnnual.detailViewWithSection('DREAM');">
                <span class="nav-icon pe-1">
                    <i class="bi bi-moon-stars"></i>
                </span>
                <span class="nav-text"><@spring.message 'txt.dream.sumry'/></span>
            </a>
        </li>
    </ul>
</div>

<!-- 태그 헤더 :: 파일 분리 -->
<#include "/view/feature/journal/annual/tag/_journal_annual_detail_tag_header.ftlh">
<hr class="my-6 text-muted" />

<div class="d-flex align-items-end justify-content-end mb-2">
    <div class="mx-3"
         data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="중요 토글">
        <label for="toggleImprtc" class="me-2 text-muted cursor-help">IMPORTANT</label>
        <input type="checkbox" id="toggleImprtc" class="form-check-input cursor-pointer" onclick="dF.JournalAnnual.toggleParam();" checked>
    </div>
    <div class="mx-3"
         data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="참조 토글">
        <label for="toggleRefrnc" class="me-2 text-muted cursor-help">REFERENCE</label>
        <input type="checkbox" id="toggleRefrnc" class="form-check-input cursor-pointer" onclick="dF.JournalAnnual.toggleParam();" checked>
    </div>
</div>

<div class="journal_annual_day_tag_div">
    <!-- handlebars template goes in here -->
</div>

<div class="journal_annual_diary_tag_div">
    <!-- handlebars template goes in here -->
</div>

<div class="journal_annual_dream_tag_div">
    <!-- handlebars template goes in here -->
</div>

<div class="card-post p-10">
    <!-- 중요 일기 -->
    <div id="journal_annual_diary_list_div">
        <!-- handlebars template goes in here -->
    </div>

    <!-- 중요 꿈 -->
    <div id="journal_annual_imprtc_dream_list_div">
        <!-- handlebars template goes in here -->
    </div>
</div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="id" id="id" value="" />
    <input type="hidden" name="yy" id="yy" value="" />
    <input type="hidden" name="mnth" id="mnth" value="99" />
</form>

<#--include::tinymce-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/tinymce/tinymce.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/tinymce.js?${releaseDate!}"></script>
<script type="text/javascript" src="/lib/prism/js/prism.js"></script>
<#include "/view/global/_tinymce_img.ftlh">

<#--include::journal_day_module-->
<#-- 변경(A-7-γ): `_journal_day_stdrd_dt_partial` / `_journal_day_context_btn_partial` include 제거 —
     `_journal_annual_entry_list_template.hbs` 가 유일 호출자였고 본 phase 에서 Vue(`JournalAnnualEntryItem`) 로 흡수. -->
<#-- 변경(A-8): `_journal_day_meta_btn_partial` include 제거 — 전체 코드베이스에서 `{{> journal_day_meta_btn_partial}}`
     호출 0건, JS 문자열 참조도 0건이라 dead partial 로 확정. partial 파일 자체도 삭제. -->

<#--include::journal_diary_module-->
<#-- 변경(A-2): annual_dtl 페이지에서 _journal_entry_reg_btn_partial / _journal_entry_toggle_btn_partial include 제거 — 결산 상세 안에서 호출 0건. -->
<#-- 변경(A-7-γ): `_journal_entry_context_btn_partial` / `_journal_entry_copy_btn_partial` /
     `_journal_entry_content_partial` include 제거 — `_journal_annual_entry_list_template.hbs` 가 유일 호출자였고
     본 phase 에서 Vue(`JournalEntryContextMenu` / `JournalEntryContent` 재사용 + `JournalAnnualEntryItem` 인라인 copy 버튼) 로 흡수. -->
<#-- 변경(E-4-δ-3): journalEntryService 적재는 _journal_entry_reg_modal.ftlh 의 ES module 묶음으로 단일 수렴. -->

<#--include::journal_dream_module-->
<#assign journalEntryRegType = "DREAM">
<#include "/view/feature/journal/entry/_modal/_journal_entry_reg_modal.ftlh">

<script type="module" src="/vue/feature/journal/entry/preloadJournalDayTagService.js?${releaseDate!}"></script>
<#-- journal_entry_tag_module 제거(E-4-γ): journalEntryTagService가 등록 모달에 포함됨. -->

<#--include::tag-->
<#include "/view/feature/journal/tag/_journal_tag_list_modal.ftlh">

<#--include::tags-->
<#include "/view/feature/journal/tag/_journal_tag_list_modal.ftlh">
<#-- 변경(A-9): `_tag_list_partial.hbs` include 제거 — `{{> tag_list_partial}}` 호출 0건(A-5-α 에서
     `_journal_annual_list_template.hbs`/`JournalAnnualListItem`, `JournalAnnualDetailCardApp`, `JournalAnnualReviewRow` 가 Vue 흡수),
     JS/TS 문자열 참조 0건이라 dead partial 로 확정. -->
<#-- 변경(A-7-δ): `tag_list_sized_partial` 는 본 페이지 태그 헤더 전용이었음(Vue 흡수) — sized partial 스크립트 등록 생략. -->

<#--include::comment-->
<#include "/view/feature/attachable/comment/_modal/_comment_reg_modal.ftlh">
<#-- 변경(A-10): `_comment_list_partial` include 제거 — `{{>}}` 호출 0건, JS 문자열 참조 0건. id `comment_list_partial` 자체는
     호출자 없음(별개의 id `comment_list` 모달은 `_comment_list_modal.ftlh` 가 보유, 본 정리 범위 밖). -->
<#-- 변경(A-7-γ): `_comment_reg_btn_partial` include 제거 — `_journal_annual_entry_list_template.hbs` 가 유일 호출자였고
     본 phase 에서 Vue(`JournalAnnualEntryItem` 우열 인라인 댓글 등록 버튼) 로 흡수. -->
<script type="text/javascript" src="/js/view/feature/attachable/related/related_content_module.js?${releaseDate!}"></script>

<#--include::state-->
<script type="text/javascript" src="/js/view/feature/attachable/state/state_module.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/feature/attachable/lifecycle/lifecycle_module.js?${releaseDate!}"></script>

<#--include::journal_annual_modules-->
<#-- 변경(A-5-α): `_journal_annual_context_btn_partial.hbs` include 제거 — 본 partial 의 유일 호출자(`_journal_annual_list_template.hbs`)가 Vue 로 흡수되어 dtl 페이지에서는 dead include. -->
<#-- 변경(A-7-β): `_journal_annual_detail_template.hbs` 및 리뷰 partial 3종(`journal_annual_review_*`) include 제거 —
     상세 카드 본문은 `JournalAnnualDetailCardApp` Vue 가 1:1 재현한다. -->
<#include "/view/feature/journal/annual/_modal/_journal_annual_review_regist_modal.ftlh">
<#-- 변경(A-7-δ): `_journal_annual_entry_tag_list_template.hbs` 제거 — `JournalAnnualEntryTagListApp` Vue 가 태그 헤더 3행을 렌더한다. -->
<#-- 변경(A-7-γ): `_journal_annual_entry_list_template.hbs` include 제거 —
     DIARY/DREAM 엔트리 리스트는 `JournalAnnualEntryListApp` Vue (`JournalAnnualEntryItem` 행 컴포넌트) 가 렌더한다. -->
<#-- 변경(A-4-β): 결산 상세는 결산 등록 모달을 포함하지 않으므로 `dF.JournalAnnual` 표면은 페이지 하단에서 적재한다.
     Review 묶음(ReviewCrud + journalAnnualReviewService + ReviewRegModalApp)은 `_journal_annual_review_regist_modal.ftlh` 가드로 단일 수렴.
     classic journal_annual_module.js / journal_annual_review_module.js 제거. -->
<script type="module" src="/vue/feature/journal/annual/services/journalAnnualCrudService.js?${releaseDate!}"></script>
<script type="module" src="/vue/feature/journal/annual/services/journalAnnualStateService.js?${releaseDate!}"></script>
<script type="module" src="/vue/feature/journal/annual/services/journalAnnualService.js?${releaseDate!}"></script>
<#-- 변경(A-7-β): 상세 카드 Vue — `JournalAnnualDetailPageBoot` 보다 선행 적재하여 `detailAjax` 완료 전 `JournalAnnualDetailVueApp` 브리지가 존재한다. -->
<script type="module" src="/vue/feature/journal/annual/JournalAnnualDetailCardApp.js?${releaseDate!}"></script>
<#-- 변경(A-7-δ): 태그 헤더 Vue — `JournalAnnualDetailPageBoot` 보다 선행 적재하여 `getTagListAjax` 콜백 전 `JournalAnnualEntryTagListVueApp` 브리지가 존재한다. -->
<script type="module" src="/vue/feature/journal/annual/JournalAnnualEntryTagListApp.js?${releaseDate!}"></script>
<#-- 변경(A-7-γ): 엔트리 리스트 Vue — `JournalAnnualDetailPageBoot` 보다 선행 적재하여 `getAnnualDiaryListAjax` /
     `getAnnualDreamListAjax` 콜백 전 `JournalAnnualEntryListVueApp` 브리지가 존재한다. -->
<script type="module" src="/vue/feature/journal/annual/JournalAnnualEntryListApp.js?${releaseDate!}"></script>

<#--include::page scripts-->
<#-- 변경(A-7-α): classic page IIFE `journal_annual_detail.js` 제거 — `JournalAnnualDetailPageBoot` ES module 단일 수렴.
     dF.JournalAnnual / dF.JournalAnnualReview 표면이 위 묶음에서 등록된 뒤 본 ES module 이 동등 부트 시퀀스를 수행한다. -->
<script type="module" src="/vue/feature/journal/annual/JournalAnnualDetailPageBoot.js?${releaseDate!}"></script>

</@layout.layout>
