/**
 * types.ts
 * file feature Vue 컴포넌트에서 사용하는 타입 정의
 *
 * @author nichefish
 */

/** 서버에서 내려오는 첨부파일 레코드 */
export type FileRecord = {
    id: number;
    fileGroupId: string | number;
    orgnFileNm: string;
    fileSize: number;
};

/** FileGroupSection(등록/수정) 라벨 */
export type FileGroupSectionLabels = {
    atchFile: string;
    fileSizeNote: string;
    downloadTooltip: string;
    del: string;
    delTooltip: string;
};

/** FileGroupDetail(상세/읽기 전용) 라벨 */
export type FileGroupDetailLabels = {
    atchFile: string;
    downloadTooltip: string;
};
