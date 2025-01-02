<#--
 * 일정 > 전체 일정 > 상세 조회 Modal
 * @included_in : schdul_cal
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="schdul_dtl" size="lg">
    <#--begin::Modal Header-->
    <div class="modal-header border-0">
        <h2 class="d-flex-center fw-bolder">
            <i class="ki-duotone ki-calendar-8 fs-1 text-muted me-5">
                <span class="path1"></span>
                <span class="path2"></span>
                <span class="path3"></span>
                <span class="path4"></span>
                <span class="path5"></span>
                <span class="path6"></span>
            </i>
            일정 정보
        </h2>
        <!--begin::Edit-->
        <div class="float-end">
            <#--begin::Close-->
            <div class="btn btn-icon btn-sm btn-color-gray-500 btn-active-icon-primary"
                 data-bs-toggle="tooltip" title="Hide Event" data-bs-dismiss="modal">
                <#--begin::Svg Icon | path: icons/duotune/arrows/arr061.svg-->
                <span class="svg-icon svg-icon-1">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none">
                        <rect opacity="0.5" x="6" y="17.3137" width="16" height="2" rx="1" transform="rotate(-45 6 17.3137)" fill="black"/>
                        <rect x="7.41422" y="6" width="16" height="2" rx="1" transform="rotate(45 7.41422 6)" fill="black"/>
                    </svg>
                </span>
                <#--end::Svg Icon-->
            </div>
            <#--end::Close-->
        </div>
    </div>
    <#--begin::Modal Body-->
    <div id="schdul_dtl_div" class="modal-body pt-0 pb-20 px-lg-17">
        <#--handlebars template goes in here-->
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end gap-2">
            <#--모달:: 버튼: 수정 모달 호출-->
            <@component.modal_btn_modify "Schdul.mdfModal(Schdul.key);" />
            <#--모달:: 버튼: 삭제-->
            <@component.modal_btn_delete "Schdul.delAjax(Schdul.key);" />
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close />
        </div>
    </div>
</@modal.layout>

<#--include:: Handlebars template-->
<#include "/view/domain/schdul/_modal/_schdul_dtl_modal_template.hbs">