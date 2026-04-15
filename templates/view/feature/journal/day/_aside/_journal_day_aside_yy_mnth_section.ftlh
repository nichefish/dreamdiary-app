<#--
 * journal_day: aside date + pin section
 * @included_in : _journal_day_aside_base.ftlh
 -->

<#--begin::row-->
<div class="d-flex-between gap-4">
    <div class="col">
        <span class="text-gray-900 fs-h6 fw-bold d-inline-block ms-6 mb-2"><@spring.message 'txt.yy'/></span>
        <div class="d-flex">
            <div class="d-flex align-items-center me-2">
                <i id="left" class="bi bi-caret-left fs-2 cursor-pointer"
                   data-bs-toggle="tooltip" data-bs-placement="left" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.journal.aside-prev-mnth'/>"></i>
            </div>
            <select name="yy" id="yy" class="form-select" aria-label="Select example"
                    onchange="dF.JournalDayAside.changeYy();">
                <option value="">----</option>
                <#assign currYy = DateUtils.getCurrYy()?number />
                <#assign startYy = 2010 />
                <#list currYy..startYy as year>
                    <option value="${year}"><#if year == 2010>~</#if>${year}</option>
                </#list>
            </select>
        </div>
    </div>
    <div class="col">
        <span class="text-gray-900 fs-h6 fw-bold d-inline-block ms-2 mb-2"><@spring.message 'txt.mnth'/></span>
        <button type="button" class="btn btn-sm btn-outline btn-light-info blink-slow ms-6 ps-2 pe-1 py-0"
                onclick="dF.JournalDayAside.todayMonth();"
                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.journal.aside-today'/>">
            TODAY
            <i class="bi bi-box-arrow-in-up-right"></i>
        </button>
        <div class="d-flex">
            <select name="mnth" id="mnth" class="form-select" aria-label="Select example"
                    onchange="dF.JournalDayAside.changeMnth();">
                <option value="">--</option>
                <#list 1..12 as mnth>
                    <option value="${mnth}">${mnth}</option>
                </#list>
            </select>
            <div class="d-flex align-items-center ms-2">
                <i id="right" class="bi bi-caret-right fs-2 cursor-pointer"
                   data-bs-toggle="tooltip" data-bs-placement="left" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.journal.aside-next-mnth'/>"></i>
            </div>
        </div>
    </div>
</div>
<div class="mt-0">
    <div class="d-flex-between align-items-center pe-3 mb-2">
        <div class="d-flex align-items-center flex-wrap gap-2">
            <span class="text-gray-900 fs-h6 fw-bold d-inline-block ms-6">Week</span>
            <div class="btn btn-sm btn-light-primary py-1 px-3 pe-none fs-8">
                <span id="journalAsideWeekRange" class="fw-semibold">----</span>
            </div>
        </div>
        <button type="button" class="btn btn-sm btn-outline btn-light-info blink-slow ms-6 ps-2 pe-1 py-0"
                onclick="dF.JournalDayAside.todayWeek();"
                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.journal.aside-today'/>">
            TODAY
            <i class="bi bi-box-arrow-in-up-right"></i>
        </button>
    </div>
    <div class="journal-aside-week-nav d-flex align-items-center gap-2">
        <div class="d-flex align-items-center me-2 cursor-pointer"
             onclick="dF.JournalDayAside.leftWeek();"
             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.journal.aside-prev-week'/>">
            <i class="bi bi-caret-left fs-2"></i>
        </div>
        <div id="journalAsideWeekDays" class="journal-aside-week-days flex-grow-1" aria-label="Weekly navigation"></div>
        <div class="d-flex align-items-center me-2 cursor-pointer"
             onclick="dF.JournalDayAside.rightWeek();"
             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.journal.aside-next-week'/>">
            <i class="bi bi-caret-right fs-2"></i>
        </div>
    </div>
</div>
<#--begin::row-->
<div class="mt-0">
    <div class="text-gray-900 fs-h6 fw-bold d-inline-block ms-6 mb-2">Pinpoint</div>

    <div class="d-flex align-items-center justify-content-center px-8 mb-4 justify-content-between gap-1">
        <button type="button" class="btn btn-sm btn-outline btn-light-primary px-2 pt-1"
                onclick="dF.JournalDayAside.pinpoint();"
                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.journal.aside-pin'/>">
            <i class="bi bi-bookmarks pe-0"></i>
        </button>
        <span class="mx-2">|</span>
        <span id="journal_aside_pinText" class="px-1">
            <span id="pinnedYy" class="fs-6 text-muted text-underline-dotted">----</span>
            <span> / </span>
            <span id="pinnedMnth" class="fs-6 text-muted text-underline-dotted">--</span>
            <i class="bi bi-pin-map fs-7"></i>
        </span>
        <span class="mx-2">|</span>
        <button type="button" class="btn btn-sm btn-outline btn-light-primary px-2 pt-1"
                onclick="dF.JournalDayAside.turnback();"
                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.journal.aside-turnback'/>">
            <i class="bi bi-reply-all pe-0"></i>
        </button>
    </div>
</div>
