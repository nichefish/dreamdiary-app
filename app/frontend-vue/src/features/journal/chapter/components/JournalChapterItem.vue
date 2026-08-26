<template>
  <!--begin::챕터-->
  <div
    class="journal-chapter-block"
    :class="{
      'is-summary-chapter': isSummaryChapter,
      'is-all-pending': allEntriesPending,
      'is-all-resolved': allEntriesResolved,
    }"
    :id="'journal-chapter-' + chapter.id"
    :data-collapsed="isCollapsed ? 'Y' : 'N'"
    :data-agg-lifecycle="aggregateLifecycleKey ?? ''"
    :data-local-ovr="localCollapsedOverride === null ? 'null' : String(localCollapsedOverride)"
    :data-server-collapsed="serverCollapsed ? 'Y' : 'N'"
  >
    <div
      v-if="debugCollapse"
      class="fs-9 text-danger px-2"
    >
      [C#{{ chapter.id }}] isCollapsed={{ isCollapsed }} | agg={{ aggregateLifecycleKey }} | localOvr={{ localCollapsedOverride }} | server={{ serverCollapsed }} | allResolved={{ allEntriesResolved }} | allPending={{ allEntriesPending }} | forceToEntries={{ localCollapsedOverride }}
    </div>
    <!--begin::챕터 헤더-->
    <div class="d-flex align-items-center mt-2">
      <!--begin::챕터 타입·말머리 라벨 + 아이콘-->
      <div
        class="d-flex-align-center fs-6 ps-1 ps-md-5 me-5 fw-bolder"
        :class="isDreamChapter ? 'journal-dream-section-header' : 'text-gray-700'"
      >
        <span class="me-2">
          {{ typeLabel }}<template v-if="isSummaryChapter || chapter.prefix">:</template>
          <template v-if="isSummaryChapter">
            <span style="color:#287D94;">{{ t("journal.chapter.summary") }}</span>
          </template>
          <template v-else-if="chapter.prefix">
            <span :style="{ color: chapter.prefix.color || '#287D94' }">{{ chapter.prefix.name }}</span>
          </template>
          <!--begin::챕터 제목 (분류 뒤 인라인 강조, 접힘·펼침 무관 항상) — 같은 분류 챕터 구분용 -->
          <span v-if="chapter.title" class="ms-1 text-gray-700">· {{ chapter.title }}</span>
          <!--end::챕터 제목-->
          <span v-if="!canManageChapter" class="badge badge-light-danger fs-8 ms-1">{{ t("journal.chapter.other-author") }}</span>
        </span>
        <i class="bi fs-4" :class="iconClass"></i>
      </div>
      <!--end::챕터 타입·말머리 라벨 + 아이콘-->

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
        <!--begin::복사 (split — 주 버튼=전체/해석 포함, ▾ 드롭다운=본문만/해석 제외)-->
        <div class="btn-group ms-2" role="group">
          <!--begin::주 버튼 (해석 포함)-->
          <button
            type="button"
            class="btn btn-sm btn-light-primary btn-outlined px-3 cursor-pointer copy-split-main"
            :title="chapterHasReflections ? t('journal.copy.full.tooltip') : t('common.copy')"
            @click="copyChapter('full')"
          >
            <i class="bi bi-copy p-0"></i>
          </button>
          <!--end::주 버튼-->
          <!--begin::본문만 드롭다운 (항상 노출 — 하위 리플렉션 없으면 본문=전체라 결과 동일)-->
          <button
            type="button"
            class="btn btn-sm btn-light-primary btn-outlined cursor-pointer copy-split-caret"
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
              <div class="menu-link flex-stack px-3" @click="copyChapter('no-pending')">
                {{ t('journal.copy.no-pending.label') }}
                <i class="bi bi-copy fs-8"></i>
              </div>
            </div>
            <div class="menu-item px-3 my-1 cursor-pointer">
              <div class="menu-link flex-stack px-3" @click="copyChapter('body')">
                {{ t('journal.copy.body.label') }}
                <i class="bi bi-clipboard fs-8"></i>
              </div>
            </div>
          </div>
          <!--end::본문만 드롭다운-->
        </div>
        <!--end::복사 (split)-->
        <!--begin::TXT보내기 (split — 주 버튼=전체/해석 포함, ▾ 드롭다운=본문만/해석 제외)-->
        <div class="btn-group" role="group">
          <button
            type="button"
            class="btn btn-sm btn-outline btn-light-primary ps-3 pe-2 copy-split-main"
            :title="chapterHasReflections ? t('journal.download.full.tooltip') : t('common.export-text')"
            @click="exportChapter(true)"
          >
            <i class="fas fa-download"></i>
          </button>
          <button
            type="button"
            class="btn btn-sm btn-outline btn-light-primary copy-split-caret"
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
              <div class="menu-link flex-stack px-3" @click="exportChapter(false)">
                {{ t('journal.download.body.label') }}
                <i class="fas fa-download fs-8"></i>
              </div>
            </div>
          </div>
        </div>
        <!--end::TXT보내기 (split)-->
        <!--begin::컨텍스트 메뉴 (⋯) — 시스템 요약 챕터는 사용자 편집 대상이 아니므로 숨긴다(엔트리 등록·복사·TXT 는 유지). -->
        <div v-if="!isSummaryChapter" class="me-0 d-flex align-items-center">
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
          :is-summary="entry.id != null && entry.id === summaryEntryId"
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
      <!--begin::접힘 시 소속 스레드 요약 (하위 엔트리의 스레드를 중복 없이 표시)-->
      <div
        v-if="chapterThreadList.length > 0 && isCollapsed"
        class="journal-chapter-threads d-flex flex-wrap align-items-center gap-1 ps-5 py-1"
      >
        <i class="bi bi-diagram-3 fs-8 text-muted"></i>
        <button
          v-for="thread in chapterThreadList"
          :key="'chapter-thread-' + thread.threadId"
          type="button"
          class="badge badge-light-primary border-0 fs-8 cursor-pointer"
          :title="t('journal.entry.thread.open.tooltip')"
          @click.stop="openThreadDetail(thread.threadId)"
        >
          {{ thread.threadTitle || ('#' + thread.threadId) }}
        </button>
      </div>
      <!--end::접힘 시 소속 스레드 요약-->
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
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
import { refreshJournalDaysForRoute } from "@/features/journal/utils/journalDayRefresh";
import type { JournalChapterDto, JournalThreadEntryDto } from "@/features/journal/stores/journal";
import { getWeekDayStr } from "@/features/journal/utils/journalDate";
import { htmlToPlainText } from "@/features/journal/utils/htmlToPlainText";
import {
  appendReflectionsToCopyText,
  type CopyReflectionMode,
  copySuccessKey,
  JOURNAL_COPY_LINE_BREAK,
} from "@/features/journal/utils/journalCopyReflection";
import { findFirstNonEmptyEntry } from "@/features/journal/utils/summaryEntryPreview";
import {
  resolveChapterAggregateLifecycle,
  resolveChapterCollapsed,
} from "@/features/journal/utils/journalLifecycleCollapse";
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
const threadStore = useJournalThreadStore();
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
  return resolveChapterCollapsed({
    localOverride: localCollapsedOverride.value,
    aggregateLifecycleKey: aggregateLifecycleKey.value,
    serverCollapsed: serverCollapsed.value,
  });
});

