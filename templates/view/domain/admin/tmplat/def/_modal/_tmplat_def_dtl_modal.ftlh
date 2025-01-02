<#--
 * 템플릿 정의(tmplat) > 상세 Modal
 * @included_in : notice_list
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="tmplat_def_dtl" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header "템플릿 정의" />
    <div class="modal-body modal-mbl-body my-5">
        <div class="table-responsive-sm">
            <div id="tmplat_def_dtl_div">
                <#--Handlebars template goes in here-->
            </div>
        </div>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close />
        </div>
    </div>
</@modal.layout>

<#--include:: Handlebars template-->
<#include "/view/domain/admin/tmplat/def/_modal/_tmplat_def_dtl_modal_template.hbs">