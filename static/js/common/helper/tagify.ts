/**
 * tagify.ts
 * 공통 - tagify 관련 함수 모듈
 *
 * @namespace: cF.tagify (노출식 모듈 패턴)
 * @author: nichefish
 */
// @ts-ignore
if (typeof cF === 'undefined') { var cF = {} as any; }
cF.tagify = (function(): Module {
    const tagTemplate = function (tagData: any): string {
        // 태그 메타데이터 (data)를 문자열로 변환하여 표시
        const ctgr: string = cF.util.isNotEmpty(tagData.data) ? tagData.data.ctgr : "";
        const ctgrSpan: string = ctgr !== "" ? `<span class="tagify__tag-category text-noti me-1">[${tagData.data.ctgr}]</span>` : "";
        return `<tag title="${tagData.value}" contenteditable="false" spellcheck="false" tabindex="-1"
                             class="tagify__tag" value="${tagData.value}" data-ctgr="${ctgr}">
                    <x title="" class="tagify__tag__removeBtn" role="button" aria-label="remove tag"></x>
                    <div>
                        <!-- 메타데이터 시각화 -->
                        ${ctgrSpan}
                        <span class="tagify__tag-text">${tagData.value}</span>
                    </div>
                </tag>`;
    };
    const metaTemplate = function (tagData: any): string {
        // 태그 메타데이터 (data)를 문자열로 변환하여 표시
        const ctgr: string = cF.util.isNotEmpty(tagData.data) ? tagData.data.ctgr : "";
        const ctgrSpan: string = ctgr !== "" ? `<span class="tagify__tag-category text-noti me-1">[${tagData.data.ctgr}]</span>` : "";
        const meta: string = cF.util.isNotEmpty(tagData.data) ? tagData.data.value : "";
        const idx: number = meta.indexOf(":");
        const label: string = idx !== -1 ? meta.slice(0, idx) : "";
        const value: string = idx !== -1 ? meta.slice(idx + 1) : meta;
        const labelSpan: string  = cF.util.isNotEmpty(label) ? `<span class="tagify__tag-meta mx-1"> - ${label}</span>` : "";
        const metaSpan: string = cF.util.isNotEmpty(meta) ? `<span class="text-dialog">: ${value}</span>` : "";
        return `<tag title="${tagData.value}" contenteditable="false" spellcheck="false" tabindex="-1"
                             class="tagify__tag" value="${tagData.value}" data-ctgr="${ctgr}" data-value="${meta}">
                    <x title="" class="tagify__tag__removeBtn" role="button" aria-label="remove tag"></x>
                    <div>
                        <!-- 메타데이터 시각화 -->                        
                        ${ctgrSpan}
                        <span class="tagify__tag-text">${tagData.value}</span>
                        ${labelSpan}${metaSpan}
                    </div>
                </tag>`;
    };

    // @ts-ignore
    /** 기본 옵션 분리 */
    const baseOptions: Record<string, any> = {
        whitelist: [],
        maxTags: 21,
        keepInvalidTags: false,
        skipInvalid: true,
        // duplicate 허용하고 수동 로직으로 중복 처리
        duplicates: false,
        editTags: {
            clicks: 2,
            // if after editing, tag is invalid, auto-revert
            keepInvalid: false
        },
        // 공백 제거
        transformTag(tagData: any): any {
            tagData.value = tagData.value.replace(/\s+/g, '_');
            return tagData;
        },
        // 태그 표시
        templates: { tag: tagTemplate }
    };

    return {
        /**
         * Tagify를 초기화합니다.
         * @param {string} selector - 초기화할 태그 입력 요소의 선택자 문자열.
         * @param {Record<string, any>} additionalOptions - 추가로 적용할 `Tagify` 설정 옵션 (선택적).
         * @returns {Tagify} - 초기화된 Tagify 인스턴스.
         */
        init: function(selector: string, additionalOptions: Record<string, any> = {}): Tagify {
            // 태그 tagify
            const inputs: HTMLElement[] = cF.util.verifySelector(selector);
            if (inputs.length === 0) return;

            const tagInput: HTMLElement = inputs[0];

            // 기본 옵션과 추가 옵션을 병합하여 Tagify 생성
            const mergedOptions: Record<string, any> = {
                ...baseOptions,
                ...additionalOptions
            };

            return new Tagify(tagInput, mergedOptions);
        },

        /**
         * 카테고리 기능을 추가하여 Tagify를 초기화합니다.
         * @param {string} selector - 초기화할 태그 입력 요소의 선택자 문자열.
         * @param {Record<string, any>} ctgrMap - 태그 카테고리 매핑 객체. 태그와 관련된 카테고리를 정의합니다.
         * @param {Record<string, any>} additionalOptions - 추가로 적용할 `Tagify` 설정 옵션 (선택적).
         * @returns {Tagify} - 초기화된 Tagify 인스턴스. 카테고리 기능이 추가된 상태입니다.
         */
        initWithCtgr: function(selector: string, ctgrMap: Record<string, any>, additionalOptions: Record<string, any> = {}): Tagify {
            const tagify: Tagify = cF.tagify.init(selector, additionalOptions);

            // tagify 스코프 설정
            const parts: string[] = selector.split(' ');
            tagify.scope = (parts.length > 1) ? document.querySelector(parts[0]) : document;
            tagify.ctgr = {
                selectContainer: tagify.scope.querySelector('#tag_ctgr_select_div'),
                select: tagify.scope.querySelector('#tag_ctgr_select'),
                displayContainer: tagify.scope.querySelector('#tag_display_div'),
                display: tagify.scope.querySelector('#tag_display'),
                inputContainer: tagify.scope.querySelector('#tag_ctgr_div'),
                input: tagify.scope.querySelector('#tag_ctgr')
            }
            // 추가 대기중인 값
            tagify.draft = { value: null, ctgr: null, meta: null };
            // 수동 중복 체크 위해 중복 제한 제거
            tagify.settings.duplicates = true;

            // 태그 자동완성 :: 메소드 분리
            cF.tagify._setAutoComplete(tagify, ctgrMap);
            // 태그 추가시 카테고리 입력 칸 prompt :: 메소드 분리
            cF.tagify._setCtgrInputPrompt(tagify, ctgrMap, { hasValueInput: false });
            // 카테고리 입력칸에 이벤트리스너 추가 (ESC 또는 탭) :: 메소드 분리
            cF.tagify._setCtgrKeyListener(tagify, { hasValueInput: false });

            return tagify;
        },

        /**
         * 카테고리 기능을 추가하여 Tagify를 초기화합니다.
         * @param {string} selector - 초기화할 태그 입력 요소의 선택자 문자열.
         * @param {Record<string, any>} ctgrMap - 태그 카테고리 매핑 객체. 태그와 관련된 카테고리를 정의합니다.
         * @param {Record<string, any>} additionalOptions - 추가로 적용할 `Tagify` 설정 옵션 (선택적).
         * @returns {Tagify} - 초기화된 Tagify 인스턴스. 카테고리 기능이 추가된 상태입니다.
         */
        initMeta: function(selector: string, ctgrMap: Record<string, any>, additionalOptions: Record<string, any> = {}): Tagify {
            const tagify: Tagify = cF.tagify.init(selector, { ...additionalOptions, templates: { tag: metaTemplate } });

            // tagify 스코프 설정
            const parts: string[] = selector.split(' ');
            tagify.scope = (parts.length > 1) ? document.querySelector(parts[0]) : document;
            tagify.ctgr = {
                selectContainer: tagify.scope.querySelector('#meta_ctgr_select_div'),
                select: tagify.scope.querySelector('#meta_ctgr_select'),
                displayContainer: tagify.scope.querySelector('#meta_display_div'),
                display: tagify.scope.querySelector('#meta_display'),
                inputContainer: tagify.scope.querySelector('#meta_ctgr_div'),
                input: tagify.scope.querySelector('#meta_ctgr')
            }
            tagify.meta = {
                inputContainer: tagify.scope.querySelector('#meta_value_div'),
                input: tagify.scope.querySelector('#meta_value')
            }
            // 추가 대기중인 값
            tagify.draft = { value: null, ctgr: null, meta: null };
            // 수동 중복 체크 위해 중복 제한 제거
            tagify.settings.duplicates = true;

            // 메타 자동완성 :: 메소드 분리
            cF.tagify._setAutoComplete(tagify, ctgrMap);
            // 메타 추가시 카테고리 입력 칸 prompt :: 메소드 분리
            cF.tagify._setCtgrInputPrompt(tagify, ctgrMap, { hasValueInput: true });
            // 카테고리 입력칸에 이벤트리스너 추가 (ESC 또는 탭) :: 메소드 분리
            cF.tagify._setCtgrKeyListener(tagify, { hasValueInput: true });
            // 값 입력칸에 이벤트리스너 추가 (ESC 또는 탭) :: 메소드 분리
            cF.tagify._setValueKeyListener(tagify);

            return tagify;
        },

        /**
         * _내부함수 : 태그 자동완성을 설정합니다.
         * @param {Tagify} tagify - Tagify 인스턴스. 자동완성을 적용할 태그 입력 요소입니다.
         * @param {Record<string, any>} ctgrMap - 태그 카테고리 매핑 객체. 태그와 관련된 카테고리를 정의합니다.
         */
        _setAutoComplete: function(tagify: Tagify, ctgrMap: Record<string, any>): void {
            if (!ctgrMap) return;

            tagify.on("input", function(e: CustomEvent): void {
                const value: string = e.detail.value;
                tagify.settings.whitelist = Object.keys(ctgrMap).filter(tag => tag.startsWith(value));
                tagify.dropdown.show(value);
            });
        },

        /**
         * _내부함수 : 태그 추가 시 카테고리 입력 칸을 프롬프트합니다.
         * @param {Tagify} tagify - Tagify 인스턴스. 카테고리 입력을 위한 태그 입력 요소입니다.
         * @param {Object} ctgrMap - 태그 카테고리 매핑 객체. 태그와 관련된 카테고리를 정의합니다.
         * @param {Object} [options={}] - 옵션 객체
         * @param {boolean} [options.hasValueInput=false] - 값 입력 존재 여부
         */
        _setCtgrInputPrompt: function(tagify: Tagify, ctgrMap: Record<string, any>, options: { hasValueInput?: boolean; } = { hasValueInput: false }): void {
            const { hasValueInput } = options;
            tagify.committing = false;

            tagify.on("add", function(e: CustomEvent): void {
                // 도메인 확정 add. 추가 작업 불필요
                if (tagify.committing) {
                    tagify.committing = false;
                    return;
                }

                // 나머지는 전부 임시 add
                const addedTag: any = e.detail.data;    // 이미 추가된 태그
                tagify.draft.value = addedTag.value;     // draft
                tagify.ctgr.input.value = '';
                cF.tagify._toggle(tagify.ctgr?.displayContainer, true);
                tagify.ctgr.display.value = tagify.draft.value;
                cF.tagify._showAndFocus(tagify.ctgr?.inputContainer, tagify.ctgr?.input);
                if (hasValueInput) cF.tagify._toggle(tagify.meta?.inputContainer, true);

                // 2. 카테고리 맵 정의시: selectbox 세팅
                cF.tagify._toggle(tagify.ctgr?.selectContainer, false);
                if (!ctgrMap) return tagify.removeTags(e.detail.tag);
                const predefinedCtgr: any = ctgrMap[tagify.draft.value];
                const filteredCtgr: any = predefinedCtgr ? predefinedCtgr.filter((item: any): any => item) : [];
                if (filteredCtgr.length === 0) return tagify.removeTags(e.detail.tag);
                // selectbox 초기화 및 선택/직접입력 표시
                tagify.ctgr.select.innerHTML = '<option value="custom">직접입력</option>' + filteredCtgr.map(item => '<option value="' + item + '">' + item + '</option>').join('');
                tagify.ctgr.select.size = filteredCtgr.length + 1;
                cF.tagify._toggle(tagify.ctgr?.selectContainer, true);
                // 자동완성 선택 이벤트 핸들러
                tagify.ctgr.select.onchange = function(): void {
                    tagify.draft.ctgr = tagify.ctgr.select.value;
                    if (tagify.draft.ctgr === "custom") {
                        // 직접입력 선택 시 입력 필드로 포커스 이동
                        tagify.ctgr.input.value = '';
                        cF.tagify._showAndFocus(tagify.ctgr?.inputContainer, tagify.ctgr?.input);
                    } else {
                        if (hasValueInput) {
                            tagify.ctgr.input.value = tagify.draft.ctgr;
                            tagify.meta.input.value = '';
                            cF.tagify._showAndFocus(tagify.meta?.inputContainer, tagify.meta?.input);
                        } else {
                            if (!tagify.draft.value || !tagify.draft.ctgr) {
                            cF.tagify._cancelInput(tagify);
                            return;
}
                            cF.tagify.commitTag(tagify, tagify.draft.value, tagify.draft.ctgr, null);
                        }
                    }
                };

                tagify.removeTags(e.detail.tag);
            });
        },

        /**
         *
         * @param {Tagify} tagify
         * @param {string} value
         * @param {string} ctgr
         * @param {string|number} meta
         */
        commitTag: function(tagify, value, ctgr, meta) {
            tagify.committing = true;
            tagify.addTags([{ value, data: { ctgr, value: meta } }]);
            cF.tagify._toggle(tagify.ctgr?.selectContainer, false);
            cF.tagify._toggle(tagify.ctgr?.displayContainer, false);
            cF.tagify._toggle(tagify.ctgr?.inputContainer, false);
            cF.tagify._toggle(tagify.meta?.inputContainer, false);
            // draft 초기화
            tagify.draft = { value: null, ctgr: null, meta: null };
            cF.tagify._focusTagInput(tagify);
        },

        /**
         * _내부 함수: 키 리스너를 설정합니다.
         * @param {Tagify} tagify - Tagify 인스턴스.
         * @param {Object} [options={}] - 옵션 객체
         * @param {boolean} [options.hasValueInput=false] - 값 입력 존재 여부
         */
        _setCtgrKeyListener: function(tagify: Tagify, options = { hasValueInput: false }): void {
            const { hasValueInput } = options;

            tagify.ctgr.input.addEventListener('keydown', function(event: KeyboardEvent): void {
                switch(event.key) {
                    // ESC = 태그 추가 없이 빠져나감. 카테고리, 값 입력 필드 숨김
                    case 'Escape':
                        event.preventDefault();
                        // ESC = 태그 추가 없이 빠져나감, 카테고리, 값 입력 필드 숨김
                        cF.tagify._cancelInput(tagify);
                        return;
                    case 'Tab':
                    case 'Enter':
                        event.preventDefault();
                        // TAB = 빈칸 아닐시 카테고리 추가. 값 항목이 있으면 값 추가
                        tagify.draft.ctgr = tagify.ctgr.input.value;
                        if (hasValueInput) {
                            cF.tagify._showAndFocus(tagify.meta?.inputContainer, tagify.meta.input);
                            return;
                        }

                        // 새 태그 추가
                        const { value, ctgr, meta } = tagify.draft;
                        cF.tagify.commitTag(tagify, value, ctgr, meta);
                }
            });
        },

        /**
         * _내부 함수: 키 리스너를 설정합니다.
         * @param {Tagify} tagify - Tagify 인스턴스.
         */
        _setValueKeyListener: function(tagify: Tagify): void {
            tagify.meta.input.addEventListener('keydown', function(event: KeyboardEvent): void {
                switch(event.key) {
                    // ESC = 태그 추가 없이 빠져나감. 카테고리, 값 입력 필드 숨김
                    case 'Escape': {
                        event.preventDefault();
                        // ESC = 태그 추가 없이 빠져나감, 카테고리, 값 입력 필드 숨김
                        cF.tagify._cancelInput(tagify);
                        return;
                    }
                    case 'Tab':
                    case 'Enter': {
                        event.preventDefault();
                        // TAB = 빈칸 아닐시 태그 추가
                        tagify.draft.meta = tagify.meta.input.value;
                        const { value, ctgr, meta } = tagify.draft;
                        if (cF.util.isEmpty(meta)) cF.tagify._cancelInput(tagify);
                        
                        // 새 태그 추가
                        cF.tagify.commitTag(tagify, value, ctgr, meta);
                        // 메타 입력 필드 숨김
                        cF.tagify._toggle(tagify.ctgr?.selectContainer, false);
                        cF.tagify._toggle(tagify.ctgr?.displayContainer, false);
                        cF.tagify._toggle(tagify.ctgr?.inputContainer, false);
                        cF.tagify._toggle(tagify.meta?.inputContainer, false);
                    }
                }
            });
        },

        /**
         * Tagify 기본 입력창으로 포커스를 이동합니다.
         * setTimeout을 사용하여 DOM 갱신 이후 안전하게 포커싱합니다.
         *
         * @param {Tagify} tagify - Tagify 인스턴스
         */
        _focusTagInput: function(tagify: Tagify) {
            setTimeout((): any => tagify.DOM.input?.focus(), 0);
        },

        /**
         * 지정한 엘리먼트의 display 상태를 토글합니다.
         *
         * @param {HTMLElement|null} el - 대상 엘리먼트
         * @param {boolean} show - true: 표시, false: 숨김
         */
        _toggle(el: HTMLElement | null, show: boolean): void {
            if (!el) return;
            el.style.display = show ? 'block' : 'none';
        },

        /**
         * 컨테이너를 표시하고, 지정된 입력 엘리먼트로 포커스를 이동합니다.
         *
         * @param {HTMLElement|null} container - 표시할 컨테이너
         * @param {HTMLElement|null} [el] - 포커스 대상 입력 엘리먼트
         */
        _showAndFocus(container: HTMLElement|null, el?: HTMLElement|null): void {
            if (!container) return;
            container.style.display = 'block';
            setTimeout(() => el.focus?.(), 0);
        },

        /**
         * 카테고리/메타 입력 과정을 취소하고 기본 태그 입력 상태로 복귀합니다. (ESC 키 처리의 공통 로직으로 사용됩니다.)
         * 수행 동작:
         * - 카테고리 입력 컨테이너 숨김
         * - 메타 값 입력 컨테이너 숨김
         * - 마킹된 임시 태그 상태 정리
         * - Tagify 기본 입력창으로 포커스 이동
         *
         * @param {Tagify} tagify - Tagify 인스턴스
         */
        _cancelInput: function(tagify: Tagify): void {
            cF.tagify._toggle(tagify.ctgr?.displayContainer, false);
            cF.tagify._toggle(tagify.ctgr?.inputContainer, false);
            cF.tagify._toggle(tagify.meta?.inputContainer, false);

            // draft 초기화
            tagify.draft = { value: null, ctgr: null, meta: null };
            // 태그 인풋으로 포커싱 이동
            cF.tagify._focusTagInput(tagify);
        }
    }
})();