<template>
  <!--begin::엔트리 행-->
  <div
    :id="domId ?? (entry.id ? 'journal-entry-' + entry.id : undefined)"
    :class="[itemClass, { 'is-collapsed': isCollapsed }, 'd-flex gap-2 py-1']"
    :data-id="entry.id"
    :data-imprtc="hasState('IMPRTC') ? 'Y' : 'N'"
    :data-refrnc="hasState('REFRNC') ? 'Y' : 'N'"
    :data-resolved="isResolved ? 'Y' : 'N'"
    :data-else-dream="isElseDream ? 'Y' : 'N'"
    :data-stdrd-dt="entry.stdrdDt"
    :data-yy="entryCacheYy"
    :data-mnth="entryCacheMnth"
  >
    <!--begin::순번-->
    <div class="d-none d-md-flex flex-column align-items-center pt-1 ps-2" style="width:56px; min-width:56px;">
      <span :class="['fw-bold fs-7', isResolved ? (isDreamEntry ? 'text-dream' : 'text-success') : 'text-muted']">#{{ entry.sortOrder }}</span>
      <span v-if="lcKey === 'PENDING'" class="badge badge-light-warning fs-8 mt-1">{{ t("journal.entry.pending-badge") }}</span>
      <!--begin::클라이언트 임시 접힘/펼침 버튼-->
      <button
        type="button"
        :class="['btn btn-xs px-1 mt-1', { 'is-active': isCollapsed }]"
        :title="isCollapsed ? t('common.expand') : t('common.collapse')"
        @click="toggleEntry"
      >
        <i :class="['bi pe-0 fs-8', isCollapsed ? 'bi-arrows-expand' : 'bi-arrows-collapse']"></i>
      </button>
      <!--end::클라이언트 임시 접힘/펼침 버튼-->
    </div>
    <!--end::순번-->

    <!--begin::본문 영역-->
    <div :class="[contentClass, 'flex-grow-1']">
      <!--begin::꿈 상태 배지 (꿈 엔트리 전용)-->
      <div v-if="isDream" class="d-flex align-items-center gap-1 mb-1 flex-wrap">
        <span v-if="hasState('NHTMR')" class="badge badge-light-danger">!{{ t('state.nightmare') }}</span>
        <span v-if="hasState('HALLUC')" class="badge badge-light-secondary">!{{ t('state.hallucination') }}</span>
      </div>
      <!--end::꿈 상태 배지-->

      <!--begin::엔트리 제목 (유형 무관, title 있을 때만)
        변경 전: 꿈 엔트리에서만 배지 행에 인라인(fs-7)으로 표시 → 일기·노트는 제목이 보이지 않았음
        변경 후: 모든 유형에서 배지 행 아래 독립 행으로 표시. 본문(.journal-content = 1rem) 대비
                 한 단계 위인 fs-5(1.15rem) + fw-bold.
        접힘(isCollapsed) 상태와 무관하게 항상 표시한다 (기존 꿈 제목 동작 유지 — 본문만 숨김).
        .journal-content 밖이라 유형별 본문 색상을 상속하지 않고 기본 텍스트색을 쓴다. -->
      <div v-if="entry.title" class="fw-bold fs-5 mb-1">{{ entry.title }}</div>
      <!--end::엔트리 제목-->

      <!--begin::마크다운 본문-->
      <div
        v-if="!isCollapsed && entry.markdownContent"
        class="journal-content p-2"
        v-html="displayMarkdownContent"
      ></div>
      <div v-else-if="isCollapsed" class="text-muted fs-8 fst-italic ps-2">{{ t("journal.entry.collapsed") }}</div>
      <!--end::마크다운 본문-->

      <!--begin::엔트리 태그-->
      <div v-if="tagList.length > 0" class="d-flex flex-wrap gap-1 mt-1 ps-2">
        <span
          v-for="tag in tagList"
          :key="tag.tagId"
          class="text-muted cursor-pointer pe-1"
          @click.stop="openTagContextMenu($event, tag)"
        >
          #<span class="border-bottom text-primary fw-lighter opacity-hover">
            <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>{{ tag.name }}
          </span>
        </span>
      </div>
      <!--end::엔트리 태그-->

      <!--begin::꿈 태그 프로필-->
      <div v-if="!isCollapsed && dreamTagProfileList.length > 0" class="journal-dream-tag-profiles">
        <div
          v-for="tag in dreamTagProfileList"
          :key="'profile-' + tag.tagId"
          class="journal-dream-tag-profile"
        >
          <span class="journal-dream-tag-profile__tag">#{{ tag.name }}</span>
          <span class="journal-dream-tag-profile__divider"></span>
          <span class="journal-dream-tag-profile__content">{{ tag.profileContent }}</span>
        </div>
      </div>
      <!--end::꿈 태그 프로필-->

      <!--begin::관련글-->
      <div v-if="relatedList.length > 0" class="d-flex flex-column gap-1 mt-2 ps-2">
        <div
          v-for="rel in relatedList"
          :key="rel.id"
          class="d-flex align-items-center flex-wrap gap-2 p-2 bg-light rounded fs-8 text-muted"
        >
          <i class="bi bi-link-45deg"></i>
          <span v-if="rel.relationType" class="badge badge-light-primary">{{ relationTypeLabel(rel.relationType) }}</span>
          <span v-if="rel.targetContentType" class="badge badge-light-secondary">{{ relatedContentTypeLabel(rel.targetContentType) }}</span>
          <button
            v-if="rel.targetId"
            type="button"
            class="btn btn-link p-0 fs-8 text-primary text-start"
            :title="t('related-content.open.tooltip')"
            @click.stop="openRelatedTarget(rel.targetId)"
          >
            {{ rel.targetTitle || '#' + rel.targetId }}
          </button>
          <span v-else>{{ rel.targetTitle }}</span>
          <span v-if="rel.reason" class="fst-italic">({{ rel.reason }})</span>
          <button
            v-if="axisWritable && rel.id"
            type="button"
            class="btn btn-xs btn-icon btn-light-danger ms-auto"
            :title="t('related-content.unlink.tooltip')"
            @click.stop="unlinkRelated(rel)"
          >
            <i class="bi bi-x-lg fs-9"></i>
          </button>
        </div>

      </div>
      <!--end::관련글-->

      <!--begin::소속 스레드-->
      <div v-if="entryThreadList.length > 0" class="d-flex flex-wrap align-items-center gap-1 mt-2 ps-2">
        <i class="bi bi-diagram-3 fs-8 text-muted"></i>
        <button
          v-for="th in entryThreadList"
          :key="'thread-' + th.id"
          type="button"
          class="badge badge-light-primary border-0 fs-8 cursor-pointer"
          :title="t('journal.entry.thread.open.tooltip')"
          @click.stop="openThreadDetail(th.threadId)"
        >
          {{ th.threadTitle || ('#' + th.threadId) }}
        </button>
      </div>
      <!--end::소속 스레드-->

      <!--begin::댓글-->
      <div v-if="commentList.length > 0" class="d-flex flex-column gap-1 mt-2 ps-2">
        <div
          v-for="cmt in commentList"
          :key="cmt.id"
          class="fs-8 text-muted ps-2 border-start border-2 border-gray-300"
        >
          {{ cmt.content }}
        </div>
      </div>
      <!--end::댓글-->
    </div>
    <!--end::본문 영역-->

    <!--begin::우측 액션 영역-->
    <div v-if="entry.id" class="journal-entry-actions d-flex flex-row align-items-start pt-1 gap-1">
      <!--begin::댓글 등록 버튼-->
      <button
        v-if="axisWritable"
        type="button"
        class="btn btn-xs btn-icon journal-entry-action-btn"
        :title="t('comment.register')"
        @click="openCommentRegist"
      >
        <i class="bi bi-chat-dots fs-8"></i>
      </button>
      <!--end::댓글 등록 버튼-->

      <!--begin::복사 버튼-->
      <button
        type="button"
        class="btn btn-xs btn-icon journal-entry-action-btn"
        :title="t('common.copy')"
        @click="copyEntry"
      >
        <i class="bi bi-copy fs-8"></i>
      </button>
      <!--end::복사 버튼-->

      <!--begin::컨텍스트 메뉴-->
      <div class="me-0">
        <button
          type="button"
          class="btn btn-xs btn-icon journal-entry-action-btn"
          data-kt-menu-trigger="click"
          data-kt-menu-placement="bottom-end"
          :title="t('common.menu')"
        >
          <i class="ki-solid ki-dots-horizontal fs-6"></i>
        </button>
        <div
          class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold w-200px py-3"
          data-kt-menu="true"
        >
          <!--begin::메뉴 헤더-->
          <div class="menu-item px-3">
            <div class="menu-content text-muted pb-2 px-3 fs-7 text-uppercase">{{ contentLabel }}</div>
          </div>
          <!--end::메뉴 헤더-->

          <!--begin::수정-->
          <div v-if="axisWritable" class="menu-item px-3 my-1 cursor-pointer">
            <div class="menu-link flex-stack px-3" @click="openModify">
              {{ t('common.edit') }}
              <i class="bi bi-pencil-square fs-8"></i>
            </div>
          </div>
          <!--end::수정-->

          <!--begin::해석 등록-->
          <div v-if="axisWritable" class="menu-item px-3 my-1 cursor-pointer">
            <div class="menu-link flex-stack px-3" @click="openInterpretationRegist">
              {{ t('journal.entry.interpretation.register') }}
              <i class="bi bi-lightbulb fs-8"></i>
            </div>
          </div>
          <!--end::해석 등록-->

          <!--begin::이력 (historyTriggeredAt 없으면 disabled)-->
          <div class="menu-item px-3 my-1 cursor-pointer">
            <div
              :class="['menu-link flex-stack px-3', { 'disabled text-muted': !hasHistory }]"
              @click="hasHistory ? openHistory() : undefined"
            >
              {{ t('journal.entry.history') }}
              <i class="bi bi-clock-history fs-8"></i>
            </div>
          </div>
          <!--end::이력-->

          <!--begin::관련 글 추가 (다른 사람 꿈 제외)-->
          <div v-if="axisWritable && !hasDreamerName(entry)" class="menu-item px-3 my-1 cursor-pointer">
            <div class="menu-link flex-stack px-3" @click="openRelated">
              {{ t('journal.entry.related-content.add') }}
              <i class="bi bi-link-45deg fs-8"></i>
            </div>
          </div>
          <!--end::관련 글 추가-->

          <!--begin::스레드에 추가 서브메뉴 (다른 사람 꿈 제외)-->
          <div
            v-if="axisWritable && !hasDreamerName(entry)"
            class="menu-item px-3"
            data-kt-menu-trigger="hover"
            data-kt-menu-placement="right-end"
            @mouseenter="ensureThreadOptions"
          >
            <a href="#" class="menu-link px-3" @click.prevent>
              <span class="menu-title">{{ t('journal.entry.thread.add') }}</span>
              <span class="menu-arrow"></span>
            </a>
            <div class="menu-sub menu-sub-dropdown py-3" style="width: 280px;">
              <!--begin::새 스레드로 시작-->
              <div class="menu-item px-3 my-1 cursor-pointer">
                <div class="menu-link flex-stack px-3 text-primary" @click="startNewThread">
                  {{ t('journal.entry.thread.new') }}
                  <i class="bi bi-plus-lg fs-8"></i>
                </div>
              </div>
              <!--end::새 스레드로 시작-->

              <div class="separator my-2"></div>

              <!--begin::스레드 후보 검색·분류-->
              <div
                class="menu-item px-3"
                data-kt-menu-dismiss="false"
                @click.stop
                @keydown.stop
              >
                <div class="menu-content px-3 py-1 w-100">
                  <input
                    v-model="membershipStore.optionKeyword"
                    type="search"
                    class="form-control form-control-sm"
                    :placeholder="t('journal.thread.filter.keyword.placeholder')"
                    data-kt-menu-dismiss="false"
                    @input="scheduleThreadCandidateSearch"
                  />
                  <select
                    v-model="membershipStore.optionCategory"
                    class="form-select form-select-sm mt-2"
                    :disabled="membershipStore.categoriesLoading"
                    data-kt-menu-dismiss="false"
                    @change="refreshThreadCandidates"
                  >
                    <option value="">{{ t("journal.thread.filter.all-categories") }}</option>
                    <option
                      v-for="category in membershipStore.categoryOptions"
                      :key="'thread-category-' + category.code"
                      :value="category.code"
                    >
                      {{ category.codeName }}
                    </option>
                  </select>
                  <div v-if="membershipStore.categoryError" class="text-danger fs-9 mt-1">
                    {{ membershipStore.categoryError }}
                  </div>
                </div>
              </div>
              <!--end::스레드 후보 검색·분류-->

              <div class="separator my-2"></div>

              <!--begin::스레드 후보 목록-->
              <div v-if="membershipStore.optionsLoading" class="menu-item px-3">
                <span class="menu-link px-3 text-muted fs-8">{{ t('common.loading') }}</span>
              </div>
              <div v-if="membershipStore.optionsError" class="menu-item px-3">
                <span class="menu-content px-3 text-danger fs-8">{{ membershipStore.optionsError }}</span>
              </div>
              <div
                v-if="!membershipStore.optionsLoading
                  && !membershipStore.optionsError
                  && membershipStore.threadOptions.length === 0"
                class="menu-item px-3"
              >
                <span class="menu-content px-3 text-muted fs-8">
                  {{ hasThreadCandidateFilter
                    ? t('journal.entry.thread.search.empty')
                    : t('journal.entry.thread.empty') }}
                </span>
              </div>
              <template v-if="membershipStore.threadOptions.length > 0">
                <div
                  v-for="opt in membershipStore.threadOptions"
                  :key="'thread-opt-' + opt.id"
                  class="menu-item px-3 my-1 cursor-pointer"
                >
                  <div class="menu-link flex-stack px-3" @click="toggleThread(opt)">
                    <span class="min-w-0">
                      <span class="d-block text-truncate">
                        {{ opt.title || t('journal.entry.thread.untitled') }}
                      </span>
                      <span v-if="opt.categoryCode" class="d-block text-muted fs-9">
                        {{ threadCategoryName(opt.categoryCode) }}
                      </span>
                    </span>
                    <i v-if="opt.member" class="bi bi-check-lg fs-8 text-success"></i>
                  </div>
                </div>
              </template>
              <!--end::스레드 후보 목록-->
            </div>
          </div>
          <!--end::스레드에 추가 서브메뉴-->

          <div v-if="axisWritable" class="separator my-2"></div>

          <!--begin::라이프사이클 서브메뉴-->
          <div v-if="axisWritable" class="menu-item px-3" data-kt-menu-trigger="hover" data-kt-menu-placement="right-end">
            <a href="#" class="menu-link px-3" @click.prevent>
              <span class="menu-title">{{ t('common.lifecycle') }}</span>
              <span class="menu-arrow"></span>
            </a>
            <div class="menu-sub menu-sub-dropdown w-175px py-4">
              <div v-for="lc in lifecycleOptions" :key="'lc-' + lc.key" class="menu-item px-3">
                <div class="menu-content px-3">
                  <label class="form-check form-check-custom form-check-solid cursor-pointer">
                    <input
                      class="form-check-input w-18px h-18px cursor-pointer"
                      type="radio"
                      :name="'entry-lc-' + entry.id"
                      :value="lc.key"
                      :checked="lcKey === lc.key"
                      @click="setLifecycle(lc.key)"
                    />
                    <span class="form-check-label fs-7" :class="lcKey === lc.key ? lc.activeClass : 'text-muted'">{{ lc.label }}</span>
                  </label>
                </div>
              </div>
            </div>
          </div>
          <!--end::라이프사이클 서브메뉴-->

          <!--begin::상태 서브메뉴-->
          <div class="menu-item px-3" data-kt-menu-trigger="hover" data-kt-menu-placement="right-end">
            <a href="#" class="menu-link px-3" @click.prevent>
              <span class="menu-title">{{ t('common.status') }}</span>
              <span class="menu-arrow"></span>
            </a>
            <div class="menu-sub menu-sub-dropdown w-175px py-4">
              <!--begin::중요/참조 토글-->
              <template v-if="axisWritable">
              <div v-for="st in statusOptions" :key="'st-' + st.key" class="menu-item px-3">
                <div class="menu-content px-3">
                  <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
                    <input
                      class="form-check-input w-30px h-20px cursor-pointer"
                      type="checkbox"
                      :checked="hasState(st.key)"
                      @click="toggleState(st.key)"
                    />
                    <span class="form-check-label fs-7" :class="hasState(st.key) ? st.activeClass : 'text-muted'">{{ st.label }}</span>
                  </label>
                </div>
              </div>
              <!--end::중요/참조 토글-->

              <!--begin::악몽/환각 토글 (꿈 전용)-->
              <template v-if="isDream">
                <div v-for="st in dreamStatusOptions" :key="'dst-' + st.key" class="menu-item px-3">
                  <div class="menu-content px-3">
                    <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
                      <input
                        class="form-check-input w-30px h-20px cursor-pointer"
                        type="checkbox"
                        :checked="hasState(st.key)"
                        @click="toggleState(st.key)"
                      />
                      <span class="form-check-label fs-7" :class="hasState(st.key) ? st.activeClass : 'text-muted'">{{ st.label }}</span>
                    </label>
                  </div>
                </div>
              </template>
              <!--end::악몽/환각 토글-->
              </template>

              <!--begin::접기 토글-->
              <div class="menu-item px-3">
                <div class="menu-content px-3">
                  <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
                    <input
                      class="form-check-input w-30px h-20px cursor-pointer"
                      type="checkbox"
                      :checked="hasState('COLLAPSED')"
                      @click="toggleState('COLLAPSED')"
                    />
                    <span class="form-check-label fs-7" :class="hasState('COLLAPSED') ? 'text-gray-700' : 'text-muted'">{{ t('common.collapse') }}</span>
                  </label>
                </div>
              </div>
              <!--end::접기 토글-->
            </div>
          </div>
          <!--end::상태 서브메뉴-->

          <div v-if="axisWritable" class="separator my-2"></div>

          <!--begin::삭제-->
          <div v-if="axisWritable" class="menu-item px-3 my-1 cursor-pointer">
            <div class="menu-link flex-stack px-3 text-danger" @click="deleteEntry">
              {{ t('common.delete') }}
              <i class="bi bi-trash text-danger p-0 fs-8"></i>
            </div>
          </div>
          <!--end::삭제-->
        </div>
      </div>
      <!--end::컨텍스트 메뉴-->
    </div>
    <!--end::우측 액션 영역-->
  </div>
  <!--end::엔트리 행-->

  <!--begin::해석 목록-->
  <JournalInterpretationItem
    v-for="interp in interpretationList"
    :key="interp.id"
    :interpretation="interp"
    :is-dream="isDreamEntry"
  />
  <!--end::해석 목록-->
