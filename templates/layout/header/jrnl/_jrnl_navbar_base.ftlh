<!--
 * 레이아웃 > 헤더 > 네비게이션 바 (base)
 * @included_in : layout/header/_header_base.ftlh
 -->

<#--begin::Navbar-->
<div class="app-navbar d-md-none d-flex flex-shrink-0 ms-5" id="jrnl_navbar">
    <#--begin::row-->
    <div class="d-flex-between align-items-center gap-4">
        <div class="col">
            <div class="d-flex">
                <div class="d-flex align-items-center">
                    <i id="left" class="bi bi-caret-left fs-2 cursor-pointer"
                       data-bs-toggle="tooltip" data-bs-placement="left" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.jrnl.aside-prev'/>"></i>
                </div>
                <select name="yy" id="yy" class="form-select" aria-label="Select example"
                        onchange="dF.JrnlDayAside.changeYyAtMobile();">
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
            <div class="d-flex">
                <select name="mnth" id="mnth" class="form-select" aria-label="Select example"
                        onchange="dF.JrnlDayAside.changeMnthAtMobile();">
                    <option value="">--</option>
                    <#list 1..12 as mnth>
                        <option value="${mnth}">${mnth}</option>
                    </#list>
                    <option value="99"><@spring.message 'mmth-all'/></option>
                </select>
                <div class="d-flex align-items-center ms-2">
                    <i id="right" class="bi bi-caret-right fs-2 cursor-pointer"
                       data-bs-toggle="tooltip" data-bs-placement="left" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.jrnl.aside-next'/>"></i>
                </div>
            </div>
        </div>
    </div>
</div>
<#--end::Navbar-->