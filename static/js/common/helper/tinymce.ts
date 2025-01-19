/**
 * tinymce.ts
 * 공통 - tinymce 에디터 관련 함수 모듈
 *
 * @namespace: cF.tinymce (노출식 모듈 패턴)
 * @author: nichefish
 */
// @ts-ignore
if (typeof cF === 'undefined') { var cF = {} as any; }
cF.tinymce = (function(): Module {
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
        init: function(selectorStr: string, imgFunc: Function = null): void {
            const editorElement: HTMLElement = document.querySelector(selectorStr);
            if (!editorElement) return;

            const options = {
                selector: selectorStr,
                ...basicOptions, // 공통 옵션을 병합
                setup: function (editor): void {
                    // 자동 이스케이핑
                    editor.on('SaveContent', function (e): void {
                        e.content = e.content.replace(/&#39/g, '&apos').replace(/&amp;/g, '&');
                    });
                    // 하단 option들 메뉴에 붙여두는 기능
                    editor.on('PostRender', function(): void {
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
                        onAction: function(): void {
                             imgFunc();
                        }
                    });
                    // 글접기/펼치기 로직
                    cF.tinymce.sectionCount = 0;
                    editor.ui.registry.addButton('moreless', {
                        icon: 'vertical-align',
                        tooltip: 'insert moreless',
                        onAction: function(): void {
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
        setContentWhenReady: function(editorNm: string, cn: string, attempt: number = 0): void {
            const editor = tinymce.get(editorNm);
            const maxAttempts = 20; // 최대 시도 횟수
            if (editor && editor.initialized) {
                editor.setContent(cn);
            } else if (attempt < maxAttempts) {
                // 초기화가 완료될 때까지 재귀적으로 시도
                setTimeout(function(): void {
                    cF.tinymce.setContentWhenReady(editorNm, cn, attempt + 1);
                }, 50);
            } else {
                console.warn("Unable to set content for editor " + editorNm + ": Initialization failed after " + maxAttempts + " + attempts.");
            }
        },

        /**
         * TinyMCE 에디터를 제거합니다.
         * @param {string|HTMLElement|JQuery} selector - 제거할 에디터의 선택자, DOM 요소 또는 jQuery 객체.
         */
        destroy: function(selector: string|HTMLElement|JQuery): void {
            const editors: HTMLElement[] = cF.util.verifySelector(selector);
            if (editors.length === 0) return;

            editors.forEach(editorElement => {
                if (typeof tinymce !== 'undefined' && tinymce !== null) {
                    tinymce.remove(editorElement); // 지정된 선택자를 가진 에디터를 제거
                }
            });
        },

        /**
         * tinymce 에디터 이미지 첨부
         */
        imgRegFunc: function(): void {
            const fileInput: HTMLInputElement = document.getElementById("atchFile0") as HTMLInputElement;
            fileInput.click();

            fileInput.addEventListener("change", function(): void {
                if (!this.value) return;
                if (!cF.validate.fileSizeChck(this)) return;      // fileSizeChck
                if (!cF.validate.fileImgExtnChck(this)) return;      // fileExtnChck

                const url: string = "/file/fileUploadAjax.do";
                const ajaxData: FormData = new FormData(document.getElementById("tinymceImageForm") as HTMLFormElement);
                cF.$ajax.multipart(url, ajaxData, function(res): void {
                    if (cF.util.isNotEmpty(res.message)) cF.ui.swalOrAlert(res.message);
                    if (!res.rslt) return;

                    const fileInfo = res.rsltObj;
                    const imgTag: string = "<img src='" + fileInfo.url + "' data-mce-src='" + fileInfo.url + "' data-originalFileName='" + fileInfo.orgnFileNm + "' >";
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

        morelessFunc: function(): void {
            if (typeof cF.tinymce.sectionCount === "undefined") cF.tinymce.sectionCount = 0;
            const sectionId = "tinymce_section_" + cF.tinymce.sectionCount;
            const sectionContentId = `tinymce_section_content_` + cF.tinymce.sectionCount;
            const toggleId = `tinymce_toggle_` + cF.tinymce.sectionCount;

            const newSection: string = `
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
