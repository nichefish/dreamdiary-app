/**
 * types.ts
 * attachable/comment feature Vue 컴포넌트에서 사용하는 타입 정의
 *
 * @author nichefish
 */

/** 댓글 작성자 정보 */
export type CommentCreatedByInfo = {
    profileImageUrl?: string;
};

/** 댓글 목록 단건 */
export type CommentItem = {
    id: number | string;
    content: string;
    createdByNm: string;
    createdAt: string;
    updatedAt?: string;
    isCreatedBy: boolean;
    createdByInfo?: CommentCreatedByInfo;
};

/** 댓글 등록/수정 폼 데이터 */
export type CommentForm = {
    id?: number | string;
    refId: number | string;
    refContentType: string;
    actvtyCtgrCd?: string;
    content: string;
};

/** 현재 로그인 사용자 정보 (댓글 작성자 표시용) */
export type CommentCurrentUser = {
    nickname: string;
    profileImageUrl?: string;
};

/** 페이지 레벨 댓글 영역 초기 컨텍스트 (서버 주입) */
export type CommentPageContext = {
    comments: CommentItem[];
    refId: number | string;
    refContentType: string;
    actvtyCtgrCd?: string;
    currentUser: CommentCurrentUser;
};