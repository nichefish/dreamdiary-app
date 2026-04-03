<#--
 * jrnl_day: aside date + pin section
 * @included_in : _jrnl_day_aside_base.ftlh
 -->

<#--begin::row-->
<div class="d-flex-between gap-4">
    <div class="col">
        <span class="text-gray-900 fs-h6 fw-bold d-inline-block ms-6 mb-2"><@spring.message 'txt.yy'/></span>
        <div class="d-flex">
            <div class="d-flex align-items-center me-2">
                <i id="left" class="bi bi-caret-left fs-2 cursor-pointer"
                   data-bs-toggle="tooltip" data-bs-placement="left" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.jrnl.aside-prev-mnth'/>"></i>
            </div>
            <select name="yy" id="yy" class="form-select" aria-label="Select example"
                    onchange="dF.JrnlDayAside.changeYy();">
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
                onclick="dF.JrnlDayAside.todayMonth();"
                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.jrnl.aside-today'/>">
            TODAY
            <i class="bi bi-box-arrow-in-up-right"></i>
        </button>
        <div class="d-flex">
            <select name="mnth" id="mnth" class="form-select" aria-label="Select example"
                    onchange="dF.JrnlDayAside.changeMnth();">
                <option value="">--</option>
                <#list 1..12 as mnth>
                    <option value="${mnth}">${mnth}</option>
                </#list>
            </select>
            <div class="d-flex align-items-center ms-2">
                <i id="right" class="bi bi-caret-right fs-2 cursor-pointer"
                   data-bs-toggle="tooltip" data-bs-placement="left" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.jrnl.aside-next-mnth'/>"></i>
            </div>
        </div>
    </div>
</div>
<div class="mt-0">
    <div class="d-flex-between align-items-center pe-3">
    <span class="text-gray-900 fs-h6 fw-bold d-inline-block ms-6 mb-2">Week</span>
    <button type="button" class="btn btn-sm btn-outline btn-light-info blink-slow ms-6 ps-2 pe-1 py-0"
            onclick="dF.JrnlDayAside.todayWeek();"
            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.jrnl.aside-today'/>">
        TODAY
        <i class="bi bi-box-arrow-in-up-right"></i>
    </button>
    </div>
    <div class="d-flex align-items-center justify-items-between gap-2">
        <div class="d-flex align-items-center me-2 cursor-pointer"
             onclick="dF.JrnlDayAside.leftWeek();"
             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.jrnl.aside-prev-week'/>">
            <i class="bi bi-caret-left fs-2"></i>
        </div>
        <div class="btn btn-sm btn-light-primary flex-grow-1 py-1">
            <span id="jrnlAsideWeekRange" class="fw-semibold">----</span>
        </div>
        <div class="d-flex align-items-center me-2 cursor-pointer"
             onclick="dF.JrnlDayAside.rightWeek();"
             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.jrnl.aside-next-week'/>">
            <i class="bi bi-caret-right fs-2"></i>
        </div>
    </div>
</div>
<#--begin::row-->
<div class="mt-0">
    <div class="text-gray-900 fs-h6 fw-bold d-inline-block ms-6 mb-2">Pinpoint</div>

    <div class="d-flex align-items-center justify-content-center px-8 mb-4 justify-content-between gap-1">
        <button type="button" class="btn btn-sm btn-outline btn-light-primary px-2 pt-1"
                onclick="dF.JrnlDayAside.pinpoint();"
                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.jrnl.aside-pin'/>">
            <i class="bi bi-bookmarks pe-0"></i>
        </button>
        <span class="mx-2">|</span>
        <span id="jrnl_aside_pinText" class="px-1">
            <span id="pinnedYy" class="fs-6 text-muted text-underline-dotted">----</span>
            <span> / </span>
            <span id="pinnedMnth" class="fs-6 text-muted text-underline-dotted">--</span>
            <i class="bi bi-pin-map fs-7"></i>
        </span>
        <span class="mx-2">|</span>
        <button type="button" class="btn btn-sm btn-outline btn-light-primary px-2 pt-1"
                onclick="dF.JrnlDayAside.turnback();"
                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.jrnl.aside-turnback'/>">
            <i class="bi bi-reply-all pe-0"></i>
        </button>
    </div>
</div>
