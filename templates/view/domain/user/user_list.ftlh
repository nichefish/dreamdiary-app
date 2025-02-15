<#--
  -- 사용자 관리 > 계정 및 권한 관리 > 목록 화면
  -->

<#import "/layout/layout_default.ftlh" as layout >

<#--toolbar::버튼 추가-->
<#assign toolbar>
    <#include "/view/domain/user/_user_list_header.ftlh">
</#assign>

<@layout.layout toolbar=toolbar!"">

<div class="card post">
    <#--begin::Card body-->
    <div class="card-body">
        <#--begin::Table-->
        <table class="table align-middle table-row-dashed fs-small gy-5 table-fixed hoverTable mb-3">
            <#--begin::Table head-->
            <thead>
                <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 fw-bolder text-muted">
                    <th class="text-center wb-keepall w-10 hidden-table">번호</th>
                    <th class="text-center wb-keepall hidden-table">권한</th>
                    <th class="col-2 text-center wb-keepall">이름 (아이디)</th>
                    <th class="col-1 text-center wb-keepall hidden-table">소속</th>
                    <th class="col-1 text-center wb-keepall hidden-table">팀</th>
                    <th class="col-1 text-center wb-keepall">직급</th>
                    <th class="col-3 text-center wb-keepall hidden-table">E-MAIL</th>
                    <th class="text-center wb-keepall hidden-table">잠금여부</th>
                </tr>
            </thead>
            <#--begin::Table body-->
            <tbody>
                <#list userList![] as user>
                    <!-- 계정신청 상태인 계정에 대해 구분 간격 표시 -->
                    <#assign divideCfYn = prevCfYn?? && user.cfYn! != prevCfYn>
                    <#assign divideRetireYn = prevRetireYn?? && user.retireYn! != prevRetireYn>
                    <#assign divide = divideCfYn || divideRetireYn>
                    <tr class="<#if user.isMe!false>bg-light</#if> bg-hover-secondary <#if divide>border-top-5</#if>" onclick="dF.User.dtl(${user.userNo!});">
                        <td class="text-center hidden-table">
                            ${user.rnum}
                        </td>
                        <td class="text-center hidden-table">
                            <#list user.authList![] as auth>
                                <#if (auth.authCd! == Constant.AUTH_MNGR)>
                                    <i class="bi bi-person-lines-fill text-info ms-1 opacity-75"></i>   <!-- 관리자 -->
                                <#elseif (auth.authCd! == Constant.AUTH_USER)>
                                    <i class="bi bi-people-fill ms-1"></i>        <!-- 사용자 -->
                                <#elseif (auth.authCd! == Constant.AUTH_DEV)>
                                    <i class="bi bi-person-fill-gear ms-1"></i>        <!-- 개발자 -->
                                </#if>
                                ${auth.authNm!}
                            </#list>
                        </td>
                        <td class="text-start ps-8">
                            <a href="javascript:void(0);" class="text-dark">
                                <#if (user.proflImgUrl)??>
                                    <div class="btn btn-icon btn-active-light-primary position-relative w-15px h-15px w-md-20px h-md-20px me-1">
                                        <img src="${(user.proflImgUrl)!}" class="img-thumbnail p-0 w-100" />
                                    </div>
                                </#if>
                                <span class="pe-1">${user.userNm!"-"} (${user.userId!"-"})</span>
                            </a>
                            <#if (user.userProflYn!"Y") == "N">
                                <i class="bi bi-person-dash vertical-middle text-muted"
                                   data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="직원정보가 없습니다.&#10;(접속은 가능하지만&#10;일부 기능이 제한됩니다.)"></i>
                            <#elseif (user.retireYn!"N") == "Y">
                                <i class="bi bi-person-dash vertical-middle text-danger"
                                   data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="퇴사한 인원입니다.&#10;(접속이 불가능합니다.)"></i>
                            <#elseif (user.reqstYn!"N") == "Y" && (user.cfYn!"N") == "N">
                                <i class="bi bi-person-plus vertical-middle text-muted blink"
                                   data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="신규 신청 계정입니다.&#10;(승인 처리 이후&#10;접속 가능합니다.)"></i>
                            <#else>
                                <i class="bi bi-person-check vertical-middle text-success"
                                   data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="소속된 직원입니다."></i>
                            </#if>
                        </td>
                        <td class="text-center hidden-table">
                            ${(user.cmpyNm)!"-"}
                        </td>
                        <td class="text-center hidden-table">
                            ${(user.teamNm)!"-"}
                        </td>
                        <td class="text-center">
                            ${(user.rankNm)!"-"}
                            <#if (user.rankNm)! == "사원" && user.apntcYn! == "Y"><span class="text-muted">(수습)</span></#if>
                        </td>
                        <td class="text-start ps-8 hidden-table">
                            ${(user.email)!"-"}
                        </td>
                        <td class="text-center hidden-table">
                            <#if !user.isCf!true>
                                <span class="text-muted">
                                    미승인<i class="bi bi-dash fs-9 text-muted blink"></i>
                                </span>
                            <#elseif user.isLocked!false>
                                <span class="text-danger">
                                    잠김<i class="bi bi-lock fs-9 text-danger"></i>
                                </span>
                            <#else>
                                <span class="text-success">
                                    사용<i class="bi bi-check text-success"></i>
                                </span>
                            </#if>
                        </td>
                    </tr>
                    <!-- 계정 승인여부 상태 저장 -->
                    <#assign prevRetireYn = user.retireYn! />
                    <#assign prevCfYn = user.cfYn! />
                <#else>
                    <tr>
                        <td colspan="7" class="text-center">
                            <@spring.message 'view.list.empty'/>
                        </td>
                    </tr>
                </#list>
            </tbody>
        </table>
    </div>
    <#--begin::Card footer-->
    <div class="card-footer">
        <#--pagination::begin-->
        <#include "/view/global/_pagination.ftlh">
        <#--pagination::end-->
    </div>
</div>

<form name="procForm" id="procForm" method="get">
    <input type="hidden" name="userNo" id="userNo" />
</form>

<#--include::script modules-->
<script type="text/javascript" src="/js/view/domain/user/user_module.js?${releaseDate!}"></script>

<!--include::page scripts-->
<script type="text/javascript" src="/js/view/domain/user/user_list.js?${releaseDate!}"></script>

</@layout.layout>