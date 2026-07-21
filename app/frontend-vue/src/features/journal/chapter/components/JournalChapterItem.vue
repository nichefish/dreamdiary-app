<template>
  <!--begin::챕터-->
  <div class="journal-chapter-block" :id="'journal-chapter-' + chapter.id">
    <!--begin::챕터 헤더-->
    <div class="d-flex align-items-center mt-2">
      <!--begin::챕터 타입·카테고리 라벨 + 아이콘-->
      <div
        class="d-flex-align-center fs-6 ps-1 ps-md-5 me-5 fw-bolder"
        :class="isDreamChapter ? 'journal-dream-section-header' : 'text-gray-700'"
      >
        <span class="me-2">
          {{ typeLabel }}<template v-if="chapter.categoryCode">:</template>
          <template v-if="chapter.categoryCode">
            <span v-if="chapter.categoryName" style="color:#287D94;">{{ chapter.categoryName }}</span>
            <span class="text-muted fs-8 me-1">{{ chapter.categoryCode }}</span>
          </template>
          <span v-if="!canManageChapter" class="badge badge-light-danger fs-8 ms-1">{{ t("journal.chapter.other-author") }}</span>
        </span>
        <i class="bi fs-4" :class="iconClass"></i>
      </div>
      <!--end::챕터 타입·카테고리 라벨 + 아이콘-->

      <!--begin::챕터 액션 버튼 (col-3) — 본인 작성 챕터만-->
      <div v-if="canManageChapter" class="col-3 d-none d-md-flex align-items-center gap-2">
        <!--begin::엔트리 등록 TEXT 버튼-->
        <button
          v-if="canWriteChapter"
          type="button"
          class="btn btn-sm btn-light-primary btn-outlined ps-4 pe-3 py-2 cursor-pointer"
          :title="entryRegistTitle"
          @click="openEntryNew"
        >
          <i class="bi fs-4 pe-1" :class="entryRegistIcon"></i>
          {{ entryRegistLabel }}
        </button>
        <!--end::엔트리 등록 TEXT 버튼-->
        <!--begin::복사 버튼-->
        <button
          type="button"
          class="btn btn-sm btn-light-primary btn-outlined ms-2 px-3 cursor-pointer"
          :title="t('common.copy')"
          @click="copyChapter"
        >
          <i class="bi bi-copy p-0"></i>
        </button>
        <!--end::복사 버튼-->
        <!--begin::TXT보내기 버튼-->
        <button
          type="button"
          class="btn btn-sm btn-outline btn-light-primary ps-3 pe-2"
          :title="t('common.export-text')"
          @click="exportChapter"
        >
          <i class="fas fa-download"></i>
        </button>
        <!--end::TXT보내기 버튼-->
        <!--begin::컨텍스트 메뉴 (⋯)-->
        <div class="me-0 d-flex align-items-center">
          <button
            class="btn btn-sm btn-icon btn-bg-light btn-active-color-primary"
            data-kt-menu-trigger="click"
            data-kt-menu-placement="bottom-end"
            :title="t('common.menu')"
          >
            <i class="ki-solid ki-dots-horizontal fs-2x"></i>
          </button>
          <div class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold w-200px py-3" data-kt-menu="true">
            <div class="menu-item px-3">
              <div class="menu-content text-muted pb-2 px-3 fs-7 text-uppercase">{{ t("journal.chapter.label") }}</div>
            </div>
            <!--begin::수정-->
            <div v-if="canWriteChapter" class="menu-item px-3 my-1">
              <div class="menu-link flex-stack px-3" @click="openChapterModify">
                {{ t("common.edit") }}
                <i class="bi bi-pencil-square fs-8"></i>
              </div>
            </div>
            <!--end::수정-->
            <!--begin::상태 서브메뉴-->
            <div v-if="canWriteChapter" class="menu-item px-3" data-kt-menu-trigger="hover" data-kt-menu-placement="right-end">
              <a href="#" class="menu-link px-3" @click.prevent>
                <span class="menu-title">{{ t("common.status") }}</span>
                <span class="menu-arrow"></span>
              </a>
              <div class="menu-sub menu-sub-dropdown w-175px py-4">
                <div class="menu-item px-3">
                  <div class="menu-content px-3">
                    <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
                      <input
                        class="form-check-input w-30px h-20px cursor-pointer chapter-context-collapsed-check"
                        type="checkbox"
                        :checked="serverCollapsed"
                        @click.prevent="toggleCollapsedState"
                      />
                      <span class="form-check-label text-muted fs-7">{{ t("journal.chapter.collapsed") }}</span>
                    </label>
                  </div>
                </div>
              </div>
            </div>
            <!--end::상태 서브메뉴-->
            <div v-if="canWriteChapter" class="separator my-2"></div>
            <!--begin::삭제-->
            <div v-if="canWriteChapter" class="menu-item px-3 my-1">
              <div class="menu-link flex-stack px-3 text-danger" @click="deleteChapter">
                {{ t("common.delete") }}
                <i class="bi bi-trash text-danger p-0 fs-8"></i>
              </div>
            </div>
            <!--end::삭제-->
          </div>
        </div>
        <!--end::컨텍스트 메뉴-->
        <!--begin::접힘 토글 버튼 (클라이언트 DOM만, 서버 상태 무변경 — 레거시 toggleChapter 동일)-->
        <button
          type="button"
          class="btn btn-sm btn-secondary ms-2 px-3 toggle-chapter-btn"
          @click="toggleChapter"
        >
          <i
            class="bi pe-0"
            :class="isCollapsed ? 'bi-arrows-expand' : 'bi-arrows-collapse'"
            :id="'chapter-toggle-icon-' + chapter.id"
          ></i>
        </button>
        <!--end::접힘 토글 버튼-->
      </div>
      <!--end::챕터 액션 버튼-->
    </div>
    <!--end::챕터 헤더-->

    <!--begin::챕터 내용-->
    <div class="journal-chapter-item" :data-id="chapter.id" :data-collapsed="isCollapsed ? 'Y' : 'N'">
      <div :class="['journal-chapter-content', { collapsed: isCollapsed }]">
        <JournalEntryItem
          v-for="entry in entryList"
          :key="entry.id"
          :dom-id="entry.id ? entryDomIdPrefix + entry.id : undefined"
          :entry="entry"
          :is-dream="chapter.chapterType === 'DREAM'"
          :force-collapsed="localCollapsedOverride"
        />
        <div v-if="entryList.length === 0 && !isCollapsed" class="text-muted fs-8 ps-5 py-2">{{ t("journal.chapter.empty") }}</div>
      </div>
      <!--begin::접힘 시 태그 요약 (챕터 접힌 상태에서만 표시)-->
      <div v-if="tagList.length > 0 && isCollapsed" class="journal-chapter-tags d-flex flex-wrap gap-1 ps-5 py-1">
        <span
          v-for="tag in tagList"
          :key="tag.tagId"
          class="text-muted cursor-pointer pe-1"
          @click.stop="openTagContextMenu($event, tag)"
        >#<span class="border-bottom text-primary fw-lighter opacity-hover"><span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>{{ tag.name }}</span></span>
      </div>
      <!--end::접힘 시 태그 요약-->
    </div>
    <!--end::챕터 내용-->
  </div>
  <!--end::챕터-->
