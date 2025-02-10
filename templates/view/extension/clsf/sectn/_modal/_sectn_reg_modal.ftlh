<#--
 * 단락(section) 목록 Modal
 * @included_in : 단락 기능이 있는 페이지들
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="sectn_reg" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header "단락 저장"  />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <form name="sectnRegForm" id="sectnRegForm" enctype="multipart/form-data">
            <div id="sectn_reg_header_div">
                <#--Handlebars template goes in here-->
            </div>
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-12">
                    <label class="d-flex align-items-center mb-2" for="tinymce_jrnlDreamCn">
                        <span class="text-gray-700 fs-6 fw-bolder">내용</span>
                    </label>
                </div>
                <div class="col-12">
                    <textarea name="cn" id="tinymce_sectnCn" class="form-control form-control-solid h-100px required"></textarea>
                    <div id="tinymce_sectnCn_validate_span"></div>
                </div>
            </div>
            <div class="row" id="sectn_reg_div">
                <#--handlebars template goes in here-->
            </div>
        </form>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 저장하기-->
            <@component.modal_btn_save "dF.Sectn.submit();" />
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close_safe "" />
        </div>
    </div>
</@modal.layout>

<#--include:: Handlebars template-->
<#include "_sectn_reg_modal_template.hbs">