/**
 * lifecycleService.ts
 * 부착 가능 컨텐츠의 단일 현재 라이프사이클 처리 서비스
 *
 * 변경(D): lifecycle_module.ts (dF.Lifecycle 글로벌 네임스페이스)를 ES 모듈로 전환.
 *          - resolveJournalCacheContext: dF.State.resolveJournalCacheContext 위임 유지.
 *          - setAjax: cF.$ajax.put 직접 호출로 전환.
 *
 * @author nichefish
 */
import * as stateService from "../state/stateService.js";

declare const cF: any;
declare const Url: any;

/**
 * 라이프사이클 변경에 필요한 저널 월간/주간 캐시 컨텍스트를 계산한다.
 *
 * 라이프사이클은 state와 같은 저널 캐시 주소 체계를 사용한다. 캐시 컨텍스트 helper가
 * 부착 가능 컨텐츠 공통으로 완전히 이동하기 전까지는 state 모듈의 helper를 재사용한다.
 *
 * @param {HTMLElement|null} [item] - 날짜 메타데이터를 가진 journal item 엘리먼트.
 * @returns {Record<string, any>} 백엔드 캐시 updater가 사용할 캐시 컨텍스트.
 */
export function resolveJournalCacheContext(item?: HTMLElement | null): Record<string, any> {
    return stateService.resolveJournalCacheContext(item);
}

/**
 * 부착 가능 컨텐츠 하나의 단일 현재 라이프사이클 값을 설정한다.
 *
 * @param {{ id: string|number, contentType: string, lifecycleKey: string, cacheContext?: Record<string, any> }} payload - 요청 파라미터.
 * @param {Function} [callback] - ajax 응답을 받을 선택 callback.
 */
export function setAjax(
    payload: { id: string | number; contentType: string; lifecycleKey: string; cacheContext?: Record<string, any> },
    callback?: Function
): void {
    const url: string = (Url as any).LIFECYCLES;
    (cF as any).$ajax.put(url, payload, function(res: any): void {
        if (!res.rslt) {
            if ((cF as any).util.isNotEmpty(res.message)) return (cF as any).ui.swalOrAlert(res.message);
        }
        if (typeof callback === "function") callback?.(res);
    }, "block");
}