</template>

<script setup lang="ts">
import { swalConfirm, swalAlert, swalRequestError, swalFire, swalAjaxResult } from "@/shared/utils/swal";
import { ref, computed, nextTick, onBeforeUnmount, provide } from "vue";
import { useRoute } from "vue-router";
import axios from "axios";
import { useJournalModalStore } from "@/features/journal/stores/journalModal";
import { useAttachableModalStore } from "@/features/attachable/stores/attachableModal";
import {
  useJournalThreadMembershipStore,
  type ThreadOption,
} from "@/features/journal/stores/journalThreadMembership";
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
import { useTagContextMenuStore } from "@/features/journal/stores/tagContextMenu";
import { useJournalStore } from "@/features/journal/stores/journal";
import { refreshJournalEntryHostForRoute } from "@/features/journal/utils/journalEntryHostRefresh";
import type { JournalEntryDto, RelatedContentItem } from "@/features/journal/stores/journal";
import { getWeekDayStr, getWeekStartDateStr } from "@/features/journal/utils/journalDate";
import { hasDreamerName } from "@/features/journal/utils/journalDream";
import { htmlToPlainText } from "@/features/journal/utils/htmlToPlainText";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import JournalInterpretationItem from "../../interpretation/components/JournalInterpretationItem.vue";
import {
  useJournalDayResolved,
  mergeDayResolvedAxis,
  JOURNAL_DAY_RESOLVED_KEY,
} from "@/features/journal/utils/journalDayResolved";

