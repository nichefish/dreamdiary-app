<!--
 * 게시판 관리 : ASIDE (우측 사이드바)
 * @included_in : board_def_list.ftlh
 -->

<#--begin::aside-->
<div id="kt_app_aside" class="app-aside flex-column mt-7"
     data-kt-drawer="true"
     data-kt-drawer-name="app-aside"
     data-kt-drawer-activate="{default: true, lg: false}"
     data-kt-drawer-overlay="true"
     data-kt-drawer-width="auto"
     data-kt-drawer-direction="end"
     data-kt-drawer-toggle="#kt_app_aside_toggle">

    <#--begin::Aside wrapper-->
    <div id="kt_app_aside_wrapper" class="hover-scroll-y px-5 mx-5 my-5"
         data-kt-scroll="true"
         data-kt-scroll-activate="true"
         data-kt-scroll-height="auto"
         data-kt-scroll-dependencies="#kt_app_header"
         data-kt-scroll-wrappers="#kt_app_aside"
         data-kt-scroll-offset="5px"
         data-kt-scroll-save-state="true">

        <#--begin::Aside1-->
        <div class="card card-reset card-p-0">
            <#--begin::Card header-->
            <div class="card-header min-h-auto mb-5">
                <h3 class="card-title text-gray-900 fw-bold fs-3">※ 예약 게시판 코드 목록</h3>
                <div class="card-toolbar">
                    <a href="#" class="btn btn-sm btn-icon btn-color-gray-500 btn-light"
                       data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="게시판과 기본 구조를&#10;공유하는 다른 엔티티에서&#10;이미 점유 중인&#10;게시판 코드입니다.&#10;(중복 사용시 데이터 참조가 오작동합니다.)">
                        <i class="bi bi-question-circle"></i>
                    </a>
                </div>
            </div>
            <!--end::Header-->
            <#--begin::Body-->
            <div class="card-body">
                <div class="text-noti">
                    ※게시판과 기본 구조를 공유하는 다른 엔티티에서 이미 점유 중인 게시판 코드입니다.
                </div>
                <div class="text-noti">
                    (중복 사용시 데이터 참조가 오작동합니다.)
                </div>

                <div class="separator my-8"></div>

                <#--begin::Table-->
                <table class="table align-middle table-row-dashed fs-small gy-4 table-fixed mb-3">
                    <#--begin::Table head-->
                    <thead>
                        <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                            <th class="col text-center wb-keepall">예약 코드</th>
                            <th class="col text-center wb-keepall">사용 엔티티)</th>
                        </tr>
                    </thead>
                    <#--begin::Table body-->
                    <tbody>

                    </tbody>
                </table>
            </div>
        </div>
        <!--end::Aside1-->
    </div>
    <!--end::Aside wrapper-->
</div>
<!--end::aside-->