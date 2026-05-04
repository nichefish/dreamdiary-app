<#--
 * 로그인 화면
 * (사이드바 없음)
 -->

<#import "/layout/layout_without_sidebar.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<script>
    /** Spring Model 값을 Vue 앱에 전달 */
    const Model = (function() {
        return {
            username: "${(username!)?js_string}",
            errorMsg: "${(errorMsg!)?js_string}",
            isDupIdLogin: ${(isDupIdLogin!false)?c},
            isCredentialExpired: ${(isCredentialExpired!false)?c},
            needsPasswordReset: ${(needsPasswordReset!false)?c},
            rememberMeParam: "${(REMEMBER_ME_PARAM!)?js_string}",
            domain: "${(Url.DOMAIN!)?js_string}",
            locale: "${.locale?replace('_', '-')?js_string}",
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

<div id="login-vue-app"></div>
<script type="module" src="/vue/auth/security/LoginFormApp.js?${releaseDate!}"></script>

</@layout.layout>