const props = defineProps<{
  entry: JournalEntryDto;
  isDream?: boolean;
  /** 챕터 토글이 전파하는 강제 접힘 여부. null=챕터 미개입, true/false=챕터 강제 */
  forceCollapsed?: boolean | null;
  /** Parent-provided DOM id, used by popup/search contexts that render the same entry component. */
  domId?: string;
  /** Search-only keyword highlights. Empty by default so monthly/weekly/chapter renders stay unchanged. */
  highlightKeywords?: string[];
}>();

const modalStore = useJournalModalStore();
const attachableStore = useAttachableModalStore();
const tagContextMenuStore = useTagContextMenuStore();
const journalStore = useJournalStore();
const threadStore = useJournalThreadStore();
const route = useRoute();
const membershipStore = useJournalThreadMembershipStore();
const { t } = useLocaleStore();

interface JournalCacheContext {
  yy?: number;
  mnth?: number;
  weekStartDt?: string;
}

/** 현재 엔트리가 꿈 유형인지 여부. 꿈 RESOLVED 전용 보라색 표시 계약에 사용한다. */
const isDreamEntry = computed(() => props.isDream || props.entry.contentType === "JOURNAL_DREAM");

const parentDayResolvedAxis = useJournalDayResolved();
const mergedDayResolvedAxis = computed(() =>
  mergeDayResolvedAxis(parentDayResolvedAxis.value, props.entry),
);
provide(JOURNAL_DAY_RESOLVED_KEY, mergedDayResolvedAxis);
const axisWritable = computed(() =>
  isDreamEntry.value ? mergedDayResolvedAxis.value.dreamWritable : mergedDayResolvedAxis.value.diaryWritable,
);

