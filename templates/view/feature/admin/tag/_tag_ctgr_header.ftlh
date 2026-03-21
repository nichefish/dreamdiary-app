<#--
 * 태그 관리 > 카테고리 헤더
 * @included in: tag_list.ftlh
 -->

<div class="card post">
    <div id="tag_ctgr_div">
        <div class="btn btn-sm btn-outlined btn-light-secondary text-muted ctgr active"
             onclick="dF.Tag.ctgrFilterAll(this);">
            전체
            <i class="bi bi-check ctgr"></i>
        </div>

        <div class="btn btn-sm btn-outlined btn-light-secondary text-muted ctgr defaultCtgr active"
             onclick="dF.Tag.ctgrFilter('defaultCtgr');">
            카테고리 없음
            <i class="bi bi-check ctgr defaultCtgr"></i>
        </div>
        <#list tagCtgrList![] as ctgr>
            <div class="btn btn-sm btn-outlined btn-light-secondary text-muted ctgr ${ctgr} active"
                 onclick="dF.Tag.ctgrFilter('${ctgr}');">
                ${ctgr}
                <i class="bi bi-check ctgr ${ctgr}"></i>
            </div>
        </#list>
    </div>
</div>