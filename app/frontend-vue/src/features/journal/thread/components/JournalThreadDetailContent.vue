<template>
  <div v-if="store.detailModel" class="journal-thread-dtl-vue-root">
    <!--begin::헤더 (제목 + 작성자/일시)-->
    <div class="mb-0">
      <div class="d-flex align-items-center flex-wrap gap-2 mb-2">
        <span v-if="store.detailModel?.prefix" class="ctgr-span" :style="{ borderColor: store.detailModel.prefix.color || '', color: store.detailModel.prefix.color || '' }">{{ store.detailModel.prefix.name }}</span>
        <span
          v-if="detailLifecycleKey"
          class="badge fs-8"
          :class="detailLifecycleBadgeClass"
        >{{ detailLifecycleLabel }}</span>
        <span class="fs-3 fw-bolder text-gray-900">{{ store.detailModel.title }}</span>
        <span
          v-if="membershipPeriodLabel"
          class="text-muted fs-7"
        >{{ membershipPeriodLabel }}</span>
        <!--begin::상세 라이프사이클 설정-->
        <div class="ms-auto">
          <button
            type="button"
            class="btn btn-sm btn-light"
            data-kt-menu-trigger="click"
            data-kt-menu-placement="bottom-end"
          >
            {{ t("common.lifecycle") }}
            <i class="bi bi-chevron-down fs-9 ms-1"></i>
          </button>
          <div
            class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold w-175px py-3"
            data-kt-menu="true"
          >
            <div v-for="lc in lifecycleOptions" :key="'thread-dtl-lc-' + lc.key" class="menu-item px-3">
              <div class="menu-content px-3">
                <label class="form-check form-check-custom form-check-solid cursor-pointer">
                  <input
                    class="form-check-input w-18px h-18px cursor-pointer"
                    type="radio"
                    name="thread-detail-lifecycle"
                    :value="lc.key"
                    :checked="detailLifecycleKey === lc.key"
                    @click="onSetLifecycle(lc.key)"
                  />
                  <span
                    class="form-check-label fs-7"
                    :class="detailLifecycleKey === lc.key ? lc.activeClass : 'text-muted'"
                  >{{ lc.label }}</span>
                </label>
              </div>
            </div>
          </div>
        </div>
        <!--end::상세 라이프사이클 설정-->
      </div>
      <div class="d-flex align-items-center flex-wrap gap-3 text-muted fs-7">
        <span v-if="store.detailModel.createdByNm"><i class="bi bi-person pe-1"></i>{{ store.detailModel.createdByNm }}</span>
        <span v-if="store.detailModel.createdDt"><i class="bi bi-clock pe-1"></i>{{ store.detailModel.createdDt }}</span>
      </div>
    </div>
    <!--end::헤더-->

    <div class="separator separator-dashed border-gray-300 my-8"></div>

    <!--begin::본문-->
    <div
      class="fs-4 fw-normal text-gray-800 px-5 py-1 pb-6 min-h-150px"
      v-html="store.detailModel.markdownContent || store.detailModel.content || ''"
    ></div>
    <!--end::본문-->

    <!--begin::소속 엔트리 태그 (백엔드 집계)-->
    <div v-if="hasDetailTags" class="mt-4">
      <i class="bi bi-tags me-1 text-gray-700"></i>
      <span class="fs-8 fw-semibold text-gray-600 me-2">{{ t("journal.thread.entry-tags.title") }}</span>
      <span
        v-for="tag in store.detailModel.tag?.list"
        :key="'thread-dtl-tag-' + String(tag.tagId)"
        class="text-muted pe-1"
      >
        <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>
        #<span class="border-bottom text-primary fw-lighter opacity-hover">{{ tag.name }}</span>
      </span>
    </div>
    <!--end::소속 엔트리 태그-->

    <!--begin::연관 스레드 목록 — 설계 §3: 스레드 피커 + 연관 목록 표시-->
    <div class="separator separator-dashed border-gray-200 my-6"></div>
    <div class="d-flex align-items-center gap-2 mb-3 px-5">
      <i class="bi bi-share text-gray-700"></i>
      <span class="fs-6 fw-bold text-gray-800">{{ t("journal.thread.related.title") }}</span>
      <span v-if="store.detailRelatedThreads.length > 0" class="badge badge-light-secondary">{{ store.detailRelatedThreads.length }}</span>
      <!--begin::연관 스레드 추가 버튼-->
      <button
        v-if="store.detailModel?.id"
        type="button"
        class="btn btn-xs btn-icon btn-bg-light btn-active-color-primary ms-auto"
        :title="t('journal.thread.related.add.tooltip')"
        @click="openRelatedThreadPicker"
      >
        <i class="bi bi-plus fs-7"></i>
      </button>
      <!--end::연관 스레드 추가 버튼-->
    </div>
    <div v-if="store.detailRelatedThreadsLoading" class="text-muted fs-7 px-5 py-2">{{ t("common.loading") }}</div>
    <div v-else-if="store.detailRelatedThreads.length === 0" class="text-muted fs-7 px-5 py-2">{{ t("journal.thread.related.empty") }}</div>
    <div v-else class="d-flex flex-column gap-1 px-3">
      <div
        v-for="rel in store.detailRelatedThreads"
        :key="'thread-related-' + String(rel.id)"
        class="d-flex align-items-center gap-2 py-1"
      >
        <i class="bi bi-diagram-3 fs-8 text-gray-500"></i>
        <span class="fs-7 text-gray-800 flex-grow-1">{{ rel.targetTitle ?? String(rel.targetId) }}</span>
        <button
          v-if="rel.targetId != null"
          type="button"
          class="btn btn-xs btn-icon"
          :class="isRelatedThreadIncluded(rel.targetId) ? 'btn-light-primary' : 'btn-light'"
          :style="isRelatedThreadIncluded(rel.targetId) ? { color: accentForRelatedThread(rel.targetId), borderColor: accentForRelatedThread(rel.targetId) } : undefined"
          :title="t('journal.thread.related.toggle.tooltip')"
          @click="onToggleRelatedThreadInclude(rel.targetId)"
        >
          <i class="bi bi-intersect fs-8"></i>
        </button>
        <button
          type="button"
          class="btn btn-xs btn-icon btn-light btn-active-color-danger"
          :title="t('journal.thread.related.remove.tooltip')"
          @click="onRemoveRelatedThread(rel)"
        >
          <i class="bi bi-x fs-8"></i>
        </button>
      </div>
    </div>
    <!--end::연관 스레드 목록-->

    <!--begin::소속 엔트리 목록-->
    <div class="separator separator-dashed border-gray-200 my-6"></div>
    <div class="d-flex align-items-center gap-2 mb-3 px-5">
      <i class="bi bi-diagram-3 text-gray-700"></i>
      <span class="fs-6 fw-bold text-gray-800">{{ t("journal.thread.entries.title") }}</span>
      <span v-if="store.detailEntries.length > 0" class="badge badge-light-primary">{{ store.detailEntries.length }}</span>
    </div>
    <div v-if="store.detailEntriesLoading" class="text-muted fs-7 px-5 py-2">{{ t("common.loading") }}</div>
    <div v-else-if="store.detailEntries.length === 0" class="text-muted fs-7 px-5 py-2">{{ t("journal.thread.entries.empty") }}</div>
    <div v-else class="px-3">
      <template v-for="group in entryGroups" :key="'thread-entry-group-' + (group.stdrdDt || 'nodate')">
        <!--begin::일자 헤더 — 저널 일자/연간 상세와 동일한 journal-day-header (날짜 + 요일)-->
        <div v-if="group.stdrdDt" class="journal-day-header mt-4 mb-1" :data-date="group.stdrdDt">
          <div class="d-flex flex-wrap align-items-center fs-5 fw-bold">
            <i class="bi bi-calendar3 fs-6 me-1"></i>
            <span>{{ group.stdrdDt }}</span>
            <span class="fs-8 text-gray-600">({{ getWeekDayStr(group.stdrdDt, t) }})</span>
          </div>
        </div>
        <!--end::일자 헤더-->
        <template v-for="entry in group.entries" :key="'thread-entry-' + entry.id">
          <!--begin::출처 칩 — 빌려온 엔트리만 (설계 §2 결정 4)-->
          <div class="thread-synth-entry">
            <div v-if="entry.sourceThreadId != null" class="thread-synth-source px-1 mb-1">
              <span
                class="badge fs-8 fw-semibold thread-synth-source__chip"
                :style="{
                  backgroundColor: accentForRelatedThread(entry.sourceThreadId),
                  borderColor: accentForRelatedThread(entry.sourceThreadId),
                  color: '#fff',
                }"
              >
                <i class="bi bi-link-45deg me-1"></i>{{ resolveRelatedThreadTitle(entry.sourceThreadId) }}
              </span>
            </div>
            <JournalEntryItem
              :entry="entry"
              :is-dream="entry.contentType === 'JOURNAL_DREAM'"
              :disable-lifecycle-collapse="true"
            />
          </div>
          <!--end::출처 칩-->
        </template>
      </template>
    </div>
    <!--end::소속 엔트리 목록-->

    <!--begin::댓글 영역-->
    <div class="separator separator-dashed border-gray-200 my-6"></div>
    <div class="d-flex align-items-center justify-content-between mb-3">
      <span class="fs-6 fw-bold text-gray-800">{{ t("comment.modal.title") }}</span>
      <div v-if="store.detailModel.id" class="d-flex gap-1">
        <button
          type="button"
          class="btn btn-xs btn-icon btn-bg-light btn-active-color-primary"
          :title="t('comment.register')"
          @click="openCommentRegist"
        >
          <i class="bi bi-chat-dots fs-8"></i>
        </button>
        <button
          v-if="commentCount > 0"
          type="button"
          class="btn btn-xs btn-light-primary"
          :title="t('journal.thread.comments.tooltip')"
          @click="openCommentList"
        >
          <i class="bi bi-chat-left-text fs-8 me-1"></i>
          {{ commentCount }}
        </button>
      </div>
    </div>
    <div v-if="commentList.length === 0" class="text-muted fs-7 py-2">{{ t("comment.modal.empty") }}</div>
    <div v-else class="d-flex flex-column gap-2">
      <div
        v-for="cmt in commentList"
        :key="cmt.id"
        class="fs-8 text-muted ps-2 border-start border-2 border-gray-300"
        v-html="cmt.markdownContent || cmt.content || ''"
      ></div>
    </div>
    <!--end::댓글 영역-->
    <!--begin::스레드 피커 모달-->
    <JournalThreadPickerModal />
    <!--end::스레드 피커 모달-->
  </div>
