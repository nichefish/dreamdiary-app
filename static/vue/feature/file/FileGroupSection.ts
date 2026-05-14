/**
 * FileGroupSection.ts
 * 첨부파일 등록/수정 영역 Vue 컴포넌트
 *
 * 변경(D): file_module.ts(jQuery 기반 dF.FileGroup 모듈)를 Vue 컴포넌트로 전환.
 *          - addFileItem / fileRecordListToggle / delNewFileSpan / delExistingFile 로직 흡수.
 *          - 기존 파일 목록 및 새 파일 항목을 reactive 상태로 관리.
 *
 * @author nichefish
 */
import type { FileRecord, FileGroupSectionLabels } from "./types.js";

declare const Vue: any;
declare const cF: any;
declare const Swal: any;
declare const Message: any;

/** 새로 추가된 파일 항목 */
interface NewFileItem {
    idx: number;
    name: string;
}

/** 기존 파일 항목 (삭제 플래그 포함) */
interface ExistingFileItem extends FileRecord {
    deleted: boolean;
}

const FileGroupSection = {
    name: "FileGroupSection",
    props: {
        existingFiles: { type: Array as () => FileRecord[], default: (): FileRecord[] => [] },
        labels: { type: Object as () => FileGroupSectionLabels, required: true },
    },
    data(): { newFiles: NewFileItem[]; existingList: ExistingFileItem[] } {
        return {
            newFiles: [],
            existingList: [],
        };
    },
    created(): void {
        /** 기존 파일 목록 초기화 (삭제 플래그 false) */
        this.existingList = (this.existingFiles as FileRecord[]).map(
            (f: FileRecord): ExistingFileItem => ({ ...f, deleted: false })
        );
    },
    computed: {
        /** 기존 파일 목록 (deleted 제외) + 새 파일 합산 → 0건이면 빈 상태 표시 */
        hasAnyFile(): boolean {
            const hasExisting = (this.existingList as ExistingFileItem[]).some((f) => !f.deleted);
            const hasNew = (this.newFiles as NewFileItem[]).length > 0;
            return hasExisting || hasNew;
        },
    },
    methods: {
        /**
         * 첨부파일 추가
         * 기존 cF.util.getReqstItemIdx 로 고유 idx 산출 후 hidden input을 동적으로 추가하고 파일 다이얼로그를 엽니다.
         */
        addFileItem(): void {
            const idx: number = (cF as any).util.getReqstItemIdx("input", "id^=fileGroup", "fileGroup");
            (this.newFiles as NewFileItem[]).push({ idx, name: "" });
            Vue.nextTick((): void => {
                const input = document.getElementById("fileGroup" + idx) as HTMLInputElement | null;
                if (!input) return;
                input.addEventListener("change", (): void => this.onFileChange(idx, input));
                input.click();
            });
        },

        /**
         * 파일 선택 change 핸들러
         * 파일 크기·확장자 검사 후 유효하면 파일명을 표시하고, 실패 시 항목을 제거합니다.
         */
        onFileChange(idx: number, input: HTMLInputElement): void {
            if (input.value === "") {
                this.removeNewFile(idx);
                return;
            }
            if (!(cF as any).validate.fileSizeChck(input) || !(cF as any).validate.fileExtnChck(input)) {
                this.removeNewFile(idx);
                return;
            }
            const filename: string = input.value.split("/").pop()?.split("\\").pop() || "";
            const item = (this.newFiles as NewFileItem[]).find((f) => f.idx === idx);
            if (item) item.name = filename;
        },

        /** 새로 추가된 파일 항목 제거 (배열에서 splice) */
        removeNewFile(idx: number): void {
            const pos = (this.newFiles as NewFileItem[]).findIndex((f) => f.idx === idx);
            if (pos !== -1) (this.newFiles as NewFileItem[]).splice(pos, 1);
        },

        /**
         * 새로 추가된 파일 삭제 확인 후 제거
         * @param {number} idx - 새 파일 항목 idx.
         */
        delNewFile(idx: number): void {
            (Swal as any).fire({
                text: (Message as any).get("view.cnfm.del"),
                showCancelButton: true,
            }).then((result: { value: boolean }): void => {
                if (!result.value) return;
                this.removeNewFile(idx);
            });
        },

        /**
         * 기존 파일 삭제 플래그(atchCtrl=D) 세팅
         * @param {number} fileId - 첨부파일 상세 번호.
         */
        delExistingFile(fileId: number): void {
            (Swal as any).fire({
                text: (Message as any).get("view.cnfm.del"),
                showCancelButton: true,
            }).then((result: { value: boolean }): void => {
                if (!result.value) return;
                const file = (this.existingList as ExistingFileItem[]).find((f) => f.id === fileId);
                if (file) file.deleted = true;
            });
        },

        /** 파일 다운로드 (전역 cF.util.fileDownload 위임) */
        fileDownload(fileGroupId: string | number, fileId: number): void {
            (cF as any).util.fileDownload(String(fileGroupId), String(fileId));
        },
    },
    template: `
    <div class="row">
        <div>
            <label class="mb-2">
                <span class="text-gray-700 fs-6 fw-bolder">{{ labels.atchFile }}</span>
                <span class="text-gray-500 fs-9 mx-2">{{ labels.fileSizeNote }}</span>
            </label>
            <!--
             * formFile: 파일 추가 트리거 전용 input (name 없음, 실제 업로드 대상 아님).
             * @click.prevent 로 formFile 자체의 파일 다이얼로그를 막고, addFileItem 으로 fileGroup{idx} 다이얼로그만 엽니다.
             -->
            <input type="file" id="formFile" class="form-control"
                   @click.prevent="addFileItem" />
        </div>
        <div class="col-xl-10 mb-8 text-sm-start" id="fileGroupSpan">
            <!--기존 파일 목록-->
            <div
                v-for="file in existingList"
                :key="'existing-' + file.id"
                class="row mt-2"
                :id="'fileGroup' + file.id"
                :style="{ display: file.deleted ? 'none' : '' }"
            >
                <div class="col-xl-8 text-sm-start" :id="'itemContainer' + file.id">
                    <i class="fas fa-file-download fs-15 me-3"></i>
                    <a href="javascript:void(0);"
                       @click.prevent="fileDownload(file.fileGroupId, file.id)"
                       data-bs-toggle="tooltip" data-bs-placement="top" :title="labels.downloadTooltip">
                        {{ file.orgnFileNm }} ({{ file.fileSize }}byte)
                    </a>
                    <div class="badge badge-light btn-primary badge-outlined mx-2 cursor-pointer"
                         @click="delExistingFile(file.id)"
                         data-bs-toggle="tooltip" data-bs-placement="top" :title="labels.delTooltip">
                        {{ labels.del }}
                    </div>
                    <!--atchCtrl :: CRUD — deleted 시 'D', 유지 시 'R'-->
                    <input type="hidden" :name="'atchCtrl' + file.id" :id="'atchCtrl' + file.id"
                           :value="file.deleted ? 'D' : 'R'" />
                </div>
            </div>
            <!--새로 추가된 파일 목록-->
            <div
                v-for="item in newFiles"
                :key="'new-' + item.idx"
                class="row mt-2"
                :id="'itemContainer' + item.idx"
            >
                <!--atchCtrl :: CRUD-->
                <input type="hidden" :name="'atchCtrl' + item.idx" :id="'atchCtrl' + item.idx" value="C" size="5" />
                <!--fileSn :: nullable-->
                <input type="hidden" :name="'fileSn' + item.idx" :value="'fileSn' + item.idx" size="5" />
                <div hidden>
                    <input type="file" :name="'fileGroup' + item.idx" :id="'fileGroup' + item.idx" class="file" />
                </div>
                <div class="col-xl-10 text-sm-start">
                    <!--fileNm :: to display-->
                    <i class="fas fa-file-download fs-15 me-3"></i>
                    <span :id="'fileNm' + item.idx">{{ item.name }}</span>
                    <button type="button" class="badge badge-light btn-primary badge-outlined mx-2"
                            @click.prevent="delNewFile(item.idx)"
                            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="labels.delTooltip">
                        {{ labels.del }}
                    </button>
                </div>
            </div>
        </div>
        <!--빈 상태 표시 (기존 fileRecordListToggle → emptyFileListDiv 역할)-->
        <div v-if="!hasAnyFile" id="emptyFileListDiv" class="text-muted fs-9 col-xl-10 mb-8">
            첨부된 파일이 없습니다.
        </div>
    </div>
    `,
};

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

runWhenDomReady(function(): void {
    const mountEl = document.getElementById("file_group_section_app");
    if (!mountEl) {
        return;
    }

    const dataEl = document.getElementById("file_group_section_data");
    const existingFiles: FileRecord[] = dataEl
        ? (() => { try { return JSON.parse(dataEl.textContent || "[]"); } catch { return []; } })()
        : [];

    const labelsEl = document.getElementById("file_group_section_labels");
    const labels: FileGroupSectionLabels = labelsEl
        ? (() => { try { return JSON.parse(labelsEl.textContent || "{}"); } catch { return {} as FileGroupSectionLabels; } })()
        : {} as FileGroupSectionLabels;

    Vue.createApp({
        components: { FileGroupSection },
        data(): { existingFiles: FileRecord[]; labels: FileGroupSectionLabels } {
            return { existingFiles, labels };
        },
        template: `<FileGroupSection :existingFiles="existingFiles" :labels="labels" />`,
    }).mount("#file_group_section_app");
});
