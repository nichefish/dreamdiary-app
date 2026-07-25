<template>
  <!--begin::저널 스레드 독립 수정 페이지-->
  <div class="card mt-3 mb-5">
    <div class="card-header border-0">
      <div class="card-title">
        <h3 class="fw-bold text-gray-900">{{ t("journal.thread.modify.modal.title") }}</h3>
      </div>
      <div class="card-toolbar">
        <button
          type="button"
          class="btn btn-sm"
          :class="closeArmed ? 'btn-warning' : 'btn-light-primary'"
          :title="closeArmed ? t('common.modal.close-armed.tooltip') : t('journal.thread.detail.modal.title')"
          @click="requestSafeClose"
        >
          <i class="bi bi-arrow-left me-1"></i>
          {{ closeArmed ? t("common.modal.close-armed.btn") : t("journal.thread.detail.modal.title") }}
        </button>
      </div>
    </div>

    <div class="card-body pt-3">
      <JournalThreadEditorForm />
    </div>

    <div class="card-footer d-flex justify-content-end gap-2">
      <button
        type="button"
        class="btn btn-sm btn-primary"
        :disabled="store.submitting"
        @click="submit"
      >
        <span v-if="store.submitting" class="spinner-border spinner-border-sm me-1" role="status"></span>
        {{ t("common.save") }}
      </button>
      <button
        type="button"
        class="btn btn-sm"
        :class="closeArmed ? 'btn-warning' : 'btn-light'"
        @click="requestSafeClose"
      >{{ closeArmed ? t("common.modal.close-armed.btn") : t("common.cancel") }}</button>
    </div>
  </div>
  <!--end::저널 스레드 독립 수정 페이지-->
</template>

<script setup lang="ts">
import { useRoute, useRouter, onBeforeRouteLeave } from "vue-router";
import { swalConfirm } from "@/shared/utils/swal";
import { useSafeModalClose } from "@/shared/utils/safeModalClose";
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
import JournalThreadEditorForm from "@/features/journal/thread/components/JournalThreadEditorForm.vue";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const route = useRoute();
const router = useRouter();
const store = useJournalThreadStore();
const { t } = useLocaleStore();

/**
 * 버튼 취소·저장 경로는 편집 상태를 먼저 닫으므로 라우터 이탈 확인을 중복 표시하지 않는다.
 * 브라우저 뒤로가기·사이드 메뉴처럼 폼을 우회하는 이동만 변경 내용 폐기 확인을 거친다.
 */
onBeforeRouteLeave(async () => {
  if (!store.registDirty || store.submitting) return true;
  const confirmed = await swalConfirm(t("common.confirm.leave-unsaved"));
  if (!confirmed) {
    console.info("[journal-thread] edit route leave canceled: unsaved changes remain", {
      threadId: store.registModel?.id,
    });
    return false;
  }
  console.info("[journal-thread] edit route leave confirmed: discarding unsaved changes", {
    threadId: store.registModel?.id,
  });
  store.closeRegist();
  return true;
});

const { closeArmed, requestSafeClose } = useSafeModalClose(() => {
  const id = Number(route.params.id);
  store.closeRegist();
  if (Number.isInteger(id) && id > 0) {
    void router.push({ name: "thread-detail", params: { id } });
    return;
  }
  console.warn("[journal-thread] edit cancel returned to list: route id is invalid", {
    routeId: route.params.id,
  });
  void router.push({ name: "thread-list" });
});

/** 독립 편집 저장 뒤 같은 탭에서 해당 스레드의 독립 상세로 복귀한다. */
async function submit(): Promise<void> {
  const confirmed = await swalConfirm(t("common.confirm.mdf"));
  if (!confirmed) return;
  const id = Number(store.registModel?.id ?? route.params.id);
  const succeeded = await store.submitRegist();
  if (!succeeded) return;
  if (!Number.isInteger(id) || id <= 0) {
    console.warn("[journal-thread] edit save returned to list: thread id is invalid", { id });
    await router.replace({ name: "thread-list" });
    return;
  }
  await router.replace({ name: "thread-detail", params: { id } });
}
</script>