function guardAxisWrite(): boolean {
  if (axisWritable.value) return true;
  void swalAlert(
    t(isDreamEntry.value ? "journal.day.dream-resolved-locked" : "journal.day.diary-resolved-locked"),
  );
  return false;
}

/** 엔트리 타입별 외부 item 클래스 (journal.scss 의 data-* 셀렉터 연동) */
const itemClass = computed(() => {
  if (props.isDream || props.entry.contentType === 'JOURNAL_DREAM') return 'journal-dream-item';
  if (props.entry.contentType === 'JOURNAL_NOTE') return 'journal-note-item';
  return 'journal-diary-item';
});

/** 엔트리 타입별 내부 content 클래스 (텍스트 색상·left-border 스타일 연동) */
const contentClass = computed(() => {
  if (props.isDream || props.entry.contentType === 'JOURNAL_DREAM') return 'journal-dream-content';
  if (props.entry.contentType === 'JOURNAL_NOTE') return 'journal-note-content';
  return 'journal-diary-content';
});

/** 메뉴 헤더에 표시할 컨텐츠 유형 레이블 */
const contentLabel = computed(() => {
  if (props.isDream || props.entry.contentType === 'JOURNAL_DREAM') return t('common.dream');
  return t('common.diary');
});

const lcKey = computed(() => props.entry.lifecycle?.lifecycleKey ?? "");
const isResolved = computed(() => lcKey.value === "RESOLVED");
/** 지정 꿈꾼(타인 꿈) — journal.scss 좌측 회색 이중선·RESOLVED 색상과 별도 */
const isElseDream = computed(() => {
  if (!(props.isDream || props.entry.contentType === "JOURNAL_DREAM")) return false;
  return hasDreamerName(props.entry);
});
const hasHistory = computed(() => !!props.entry.history?.historyTriggeredAt);

