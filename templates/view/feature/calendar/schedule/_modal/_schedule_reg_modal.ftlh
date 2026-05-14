<#--
 * 일정 > 전체 일정 > 일정 등록 Modal
 * @included_in : schedule_cal
 -->

<#import "/layout/modal_layout.ftlh" as modal>

<@modal.layout id="schedule_reg" size="lg">
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
            일정 저장
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
    <div class="modal-body py-10 px-lg-17">
        <#--begin::Form-->
        <form name="scheduleRegForm" id="scheduleRegForm">
            <#--Vue 컴포넌트 마운트 포인트 (ScheduleRegModal.ts)-->
            <div id="schedule_reg_modal_app"></div>
            <#--begin::잔디 알림 발송 영역 (공개 일정만 표시, ScheduleRegModal.ts 에서 show/hide 제어)-->
            <div id="schedule_jandi_area" style="display:none;">
                <@component.jandi_reg_area 2 "SCHEDULE" />
            </div>
        </form>
    </div>
    <#--begin::Modal Footer-->
    <div class="modal-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end gap-2">
            <#--모달:: 버튼: 저장하기-->
            <@component.modal_btn_save "window.dispatchEvent(new CustomEvent('schedule:submit-reg-form'));" />
            <#--모달:: 버튼: 모달 닫기-->
            <@component.modal_btn_close_safe "" />
        </div>
    </div>
</@modal.layout>

