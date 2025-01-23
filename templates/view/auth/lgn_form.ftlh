<#--
 * 로그인 화면
 * (사이드바 없음)
 -->

<#import "/layout/layout_without_sidebar.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<script>
    /** Model 값을 객체에 담아 typescript로 전달 */
    const Model = (function() {
        return {
            userId: "${userId!}",
            errorMsg: "${errorMsg!}",
            isDupIdLgn: ${(isDupIdLgn!false)?c},
            isCredentialExpired: ${(isCredentialExpired!false)?c},
            needsPwReset: ${(needsPwReset!false)?c},
        }
    })();
</script>
<style>
    .app-page {
        background-image: url("/media/img/dreamdiary.jpg");
        height: 100%;
        background-position: center;
        background-repeat: no-repeat;
        background-size: cover;
        position: relative;
    }
</style>

<!--begin::LgnPage-->
<div class="w-lg-500px rounded mt-20 p-10 p-lg-15 mx-auto my-auto">
    <div class="aside-logo flex-column-auto pb-5 mb-20">
        <!-- -->
    </div>
    <div class="d-flex justify-content-center fs-4 mb-15 text-secondary fw-bold ls-1">
        <i class="bi bi-cloud-moon fs-2"></i>
        <span class="px-2">${Url.DOMAIN!}</span>
        <i class="bi bi-stars fs-2"></i>
    </div>
    <#--begin::Form-->
    <form name="lgnForm" id="lgnForm" class="w-100" method="post">
        <div class="opacity-75">
            <label class="form-label fs-6 text-secondary fw-bolder" for="userId">아이디</label>
            <input type="text" name="userId" id="userId" class="form-control form-control-lg enter required"
                   data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="아이디를 입력하세요."
                   value="" placeholder="아이디를 입력하세요." autocomplete="off" maxlength="20" />
        </div>
        <div class="h-15px mt-1 mb-5">
            <span id="userId_validate_span" class="text-danger"></span>
        </div>
        <div class="opacity-75">
            <label class="form-label text-secondary fw-bolder fs-6 mb-1" for="password">비밀번호</label>
            <input type="password" name="password" id="password" class="form-control form-control-lg enter required"
                   data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="비밀번호를 입력하세요."
                   value="" placeholder="비밀번호를 입력하세요." maxlength="20" autocomplete="off" />
        </div>
        <div class="h-15px mt-1 mb-3">
            <span id="password_validate_span" class="text-danger"></span>
        </div>
        <div class="text-left">
            <span id="errorMsgSpan" class="text-danger"></span>
        </div>
        <div class="me-2 float-end">
            <label class="form-check form-check-custom form-check-solid" for="${REMEMBER_ME_PARAM!}"
                   data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="현재 브라우저에서&#10;로그인 상태를 유지합니다.">
                <span class="form-check-label text-secondary me-2">
                    로그인 상태 유지
                </span>
                <input type="checkbox" class="form-check-input" id="${REMEMBER_ME_PARAM!}" name="${REMEMBER_ME_PARAM!}">
            </label>
        </div>
        <div class="d-flex flex-column text-center mb-4 mt-12 gap-2">
            <button type="button" class="btn btn-lg btn-light-primary opacity-75 w-100"
                    onclick="Page.lgn();"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="로그인합니다." >
                <span class="indicator-label">Sign In</span>
                <span class="indicator-progress">Please wait...
                    <span class="spinner-border spinner-border-sm align-middle ms-2"></span>
                </span>
            </button>
            <button type="button" class="btn btn-lg btn-light-danger opacity-75 w-100"
                    onclick="Page.oauth2popup('GOOGLE');"
                    data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="Google 계정으로 로그인합니다." >
                <span class="indicator-label">
                    <i class="bi bi-google blink"></i> Sign In With Google
                </span>
                <span class="indicator-progress">Please wait...
                    <span class="spinner-border spinner-border-sm align-middle ms-2"></span>
                </span>
            </button>
        </div>
        <div class="d-flex justify-content-end mb-5">
            <button type="button" class="badge btn btn-sm btn-light-primary badge-outlined btn-outlined fw-light opacity-75 blink"
                    onclick="Page.reqstUser();"
                    data-bs-toggle="tooltip" data-bs-placement="bottom" title="계정 신청 화면으로&#10;이동합니다.">
                <i class="bi bi-person-plus-fill blink"></i>신규 계정 신청
            </button>
        </div>
        <div class="text-end mb-5">
            ©2024. nichefish. All rights reserved.
        </div>
    </form>
    <!--end::Form-->
</div>

<#--include::password change-->
<#include "/view/auth/_modal/_lgn_pw_chg_modal.ftlh">
<script type="text/javascript" src="/js/view/auth/lgn_pw_chg_module.js?${releaseDate!}"></script>

<#--include::page scripts-->
<script type="text/javascript" src="/js/view/auth/lgn_form.js?${releaseDate!}"></script>

</@layout.layout>