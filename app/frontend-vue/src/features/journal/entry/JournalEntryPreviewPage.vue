<template>
  <!--begin::작성 중 저널 엔트리/리플렉션 미리보기 팝업-->
  <div class="p-5">
    <div class="card post">
      <div class="card-body">
        <div v-if="loading" class="d-flex justify-content-center py-10">
          <span class="spinner-border text-primary" role="status"></span>
        </div>
        <div v-else-if="errorText" class="text-danger py-10 text-center">
          {{ errorText }}
        </div>
        <template v-else-if="view">
          <div class="mb-0">
            <div class="d-flex flex-wrap align-items-baseline gap-3 mb-2">
              <span class="fs-3 fw-bold text-gray-900">
                <i :class="['bi me-2 text-primary', typeIconClass]"></i>{{ typeLabel }}
              </span>
              <span v-if="view.sortOrder != null" class="text-muted fs-6"># {{ view.sortOrder }}</span>
            </div>
            <div v-if="view.prefixName || view.title" class="d-flex align-items-center flex-wrap fs-5 fw-semibold text-gray-800 mb-2">
              <span
                v-if="view.prefixName"
                class="badge me-2 fs-8"
                :style="{ borderColor: view.prefixColor || '', color: view.prefixColor || '' }"
              >{{ view.prefixName }}</span>
              <span v-if="view.title">{{ view.title }}</span>
            </div>
          </div>

          <div class="separator separator-dashed border-gray-300 my-8"></div>

          <div class="px-2 py-1 pb-4 journal-markdown-preview">
            <div :class="itemClass">
              <div class="col-1 py-3 d-none d-md-flex border-2 border-gray-300 border-end ps-5 me-4 h-75" style="width:85px;">
                &nbsp;
              </div>
              <div class="col">
                <div :class="contentClass">
                  <div
                    v-if="view.markdownContent"
                    class="journal-content p-2"
                    v-html="view.markdownContent"
                  ></div>
                  <div v-else class="text-muted fs-7 fst-italic">
                    {{ t("journal.entry.view.empty") }}
                  </div>
                </div>
              </div>
              <div class="col-1 py-3 d-none d-md-flex w-50px ps-2">&nbsp;</div>
            </div>
          </div>
        </template>
      </div>
      <div class="card-footer">
        <div class="d-flex justify-content-end">
          <button type="button" class="btn btn-sm btn-light" @click="closePopup">
            {{ t("common.close") }}
          </button>
        </div>
      </div>
    </div>
  </div>
  <!--end::작성 중 저널 엔트리/리플렉션 미리보기 팝업-->
</template>

<script setup lang="ts">
/**
 * JournalEntryPreviewPage.vue
 * 등록/수정 모달의 작성 중 본문을 목록과 같은 journal-content로 새 창에 표시한다.
 * 페이로드는 opener가 localStorage에 쓴 뒤 storage 이벤트로 전달한다.
 */
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import {
  clearPreviewView,
  isPreviewId,
  previewStorageKey,
  readPreviewView,
  type JournalEntryPreviewView,
} from "@/features/journal/utils/journalEntryPreview";

const WAIT_MS = 20000;
const POLL_MS = 200;

const { t } = useLocaleStore();
const route = useRoute();
const loading = ref(true);
const view = ref<JournalEntryPreviewView | null>(null);
const timedOut = ref(false);

let pollTimer: number | null = null;
let timeoutTimer: number | null = null;
let waitingPreviewId: string | null = null;

const previewId = computed(() => {
  const raw = route.query.previewId;
  const value = Array.isArray(raw) ? raw[0] : raw;
  return isPreviewId(value) ? value : "";
});

const errorText = computed(() => {
  if (!previewId.value) return t("journal.entry.preview.missing");
  if (view.value?.error) return view.value.error;
  if (timedOut.value) return t("journal.entry.preview.missing");
  return "";
});

const typeLabel = computed(() => {
  const type = view.value?.contentType;
  if (type === "JOURNAL_DREAM") return t("common.dream");
  if (type === "JOURNAL_NOTE") return t("journal.note");
  if (type === "JOURNAL_REFLECTION") return t("journal.reflection.label");
  return t("common.diary");
});

const typeIconClass = computed(() => {
  const type = view.value?.contentType;
  if (type === "JOURNAL_DREAM") return "bi-moon-stars";
  if (type === "JOURNAL_REFLECTION") return "bi-chat-quote";
  if (type === "JOURNAL_NOTE") return "bi-journal";
  return "bi-journal-text";
});

const itemClass = computed(() => {
  const type = view.value?.contentType;
  if (type === "JOURNAL_DREAM") return "journal-dream-item";
  if (type === "JOURNAL_NOTE") return "journal-note-item";
  if (type === "JOURNAL_REFLECTION") return "journal-reflection-embed";
  return "journal-diary-item";
});

const contentClass = computed(() => {
  const type = view.value?.contentType;
  if (type === "JOURNAL_DREAM") return "journal-dream-content";
  if (type === "JOURNAL_NOTE") return "journal-note-content";
  if (type === "JOURNAL_REFLECTION") return "journal-reflection-content";
  return "journal-diary-content";
});

function stopWaiting(): void {
  if (pollTimer != null) {
    window.clearInterval(pollTimer);
    pollTimer = null;
  }
  if (timeoutTimer != null) {
    window.clearTimeout(timeoutTimer);
    timeoutTimer = null;
  }
  waitingPreviewId = null;
}

function applyView(previewKey: string, next: JournalEntryPreviewView): void {
  stopWaiting();
  view.value = next;
  loading.value = false;
  timedOut.value = false;
  clearPreviewView(previewKey);
}

function tryRead(previewKey: string): boolean {
  const stored = readPreviewView(previewKey);
  if (!stored) return false;
  applyView(previewKey, stored);
  return true;
}

function startWaiting(previewKey: string): void {
  stopWaiting();
  waitingPreviewId = previewKey;
  loading.value = true;
  view.value = null;
  timedOut.value = false;
  if (tryRead(previewKey)) return;

  pollTimer = window.setInterval(() => {
    if (waitingPreviewId) tryRead(waitingPreviewId);
  }, POLL_MS);
  timeoutTimer = window.setTimeout(() => {
    if (!view.value) {
      loading.value = false;
      timedOut.value = true;
      console.warn("[JournalEntryPreviewPage] preview payload wait timed out", { previewId: previewKey });
    }
    stopWaiting();
  }, WAIT_MS);
}

function onStorage(event: StorageEvent): void {
  if (!waitingPreviewId) return;
  if (event.key && event.key !== previewStorageKey(waitingPreviewId)) return;
  tryRead(waitingPreviewId);
}

function closePopup(): void {
  window.close();
}

watch(previewId, (id) => {
  if (!id) {
    stopWaiting();
    loading.value = false;
    view.value = null;
    return;
  }
  startWaiting(id);
}, { immediate: true });

onMounted(() => {
  window.addEventListener("storage", onStorage);
});

onUnmounted(() => {
  window.removeEventListener("storage", onStorage);
  stopWaiting();
});
</script>
