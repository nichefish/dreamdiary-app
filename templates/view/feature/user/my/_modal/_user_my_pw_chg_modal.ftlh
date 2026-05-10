<#--
 * 내 정보(user_my) > 내 비밀번호 변경 Modal
 * @included_in : user_my_page
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = "비밀번호 변경" />
<@modal.layout id="user_my_pw_chg" size="md">
    <#--begin::Modal Header-->
    <@component.modal_header_dark modal_title />
    <#--begin::Modal Body-->
    <div class="modal-body">
        <#--begin::Form-->
        <form name="myPwChgForm" id="myPwChgForm" class="form">
            <div id="user_my_pw_chg_div">
                <#-- Vue(UserMyPageApp)가 비밀번호 변경 폼을 렌더링 -->
            </div>
        </form>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <div id="user_my_pw_chg_footer_div">
            <#-- Vue(UserMyPageApp)가 비밀번호 변경 footer 버튼을 렌더링 -->
        </div>
    </div>
</@modal.layout>

<#--include:: Handlebars template-->
<#-- Vue(UserMyPageApp)가 기존 _user_my_pw_chg_modal_template.hbs를 대체 -->