watch(serverCollapsed, () => {
  localCollapsedOverride.value = null;
});

/**
 * 신규 엔트리 등록 대상 챕터는 완료 자동 접힘·서버 COLLAPSED보다 우선해 현재 화면에서 펼친다.
 * 서버 상태는 유지하며, 목록과 일자 상세에 같은 챕터가 동시에 있으면 각 인스턴스가 동일 신호를 적용한다.
 */
watch(
  () => modalStore.entryCreatedExpandChapterId,
  (chapterId) => {
    if (chapterId == null || String(chapterId) !== String(props.chapter.id)) return;
    localCollapsedOverride.value = false;
    console.info("[JournalChapterItem] 신규 엔트리 등록 챕터를 펼침", { chapterId });
  },
  { immediate: true },
);

/** 본인 작성 챕터만 수정·삭제·엔트리/리플렉션 등록·서버 상태 변경 가능 (백엔드 isCreatedBy) */
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

/**
 * 챕터 엔트리 목록(journal_entry 행: 일기·꿈·노트). Reflection 은 별도 Aggregate(journal_reflection)이라
 * 이 목록에 1급 행으로 들어오지 않고, 대상 엔트리 아래 embed(reflectionList)로만 표시된다.
 */
const entryList = computed(() => props.chapter.journalEntryList ?? []);
const tagList = computed(() => props.chapter.tag?.list ?? []);

