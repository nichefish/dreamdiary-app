<#--
 * 템플릿 관리(tmplat) > 등록 Modal
 * @included_in : cl_cd_list
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="tmplat_def_reg" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header "템플릿 정의 저장"  />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <div class="table-responsive-sm">
            <#--begin::Form-->
            <form name="tmplatDefRegForm" id="tmplatDefRegForm">
                <div id="tmplat_def_reg_div">
                    <#--Handlebars template goes in here-->
                </div>
            </form>
        </div>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 저장하기-->
            <@component.modal_btn_save "dF.TmplatDef.submit();" />
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close_safe "" />
        </div>
    </div>
</@modal.layout>

<#--include:: Handlebars template-->
<#include "/view/domain/admin/tmplat/def/_modal/_tmplat_def_reg_modal_template.hbs">