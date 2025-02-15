<#--
 * 레이아웃 > 단락(section) 모듈
 * @included_in : 단락 기능이 있는 페이지들
 -->

<#--begin::sectn button-->
<div class="col d-flex justify-content-end">
    <#--헤더:: 버튼: 등록 모달 호출하기-->
    <@component.header_btn_reg_modal "dF.Sectn.regModal(${post.postNo!}, '${post.contentType!}');" />
</div>

<!--begin::section-->
<div id="sectn_list_div" class="accordion accordion-icon-collapse">
    <#if post.sectn?? && post.sectn.hasSectn>
        <div class="separator separator-dashed border-gray-300 my-8"></div>
        <div class="draggable-zone">
            <#list post.sectn.list![] as sectn>
                <!--begin::accordion Item-->
                <div id="${sectn.postNo!}" class="row d-flex align-items-center mb-4 sortable-item draggable">
                    <!--begin::accordion item header-->
                    <div class="accordion-header py-3 d-flex align-items-center gap-2 <#if sectn.deprcYn == "Y">collapsed</#if>" data-bs-toggle="collapse" data-bs-target="#kt_accordion_sectn_item_${sectn?index}">
                        <!--begin::sort handle icon-->
                        <div class="draggable-handle d-flex align-items-center cursor-move"
                             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="드래그하여&#10;항목을 이동합니다.">
                            <i class="ki-duotone ki-abstract-14 fs-2x">
                                <span class="path1"></span>
                                <span class="path2"></span>
                            </i>
                        </div>
                        <!--begin::collapse icon-->
                        <span class="accordion-icon">
                            <i class="ki-duotone ki-plus-square fs-3 accordion-icon-off"><span class="path1"></span><span class="path2"></span><span class="path3"></span></i>
                            <i class="ki-duotone ki-minus-square fs-3 accordion-icon-on"><span class="path1"></span><span class="path2"></span></i>
                        </span>
                        <!--begin::title-->
                        <h3 class="fs-4 fw-semibold mb-1 <#if sectn.deprcYn == "Y">strikethrough text-muted</#if>">
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
                                                        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'comment'/> <@spring.message 'bs.tooltip.modal.mdf'/>">
                                                    <i class="bi bi-pencil-square p-0"></i>
                                                </button>
                                                <button type="button" class="btn btn-sm btn-light-danger btn-outlined ms-2 py-1 px-2 cursor-pointer"
                                                        onclick="dF.Comment.modal.delAjax(${comment.postNo!});"
                                                        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'comment'/> <@spring.message 'bs.tooltip.del'/>">
                                                    <i class="bi bi-trash p-0"></i>
                                                </button>
                                            </div>
                                        </div>
                                    </#list>
                                </div>
                            </#if>
                        </div>
                        <div class="col-1 py-3 w-50px">
                            <button type="button" class="btn btn-light-primary btn-outlined ms-2 py-1 px-3 h-30px cursor-pointer"
                                    onclick="dF.Comment.modal.regModal(${sectn.postNo!}, '${sectn.contentType!}');"
                                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'comment'/> <@spring.message 'bs.tooltip.modal.reg'/>">
                                <i class="bi bi-chat-left-dots p-0"></i>
                            </button>
                        </div>
                        <div class="col-1 py-3 pe-0 me-2 w-10px">
                            <div class="border-2 border-gray-300 border-end h-30px">&nbsp;</div>
                        </div>
                        <div class="col-1 py-3 w-125px">
                            <button type="button" class="btn btn-light-primary btn-outlined ms-2 py-1 px-3 h-30px cursor-pointer"
                                    onclick="dF.Sectn.mdfModal(${sectn.postNo!});"
                                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="단락 <@spring.message 'bs.tooltip.modal.mdf'/>">
                                <i class="bi bi-pencil-square p-0"></i>
                            </button>
                            <button type="button" class="btn btn-light-danger btn-outlined ms-2 py-1 px-3 h-30px cursor-pointer"
                                    onclick="dF.Sectn.delAjax(${sectn.postNo!});"
                                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="단락 <@spring.message 'bs.tooltip.del'/>">
                                <i class="bi bi-trash p-0"></i>
                            </button>
                        </div>
                    </div>
                    <#if sectn?index + 1 < sectn?size>
                        <div class="separator separator-dashed border-gray-300 my-6"></div>
                    </#if>
                </div>
            </#list>
        </div>
    </#if>
</div>
