<#--
 * 저널 결산(jrnl_sumry) > 태그 Modal
 * @included_in : jrnl_sumry_dtl.ftlh
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="jrnl_sumry_reg" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header "저널 결산 저장"  />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <#--begin::Form-->
        <form name="jrnlSumryRegForm" id="jrnlSumryRegForm" class="form" enctype="multipart/form-data">
            <div id="jrnl_sumry_reg_modal_header_div">
                <#--Handlebars template goes in here-->
            </div>
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-12">
                    <label class="d-flex align-items-center mb-2" for="tinymce_jrnlSumryCn">
                        <span class="text-gray-700 fs-6 fw-bolder">내용</span>
                    </label>
                </div>
                <div class="col-12">
                    <textarea name="cn" id="tinymce_jrnlSumryCn" class="form-control form-control-solid h-100px required"></textarea>
                    <div id="tinymce_jrnlSumryCn_validate_span"></div>
                </div>
            </div>
            <div id="jrnl_sumry_reg_div">
                <#--Handlebars template goes in here-->
            </div>
        </form>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 저장하기-->
            <@component.modal_btn_save "dF.JrnlSumry.submit();" />
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close_safe "" />
        </div>
    </div>
</@modal.layout>

<#--include:: Handlebars template-->
<#include "/view/domain/jrnl/sumry/_modal/_jrnl_sumry_reg_modal_header_template.hbs">
<#include "/view/domain/jrnl/sumry/_modal/_jrnl_sumry_reg_modal_template.hbs">