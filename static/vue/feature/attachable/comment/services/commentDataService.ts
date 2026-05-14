/**
 * commentDataService.ts
 * 댓글 데이터 조회 서비스
 *
 * 변경(D): comment_page_module.ts / comment_modal_module.ts 의 AJAX 조회 로직을 서비스로 분리.
 *
 * @author nichefish
 */
import type { CommentItem, CommentForm } from "../types.js";

declare const cF: any;
declare const Url: any;

export default {
    /**
     * 댓글 목록 조회 (AJAX) — 모달 목록 조회용
     * @param {string|number} refId - 참조 게시물 번호.
     * @param {string} refContentType - 참조 콘텐츠 타입.
     * @param {string} [actvtyCtgrCd] - 활동 카테고리 코드.
     */
    getList(refId: string | number, refContentType: string, actvtyCtgrCd?: string): Promise<CommentItem[]> {
        return new Promise((resolve, reject): void => {
            const ajaxData: Record<string, unknown> = { refId, refContentType };
            if (actvtyCtgrCd) ajaxData.actvtyCtgrCd = actvtyCtgrCd;
            (cF as any).ajax.get((Url as any).COMMENTS, ajaxData, (res: any): void => {
                if (res.rslt) resolve(res.rsltList as CommentItem[] || []);
                else reject(res.message as string);
            });
        });
    },

    /**
     * 댓글 단건 조회 (AJAX) — 수정 모달 데이터 로드용
     * @param {string|number} id - 댓글 번호.
     */
    getDetail(id: string | number): Promise<CommentForm> {
        return new Promise((resolve, reject): void => {
            const url: string = (cF as any).util.bindUrl((Url as any).COMMENT, { id });
            (cF as any).ajax.get(url, null, (res: any): void => {
                if (res.rslt) resolve(res.rsltObj as CommentForm);
                else reject(res.message as string);
            });
        });
    },
};