<#--
 * 레이아웃 > head
 * metronic v8.2.5
 * @included_in : layout
 -->

<#setting number_format="c">

<head>
    <title>${Constant.SITE_NM!}</title>
    <base href="${Url.MAIN!}">
    <meta name="description" content="${Constant.SITE_DESC!}"/>
    <meta name="keywords" content="${Constant.SITE_KEYWORDS!}"/>
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <meta charset="utf-8" />
    <meta property="og:locale" content="ko_KR" />
    <meta property="og:type" content="article" />
    <meta property="og:title" content="${Constant.SITE_NM!}" />
    <meta property="og:url" content="${Url.DOMAIN!}"/>
    <meta property="og:site_name" content="${Constant.SITE_NM!}" />
    <link rel="shortcut icon" href="/favicon.ico" />

    <!--Metronic:: Gloabl Css Bundle(used by all pages) -->
    <link rel="stylesheet" type="text/css" href="/metronic/assets/plugins/global/plugins.bundle.css" />
    <link rel="stylesheet" type="text/css" href="/metronic/assets/css/style.bundle.css" />
    <!--Fonts:: Gloabl Css Bundle(used by all pages) -->
    <link rel="stylesheet" type="text/css" href="/lib/fontawesome/css/fontawesome.css" />
    <link rel="stylesheet" type="text/css" href="/lib/bootstrap-icon/bootstrap-icons.css" />

    <!--My:: Gloabl Css Bundle(used by all pages) -->
    <link rel="stylesheet" type="text/css" href="/css/commons.css?${releaseDate!}" />
    <link rel="stylesheet" type="text/css" href="/css/commons-respns.css?${releaseDate!}" />
    <link rel="stylesheet" type="text/css" href="/css/font.css?${releaseDate!}" />

    <!--Javascript:: Global Javascript Bundle(used by all pages)-->
    <script type="text/javascript" src="/js/vue.global<#if activeProfile! == "prod">.prod</#if>.js"></script>
    <script type="text/javascript" src="/js/stomp.min.js"></script>
    <script type="text/javascript" src="/js/sockjs.min.js"></script>
    <script type="text/javascript" src="/js/handlebars.min.js"></script>
    <script type="text/javascript" src="/js/moment.min.js"></script>
    <!--Metronic:: Global Javascript Bundle(used by all pages)-->
    <script type="text/javascript" src="/metronic/assets/plugins/global/plugins.bundle.js"></script>

    <script type="text/javascript" src="/js/jquery.cookie.js"></script>
    <script type="text/javascript" src="/js/jquery.blockUI.min.js"></script>
    <script type="text/javascript" src="/js/jquery.form.min.js"></script>
    <script type="text/javascript" src="/js/jquery.validate.min.js"></script>
    <script type="text/javascript" src="/js/additional-methods.js"></script>

    <script type="text/javascript" src="/metronic/assets/js/scripts.bundle.js"></script>

    <!--Custom Javascript Bundle(used by all pages)-->
    <script type="text/javascript" src="/js/commons.js?${releaseDate!}"></script>
    <script type="text/javascript" src="/js/commons-validate.js?${releaseDate!}"></script>
    <script type="text/javascript" src="/js/commons-date.js?${releaseDate!}"></script>
    <script type="text/javascript" src="/js/commons-datepicker.js?${releaseDate!}"></script>
    <script type="text/javascript" src="/js/commons-tagify.js?${releaseDate!}"></script>
    <script type="text/javascript" src="/js/commons-helpers.js?${releaseDate!}"></script>
</head>
