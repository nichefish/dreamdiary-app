<#--
 * tag profile modal
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = "태그 프로필 설정" />
<@modal.layout id="tag_profile" size="lg">
    <@component.modal_header modal_title />
    <div class="modal-body modal-mbl-body my-5">
        <div class="row" id="tag_profile_div">
            <form id="tagProfileForm" method="post">
                <input type="hidden" name="id" value="">
                <input type="hidden" name="categoryProfileId" value="">
                <input type="hidden" name="tagId" value="">
                <input type="hidden" name="tagCategoryId" value="">
                <input type="hidden" name="contentType" value="">

                <div class="d-flex align-items-center gap-2 mb-5">
                    <span class="badge badge-light-primary" id="tagProfileContentTypeLabel"></span>
                    <span class="fs-7 text-noti d-none" id="tagProfileCtgrBadge"></span>
                    <span class="fs-6 fw-bold text-primary"><span>#</span><span id="tagProfileTagNm"></span></span>
                </div>

                <div class="row">
                    <div class="col-6 mb-3" id="tagCategoryTextClassCol">
                        <label for="tagCategoryTextClassCd" class="form-label fw-bold">
                            <span id="tagCategoryColorPreview"></span> <@spring.message 'txt.attachable.tag.profile.category-tag-color'/>
                        </label>
                        <select id="tagCategoryTextClassCd" name="categoryTextClassCd" class="form-select form-select-solid">
                            <#list TEXT_CLASS_CD![] as item>
                                <option value="${item.code!}" data-text-class="${item.description!}" class="${item.description!}">
                                    ${item.codeName!}
                                </option>
                            </#list>
                        </select>
                        <div class="fs-8 text-muted mt-1 fw-bold d-none" id="tagCategoryNoGuide"><@spring.message 'txt.attachable.tag.profile.no-category-guide'/></div>
                    </div>

                    <div class="col-6 mb-4">
                        <label for="tagTextClassCd" class="form-label fw-bold"><span id="tagColorPreview"></span> <@spring.message 'txt.attachable.tag.profile.individual-tag-color'/></label>
                        <select id="tagTextClassCd" name="textClassCd" class="form-select form-select-solid">
                            <option value="" data-text-class="" class="" id="tagTextClassDefaultOption"></option>
                            <#list TEXT_CLASS_CD![] as item>
                                <option value="${item.code!}" data-text-class="${item.description!}" class="${item.description!}">
                                    ${item.codeName!}
                                </option>
                            </#list>
                        </select>
                    </div>
                </div>

                <label for="tagProfileCn" class="form-label fw-bold"><@spring.message 'txt.attachable.tag.profile.profile'/></label>
                <textarea id="tagProfileCn" name="content" class="form-control" rows="10"
                          placeholder="<@spring.message 'txt.attachable.tag.profile.profile-placeholder'/>"></textarea>
            </form>
        </div>
    </div>
    <div class="modal-footer">
        <div class="d-flex justify-content-between w-100">
            <button type="button" class="btn btn-sm btn-light-danger d-none" id="tag_profile_del_btn"
                    onclick="this.setAttribute('data-journal-day-action','tag-delete-profile'); dF.JournalDayRuntimeService.handleLegacyActionClick(event);"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message 'bs.tooltip.del'/>">
                <i class="bi bi-trash"></i>삭제
            </button>
            <div class="d-flex justify-content-end ms-auto">
                <@component.modal_btn_save "this.setAttribute('data-journal-day-action','tag-submit-profile'); dF.JournalDayRuntimeService.handleLegacyActionClick(event);" />
                <@component.modal_btn_close />
            </div>
        </div>
    </div>
</@modal.layout>
<div id="journal_day_tag_profile_app" class="d-none"></div>
