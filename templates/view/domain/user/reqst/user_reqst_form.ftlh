<#--
 * 사용자 관리 > 계정 및 권한 관리 > 등록 화면
 -->

<#import "/layout/layout_without_sidebar.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<style>
    @media (max-width: 768px) {
        #idDupChckBtn{
            font-size:12px;
        }
    }
</style>

<div class="card post w-75 mx-auto my-auto">
    <#--begin::Card header-->
    <div class="card-header min-h-auto mb-10">
        <div class="row mb-8 p-0">
            <div data-kt-swapper="true" data-kt-swapper-mode="prepend"
                 data-kt-swapper-parent="{default: '#kt_content_container', 'lg': '#kt_toolbar_container'}"
                 class="page-title d-flex align-items-center flex-wrap me-3 mb-5 mb-lg-0">
                <#--begin::title-->
                <h1 class="d-flex align-items-center text-dark fw-bolder fs-3 my-1">
                    <!-- breadcrumb -->
                    <ol class="breadcrumb text-muted fs-6 fw-semibold">
                        <li class="breadcrumb-item">
                            <a href="javascript:void(0);"
                               onclick="UserReqst.return();"
                               data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="메인 화면으로 돌아갑니다.">
                                <i class="bi bi-house-fill fs-4 me-1 text-primary"></i>
                            </a>
                        </li>
                        <li class="breadcrumb-item text-muted">${(siteAcsInfo.upperMenuNm)!}</li>
                        <li class="breadcrumb-item text-muted">${(siteAcsInfo.menuNm)!}</li>
                    </ol>
                    <!--begin::Separator-->
                    <span class="h-20px border-gray-200 border-start ms-3 mx-2"></span>
                    <!--end::Separator-->
                    <!--begin::Description-->
                    <small class="text-muted fs-7 fw-bold my-1 ms-1">${(siteAcsInfo.pageNm)!}</small>
                </h1>
            </div>
        </div>
    </div>

    <#--begin::Body-->
    <div class="card-body">
        <#--begin::Card body-->
        <div class="card-body">
            <#--begin::Form-->
            <form name="userReqstForm" id="userReqstForm" class="form" method="post" enctype="multipart/form-data">
                <input type="hidden" name="userNo" value="${user.userNo!}">
                <input type="hidden" name="atchFileNo" value="${user.atchFileNo!}">
                <#--begin::Row-->
                <div class="row mb-4">
                    <div class="col-xl-2">
                        <div class="fw-bold col-form-label text-lg-center text-sm-end required">
                            <label for="userId">아이디</label>
                        </div>
                    </div>
                    <div class="col-xl-2 col-8">
                        <input type="text" name="userId" id="userId" class="form-control form-control-solid required no-space"
                               value="${user.userId!}" placeholder="아이디" maxlength="16" />
                        <div class="form-text text-noti">
                            <@spring.message "noti.req.userid"/>
                        </div>
                        <div id="userId_validate_span" class="text-danger"></div>
                        <div id="ipDupChckPassed_validate_span" class="text-danger"></div>
                    </div>
                    <div class="col-xl-2 col-4">
                        <button type="button" class="btn btn-sm btn-secondary required blink" id="idDupChckBtn"
                                onclick="User.idDupChckAjax();"
                                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="입력한 아이디에 대한&#10;중복 여부를 확인합니다.">
                            <span class="hidden-table">아이디 </span>중복 확인
                        </button>
                        <input type="hidden" name="ipDupChckPassed" id="ipDupChckPassed" value="N"/>
                    </div>
                </div>
                <#--begin::Row-->
                <div class="row mb-4">
                    <div class="col-xl-2">
                        <div class="fw-bold col-form-label text-lg-center text-sm-end required">
                            <label for="password">비밀번호</label>
                        </div>
                    </div>
                    <div class="col-xl-2">
                        <input type="password" name="password" id="password" class="form-control form-control-solid required no-space"
                               placeholder="비밀번호" maxlength="20" autocomplete="off" />
                        <div class="form-text text-noti">
                            <@spring.message "noti.req.password"/>
                        </div>
                        <div id="password_validate_span"></div>
                    </div>
                    <div class="col-xl-2">
                        <div class="fw-bold col-form-label text-sm-start text-lg-end required">
                            <label for="passwordCf">비밀번호 확인</label>
                        </div>
                    </div>
                    <div class="col-xl-2">
                        <input type="password" name="passwordCf" id="passwordCf" class="form-control form-control-solid required no-space"
                               placeholder="비밀번호 확인" maxlength="20" autocomplete="off" />
                        <div class="form-text text-noti">
                            <@spring.message "noti.req.password.cf"/>
                        </div>
                        <div id="passwordCf_validate_span"></div>
                    </div>
                </div>
                <#--begin::Row-->
                <div class="row mb-4">
                    <div class="col-xl-2">
                        <div class="fw-bold col-form-label text-lg-center text-sm-end required">
                            <label for="authCd" class="cursor-help"
                                   data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="계정 권한을 설정합니다.&#10;(계정 신청시 사용자 권한으로&#10;고정됩니다.)">
                                권한
                                <i class="bi bi-question-circle"></i>
                            </label>
                        </div>
                    </div>
                    <div class="col-xl-2 col-form-label">
                        <i class="bi bi-people-fill me-1 blink-slow"></i>
                        <span class="fw-bold text-gray-600">사용자</span>
                        <input type="hidden" name="authList[0].authCd" id="authCd" value="${Constant.AUTH_USER!}">
                        <div id="authCd_validate_span"></div>
                    </div>
                </div>
                <#--begin::Row-->
                <!-- 가리고 숨기고를 반복하는 곳은 style로 선언해야 한다. -->
                <div class="row mb-4" id="nickNmShowDiv" <#if user.userProfl??>style="display:none;"</#if>>
                    <div class="col-xl-2">
                        <div class="fw-bold col-form-label text-lg-center text-sm-start required"
                             data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="프로필 정보 미입력시&#10;사용할 이름입니다.&#10;(외부사용자, 임시계정 등)">
                            <label for="nickNm" class="cursor-help">
                                표시이름
                                <i class="bi bi-question-circle"></i>
                            </label>
                        </div>
                    </div>
                    <div class="col-xl-2">
                        <input type="text" name="nickNm" id="nickNm" class="form-control form-control-solid required"
                               value="${user.nickNm!}" placeholder="표시이름" maxlength="20" />
                        <div class="form-text text-noti">
                            <@spring.message "noti.req.nicknm"/>
                        </div>
                        <div id="nickNm_validate_span"></div>
                    </div>
                </div>
                <#--begin::Row-->
                <div class="row mb-4">
                    <div class="col-xl-2">
                        <div class="fw-bold col-form-label text-sm-start text-lg-center required">
                            <label for="emailId" class="cursor-help"
                                   data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="이메일 주소입니다. 알림 및 계정 복구 등에 사용됩니다.">
                                E-mail
                                <i class="bi bi-question-circle"></i>
                            </label>
                        </div>
                    </div>
                    <div class="col-lg-2 col-4">
                        <input type="text" name="emailId" id="emailId" class="form-control form-control-solid required no-space"
                               value="${user.emailId!}" placeholder="이메일 ID 입력" maxlength="20" />
                        <div id="emailId_validate_span"></div>
                    </div>
                    <div class="col-xl-2 col-1 d-flex-center fw-bold text-gray-700" style="width:2%;">
                        <label for="emailDomain"><label for="emailDomainSelect">@</label></label>
                    </div>
                    <div class="col-lg-2 col-5 vertical-center">
                        <input type="text" name="emailDomain" id="emailDomain" class="form-control form-control-solid required no-space"
                               value="gmail.com" placeholder="직접입력" maxlength="20" />
                        <div id="emailDomain_validate_span"></div>
                    </div>
                    <div class="col-xl-2 col-1">
                        <select name="emailDomainSelect" id="emailDomainSelect" class="form-select form-select-solid">
                            <option value="">직접입력</option>
                            <option value="gmail.com" <#if isReg!false>selected</#if>>gmail.com</option>
                            <option value="naver.com">naver.com</option>
                            <option value="kakao.com">kakao.com</option>
                        </select>
                    </div>
                </div>
                <#--begin::Row-->
                <div class="row mb-4">
                    <div class="col-xl-2">
                        <div class="fw-bold col-form-label text-sm-start text-lg-center">
                            <label for="cttpc">
                                전화번호
                            </label>
                        </div>
                    </div>
                    <div class="col-xl-2">
                        <input type="text" name="cttpc" id="cttpc" class="form-control form-control-solid"
                               <#if user.userInfo??>value="${user.cttpc!}"</#if> placeholder="전화번호" maxlength="20" />
                        <div class="form-text text-noti"><@spring.message "noti.req.cttpc"/></div>
                        <div id="cttpc_validate_span"></div>
                    </div>
                </div>
                <#--begin::Row-->
                <div class="row mb-4">
                    <div class="col-xl-2">
                        <div class="fw-bold col-form-label text-lg-center text-sm-start ">
                            <label for="acsIpListStr" class="cursor-help"
                                   data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="특정 IP에서만&#10;접속 가능하도록 설정합니다.">
                                접속 IP
                                <i class="bi bi-question-circle"></i>
                            </label>
                        </div>
                    </div>
                    <div class="col-xl-10">
                        <div class="form-check form-switch form-check-custom form-check-solid mt-2">
                            <input type="checkbox" name="useAcsIpYn" id="useAcsIpYn" class="form-check-input" value="Y" />
                            <label class="form-check-label fw-bold ms-3" for="useAcsIpYn" id="useAcsIpYnLabel" style="color:gray;">
                                미사용
                            </label>
                        </div>
                        <!-- 가리고 숨기고를 반복하는 곳은 style로 선언해야 한다. -->
                        <div id="acsIpListSpan" class="mt-2 mb-0" style="display:none;">
                            <input type="text" name="acsIpListStr" id="acsIpListStr" class="form-control form-control-solid no-space"
                                   value="" maxlength="500" autocomplete="off"/>
                            <div class="form-text text-noti"><@spring.message "noti.req.acsip"/></div>
                            <div id="acsIpListStr_valid_span"></div>
                        </div>
                    </div>
                </div>
                <#--begin::Row-->
                <div class="row mb-4">
                    <div class="col-xl-2">
                        <div class="fw-bold col-form-label text-sm-start text-lg-center">
                            <label for="cn" class="cursor-help"
                                   data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="비고 사항을 입력합니다.">
                                계정 설명
                                <i class="bi bi-question-circle"></i>
                            </label>
                        </div>
                    </div>
                    <div class="col-xl-9">
                        <textarea name="cn" id="cn" class="form-control form-control-solid h-100px" wrap="hard" maxlength="4000" placeholder="비고 사항을 입력합니다.">${user.cn!}</textarea>
                    </div>
                </div>

                <#--사용자 프로필 정보 영역-->
                <div id="user_profl_div">
                    <#--handlebars template goes in here-->
                </div>

                <#--사용자 인사정보 영역-->
                <div id="user_emplym_div">
                    <#--handlebars template goes in here-->
                </div>
            </form>
        </div>
    </div>
    <#--begin::Card footer-->
    <div class="card-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-between">
            <div>
                <div class="d-flex gap-2">
                    <div id="user_profl_reg_btn_div">
                        <button type="button" id="userProflBtn" class="btn btn-sm btn-primary blink"
                                onclick="UserProfl.enableUserProfl();"
                                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="사용자 프로필 정보를&#10;추가합니다.">
                            프로필 정보 추가+
                        </button>
                    </div>
                    <div>
                        <button type="button" id="userEmplymBtn" class="btn btn-sm btn-primary blink"
                                onclick="UserEmplym.enableUserEmplym();"
                                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="입사인원에 대하여&#10;직원 인사정보를 추가합니다.">
                            직원 인사정보 추가+
                        </button>
                        <div class="text-noti">
                            <@spring.message "noti.req.user.emplym"/>
                        </div>
                    </div>
                </div>
            </div>
            <div class="gap-2">
                <button type="button" class="btn btn-sm btn-primary"
                        onclick="cF.util.submit('#userReqstForm');"
                        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="<@spring.message "bs.tooltip.save"/>">
                    <i class="bi bi-pencil-square"></i>신규계정 신청
                </button>
                <button type="button" class="btn btn-sm btn-light"
                        onclick="UserReqst.return();"
                        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="메인 화면으로 돌아갑니다.">
                    <i class="bi bi-backspace"></i>돌아가기
                </button>
            </div>
        </div>
    </div>
</div>

<#include "/view/domain/user/profl/_user_profl_reg_template.hbs">       <#--사용자 프로필 정보 영역-->
<#include "/view/domain/user/emplym/_user_emplym_reg_template.hbs">       <#--사용자 인사정보 영역-->
<#--include::script modules-->
<script type="text/javascript" src="/js/view/domain/user/user_module.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/domain/user/profl/user_profl_module.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/domain/user/reqst/user_reqst_module.js?${releaseDate!}"></script>
<script type="text/javascript" src="/js/view/domain/user/reqst/user_reqst_form.js?${releaseDate!}"></script>

</@layout.layout>