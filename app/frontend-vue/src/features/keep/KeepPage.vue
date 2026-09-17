<template>
  <!--begin::Keep 전수관리 화면 (전 범위 journal_todo projection; 사이드바는 활성 projection으로 별도 유지)-->
  <div class="d-flex flex-column flex-column-fluid p-5">
    <div class="card">
      <div class="card-header d-flex align-items-center justify-content-between flex-wrap gap-2">
        <h2 class="card-title fw-bold fs-2 m-0">
          <i class="bi bi-bookmark-star fs-2 me-2"></i> Keep
        </h2>
        <div class="d-flex align-items-center gap-2">
          <input
            v-model="keyword"
            type="text"
            class="form-control form-control-sm"
            style="width: 220px;"
            placeholder="검색"
          />
          <button type="button" class="btn btn-sm btn-primary" @click.prevent="openAdd">
            <i class="bi bi-plus fs-3 pe-0"></i> 추가
          </button>
        </div>
      </div>
      <div class="card-body">
        <div v-if="error" class="text-danger fs-7 mb-3">{{ error }}</div>

        <!-- 미해결 (OPEN·PENDING) -->
        <h4 class="fw-bold fs-5 text-gray-800 mb-3">
          미해결 <span class="text-muted fs-7 ms-1">{{ openItems.length }}</span>
        </h4>
        <div v-if="openItems.length === 0" class="text-muted fs-7 mb-8">미해결 항목이 없습니다.</div>
        <div v-else class="mb-8">
          <div
            v-for="item in openItems"
            :key="'keep-open-' + item.id"
            class="d-flex align-items-center py-2 border-bottom border-gray-200"
          >
            <input
              type="checkbox"
              class="form-check-input cursor-pointer me-3"
              :title="t('status.completed')"
              @change.prevent="completeTodo(item.id)"
            />
            <div
              class="flex-grow-1 text-truncate cursor-pointer"
              :title="item.title"
              @click.prevent="openTodoEdit(item.id)"
            >
              <span v-if="isPending(item)" class="badge badge-light-warning fs-9 me-1">{{ t("lifecycle.pending") }}</span>
              {{ item.title }}
              <span v-if="item.yy" class="text-muted fs-8 ms-2">{{ item.yy }}.{{ padMonth(item.mnth) }}</span>
            </div>
            <div class="dropdown">
              <button
                type="button"
                class="btn btn-sm btn-icon btn-active-light-primary py-2 px-2 cursor-pointer"
                data-bs-toggle="dropdown"
                :title="t('journal.todo.more')"
              >
                <i class="bi bi-three-dots-vertical p-0"></i>
              </button>
              <ul class="dropdown-menu dropdown-menu-end shadow py-2" style="min-width: 0; width: max-content;">
                <li v-if="!isPending(item)">
                  <a class="dropdown-item cursor-pointer" @click.prevent="setPending(item.id)">{{ t("lifecycle.pending") }}</a>
                </li>
                <li v-else>
                  <a class="dropdown-item cursor-pointer" @click.prevent="releasePending(item.id)">{{ t("journal.todo.pending.release") }}</a>
                </li>
              </ul>
            </div>
          </div>
        </div>

        <!-- 완료 (RESOLVED) -->
        <h4 class="fw-bold fs-5 text-gray-500 mb-3">
          완료 <span class="text-muted fs-7 ms-1">{{ resolvedItems.length }}</span>
        </h4>
        <div v-if="resolvedItems.length === 0" class="text-muted fs-7">완료된 항목이 없습니다.</div>
        <div v-else>
          <div
            v-for="item in resolvedItems"
            :key="'keep-done-' + item.id"
            class="d-flex align-items-center py-2 border-bottom border-gray-200 text-muted"
          >
            <i class="bi bi-check2 text-success fs-4 me-3"></i>
            <div
              class="flex-grow-1 text-truncate cursor-pointer text-decoration-line-through"
              :title="item.title"
              @click.prevent="openTodoEdit(item.id)"
            >
              {{ item.title }}
            </div>
            <div class="dropdown">
              <button
                type="button"
                class="btn btn-sm btn-icon btn-active-light-primary py-2 px-2 cursor-pointer"
                data-bs-toggle="dropdown"
                :title="t('journal.todo.more')"
              >
                <i class="bi bi-three-dots-vertical p-0"></i>
              </button>
              <ul class="dropdown-menu dropdown-menu-end shadow py-2" style="min-width: 0; width: max-content;">
                <li>
                  <a class="dropdown-item cursor-pointer" @click.prevent="reopenTodo(item.id)">되돌리기</a>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <JournalTodoRegistModal />
  <!--end::Keep 전수관리 화면-->
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from "vue";
import axios from "axios";
import { swalAlert, swalRequestError } from "@/shared/utils/swal";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { useJournalModalStore } from "@/features/journal/stores/journalModal";
import JournalTodoRegistModal from "@/features/journal/todo/modals/JournalTodoRegistModal.vue";

/** Keep 전 범위 항목 — 백엔드 JournalTodoDto 직렬화(scope=all) 중 화면 표시·검색분. */
interface KeepItem {
  id: number;
  title?: string;
  content?: string;
  yy?: number;
  mnth?: number;
  /** 라이프사이클 현재값(백엔드 enrich). 부재 시 OPEN. */
  lifecycle?: { lifecycleKey?: string | null } | null;
}

const { t } = useLocaleStore();
const modalStore = useJournalModalStore();