</template>

<script setup lang="ts">
import { swalConfirm, swalAlert, swalRequestError, swalFire, swalAjaxResult } from "@/shared/utils/swal";
import { computed, ref, watch, nextTick } from "vue";
import { useRoute } from "vue-router";
import axios from "axios";
import { useTagContextMenuStore } from "@/features/journal/stores/tagContextMenu";
import { useJournalModalStore } from "@/features/journal/stores/journalModal";
import { useJournalStore } from "@/features/journal/stores/journal";
import { refreshJournalDaysForRoute } from "@/features/journal/utils/journalDayRefresh";
import type { JournalChapterDto } from "@/features/journal/stores/journal";
import { getWeekDayStr } from "@/features/journal/utils/journalDate";
import { htmlToPlainText } from "@/features/journal/utils/htmlToPlainText";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import JournalEntryItem from "../../entry/components/JournalEntryItem.vue";
import { useJournalDayResolved } from "@/features/journal/utils/journalDayResolved";

const props = withDefaults(defineProps<{
  chapter: JournalChapterDto;
  entryDomIdPrefix?: string;
}>(), {
  entryDomIdPrefix: "journal-entry-",
});

const modalStore = useJournalModalStore();
const journalStore = useJournalStore();
const tagContextMenuStore = useTagContextMenuStore();
const { t } = useLocaleStore();
const route = useRoute();

