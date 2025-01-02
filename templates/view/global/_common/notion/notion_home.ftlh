<#--
  -- 활동 로그 목록 화면
  -->

<#import "/layout/layout_default.ftlh" as layout >

<#--<script defer="defer" src="/static/react/static/js/main.js"></script>
<link rel="stylesheet" type="text/css" href="/static/react/static/css/main.css">-->

<@layout.layout toolbar=toolbar!"">

<script>
    const Page = (function() {
        return {
            /**
             * Page 객체 초기화
             */
            init: function() {
                /* 모든 table 헤더에 클릭 이벤트를 설정한다. */
                cF.util.initSortTable();

                const domain = "";
                const pageId ="";
                Page.setIframe(pageId);
            },

            /**
             * iframe 세팅
             */
            setIframe: function(pageId) {
                const iframe = $('<iframe>', {
                  src: domain + pageId,
                  id: 'myFrame',
                  frameborder: 0,
                  scrolling: 'yes',
                  width: '100%',
                  height: '700px',
                  css: {
                    border: 'none'
                  }
                }).appendTo('#react_notion_root');
            }
        }
    })();
    document.addEventListener("DOMContentLoaded", function() {
        Page.init();
    });
</script>

<div class="card post">
    <div id="notion_page_rendered">
        ${notionPage!?no_esc}
    </div>

    <div id="react_notion_root">
        <#--Handlebars template goes in here-->
    </div>
</div>
<!--end::Card-->

</@layout.layout>
