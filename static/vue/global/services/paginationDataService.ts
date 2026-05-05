export type GlobalPaginationState = {
    currPageNo: number;
    lastPageNo: number;
    totalCnt: number;
    pageSize: number;
    isFirstPage: boolean;
    isLastPage: boolean;
    prevPageNo: number;
    nextPageNo: number;
};

/**
 * 페이지에 주입된 pagination JSON(script tag)을 읽어 PaginationState에 반영한다.
 *
 * @param dataElementId JSON이 담긴 script element id
 * @param pagination 반영 대상 pagination 상태 객체
 * @param logPrefix 오류 로그 prefix
 */
export function applyPaginationFromPageData(
    dataElementId: string,
    pagination: GlobalPaginationState,
    logPrefix: string
): void {
    const dataEl: HTMLElement | null = document.getElementById(dataElementId);
    if (!dataEl)
        return;

    try {
        const parsed: any = JSON.parse(dataEl.textContent || "{}");
        pagination.currPageNo = Number(parsed.currPageNo || 1);
        pagination.lastPageNo = Number(parsed.lastPageNo || 1);
        pagination.totalCnt = Number(parsed.totalCnt || 0);
        pagination.pageSize = Number(parsed.pageSize || 10);
        pagination.isFirstPage = !!parsed.isFirstPage;
        pagination.isLastPage = !!parsed.isLastPage;
        pagination.prevPageNo = Number(parsed.prevPageNo || 0);
        pagination.nextPageNo = Number(parsed.nextPageNo || 0);
    } catch (e) {
        console.error(`${logPrefix} ${dataElementId} parse failed`, e);
    }
}
