<#--
 * 로그인 정책(lgn_policy) > 관리 화면
 -->

<#import "/layout/layout_default.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<div class="card post">
    <#--begin::Card body-->
    <div class="card-body">
        <#--begin::Form-->
        <form name="lgnPolicyForm" id="lgnPolicyForm" class="form">
            <input type="hidden" name="lgnPolicyNo" value="${lgnPolicy.lgnPolicyNo!}">
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-form-label fs-6 fw-bold">
                    <label for="lgnLockDy">장기간 미로그인 잠금일수</label>
                </div>
                <div class="col-xl-1">
                    <input type="text" name="lgnLockDy" id="lgnLockDy" class="form-control form-control-solid text-end required number"
                           value="${lgnPolicy.lgnLockDy!}" maxlength="3" />
                    <div id="lgnLockDy_validate_span"></div>
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
                    <label for="lgnTryLmt">로그인 제한 횟수</label>
                </div>
                <div class="col-xl-1">
                    <input type="text" name="lgnTryLmt" id="lgnTryLmt" class="form-control form-control-solid text-end required number"
                           value="${lgnPolicy.lgnTryLmt!}" maxlength="3" />
                    <div id="lgnTryLmt_validate_span"></div>
                </div>
                <div class="col-xl-1 col-form-label w-5">
                    회
                </div>
                <div class="col-xl-8 text-noti">
                    ※ 해당 횟수만큼 연속 로그인 실패시 자동 계정 잠금<br />
                    (로그인 성공시 카운트 초기화)
                </div>
            </div>
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-form-label fs-6 fw-bold">
                    <label for="pwChgDy">비밀번호 변경 일수</label>
                </div>
                <div class="col-xl-1">
                    <input type="text" name="pwChgDy" id="pwChgDy" class="form-control form-control-solid text-end required number"
                           value="${lgnPolicy.pwChgDy!}" maxlength="3" />
                    <div id="pwChgDy_validate_span"></div>
                </div>
                <div class="col-xl-1 col-form-label w-5">
                    일
                </div>
                <div class="col-xl-8 text-noti">
                    ※ 비밀번호 변경 이후 해당 일수 이후부터 비밀번호 변경해야 로그인 가능<br />
                    (비밀번호 변경시 카운트 초기화)
                </div>
            </div>
            <#--begin::Row-->
            <div class="row mb-4">
                <div class="col-xl-2 col-form-label fs-6 fw-bold">
                    <label for="pwForReset">비밀번호 초기화 설정값</label>
                </div>
                <div class="col-xl-2">
                    <input type="password" name="pwForReset" id="pwForReset" class="form-control form-control-solid required"
                           value="${lgnPolicy.pwForReset!}" maxlength="16" autocomplete="off" />
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
            <@component.post_btn_submit "dF.LgnPolicy.submit();" />
        </div>
    </div>
</div>
<!--end::Card-->

<#--include::script modules-->
<script type="text/javascript" src="/js/view/domain/admin/lgn_policy/lgn_policy_module.js"></script>
<script type="text/javascript" src="/js/view/domain/admin/lgn_policy/lgn_policy_reg_form.js"></script>

</@layout.layout>