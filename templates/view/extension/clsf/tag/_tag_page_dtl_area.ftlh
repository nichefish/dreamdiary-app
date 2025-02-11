<#--
 * 레이아웃 > 태그 모듈
 -->

<#if post.tag?? && post.tag.tagStrList?has_content>
    <#--begin::Row-->
    <div class="d-flex flex-stack flex-wrap mb-3 bg-light">
        <div class="col-xl-1 col-form-label fs-6 fw-bold px-5">
            <@spring.message 'tag'/>
        </div>
        <div class="col-xl-11 text-sm-start" id="tagSpan">
            <#list post.tag.list as tag>
                <span class="py-2 me-3 cursor-pointer opacity-hover"
                      onclick="dF.Tag.dtlModal('${tag.refTagNo}');"
                      data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                      title="<@spring.message 'view.tag.content-list'/>">
                    <span class="${tag.tagClass!}">
                        <#if tag.ctgr != ""><span class="fs-7 text-noti">[${tag.ctgr!}]</span></#if>
                        #<span class="fs-6 border-bottom text-primary fw-lighter opacity-hover em_${tag.tagNm!}">${tag.tagNm!}</span>
                    </span>
                </span>
            </#list>
        </div>
    </div>
</#if>