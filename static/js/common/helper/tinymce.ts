/**
 * tinymce.ts
 * @namespace: cF.tinymce
 * @author: nichefish
 * @depdendency: tinymce.js
 * 공통 - tinymce 에디터 관련 함수 모듈
 * (노출식 모듈 패턴 적용 :: cF.tinymce.init("#aaa", func) 이런식으로 사용)
 */
if (typeof cF === 'undefined') { let cF = {}; }
cF.tinymce = (function() {

    /** 기본 옵션 분리 */
    const basicOptions = {
        editor_encoding: "raw",
        height: 540,
        menubar: false,
        branding: false,
        statusbar: false,
        default_link_target: "_blank",
        convert_urls: false,
        plugins: 'help quickbars searchreplace link autolink pageembed table lists advlist checklist emoticons hr visualchars visualblocks pagebreak code codesample',
        toolbar1: 'undo redo | searchreplace | styles styleselect fontselect fontsizeselect | bold italic underline strikethrough | forecolor backcolor | align | code codesample | help',
        toolbar2: 'emoticons custom_image link pageembed | hr | numlist bullist checklist moreless | visualchars visualblocks pagebreak | table tabledelete | tableprops tablerowprops tablecellprops | tableinsertrowbefore tableinsertrowafter tabledeleterow | tableinsertcolbefore tableinsertcolafter tabledeletecol',
        contextmenu: 'link custom_image lists table',

        /* https://www.tiny.cloud/docs/plugins/opensource/textpattern/
        textpattern_patterns: [
            {start: '*', end: '*', format: 'italic'},
            {start: '**', end: '**', format: 'bold'},
            {start: '~', end: '~', cmd: 'createLink', value: 'https://tiny.cloud'}
        ], */
    };

    return {
        /**
         * tinymce 에디터를 초기화합니다.
         * @dependency: tinyMCE
         * @param {string} selectorStr - 초기화할 에디터의 선택자 문자열.
         * @param {Function} imgFunc - 이미지 업로드 로직을 처리할 함수.
         */
        init: function(selectorStr: string, imgFunc: Function = null) {
            const editorElement = document.querySelector(selectorStr);
            if (!editorElement) return;

            const options = {
                selector: selectorStr,
                ...basicOptions, // 공통 옵션을 병합
                setup: function (editor) {
                    // 자동 이스케이핑
                    editor.on('SaveContent', function (e) {
                        e.content = e.content.replace(/&#39/g, '&apos').replace(/&amp;/g, '&');
                    });
                    // 하단 option들 메뉴에 붙여두는 기능
                    editor.on('PostRender', function(e) {
                        const container = editor.getContainer();
                        const uiContainer = document.querySelectorAll('.tox-tinymce-aux');
                        uiContainer.forEach((c) => {
                            container.parentNode.appendChild(c);
                        });
                    });
                    // 이미지 업로드 로직
                    if (typeof imgFunc !== 'function') imgFunc = cF.tinymce.imgRegFunc;
                    editor.ui.registry.addButton('custom_image', {
                        icon: 'image',
                        tooltip: 'insert Image',
                        onAction: function() {
                             imgFunc();
                        }
                    });
                    // 글접기/펼치기 로직
                    cF.tinymce.sectionCount = 0;
                    editor.ui.registry.addButton('moreless', {
                        icon: 'vertical-align',
                        tooltip: 'insert moreless',
                        onAction: function() {
                            cF.tinymce.morelessFunc();
                        }
                    });
                }
            };
            tinymce.init(options);
        },

        /**
         * TinyMCE 에디터가 준비되면 컨텐츠를 설정합니다.
         * @param {string} editorNm - 설정할 에디터의 이름.
         * @param {string} cn - 설정할 내용.
         * @param {number} [attempt=0] - 현재 시도 횟수.
         */
        setContentWhenReady: function(editorNm: string, cn: string, attempt = 0) {
            const editor = tinymce.get(editorNm);
            const maxAttempts = 20; // 최대 시도 횟수
            if (editor && editor.initialized) {
                editor.setContent(cn);
            } else if (attempt < maxAttempts) {
                // 초기화가 완료될 때까지 재귀적으로 시도
                setTimeout(function() {
                    cF.tinymce.setContentWhenReady(editorNm, cn, attempt + 1);
                }, 50);
            } else {
                console.warn("Unable to set content for editor " + editorNm + ": Initialization failed after " + maxAttempts + " + attempts.");
            }
        },

        /**
         * TinyMCE 에디터를 제거합니다.
         * @param {string|HTMLElement|jQuery} selector - 제거할 에디터의 선택자, DOM 요소 또는 jQuery 객체.
         */
        destroy: function(selector) {
            const editorElements = cF.util.verifySelector(selector);
            if (editorElements.length === 0) return;

            editorElements.forEach(editorElement => {
                if (typeof tinymce !== 'undefined' && tinymce !== null) {
                    tinymce.remove(editorElement); // 지정된 선택자를 가진 에디터를 제거
                }
            });
        },

        /**
         * tinymce 에디터 이미지 첨부
         */
        imgRegFunc: function() {
            const fileInput = document.getElementById("atchFile0");
            fileInput.click();

            fileInput.addEventListener("change", function() {
                if (!this.value) return;
                if (!cF.validate.fileSizeChck(this)) return false;      // fileSizeChck
                if (!cF.validate.fileImgExtnChck(this)) return false;      // fileExtnChck

                const url = "/file/fileUploadAjax.do";
                const ajaxData = new FormData(document.getElementById("tinymceImageForm"));
                cF.util.blockUIMultipartAjax(url, ajaxData, function(res) {
                    if (cF.util.isNotEmpty(res.message)) alert(res.message);
                    if (!res.rslt) return;

                    const fileInfo = res.rsltObj;
                    const imgTag = "<img src='" + fileInfo.url + "' data-mce-src='" + fileInfo.url + "' data-originalFileName='" + fileInfo.orgnFileNm + "' >";
                    tinymce.execCommand('mceInsertContent', true, imgTag);

                    // file input 초기화
                    fileInput.value = "";
                    const templateDiv = document.getElementById("tinymceImageTemplate");
                    const originalTemplate = templateDiv.innerHTML;
                    templateDiv.innerHTML = "";
                    templateDiv.innerHTML = originalTemplate;
                });
            });
        },

        morelessFunc: function() {
            if (typeof cF.tinymce.sectionCount === "undefined") cF.tinymce.sectionCount = 0;
            const sectionId = "tinymce_section_" + cF.tinymce.sectionCount;
            const sectionContentId = `tinymce_section_content_` + cF.tinymce.sectionCount;
            const toggleId = `tinymce_toggle_` + cF.tinymce.sectionCount;

            const newSection = `
                <div class="tinymce-section" id="${sectionId}">
                  <span id="${toggleId}" class="tinymce-collapse-toggle">
                    Toggle Section ${cF.tinymce.sectionCount}
                  </span>
                  <div id="${sectionContentId}" class="tinymce-collapsed">
                    Section content goes here.
                  </div>
                </div>
              `;
            tinymce.execCommand('mceInsertContent', false, newSection);

            cF.tinymce.sectionCount++;
            console.log("sectionCount: " + cF.tinymce.sectionCount);
            // const toggleElement = document.getElementById(toggleId);
            // toggleElement.addEventListener('click', () => toggleCollapse(toggleId, sectionContentId));

            // function toggleCollapse(toggleId, sectionContentId) {
            //     const contentElement = document.getElementById(sectionContentId);
            //     if (contentElement) {
            //         contentElement.classList.toggle('collapsed');
            //     }
            // }
        }
    }
})();
