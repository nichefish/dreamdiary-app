<#--
 * 게시판 > 태그 전체 목록 Modal
 * 변경(tag 모듈 Vue 전환, Sub-phase B):
 *   - HBS 기반 렌더링 제거. Vue 컴포넌트(TagListModal) 가 모달 본문을 담당한다.
 *   - 기존 `_tag_list_modal_template.hbs` include 불필요.
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = springMacroRequestContext.getMessage("tag") />
<@modal.layout id="tag_list" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header modal_title />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <div id="tag_list_modal_app"></div>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <div class="d-flex justify-content-end">
            <@component.modal_btn_close />
        </div>
    </div>
</@modal.layout>

<#--begin::JSON injection-->
<script type="application/json" id="tag_list_modal_labels">{"titleListAll":"${springMacroRequestContext.getMessage('view.tag.list-all')?js_string}","tooltipContentList":"${springMacroRequestContext.getMessage('view.tag.content-list')?js_string}"}</script>

<script type="module" src="/vue/feature/attachable/tag/TagListModal.js?${releaseDate!}"></script>