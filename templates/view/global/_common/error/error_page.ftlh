<#--
 * 에러 페이지
 * (사이드바 없음)
 -->

<#import "/layout/layout_without_sidebar.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<div id="error-page-app"></div>

<#--include::script modules-->
<script type="module" src="/vue/global/_common/error/ErrorPageApp.js?${releaseDate!}"></script>
<script type="text/javascript">
  document.addEventListener('DOMContentLoaded', function() {
    if (typeof window.ErrorPageVueApp !== 'undefined' && window.ErrorPageVueApp.mount) {
      window.ErrorPageVueApp.mount('#error-page-app', {
        errorType: 'general',
        errorMsg: '${errorMsg!}'
      });
    }
  });
</script>

</@layout.layout>