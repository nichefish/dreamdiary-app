<#--
 * 저널 결산(journal_annual) : ASIDE (우측 사이드바)
 * @included_in : journal_annual_page.ftlh
 *
 * 변경(A-5-β-2):
 *   - card-body#journal_aside 내부 컨텐츠를 Vue(JournalAnnualAsidePanelApp) 로 흡수한다.
 *   - SSR 데이터(yyOptions / mnthOptions / labels) 는 window.__journalAnnualAsideBootstrap 으로 주입한다.
 *   - dummy 영역(꿈 키워드 검색, Crated select) 과 dead 버튼(Default / View more, href="#") 은 기존 마크업과 동일하게 보존한다.
 *   - 외부 wrapper(#kt_app_aside / #kt_app_aside_wrapper) 와 card-header(Filters + 검색 아이콘 a href="#") 는
 *     KTDrawer/KTScroll 훅 보존 및 변경 범위 최소화를 위해 SSR 으로 그대로 둔다.
 *   - card-body id="journal_aside" 는 외부 의존성(annual 컨텍스트에서 외부 참조 0 검증 후) 마운트 컨테이너로 보존한다.
 -->

<#assign yyLabel><@spring.message 'txt.yy'/></#assign>
<#assign mnthLabel><@spring.message 'txt.mnth'/></#assign>
<#assign allYearsLabel><@spring.message 'txt.journal.annual.all-years'/></#assign>
<#assign allMonthsLabel><@spring.message 'txt.journal.annual.all-months'/></#assign>
<#assign yearSuffixLabel><@spring.message 'txt.date.suffix.year-after-number'/></#assign>
<#assign currYy = DateUtils.getCurrYy()?number />
<#assign startYy = 2010 />

<#--begin::aside-->
<div id="kt_app_aside" class="app-aside flex-column mt-7"
     data-kt-drawer="true"
     data-kt-drawer-name="app-aside"
     data-kt-drawer-activate="{default: true, lg: false}"
     data-kt-drawer-overlay="true"
     data-kt-drawer-width="auto"
     data-kt-drawer-direction="end"
     data-kt-drawer-toggle="#kt_app_aside_toggle">

    <#--begin::Aside wrapper-->
    <div id="kt_app_aside_wrapper" class="hover-scroll-y px-5 mx-5 my-5"
         data-kt-scroll="true"
         data-kt-scroll-activate="true"
         data-kt-scroll-height="auto"
         data-kt-scroll-dependencies="#kt_app_header"
         data-kt-scroll-wrappers="#kt_app_aside"
         data-kt-scroll-offset="5px"
         data-kt-scroll-save-state="true">

        <#--begin::Aside-->
        <div class="card card-reset card-p-0">
            <#--begin::Card header-->
            <div class="card-header min-h-auto mb-5">
                <h3 class="card-title text-gray-900 fw-bold fs-3">Filters</h3>
                <#--begin::Toolbar-->
                <div class="card-toolbar">
                    <a href="#" class="btn btn-sm btn-icon btn-color-gray-500 btn-light">
                        <i class="ki-duotone ki-magnifier fs-3">
                            <span class="path1"></span>
                            <span class="path2"></span>
                        </i>
                    </a>
                </div>
            </div>
            <!--end::Header-->
            <#--begin::Body-->
            <div class="card-body" id="journal_aside">
                <#-- 변경(A-5-β-2): card-body 내부를 Vue 마운트 루트로 교체. 자식 마크업은 JournalAnnualAsidePanelApp 가 렌더한다. -->
                <div id="journal_annual_aside_panel_mount" style="display:contents"></div>
                <!--end::Body-->
            </div>
        </div>
        <!--end::Aside-->
    </div>
    <!--end::Aside wrapper-->
</div>
<!--end::aside-->

<#-- 변경(A-5-β-2): SSR 데이터(yyOptions / mnthOptions / labels) 부트스트랩.
     yy 옵션은 currYy ~ 2010 역순 + 2010 의 경우 "~" 접두사 + 연 접미사(@spring.message) 적용으로 기존 표시와 동등. -->
<script>
(function() {
    var yyOptions = [];
    <#list currYy..startYy as year>
    yyOptions.push({
        value: "${year}",
        label: "<#if year == 2010>~</#if>${year}${yearSuffixLabel?js_string}"
    });
    </#list>
    var mnthOptions = [];
    for (var m = 1; m <= 12; m++) mnthOptions.push({ value: String(m), label: String(m) });
    window.__journalAnnualAsideBootstrap = window.__journalAnnualAsideBootstrap || {
        yyOptions: yyOptions,
        mnthOptions: mnthOptions,
        labels: {
            yy: "${yyLabel?js_string}",
            mnth: "${mnthLabel?js_string}",
            allYears: "${allYearsLabel?js_string}",
            allMonths: "${allMonthsLabel?js_string}"
        }
    };
})();
</script>
<#-- 변경(A-4-β): classic journal_annual_aside_module.js 제거 — journalAnnualAsideService ES module 단일 수렴. -->
<script type="module" src="/vue/feature/journal/annual/services/journalAnnualAsideService.js?${releaseDate!}"></script>
<#-- 변경(A-5-β-2): card-body 내부를 흡수한 Vue 앱. service 가 등록한 dF.JournalAnnualAside.yyMnth 본체에 위임한다. -->
<script type="module" src="/vue/feature/journal/annual/JournalAnnualAsidePanelApp.js?${releaseDate!}"></script>
