<#--
 * oauth2_popup_google.ftlh
 * Google OAuth2 콜백 팝업 화면(Vue: OAuth2PopupApp)
 -->

<#import "/layout/layout_without_sidebar.ftlh" as layout >

<@layout.layout toolbar=toolbar!"">

<script id="oauth2_popup_data" type="application/json">
{
    "providerKey": "google",
    "providerLabel": "Google OAuth 2.0",
    "authenticatedText": "Authenticated with Google.",
    "errorMsg": "${(errorMsg!'')?js_string}",
    "returnMainLabel": "${springMacroRequestContext.getMessage('view.return-to-main')?js_string}",
    "returnMainTooltip": "${springMacroRequestContext.getMessage('bs.tooltip.return-to-main')?js_string}"
}
</script>

<div id="oauth2_popup_app"></div>

<script type="module" src="/vue/auth/oauth2/OAuth2PopupApp.js?${releaseDate!}"></script>

</@layout.layout>
