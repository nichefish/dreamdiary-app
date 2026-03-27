<#--
 * jrnl_day: aside dream filter section
 * @included_in : _jrnl_day_aside_diary_filter.ftlh
 -->

<div class="d-flex flex-column gap-2 mb-2 px-2">
    <div class="d-flex align-items-center justify-content-between"
         data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.jrnl.aside-dream-toggle'/>">
        <label for="toggleDreams" class="text-muted cursor-help mb-0">DREAMS</label>
        <input type="checkbox" id="toggleDreams" class="form-check-input cursor-pointer m-0" onclick="dF.JrnlDay.toggleParam();" checked>
    </div>
    <div class="d-flex flex-column ps-3 gap-1">
        <div class="d-flex align-items-center justify-content-between">
            <label for="dreamFilterKeyword" class="text-muted mb-0">- DREAM KEYWORDS</label>
        </div>
        <div class="d-flex gap-1">
            <input type="text" name="dreamFilterKeyword" id="dreamFilterKeyword" class="form-control form-control-sm"
                   value="" placeholder="<@spring.message 'txt.jrnl.day.filter.dream.placeholder'/>" maxlength="200"/>
            <button type="button" class="btn btn-sm btn-outline btn-light-primary px-3" disabled>
                <i class="bi bi-funnel pe-0"></i>
            </button>
        </div>
    </div>
</div>
