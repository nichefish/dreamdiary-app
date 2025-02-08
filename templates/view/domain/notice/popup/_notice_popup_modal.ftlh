<#--
 * 공지사항 팝업 Modal
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="notice_popup" size="xl">
    <#--begin::Modal Header-->
    <div class="modal-header">
        <h5 class="modal-title" id="lgnNotifyModalLongTitle">[공지사항 팝업]</h5>
        <div class="btn btn-sm btn-icon btn-active-light-primary ms-2"
             onclick="dF.NoticePopup.closeBtnFunc();" aria-label="Close">
            <i class="fas fa-times"></i>
        </div>
    </div>
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <div id="notice_popup_div">
            <#--handlebars template goes in here-->
        </div>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer flex-end">
        <button type="reset" class="btn btn-light me-3" aria-label="Close"
                onclick="dF.NoticePopup.closeBtnFunc();">
            닫기
        </button>
    </div>
</@modal.layout>

<#--include:: Handlebars template-->
<#include "_notice_popup_modal_template.hbs">