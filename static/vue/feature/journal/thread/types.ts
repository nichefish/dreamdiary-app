/**
 * 저널 스레드 목록 Vue 에서 다루는 행 타입 (`journal_thread_list_data` 와 계약).
 *
 * 변경: 사용자 목록 `UserRow` 패턴과 동일하게, 페이지에 주입된 JSON 스키마만 반영하고
 * Dto 전 필드를 포함하지 않는다.
 *
 * @author nichefish
 */
export type JournalThreadListTagRow = {
    tagId: string;
    ctgr: string;
    name: string;
};

export type JournalThreadListRow = {
    rnum: number;
    id: number;
    contentType: string;
    categoryName: string;
    title: string;
    isNew: boolean;
    commentCnt: number;
    fileGroupId: string | number;
    hasFiles: boolean;
    hasTagsLayout: boolean;
    tags: JournalThreadListTagRow[];
};

export type JournalThreadListLabels = {
    pageDetail: string;
    comment: string;
    atchFile: string;
    tagContentList: string;
    modalView: string;
    emptyList: string;
};