/**
 * 이 챕터가 서버가 지정한 그날 요약 챕터인지 여부.
 * 프리뷰용 findSummaryChapter 의 non-DREAM fallback 과 달리 summaryYn 엄격 매칭만 쓴다 —
 * 요약 챕터가 없는 날에 임의 챕터를 요약으로 강조하지 않기 위함이다.
 */
const isSummaryChapter = computed(() => props.chapter.summaryYn === "Y");

/** 하위 엔트리에 리플렉션(해석)이 하나라도 있는지 — "해석 제외 복사" 버튼 노출 조건. */
const chapterHasReflections = computed(() => entryList.value.some((e) => (e.reflectionList?.length ?? 0) > 0));

/** 요약 강조 대상 엔트리 id — 시스템 요약 챕터의 첫 non-empty 엔트리(그날 전체 요약). 그 외엔 undefined. */
const summaryEntryId = computed(() =>
  isSummaryChapter.value ? findFirstNonEmptyEntry(entryList.value)?.id : undefined,
);

/**
 * 접힌 챕터 밖에 표시할 소속 스레드 목록.
 * <p>
 * 변경 전에는 스레드 칩이 각 엔트리 본문 안에만 있어 챕터 접기 시 함께 숨겨졌다.
 * 변경 후에는 하위 엔트리의 소속을 현재 엔트리 순서대로 모으고 threadId로 중복 제거해,
 * 태그 요약과 같은 접힘 바깥 영역에서 스레드의 존재를 유지한다.
 */
const chapterThreadList = computed<JournalThreadEntryDto[]>(() => {
  const threadMap = new Map<number, JournalThreadEntryDto>();
  for (const entry of entryList.value) {
    for (const thread of entry.threadList ?? []) {
      const previous = threadMap.get(thread.threadId);
      if (!previous || (!previous.threadTitle?.trim() && thread.threadTitle?.trim())) {
        threadMap.set(thread.threadId, thread);
      }
    }
  }
  return Array.from(threadMap.values());
});

/** 챕터가 전체 완료 또는 전체 보류 상태에 진입하면 자동 접힘이 적용되도록 임시 펼침 상태를 해제한다. */
const aggregateLifecycleKey = computed(() => resolveChapterAggregateLifecycle(entryList.value));

/** 하위 엔트리가 1개 이상이고 전부 RESOLVED인지 여부 */
const allEntriesResolved = computed(() => aggregateLifecycleKey.value === "RESOLVED");

/** 하위 엔트리가 1개 이상이고 전부 PENDING인지 여부 */
const allEntriesPending = computed(() => aggregateLifecycleKey.value === "PENDING");

/** localStorage("debug_collapse")=true 일 때 챕터 접힘/집계 메타정보를 표시한다. */
const debugCollapse = computed(() => localStorage.getItem("debug_collapse") === "true");

