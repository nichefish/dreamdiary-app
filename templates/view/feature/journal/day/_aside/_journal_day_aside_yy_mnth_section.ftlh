<#--
 * journal_day: aside date + pin section
 * @included_in : _journal_day_aside_base.ftlh
 *
 * 변경 후: 년·월·Week 래핑·Pinpoint 마크업은 Vue(<code>JournalDayAsideYyMnthApp</code>)가 렌더한다.
 * 연도 옵션은 서버와 동일하게 FTL에서 <code>window.__journalAsideYyMnthBootstrap</code> 에만 적재한다(UI 텍스트는 Vue에서 <code>Message.get</code>).
 -->

<script>
(function() {
    <#assign currYy = DateUtils.getCurrYy()?number />
    <#assign startYy = 2010 />
    var years = [];
    <#list currYy..startYy as year>
    years.push({ value: "${year}", label: "<#if year == 2010>~</#if>${year?js_string}" });
    </#list>
    window.__journalAsideYyMnthBootstrap = window.__journalAsideYyMnthBootstrap || { yyOptions: years };
})();
</script>
<#-- display:contents — 상위 gap-4 flex 열에서 래퍼가 레이아웃 간격을 바꾸지 않도록(FTL 한 단계 더 있던 구조와 동등). -->
<div id="journal_day_aside_yy_mnth_mount" style="display:contents"></div>
<script type="module" src="/vue/feature/journal/day/JournalDayAsideYyMnthApp.js?${releaseDate!}"></script>
