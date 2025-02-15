<#--
 * 게시판 > 내선번호 관련 Modal (슬기로운 회사생활)
 * @included_in : board_post_list
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<#assign modal_title = "내선번호" />
<@modal.layout id="extn_phone" size="xl">
    <#--begin::Modal Header-->
    <@component.modal_header modal_title />
    <#--begin::Modal Body-->
    <div class="modal-body modal-mbl-body my-5">
        <div class="table-responsive-sm">
            <#--begin::Table-->
            <table class="table table-row-bordered table-row-gray-300 gy-2 align-middle fs-small">
                <#--begin::Table body-->
                <tbody>
                    <tr class="pt-10px">
                        <td>내선번호</td>
                        <td><u>번호 뒤 네자리</u> (전화기 및 전화선에 표시되어 있음)</td>
                    </tr>
                    <tr class="pt-10px">
                        <td>전화 돌려주기</td>
                        <td><u>플래시</u>(*밑에 있음) 누르고 <u>내선번호</u> 누름</td>
                    </tr>
                    <tr class="pt-10px">
                        <td>전화 당겨받기</td>
                        <td>수화기 들고 <u>**</u></td>
                    </tr>
                    <tr class="pt-10px">
                        <td>참고: 건물 내에 국번이 같은 곳은 내선번호만 누르면 연결됨</td>
                        <td></td>
                    </tr>
                </tbody>
            </table>
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