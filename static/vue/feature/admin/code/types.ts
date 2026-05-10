/**
 * types.ts
 * 코드 관리 Vue 화면 공통 타입
 */

export type CodeGroupRow = {
    rnum: number;
    id: number;
    groupCode: string;
    groupName: string;
    description: string;
    codeItemCnt: number;
    useYn: string;
};

export type CodeItemRow = {
    rnum: number;
    id: number;
    code: string;
    codeName: string;
    description: string;
    sortOrder: number;
    useYn: string;
};

export type CodeGroupDetail = {
    id?: number;
    groupCode: string;
    groupName: string;
    description: string;
    codeItems: CodeItemRow[];
};

export type CodeGroupForm = {
    id?: number;
    groupCode: string;
    groupName: string;
    description: string;
    useYn: string;
    registYn: string;
};

export type CodeItemForm = {
    id?: number;
    groupCode: string;
    code: string;
    codeName: string;
    description: string;
    useYn: string;
    registYn: string;
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

export type CodeAdminState = {
    rows: CodeGroupRow[];
    pagination: PaginationState;
    groupForm: CodeGroupForm;
    detail: CodeGroupDetail;
    itemForm: CodeItemForm;
    /** 상세 모달에서 자식 모달(항목/그룹 등록)로 들어갔을 때, 자식이 닫히면 다시 열 Bootstrap 모달 id */
    modalReturnTarget: string | null;
};

export type CodeAdminActions = {
    page: (pageNo: number, pageSize?: number) => void;
    search: () => void;
    openGroupRegist: (payload?: Record<string, any>) => void;
    submitGroupRegist: () => void;
    registGroupAjax: () => void;
    openGroupDetail: (id: number) => void;
    openGroupModifyCurrent: () => void;
    toggleGroupUse: (id: number) => void;
    deleteGroup: (id: number) => void;
    openItemRegist: (payload?: Record<string, any>) => void;
    openItemModify: (id: number) => void;
    submitItemRegist: () => void;
    registItemAjax: () => void;
    deleteItem: (id: number) => void;
};

