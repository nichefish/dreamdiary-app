<!--
 * Board management aside — 예약 코드 행은 ReservedStructuralBoard enum + 컨트롤러 모델로만 채운다.
 * @included_in : board_list.ftlh
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

    <div id="kt_app_aside_wrapper" class="hover-scroll-y px-5 mx-5 my-5"
         data-kt-scroll="true"
         data-kt-scroll-activate="true"
         data-kt-scroll-height="auto"
         data-kt-scroll-dependencies="#kt_app_header"
         data-kt-scroll-wrappers="#kt_app_aside"
         data-kt-scroll-offset="5px"
         data-kt-scroll-save-state="true">

        <div class="card card-reset card-p-0">
            <div class="card-header min-h-auto mb-5">
                <h3 class="card-title text-gray-900 fw-bold fs-3"><@spring.message 'txt.board.group.aside.reserved-title'/></h3>
                <div class="card-toolbar">
                    <a href="#" class="btn btn-sm btn-icon btn-color-gray-500 btn-light"
                       data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                       title="<@spring.message 'txt.board.group.aside.tooltip'/>">
                        <i class="bi bi-question-circle"></i>
                    </a>
                </div>
            </div>
            <div class="card-body">
                <div class="text-noti"><@spring.message 'txt.board.group.aside.notice-ln1'/></div>
                <div class="text-noti"><@spring.message 'txt.board.group.aside.notice-ln2'/></div>

                <div class="separator my-8"></div>

                <table class="table align-middle table-row-dashed fs-small gy-4 table-fixed mb-3">
                    <thead>
                        <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                            <th class="col text-center wb-keepall"><@spring.message 'txt.board.group.aside.col.code'/></th>
                            <th class="col text-center wb-keepall"><@spring.message 'txt.board.group.aside.col.consumer'/></th>
                        </tr>
                    </thead>
                    <tbody>
                        <#list reservedStructuralBoards![] as rb>
                            <tr>
                                <td class="text-center text-gray-800">${rb.boardKey}</td>
                                <td class="text-center text-gray-700">${rb.consumerLabel}</td>
                            </tr>
                        <#else>
                            <tr><td colspan="2" class="text-center text-muted">—</td></tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<!--end::aside-->
