<#--
 * 분류 코드(CL_CD) > 상세 화면
 -->

<#import "/layout/layout_default.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<div class="card post">
    <#--begin::Card body-->
    <div class="card-body">
        <#--begin::title-->
        <div class="mb-0">
            <#--begin::Head-->
            <div class="d-flex align-items-center mb-9">
                <#--begin::title-->
                <div class="col-12">
                    <h1 class="fs-2x fw-bolder text-gray-900 mb-0 me-1">
                        <#if clCd.clCtgrNm??>
                            <span class="fw-bolder fs-2 ctgr-span ctgr-imprtc">
                                ${clCd.clCtgrNm!}
                            </span>
                        </#if>
                        ${clCd.clCd!} (${clCd.clCdNm!})
                    </h1>
                </div>
            </div>
        </div>

        <div class="separator separator-dashed border-gray-300 my-8"></div>

        <!--begin::cn-->
        <div class="fs-4 fw-normal text-gray-800 px-5 py-1 pb-6 min-h-150px">
            ${clCd.dc?no_esc}
        </div>
    </div>
    <#--begin::Card footer-->
    <div class="card-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--페이지:: 버튼: 수정 모달 호출-->
            <button type="button" class="btn btn-sm btn-light btn-active-primary me-2"
                    onclick="dF.ClCd.mdfModal('${clCd.clCd!}');"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="구분 코드 수정 팝업을&#10;호출합니다.">
                <i class="bi bi-pencil-square"></i>수정
            </button>
            <#--페이지:: 버튼: 등록자 또는 관리자가 삭제 가능-->
            <button type="button" class="btn btn-sm btn-light btn-active-danger me-2"
                    onclick="dF.ClCd.delAjax('${clCd.clCd!}');"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="구분 코드를 삭제합니다.">
                <i class="bi bi-trash"></i>삭제
            </button>
            <#--페이지:: 버튼: 목록으로 가기-->
            <@component.post_btn_toList "dF.ClCd.list();" />
        </div>
    </div>
</div>

<div class="card post">
    <#--begin::Card header-->
    <div class="card-header border-0 pt-6">
        <div class="d-flex justify-content-left align-items-center">
            <h5>상세 코드 목록</h5>
        </div>
        <div class="card-title text-end">
            <div class="d-flex align-items-center position-relative my-1">
            </div>
        </div>
        <div class="card-toolbar gap-2">
            <!--begin::Toolbar-->
            <div>
                <button type="button" class="btn btn-sm btn-primary"
                        onclick="dF.DtlCd.regModal();"
                        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="상세 코드 등록 모달을&#10;호출합니다.">
                    <i class="fas fa-plus"></i>상세 코드 추가
                </button>
            </div>
            <!--end::Toolbar-->
        </div>
    </div>
    <#--begin::Card body-->
    <div class="card-body">
        <table class="table align-middle table-row-dashed fs-small gy-5 table-fixed hoverTable mb-3">
            <#--begin::Table head-->
            <thead>
                <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 fw-bolder text-muted">
                    <th class="col-1 text-center wb-keepall w-8">번호</th>
                    <th class="col-2 text-center wb-keepall">
                        <span class="cursor-help"
                              data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                              title="코드를 특정할 때 내부적으로&#10;사용되는 고유 코드입니다.&#10;(등록 후 수정할 수 없습니다.)">
                            상세 코드
                        </span>
                    </th>
                    <th class="col-2 text-center wb-keepall">
                        <span class="cursor-help"
                              data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                              title="외부적으로 사용되는&#10;코드 이름입니다.">
                            상세 코드명
                        </span>
                    </th>
                    <th class="col-4 text-center wb-keepall">상세 코드 설명</th>
                    <th class="col-1 text-center wb-keepall">
                        <span class="cursor-help"
                              data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                              title="표시될 순서를 입력합니다.&#10;순서가 겹칠 경우&#10;내부 로직에 따라 정렬됩니다.">
                            정렬 순서
                        </span>
                    </th>
                    <th class="col-1 text-center wb-keepall">
                        <span class="cursor-help"
                              data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                              title="'사용'으로 체크된 코드만&#10;실제 코드 사용시 조회됩니다.">
                            사용 여부
                        </span>
                    </th>
                    <th class="text-center wb-keepall w-8">-</th>
                </tr>
            </thead>
            <#--begin::Table body-->
            <tbody class="draggable-zone">
                <#list clCd.dtlCdList![] as dtlCd>
                    <tr class="sortable-item draggable bg-hover-secondary" id="${dtlCd.dtlCd!}" data-id="${dtlCd.dtlCd!}" onclick="dF.DtlCd.mdfModal('${dtlCd.dtlCd!}');">
                        <td class="text-center">
                            <div class="d-flex-center draggable-handle cursor-move pt-2"
                                 data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="드래그하여&#10;항목을 이동합니다.">
                                <i class="ki-duotone ki-abstract-14 fs-2x">
                                    <span class="path1"></span>
                                    <span class="path2"></span>
                                </i>
                            </div>
                        </td>
                        <td class="text-start ps-10 cursor-pointer">
                            <span class="fw-bold border-bottom">
                                ${dtlCd.dtlCd!}
                            </span>
                        </td>
                        <td class="text-start ps-10">
                            <span class="border-bottom">
                                ${dtlCd.dtlCdNm!}
                            </span>
                        </td>
                        <td class="text-start ps-10">
                            ${dtlCd.dc!}
                        </td>
                        <td class="text-center">
                            ${dtlCd.sortOrdr!}
                        </td>
                        <td class="text-center">
                            <#if ((dtlCd.state.useYn)!"N") == "Y">
                                <div class="badge badge-success cursor-pointer"
                                     onclick="dF.DtlCd.unuseAjax('${dtlCd.dtlCd!}');"
                                     data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="클릭하여 사용 상태를&#10;변경합니다.">
                                    <i class="bi bi-check text-light blink-slow"></i>사용
                                </div>
                            <#else>
                                <div class="badge badge-secondary btn-white cursor-pointer"
                                     onclick="dF.DtlCd.useAjax('${dtlCd.dtlCd!}');"
                                     data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="클릭하여 사용 상태를&#10;변경합니다.">
                                    <i class="bi bi-x blink-slow"></i>미사용
                                </div>
                            </#if>
                        </td>
                        <td class="text-center">
                            <div class="badge badge-danger badge-outlined cursor-pointer"
                                 onclick="dF.DtlCd.delAjax('${dtlCd.dtlCd!}')"
                                 data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message "bs.tooltip.del"/>">
                                <i class="bi bi-trash text-danger fs-7 blink-slow"></i>삭제
                            </div>
                        </td>
                    </tr>
                <#else>
                    <tr>
                        <td colspan=8" class="text-center"><@spring.message "view.list.empty"/></td>
                    </tr>
                </#list>
            </tbody>
        </table>
    </div>
</div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="clCd" id="clCd" value="${clCd.clCd!}" />
</form>

<#--include::modals-->
<#include "/view/domain/admin/cd/_modal/_cl_cd_reg_modal.ftlh">
<#include "/view/domain/admin/cd/_modal/_dtl_cd_reg_modal.ftlh">
<#--include::script modules-->
<script type="text/javascript" src="/metronic/assets/plugins/custom/draggable/draggable.bundle.js"></script>
<script type="text/javascript" src="/js/common/helper/draggable.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/domain/admin/cd/cl_cd_module.js"></script>
<script type="text/javascript" src="/js/view/domain/admin/cd/cl_cd_dtl.js"></script>
<#include "/view/domain/admin/cd/_dtl_cd_scripts.ftlh">

</@layout.layout>