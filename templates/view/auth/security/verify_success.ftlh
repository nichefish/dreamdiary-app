<#--
 * 에러 페이지
 * (사이드바 없음)
 -->

<#import "/layout/layout_without_sidebar.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<script>
    document.addEventListener("DOMContentLoaded", function() {
        setTimeout(function() {
            location.href = Url.AUTH_LGN_FORM;
        }, 3000);
    });
</script>

<div class="d-flex-center">
    <span class="ctgr-span ctgr-success">Authentication Success Page</span>
</div>
<div class="d-flex-center flex-column mt-8">
    <div>계정 신청 인증에 성공했습니다.</div>

    <div class="text-gray-500 fw-bold min-h-100px">3초 후 로그인 페이지로 이동합니다...</div>

    <div class="mt-10">
        <button type="button" class="btn btn-sm btn-outlined btn-light-primary btn-active-primary py-2 px-3"
                onclick="Page.main();"
                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="메인 화면으로 돌아갑니다.">
            <i class="bi bi-arrow-counterclockwise"></i>메인으로 돌아가기...
        </button>
    </div>
</div>

<#--include::script modules-->
<script type="text/javascript" src="/js/view/global/_common/error/error_page.js?${releaseDate!}"></script>

</@layout.layout>