/** 서버 COLLAPSED 상태 (⋯ 메뉴 접힘 스위치·목록 재조회 반영) */
const serverCollapsed = computed(() =>
  (props.chapter.state?.list ?? []).some((s) => s.stateKey === "COLLAPSED")
);

/**
 * 클라이언트 임시 접힘 (우측 화살표 버튼).
 * 변경 전: 우측 버튼이 toggleCollapsedState(서버 POST)를 호출해 레거시 toggleChapter(클라이언트만)와 달랐음.
 * 변경 후: 레거시 journalChapterCrudService.toggleChapter 와 동일하게 로컬만 토글.
 */
const localCollapsedOverride = ref<boolean | null>(null);

const isCollapsed = computed(() => {
  if (localCollapsedOverride.value !== null) return localCollapsedOverride.value;
  /* 하위 엔트리 전체 RESOLVED 시 자동 접힘 */
  if (allEntriesResolved.value) return true;
  return serverCollapsed.value;
});

watch(serverCollapsed, () => {
  localCollapsedOverride.value = null;
});

/** 본인 작성 챕터만 수정·삭제·엔트리 등록·서버 상태 변경 가능 (백엔드 isCreatedBy) */
const canManageChapter = computed(() => props.chapter.isCreatedBy === true);

const dayResolvedAxis = useJournalDayResolved();
/** NOTE 챕터 포함 일기 축 — diaryResolvedYn 잠금 시 쓰기 불가 */
const canWriteChapter = computed(
  () => canManageChapter.value && dayResolvedAxis.value.diaryWritable,
);

const isDreamChapter = computed(() => props.chapter.chapterType === "DREAM");
const isNoteChapter = computed(() => props.chapter.chapterType === "NOTE");

/** 소유권·일기 축 잠금 없을 때만 쓰기 허용 */
function guardChapterOwner(forWrite = false): boolean {
  if (!canManageChapter.value) {
    void swalAlert(t("journal.chapter.owner-only"));
    return false;
  }
  if (forWrite && !dayResolvedAxis.value.diaryWritable) {
    void swalAlert(t("journal.day.diary-resolved-locked"));
    return false;
  }
  return true;
}

const entryRegistIcon = computed(() => (isDreamChapter.value ? "bi-moon-stars" : "bi-book"));
const entryRegistLabel = computed(() => {
  if (isDreamChapter.value) return t("journal.dream.reg");
  if (isNoteChapter.value) return t("journal.note.reg");
  return t("journal.diary.reg");
});
const entryRegistTitle = computed(() => entryRegistLabel.value);

const iconClass = computed(() => {
  if (props.chapter.chapterType === "DREAM") return "bi-moon-stars";
  if (props.chapter.chapterType === "NOTE") return "bi-journal-text";
  return "bi-book";
});

const typeLabel = computed(() => {
  if (props.chapter.chapterType === "DREAM") return t("journal.chapter.type.dream");
  if (props.chapter.chapterType === "NOTE") return t("journal.chapter.type.note");
  return t("journal.chapter.type.diary");
});

const entryList = computed(() => props.chapter.journalEntryList ?? []);
const tagList = computed(() => props.chapter.tag?.list ?? []);

/** 하위 엔트리가 1개 이상이고 전부 RESOLVED인지 여부 */
const allEntriesResolved = computed(() => {
  const list = entryList.value;
  return list.length > 0 && list.every((e) => e.lifecycle?.lifecycleKey === 'RESOLVED');
});

