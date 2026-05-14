<#--
 * 게시판 > 태그 상세 Modal
 * 변경(tag 모듈 Vue 전환, Sub-phase B):
 *   - HBS 기반 렌더링 제거. Vue 컴포넌트(TagDtlModal) 가 모달 본문을 담당한다.
 *   - 기존 `_tag_dtl_modal_template.hbs`, `_tag_dtl_post_list_template.hbs` include 불필요.
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = springMacroRequestContext.getMessage("tag") />
<@modal.layout id="tag_dtl" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header modal_title />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <div class="table-responsive-sm">
            <div id="tag_dtl_modal_app"></div>
        </div>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <div class="d-flex justify-content-end">
            <@component.modal_btn_close />
        </div>
    </div>
</@modal.layout>

<#--begin::JSON injection-->
<script type="application/json" id="tag_dtl_modal_labels">{"postCountPrefix":"${springMacroRequestContext.getMessage('txt.attachable.tag.detail.post-count-prefix')?js_string}","postCountSuffix":"${springMacroRequestContext.getMessage('txt.attachable.tag.detail.post-count-suffix')?js_string}","tooltipOpenAll":"${springMacroRequestContext.getMessage('bs.tooltip.attachable.tag.detail.open-all-tags')?js_string}","btnListAll":"${springMacroRequestContext.getMessage('view.tag.list-all')?js_string}","thTitle":"${springMacroRequestContext.getMessage('txt.attachable.tag.post-list.title')?js_string}","thAuthor":"${springMacroRequestContext.getMessage('txt.attachable.tag.post-list.author')?js_string}","thCreatedAt":"${springMacroRequestContext.getMessage('txt.attachable.tag.post-list.created-at')?js_string}","thHit":"${springMacroRequestContext.getMessage('txt.attachable.tag.post-list.hit')?js_string}","tooltipMovePost":"${springMacroRequestContext.getMessage('bs.tooltip.attachable.tag.post-list.move-post')?js_string}"}</script>

<script type="module" src="/vue/feature/attachable/tag/TagDtlModal.js?${releaseDate!}"></script>