<#--
 * 인증 정책(auth_policy) > 관리 화면
 -->

<#import "/layout/layout_default.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<div class="card post">
    <#--begin::Card body-->
    <div class="card-body">
        <#--begin::Form-->
        <form name="authPolicyForm" id="authPolicyForm" class="form">
            <input type="hidden" name="id" value="${authPolicy.id!}">
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-form-label fs-6 fw-bold">
                    <label for="inactiveLockDays">장기간 미로그인 잠금일수</label>
                </div>
                <div class="col-xl-1">
                    <input type="text" name="inactiveLockDays" id="inactiveLockDays" class="form-control form-control-solid text-end required number"
                           value="${authPolicy.inactiveLockDays!}" maxlength="3" />
                    <div id="inactiveLockDays_validate_span"></div>
                </div>
                <div class="col-xl-1 col-form-label w-5">
                    일
                </div>
                <div class="col-xl-8 text-noti">
                    ※ 마지막 로그인 이후 해당 일수 동안 미로그인시 자동 계정 잠금<br />
                    (로그인 성공시 카운트 초기화)
                </div>
            </div>
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-form-label fs-6 fw-bold">
                    <label for="loginAttemptLimit">로그인 제한 횟수</label>
                </div>
                <div class="col-xl-1">
                    <input type="text" name="loginAttemptLimit" id="loginAttemptLimit" class="form-control form-control-solid text-end required number"
                           value="${authPolicy.loginAttemptLimit!}" maxlength="3" />
                    <div id="loginAttemptLimit_validate_span"></div>
                </div>
                <div class="col-xl-1 col-form-label w-5">
                    회
                </div>
                <div class="col-xl-2">
                    <input type="text" name="loginAttemptWindowMinutes" id="loginAttemptWindowMinutes" class="form-control form-control-solid text-end required number"
                           value="${authPolicy.loginAttemptWindowMinutes!}" maxlength="3" />
                    <div id="loginAttemptWindowMinutes_validate_span"></div>
                </div>
                <div class="col-xl-1 col-form-label w-5">
                    분
                </div>
                <div class="col-xl-8 text-noti">
                    ※ 해당 횟수만큼 연속 로그인 실패시 자동 계정 잠금<br />
                    (로그인 성공시 카운트 초기화)
                </div>
            </div>
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-form-label fs-6 fw-bold">
                    <label for="passwordChangeCycleDays">비밀번호 변경 일수</label>
                </div>
                <div class="col-xl-1">
                    <input type="text" name="passwordChangeCycleDays" id="passwordChangeCycleDays" class="form-control form-control-solid text-end required number"
                           value="${authPolicy.passwordChangeCycleDays!}" maxlength="3" />
                    <div id="passwordChangeCycleDays_validate_span"></div>
                </div>
                <div class="col-xl-1 col-form-label w-5">
                    일
                </div>
                <div class="col-xl-8 text-noti">
                    ※ 비밀번호 변경 이후 해당 일수 이후부터 비밀번호 변경해야 로그인 가능<br />
                    (비밀번호 변경시 카운트 초기화)
                </div>
            </div>
            <div class="row mb-4">
                <div class="col-xl-2 col-form-label fs-6 fw-bold">
                    <label for="accountLockDurationMinutes">계정 잠금 지속 시간</label>
                </div>
                <div class="col-xl-1">
                    <input type="text" name="accountLockDurationMinutes" id="accountLockDurationMinutes" class="form-control form-control-solid text-end required number"
                           value="${authPolicy.accountLockDurationMinutes!}" maxlength="4" />
                    <div id="accountLockDurationMinutes_validate_span"></div>
                </div>
                <div class="col-xl-1 col-form-label w-5">분</div>
                <div class="col-xl-8 text-noti">
                    ※ 로그인 실패 제한 초과 시 잠금 유지 시간
                </div>
            </div>
            <div class="row mb-4">
                <div class="col-xl-2 col-form-label fs-6 fw-bold">
                    <label for="passwordResetTokenExpiryMinutes">비밀번호 재설정 토큰 만료 시간</label>
                </div>
                <div class="col-xl-1">
                    <input type="text" name="passwordResetTokenExpiryMinutes" id="passwordResetTokenExpiryMinutes" class="form-control form-control-solid text-end required number"
                           value="${authPolicy.passwordResetTokenExpiryMinutes!}" maxlength="5" />
                    <div id="passwordResetTokenExpiryMinutes_validate_span"></div>
                </div>
                <div class="col-xl-1 col-form-label w-5">분</div>
                <div class="col-xl-8 text-noti">
                    ※ 비밀번호 재설정 링크/토큰 유효 시간
                </div>
            </div>
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-form-label fs-6 fw-bold">
                    <label for="pwForReset">비밀번호 초기화 설정값</label>
                </div>
                <div class="col-xl-2">
                    <input type="password" name="pwForReset" id="pwForReset" class="form-control form-control-solid required"
                           value="${authPolicy.pwForReset!}" maxlength="16" autocomplete="off" />
                    <div id="pwForReset_validate_span"></div>
                </div>
                <div class="col-xl-8 text-noti">
                    ※ 계정 '비밀번호 초기화'시 초기값으로 설정할 비밀번호<br/>
                    ※ 개인에게 초기화 비밀번호 발급시 변경 필수 + 평소 주기적으로 변경 요망
                </div>
            </div>
        </form>
    </div>
    <#--begin::Card footer-->
    <div class="card-footer">
        <#--버튼 영역-->
        <div class="d-flex justify-content-end">
            <#--페이지:: 버튼: 저장하기-->
            <@component.post_btn_submit "dF.AuthPolicy.submit();" />
        </div>
    </div>
</div>
<!--end::Card-->

<#--include::script modules-->
<script type="text/javascript" src="/js/view/feature/admin/auth/policy/auth_policy_module.js"></script>

<!--include::page scripts-->
<script type="text/javascript" src="/js/view/feature/admin/auth/policy/auth_policy_reg_form.js"></script>

</@layout.layout>