/** 클라이언트 임시 접힘 오버라이드. null=서버 상태 따름, true=강제 접힘, false=강제 펼침 */
const localCollapsedOverride = ref<boolean | null>(null);

/** 서버 상태(COLLAPSED) + 클라이언트 임시 오버라이드를 합산한 최종 접힘 여부. RESOLVED 시 자동 접힘.
 * 우선순위: 엔트리 자체 토글 > 챕터 강제(forceCollapsed) > RESOLVED 자동 접힘 > 서버 COLLAPSED */
const isCollapsed = computed(() => {
  if (localCollapsedOverride.value !== null) return localCollapsedOverride.value;
  if (props.forceCollapsed !== null && props.forceCollapsed !== undefined) return props.forceCollapsed;
  if (isResolved.value) return true;
  return hasState("COLLAPSED");
});

function hasState(key: string): boolean {
  return (props.entry.state?.list ?? []).some((s) => s.stateKey === key);
}

const tagList = computed(() => props.entry.tag?.list ?? []);
const displayMarkdownContent = computed(() => highlightKeywordsInHtml(props.entry.markdownContent ?? "", props.highlightKeywords ?? []));
/** 변경 전: 태그 프로필은 설정 모달에서만 보였음. 변경 후: 꿈 엔트리에서만 본문 아래에 프로필을 표시. */
const dreamTagProfileList = computed(() => {
  if (!(props.isDream || props.entry.contentType === "JOURNAL_DREAM")) return [];
  return tagList.value.filter((tag) => typeof tag.profileContent === "string" && tag.profileContent.trim() !== "");
});
/** 연결 해제 성공 직후 재조회 전에도 현재 행에서 제거된 관계를 숨긴다. */
const unlinkedRelatedIds = ref<Set<number>>(new Set());
const relatedList = computed(() => (props.entry.relatedContentList ?? []).filter(
  (related) => related.id == null || !unlinkedRelatedIds.value.has(related.id)
));
const commentList = computed(() => props.entry.comment?.list ?? []);
const interpretationList = computed(() => props.entry.journalInterpretationList ?? []);

function highlightKeywordsInHtml(html: string, keywords: string[]): string {
  const uniqueKeywords = Array.from(new Set(keywords.map((keyword) => keyword.trim()).filter(Boolean)));
  if (!html || uniqueKeywords.length === 0 || typeof document === "undefined") return html;

  const template = document.createElement("template");
  template.innerHTML = html;

  const textNodes: Text[] = [];
  const skippedTags = new Set(["MARK", "SCRIPT", "STYLE", "TEXTAREA"]);
  const lowerKeywords = uniqueKeywords.map((keyword) => keyword.toLowerCase());
  const walker = document.createTreeWalker(
    template.content,
    NodeFilter.SHOW_TEXT,
    {
      acceptNode(node) {
        const parent = node.parentElement;
        const value = node.nodeValue ?? "";
        if (!parent || skippedTags.has(parent.tagName)) return NodeFilter.FILTER_REJECT;
        return lowerKeywords.some((keyword) => value.toLowerCase().includes(keyword))
          ? NodeFilter.FILTER_ACCEPT
          : NodeFilter.FILTER_REJECT;
      },
    }
  );

  while (walker.nextNode()) {
    textNodes.push(walker.currentNode as Text);
  }

  textNodes.forEach((node) => {
    const text = node.nodeValue ?? "";
    const lowerText = text.toLowerCase();
    const fragment = document.createDocumentFragment();
    let cursor = 0;

    while (cursor < text.length) {
      const nextMatch = findNextKeywordMatch(lowerText, lowerKeywords, cursor);
      if (!nextMatch) {
        fragment.appendChild(document.createTextNode(text.slice(cursor)));
        break;
      }
      if (nextMatch.index > cursor) {
        fragment.appendChild(document.createTextNode(text.slice(cursor, nextMatch.index)));
      }

      const mark = document.createElement("mark");
      mark.className = "journal-entry-search-keyword-mark";
      mark.textContent = text.slice(nextMatch.index, nextMatch.index + nextMatch.length);
      fragment.appendChild(mark);
      cursor = nextMatch.index + nextMatch.length;
    }

    node.parentNode?.replaceChild(fragment, node);
  });

  return template.innerHTML;
}

