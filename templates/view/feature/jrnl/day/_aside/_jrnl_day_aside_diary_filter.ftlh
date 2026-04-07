<#--
 * jrnl_day: aside diary filter section
 * @included_in : _jrnl_day_aside_base.ftlh
 -->

<div class="d-flex flex-column gap-2 mb-2 px-2">
    <div class="d-flex align-items-center justify-content-between"
         data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.jrnl.aside-diary-toggle'/>">
        <label for="toggleDiaries" class="text-muted cursor-help mb-0">DIARIES</label>
        <input type="checkbox" id="toggleDiaries" class="form-check-input cursor-pointer m-0" onclick="dF.JrnlDay.toggleParam();" checked>
    </div>
    <div id="entryCtgrFilterSection" class="d-flex flex-column ps-3 gap-1">
        <div class="d-flex align-items-center justify-content-between">
            <label for="entryCtgrFilter" class="text-muted mb-0">- ENTRY CATEGORIES</label>
            <input type="checkbox" id="toggleEntryCtgr" class="form-check-input cursor-pointer m-0"
                   onclick="dF.JrnlDay.toggleEntryCtgr();" checked>
        </div>
        <select id="entryCtgrFilter" class="form-select form-select-sm w-100"
                multiple size="4"
                data-bs-toggle="tooltip" data-bs-placement="top" title="<@spring.message 'bs.tooltip.select.multi'/>"
                onmousedown="return dF.JrnlDay.handleEntryCtgrMouseDown(event);"
                onchange="dF.JrnlDay.changeEntryCtgr();">
            <option value="__ALL__"><@spring.message 'txt.ctgr.all'/></option>
            <#list JRNL_ENTRY_CTGR_CD![] as ctgr>
                <option value="${ctgr.dtlCd!}">[${ctgr.dtlCdNm!}]</option>
            </#list>
        </select>
    </div>
    <div class="d-flex flex-column ps-3 gap-1">
        <div class="d-flex align-items-center justify-content-between">
            <label for="diaryFilterKeyword" class="text-muted mb-0">- DIARY KEYWORDS</label>
        </div>
        <div class="d-flex gap-1">
            <input type="text" name="diaryFilterKeyword" id="diaryFilterKeyword" class="form-control form-control-sm"
                   value="" placeholder="<@spring.message 'txt.jrnl.day.filter.diary.placeholder'/>" maxlength="200"/>
            <button type="button" class="btn btn-sm btn-outline btn-light-primary px-3" disabled>
                <i class="bi bi-funnel pe-0"></i>
            </button>
        </div>
    </div>
</div>