/**
 * 하위 엔트리 중 데이터 규칙(RESOLVED 자동 접힘 · 서버 COLLAPSED)으로 접히는 것이 있는지 여부.
 * 엔트리 개별 로컬 토글(localCollapsedOverride)은 각 엔트리가 소유해 챕터에서 볼 수 없으므로
 * 데이터 기준으로만 판정한다 — 사용자가 손으로 접은 엔트리는 감지하지 못하는 근사치다.
 */
const hasDataCollapsedEntry = computed(() =>
  entryList.value.some((e) =>
    e.lifecycle?.lifecycleKey === 'RESOLVED'
    || (e.state?.list ?? []).some((s) => s.stateKey === 'COLLAPSED')
  )
);

/** 챕터 접힘 상태 태그 클릭 컨텍스트 메뉴 열기 */
function openTagContextMenu(event: MouseEvent, tag: { tagId: number | string; name: string; ctgr?: string }): void {
  const contentType = isDreamChapter.value
    ? "JOURNAL_DREAM"
    : isNoteChapter.value
      ? "JOURNAL_NOTE"
      : "JOURNAL_DIARY";
  tagContextMenuStore.open(event, {
    tagId: tag.tagId,
    name: tag.name,
    ctgr: tag.ctgr ?? "",
    contentType,
  });
}

/** 챕터 수정 모달 열기 */
function openChapterModify() {
  if (!guardChapterOwner(true)) return;
  modalStore.openChapterRegist({
    id: props.chapter.id,
    journalDayId: props.chapter.journalDayId,
    stdrdDt: props.chapter.stdrdDt,
    chapterType: props.chapter.chapterType,
    categoryCode: props.chapter.categoryCode,
    title: props.chapter.title,
    sortOrder: props.chapter.sortOrder,
  });
}

/** 일기/노트/꿈 엔트리 신규 등록 모달 열기 */
function openEntryNew() {
  if (!guardChapterOwner(true)) return;
  if (!props.chapter.journalDayId) return;
  /* NOTE 챕터 엔트리도 백엔드 contentType 은 JOURNAL_DIARY (JournalEntryTypeResolver). */
  const contentType = isDreamChapter.value ? "JOURNAL_DREAM" : "JOURNAL_DIARY";
  modalStore.openEntryRegist({
    contentType,
    journalDayId: props.chapter.journalDayId,
    journalChapterId: props.chapter.id,
    stdrdDt: props.chapter.stdrdDt,
    chapterList: [{ id: props.chapter.id, title: props.chapter.title, categoryCode: props.chapter.categoryCode, categoryName: props.chapter.categoryName, sortOrder: props.chapter.sortOrder }],
  });
}

/**
 * 클라이언트 접힘/펼침 (서버 상태 저장 없음).
 *
 * 변경 전: 항상 `!isCollapsed` 로 토글했다. 챕터가 펼쳐진 상태에서 하위 엔트리만 접혀 있으면
 * (일부 엔트리만 RESOLVED 등) 첫 클릭이 챕터를 접어버려, 엔트리를 펼치려면 접기→펼치기 2클릭이 필요했다.
 * 원인은 엔트리에 넘기는 forceCollapsed(=localCollapsedOverride)가 토글 전까지 null 이라
 * "챕터가 펼쳐져 있다"는 신호가 엔트리에 전달되지 않았기 때문이다.
 *
 * 변경 후: 아직 사용자가 토글하지 않았고(override=null) 챕터가 펼쳐져 있는데 데이터 규칙으로
 * 접힌 엔트리가 있으면, 접기 대신 override=false 로 하위 엔트리를 전체 펼친다(1클릭).
 * 이후 클릭부터는 override 가 non-null 이므로 기존대로 접기/펼치기 토글이 동작한다.
 *
 * 트레이드오프: 이 상태에서 "챕터를 접으려면" 전체 펼침 후 한 번 더 눌러야 한다(접기 2클릭).
 */
function toggleChapter(): void {
  if (localCollapsedOverride.value === null && !isCollapsed.value && hasDataCollapsedEntry.value) {
    localCollapsedOverride.value = false;
    return;
  }
  localCollapsedOverride.value = !isCollapsed.value;
}