function findNextKeywordMatch(
  lowerText: string,
  lowerKeywords: string[],
  cursor: number,
): { index: number; length: number } | null {
  let best: { index: number; length: number } | null = null;
  lowerKeywords.forEach((keyword) => {
    const index = lowerText.indexOf(keyword, cursor);
    if (index === -1) return;
    if (!best || index < best.index || (index === best.index && keyword.length > best.length)) {
      best = { index, length: keyword.length };
    }
  });
  return best;
}

function parseCacheNumber(value: unknown): number | undefined {
  const parsed = typeof value === "number" ? value : Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : undefined;
}

function resolveEntryYy(): number | undefined {
  const stdrdDt = props.entry.stdrdDt?.trim();
  if (stdrdDt && stdrdDt.length >= 4) return parseCacheNumber(stdrdDt.slice(0, 4));
  return parseCacheNumber(journalStore.yy);
}

function resolveEntryMnth(): number | undefined {
  const stdrdDt = props.entry.stdrdDt?.trim();
  if (stdrdDt && stdrdDt.length >= 7) return parseCacheNumber(stdrdDt.slice(5, 7));
  return parseCacheNumber(journalStore.mnth);
}

function resolveEntryWeekStartDt(): string | undefined {
  const weekStartDt = journalStore.weekStartDt?.trim();
  if (weekStartDt) return weekStartDt;
  const stdrdDt = props.entry.stdrdDt?.trim();
  return stdrdDt ? getWeekStartDateStr(stdrdDt) : undefined;
}

const entryCacheYy = computed(() => resolveEntryYy());
const entryCacheMnth = computed(() => resolveEntryMnth());

function resolveJournalCacheContext(): JournalCacheContext {
  const cacheContext: JournalCacheContext = {};
  const yy = entryCacheYy.value;
  const mnth = entryCacheMnth.value;
  const weekStartDt = resolveEntryWeekStartDt();
  if (yy != null) cacheContext.yy = yy;
  if (mnth != null) cacheContext.mnth = mnth;
  if (weekStartDt) cacheContext.weekStartDt = weekStartDt;
  if (Object.keys(cacheContext).length === 0) {
    console.warn("[journal] missing cache context for entry state update", {
      id: props.entry.id,
      contentType: props.entry.contentType,
      stdrdDt: props.entry.stdrdDt,
    });
  }
  return cacheContext;
}

/** 라이프사이클 옵션 (OPEN/PENDING/RESOLVED) */
const lifecycleOptions = computed(() => [
  { key: "OPEN", label: t("journal.entry.lifecycle.open"), activeClass: "text-gray-800" },
  { key: "PENDING", label: t("lifecycle.pending"), activeClass: "text-primary" },
  { key: "RESOLVED", label: t("status.completed"), activeClass: isDreamEntry.value ? "text-dream" : "text-success" },
]);

/** 상태 옵션 (중요/참조) */
const statusOptions = computed(() => [
  { key: "IMPRTC", label: t("state.important"), activeClass: "text-danger" },
  { key: "REFRNC", label: t("state.reference"), activeClass: "text-warning" },
]);

/** 꿈 전용 상태 옵션 (악몽/환각) */
const dreamStatusOptions = computed(() => [
  { key: "NHTMR", label: t("state.nightmare"), activeClass: "text-info" },
  { key: "HALLUC", label: t("state.hallucination"), activeClass: "text-gray-700" },
]);

/** 태그 클릭 컨텍스트 메뉴 열기 */
function openTagContextMenu(event: MouseEvent, tag: { tagId: number | string; name: string; ctgr?: string }): void {
  tagContextMenuStore.open(event, {
    tagId: tag.tagId,
    name: tag.name,
    ctgr: tag.ctgr ?? "",
    contentType: props.entry.contentType ?? "",
  });
}

/** 클라이언트 전용 임시 접힘/펼침 토글 (서버 상태 무변경) */
function toggleEntry(): void {
  localCollapsedOverride.value = !isCollapsed.value;
}
/** HTML 마크업을 제거하고 평문으로 변환한다 (복사 시 사용). */
/** 엔트리 내용을 클립보드에 복사한다. 레거시 copy() 와 동일 형식: 날짜(요일)\n마크다운 원문 */
async function copyEntry(): Promise<void> {
  const weekDay = getWeekDayStr(props.entry.stdrdDt, t);
  const dateLine = weekDay
    ? `${props.entry.stdrdDt} (${weekDay})`
    : (props.entry.stdrdDt ?? "");
  /* content = TinyMCE HTML 원문(마크다운 재처리 이전); markdownContent = MarkdownUtils 처리 후 HTML */
  const raw = htmlToPlainText(props.entry.content ?? props.entry.markdownContent ?? "");
  const text = [dateLine, raw].filter(Boolean).join("\n");
  try {
    await navigator.clipboard.writeText(text);
    void swalFire({ icon: "success", text: t("common.copy.success") });
  } catch (error: unknown) {
    console.error("[journal-entry] clipboard copy failed", error);
    void swalFire({ icon: "error", text: t("common.copy.failure") });
  }
}

