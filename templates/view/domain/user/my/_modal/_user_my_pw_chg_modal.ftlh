<#--
 * 내 정보(user_my) > 내 비밀번호 변경 Modal
 * @included_in : user_my_dtl
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="user_my_pw_chg" size="md">
    <#--begin::Modal Header-->
    <@component.modal_header_dark "비밀번호 변경" />
    <#--begin::Modal Body-->
    <div class="modal-body">
        <#--begin::Form-->
        <form name="myPwChgForm" id="myPwChgForm" class="form">
            <div id="user_my_pw_chg_div">
                <#--handlebars template goes in here-->
            </div>
        </form>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--모달:: 버튼: 저장하기-->
            <@component.modal_btn_save "UserMyPwChg.submit();" />
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close />
        </div>
    </div>
</@modal.layout>

<#--include:: Handlebars template-->
<#include "/view/domain/user/my/_modal/_user_my_pw_chg_modal_template.hbs">