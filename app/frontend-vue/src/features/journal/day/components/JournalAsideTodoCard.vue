<template>
  <!--begin::aside TODO 카드 (레거시 JournalDayAsideTodoCardApp 이식 — 헤더·목록 단일 카드)-->
  <div class="card card-reset card-p-0 p-5 mt-8" style="width:280px; min-width:280px; max-width:280px;">
    <div id="journal_todo_aside_header" class="card-header min-h-auto mb-5 px-0 border-0">
      <h3 class="card-title text-gray-900 fw-bold fs-3 mb-0">
        <i class="bi bi-list-task fs-2 me-1"></i> {{ t("journal.aside.todo.title") }}
      </h3>
      <div class="card-toolbar">
        <button
          type="button"
          class="btn btn-sm btn-icon btn-primary"
          :title="t('journal.aside-todo-add.tooltip')"
          @click.prevent="openTodoRegist"
        >
          <i class="bi bi-plus fs-2 pe-0" id="journalTodoAsideRegistIcon"></i>
        </button>
      </div>
    </div>
    <div id="journal_todo_list_div">
      <div v-if="store.todoError" class="journal-day d-flex-center text-danger fs-7">
        {{ store.todoError }}
      </div>
      <template v-else-if="store.todoList.length > 0">
        <div
          v-for="item in store.todoList"
          :key="'todo-' + item.id"
          class="row d-flex-align-center justify-content-between"
        >
          <div class="col text-truncate cursor-pointer" :title="item.title">
            {{ item.title }}
          </div>
          <div class="col-3 d-flex justify-content-end">
            <button
              type="button"
              class="btn btn-sm btn-light-danger btn-outlined py-2 px-3 cursor-pointer"
              :title="t('common.del')"
              @click.prevent="deleteTodo(item.id)"
            >
              <i class="bi bi-trash p-0"></i>
            </button>
          </div>
        </div>
      </template>
      <div v-else class="journal-day d-flex-center">
        {{ t("journal.todo.empty") }}
      </div>
    </div>
  </div>
  <!--end::aside TODO 카드-->
</template>

<script setup lang="ts">
import { onMounted, watch } from "vue";
import axios from "axios";
import { swalAlert, swalConfirm, swalRequestError, swalAjaxResult } from "@/shared/utils/swal";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { useJournalStore } from "@/features/journal/stores/journal";
import { useJournalModalStore } from "@/features/journal/stores/journalModal";

const store = useJournalStore();
const modalStore = useJournalModalStore();
const { t } = useLocaleStore();

/** 할일 등록 모달 열기 (레거시 registModal — 현재 년/월 파생 동일) */
function openTodoRegist(): void {
  modalStore.openTodoRegist({ yy: store.yy, mnth: store.mnth });
}

/** 할일 삭제 (레거시 journalTodoCrudService.deleteAjax 등가 — 삭제 후 목록 갱신) */
async function deleteTodo(id: number): Promise<void> {
  if (!await swalConfirm(t("journal.todo.delete.confirm"))) return;
  try {
    const res = await axios.delete(`/api/journal/todo/${id}`);
    const ok = res.data?.rslt === true;
    await swalAjaxResult({
      rslt: ok,
      message: res.data?.message,
      successFallback: t("common.result.deleted"),
      failureFallback: t("common.result.failure"),
    });
    if (ok) {
      void store.fetchTodos();
    }
  } catch (e: unknown) {
    void swalRequestError(e);
  }
}

// 년/월 이동 시 해당 월 TODO 재조회 (레거시 yyMnthListAjax 등가)
watch(
  () => [store.yy, store.mnth] as const,
  () => {
    void store.fetchTodos();
  },
);

onMounted(() => {
  void store.fetchTodos();
});
</script>