/** 전 범위 항목(미해결+완료) 원본 목록 */
const list = ref<KeepItem[]>([]);
/** 조회 실패 메시지 */
const error = ref<string | null>(null);
/** 제목·본문 검색어 (클라이언트 필터) */
const keyword = ref("");

/** 검색어로 거른 목록. 제목·본문 부분일치. */
const filtered = computed<KeepItem[]>(() => {
  const k = keyword.value.trim().toLowerCase();
  if (!k) return list.value;
  return list.value.filter(
    (it) => (it.title ?? "").toLowerCase().includes(k) || (it.content ?? "").toLowerCase().includes(k)
  );
});
/** 미해결 투영 = OPEN·PENDING (RESOLVED 제외) */
const openItems = computed<KeepItem[]>(() => filtered.value.filter((it) => it.lifecycle?.lifecycleKey !== "RESOLVED"));
/** 완료 투영 = RESOLVED */
const resolvedItems = computed<KeepItem[]>(() => filtered.value.filter((it) => it.lifecycle?.lifecycleKey === "RESOLVED"));

/** 현재 보류(PENDING) 상태 여부. */
function isPending(item: KeepItem): boolean {
  return item.lifecycle?.lifecycleKey === "PENDING";
}

/** 월 2자리 표기(발생지 표시용). 값이 없으면 빈 문자열. */
function padMonth(mnth?: number): string {
  return mnth != null ? String(mnth).padStart(2, "0") : "";
}

/**
 * 전 범위 목록 조회 (GET /api/journal/todos?scope=all).
 * 미해결·완료를 함께 받아 클라이언트에서 그룹·검색 투영한다.
 */
async function fetchList(): Promise<void> {
  error.value = null;
  try {
    const res = await axios.get("/api/journal/todos", { params: { scope: "all" } });
    if (!res.data?.rslt) {
      error.value = res.data?.message ?? t("journal.todo.list.load.failure");
      return;
    }
    list.value = (res.data?.rsltList ?? []) as KeepItem[];
  } catch (e: unknown) {
    error.value = t("journal.todo.list.load.failure");
    console.error("[keep] fetchList failed", e);
  }
}

/**
 * 라이프사이클 전이 (PUT /api/lifecycles). 성공 시 목록을 재조회한다.
 * 아사이드 카드와 동일 계약이며, keep 은 완료(RESOLVED)도 화면에 유지한다.
 */
async function setLifecycle(id: number, lifecycleKey: "OPEN" | "PENDING" | "RESOLVED"): Promise<void> {
  try {
    const res = await axios.put("/api/lifecycles", { id, contentType: "JOURNAL_TODO", lifecycleKey });
    if (res.data?.rslt === true) {
      void fetchList();
    } else {
      void swalAlert(res.data?.message ?? t("common.result.failure"));
    }
  } catch (e: unknown) {
    void swalRequestError(e);
  }
}
/** 완료 처리 (RESOLVED 전이) — 완료 그룹으로 이동한다. */
function completeTodo(id: number): void { void setLifecycle(id, "RESOLVED"); }
/** 보류 처리 (PENDING 전이). */
function setPending(id: number): void { void setLifecycle(id, "PENDING"); }
/** 보류 해제 (OPEN 전이). */
function releasePending(id: number): void { void setLifecycle(id, "OPEN"); }
/** 완료 되돌리기 (RESOLVED -> OPEN 전이) — 미해결 그룹으로 복귀한다. */
function reopenTodo(id: number): void { void setLifecycle(id, "OPEN"); }

/**
 * 신규 추가 모달 열기.
 * keep 에서 만든 항목은 저널 맥락이 없어, 발생지(yy/mnth)로 현재 년/월을 기본 전달한다.
 * (발생지 의미의 재정의는 별도 결정 대상이다.)
 */
function openAdd(): void {
  const now = new Date();
  modalStore.openTodoRegist({ yy: now.getFullYear(), mnth: now.getMonth() + 1 });
}

/**
 * 수정 모달 열기 — 상세(GET /api/journal/todo/:id)를 조회해 기존 데이터로 modify 모드로 연다.
 * 저장 성공 시 모달 close 를 watch 하여 목록을 재조회한다.
 */
async function openTodoEdit(id: number): Promise<void> {
  try {
    const res = await axios.get(`/api/journal/todo/${id}`);
    const dto = (res.data?.rsltObj ?? res.data?.obj) as {
      id?: number;
      yy?: number;
      mnth?: number;
      categoryCode?: string;
      title?: string;
      sortOrder?: number;
      content?: string;
      tag?: { tagListStrWithCtgr?: string };
    } | undefined;
    if (res.data?.rslt !== true || dto?.id == null) {
      void swalAlert(res.data?.message ?? t("common.result.failure"));
      return;
    }
    modalStore.openTodoRegist({
      id: dto.id,
      yy: dto.yy,
      mnth: dto.mnth,
      categoryCode: dto.categoryCode,
      title: dto.title,
      sortOrder: dto.sortOrder,
      content: dto.content,
      tag: { tagListStrWithCtgr: dto.tag?.tagListStrWithCtgr ?? "" },
    });
  } catch (e: unknown) {
    void swalRequestError(e);
  }
}

// 등록/수정 모달이 닫히면(저장 포함) 전 범위 목록을 재조회한다.
// 모달 submit 은 journalStore.fetchTodos(활성)만 갱신하므로 keep 은 여기서 별도 재조회한다.
watch(
  () => modalStore.todoRegistOpen,
  (open, prev) => {
    if (prev && !open) void fetchList();
  }
);

onMounted(() => {
  void fetchList();
});
</script>