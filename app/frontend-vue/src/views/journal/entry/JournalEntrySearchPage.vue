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
        고급 필터
      </button>
      <!--end::고급 필터 토글-->
      <!--begin::초기화-->
      <button type="button" class="btn btn-sm btn-outline btn-light-secondary text-dark" @click="resetSearch">
        초기화
      </button>
      <!--end::초기화-->
      <!--begin::정렬 토글-->
      <button type="button" class="btn btn-sm btn-outline btn-light-primary px-3" title="정렬" @click="toggleSort">
        <i class="bi" :class="sort === 'asc' ? 'bi-sort-down-alt' : 'bi-sort-up'"></i>
      </button>
      <!--end::정렬 토글-->
      <!--begin::검색-->
      <button type="button" class="btn btn-sm btn-primary" @click="doSearch">
        검색
      </button>
      <!--end::검색-->
      <!--begin::구분선-->
      <div class="border-start border-gray-300 h-25px ms-1"></div>
      <!--end::구분선-->
      <!--begin::전체 복사-->
      <button type="button" class="btn btn-sm btn-outline btn-light-primary px-3" title="전체 복사" @click="copyAll">
        <i class="bi bi-copy"></i>
      </button>
      <!--end::전체 복사-->
      <!--begin::TXT 내보내기-->
      <button type="button" class="btn btn-sm btn-outline btn-light-primary px-3" title="TXT 내보내기" @click="exportTxt">
        <i class="fas fa-download"></i>
      </button>
      <!--end::TXT 내보내기-->
      <!--begin::키워드 배지 목록 (회색 — 텍스트 검색어)-->
      <div v-if="searchKeywords.length > 0" class="d-flex flex-wrap gap-2 ms-1">
        <span
          v-for="kw in searchKeywords"
          :key="kw"
          class="badge badge-light-secondary fw-lighter d-flex align-items-center gap-2 px-3 py-2 text-gray-700 cursor-pointer"
          @click="removeKeyword(kw)"
        >
          {{ kw }}
          <i class="bi bi-x"></i>
        </span>
      </div>
      <!--end::키워드 배지 목록-->
      <!--begin::태그 배지 목록 (파란색 — #태그명)-->
      <div v-if="tagIds.length > 0" class="d-flex flex-wrap gap-2 border-start border-gray-300 ps-3">
        <span
          v-for="tagId in tagIds"
          :key="tagId"
          class="badge badge-light-primary fw-lighter d-flex align-items-center gap-2 px-3 py-2 text-primary cursor-pointer"
          @click="removeTag(tagId)"
        >
          #{{ tagLabelMap[tagId] ?? tagId }}
          <i class="bi bi-x"></i>
        </span>
      </div>
      <!--end::태그 배지 목록-->
      <!--begin::결과 건수-->
      <span class="text-muted fs-7 ms-auto">{{ resultLabel }}</span>
      <!--end::결과 건수-->
    </div>
    <!--end::컨트롤 바-->

    <!--begin::고급 필터 아코디언-->
    <div v-show="showAdvanced" class="mb-4 px-4 py-3 bg-light rounded">
      <!--begin::유형 선택-->
      <div class="d-flex align-items-center gap-2 mb-3">
        <span class="fw-bold fs-7 text-gray-700 min-w-50px">유형</span>
        <div class="btn-group btn-group-sm">
          <button
            type="button"
            :class="['btn', type === 'DIARY' ? 'btn-primary' : 'btn-light']"
            @click="changeType('DIARY')"
          >일기</button>
          <button
            type="button"
            :class="['btn', type === 'DREAM' ? 'btn-info' : 'btn-light']"
            @click="changeType('DREAM')"
          >꿈</button>
        </div>
      </div>
      <!--end::유형 선택-->
      <!--begin::키워드 입력-->
      <div class="d-flex align-items-center gap-2">
        <span class="fw-bold fs-7 text-gray-700 min-w-50px">키워드</span>
        <input
          v-model="keywordInput"
          type="text"
          class="form-control form-control-sm journal-entry-search-input"
          placeholder="키워드를 입력하세요"
          maxlength="200"
          @keydown.enter.prevent="addKeyword"
        />
        <button type="button" class="btn btn-sm btn-light-primary w-100px" @click="addKeyword">
          + 추가
        </button>
      </div>
      <!--end::키워드 입력-->
    </div>
    <!--end::고급 필터 아코디언-->

    <!--begin::결과 목록-->
    <div v-if="loading" class="text-muted fs-7 py-10">검색 중...</div>
    <div v-else-if="entries.length === 0" class="text-muted fs-7 py-10">검색 결과가 없습니다.</div>
    <div v-else class="d-flex flex-column">
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
          class="d-flex align-items-center gap-2 text-gray-700 fw-bold fs-6 ps-2 pt-3 pb-1"
        >
          <span>{{ entry.stdrdDt }}</span>
          <span v-if="entry.stdrdDt" class="text-muted fs-7">({{ getWeekDayStr(entry.stdrdDt) }})</span>
          <button
            v-if="entry.stdrdDt"
            type="button"
            class="btn btn-xs btn-icon btn-light-primary"
            title="새 창으로 보기 (일자 뷰)"
            @click="openDailyView(entry.stdrdDt)"
          >
            <i class="bi bi-box-arrow-up-right fs-8"></i>
          </button>
        </div>
        <!--end::날짜 헤더-->
        <JournalEntryItem
          :dom-id="entry.id ? 'journal-entry-search-' + entry.id : undefined"
          :entry="entry"
          :is-dream="entry.contentType === 'JOURNAL_DREAM'"
        />
      </template>
    </div>
    <!--end::결과 목록-->

    <!--begin::모달 컨테이너-->
    <JournalEntryRegistModal @success="onEntrySaveSuccess" />
    <JournalInterpretationRegistModal />
    <CommentRegistModal />
    <CommentListModal />
    <HistoryModal @success="onHistorySuccess" />
    <RelatedContentAddModal />
    <JournalTagContextMenu />
    <JournalTagProfileModal />
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
import { nextTick, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import axios from "axios";
import Swal from "sweetalert2/dist/sweetalert2.js";
import { swalAlert } from "@/utils/swal";
import { useJournalStore } from "@/stores/journal";
import type { JournalEntryDto } from "@/stores/journal";
import { getWeekDayStr } from "@/utils/journalDate";
import { reinitMetronicAfterDom } from "@/utils/metronicReinit";
import JournalEntryItem from "./components/JournalEntryItem.vue";
import JournalEntryRegistModal from "./modals/JournalEntryRegistModal.vue";
import JournalInterpretationRegistModal from "../interpretation/modals/JournalInterpretationRegistModal.vue";
import CommentRegistModal from "../shared/modals/CommentRegistModal.vue";
import CommentListModal from "@/views/attachable/CommentListModal.vue";
import HistoryModal from "@/views/attachable/HistoryModal.vue";
import RelatedContentAddModal from "../shared/modals/RelatedContentAddModal.vue";
import JournalTagContextMenu from "../shared/components/JournalTagContextMenu.vue";
import JournalTagProfileModal from "../shared/modals/JournalTagProfileModal.vue";

interface JournalEntrySaveEvent {
  entryId?: number | string;
  stdrdDt?: string;
  isModify?: boolean;
}

interface SearchTagDto {
  id?: number | string;
  tagId?: number | string;
  name?: string;
}

const route = useRoute();
const router = useRouter();
const journalStore = useJournalStore();

const entries = ref<JournalEntryDto[]>([]);
const loading = ref(false);
const keywordInput = ref("");
const showAdvanced = ref(false);

const type = ref("DIARY");
const sort = ref("desc");
const tagIds = ref<string[]>([]);
const searchKeywords = ref<string[]>([]);

/** tagId 를 화면 표시명으로 바꾸기 위한 로컬 캐시. URL 검색 조건에는 tagIds 만 사용한다. */
const tagLabelMap = ref<Record<string, string>>({});

const resultLabel = ref("0건");

/** route query 를 파싱해 로컬 ref 에 반영한다. */
function syncFromRoute(): void {
  type.value = String(route.query.type ?? "DIARY").toUpperCase();
  sort.value = String(route.query.sort ?? "desc").toLowerCase() === "asc" ? "asc" : "desc";
  tagIds.value = normalizeQueryList(route.query.tagIds);
  searchKeywords.value = normalizeQueryList(route.query.searchKeywords);
  keywordInput.value = "";
}

watch(() => route.fullPath, () => {
  syncFromRoute();
  void loadEntries();
}, { immediate: true });

async function loadEntries(): Promise<void> {
  loading.value = true;
  try {
    const params = new URLSearchParams();
    params.set("type", type.value);
    params.set("sort", sort.value);
    tagIds.value.forEach((tagId) => params.append("tagIds", tagId));
    searchKeywords.value.forEach((kw) => params.append("searchKeywords", kw));

    const res = await axios.get("/api/journal/entries", { params });
    entries.value = res.data?.rsltList ?? [];
    hydrateTagNamesFromEntries(entries.value);
    void hydrateMissingTagNames();
    resultLabel.value = `${entries.value.length}건`;
  } catch {
    entries.value = [];
    resultLabel.value = "0건";
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

async function fetchEntryDetail(entryId: number | string): Promise<JournalEntryDto | null> {
  try {
    const res = await axios.get(`/api/journal/entry/${entryId}`);
    return res.data?.rsltObj ?? null;
  } catch {
    return null;
  }
}

function findEntryIndex(entryId: number | string): number {
  return entries.value.findIndex((entry) => String(entry.id) === String(entryId));
}

function hasStateList(entry: JournalEntryDto): boolean {
  return Array.isArray(entry.state?.list);
}

function mergeSearchEntryReplacement(updatedEntry: JournalEntryDto, currentEntry: JournalEntryDto): JournalEntryDto {
  return {
    ...updatedEntry,
    state: hasStateList(updatedEntry) ? updatedEntry.state : currentEntry.state,
    comment: updatedEntry.comment ?? currentEntry.comment,
  };
}

async function scrollToSearchEntry(entryId?: number | string): Promise<void> {
  await nextTick();
  window.requestAnimationFrame(() => {
    const el = entryId
      ? document.getElementById(`journal-entry-search-${entryId}`)
      : null;
    if (el) el.scrollIntoView({ behavior: "smooth", block: "center" });
  });
}

/** 현재 로컬 ref 상태를 URL query 로 replace한다. */
function pushQuery(overrides: Partial<{ type: string; sort: string; tagIds: string[]; searchKeywords: string[] }> = {}): void {
  const t = overrides.type ?? type.value;
  const s = overrides.sort ?? sort.value;
  const ids = overrides.tagIds ?? tagIds.value;
  const kws = overrides.searchKeywords ?? searchKeywords.value;
  const query: Record<string, string | string[]> = { type: t };
  if (s === "asc") query.sort = "asc";
  if (ids.length > 0) query.tagIds = ids;
  if (kws.length > 0) query.searchKeywords = kws;
  void router.replace({ name: "journal-entry-search", query });
}

/** 키워드 추가 (중복 무시) */
function addKeyword(): void {
  const kw = keywordInput.value.trim();
  if (!kw) return;
  if (searchKeywords.value.map((k) => k.toLowerCase()).includes(kw.toLowerCase())) return;
  const next = [...searchKeywords.value, kw];
  keywordInput.value = "";
  pushQuery({ searchKeywords: next });
}

/** 키워드 배지 X 클릭 → 제거 후 재검색 */
function removeKeyword(kw: string): void {
  pushQuery({ searchKeywords: searchKeywords.value.filter((k) => k !== kw) });
}

/** 태그 배지 X 클릭 → 캐시·URL 에서 제거 후 재검색 */
function removeTag(tagId: string): void {
  delete tagLabelMap.value[tagId];
  pushQuery({ tagIds: tagIds.value.filter((id) => id !== tagId) });
}

/** 검색 실행 (고급 필터 아코디언의 "검색" 버튼) */
function doSearch(): void {
  /* 현재 keywordInput 에 값이 있으면 키워드로 추가 */
  if (keywordInput.value.trim()) {
    addKeyword();
    return;
  }
  /* 이미 URL 과 일치하므로 강제 재조회만 수행 */
  void loadEntries();
}

/** 정렬 토글 */
function toggleSort(): void {
  pushQuery({ sort: sort.value === "desc" ? "asc" : "desc" });
}

/** 유형 변경 (키워드·태그 유지) */
function changeType(newType: string): void {
  pushQuery({ type: newType });
}

/** 초기화 */
function resetSearch(): void {
  tagLabelMap.value = {};
  void router.replace({ name: "journal-entry-search", query: { type: type.value } });
}

/** TXT 내보내기. 레거시 exportUrl = /api/journal/entries/export */
function exportTxt(): void {
  if (searchKeywords.value.length === 0 && tagIds.value.length === 0) {
    void swalAlert("검색 조건을 하나 이상 입력하세요.");
    return;
  }
  const params = new URLSearchParams();
  params.set("type", type.value);
  params.set("sort", sort.value);
  tagIds.value.forEach((id) => params.append("tagIds", id));
  searchKeywords.value.forEach((kw) => params.append("searchKeywords", kw));
  window.location.href = `/api/journal/entries/export?${params.toString()}`;
}

/** 일자 뷰를 새 창으로 연다. */
function openDailyView(stdrdDt: string | undefined): void {
  if (!stdrdDt) return;
  const base = import.meta.env.BASE_URL.replace(/\/$/, "");
  const w = Math.min(1600, window.screen.availWidth);
  const h = Math.min(1080, window.screen.availHeight);
  window.open(`${base}/journal/daily?stdrdDt=${stdrdDt}`, "_blank", `width=${w},height=${h}`);
}

async function onEntrySaveSuccess(payload?: JournalEntrySaveEvent): Promise<void> {
  const entryId = payload?.entryId;
  if (!entryId) {
    await loadEntries();
    return;
  }

  const entryIndex = findEntryIndex(entryId);
  if (entryIndex < 0) {
    await loadEntries();
    await scrollToSearchEntry(entryId);
    return;
  }

  const updatedEntry = await fetchEntryDetail(entryId);
  if (!updatedEntry) {
    await loadEntries();
    await scrollToSearchEntry(entryId);
    return;
  }

  entries.value.splice(entryIndex, 1, mergeSearchEntryReplacement(updatedEntry, entries.value[entryIndex]));
  await reinitMetronicAfterDom();
  await scrollToSearchEntry(entryId);
}

/** 이력 복원/삭제 성공 시 목록 갱신 */
function onHistorySuccess(): void {
  void loadEntries();
}

/**
 * 현재 검색 결과 전체를 클립보드에 복사.
 * 레거시 JournalEntrySearch.copy() 와 동일 포맷:
 *   날짜(요일)\n#순번\n본문 — 날짜가 바뀔 때만 날짜 헤더 삽입, 엔트리 간 빈 줄.
 */
async function copyAll(): Promise<void> {
  if (entries.value.length === 0) {
    void swalAlert("복사할 검색 결과가 없습니다.");
    return;
  }
  let prevDate: string | null = null;
  const blocks = entries.value.map((entry) => {
    const dt = entry.stdrdDt ?? "";
    const weekDay = getWeekDayStr(dt);
    const dateLabel = dt ? (weekDay ? `${dt} (${weekDay})` : dt) : "";
    const content = htmlToPlainText(entry.content ?? entry.markdownContent ?? "");
    let block = "";
    if (dateLabel !== prevDate) {
      block += `\r\n${dateLabel}\r\n`;
      prevDate = dateLabel;
    }
    block += [`#${entry.sortOrder ?? ""}`, content].join("\r\n");
    return block;
  });
  const text = blocks.join("\r\n\r\n").trim();
  try {
    await navigator.clipboard.writeText(text);
    void Swal.fire({ text: `현재 페이지 ${entries.value.length}건이 복사되었습니다.`, timer: 1500, showConfirmButton: false });
  } catch {
    void swalAlert("복사에 실패했습니다.");
  }
}

/** HTML 태그 제거 후 일반 텍스트로 변환 (줄바꿈 보존). 레거시 cF.util.htmlToText 와 동일 동작. */
function htmlToPlainText(html: string): string {
  return html
    .replace(/<\s*hr\b[^>]*\/?>/gi, "\n------\n")
    .replace(/<\s*br\s*\/?>/gi, "\n")
    .replace(/<\s*\/?p[^>]*>/gi, "\n")
    .replace(/<[^>]+>/g, "")
    .replace(/&nbsp;/g, " ")
    .replace(/&amp;/g, "&")
    .replace(/&lt;/g, "<")
    .replace(/&gt;/g, ">")
    .replace(/&quot;/g, '"')
    .split("\n").map((l) => l.trim()).join("\n")
    .replace(/\n+/g, "\n")
    .trim();
}

/** YYYY-MM-DD → "YYYY-MM" (월 변경 감지용) */
function getYyMm(stdrdDt?: string | null): string {
  return stdrdDt?.slice(0, 7) ?? "";
}

/** YYYY-MM-DD → "YYYY년 M월" 표시 레이블 */
function getYyMmLabel(stdrdDt?: string | null): string {
  if (!stdrdDt) return "";
  const [yy, mm] = stdrdDt.split("-");
  return `${yy}년 ${String(Number(mm))}월`;
}

function normalizeQueryList(value: unknown): string[] {
  const rawList = Array.isArray(value) ? value : [value];
  return rawList
    .flatMap((item) => String(item ?? "").split(","))
    .map((item) => item.trim())
    .filter(Boolean);
}

/* 인라인 상태/lifecycle 변경(JournalEntryItem → fetchDays) 완료 후 검색 목록 갱신 */
watch(() => journalStore.loading, (newVal, oldVal) => {
  if (!newVal && oldVal) void loadEntries();
});
</script>

<style scoped>
.journal-entry-search-page {
  padding: 1.5rem 2rem;
}

.journal-entry-search-input {
  max-width: 28rem;
}

.journal-entry-search-page :deep(.journal-interpretation-item) {
  margin-top: 0.35rem !important;
}
</style>
