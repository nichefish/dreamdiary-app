<#--
 -- 게시판 > 목록 툴바 Header
 -- @included_in : board_post_list
 -->

<#if tagList?? && tagList?has_content>
    <hr class="my-6 text-muted" />
    <div class="d-flex align-items-center mb-6 ms-4">
        <div class="col-1 ms-4 me-6 text-center fs-6">
            <b class="pe-3">태그 : </b>
            <button type="button" class="btn btn-sm btn-secondary btn-outline btn-active-secondary w-30px h-30px p-0"
                    onclick="dF.Tag.resetFilter();"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="태그 필터링을 리셋합니다.">
                <i class="bi bi-arrow-counterclockwise fs-5 pe-0"></i>
            </button>
        </div>
        <div class="col-10">
            <#list tagList![] as tag>
                <#--검색에서 선택된 태그인지 여부 체크-->
                <#list searchParam.tags![] as tagNo>
                    <#assign ifSelected = tag.tagNo == tagNo>
                    <#if ifSelected>
                        <#assign isTagSelected = true>
                        <#break> <#--이 줄은 작동하지 않지만 isTagSelected를 true로 설정 -->
                    </#if>
                </#list>
                <#if isTagSelected!false>
                    <i class="bi bi-check2-circle text-primary fw-bolder fs-2 blink-fast pe-0 cursor-help"
                       data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="현재 활성화된 필터입니다."></i>
                </#if>
                <span class="py-2 me-3 cursor-pointer opacity-hover"
                      onclick="dF.Tag.filter('${tag.tagNo!}');"
                      data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="해당 태그로 게시물 목록을&#10;필터링합니다.">
                    <span class="${tag.tagClass!}">
                        <#if tag.ctgr != ""><span class="fs-7 text-noti">[${tag.ctgr!}]</span></#if>
                        <span class="em_${tag.tagNm!} <#if isTagSelected!false>fw-bolder text-underline-dotted</#if>">
                            ${tag.tagNm!}
                        </span>
                    </span>
                    <span class="fs-9 text-noti fw-normal" style="margin-left:-0.25em;">${tag.contentSize!0}</span>
                </span>
                <#assign isTagSelected = false>
            </#list>
        </div>
    </div>
</#if>
