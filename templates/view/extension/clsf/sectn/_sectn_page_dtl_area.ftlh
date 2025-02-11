<#--
 * 레이아웃 > 단락(section) 모듈
 * @included_in : 단락 기능이 있는 페이지들
 -->

<!--begin::section-->
<div class="accordion accordion-icon-collapse">
    <#list post.sectn.list![] as sectn>
        <!--begin::accordion Item-->
        <div class="row d-flex align-items-center mb-4">
            <!--begin::accordion item header-->
            <div class="accordion-header py-3 d-flex <#if sectn.deprcYn == "Y">collapsed</#if>" data-bs-toggle="collapse" data-bs-target="#kt_accordion_sectn_item_${sectn?index}">
                <!--begin::collapse icon-->
                <span class="accordion-icon">
                    <i class="ki-duotone ki-plus-square fs-3 accordion-icon-off"><span class="path1"></span><span class="path2"></span><span class="path3"></span></i>
                    <i class="ki-duotone ki-minus-square fs-3 accordion-icon-on"><span class="path1"></span><span class="path2"></span></i>
                </span>
                <!--begin::title-->
                <h3 class="fs-4 fw-semibold mb-0 ms-4 <#if sectn.deprcYn == "Y">strikethrough text-muted</#if>">
                    ＜ ${sectn.title!} ＞
                </h3>
            </div>
            <!--begin::accordion item Body-->
            <div id="kt_accordion_sectn_item_${sectn?index}" class="fs-6 d-flex collapse <#if sectn.deprcYn != "Y">show</#if> ps-10">
                <div class="col mx-5 fs-4 fw-normal <#if sectn.deprcYn == "Y">text-muted strikethrough</#if>">
                    ${sectn.markdownCn?no_esc}
                    <#if sectn.comment?? && sectn.comment.cnt gt 0>
                        <div class="row pb-2 fs-4">
                            <#list sectn.comment.list![] as comment>
                                <div class="row ms-10 text-noti my-1 <#if comment?index == 0>mt-3</#if> <#if comment?index == sectn.comment.list?size - 1>mb-3</#if>">
                                    <div class="col div-textarea-smp me-10">- ${comment.markdownCn?no_esc}</div>
                                    <div class="col-1">
                                        <button type="button" class="btn btn-sm btn-light-primary btn-outlined ms-2 py-1 px-2 cursor-pointer"
                                                onclick="dF.Comment.modal.mdfModal(${comment.postNo!});"
                                                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message "comment"/> <@spring.message "bs.tooltip.modal.mdf"/>">
                                            <i class="bi bi-pencil-square p-0"></i>
                                        </button>
                                        <button type="button" class="btn btn-sm btn-light-danger btn-outlined ms-2 py-1 px-2 cursor-pointer"
                                                onclick="dF.Comment.modal.delAjax(${comment.postNo!});"
                                                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message "comment"/> <@spring.message "bs.tooltip.del"/>">
                                            <i class="bi bi-trash p-0"></i>
                                        </button>
                                    </div>
                                </div>
                            </#list>
                        </div>
                    </#if>
                </div>
                <div class="col-1 py-3 jrnl-dream-cn d-flex justify-content-between w-50px gap-1">
                    <button type="button" class="btn btn-light-primary btn-outlined ms-2 py-1 px-3 h-30px cursor-pointer"
                            onclick="dF.Comment.modal.regModal(${sectn.postNo!}, '${sectn.contentType!}');"
                            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message "comment"/> <@spring.message "bs.tooltip.modal.reg"/>">
                        <i class="bi bi-chat-left-dots p-0"></i>
                    </button>
                </div>
            </div>
            <#if sectn?index + 1 < sectn?size>
                <div class="separator separator-dashed border-gray-300 my-6"></div>
            </#if>
        </div>
    </#list>
</div>
<!--end::Accordion-->