</template>

<script setup lang="ts">
import { computed, watch } from "vue";
import { useAttachableModalStore } from "@/features/attachable/stores/attachableModal";
import JournalEntryItem from "@/features/journal/entry/components/JournalEntryItem.vue";
import type { JournalEntryDto, RelatedContentItem } from "@/features/journal/stores/journal";
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
import { getWeekDayStr } from "@/features/journal/utils/journalDate";
import { formatThreadMembershipPeriod } from "@/features/journal/utils/threadMembershipPeriod";
import { reinitMetronicAfterDom } from "@/shared/utils/metronicReinit";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import JournalThreadPickerModal from "../modals/JournalThreadPickerModal.vue";

const store = useJournalThreadStore();
const attachableStore = useAttachableModalStore();
const { t } = useLocaleStore();

/**
 * 일자 그룹: 소속 엔트리를 stdrdDt 별로 묶은 렌더 단위.
 * stdrdDt 가 빈 문자열이면 일자 미상 그룹(헤더 없이 카드만 렌더)이다.
 */
interface ThreadEntryGroup {
  stdrdDt: string;
  entries: JournalEntryDto[];
}

/**
 * 소속 엔트리를 stdrdDt 별로 그룹핑한다.
 * <p>
 * 백엔드가 일자 → 원본 엔트리 sortOrder → ID 오름차순으로 내려주므로 first-seen 순서가 곧 표시 순서다.
 * 저널 일자·연간 상세와 동일하게 그룹마다 journal-day-header 를 얹어 시각을 통일한다.
 *
 * @param entries 백엔드가 정렬해 내려준 소속 엔트리 목록
 * @return 일자별 그룹 목록 (입력 순서 보존)
 */
