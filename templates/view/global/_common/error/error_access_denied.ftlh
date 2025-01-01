<#--
 * 에러 페이지 : 403 ACCESS DENIED
 * (사이드바 없음)
 -->

<#import "/layout/layout_without_sidebar.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<script>
    const Page = (function() {
        return {
            /**
             * Page 객체 초기화
             */
            init: function() {
                //
            },

            /**
             * 메인으로 돌아가기
             */
            main: function() {
                cF.util.blockUIReplace("${Url.MAIN!}");
            }
        }
    })();
</script>

<div class="d-flex-center">
    <span class="ctgr-span ctgr-imprtc">Access Denied</span>
</div>
<div class="d-flex-center flex-column mt-8">
    <div>접근 권한이 없습니다.</div>

    <div class="text-gray-500 fw-bold min-h-100px">${errorMsg!}</div>

    <div class="mt-10">
        <button type="button" class="btn btn-sm btn-outlined btn-light-primary btn-active-primary py-2 px-3"
                onclick="Page.main();"
                data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" title="메인 화면으로 돌아갑니다.">
            <i class="bi bi-arrow-counterclockwise"></i>메인으로 돌아가기...
        </button>
    </div>
</div>

</@layout.layout>