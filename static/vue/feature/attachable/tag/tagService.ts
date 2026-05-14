/**
 * tagService.ts
 * 태그 공통 서비스.
 *
 * 변경(tag 모듈 Vue 전환, Sub-phase A):
 *   - tag_module.ts 의 순수 함수(`groupTagsByCategory`)와 DOM 조작 함수
 *     (`hideSingleTag`, `filter`, `resetFilter`)를 ES 모듈로 추출한다.
 *   - `ctgrFilterAll`, `ctgrFilter` 는 호출자 없는 죽은 코드이므로 이관하지 않는다.
 *   - 글로벌 등록(`dF.TagService`)은 Sub-phase C에서 추가한다.
 *
 * @author nichefish
 */

declare const cF: any;

/**
 * 태그 목록을 카테고리별로 그룹화한다.
 * 변경 전: dF.Tag.groupTagsByCategory
 *
 * @param {object[]} tagList - 태그 목록.
 * @returns {Record<string, any[]>} 카테고리 키 → 태그 배열 맵. 'Unknown' 카테고리는 마지막에 정렬.
 */
export function groupTagsByCategory(tagList: Record<string, any>[]): Record<string, any[]> {
    const groupedTags = tagList.reduce((acc: Record<string, any[]>, tag: Record<string, any>) => {
        const category: string = tag.ctgr || '';
        if (!acc[category]) {
            acc[category] = [];
        }
        acc[category].push(tag);
        return acc;
    }, {});

    return Object.keys(groupedTags)
        .sort((a: string, b: string) => {
            if (a === 'Unknown') return 1;
            if (b === 'Unknown') return -1;
            return a.localeCompare(b);
        })
        .reduce((acc: Record<string, any[]>, key: string) => {
            acc[key] = groupedTags[key];
            return acc;
        }, {});
}

/**
 * 글 1개짜리 태그를 토글하여 숨기거나 표시한다.
 * 변경 전: dF.Tag.hideSingleTag — jQuery `$(selectorDiv + " span.ts-1").parent().toggle()`.
 *          vanilla JS로 대체.
 *
 * @param {string} selectorDiv - 범위를 제한할 CSS 셀렉터 (예: '#journal_tag_list_div').
 */
export function hideSingleTag(selectorDiv: string): void {
    document.querySelectorAll(selectorDiv + " span.ts-1").forEach((el: Element) => {
        const parent = el.parentElement;
        if (!parent) return;
        parent.style.display = parent.style.display === "none" ? "" : "none";
    });
}

/**
 * 태그로 글 목록을 필터링한다.
 * 변경 전: dF.Tag.filter
 *
 * @param {string|number} id - 필터링할 태그 ID. 빈 문자열("")이면 필터 해제.
 */
export function filter(id: string | number): void {
    if (isNaN(Number(id))) return;

    const pageNoElement = document.querySelector("#listForm #pageNo") as HTMLInputElement | null;
    const tagsElement = document.querySelector("#listForm #tags") as HTMLInputElement | null;
    if (!pageNoElement || !tagsElement) return;

    pageNoElement.value = "1";
    tagsElement.value = String(id);

    const listForm = document.getElementById("listForm") as HTMLElement | null;
    const listUrl: string = listForm?.dataset.url ?? "";
    const url: string = `${listUrl}?actionTyCd=SEARCH`;
    (cF as any).form.blockUISubmit("#listForm", url);
}

/**
 * 글 목록 태그 필터링을 초기화한다.
 * 변경 전: dF.Tag.resetFilter
 */
export function resetFilter(): void {
    filter("");
}

const tagService = { groupTagsByCategory, hideSingleTag, filter, resetFilter };
export default tagService;

/**
 * 글로벌 노출. ftlh onclick 핸들러가 dF.TagService.<method>(...) 로 호출 가능하도록 등록한다.
 * 변경(tag 모듈 Vue 전환, Sub-phase C): tag_module.js 레이아웃 교체 시점에 추가.
 */
(function registerOnDf(): void {
    const w = window as any;
    if (w.dF == null) w.dF = {};
    w.dF.TagService = tagService;
})();