function groupEntriesByDate(entries: JournalEntryDto[]): ThreadEntryGroup[] {
  const groups: ThreadEntryGroup[] = [];
  const groupMap = new Map<string, ThreadEntryGroup>();
  entries.forEach((entry) => {
    const stdrdDt = entry.stdrdDt ?? "";
    let group = groupMap.get(stdrdDt);
    if (!group) {
      group = { stdrdDt, entries: [] };
      groupMap.set(stdrdDt, group);
      groups.push(group);
    }
    group.entries.push(entry);
  });
  return groups;
}

const entryGroups = computed(() => groupEntriesByDate(store.detailEntries));

/**
 * 소속 엔트리가 렌더된 뒤 Metronic KTMenu(⋯ 컨텍스트 메뉴)를 재바인딩한다.
 * 검색·일자 화면과 달리 스레드 상세는 재초기화 경로가 없어 ⋯ 메뉴가 열리지 않았다(모달·독립 페이지 공용).
 * 최초 로드와 refreshOpenDetail 재조회 모두 detailEntries 재할당이라 함께 잡는다.
 */
watch(() => store.detailEntries, () => {
  void reinitMetronicAfterDom();
}, { immediate: true });

watch(() => store.detailModel?.id, () => {
  void reinitMetronicAfterDom();
});

const hasDetailTags = computed(() =>
  Array.isArray(store.detailModel?.tag?.list) && store.detailModel!.tag!.list!.length > 0
);

const lifecycleOptions = computed(() => [
  { key: "OPEN", label: t("journal.entry.lifecycle.open"), activeClass: "text-gray-800" },
  { key: "PENDING", label: t("lifecycle.pending"), activeClass: "text-gray-600" },
  { key: "RESOLVED", label: t("status.completed"), activeClass: "text-success" },
]);