/** 엔트리 수정 모달 열기 */
function openModify() {
  if (!guardAxisWrite()) return;
  if (!props.entry.id) return;
  void modalStore.openEntryModify(props.entry.id);
}

/** 댓글 등록 모달 열기 */
function openCommentRegist() {
  if (!guardAxisWrite()) return;
  if (!props.entry.id || !props.entry.contentType) return;
  attachableStore.openCommentRegist(props.entry.id, props.entry.contentType);
}

/** 이력 모달 열기 */
function openHistory() {
  if (!props.entry.id || !props.entry.contentType) return;
  void attachableStore.openHistory(props.entry.contentType, props.entry.id, {
    writeLocked: !axisWritable.value,
  });
}

/** 관련 글 추가 모달 열기 */
function openRelated() {
  if (!guardAxisWrite()) return;
  if (!props.entry.id || !props.entry.contentType) return;
  attachableStore.openRelated(props.entry.contentType, props.entry.id);
}

/** 이 엔트리가 속한 스레드 목록. */
const entryThreadList = computed(() => props.entry.threadList ?? []);

/** 검색·분류가 적용 중인지 여부. 정상 빈 목록의 안내 문구를 구분한다. */
const hasThreadCandidateFilter = computed(() =>
  membershipStore.optionKeyword.trim() !== "" || membershipStore.optionCategory !== "",
);

/** 제목 입력마다 API를 호출하지 않도록 마지막 입력 뒤 250ms에 조회한다. */
let threadCandidateSearchTimer: ReturnType<typeof setTimeout> | undefined;

/** 서브메뉴 진입 시 현재 엔트리 기준 후보와 분류를 조회한다. */
function ensureThreadOptions(): void {
  if (!props.entry.id) return;
  void membershipStore.openThreadOptions(props.entry.id);
}

/** 제목 검색을 debounce하여 현재 엔트리 후보를 갱신한다. */
function scheduleThreadCandidateSearch(): void {
  if (threadCandidateSearchTimer) clearTimeout(threadCandidateSearchTimer);
  const entryId = props.entry.id;
  if (!entryId) return;
  threadCandidateSearchTimer = setTimeout(() => {
    if (membershipStore.candidateEntryId === entryId) {
      void membershipStore.fetchThreadOptions(entryId);
    }
  }, 250);
}

/** 분류 변경 시 현재 엔트리 후보를 즉시 갱신한다. */
function refreshThreadCandidates(): void {
  if (threadCandidateSearchTimer) clearTimeout(threadCandidateSearchTimer);
  const entryId = props.entry.id;
  if (!entryId || membershipStore.candidateEntryId !== entryId) return;
  void membershipStore.fetchThreadOptions(entryId);
}

/** 후보 분류 코드를 현재 locale의 표시명으로 변환한다. */
function threadCategoryName(categoryCode: string): string {
  return membershipStore.categoryOptions.find((category) => category.code === categoryCode)?.codeName
    ?? categoryCode;
}

/** 스레드 소속 토글: 속해 있으면 제외, 아니면 추가. 성공 시 목록 갱신. */
async function toggleThread(option: ThreadOption): Promise<void> {
  if (!guardAxisWrite()) return;
  if (!props.entry.id) return;
  const entryId = props.entry.id;
  const ok = option.member
    ? await membershipStore.removeFromThread(option.id, entryId)
    : await membershipStore.addToThread(option.id, entryId);
  if (ok) {
    if (membershipStore.candidateEntryId === entryId) {
      await membershipStore.fetchThreadOptions(entryId);
    }
    scrollAfterFetch();
  }
}

/** 제목만 받아 새 스레드를 만들고 이 엔트리를 소속시킨다. */
async function startNewThread(): Promise<void> {
  if (!guardAxisWrite()) return;
  if (!props.entry.id) return;
  const result = await swalFire({
    input: "text",
    inputLabel: t("journal.entry.thread.new.prompt"),
    inputPlaceholder: t("journal.entry.thread.new.placeholder"),
    showCancelButton: true,
    confirmButtonText: t("common.save"),
    cancelButtonText: t("common.cancel"),
    inputValidator: (value: string) =>
      value && value.trim() ? null : t("journal.entry.thread.new.required"),
  });
  const title = typeof result.value === "string" ? result.value.trim() : "";
  if (!title) return;
  const entryId = props.entry.id;
  const ok = await membershipStore.createThreadAndAdd(title, entryId);
  if (ok) {
    if (membershipStore.candidateEntryId === entryId) {
      await membershipStore.fetchThreadOptions(entryId);
    }
    scrollAfterFetch();
  }
}

onBeforeUnmount(() => {
  if (threadCandidateSearchTimer) clearTimeout(threadCandidateSearchTimer);
});

/**
 * 현재 저널 화면을 유지한 채 전역 스레드 상세 모달을 연다.
 * 스레드 목록의 route 기반 상세 진입은 딥링크 계약으로 별도 유지한다.
 */
function openThreadDetail(threadId: number): void {
  void threadStore.openDetail(threadId);
}


/** 관련 엔트리 원문 열기 */
function openRelatedTarget(targetId: number): void {
  void modalStore.openEntryView(targetId);
}

/** 관련 콘텐츠 유형을 현재 locale 레이블로 변환한다. */
function relatedContentTypeLabel(contentType: string): string {
  if (contentType === "JOURNAL_DIARY") return t("related-content.content-type.diary");
  if (contentType === "JOURNAL_DREAM") return t("related-content.content-type.dream");
  return contentType;
}

