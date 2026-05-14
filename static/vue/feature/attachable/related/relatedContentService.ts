/**
 * relatedContentService.ts
 * 관련 글 AJAX 서비스
 *
 * 변경(D): related_content_module.ts 의 saveAjax / deleteAjax / openTarget / searchTargets 를
 *          ES 모듈 named export 로 분리.
 *
 * @author nichefish
 */
declare const cF: any;
declare const Url: any;
declare const Swal: any;

/** 관련 글 출처(source) */
export type RelatedSource = {
    id: number;
    contentType: string;
};

/** 관련 글 등록 페이로드 */
export type RelatedContentPayload = {
    srcId: number;
    srcContentType: string;
    targetId: number;
    targetContentType: string;
    relationType: string;
    reason: string;
};

/** 관련 글 검색 결과 항목 */
export type RelatedTargetItem = {
    id: number;
    contentType: string;
    title: string;
    stdrdDt: string;
    content: string;
};

/**
 * 관련 글 등록 (AJAX)
 * @param {RelatedSource} source - 출처 게시물.
 * @param {RelatedContentPayload} payload - 등록 데이터.
 * @param {() => void} onSuccess - 성공 시 콜백.
 */
export function saveAjax(
    source: RelatedSource,
    payload: RelatedContentPayload,
    onSuccess: () => void
): void {
    const url: string = (cF as any).util.bindUrl((Url as any).RELATEDS, {
        contentType: source.contentType,
        id: source.id,
    });
    (cF as any).ajax.post(url, payload, function(res: any): void {
        (Swal as any).fire({ text: res.message }).then(function(): void {
            if (res.rslt) onSuccess();
        });
    });
}

/**
 * 관련 글 연결 삭제 (AJAX)
 * 기존 deleteAjax() 대응. Swal 확인 후 삭제.
 * @param {number} relatedContentId - 삭제할 관련 글 번호.
 * @param {() => void} onSuccess - 성공 시 콜백.
 */
export function deleteRelated(
    relatedContentId: number,
    onSuccess: () => void
): void {
    if (!Number.isInteger(Number(relatedContentId)) || Number(relatedContentId) <= 0) return;
    (Swal as any).fire({
        text: "관련 글 연결만 제거합니다. 원본 글 자체는 삭제되지 않습니다.",
        showCancelButton: true,
    }).then(function(result: { value: boolean }): void {
        if (!result.value) return;
        const url: string = (cF as any).util.bindUrl((Url as any).RELATED, { relatedContentId });
        (cF as any).$ajax.delete(url, null, function(res: any): void {
            (Swal as any).fire({ text: res.message }).then(function(): void {
                if (res.rslt) onSuccess();
            });
        });
    });
}

/**
 * 관련 글 상세 열기 — dF.JournalEntry 모듈 브리지
 * 기존 openTarget() 대응.
 * @param {string} contentType - 대상 콘텐츠 타입.
 * @param {number} id - 대상 게시물 번호.
 */
export function openTarget(contentType: string, id: number): void {
    if (!Number.isInteger(Number(id)) || Number(id) <= 0) return;
    const entryModule = (window as any).dF?.JournalEntry?.get?.(contentType);
    if (typeof entryModule?.dtlModal === "function") {
        entryModule.dtlModal(id);
        return;
    }
    if (typeof entryModule?.mdfModal === "function") {
        entryModule.mdfModal(id);
        return;
    }
    (Swal as any).fire({ text: "상세 화면을 열 수 없습니다." });
}

/**
 * 대상 유형별 검색 URL 반환
 * @param {string} contentType - 대상 콘텐츠 타입.
 */
export function getSearchUrl(contentType: string): string {
    if (contentType === "JOURNAL_DIARY") return (Url as any).JOURNAL_DIARIES;
    if (contentType === "JOURNAL_DREAM") return (Url as any).JOURNAL_DREAMS;
    return "";
}

/**
 * 연결 대상 글 검색 (AJAX)
 * @param {string} contentType - 검색 대상 콘텐츠 타입.
 * @param {string} keyword - 검색 키워드.
 * @param {(list: RelatedTargetItem[]) => void} callback - 결과 콜백.
 */
export function searchTargets(
    contentType: string,
    keyword: string,
    callback: (list: RelatedTargetItem[]) => void
): void {
    const url: string = getSearchUrl(contentType);
    if (!url) { callback([]); return; }
    const ajaxData: Record<string, any> = { searchKeywords: keyword, pageSize: 8, sort: "DESC" };
    (cF as any).ajax.get(url, ajaxData, function(res: any): void {
        if (!res.rslt) { callback([]); return; }
        const list: RelatedTargetItem[] = (Array.isArray(res.rsltList) ? res.rsltList : [])
            .map(function(item: Record<string, any>): RelatedTargetItem {
                return {
                    id: Number(item?.id ?? 0),
                    contentType: String(item?.contentType ?? contentType),
                    title: String(item?.title ?? "").trim(),
                    stdrdDt: String(item?.stdrdDt ?? "").trim(),
                    content: String(item?.content ?? item?.markdownContent ?? "").trim(),
                };
            })
            .filter(function(item: RelatedTargetItem): boolean {
                return Number.isInteger(item.id) && item.id > 0;
            });
        callback(list);
    });
}