const detailLifecycleKey = computed(() => store.detailModel?.lifecycle?.lifecycleKey || "OPEN");
const detailLifecycleLabel = computed(() => {
  const key = detailLifecycleKey.value;
  if (key === "PENDING") return t("lifecycle.pending");
  if (key === "RESOLVED") return t("status.completed");
  return t("journal.entry.lifecycle.open");
});
const detailLifecycleBadgeClass = computed(() => {
  const key = detailLifecycleKey.value;
  if (key === "PENDING") return "badge-light-secondary text-gray-600";
  if (key === "RESOLVED") return "badge-light-success";
  return "badge-light";
});

async function onSetLifecycle(lifecycleKey: string): Promise<void> {
  const id = store.detailModel?.id;
  if (!id) return;
  await store.setLifecycle(id, lifecycleKey);
}

/**
 * 소속 엔트리 기준일 기간. 목록과 동일 포맷.
 * 모달·독립 상세가 이 컴포넌트를 공유하므로 한 곳에서 표시한다.
 */
const membershipPeriodLabel = computed(() => {
  const model = store.detailModel;
  if (!model) return "";
  return formatThreadMembershipPeriod(model, (first, last) =>
    t("journal.thread.list.membership-period").replace("{0}", first).replace("{1}", last),
  );
});

const commentList = computed(() => store.detailModel?.comment?.list ?? []);

const commentCount = computed(() => {
  const cnt = store.detailModel?.comment?.cnt;
  if (typeof cnt === "number") return cnt;
  return commentList.value.length;
});

const threadContentType = computed(
  () => store.detailModel?.contentType ?? "JOURNAL_THREAD"
);

function openCommentRegist(): void {
  const id = store.detailModel?.id;
  if (!id) return;
  void attachableStore.openCommentRegist(id, threadContentType.value);
}

function openCommentList(): void {
  const id = store.detailModel?.id;
  if (!id) return;
  void attachableStore.openCommentList(id, threadContentType.value);
}

/** 빌려온 엔트리 출처 칩 색 — 스레드 ID로 팔레트 고정 */
const THREAD_SYNTH_ACCENTS = [
  "#009ef7",
  "#7239ea",
  "#50cd89",
  "#ffc700",
  "#f1416c",
  "#181c32",
] as const;

/**
 * 연관 스레드 ID에 대응하는 합성 강조색을 반환한다.
 *
 * @param relatedThreadId 연관 스레드 ID
 * @returns CSS 색상 문자열
 */
function accentForRelatedThread(relatedThreadId: number): string {
  const idx = Math.abs(relatedThreadId) % THREAD_SYNTH_ACCENTS.length;
  return THREAD_SYNTH_ACCENTS[idx];
}

/**
 * 연관 스레드 제목을 detailRelatedThreads 목록에서 해석한다.
 * 빌려온 엔트리의 출처 칩 표시에 사용한다.
 *
 * @param sourceThreadId 출처 스레드 ID
 * @returns 스레드 제목. 목록에 없으면 ID 문자열 반환.
 */
function resolveRelatedThreadTitle(sourceThreadId: number): string {
  const rel = store.detailRelatedThreads.find((r) => r.targetId === sourceThreadId);
  return rel?.targetTitle ?? String(sourceThreadId);
}

/**
 * 연관 스레드 추가 모달 열기.
 */
function openRelatedThreadPicker(): void {
  store.openPicker();
}

/**
 * 연관 스레드 행의 뷰 합성 토글.
 *
 * @param relatedThreadId 연관 스레드 ID
 */
function isRelatedThreadIncluded(relatedThreadId: number): boolean {
  return store.detailIncludedRelatedThreadIds.includes(relatedThreadId);
}

/**
 * 연관 스레드 행의 뷰 합성 토글을 전환한다.
 *
 * @param relatedThreadId 연관 스레드 ID
 */
async function onToggleRelatedThreadInclude(relatedThreadId: number): Promise<void> {
  await store.toggleRelatedThreadInclude(relatedThreadId);
}

/**
 * 연관 스레드 제거.
 *
 * @param rel 삭제할 연관 항목
 */
async function onRemoveRelatedThread(rel: { id?: number; targetTitle?: string; targetId?: number }): Promise<void> {
  const baseThreadId = store.detailModel?.id;
  if (!baseThreadId || !rel.id) return;
  await store.removeRelatedThread(baseThreadId, rel.id);
}
</script>

<style scoped>
/* 빌려온(연관) 엔트리: 채워진 출처 칩만으로 base와 구분 */
.thread-synth-entry {
  margin-top: 0.25rem;
}

.thread-synth-source__chip {
  border: 1px solid transparent;
  letter-spacing: 0;
}
</style>
