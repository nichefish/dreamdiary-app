/**
 * FileGroupDetail.ts
 * 첨부파일 상세 영역 Vue 컴포넌트 (읽기 전용)
 *
 * 변경(D): _file_page_dtl_area.ftlh Freemarker 서버 렌더링을 Vue 컴포넌트로 전환.
 *
 * @author nichefish
 */
import type { FileRecord, FileGroupDetailLabels } from "./types.js";

declare const Vue: any;
declare const cF: any;

const FileGroupDetail = {
    name: "FileGroupDetail",
    props: {
        files: { type: Array as () => FileRecord[], default: (): FileRecord[] => [] },
        labels: { type: Object as () => FileGroupDetailLabels, required: true },
    },
    computed: {
        fileList(): FileRecord[] {
            return this.files as FileRecord[];
        },
    },
    methods: {
        /** 파일 다운로드 (전역 cF.util.fileDownload 위임) */
        fileDownload(fileGroupId: string | number, fileId: number): void {
            (cF as any).util.fileDownload(String(fileGroupId), String(fileId));
        },
    },
    template: `
    <template v-if="fileList && fileList.length > 0">
        <div class="d-flex flex-stack flex-wrap mb-3 bg-light">
            <div class="col-xl-1 col-form-label fs-6 fw-bold px-5">{{ labels.atchFile }}</div>
            <div class="col-xl-11 my-3">
                <div v-for="file in fileList" :key="file.id" class="row my-1">
                    <div class="col-xl-8" :id="'itemContainer' + file.id">
                        <a href="javascript:void(0);"
                           @click.prevent="fileDownload(file.fileGroupId, file.id)"
                           data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click"
                           :title="labels.downloadTooltip">
                            <i class="fas fa-file-download fs-15 me-1"></i>
                            {{ file.orgnFileNm }}({{ file.fileSize }}byte)
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </template>
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
    const mountEl = document.getElementById("file_group_detail_app");
    if (!mountEl) {
        return;
    }

    const dataEl = document.getElementById("file_group_detail_data");
    const files: FileRecord[] = dataEl
        ? (() => { try { return JSON.parse(dataEl.textContent || "[]"); } catch { return []; } })()
        : [];

    const labelsEl = document.getElementById("file_group_detail_labels");
    const labels: FileGroupDetailLabels = labelsEl
        ? (() => { try { return JSON.parse(labelsEl.textContent || "{}"); } catch { return {} as FileGroupDetailLabels; } })()
        : {} as FileGroupDetailLabels;

    Vue.createApp({
        components: { FileGroupDetail },
        data(): { files: FileRecord[]; labels: FileGroupDetailLabels } {
            return { files, labels };
        },
        template: `<FileGroupDetail :files="files" :labels="labels" />`,
    }).mount("#file_group_detail_app");
});
