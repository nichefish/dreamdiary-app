<#--
 * journal_day: aside journal entry filter section
 * @included_in : _journal_day_aside_base.ftlh
 -->

<#assign entryFilterTitle = journalEntryFilterTitle!"ENTRIES">
<#assign entryFilterToggleId = journalEntryFilterToggleId!"toggleEntries">
<#assign entryFilterTooltipKey = journalEntryFilterTooltipKey!"">
<#assign entryFilterKeywordId = journalEntryFilterKeywordId!"entryFilterKeyword">
<#assign entryFilterKeywordName = journalEntryFilterKeywordName!"entryFilterKeyword">
<#assign entryFilterKeywordLabel = journalEntryFilterKeywordLabel!"- ENTRY KEYWORDS">
<#assign entryFilterKeywordPlaceholderKey = journalEntryFilterKeywordPlaceholderKey!"">
<#assign entryFilterShowChapterCtgr = journalEntryFilterShowChapterCtgr!false>

<div class="d-flex flex-column gap-2 mb-2 px-2">
    <div class="d-flex align-items-center justify-content-between"
         data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message entryFilterTooltipKey/>">
        <label for="${entryFilterToggleId}" class="text-muted cursor-help mb-0">${entryFilterTitle}</label>
        <input type="checkbox" id="${entryFilterToggleId}" class="form-check-input cursor-pointer m-0" onclick="dF.JournalDay.toggleParam();" checked>
    </div>
    <#if entryFilterShowChapterCtgr>
        <div id="chapterCtgrFilterSection" class="d-flex flex-column ps-3 gap-1">
            <div class="d-flex align-items-center justify-content-between">
                <label for="chapterCtgrFilter" class="text-muted mb-0">- CHAPTER CATEGORIES</label>
                <input type="checkbox" id="toggleChapterCtgr" class="form-check-input cursor-pointer m-0"
                       onclick="dF.JournalDay.toggleChapterCtgr();" checked>
            </div>
            <select id="chapterCtgrFilter" class="form-select form-select-sm w-100"
                    multiple size="4"
                    data-bs-toggle="tooltip" data-bs-placement="top" title="<@spring.message 'bs.tooltip.select.multi'/>"
                    onmousedown="return dF.JournalDay.handleChapterCtgrMouseDown(event);"
                    onchange="dF.JournalDay.changeChapterCtgr();">
                <option value="__ALL__"><@spring.message 'txt.ctgr.all'/></option>
                <#list JOURNAL_CHAPTER_CTGR_CD![] as ctgr>
                    <option value="${ctgr.code!}">[${ctgr.codeName!}]</option>
                </#list>
            </select>
        </div>
    </#if>
    <div class="d-flex flex-column ps-3 gap-1">
        <div class="d-flex align-items-center justify-content-between">
            <label for="${entryFilterKeywordId}" class="text-muted mb-0">${entryFilterKeywordLabel}</label>
        </div>
        <div class="d-flex gap-1">
            <input type="text" name="${entryFilterKeywordName}" id="${entryFilterKeywordId}" class="form-control form-control-sm"
                   value="" placeholder="<@spring.message entryFilterKeywordPlaceholderKey/>" maxlength="200"/>
            <button type="button" class="btn btn-sm btn-outline btn-light-primary px-3" disabled>
                <i class="bi bi-funnel pe-0"></i>
            </button>
        </div>
    </div>
</div>
