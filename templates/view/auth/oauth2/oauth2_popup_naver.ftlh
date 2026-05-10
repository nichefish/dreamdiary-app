<#--
 * oauth2_popup_naver.ftlh
 * Naver OAuth2 콜백 팝업 화면(Vue: OAuth2PopupApp)
 -->

<#import "/layout/layout_without_sidebar.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<script id="oauth2_popup_data" type="application/json">
{
    "providerKey": "naver",
    "providerLabel": "Naver OAuth2.0",
    "authenticatedText": "Authenticated with Naver.",
    "errorMsg": "${(errorMsg!'')?js_string}",
    "returnMainLabel": "${springMacroRequestContext.getMessage('view.return-to-main')?js_string}",
    "returnMainTooltip": "${springMacroRequestContext.getMessage('bs.tooltip.return-to-main')?js_string}"
}
</script>

<div id="oauth2_popup_app"></div>

<script type="module" src="/vue/auth/oauth2/OAuth2PopupApp.js?${releaseDate!}"></script>

</@layout.layout>
