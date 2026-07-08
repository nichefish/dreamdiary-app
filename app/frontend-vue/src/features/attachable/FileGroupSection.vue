<template>
  <!--begin::첨부파일 등록/수정 영역-->
  <div class="row">
    <div>
      <label class="mb-2">
        <span class="text-gray-700 fs-6 fw-bolder">{{ t('attach.label') }}</span>
        <span class="text-gray-500 fs-9 mx-2">{{ t('attach.section.size-limit') }}</span>
      </label>
      <!--
       * formFile: 파일 추가 트리거 전용 input (name 없음, 실제 업로드 대상 아님).
       * @click.prevent 로 formFile 자체의 파일 다이얼로그를 막고 addFileItem 으로 fileGroup{idx} 다이얼로그를 엽니다.
       -->
      <input type="file" id="formFile" class="form-control" @click.prevent="addFileItem" />
    </div>
    <div class="col-xl-10 mb-8 text-sm-start" id="fileGroupSpan">
      <!--begin::기존 파일 목록-->
      <div
        v-for="file in existingList"
        :key="'existing-' + file.id"
        class="row mt-2"
        :id="'fileGroup' + file.id"
        :style="{ display: file.deleted ? 'none' : '' }"
      >
        <div class="col-xl-8 text-sm-start" :id="'itemContainer' + file.id">
          <i class="fas fa-file-download fs-15 me-3"></i>
          <a
            href="javascript:void(0);"
            @click.prevent="fileDownload(file.fileGroupId, file.id)"
            data-bs-toggle="tooltip"
            data-bs-placement="top"
            :title="t('attach.download.tooltip')"
          >
            {{ file.orgnFileNm }} ({{ file.fileSize }}{{ t('attach.file-size.unit') }})
          </a>
          <div
            class="badge badge-light btn-primary badge-outlined mx-2 cursor-pointer"
            @click="deleteExistingFile(file.id)"
            data-bs-toggle="tooltip"
            data-bs-placement="top"
            :title="t('attach.delete.tooltip')"
          >{{ t('common.del') }}</div>
          <!--atchCtrl :: CRUD — deleted 시 'D', 유지 시 'R'-->
          <input
            type="hidden"
            :name="'atchCtrl' + file.id"
            :id="'atchCtrl' + file.id"
            :value="file.deleted ? 'D' : 'R'"
          />
        </div>
      </div>
      <!--end::기존 파일 목록-->
      <!--begin::새로 추가된 파일 목록-->
      <div
        v-for="item in newFiles"
        :key="'new-' + item.idx"
        class="row mt-2"
        :id="'itemContainer' + item.idx"
      >
        <!--atchCtrl :: CRUD-->
        <input type="hidden" :name="'atchCtrl' + item.idx" :id="'atchCtrl' + item.idx" value="C" />
        <!--fileSn :: nullable-->
        <input type="hidden" :name="'fileSn' + item.idx" :value="'fileSn' + item.idx" />
        <div hidden>
          <input type="file" :name="'fileGroup' + item.idx" :id="'fileGroup' + item.idx" class="file" />
        </div>
        <div class="col-xl-10 text-sm-start">
          <i class="fas fa-file-download fs-15 me-3"></i>
          <span :id="'fileNm' + item.idx">{{ item.name }}</span>
          <button
            type="button"
            class="badge badge-light btn-primary badge-outlined mx-2"
            @click.prevent="deleteNewFile(item.idx)"
            data-bs-toggle="tooltip"
            data-bs-placement="top"
            data-bs-dismiss="click"
            :title="t('attach.delete.tooltip')"
          >{{ t('common.del') }}</button>
        </div>
      </div>
      <!--end::새로 추가된 파일 목록-->
    </div>
    <!--begin::빈 상태 표시-->
    <div v-if="!hasAnyFile" id="emptyFileListDiv" class="text-muted fs-9 col-xl-10 mb-8">
      {{ t('attach.section.empty') }}
    </div>
    <!--end::빈 상태 표시-->
  </div>
  <!--end::첨부파일 등록/수정 영역-->
</template>

<script setup lang="ts">
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalConfirm, swalAlert } from "@/shared/utils/swal";
import { ref, computed, nextTick } from "vue";
import type { FileRecord } from "@/features/attachable/stores/attachableModal";

interface NewFileItem { idx: number; name: string; }
interface ExistingFileItem extends FileRecord { deleted: boolean; }

const { t } = useLocaleStore();
const MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

const props = withDefaults(
  defineProps<{ existingFiles?: FileRecord[] }>(),
  { existingFiles: () => [] }
);

let idxCounter = 0;
const newFiles = ref<NewFileItem[]>([]);
const existingList = ref<ExistingFileItem[]>(
  props.existingFiles.map((f) => ({ ...f, deleted: false }))
);

const hasAnyFile = computed(
  () => existingList.value.some((f) => !f.deleted) || newFiles.value.length > 0
);

/** 파일 추가 — 고유 idx 부여 후 숨겨진 input 의 파일 다이얼로그를 열어 파일을 선택한다. */
function addFileItem(): void {
  const idx = ++idxCounter;
  newFiles.value.push({ idx, name: "" });
  nextTick(() => {
    const input = document.getElementById("fileGroup" + idx) as HTMLInputElement | null;
    if (!input) return;
    input.addEventListener("change", () => onFileChange(idx, input));
    input.click();
  });
}

/** 파일 선택 change 핸들러 — 크기 검사 후 유효하면 파일명을 표시하고, 실패 시 항목을 제거한다. */
function onFileChange(idx: number, input: HTMLInputElement): void {
  if (!input.value) { removeNewFile(idx); return; }
  const file = input.files?.[0];
  if (file && file.size > MAX_FILE_SIZE) {
    void swalAlert(t("attach.validate.size-limit"));
    input.value = "";
    removeNewFile(idx);
    return;
  }
  const filename = input.value.split("/").pop()?.split("\\").pop() ?? "";
  const item = newFiles.value.find((f) => f.idx === idx);
  if (item) item.name = filename;
}

function removeNewFile(idx: number): void {
  const pos = newFiles.value.findIndex((f) => f.idx === idx);
  if (pos !== -1) newFiles.value.splice(pos, 1);
}

/** 새로 추가된 파일 삭제 확인 후 제거 */
async function deleteNewFile(idx: number): Promise<void> {
  if (!await swalConfirm(t("attach.delete.confirm"))) return;
  removeNewFile(idx);
}

/** 기존 파일 삭제 플래그(atchCtrl=D) 세팅 */
async function deleteExistingFile(fileId: number): Promise<void> {
  if (!await swalConfirm(t("attach.delete.confirm"))) return;
  const file = existingList.value.find((f) => f.id === fileId);
  if (file) file.deleted = true;
}

/** 파일 다운로드 */
function fileDownload(fileGroupId: string | number, fileId: number): void {
  window.open(`/api/file/file-download.do?fileGroupId=${fileGroupId}&fileId=${fileId}`, "_blank");
}
</script>
