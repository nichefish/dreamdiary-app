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
    <link rel="shortcut icon" href="/static/favicon.ico" />

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
    <script type="text/javascript" src="/js/vendor/vue.global<#if activeProfile! == "prod">.prod</#if>.js"></script>
    <script type="text/javascript" src="/js/vendor/stomp.min.js"></script>
    <script type="text/javascript" src="/js/vendor/sockjs.min.js"></script>
    <script type="text/javascript" src="/js/vendor/handlebars.min.js"></script>
    <script type="text/javascript" src="/js/vendor/moment.min.js"></script>
    <!--Metronic:: Global Javascript Bundle(used by all pages)-->
    <script type="text/javascript" src="/metronic/assets/plugins/global/plugins.bundle.js"></script>

    <script type="text/javascript" src="/js/vendor/jquery/jquery.cookie.js"></script>
    <script type="text/javascript" src="/js/vendor/jquery/jquery.blockUI.min.js"></script>
    <script type="text/javascript" src="/js/vendor/jquery/jquery.form.min.js"></script>
    <script type="text/javascript" src="/js/vendor/jquery/jquery.validate.min.js"></script>
    <script type="text/javascript" src="/js/vendor/jquery/additional-methods.js"></script>

    <script type="text/javascript" src="/metronic/assets/js/scripts.bundle.js"></script>

    <!--Custom Javascript Bundle(used by all pages)-->
    <script type="text/javascript" src="/js/common/util.js?${releaseDate!}"></script>
    <script type="text/javascript" src="/js/common/ajax.js?${releaseDate!}"></script>
    <script type="text/javascript" src="/js/common/date.js?${releaseDate!}"></script>
    <script type="text/javascript" src="/js/common/format.js?${releaseDate!}"></script>
    <script type="text/javascript" src="/js/common/validate.js?${releaseDate!}"></script>
    <script type="text/javascript" src="/js/common/regex.js?${releaseDate!}"></script>
    <!--Custom Javascript Bundle :: helpers (used by all pages)-->
    <script type="text/javascript" src="/js/common/helper/datepicker.js?${releaseDate!}"></script>
    <script type="text/javascript" src="/js/common/helper/tagify.js?${releaseDate!}"></script>
    <script type="text/javascript" src="/js/common/helper/handlebars.js?${releaseDate!}"></script>
    <script type="text/javascript" src="/js/common/helper/draggable.js?${releaseDate!}"></script>


    <script>
        /** AuthInfo 객체 생성 */
        const AuthInfo = (function() {
            return Object.freeze({
                userId: "${(authInfo.userId)!}",
                nickNm: "${(authInfo.nickNm)!}",
                email: "${(authInfo.email)!}",
                proflImgUrl: "${(authInfo.proflImgUrl)!}",
                isMngr: ${((authInfo.isMngr)!false)?c},
                authList: [
                    <#list (authInfo.authList)![] as auth>
                        {
                            authCd: "${auth.authCd!}",
                            authNm: "${auth.authNm!}",
                        }<#if auth_has_next>,</#if>
                    </#list>
                ]
            });
        })();
        /** UrlMap으로부터 자바스크립트 객체 생성 */
        const Url = (function() {
            return Object.freeze({
                <#list urlMap! as key, value>
                    "${key!}": "${value!}",
                </#list>
            });
        })();
        /** MessageMap으로부터 자바스크립트 객체 생성 */
        const Message = (function() {
            const messageMap = {
                <#list messageMap! as key, value>
                    "${key!}": "${value!}",
                </#list>
            };

            return Object.freeze({
                /**
                 * 주어진 키에 해당하는 메시지를 반환한다. (부재시 기본 메시지 반환)
                 *
                 * @param {string} key - 메시지 번들의 키
                 * @param {string} [defaultMsg=""] - 기본 메시지 (선택 사항)
                 * @returns {string} - 메시지 번들에서 찾은 메시지 또는 기본 메시지
                 */
                get: function(key, defaultMsg = "") {
                    if (!messageMap[key]) {
                        console.warn("Message not found for key: " + key);
                        return defaultMsg;
                    }
                    return messageMap[key];
                }
            });
        })();
    </script>
</head>
