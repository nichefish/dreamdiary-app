/**
 * stateService.ts
 * 부착 가능 컨텐츠 상태 서비스
 *
 * 변경(D): state_module.ts (dF.State 글로벌 네임스페이스)를 ES 모듈로 전환.
 *          - resolveJournalCacheContext: 동일 로직을 named export 로 직접 제공.
 *          - toggleAjax: cF.$ajax.postJson 직접 호출로 전환.
 *          - init(): 초기화 플래그 세팅만 하는 no-op 이므로 마이그레이션 시 제거.
 *
 * @author nichefish
 */
import { getJournalDayListBridge } from "../../journal/day/journalDayListBridge.js";

declare const cF: any;
declare const Url: any;

/**
 * 라이프사이클·상태 변경에 필요한 저널 월간/주간 캐시 컨텍스트를 계산한다.
 *
 * @param {HTMLElement|null} [item] - 날짜 메타데이터를 가진 journal item 엘리먼트.
 * @returns {Record<string, any>} 백엔드 캐시 updater 가 사용할 캐시 컨텍스트.
 */
export function resolveJournalCacheContext(item?: HTMLElement | null): Record<string, any> {
    const itemYy: string = item?.dataset?.yy ?? "";
    const itemMnth: string = item?.dataset?.mnth ?? "";
    const dayElement: HTMLElement = item?.closest?.(".journal-day") as HTMLElement;
    const stdrdDt: string = item?.dataset?.stdrdDt
        ?? dayElement?.dataset?.stdrdDt
        ?? (cF as any).util.getUrlParam("stdrdDt")
        ?? "";

    const resolvedYy: string = (cF as any).util.isNotEmpty(itemYy)
        ? itemYy
        : ((cF as any).util.isNotEmpty(stdrdDt) ? stdrdDt.substring(0, 4) : ((cF as any).util.getUrlParam("yy") ?? ""));
    const resolvedMnth: string = (cF as any).util.isNotEmpty(itemMnth)
        ? itemMnth
        : ((cF as any).util.isNotEmpty(stdrdDt) ? String(parseInt(stdrdDt.substring(5, 7), 10)) : ((cF as any).util.getUrlParam("mnth") ?? ""));

    const weekStartDtFromSearchParams: string = getJournalDayListBridge()?.getSearchParams?.()?.weekStartDt ?? "";
    const weekStartDt: string = (cF as any).util.isNotEmpty(weekStartDtFromSearchParams)
        ? weekStartDtFromSearchParams
        : ((cF as any).util.isNotEmpty(stdrdDt) ? ((cF as any).date.getWeekdayDateStr(stdrdDt, 1, (cF as any).date.ptnDate) ?? "") : "");

    const cacheContext: Record<string, any> = {};
    if ((cF as any).util.isNotEmpty(resolvedYy)) cacheContext.yy = Number(resolvedYy);
    if ((cF as any).util.isNotEmpty(resolvedMnth)) cacheContext.mnth = Number(resolvedMnth);
    if ((cF as any).util.isNotEmpty(weekStartDt)) cacheContext.weekStartDt = weekStartDt;

    return cacheContext;
}

/**
 * 상태 토글 처리 (Ajax).
 *
 * @param {{ id: string|number, contentType: string, stateKey: string, cacheContext?: Record<string, any> }} payload - 요청 파라미터.
 * @param {Function} callback - ajax 응답을 받을 callback.
 */
export function toggleAjax(
    payload: { id: string | number; contentType: string; stateKey: string; cacheContext?: Record<string, any> },
    callback: Function
): void {
    const url: string = (Url as any).STATES;
    (cF as any).$ajax.postJson(url, payload, function(res: any): void {
        if (!res.rslt) {
            if ((cF as any).util.isNotEmpty(res.message)) return (cF as any).ui.swalOrAlert(res.message);
        }
        if (typeof callback === "function") callback?.(res);
    }, "block");
}