/** 관계 유형을 현재 locale 레이블로 변환한다. */
function relationTypeLabel(relationType: string): string {
  const normalized = relationType.toLowerCase();
  if (["reference", "extension", "parallel", "cause"].includes(normalized)) {
    return t(`enum.relation-type.${normalized}`);
  }
  return relationType;
}

/** 관련 글 연결 해제. FLOW 축은 스레드 소속으로 수렴·제거됐으므로 목록에는 일반 관련글만 남는다. */
async function unlinkRelated(related: RelatedContentItem): Promise<void> {
  if (!guardAxisWrite()) return;
  if (!related.id) {
    console.warn("[journal-entry] related content id missing for unlink", {
      entryId: props.entry.id,
      relationType: related.relationType,
      targetId: related.targetId,
    });
    return;
  }
  if (!await swalConfirm(t("related-content.unlink.confirm"))) return;
  try {
    const res = await axios.delete(`/api/related/${related.id}`);
    if (res.data?.rslt) {
      unlinkedRelatedIds.value = new Set([...unlinkedRelatedIds.value, related.id]);
      await swalAjaxResult({
        rslt: true,
        message: res.data?.message,
        successFallback: t("related-content.unlink.success"),
      });
      scrollAfterFetch();
    } else {
      console.warn("[journal-entry] related content unlink rejected", {
        entryId: props.entry.id,
        relatedContentId: related.id,
        relationType: related.relationType,
        message: res.data?.message,
      });
      void swalAjaxResult({
        rslt: false,
        message: res.data?.message,
        failureFallback: t("related-content.unlink.failure"),
      });
    }
  } catch (e: unknown) {
    console.error("[journal-entry] related content unlink failed", {
      entryId: props.entry.id,
      relatedContentId: related.id,
      relationType: related.relationType,
    }, e);
    void swalRequestError(e, t("related-content.unlink.failure"));
  }
}

/** 해석 등록 모달 열기 */
function openInterpretationRegist() {
  if (!guardAxisWrite()) return;
  if (!props.entry.id || !props.entry.contentType) return;
  modalStore.openInterpretationRegist({
    refId: props.entry.id,
    refContentType: props.entry.contentType,
    stdrdDt: props.entry.stdrdDt,
  });
}

/**
 * 액션 성공 후 현재 표시 호스트를 재조회하고, 일자 목록을 갱신한 경우에만 해당 일자로 스크롤한다.
 * 변경 전에는 모든 라우트에서 fetchDays 완료 후 일자 DOM을 찾았으나, 스레드 상세에서는
 * 열린 스레드의 원본 엔트리·집계 태그를 다시 조회하고 모달 내부 읽기 위치를 유지한다.
 */
function scrollAfterFetch(stdrdDt = props.entry.stdrdDt): void {
  const dt = stdrdDt;
  void refreshJournalEntryHostForRoute(journalStore, threadStore, route, dt).then((scope) => {
    /* 검색·스레드 상세는 배경 일자 스크롤 대상이 아니다. */
    if (scope === "thread-detail" || scope === "journal-entry-search" || !dt) return;
    void nextTick(() => {
      const el = document.getElementById(`journal-day-${dt}`);
      if (el) el.scrollIntoView({ behavior: "smooth", block: "start" });
    });
  });
}

/** 라이프사이클 설정 (PUT /api/lifecycles) */
async function setLifecycle(lifecycleKey: string): Promise<void> {
  if (!guardAxisWrite()) return;
  if (!props.entry.id || !props.entry.contentType) return;
  try {
    const res = await axios.put("/api/lifecycles", {
      id: props.entry.id,
      contentType: props.entry.contentType,
      lifecycleKey,
      cacheContext: resolveJournalCacheContext(),
    });
    if (res.data?.rslt) {
      scrollAfterFetch();
    } else {
      void swalFire({ icon: "error", text: res.data?.message ?? t("common.result.failure") });
    }
  } catch (e: unknown) {
    void swalRequestError(e);
  }
}

/** 상태 토글 (POST /api/states) */
async function toggleState(stateKey: string): Promise<void> {
  if (stateKey !== "COLLAPSED" && !guardAxisWrite()) return;
  if (!props.entry.id || !props.entry.contentType) return;
  try {
    const res = await axios.post("/api/states", {
      id: props.entry.id,
      contentType: props.entry.contentType,
      stateKey,
      cacheContext: resolveJournalCacheContext(),
    });
    if (res.data?.rslt) {
      scrollAfterFetch();
    } else {
      void swalFire({ icon: "error", text: res.data?.message ?? t("common.result.failure") });
    }
  } catch (e: unknown) {
    void swalRequestError(e);
  }
}

/** 엔트리 삭제 (DELETE /api/journal/entry/{id}) */
async function deleteEntry(): Promise<void> {
  if (!guardAxisWrite()) return;
  if (!props.entry.id) return;
  const stdrdDt = props.entry.stdrdDt;
  const confirmed = await swalConfirm(t("common.confirm.del"));
  if (!confirmed) return;
  try {
    const res = await axios.delete(`/api/journal/entry/${props.entry.id}`);
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
        failureFallback: t("journal.entry.delete.failure"),
      });
    }
  } catch (e: unknown) {
    void swalRequestError(e);
  }
}
</script>

<style scoped>
:deep(.journal-entry-search-keyword-mark) {
  background-color: #fff3cd;
  border-radius: 0.25rem;
  box-shadow: inset 0 -0.35em 0 rgba(255, 193, 7, 0.35);
  color: inherit;
  font-weight: 700;
  padding: 0 0.12em;
}
</style>
