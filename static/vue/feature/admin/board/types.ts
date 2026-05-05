/**
 * 게시판 관리(Board group) Vue 화면 공통 타입
 */

export type BoardRow = {
    rnum: number;
    id: number;
    boardKey: string;
    boardName: string;
    categoryGroupCode: string;
    description: string;
    useYn: string;
    /** board_post.content_type = boardKey 인 행 수 */
    postCount?: number;
};

export type BoardRegistFormState = {
    /** 수정 모드일 때만 세팅 */
    id?: number;
    boardKey: string;
    boardName: string;
    categoryGroupCode: string;
    description: string;
    useYn: string;
    regYn: string;
};

export type PaginationState = {
    currPageNo: number;
    lastPageNo: number;
    totalCnt: number;
    pageSize: number;
    isFirstPage: boolean;
    isLastPage: boolean;
    prevPageNo: number;
    nextPageNo: number;
};

export type BoardAdminState = {
    rows: BoardRow[];
    pagination: PaginationState;
    boardForm: BoardRegistFormState;
};

export type BoardAdminActions = {
    page: (pageNo: number, pageSize?: number) => void;
    openBoardRegist: (payload?: Record<string, any>) => void;
    submitBoardRegist: () => void;
    openBoardModify: (id: number) => void;
    registBoardAjax: () => void;
    toggleBoardUse: (id: number, currentlyUse: boolean) => void;
    deleteBoard: (id: number) => void;
};