/** fetchDays 완료 후 해당 일자로 스크롤 */
function scrollAfterFetch(stdrdDt = props.chapter.stdrdDt): void {
  const dt = stdrdDt;
  if (!dt) return;
  const afterFetch = () => {
    void nextTick(() => {
      const el = document.getElementById(`journal-day-${dt}`);
      if (el) el.scrollIntoView({ behavior: "smooth", block: "start" });
    });
  };

  void refreshJournalDaysForRoute(journalStore, route, dt).then(afterFetch);
}

/** 챕터 접힘 상태 토글 (서버 반영 후 목록 갱신 — ⋯ 메뉴 전용) */
async function toggleCollapsedState(): Promise<void> {
  if (!guardChapterOwner(true)) return;
  if (!props.chapter.id) return;
  try {
    await axios.post("/api/states", {
      id: props.chapter.id,
      contentType: "JOURNAL_CHAPTER",
      stateKey: "COLLAPSED",
    });
    localCollapsedOverride.value = null;
    scrollAfterFetch();
  } catch {
    console.error("[JournalChapterItem] toggleCollapsedState 실패");
  }
}

/** 챕터 TXT보내기 (레거시 exportTxt — 페이지 다운로드) */
function exportChapter(): void {
  if (!props.chapter.id) return;
  window.location.href = `/api/journal/chapter/${props.chapter.id}/export`;
}

/** 챕터 삭제 */
async function deleteChapter(): Promise<void> {
  if (!guardChapterOwner(true)) return;
  if (!props.chapter.id) return;
  const stdrdDt = props.chapter.stdrdDt;
  if (!await swalConfirm(t("journal.chapter.delete.confirm"))) return;
  try {
    const res = await axios.delete(`/api/journal/chapter/${props.chapter.id}`);
    if (res.data?.rslt) {
      await swalAjaxResult({
        rslt: true,
        message: res.data?.message,
        successFallback: t("common.result.deleted"),
      });
      scrollAfterFetch(stdrdDt);
    } else {
      void swalAjaxResult({
        rslt: false,
        message: res.data?.message,
        failureFallback: t("journal.chapter.delete.failure"),
      });
    }
  } catch (e: unknown) {
    void swalRequestError(e, t("common.error.processing"));
  }
}

/** HTML 태그 제거 후 일반 텍스트로 변환 (줄바꿈 보존) */
/** 챕터 전체 내용을 클립보드에 복사. 레거시 형식: 날짜(요일) / 카테고리 / 제목\n#순번\n본문 */
async function copyChapter(): Promise<void> {
  const lines: string[] = [];
  const headerParts: string[] = [];
  if (props.chapter.stdrdDt) {
    const weekDay = getWeekDayStr(props.chapter.stdrdDt, t);
    headerParts.push(weekDay ? `${props.chapter.stdrdDt} (${weekDay})` : props.chapter.stdrdDt);
  }
  if (props.chapter.categoryName) headerParts.push(props.chapter.categoryName);
  if (props.chapter.title) headerParts.push(props.chapter.title);
  if (headerParts.length > 0) lines.push(headerParts.join(" / "));
  for (const entry of entryList.value) {
    const sortNum = entry.sortOrder != null ? "#" + String(entry.sortOrder) : "";
    /* content = TinyMCE HTML 원문(마크다운 재처리 이전); markdownContent = MarkdownUtils 처리 후 HTML */
    const raw = htmlToPlainText(entry.content ?? entry.markdownContent ?? "");
    if (sortNum) lines.push(sortNum);
    if (raw) lines.push(raw);
    lines.push("");
  }
  const text = lines.join("\n").trim();
  try {
    await navigator.clipboard.writeText(text);
    void swalFire({ icon: "success", text: t("common.copy.success") });
  } catch (error: unknown) {
    console.error("[journal-chapter] clipboard copy failed", error);
    void swalFire({ icon: "error", text: t("common.copy.failure") });
  }
}
</script>
