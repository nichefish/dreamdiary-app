<template>
  <!--begin::저널 스레드 독립 상세 페이지-->
  <div class="card mt-3 mb-5">
    <div class="card-header border-0">
      <div class="card-title">
        <h3 class="fw-bold text-gray-900">{{ t("journal.thread.detail.modal.title") }}</h3>
      </div>
      <div class="card-toolbar gap-2">
        <!--begin::복사 (split — 주 버튼=전체/해석 포함, ▾ 드롭다운=본문만/해석 제외)-->
        <div v-if="store.detailModel?.id" class="btn-group" role="group">
          <button
            type="button"
            class="btn btn-sm btn-icon btn-light-primary copy-split-main"
            :title="hasThreadReflections ? t('journal.copy.full.tooltip') : t('common.copy')"
            @click="onCopy(true)"
          >
            <i class="bi bi-copy"></i>
          </button>
          <button
            type="button"
            class="btn btn-sm btn-light-primary copy-split-caret"
            data-kt-menu-trigger="click"
            data-kt-menu-placement="bottom-end"
            :title="t('common.menu')"
          >
            <i class="bi bi-caret-down-fill fs-9 pe-0"></i>
          </button>
          <div
            class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold w-175px py-2"
            data-kt-menu="true"
          >
            <div class="menu-item px-3 my-1 cursor-pointer">
              <div class="menu-link flex-stack px-3" @click="onCopy(false)">
                {{ t('journal.copy.body.label') }}
                <i class="bi bi-clipboard fs-8"></i>
              </div>
            </div>
          </div>
        </div>
        <!--end::복사 (split)-->
        <!--begin::다운로드 (split — 주 버튼=전체/해석 포함, ▾ 드롭다운=본문만/해석 제외)-->
        <div v-if="store.detailModel?.id" class="btn-group" role="group">
          <button
            type="button"
            class="btn btn-sm btn-icon btn-light-primary copy-split-main"
            :title="hasThreadReflections ? t('journal.download.full.tooltip') : t('common.export-text.tooltip')"
            @click="onDownload(true)"
          >
            <i class="bi bi-download"></i>
          </button>
          <button
            type="button"
            class="btn btn-sm btn-light-primary copy-split-caret"
            data-kt-menu-trigger="click"
            data-kt-menu-placement="bottom-end"
            :title="t('common.menu')"
          >
            <i class="bi bi-caret-down-fill fs-9 pe-0"></i>
          </button>
          <div
            class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold w-175px py-2"
            data-kt-menu="true"
          >
            <div class="menu-item px-3 my-1 cursor-pointer">
              <div class="menu-link flex-stack px-3" @click="onDownload(false)">
                {{ t('journal.download.body.label') }}
                <i class="bi bi-download fs-8"></i>
              </div>
            </div>
          </div>
        </div>
        <!--end::다운로드 (split)-->
        <button
          v-if="store.detailModel?.id"
          type="button"
          class="btn btn-sm btn-light-primary"
          :title="t('common.mdf')"
          @click="goToEdit"
        >
          <i class="bi bi-pencil-square me-1"></i>
          {{ t("common.mdf") }}
        </button>
        <button
          type="button"
          class="btn btn-sm btn-light-primary"
          :title="t('route.title.journal-thread')"
          @click="goToList"
        >
          <i class="bi bi-arrow-left me-1"></i>
          {{ t("route.title.journal-thread") }}
        </button>
      </div>
    </div>

    <div class="card-body pt-3">
      <div v-if="store.detailLoading" class="d-flex justify-content-center py-10">
        <span class="spinner-border text-primary" role="status"></span>
      </div>
      <JournalThreadDetailContent v-else-if="store.detailModel" />
    </div>
  </div>
  <!--end::저널 스레드 독립 상세 페이지-->
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRouter } from "vue-router";
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
import JournalThreadDetailContent from "@/features/journal/thread/components/JournalThreadDetailContent.vue";
import { copyThreadDetail, downloadThreadDetail } from "@/features/journal/utils/journalThreadExport";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const router = useRouter();
const store = useJournalThreadStore();
const { t } = useLocaleStore();

/** 독립 상세를 닫고 스레드 목록의 정식 route로 이동한다. */
function goToList(): void {
  void router.push({ name: "thread-list" });
}

/** 독립 상세의 현재 스레드를 같은 탭의 독립 수정 route로 전환한다. */
function goToEdit(): void {
  const id = Number(store.detailModel?.id);
  if (!Number.isInteger(id) || id <= 0) {
    console.warn("[journal-thread] detail edit skipped: detail id is missing");
    return;
  }
  void router.push({ name: "thread-edit", params: { id } });
}
/** 스레드 소속 엔트리 중 하나라도 리플렉션이 있으면 true (▾ 드롭다운 노출 조건). */
const hasThreadReflections = computed(() =>
  store.detailEntries.some((e) => (e.reflectionList?.length ?? 0) > 0),
);

/** 스레드 제목 + 소속 엔트리를 클립보드에 복사한다. (includeReflection: 해석 포함 여부) */
function onCopy(includeReflection = true): void {
  void copyThreadDetail(store.detailModel, store.detailEntries, t, includeReflection);
}

/** 스레드 소속 엔트리를 서버 텍스트 내보내기로 다운로드한다. (includeReflection: 해석 포함 여부) */
function onDownload(includeReflection = true): void {
  const id = store.detailModel?.id;
  if (!id) return;
  downloadThreadDetail(id, includeReflection);
}

</script>
