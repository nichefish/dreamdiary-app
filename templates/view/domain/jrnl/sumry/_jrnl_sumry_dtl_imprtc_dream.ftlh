<#--
 * 저널 결산(jrnl_sumry) : 페이지
 -->

<hr class="my-6 text-muted" />
<div class="d-flex align-items-center mb-6 ms-4">
    <div class="col-1 ms-4 me-6 text-center fs-6">
        <b><@spring.message 'dream.tag'/> : </b>
    </div>
    <div class="col" id="jrnl_dream_tag_list_div">
        <#list dreamTagList![] as tag>
            <span class="py-2 me-3 cursor-pointer opacity-hover"
                  onclick="dF.JrnlDreamTag.dtlModal(${tag.tagNo!}, '${tag.tagNm!}');"
                  data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="해당 태그로 게시물 목록을&#10;필터링합니다.">
                <span class="${tag.tagClass!}">
                    <#if tag.ctgr?? && tag.ctgr != ""><span class="fs-7 text-noti">[${tag.ctgr!}]</span></#if>
                    ${tag.tagNm!}
                </span>
                <span class="fs-9 text-noti fw-normal" style="margin-left:-0.25em;">${tag.contentSize!}</span>
            </span>
        </#list>
    </div>
    <div class="col-1 ms-4 me-6 text-center fs-6 w-50px">
        <!-- -->
    </div>
</div>

<!--begin::Card-->
<div class="card post">
    <#--begin::Card body-->
    <div class="card-body">
        <div class="row p-5">
            <label class="d-flex align-items-center my-2" for="title">
                <span class="text-gray-700 fs-6 ps-5 fw-bolder">
                    중요 꿈
                    <i class="bi bi-moon-stars fs-4 pe-1"></i>
                </span>
            </label>
            <#list imprtcDreamList![] as dream>
                <div class="row jrnl-item my-2">
                    <div class="col-1 py-3 d-flex-between border-2 border-gray-300 border-end ps-10 me-4 w-130px h-75">
                        <div>
                            <i class="bi bi-calendar3"></i> ${dream.stdrdDt!}
                        </div>
                    </div>
                    <div class="col py-3 jrnl-sumry-cn bg-secondary">
                        <#if (dream.nhtmrYn! == 'Y')><div class="ctgr-span ctgr-imprtc w-auto">!<@spring.message 'dream.nhtmr'/></div></#if>
                        <#if (dream.hallucYn! == 'Y')><div class="ctgr-span ctgr-gray w-auto">!<@spring.message 'dream.halluc'/></div></#if>
                        <#if (dream.title?? && dream.title! != "")><div class="d-inline-block fw-bold">${dream.title!}</div></#if>
                        <div class="cn">
                            ${(dream.markdownCn!"-")?no_esc}
                        </div>

                        <#if dream.tag.list??>
                            <div class="ms-2 mt-3">
                                <#list dream.tag.list as tag>
                                    <span class="text-muted cursor-pointer pe-1" onclick="dF.Tag.dtlModal(${tag.refTagNo!});"
                                          data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                                          title="<@spring.message 'view.tag.content-list'/>">
                                        <#if tag.ctgr?? && tag.ctgr != ""><span class="fs-7 text-noti">[${tag.ctgr!}]</span></#if>
                                        #<span class="border-bottom text-primary fw-lighter opacity-hover">${tag.tagNm!}</span>
                                    </span>
                                </#list>
                            </div>
                        </#if>
                    </div>
                    <div class="col-1 py-3 d-flex-between w-50px gap-1">
                        <button type="button" class="btn btn-sm btn-light-primary btn-outlined ms-2 py-1 px-2 cursor-pointer"
                                onclick="dF.Comment.modal.regModal(${dream.postNo!}, 'JRNL_DREAM');"
                                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'comment'/> <@spring.message 'bs.tooltip.modal.reg'/>">
                            <i class="bi bi-chat-left-dots p-0"></i>
                        </button>
                    </div>
                    <div class="col-1 ms-4 pe-0 border-2 border-gray-300 border-end h-75 w-10px">&nbsp;</div>
                    <div class="col-1 py-3 d-flex-between w-50px gap-1">
                        <button type="button" class="btn btn-sm btn-light-primary btn-outlined ms-2 py-1 px-2 cursor-pointer"
                                onclick="dF.JrnlDream.mdfModal(${dream.postNo!}, '${dream.stdrdDt!}');"
                                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'jrnl.dream'/> <@spring.message 'bs.tooltip.modal.mdf'/>">
                            <i class="bi bi-pencil-square p-0"></i>
                        </button>
                        <button type="button" class="btn btn-sm btn-light-danger btn-outlined ms-2 py-1 px-2 cursor-pointer"
                                onclick="dF.JrnlDream.delAjax(${dream.postNo!});"
                                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'jrnl.dream'/> <@spring.message 'bs.tooltip.del'/>">
                            <i class="bi bi-trash p-0"></i>
                        </button>
                    </div>
                </div>
                <#if dream.comment.list??>
                    <div class="row py-2">
                        <div class="col-1 w-130px">
                            &nbsp;
                        </div>
                        <div class="col ms-10 text-noti">
                            <#list dream.comment.list![] as comment>
                                <div class="row div-textarea-smp pb-1">
                                    ${(comment.cn!)?no_esc}
                                </div>
                            </#list>
                        </div>
                    </div>
                </#if>
            </#list>
        </div>
    </div>
</div>