watch(
  aggregateLifecycleKey,
  (lifecycleKey, previousLifecycleKey) => {
    if (lifecycleKey === null || lifecycleKey === previousLifecycleKey) return;
    if (localCollapsedOverride.value === null) return;

    localCollapsedOverride.value = null;
    console.info('[JournalChapterItem] 전체 lifecycle 자동 접힘으로 임시 펼침 상태를 해제함', {
      chapterId: props.chapter.id,
      lifecycleKey,
    });
  },
  { immediate: true },
);

/**
 * 하위 엔트리 중 lifecycle 자동 접힘 또는 서버 COLLAPSED로 접히는 것이 있는지 여부.
 * 엔트리 개별 로컬 토글(localCollapsedOverride)은 각 엔트리가 소유해 챕터에서 볼 수 없으므로
 * 데이터 기준으로만 판정한다 — 사용자가 손으로 접은 엔트리는 감지하지 못하는 근사치다.
 */
const hasDataCollapsedEntry = computed(() =>
  entryList.value.some((e) =>
    e.lifecycle?.lifecycleKey === 'RESOLVED'
    || e.lifecycle?.lifecycleKey === 'PENDING'
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

/** 접힘 요약의 스레드 버튼에서 현재 저널 화면을 유지한 채 전역 상세 모달을 연다. */
function openThreadDetail(threadId: number): void {
  void threadStore.openDetail(threadId);
}

/** 챕터 수정 모달 열기 */
function openChapterModify() {
  if (!guardChapterOwner(true)) return;
  modalStore.openChapterRegist({
    id: props.chapter.id,
    journalDayId: props.chapter.journalDayId,
    stdrdDt: props.chapter.stdrdDt,
    chapterType: props.chapter.chapterType,
    prefixId: props.chapter.prefixId,
    prefix: props.chapter.prefix,
    summaryYn: props.chapter.summaryYn,
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
    chapterList: [{
      id: props.chapter.id,
      title: props.chapter.title,
      prefixId: props.chapter.prefixId,
      prefix: props.chapter.prefix,
      summaryYn: props.chapter.summaryYn,
      sortOrder: props.chapter.sortOrder,
      chapterType: props.chapter.chapterType,
    }],
  });
}

/**
 * 클라이언트 접힘/펼침. 서버 상태는 변경하지 않는다.
 * 챕터가 펼쳐진 상태에서 lifecycle 또는 서버 상태로 접힌 엔트리가 있으면 첫 클릭은 하위 엔트리를
 * 전체 펼친다. 이후 클릭은 챕터와 하위 엔트리를 함께 접거나 펼친다.
 */
function toggleChapter(): void {
  const before = {
    isCollapsed: isCollapsed.value,
    localOverride: localCollapsedOverride.value,
    aggregateLifecycleKey: aggregateLifecycleKey.value,
    allEntriesResolved: allEntriesResolved.value,
  };
  if (localCollapsedOverride.value === null && !isCollapsed.value && hasDataCollapsedEntry.value) {
    localCollapsedOverride.value = false;
    if (debugCollapse.value) {
      console.info("[JournalChapterItem] toggleChapter expand-entries-first", { chapterId: props.chapter.id, before, afterLocalOvr: false });
    }
    return;
  }
  localCollapsedOverride.value = !isCollapsed.value;
  if (debugCollapse.value) {
    console.info("[JournalChapterItem] toggleChapter", {
      chapterId: props.chapter.id,
      before,
      afterLocalOvr: localCollapsedOverride.value,
      afterCollapsed: resolveChapterCollapsed({
        localOverride: localCollapsedOverride.value,
        aggregateLifecycleKey: aggregateLifecycleKey.value,
        serverCollapsed: serverCollapsed.value,
      }),
    });
  }
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

/** 챕터 TXT보내기 (레거시 exportTxt — 페이지 다운로드). includeReflection: 해석 포함 여부(기본 true). */
function exportChapter(includeReflection = true): void {
  if (!props.chapter.id) return;
  window.location.href = `/api/journal/chapter/${props.chapter.id}/export?includeReflection=${includeReflection}`;
}

/** 챕터 삭제 */
async function deleteChapter(): Promise<void> {
  if (!guardChapterOwner(true)) return;
  if (!props.chapter.id) return;
  if (entryList.value.length > 0) {
    void swalAlert(t("journal.chapter.delete.blocked-by-entries"));
    return;
  }
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
/** 챕터 전체 내용을 클립보드에 복사. 형식: 날짜(요일) / 말머리 / 제목 → 각 엔트리 #순번·본문, 그 밑에 그 엔트리를 문(target) 리플렉션 본문(원문·해석은 한 몸으로 함께 복사). */
async function copyChapter(mode: CopyReflectionMode = "full"): Promise<void> {
  const lines: string[] = [];
  const headerParts: string[] = [];
  if (props.chapter.stdrdDt) {
    const weekDay = getWeekDayStr(props.chapter.stdrdDt, t);
    headerParts.push(weekDay ? `${props.chapter.stdrdDt} (${weekDay})` : props.chapter.stdrdDt);
  }
  if (isSummaryChapter.value) headerParts.push(t("journal.chapter.summary"));
  else if (props.chapter.prefix?.name) headerParts.push(props.chapter.prefix.name);
  if (props.chapter.title) headerParts.push(props.chapter.title);
  if (headerParts.length > 0) lines.push(headerParts.join(" / "));
  for (const entry of entryList.value) {
    const sortNum = entry.sortOrder != null ? "#" + String(entry.sortOrder) : "";
    /* content = TinyMCE HTML 원문(마크다운 재처리 이전); markdownContent = MarkdownUtils 처리 후 HTML */
    const raw = htmlToPlainText(entry.content ?? entry.markdownContent ?? "");
    const entryBaseText = [sortNum, raw].filter(Boolean).join(JOURNAL_COPY_LINE_BREAK);
    /* 공통 formatter가 모드별 리플렉션 포함 여부와 본문 사이의 CRLF 빈 줄 경계를 함께 보장한다. */
    const entryText = appendReflectionsToCopyText(entryBaseText, entry.reflectionList, mode);
    if (entryText) lines.push(entryText);
    lines.push("");
  }
  const text = lines.join(JOURNAL_COPY_LINE_BREAK).trim();
  try {
    await navigator.clipboard.writeText(text);
    /* 성공 토스트는 복사 범위를 명시한다: 전체/보류 제외/본문만, 리플렉션이 없으면 공용 문구. */
    const successKey = copySuccessKey(mode, chapterHasReflections.value);
    void swalFire({ icon: "success", text: t(successKey) });
  } catch (error: unknown) {
    console.error("[journal-chapter] clipboard copy failed", error);
    void swalFire({ icon: "error", text: t("common.copy.failure") });
  }
}
</script>

<style scoped>
/* 시스템 요약 챕터를 '그날 전체 요약' 카드로 분리 — 챕터 헤더+엔트리 전체를 중립 표면 카드로 감싼다.
   배경 tint·좌측선은 '같은 엔트리의 상태' 언어라 요약이 그 계열의 level 로 오인됐다.
   카드 재질(gray-100 표면+테두리+라운드+옅은 그림자)로 챕터 전체를 감싸 '다른 종류'로 읽히게 하고,
   아래는 margin 으로 다음 챕터와 띄운다. 다크모드: --bs-* 토큰 자동 대응. */
.is-summary-chapter {
  background: color-mix(in srgb, var(--bs-gray-100) 40%, var(--bs-body-bg));
  border: 1px solid var(--bs-gray-300);
  border-radius: 0.65rem;
  padding: calc(0.75rem - 1pt) 1.25rem calc(0.75rem - 3pt);
  margin-bottom: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}
</style>
