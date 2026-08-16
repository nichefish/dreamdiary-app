<template>
  <div class="journal-entry-search-page">
    <!--begin::컨트롤 바-->
    <div class="d-flex flex-wrap align-items-center gap-2 mb-3">
      <!--begin::고급 필터 토글-->
      <button
        type="button"
        class="btn btn-sm btn-light-primary d-flex align-items-center text-gray-700 fs-7 fw-bold"
        @click="showAdvanced = !showAdvanced"
      >
        <span class="svg-icon svg-icon-muted svg-icon-2 me-1">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M19.0759 3H4.72777C3.95892 3 3.47768 3.83148 3.86067 4.49814L8.56967 12.6949C9.17923 13.7559 9.5 14.9582 9.5 16.1819V19.5072C9.5 20.2189 10.2223 20.7028 10.8805 20.432L13.8805 19.1977C14.2553 19.0435 14.5 18.6783 14.5 18.273V13.8372C14.5 12.8089 14.8171 11.8056 15.408 10.964L19.8943 4.57465C20.3596 3.912 19.8856 3 19.0759 3Z" fill="rgba(0, 158, 247, 0.5)"/>
          </svg>
        </span>
        {{ t("journal.day.filter.advanced") }}
      </button>
      <!--end::고급 필터 토글-->
      <!--begin::초기화-->
      <button type="button" class="btn btn-sm btn-outline btn-light-secondary text-dark" :disabled="isActionLocked" @click="resetSearch">
        {{ t("common.reset") }}
      </button>
      <!--end::초기화-->
      <!--begin::정렬 기준 (날짜/제목)-->
      <div class="btn-group btn-group-sm" role="group" :aria-label="t('journal.entry.search.sort-field.label')">
        <button
          type="button"
          :class="['btn', sortField === 'date' ? 'btn-primary' : 'btn-light']"
          :disabled="isActionLocked"
          @click="setSortField('date')"
        >{{ t('journal.entry.search.sort-field.date') }}</button>
        <button
          type="button"
          :class="['btn', sortField === 'title' ? 'btn-primary' : 'btn-light']"
          :disabled="isActionLocked"
          @click="setSortField('title')"
        >{{ t('journal.entry.search.sort-field.title') }}</button>
      </div>
      <!--end::정렬 기준-->
      <!--begin::정렬 토글-->
      <button type="button" class="btn btn-sm btn-outline btn-light-primary px-3" :disabled="isActionLocked" :title="t('journal.entry.search.sort.tooltip')" @click="toggleSort">
        <i class="bi" :class="sort === 'asc' ? 'bi-sort-down-alt' : 'bi-sort-up'"></i>
      </button>
      <!--end::정렬 토글-->
      <!--begin::검색-->
      <button type="button" class="btn btn-sm btn-primary" :disabled="isActionLocked" @click="doSearch">
        {{ t("common.search") }}
      </button>
      <!--end::검색-->
      <!--begin::구분선-->
      <div class="border-start border-gray-300 h-25px ms-1"></div>
      <!--end::구분선-->
      <!--begin::결과 전체 복사 (split — 주 버튼=해석 포함, ▾ 드롭다운=해석 제외)-->
      <div class="btn-group" role="group">
        <!--begin::주 버튼 (해석 포함)-->
        <button type="button" class="btn btn-sm btn-outline btn-light-primary px-3 copy-split-main" :disabled="!canCopyResults" :title="t('journal.entry.search.copy-all.include.tooltip')" @click="copyAll(true)">
          <i class="bi bi-copy"></i>
        </button>
        <!--end::주 버튼-->
        <!--begin::해석 제외 드롭다운-->
        <button type="button" class="btn btn-sm btn-outline btn-light-primary copy-split-caret" :disabled="!canCopyResults" data-kt-menu-trigger="click" data-kt-menu-placement="bottom-end" :title="t('common.menu')">
          <i class="bi bi-caret-down-fill fs-9 pe-0"></i>
        </button>
        <div class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold w-200px py-2" data-kt-menu="true">
          <div class="menu-item px-3 my-1 cursor-pointer">
            <div class="menu-link flex-stack px-3" @click="copyAll(false)">
              {{ t('journal.entry.search.copy-all.exclude.tooltip') }}
              <i class="bi bi-clipboard fs-8"></i>
            </div>
          </div>
        </div>
        <!--end::해석 제외 드롭다운-->
      </div>
      <!--end::결과 전체 복사 (split)-->
      <!--begin::TXT 내보내기 (split — 주 버튼=해석 포함, ▾ 드롭다운=본문만/해석 제외)-->
      <div class="btn-group" role="group">
        <button type="button" class="btn btn-sm btn-outline btn-light-primary px-3 copy-split-main" :disabled="!canExportResults" :title="t('journal.entry.search.export-txt.tooltip')" @click="exportTxt(true)">
          <i class="fas fa-download"></i>
        </button>
        <button type="button" class="btn btn-sm btn-outline btn-light-primary copy-split-caret" :disabled="!canExportResults" data-kt-menu-trigger="click" data-kt-menu-placement="bottom-end" :title="t('common.menu')">
          <i class="bi bi-caret-down-fill fs-9 pe-0"></i>
        </button>
        <div class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold w-200px py-2" data-kt-menu="true">
          <div class="menu-item px-3 my-1 cursor-pointer">
            <div class="menu-link flex-stack px-3" @click="exportTxt(false)">
              {{ t('journal.download.body.label') }}
              <i class="fas fa-download fs-8"></i>
            </div>
          </div>
        </div>
      </div>
      <!--end::TXT 내보내기 (split)-->
      <!--begin::키워드 배지 목록 (회색 — 텍스트 검색어)-->
      <div v-if="searchKeywords.length > 0" class="d-flex flex-wrap gap-2 ms-1">
        <span
          v-for="kw in searchKeywords"
          :key="kw"
          class="badge badge-light-secondary fw-lighter d-flex align-items-center gap-2 px-3 py-2 text-gray-700 cursor-pointer"
          :title="t('journal.entry.search.keyword.remove.tooltip')"
          @click="removeKeyword(kw)"
        >
          {{ kw }}
          <i class="bi bi-x"></i>
        </span>
      </div>
      <!--end::키워드 배지 목록-->
      <!--begin::제목 조건 배지 (제목만 매칭)-->
      <div v-if="title" class="d-flex align-items-center ms-1">
        <span
          class="badge badge-light-info fw-lighter d-flex align-items-center gap-2 px-3 py-2 text-info cursor-pointer"
          :title="t('journal.entry.search.title.remove.tooltip')"
          @click="removeTitle"
        >
          {{ t('journal.entry.search.title.badge-prefix') }} {{ title }}
          <i class="bi bi-x"></i>
        </span>
      </div>
      <!--end::제목 조건 배지-->
      <!--begin::태그 배지 목록 (파란색 — #태그명)-->
      <div v-if="tagIds.length > 0" class="d-flex flex-wrap gap-2 border-start border-gray-300 ps-3">
        <span
          v-for="tagId in tagIds"
          :key="tagId"
          class="badge badge-light-primary fw-lighter d-flex align-items-center gap-2 px-3 py-2 text-primary cursor-pointer"
          :title="t('journal.entry.search.tag.remove.tooltip')"
          @click="removeTag(tagId)"
        >
          #{{ tagLabelMap[tagId] ?? tagId }}
          <i class="bi bi-x"></i>
        </span>
      </div>
      <!--end::태그 배지 목록-->
      <!--begin::꿈 상태 배지 목록-->
      <div v-if="states.length > 0" class="d-flex flex-wrap gap-2 border-start border-gray-300 ps-3">
        <span
          v-for="state in states"
          :key="state"
          :class="['badge fw-lighter d-flex align-items-center gap-2 px-3 py-2 cursor-pointer', stateBadgeClass(state)]"
          :title="t('journal.entry.search.state.remove.tooltip')"
          @click="removeState(state)"
        >
          {{ getStateLabel(state) }}
          <i class="bi bi-x"></i>
        </span>
      </div>
      <!--end::꿈 상태 배지 목록-->
      <!--begin::결과 건수-->
      <div class="d-flex align-items-center gap-2 ms-auto">
        <span class="text-muted fs-8">{{ conditionSummaryLabel }}</span>
        <span v-if="resultStatusLabel" class="text-muted fs-8">{{ resultStatusLabel }}</span>
        <span class="text-muted fs-7">{{ resultLabel }}</span>
      </div>
      <!--end::결과 건수-->
    </div>
    <!--end::컨트롤 바-->

    <!--begin::고급 필터 아코디언-->
    <div v-if="searchErrorMessage" class="alert alert-warning py-3 px-4 mb-4 fs-7">
      {{ searchErrorMessage }}
    </div>

    <div v-show="showAdvanced" class="mb-4 px-4 py-3 bg-light rounded">
      <!--begin::유형 선택-->
      <div class="d-flex align-items-center gap-2 mb-3">
        <span class="fw-bold fs-7 text-gray-700 min-w-50px">{{ t("journal.entry.search.type") }}</span>
        <div class="btn-group btn-group-sm">
          <button
            type="button"
            :class="['btn', type === 'DIARY' ? 'btn-primary' : 'btn-light']"
            @click="changeType('DIARY')"
          >{{ t("common.diary") }}</button>
          <button
            type="button"
            :class="['btn', type === 'DREAM' ? 'btn-info' : 'btn-light']"
            @click="changeType('DREAM')"
          >{{ t("common.dream") }}</button>
        </div>
      </div>
      <!--end::유형 선택-->
      <!--begin::꿈 상태 선택 (복수 선택은 OR)-->
      <div v-if="type === 'DREAM'" class="d-flex align-items-center gap-2 mb-3">
        <span class="fw-bold fs-7 text-gray-700 min-w-50px">{{ t("journal.entry.search.state") }}</span>
        <div class="btn-group btn-group-sm" role="group" :aria-label="t('journal.entry.search.state')">
          <button
            type="button"
            :class="['btn', states.includes('NHTMR') ? 'btn-danger' : 'btn-light']"
            :aria-pressed="states.includes('NHTMR')"
            @click="toggleState('NHTMR')"
          >{{ t("state.nightmare") }}</button>
          <button
            type="button"
            :class="['btn', states.includes('HALLUC') ? 'btn-secondary' : 'btn-light']"
            :aria-pressed="states.includes('HALLUC')"
            @click="toggleState('HALLUC')"
          >{{ t("state.hallucination") }}</button>
        </div>
        <span class="text-muted fs-8">{{ t("journal.entry.search.state.or-hint") }}</span>
      </div>
      <!--end::꿈 상태 선택-->
      <!--begin::키워드 입력-->
      <div class="d-flex align-items-center gap-2">
        <span class="fw-bold fs-7 text-gray-700 min-w-50px">{{ t("common.keyword") }}</span>
        <input
          ref="keywordInputEl"
          v-model="keywordInput"
          type="text"
          class="form-control form-control-sm journal-entry-search-input"
          :placeholder="t('journal.entry.search.keyword.placeholder')"
          :title="t('journal.entry.search.input.enter-to-add')"
          maxlength="200"
          @keydown.enter.prevent="addKeyword"
        />
        <button type="button" class="btn btn-sm btn-light-primary w-100px" @click="addKeyword">
          + {{ t("common.add") }}
        </button>
      </div>
      <div class="text-muted fs-8 mt-1" style="padding-left: 60px;">
        {{ t("journal.entry.search.input.enter-to-add") }}
      </div>
      <!--end::키워드 입력-->
      <!--begin::제목 검색 입력 (제목만 매칭 — 키워드는 제목+본문)-->
      <div class="d-flex align-items-center gap-2 mt-3">
        <span class="fw-bold fs-7 text-gray-700 min-w-50px">{{ t('journal.entry.search.title.label') }}</span>
        <input
          v-model="titleInput"
          type="text"
          class="form-control form-control-sm journal-entry-search-input"
          :placeholder="t('journal.entry.search.title.placeholder')"
          maxlength="100"
          @keydown.enter.prevent="doSearch"
        />
        <button type="button" class="btn btn-sm btn-light-primary w-100px" @click="doSearch">
          <i class="bi bi-search"></i>
        </button>
      </div>
      <!--end::제목 검색 입력-->
      <!--begin::태그 입력-->
      <div class="d-flex align-items-center gap-2 mt-3">
        <span class="fw-bold fs-7 text-gray-700 min-w-50px">{{ t("common.tag") }}</span>
        <input
          v-model="tagInput"
          type="text"
          class="form-control form-control-sm journal-entry-search-input"
          :placeholder="t('journal.entry.search.tag.placeholder')"
          :title="tagInputTitle"
          maxlength="100"
          list="journal-entry-search-tag-options"
          autocomplete="off"
          :disabled="isTagCategoryChoicePending"
          @focus="ensureTagSelectorData()"
          @keydown.enter.prevent="addTagFromInput"
        />
        <datalist id="journal-entry-search-tag-options">
          <option
            v-for="tagName in tagNameOptions"
            :key="tagName"
            :value="tagName"
          />
        </datalist>
        <button type="button" class="btn btn-sm btn-light-primary w-100px" :disabled="isTagCategoryChoicePending" @click="addTagFromInput">
          + {{ t("common.add") }}
        </button>
      </div>
      <div class="text-muted fs-8 mt-1" style="padding-left: 60px;">
        {{ tagInputHint }}
      </div>
      <div v-if="tagCategoryChoices.length > 0" class="d-flex align-items-center gap-2 mt-2" style="padding-left: 60px;">
        <span class="text-muted fs-8">{{ t("journal.entry.search.category.select") }}</span>
        <button
          v-for="ctgr in tagCategoryChoices"
          :key="ctgr"
          type="button"
          class="btn btn-xs btn-light-primary"
          @click="selectTagCategory(ctgr)"
        >
          {{ ctgr || t("journal.entry.search.category.none") }}
        </button>
        <button type="button" class="btn btn-xs btn-light-secondary" @click="cancelTagCategoryChoice">
          {{ t("common.cancel") }}
        </button>
      </div>
      <div v-if="hasPendingSearchInputs" class="text-primary fs-8 mt-3" style="padding-left: 60px;">
        {{ t("journal.entry.search.pending-inputs") }}
      </div>
      <!--end::태그 입력-->
    </div>
    <!--end::고급 필터 아코디언-->

    <!--begin::결과 목록-->
    <div v-if="!hasSearchConditions" class="text-muted fs-7 py-10">
      <div class="mb-3">{{ t("journal.entry.search.no-condition") }}</div>
      <button type="button" class="btn btn-sm btn-light-primary" @click="openConditionEditor">
        {{ t("journal.entry.search.add-condition") }}
      </button>
    </div>
    <div v-else-if="loading && entries.length === 0" class="text-muted fs-7 py-10">{{ t("journal.entry.search.loading") }}</div>
    <div v-else-if="!loading && !searchErrorMessage && searchAttempted && entries.length === 0" class="text-muted fs-7 py-10">
      <div class="mb-3">{{ t("common.search.rslt.empty") }}</div>
      <button type="button" class="btn btn-sm btn-light-primary" @click="openConditionEditor">
        {{ t("journal.entry.search.refine-condition") }}
      </button>
    </div>
    <div v-else class="d-flex flex-column">
      <div v-if="loading" class="text-muted fs-8 px-2 pb-2">
        {{ t("journal.entry.search.refreshing.keep-previous") }}
      </div>
      <div class="text-muted fs-8 px-2 pb-2">
        {{ resultSummaryLabel }}
      </div>
      <template v-for="(entry, idx) in entries" :key="entry.id">
        <!--begin::월 구분선 (월이 바뀔 때만)-->
        <div
          v-if="idx === 0 || getYyMm(entry.stdrdDt) !== getYyMm(entries[idx - 1].stdrdDt)"
          class="d-flex align-items-center gap-3 my-4"
        >
          <div class="flex-grow-1 border-bottom border-gray-300"></div>
          <span class="text-muted fs-7 fw-bold text-nowrap">{{ getYyMmLabel(entry.stdrdDt) }}</span>
          <div class="flex-grow-1 border-bottom border-gray-300"></div>
        </div>
        <!--end::월 구분선-->
        <!--begin::날짜 헤더 (날짜가 바뀔 때만)-->
        <div
          v-if="idx === 0 || entry.stdrdDt !== entries[idx - 1].stdrdDt"
          class="d-flex align-items-center gap-2 fw-bold fs-6 ps-2 pt-3 pb-1"
          :class="entry.isHolyday ? 'text-danger' : 'text-gray-700'"
        >
          <span>{{ entry.stdrdDt }}</span>
          <span
            v-if="entry.stdrdDt"
            class="fs-7"
            :class="entry.isHolyday ? 'text-danger' : 'text-muted'"
          >({{ getDisplayWeekDayStr(entry.stdrdDt) }})</span>
          <span v-if="entry.holydayNm" class="fs-7 fw-normal text-muted text-truncate">{{ entry.holydayNm }}</span>
          <span v-if="entry.stdrdDt" class="badge badge-light-secondary fw-lighter">
            {{ getDateEntryCountLabel(entry.stdrdDt) }}
          </span>
          <!--begin::이 날짜 엔트리 복사 (split — 주 버튼=전체/해석 포함, ▾ 드롭다운=본문만/해석 제외)-->
          <div v-if="entry.stdrdDt" class="btn-group" role="group">
            <button
              type="button"
              class="btn btn-xs btn-icon btn-light-primary copy-split-main"
              :title="t('journal.entry.search.copy-date.include.tooltip')"
              @click="copyDate(entry.stdrdDt, true)"
            >
              <i class="bi bi-copy fs-8"></i>
            </button>
            <button
              type="button"
              class="btn btn-xs btn-icon btn-light-primary copy-split-caret"
              data-kt-menu-trigger="click"
              data-kt-menu-placement="bottom-end"
              :title="t('common.menu')"
            >
              <i class="bi bi-caret-down-fill fs-9 pe-0"></i>
            </button>
            <div class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold w-200px py-2" data-kt-menu="true">
              <div class="menu-item px-3 my-1 cursor-pointer">
                <div class="menu-link flex-stack px-3" @click="copyDate(entry.stdrdDt, false)">
                  {{ t('journal.entry.search.copy-date.exclude.tooltip') }}
                  <i class="bi bi-clipboard fs-8"></i>
                </div>
              </div>
            </div>
          </div>
          <!--end::이 날짜 엔트리 복사 (split)-->
          <button
            v-if="entry.stdrdDt"
            type="button"
            class="btn btn-xs btn-icon btn-light-primary"
            :title="t('journal.entry.search.open-daily.tooltip')"
            @click="openDailyView(entry.stdrdDt)"
          >
            <i class="bi bi-box-arrow-up-right fs-8"></i>
          </button>
        </div>
        <!--end::날짜 헤더-->
        <JournalEntryItem
          :dom-id="entry.id ? 'journal-entry-search-' + entry.id : undefined"
          :entry="entry"
          :highlight-keywords="searchKeywords"
          :is-dream="entry.contentType === 'JOURNAL_DREAM'"
        />
      </template>
    </div>
    <!--end::결과 목록-->

    <!--begin::모달 컨테이너-->
    <JournalEntryRegistModal
      @prepare-success="onEntrySavePrepare"
      @success="onEntrySaveSuccess"
    />
    <JournalReflectionRegistModal />
    <CommentRegistModal />
    <CommentListModal />
    <HistoryModal @success="onHistorySuccess" />
    <RelatedContentAddModal />
    <JournalTagContextMenu />
    <JournalTagProfileModal @success="onTagProfileSuccess" />
    <!--end::모달 컨테이너-->
  </div>
</template>

<script setup lang="ts">
/**
 * JournalEntrySearchPage.vue
 * 저널 엔트리(일기/꿈) 검색 전체 페이지 (팝업 전용).
 * JournalEntryItem 을 그대로 사용해 저널 일자 목록과 동일한 UI·컨텍스트 메뉴 제공.
 * 레거시 journal_entry_search_module.ts 의 멀티키워드·멀티태그 AND 검색을 Vue SPA 로 재현.
 */
import { computed, nextTick, onScopeDispose, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import axios from "axios";
import Swal from "sweetalert2/dist/sweetalert2.js";
import { swalAlert } from "@/shared/utils/swal";
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
import type { JournalEntryDto } from "@/features/journal/stores/journal";
import { registerJournalEntrySearchHost } from "@/features/journal/utils/journalEntryHostRefresh";
import { getWeekDayStr } from "@/features/journal/utils/journalDate";
import { htmlToPlainText } from "@/features/journal/utils/htmlToPlainText";
import { joinAppBasePath } from "@/shared/utils/appPath";
import { reinitMetronicAfterDom } from "@/shared/utils/metronicReinit";
import {
  buildEntrySearchParams,
  buildEntrySearchRouteQuery,
  parseEntrySearchQuery,
} from "@/features/journal/utils/entrySearchQuery";
import JournalEntryItem from "./components/JournalEntryItem.vue";
import JournalEntryRegistModal from "./modals/JournalEntryRegistModal.vue";
import JournalReflectionRegistModal from "../reflection/modals/JournalReflectionRegistModal.vue";
import CommentRegistModal from "../shared/modals/CommentRegistModal.vue";
import CommentListModal from "@/features/attachable/CommentListModal.vue";
import HistoryModal from "@/features/attachable/HistoryModal.vue";
import RelatedContentAddModal from "../shared/modals/RelatedContentAddModal.vue";
import JournalTagContextMenu from "../shared/components/JournalTagContextMenu.vue";
import JournalTagProfileModal from "../shared/modals/JournalTagProfileModal.vue";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

interface JournalEntrySaveEvent {
  entryId?: number | string;
  stdrdDt?: string;
  isModify?: boolean;
}

interface JournalEntrySavePrepareEvent extends JournalEntrySaveEvent {
  waitUntil: (task: Promise<void>) => void;
}

interface SearchTagDto {
  id?: number | string;
  tagId?: number | string;
  name?: string;
  ctgr?: string;
}

const route = useRoute();
const router = useRouter();
const threadStore = useJournalThreadStore();
const { t } = useLocaleStore();

const entries = ref<JournalEntryDto[]>([]);
const loading = ref(false);
const actionInProgress = ref(false);
const conditionChangedMessage = ref("");
const keywordInput = ref("");
const keywordInputEl = ref<HTMLInputElement | null>(null);
const tagInput = ref("");
const tagCategoryMap = ref<Record<string, string[]>>({});
const tagCatalog = ref<SearchTagDto[]>([]);
const tagSelectorLoadedType = ref("");
const pendingTagName = ref("");
const tagCategoryChoices = ref<string[]>([]);
const showAdvanced = ref(false);

const type = ref("DIARY");
const sort = ref("desc");
/** 정렬 기준 축: "date"(기본) | "title" */
const sortField = ref("date");
const tagIds = ref<string[]>([]);
const searchKeywords = ref<string[]>([]);
/** 꿈 전용 상태 검색 조건. URL에는 NHTMR/HALLUC만 보존하며 복수 선택은 OR로 조회한다. */
const states = ref<string[]>([]);
/** 제목 전용 검색 — 적용된 조건(URL SSOT)과 입력 박스를 분리한다(키워드 패턴과 동일). */
const title = ref("");
const titleInput = ref("");
const searchAttempted = ref(false);
const searchErrorMessage = ref("");

/** tagId 를 화면 표시명으로 바꾸기 위한 로컬 캐시. URL 검색 조건에는 tagIds 만 사용한다. */
const tagLabelMap = ref<Record<string, string>>({});

const hasSearchConditions = computed(() =>
  searchKeywords.value.length > 0
  || tagIds.value.length > 0
  || states.value.length > 0
  || title.value.trim().length > 0,
);
const hasPendingSearchInputs = computed(() => keywordInput.value.trim().length > 0 || tagInput.value.trim().length > 0);
const isTagCategoryChoicePending = computed(() => tagCategoryChoices.value.length > 0);
const isActionLocked = computed(() => loading.value || actionInProgress.value);
const canCopyResults = computed(() => !isActionLocked.value && (entries.value.length > 0 || hasPendingSearchInputs.value));
const canExportResults = computed(() => !isActionLocked.value && (hasSearchConditions.value || hasPendingSearchInputs.value));
const resultLabel = computed(() => t("journal.entry.search.result-count").replace("{0}", String(entries.value.length)));
const resultSummaryLabel = computed(() =>
  t("journal.entry.search.result-summary")
    .replace("{0}", String(entries.value.length))
    .replace("{1}", String(resultDateCount.value))
    .replace("{2}", String(resultMonthCount.value))
);
const typeLabel = computed(() => type.value === "DREAM" ? t("common.dream") : t("common.diary"));
const sortLabel = computed(() => sort.value === "asc" ? t("journal.entry.search.sort.asc") : t("journal.entry.search.sort.desc"));
const resultStatusLabel = computed(() => {
  if (!hasSearchConditions.value) return "";
  if (loading.value && entries.value.length > 0) return t("journal.entry.search.refreshing");
  if (conditionChangedMessage.value) return conditionChangedMessage.value;
  return t("journal.entry.search.result-basis");
});
const conditionSummaryLabel = computed(() =>
  t("journal.entry.search.condition-summary")
    .replace("{0}", typeLabel.value)
    .replace("{1}", sortLabel.value)
    .replace("{2}", String(searchKeywords.value.length))
    .replace("{3}", String(tagIds.value.length))
    .replace("{4}", String(states.value.length))
);

const tagNameOptions = computed(() => Object.keys(tagCategoryMap.value).sort((a, b) => a.localeCompare(b)));
const resultDateCount = computed(() => new Set(entries.value.map((entry) => entry.stdrdDt).filter(Boolean)).size);
const resultMonthCount = computed(() => new Set(entries.value.map((entry) => getYyMm(entry.stdrdDt)).filter(Boolean)).size);
const tagInputHint = computed(() => isTagCategoryChoicePending.value
  ? t("journal.entry.search.tag.category.pending")
  : t("journal.entry.search.input.enter-to-add"));
const tagInputTitle = computed(() => isTagCategoryChoicePending.value
  ? t("journal.entry.search.tag.category.pending")
  : t("journal.entry.search.input.enter-to-add"));

/** route query 를 파싱해 로컬 ref 에 반영한다. */
function syncFromRoute(): void {
  const cond = parseEntrySearchQuery(route.query);
  type.value = cond.type;
  sort.value = cond.sort;
  sortField.value = cond.sortField;
  tagIds.value = cond.tagIds;
  searchKeywords.value = cond.searchKeywords;
  states.value = cond.states;
  title.value = cond.title;
  titleInput.value = cond.title;
  keywordInput.value = "";
  tagInput.value = "";
  cancelTagCategoryChoice();
  searchErrorMessage.value = "";
}

watch(() => route.fullPath, () => {
  syncFromRoute();
  void loadEntries();
}, { immediate: true });

async function loadEntries(): Promise<void> {
  if (!hasSearchConditions.value) {
    entries.value = [];
    loading.value = false;
    searchAttempted.value = false;
    searchErrorMessage.value = "";
    conditionChangedMessage.value = "";
    return;
  }

  loading.value = true;
  searchAttempted.value = true;
  searchErrorMessage.value = "";
  try {
    const params = buildEntrySearchParams({
      type: type.value,
      sort: sort.value,
      sortField: sortField.value,
      tagIds: tagIds.value,
      searchKeywords: searchKeywords.value,
      states: states.value,
      title: title.value,
    });

    const res = await axios.get("/api/journal/entries", { params });
    if (!res.data?.rslt) {
      console.error("[JournalEntrySearchPage] entry search soft-fail", { message: res.data?.message });
      searchErrorMessage.value = (res.data?.message as string | undefined)
        ?? t("journal.entry.search.load.failure.keep-previous").replace("{0}", String(entries.value.length));
      return;
    }
    entries.value = res.data?.rsltList ?? [];
    conditionChangedMessage.value = "";
    hydrateTagNamesFromEntries(entries.value);
    void hydrateMissingTagNames();
  } catch (e: unknown) {
    console.error("[JournalEntrySearchPage] entry search failed", e);
    searchErrorMessage.value = t("journal.entry.search.load.failure.keep-previous")
      .replace("{0}", String(entries.value.length));
  } finally {
    loading.value = false;
    void reinitMetronicAfterDom();
  }
}

function cacheTagName(tagId?: number | string, name?: string): void {
  if (tagId === undefined || tagId === null || !name) return;
  tagLabelMap.value[String(tagId)] = name;
}

function hydrateTagNamesFromEntries(entryList: JournalEntryDto[]): void {
  entryList.forEach((entry) => {
    (entry.tag?.list ?? []).forEach((tag) => cacheTagName(tag.tagId, tag.name));
  });
}

async function hydrateMissingTagNames(): Promise<void> {
  const missingIds = tagIds.value.filter((tagId) => !tagLabelMap.value[tagId]);
  if (missingIds.length === 0) return;

  const requestedType = type.value;
  try {
    const res = await axios.get("/api/journal/entry/tags", { params: { type: requestedType } });
    if (requestedType !== type.value) return;
    const list = (res.data?.rsltList ?? []) as SearchTagDto[];
    list.forEach((tag) => cacheTagName(tag.id ?? tag.tagId, tag.name));
  } catch {
    // 태그명 표시에 실패해도 tagIds 검색 자체는 유지한다.
  }
}

async function ensureTagSelectorData(): Promise<void> {
  const requestedType = type.value;
  if (tagSelectorLoadedType.value === requestedType) return;

  try {
    const [categoryRes, tagRes] = await Promise.all([
      axios.get("/api/journal/entry/tag/categories", { params: { type: requestedType } }),
      axios.get("/api/journal/entry/tags", { params: { type: requestedType } }),
    ]);
    if (requestedType !== type.value) return;
    tagCatalog.value = (tagRes.data?.rsltList ?? []) as SearchTagDto[];
    tagCategoryMap.value = mergeCatalogIntoCategoryMap(
      normalizeCategoryMap(categoryRes.data?.rsltMap ?? categoryRes.data?.rsltObj),
      tagCatalog.value,
    );
    tagCatalog.value.forEach((tag) => cacheTagName(tag.id ?? tag.tagId, tag.name));
    tagSelectorLoadedType.value = requestedType;
  } catch {
    console.warn("[JournalEntrySearchPage] tag selector data load failed.", { type: requestedType });
  }
}

function mergeCatalogIntoCategoryMap(baseMap: Record<string, string[]>, catalog: SearchTagDto[]): Record<string, string[]> {
  const next: Record<string, string[]> = {};
  for (const [tagName, categories] of Object.entries(baseMap)) {
    next[tagName] = [...categories];
  }
  catalog.forEach((tag) => {
    const name = String(tag.name ?? "").trim();
    if (!name) return;
    const ctgr = String(tag.ctgr ?? "");
    const categories = next[name] ? [...next[name]] : [];
    if (!categories.includes(ctgr)) categories.push(ctgr);
    next[name] = categories;
  });
  return next;
}

function normalizeCategoryMap(raw: unknown): Record<string, string[]> {
  if (!raw || typeof raw !== "object" || Array.isArray(raw)) return {};
  const out: Record<string, string[]> = {};
  for (const [tagName, categories] of Object.entries(raw as Record<string, unknown>)) {
    if (!Array.isArray(categories)) continue;
    out[tagName] = categories.map((c) => String(c ?? "")).filter((c) => c.length > 0);
  }
  return out;
}

function normalizeTagName(raw: string): string {
  return raw.trim().replace(/\s+/g, "_");
}

function findKnownTagName(input: string): string {
  const normalized = normalizeTagName(input);
  if (tagCategoryMap.value[normalized]) return normalized;
  return tagNameOptions.value.find((name) => name.toLowerCase() === normalized.toLowerCase()) ?? normalized;
}

async function addTagFromInput(): Promise<boolean> {
  await ensureTagSelectorData();
  const tagName = findKnownTagName(tagInput.value);
  const categories = tagCategoryMap.value[tagName] ?? [];
  if (!tagName || categories.length === 0) {
    void swalAlert(t("journal.entry.search.tag.select-existing"));
    return false;
  }
  if (categories.length === 1) {
    return addTagByNameAndCategory(tagName, categories[0]);
  }
  pendingTagName.value = tagName;
  tagCategoryChoices.value = categories;
  return false;
}

function selectTagCategory(ctgr: string): void {
  void addTagByNameAndCategory(pendingTagName.value, ctgr);
}

function cancelTagCategoryChoice(): void {
  pendingTagName.value = "";
  tagCategoryChoices.value = [];
}

async function addTagByNameAndCategory(tagName: string, ctgr: string): Promise<boolean> {
  const matchedTag = tagCatalog.value.find((tag) =>
    String(tag.name ?? "") === tagName && String(tag.ctgr ?? "") === ctgr
  );
  const tagId = matchedTag?.id ?? matchedTag?.tagId;
  if (tagId === undefined || tagId === null) {
    console.warn("[JournalEntrySearchPage] selected tag id not found.", { tagName, ctgr });
    void swalAlert(t("journal.entry.search.tag.not-found"));
    return false;
  }

  const nextTagId = String(tagId);
  cacheTagName(nextTagId, tagName);
  tagInput.value = "";
  cancelTagCategoryChoice();
  if (tagIds.value.includes(nextTagId)) {
    void swalAlert(t("journal.entry.search.tag.duplicate"));
    return false;
  }
  await pushQuery({ tagIds: [...tagIds.value, nextTagId] });
  return true;
}

async function scrollToSearchEntry(entryId?: number | string): Promise<void> {
  await nextTick();
  window.requestAnimationFrame(() => {
    const el = entryId
      ? document.getElementById(`journal-entry-search-${entryId}`)
      : null;
    if (el) {
      el.scrollIntoView({ behavior: "smooth", block: "center" });
    } else {
      console.warn("[JournalEntrySearchPage] saved entry scroll target not found.", { entryId });
    }
  });
}

/** 현재 로컬 ref 상태를 URL query 로 replace한다. */
async function pushQuery(overrides: Partial<{ type: string; sort: string; sortField: string; tagIds: string[]; searchKeywords: string[]; states: string[]; title: string }> = {}, statusMessage = ""): Promise<void> {
  conditionChangedMessage.value = statusMessage;
  const t = overrides.type ?? type.value;
  const s = overrides.sort ?? sort.value;
  const sf = overrides.sortField ?? sortField.value;
  const ids = overrides.tagIds ?? tagIds.value;
  const kws = overrides.searchKeywords ?? searchKeywords.value;
  const sts = overrides.states ?? states.value;
  const ttl = overrides.title ?? title.value;
  const query = buildEntrySearchRouteQuery({ type: t, sort: s, sortField: sf, tagIds: ids, searchKeywords: kws, states: sts, title: ttl });
  await router.replace({ name: "journal-entry-search", query });
}

/** 키워드 추가. 중복 입력은 사용자에게 알려 검색 조건 변화가 없음을 명시한다. */
async function addKeyword(): Promise<boolean> {
  const kw = keywordInput.value.trim();
  if (!kw) return false;
  if (searchKeywords.value.map((k) => k.toLowerCase()).includes(kw.toLowerCase())) {
    keywordInput.value = "";
    void swalAlert(t("journal.entry.search.keyword.duplicate"));
    return false;
  }
  const next = [...searchKeywords.value, kw];
  keywordInput.value = "";
  await pushQuery({ searchKeywords: next });
  return true;
}

/** 키워드 배지 X 클릭 → 제거 후 재검색 */
function removeKeyword(kw: string): void {
  pushQuery(
    { searchKeywords: searchKeywords.value.filter((k) => k !== kw) },
    t("journal.entry.search.condition.keyword-removed"),
  );
}

/** 태그 배지 X 클릭 → 캐시·URL 에서 제거 후 재검색 */
function removeTag(tagId: string): void {
  delete tagLabelMap.value[tagId];
  pushQuery(
    { tagIds: tagIds.value.filter((id) => id !== tagId) },
    t("journal.entry.search.condition.tag-removed"),
  );
}

/** 꿈 상태 배지 X 클릭 → 제거 후 재검색 */
function removeState(state: string): void {
  void pushQuery(
    { states: states.value.filter((key) => key !== state) },
    t("journal.entry.search.condition.state-changed"),
  );
}

/** 꿈 상태 토글. 복수 선택 시 백엔드 state EXISTS/IN 계약에 따라 OR로 검색한다. */
function toggleState(state: "NHTMR" | "HALLUC"): void {
  const nextStates = states.value.includes(state)
    ? states.value.filter((key) => key !== state)
    : [...states.value, state];
  void pushQuery({ states: nextStates }, t("journal.entry.search.condition.state-changed"));
}

function getStateLabel(state: string): string {
  return state === "HALLUC" ? t("state.hallucination") : t("state.nightmare");
}

function stateBadgeClass(state: string): string {
  return state === "HALLUC" ? "badge-light-secondary text-gray-700" : "badge-light-danger text-danger";
}

/** 검색 실행 (고급 필터 아코디언의 "검색" 버튼) */
async function doSearch(): Promise<void> {
  if (isActionLocked.value) return;
  if (!await finalizePendingSearchInputs()) return;
  if (!hasSearchConditions.value) {
    entries.value = [];
    searchAttempted.value = false;
    searchErrorMessage.value = "";
    conditionChangedMessage.value = "";
    return;
  }
  /* 이미 URL 과 일치하므로 강제 재조회만 수행 */
  void loadEntries();
}

/** 실행 버튼들이 같은 규칙으로 입력 중인 키워드/태그를 URL 검색 조건에 먼저 반영한다. */
async function finalizePendingSearchInputs(): Promise<boolean> {
  if (keywordInput.value.trim() && !await addKeyword()) return false;
  if (tagInput.value.trim() && !await addTagFromInput()) return false;
  if (titleInput.value.trim() !== title.value) {
    await pushQuery({ title: titleInput.value.trim() });
  }
  return true;
}

async function openConditionEditor(): Promise<void> {
  showAdvanced.value = true;
  await nextTick();
  keywordInputEl.value?.focus();
}

/** 정렬 토글 */
function toggleSort(): void {
  pushQuery({ sort: sort.value === "desc" ? "asc" : "desc" }, t("journal.entry.search.condition.sort-changed"));
}

/** 정렬 기준 변경 (날짜/제목) */
function setSortField(field: string): void {
  if (sortField.value === field) return;
  pushQuery({ sortField: field }, t("journal.entry.search.condition.sort-changed"));
}

/** 제목 조건 제거 → 재검색 */
function removeTitle(): void {
  titleInput.value = "";
  void pushQuery({ title: "" }, t("journal.entry.search.condition.title-changed"));
}

/** 유형 변경 (키워드·태그 유지) */
function changeType(newType: string): void {
  tagInput.value = "";
  cancelTagCategoryChoice();
  pushQuery(
    { type: newType, states: newType === "DREAM" ? states.value : [] },
    t("journal.entry.search.condition.type-changed"),
  );
}

/** 초기화 */
function resetSearch(): void {
  tagLabelMap.value = {};
  entries.value = [];
  searchAttempted.value = false;
  searchErrorMessage.value = "";
  conditionChangedMessage.value = "";
  void router.replace({ name: "journal-entry-search", query: { type: type.value } });
}

/** TXT 내보내기. 레거시 exportUrl = /api/journal/entries/export */
async function exportTxt(includeReflection = true): Promise<void> {
  if (isActionLocked.value) return;
  actionInProgress.value = true;
  try {
    if (!await finalizePendingSearchInputs()) return;
    if (!hasSearchConditions.value) {
      void swalAlert(t("journal.entry.search.condition.required"));
      return;
    }
    const params = buildEntrySearchParams({
      type: type.value,
      sort: sort.value,
      sortField: sortField.value,
      tagIds: tagIds.value,
      searchKeywords: searchKeywords.value,
      states: states.value,
      title: title.value,
    });
    window.location.href = `/api/journal/entries/export?${params.toString()}&includeReflection=${includeReflection}`;
  } finally {
    actionInProgress.value = false;
  }
}

/** 일자 뷰를 새 창으로 연다. */
function openDailyView(stdrdDt: string | undefined): void {
  if (!stdrdDt) return;
  const w = Math.min(1600, window.screen.availWidth);
  const h = Math.min(1080, window.screen.availHeight);
  window.open(joinAppBasePath(`/journal/daily-popup?stdrdDt=${stdrdDt}`), "_blank", `width=${w},height=${h}`);
}

/**
 * 검색 팝업에서 엔트리 저장(등록·수정) 성공 시 검색 결과를 재조회한다.
 * <p>
 * 변경 전에는 수정 대상 엔트리를 상세 조회해 자리에서 부분 교체(splice)했다. 그 방식은
 * 목록에 남아 있는 엔트리의 내용만 갱신할 뿐, 검색 조건(`tagIds` AND) 기준 소속 변화를
 * 반영하지 못했다. 예: 검색 필터로 걸린 태그를 수정으로 제거해도 결과 목록에서 빠지지 않았다.
 * 변경 후에는 서버 검색 쿼리를 소속(결과 포함 여부)의 단일 진실 원천으로 보고 `loadEntries()`로
 * 재조회해 빠짐/들어옴을 항상 서버 기준으로 반영한다. 저장 위치 스크롤은 `onEntrySaveSuccess`가
 * 담당하며, 재조회로 엔트리가 빠지면 스크롤 타깃 미발견으로 자연히 넘어간다.
 */
async function prepareEntrySaveDom(): Promise<void> {
  await loadEntries();
}

function onEntrySavePrepare(payload: JournalEntrySavePrepareEvent): void {
  payload.waitUntil(prepareEntrySaveDom());
}

async function onEntrySaveSuccess(payload?: JournalEntrySaveEvent): Promise<void> {
  const entryId = payload?.entryId;
  if (!entryId) return;
  await scrollToSearchEntry(entryId);
}

/**
 * 이력 복원/삭제 성공 시 검색 결과를 갱신한다.
 * 검색 결과 위에 스레드 상세가 열려 있으면 전경 상세도 함께 갱신한다.
 */
function onHistorySuccess(): void {
  const tasks: Promise<unknown>[] = [loadEntries()];
  if (threadStore.detailOpen) tasks.push(threadStore.refreshOpenDetail());
  void Promise.all(tasks);
}

/** 태그 프로필 저장·삭제 후 검색 결과(프로필 본문 등)를 다시 조회한다. */
function onTagProfileSuccess(): void {
  void loadEntries();
}

/**
 * 현재 검색 결과 전체를 클립보드에 복사.
 * 레거시 JournalEntrySearch.copy() 와 동일 포맷:
 *   날짜(요일)\n#순번\n본문 — 날짜가 바뀔 때만 날짜 헤더 삽입, 엔트리 간 빈 줄.
 */
async function copyAll(includeReflection: boolean): Promise<void> {
  if (isActionLocked.value) return;
  actionInProgress.value = true;
  try {
    const hasPendingInput = keywordInput.value.trim().length > 0 || tagInput.value.trim().length > 0;
    if (!await finalizePendingSearchInputs()) return;
    if (hasPendingInput) {
      await loadEntries();
      if (searchErrorMessage.value) return;
    }
    if (entries.value.length === 0) {
      void swalAlert(t("journal.entry.search.copy.empty"));
      return;
    }
    let prevDate: string | null = null;
    const blocks = entries.value.map((entry) => {
      const dt = entry.stdrdDt ?? "";
      const weekDay = getWeekDayStr(dt, t);
      const dateLabel = dt ? (weekDay ? `${dt} (${weekDay})` : dt) : "";
      const content = htmlToPlainText(entry.content ?? entry.markdownContent ?? "");
      let block = "";
      if (dateLabel !== prevDate) {
        block += `\r\n${dateLabel}\r\n`;
        prevDate = dateLabel;
      }
      block += [`#${entry.sortOrder ?? ""}`, content].join("\r\n");
      /* 해석 포함 시에만: 이 엔트리를 target 으로 한 리플렉션 본문을 빈 줄로 이어 붙인다(포맷: 마커 없음). */
      if (includeReflection) {
        for (const reflection of entry.reflectionList ?? []) {
          const reflRaw = htmlToPlainText(reflection.content ?? reflection.markdownContent ?? "");
          if (reflRaw) block += `

${reflRaw}`;
        }
      }
      return block;
    });
    const text = blocks.join("\r\n\r\n").trim();
    try {
      await navigator.clipboard.writeText(text);
      void Swal.fire({
        text: t(hasPendingInput ? "journal.entry.search.copy.success-with-pending" : "journal.entry.search.copy.success-count")
          .replace("{0}", String(entries.value.length)),
        timer: 1500,
        showConfirmButton: false,
      });
    } catch (error: unknown) {
      console.error("[journal-entry-search] clipboard copy failed", error);
      void swalAlert(t("common.copy.failure"));
    }
  } finally {
    actionInProgress.value = false;
  }
}

/**
 * 검색 결과 중 특정 날짜에 속한 엔트리만 클립보드에 복사한다.
 * copyAll 과 동일 포맷(날짜(요일) 헤더 1회 + #순번\n본문, 엔트리 간 빈 줄)을 그 날짜 한정으로 적용한다.
 * 복사 계약: 소스는 저작 원문 content 우선(→ htmlToPlainText). 해석 포함 시 target 리플렉션 본문을 빈 줄로 이어 붙인다.
 *
 * @param stdrdDt 대상 일자(YYYY-MM-DD)
 * @param includeReflection 해석(리플렉션) 포함 여부
 */
async function copyDate(stdrdDt: string | undefined, includeReflection: boolean): Promise<void> {
  if (!stdrdDt || isActionLocked.value) return;
  actionInProgress.value = true;
  try {
    const dateEntries = entries.value.filter((entry) => entry.stdrdDt === stdrdDt);
    if (dateEntries.length === 0) {
      void swalAlert(t("journal.entry.search.copy.empty"));
      return;
    }
    const weekDay = getWeekDayStr(stdrdDt, t);
    const dateLabel = weekDay ? `${stdrdDt} (${weekDay})` : stdrdDt;
    const blocks = dateEntries.map((entry) => {
      const content = htmlToPlainText(entry.content ?? entry.markdownContent ?? "");
      let block = [`#${entry.sortOrder ?? ""}`, content].join("\r\n");
      /* 해석 포함 시에만: 이 엔트리를 target 으로 한 리플렉션 본문을 빈 줄로 이어 붙인다(포맷: 마커 없음). */
      if (includeReflection) {
        for (const reflection of entry.reflectionList ?? []) {
          const reflRaw = htmlToPlainText(reflection.content ?? reflection.markdownContent ?? "");
          if (reflRaw) block += `\r\n\r\n${reflRaw}`;
        }
      }
      return block;
    });
    const text = `${dateLabel}\r\n${blocks.join("\r\n\r\n")}`.trim();
    try {
      await navigator.clipboard.writeText(text);
      void Swal.fire({
        text: t("journal.entry.search.copy-date.success")
          .replace("{0}", stdrdDt)
          .replace("{1}", String(dateEntries.length)),
        timer: 1500,
        showConfirmButton: false,
      });
    } catch (error: unknown) {
      console.error("[journal-entry-search] clipboard date copy failed", error);
      void swalAlert(t("common.copy.failure"));
    }
  } finally {
    actionInProgress.value = false;
  }
}

/** HTML 태그 제거 후 일반 텍스트로 변환 (줄바꿈 보존). 레거시 cF.util.htmlToText 와 동일 동작. */
/** YYYY-MM-DD → "YYYY-MM" (월 변경 감지용) */
function getYyMm(stdrdDt?: string | null): string {
  return stdrdDt?.slice(0, 7) ?? "";
}

/** YYYY-MM-DD → "YYYY년 M월" 표시 레이블 */
function getYyMmLabel(stdrdDt?: string | null): string {
  if (!stdrdDt) return "";
  const [yy, mm] = stdrdDt.split("-");
  return t("journal.entry.search.year-month")
    .replace("{0}", yy)
    .replace("{1}", String(Number(mm)));
}

function getDisplayWeekDayStr(stdrdDt?: string | null): string {
  return getWeekDayStr(stdrdDt, t);
}

function getDateEntryCountLabel(stdrdDt?: string | null): string {
  const count = entries.value.filter((entry) => entry.stdrdDt === stdrdDt).length;
  return t("journal.entry.search.date-entry-count").replace("{0}", String(count));
}

/**
 * 엔트리 액션 후 `refreshJournalEntryHostForRoute`가 호출할 검색 로컬 갱신 경로.
 * 변경 전: journalStore.loading watch로 fetchDays 부수효과에 의존해 스레드 소속 칩이 빠질 수 있었다.
 * 변경 후: 호스트 갱신이 이 콜백으로 loadEntries를 직접 호출한다.
 */
const unregisterSearchHost = registerJournalEntrySearchHost(() => loadEntries());
onScopeDispose(unregisterSearchHost);
</script>

<style scoped>
.journal-entry-search-page {
  padding: 1.5rem 2rem;
}

.journal-entry-search-input {
  max-width: 28rem;
}
</style>
