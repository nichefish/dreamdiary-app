                                      /**
 * tagify.ts
 * @namespace: cF.taf
 * @author: nichefish
 * @dependency: jquery.blockUI.js, jquery.forms.js
 * 공통 - tagify(라이브러리) 관련 함수 모듈
 * (노출식 모듈 패턴 적용 :: cF.util.enterKey("#userId") 이런식으로 사용)
 */
if (typeof cF === 'undefined') { let cF = {}; }
cF.tagify = (function() {

    /** 기본 옵션 분리 */
    const baseOptions = {
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
        templates: {
            tag: function (tagData: object) {
                // 태그 메타데이터 (data)를 문자열로 변환하여 표시
                const ctgr = cF.util.isNotEmpty(tagData.data) ? tagData.data.ctgr : "";
                const ctgrSpan = ctgr !== "" ? `<span class="tagify__tag-category text-noti me-1">[${tagData.data.ctgr}]</span>` : "";
                return `<tag title="${tagData.value}" contenteditable="false" spellcheck="false" tabindex="-1"
                                     class="tagify__tag" value="${tagData.value}" data-ctgr="${ctgr}">
                                    <x title="" class="tagify__tag__removeBtn" role="button" aria-label="remove tag"></x>
                                    <div>
                                        <!-- 메타데이터 시각화 -->
                                        ${ctgrSpan}
                                        <span class="tagify__tag-text">${tagData.value}</span>
                                    </div>
                                </tag>`;
            }
        },
    };

    return {
        /**
         * Tagify를 초기화합니다.
         * @param {string} selector - 초기화할 태그 입력 요소의 선택자 문자열.
         * @param {object} additionalOptions - 추가로 적용할 `Tagify` 설정 옵션 (선택적).
         * @returns {Tagify} - 초기화된 Tagify 인스턴스.
         */
        init: function(selector: string, additionalOptions = {}) {
            // 태그 tagify
            const inputs = cF.util.verifySelector(selector);
            if (inputs.length === 0) return;

            const tagInput = inputs[0];

            // 기본 옵션과 추가 옵션을 병합하여 Tagify 생성
            const mergedOptions = {
                ...baseOptions,
                ...additionalOptions
            };
            return new Tagify(tagInput, mergedOptions);
        },

        /**
         * 카테고리 기능을 추가하여 Tagify를 초기화합니다.
         * @param {string} selector - 초기화할 태그 입력 요소의 선택자 문자열.
         * @param {Object} tagCtgrMap - 태그 카테고리 매핑 객체. 태그와 관련된 카테고리를 정의합니다.
         * @param {object} additionalOptions - 추가로 적용할 `Tagify` 설정 옵션 (선택적).
         * @returns {Tagify} - 초기화된 Tagify 인스턴스. 카테고리 기능이 추가된 상태입니다.
         */
        initWithCtgr: function(selector: string, tagCtgrMap, additionalOptions = {}) {
            const tagify = cF.tagify.init(selector, additionalOptions);

            // tagify 스코프 설정
            const parts = selector.split(' ');
            tagify.scope = (parts.length > 1) ? document.querySelector(parts[0]) : document;
            tagify.categoryInputContainer = tagify.scope.querySelector('#tag_ctgr_div');
            tagify.tagCtgrInput = tagify.scope.querySelector('#tag_ctgr');
            if (!tagify.categoryInputContainer || !tagify.tagCtgrInput) return tagify;

            // 수동 중복 체크 위해 중복 제한 제거
            tagify.settings.duplicates = true;

            // tagify 컨테이너 세팅
            tagify.metaInfoContainer = tagify.scope.querySelector('#tag_ctgr_select_div');
            tagify.metaInfoSelect = tagify.scope.querySelector('#tag_ctgr_select');

            // 태그 자동완성 :: 메소드 분리
            cF.tagify._setAutoComplete(tagify, tagCtgrMap);
            // 태그 추가시 카테고리 입력 칸 prompt :: 메소드 분리
            cF.tagify._setCtgrInputPrompt(tagify, tagCtgrMap);
            // 카테고리 입력칸에 이벤트리스너 추가 (ESC 또는 탭) :: 메소드 분리
            cF.tagify._setKeyListener(tagify);
            // 추가된 태그가 제거될 때 ctgr 관련 숨기기 :: 메소드 분리
            cF.tagify._hideCtgrDiv(tagify);

            return tagify;
        },

        /**
         * _내부함수 : 태그 자동완성을 설정합니다.
         * @param {Tagify} tagify - Tagify 인스턴스. 자동완성을 적용할 태그 입력 요소입니다.
         * @param {Object} tagCtgrMap - 태그 카테고리 매핑 객체. 태그와 관련된 카테고리를 정의합니다.
         */
        _setAutoComplete: function(tagify, tagCtgrMap) {
            if (!tagCtgrMap) return;
            tagify.on("input", function(e) {
                const value = e.detail.value;
                tagify.settings.whitelist = Object.keys(tagCtgrMap).filter(tag => tag.startsWith(value));
                tagify.dropdown.show(value);
            });
        },

        /**
         * _내부함수 : 태그 추가 시 카테고리 입력 칸을 프롬프트합니다.
         * @param {Tagify} tagify - Tagify 인스턴스. 카테고리 입력을 위한 태그 입력 요소입니다.
         * @param {Object} tagCtgrMap - 태그 카테고리 매핑 객체. 태그와 관련된 카테고리를 정의합니다.
         */
        _setCtgrInputPrompt: function(tagify, tagCtgrMap) {
            tagify.on("add", function(e) {
                // 기본 태그 (카테고리 붙이기 전) 처리시에만 동작
                const newTag = e.detail.data;
                if (newTag.data !== undefined) {
                    // 태그에서 마킹 정보 클리어 :: 메소드 분리
                    cF.tagify._clearMarks(tagify);
                    return;
                }

                // setTimeout을 사용하여 포커스 호출 지연
                setTimeout(() => {
                    // 1. 마지막 추가된 태그 임시 마킹
                    const addedTagElmt = tagify.getTagElms().slice(-1)[0];
                    addedTagElmt.setAttribute('data-marked', 'true');  // 마킹

                    // 2. 카테고리 입력 칸 prompt
                    const newValue = newTag.value;
                    tagify.categoryInputContainer.style.display = 'block';
                    tagify.tagCtgrInput.value = '';
                    tagify.tagCtgrInput.dataset.tagValue = newValue;
                    tagify.tagCtgrInput.focus();

                    // 3. 카테고리 맵 정의시: selectbox 세팅
                    tagify.metaInfoContainer.style.display = 'none';
                    if (!tagCtgrMap) return;
                    const predefinedCtgr = tagCtgrMap[newValue];
                    const filteredCtgr = predefinedCtgr ? predefinedCtgr.filter(item => item) : [];
                    if (filteredCtgr.length === 0) return;

                    // 초기화 및 직접입력 추가
                    tagify.metaInfoSelect.innerHTML = '<option value="custom">직접입력</option>' + filteredCtgr.map(item => '<option value="' + item + '">' + item + '</option>').join('');

                    // 메타정보 컨테이너 표시
                    tagify.metaInfoSelect.size = filteredCtgr.length + 1;
                    tagify.metaInfoContainer.style.display = 'block';

                    // 자동완성 선택 이벤트 핸들러
                    tagify.metaInfoSelect.onchange = function() {
                        const selectedCtgr = tagify.metaInfoSelect.value;
                        if (selectedCtgr !== "custom") {
                            tagify.removeTags(addedTagElmt);
                            tagify.addTags([{ "value": newValue, "data": { "ctgr": selectedCtgr } }]);
                            tagify.metaInfoContainer.style.display = 'none';  // 선택 후 컨테이너 숨김
                        } else {
                            // 직접입력 선택 시 입력 필드로 포커스 이동
                            tagify.categoryInputContainer.style.display = 'block';
                            tagify.tagCtgrInput.value = '';
                            tagify.tagCtgrInput.dataset.tagValue = newValue;
                            tagify.tagCtgrInput.focus();
                        }
                    };
                }, 0);
            });
        },

        /**
         * 태그에서 마킹 정보 클리어
         */
        _clearMarks: function(tagify) {
            const markedTags = tagify.scope.querySelectorAll('[data-marked="true"]');
            markedTags.forEach(tagElmt => {
                tagElmt.removeAttribute('data-marked');
            });
        },

        /**
         * _내부 함수: 키 리스너를 설정합니다.
         * @param {Tagify} tagify - Tagify 인스턴스.
         */
        _setKeyListener: function(tagify) {
            tagify.tagCtgrInput.addEventListener('keydown', function(event) {
                tagify.metaInfoContainer.style.display = 'none';
                const markedTags = tagify.scope.querySelectorAll('[data-marked="true"]');

                if (event.key === 'Escape') {
                    event.preventDefault();
                    // ESC = 태그 추가 없이 빠져나감
                    // 카테고리 입력 필드 숨김
                    tagify.categoryInputContainer.style.display = 'none';
                    // 태그에서 마킹 정보 클리어 :: 메소드 분리
                    cF.tagify._clearMarks(tagify);
                    // 태그 인풋으로 포커싱 이동
                    setTimeout(() => {
                        if (tagify.DOM.input) tagify.DOM.input.focus();
                    }, 0);
                } else if (event.key === 'Tab') {
                    event.preventDefault();
                    // TAB = 빈칸 아닐시 카테고리 추가
                    const newCtgr = tagify.tagCtgrInput.value;
                    const newValue = tagify.tagCtgrInput.dataset.tagValue;

                    // 중복 체크 및 마킹 태그 제거
                    // (카테고리 미추가헐 경우 마킹한 임시태그는 이 과정에서 자연스레 없어짐.)
                    const tags = tagify.getTagElms(); // 모든 태그 DOM 요소 가져오기
                    Array.from(tags).forEach(existingTagElmt => {
                        const existingTagData = tagify.getSetTagData(existingTagElmt);
                        if (existingTagData.value !== newValue) return;
                        const existingCtgr = existingTagData.data ? existingTagData.data.ctgr : "";
                        if (existingCtgr === newCtgr) tagify.removeTags(existingTagElmt);  // 마킹된 태그 제거
                    });
                    // 새 태그 추가
                    tagify.addTags([{ "value": newValue, "data": { "ctgr": newCtgr } }]);
                    // 카테고리 입력 필드 숨김
                    tagify.categoryInputContainer.style.display = 'none';
                    // 카테고리 붙인 걍우 임시 태그를 찾아 제거
                    markedTags.forEach(tagElmt => {
                        if (newCtgr) tagify.removeTags(tagElmt);  // 마킹된 태그 제거
                    });
                    // 태그에서 마킹 정보 클리어 :: 메소드 분리
                    cF.tagify._clearMarks(tagify);
                    // 태그 인풋으로 포커싱 이동
                    setTimeout(() => {
                        if (tagify.DOM.input) tagify.DOM.input.focus();
                    }, 0);
                }
            });
        },

        /**
         * _내부 함수: 카테고리 입력 칸을 숨깁니다.
         * @param {Tagify} tagify - Tagify 인스턴스.
         */
        _hideCtgrDiv: function(tagify) {
            tagify.on("remove", function() {
                tagify.metaInfoContainer.style.display = 'none';
                tagify.categoryInputContainer.style.display = 'none';
                // 태그에서 마킹 정보 클리어 :: 메소드 분리
                cF.tagify._clearMarks(tagify);
            });
        },
    }
})();
