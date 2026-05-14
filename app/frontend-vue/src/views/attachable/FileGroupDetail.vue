<template>
  <!--begin::첨부파일 상세 (읽기 전용)-->
  <template v-if="files && files.length > 0">
    <div class="d-flex flex-stack flex-wrap mb-3 bg-light">
      <div class="col-xl-1 col-form-label fs-6 fw-bold px-5">첨부파일</div>
      <div class="col-xl-11 my-3">
        <div v-for="file in files" :key="file.id" class="row my-1">
          <div class="col-xl-8" :id="'itemContainer' + file.id">
            <a
              href="javascript:void(0);"
              @click.prevent="fileDownload(file.fileGroupId, file.id)"
              data-bs-toggle="tooltip"
              data-bs-placement="top"
              data-bs-dismiss="click"
              title="파일을 다운로드합니다."
            >
              <i class="fas fa-file-download fs-15 me-1"></i>
              {{ file.orgnFileNm }}({{ file.fileSize }}byte)
            </a>
          </div>
        </div>
      </div>
    </div>
  </template>
  <!--end::첨부파일 상세-->
</template>

<script setup lang="ts">
import type { FileRecord } from "@/stores/attachableModal";

defineProps<{ files: FileRecord[] }>();

/** 파일 다운로드 */
function fileDownload(fileGroupId: string | number, fileId: number): void {
  window.open(`/api/file/file-download.do?fileGroupId=${fileGroupId}&fileId=${fileId}`, "_blank");
}
</script>