<template>
  <!--begin::저널 일자 필터 모달 (메타/태그 다중 AND 검색)-->
  <div ref="modalEl" class="modal fade" id="journal_day_meta_modal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-xxl">
      <div class="modal-content">

        <!--begin::Modal Header-->
        <div class="modal-header">
          <h5 class="modal-title">
            {{ t("journal.day.filter.modal.title") }}
            <template v-for="(sm, idx) in selectedMetas" :key="'hd-meta-' + sm.metaId">
              <span class="text-primary">{{ sm.metaName }}</span>
              <span v-if="idx < selectedMetas.length - 1 || selectedTags.length > 0" class="text-muted mx-1">+</span>
            </template>
            <template v-for="(st, idx) in selectedTags" :key="'hd-tag-' + st.tagId">
              <span class="text-success">#{{ st.tagName }}</span>
              <span v-if="idx < selectedTags.length - 1" class="text-muted mx-1">+</span>
            </template>
            <span v-if="payload" class="text-muted fs-7 ms-2">-- {{ t("journal.day.filter.result-count").replace("{0}", String(dayCount)) }}</span>
          </h5>
          <button type="button" class="btn-close" @click="close"></button>
        </div>
        <!--end::Modal Header-->

        <!--begin::Modal Body-->
        <div class="modal-body modal-mbl-body my-5">
          <!--begin::로딩-->
          <div v-if="modalStore.filterModalLoading" class="d-flex justify-content-center py-10">
            <span class="spinner-border text-primary" role="status"></span>
          </div>
          <!--end::로딩-->

          <!--begin::필터 일자 목록-->
          <div v-else-if="payload" id="journal_day_meta_div">
            <div class="d-flex flex-column mb-4">

              <!--begin::컨트롤 행 (연도 선택 + 선택 필터 칩)-->
              <div class="d-flex align-items-center gap-4 mb-4 flex-wrap">
                <!--begin::연도 선택기-->
                <div class="d-flex align-items-center gap-2 flex-shrink-0">
                  <label for="journal_day_meta_yy" class="form-label mb-0 fw-bold">{{ t("common.year") }}</label>
                  <select
                    id="journal_day_meta_yy"
                    class="form-select form-select-sm w-auto"
                    :value="payload.yy"
                    @change="onYyChange"
                  >
                    <option
                      v-for="opt in payload.yearOptions"
                      :key="'yy-' + opt.value"
                      :value="String(opt.value)"
                    >{{ String(opt.value) === "" ? t("journal.day.filter.all-years") : opt.label }}</option>
                  </select>
                </div>
                <!--end::연도 선택기-->

                <!--begin::선택 필터 칩 목록-->
                <div class="d-flex align-items-center gap-2 flex-wrap">
                  <i class="bi bi-funnel-fill text-primary fs-6 flex-shrink-0"></i>
                  <!--begin::메타 칩-->
                  <span
                    v-for="sm in selectedMetas"
                    :key="'chip-meta-' + sm.metaId"
                    class="badge badge-light-primary rounded-pill d-inline-flex align-items-center gap-2 px-3 py-2 fs-7"
                  >
                    {{ sm.metaName }}
                    <i
                      class="bi bi-x-circle-fill cursor-pointer opacity-75"
                      :title="t('journal.day.filter.remove.tooltip').replace('{0}', sm.metaName)"
                      @click.stop="removeMeta(sm.metaId)"
                    ></i>
                  </span>
                  <!--end::메타 칩-->
                  <!--begin::태그 칩-->
                  <span
                    v-for="st in selectedTags"
                    :key="'chip-tag-' + st.tagId"
                    class="badge badge-light-success rounded-pill d-inline-flex align-items-center gap-2 px-3 py-2 fs-7"
                  >
                    #{{ st.tagName }}
                    <i
                      class="bi bi-x-circle-fill cursor-pointer opacity-75"
                      :title="t('journal.day.filter.remove.tooltip').replace('{0}', '#' + st.tagName)"
                      @click.stop="removeTag(st.tagId)"
                    ></i>
                  </span>
                  <!--end::태그 칩-->
                </div>
                <!--end::선택 필터 칩 목록-->

                <!--begin::태그 입력 검색 (모달 내 datalist 미표시 대응: 인라인 typeahead + 엔트리 검색과 동일 매칭)-->
                <div class="d-flex align-items-start gap-2 flex-shrink-0">
                  <span class="fw-bold fs-7 text-gray-700 pt-2">{{ t("common.tag") }}</span>
                  <div class="d-flex flex-column gap-1">
                    <div class="d-flex align-items-center gap-2">
                      <div class="position-relative">
                        <input
                          v-model="tagInput"
                          type="text"
                          class="form-control form-control-sm w-200px"
                          :placeholder="t('journal.entry.search.tag.placeholder')"
                          maxlength="100"
                          autocomplete="off"
                          @focus="onTagInputFocus"
                          @blur="onTagInputBlur"
                          @input="onTagInputChange"
                          @keydown.enter.prevent="addTagFromInput"
                          @keydown.escape.prevent="hideTagSuggestions"
                        />
                        <div
                          v-if="showTagSuggestions && tagSuggestions.length > 0"
                          class="dropdown-menu show position-absolute w-100 mt-1 py-1"
                          style="max-height: 220px; overflow-y: auto; z-index: 1060;"
                        >
                          <button
                            v-for="tagName in tagSuggestions"
                            :key="'tag-suggest-' + tagName"
                            type="button"
                            class="dropdown-item py-2 fs-7"
                            @mousedown.prevent="selectTagSuggestion(tagName)"
                          >
                            #{{ tagName }}
                          </button>
                        </div>
                      </div>
                      <button type="button" class="btn btn-sm btn-light-primary flex-shrink-0" @click="addTagFromInput">
                        + {{ t("common.add") }}
                      </button>
                    </div>
                    <span v-if="tagInputHint" class="text-danger fs-8">{{ tagInputHint }}</span>
                  </div>
                </div>
                <!--end::태그 입력 검색-->
              </div>
              <!--end::컨트롤 행-->

              <!--begin::동명 태그 카테고리 선택 행 (엔트리 검색과 동일 UX)-->
              <div v-if="tagCategoryChoices.length > 0" class="d-flex align-items-center gap-2 mb-4">
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
              <!--end::동명 태그 카테고리 선택 행-->

              <!--begin::일자 목록-->
              <template
                v-for="(day, index) in filteredList"
                :key="'filter-day-' + index + '-' + (day.stdrdDt ?? '')"
              >
                <!--begin::월 구분 헤더-->
                <div v-if="showMonthHeader(day, index)" class="d-flex-center mt-6 mb-4 fs-5 text-dark">
                  {{ t("journal.entry.search.year-month").replace("{0}", String(day.yy ?? "")).replace("{1}", String(day.mnth ?? "")) }}
                </div>
                <!--end::월 구분 헤더-->

                <!--begin::일자 행 (날짜 | 오른쪽 컨텐츠)-->
                <div class="d-flex align-items-start gap-2 mb-2">
                  <!--begin::날짜 영역 (고정, 세로 상단 정렬)-->
                  <div
                    class="d-flex align-items-center gap-1 flex-shrink-0 pt-1"
                    :class="{ 'text-danger': isJournalDayOff(day) }"
                  >
                    <i class="bi bi-calendar3 fs-6 me-1" :class="{ 'text-danger': isJournalDayOff(day) }"></i>
                    {{ day.stdrdDt }}
                    <span class="fs-8" :class="isJournalDayOff(day) ? 'text-danger' : 'text-gray-600'">({{ getWeekDayStr(day.stdrdDt, t) }})</span>
                    <JournalDayVacationIndicator
                      :status="day.vacationDayStatus"
                      :reason-list="day.vacationReasonList"
                      compact
                    />
                    <button
                      type="button"
                      class="btn btn-icon btn-sm btn-light-primary"
                      :title="t('journal.entry.search.open-daily.tooltip')"
                      @click="openDailyView(day.stdrdDt)"
                    ><i class="bi bi-box-arrow-up-right fs-8 p-0"></i></button>
                    <span class="fs-7 ms-1 text-muted" v-html="day.weather"></span>
                  </div>
                  <!--end::날짜 영역-->

                  <!--begin::오른쪽 컨텐츠 (메타 행 + 태그 행)-->
                  <div class="d-flex flex-column gap-1">

                    <!--begin::메타 행 (선택 메타 값 + 비선택 메타 칩)-->
                    <div class="d-flex align-items-center gap-3 flex-wrap">
                      <!--begin::선택된 메타 값-->
                      <div
                        v-for="(metaRow, mIdx) in selectedMetaRows(day)"
                        :key="'sm-' + index + '-' + mIdx"
                      >
                        <span v-if="metaRow.ctgr" class="text-noti pe-1">[{{ metaRow.ctgr }}]</span>
                        {{ metaRow.name }}
                        <span class="text-dialog">: {{ metaRow.value }}{{ metaRow.unit }}</span>
                      </div>
                      <!--end::선택된 메타 값-->

                      <!--begin::비선택 메타 (클릭 시 필터 추가)-->
                      <span
                        v-for="(other, oIdx) in otherMetaRows(day)"
                        :key="'om-' + index + '-' + oIdx"
                        class="badge badge-light rounded-pill cursor-pointer d-inline-flex align-items-center gap-1 px-2 py-1 fs-8"
                        :title="t('journal.day.filter.add.tooltip').replace('{0}', (other.ctgr ? '[' + other.ctgr + '] ' : '') + (other.name ?? ''))"
                        @click="addMeta(String(other.metaId ?? ''), other.name ?? '', other.ctgr)"
                      >
                        <span v-if="other.ctgr" class="text-noti">[{{ other.ctgr }}]</span>
                        {{ other.name }}
                        <span class="text-muted">{{ other.value }}{{ other.unit }}</span>
                        <i class="bi bi-plus-circle text-primary"></i>
                      </span>
                      <!--end::비선택 메타-->
                    </div>
                    <!--end::메타 행-->

                    <!--begin::태그 행-->
                    <div v-if="(day.tag?.list?.length ?? 0) > 0" class="d-flex flex-wrap gap-1">
                      <!--begin::선택된 태그 (굵게, 클릭 시 필터 제거)-->
                      <span
                        v-for="tag in selectedTagItems(day)"
                        :key="'st-tag-' + tag.tagId"
                        class="text-muted cursor-pointer"
                        :title="t('journal.day.filter.remove-filter.tooltip').replace('{0}', '#' + (tag.ctgr ? '[' + tag.ctgr + '] ' : '') + tag.name)"
                        @click.stop="removeTag(String(tag.tagId))"
                      >
                        #<span class="border-bottom text-success fw-bold opacity-hover">
                          <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>
                          {{ tag.name }}
                        </span>
                      </span>
                      <!--end::선택된 태그-->
                      <!--begin::비선택 태그 (클릭 시 필터 추가)-->
                      <span
                        v-for="tag in otherTagItems(day)"
                        :key="'ot-tag-' + tag.tagId"
                        class="text-muted cursor-pointer"
                        :title="t('journal.day.filter.add.tooltip').replace('{0}', '#' + (tag.ctgr ? '[' + tag.ctgr + '] ' : '') + tag.name)"
                        @click.stop="addTag(String(tag.tagId), tag.name, tag.ctgr)"
                      >
                        #<span class="border-bottom text-primary fw-lighter opacity-hover">
                          <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>
                          {{ tag.name }}
                        </span>
                      </span>
                      <!--end::비선택 태그-->
                    </div>
                    <!--end::태그 행-->

                  </div>
                  <!--end::오른쪽 컨텐츠-->
                </div>
                <!--end::일자 행-->

              </template>
              <!--end::일자 목록-->

            </div>
          </div>
          <!--end::필터 일자 목록-->

          <div v-else class="text-center text-muted py-10">
            {{ t("journal.day.meta.graph.empty") }}
          </div>
        </div>
        <!--end::Modal Body-->

        <!--begin::Modal Footer-->
        <div class="modal-footer">
          <button type="button" class="btn btn-sm btn-light" @click="close">{{ t("common.close") }}</button>
        </div>
        <!--end::Modal Footer-->

      </div>
    </div>
  </div>
  <!--end::저널 일자 필터 모달-->
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from "vue";
import { Modal } from "bootstrap";
import axios from "axios";
import { useJournalModalStore } from "@/features/journal/stores/journalModal";
import type { JournalDayDto, MetaContentItem, TagItem } from "@/features/journal/stores/journal";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { getWeekDayStr } from "@/features/journal/utils/journalDate";
import { joinAppBasePath } from "@/shared/utils/appPath";
import { isJournalDayOff } from "@/features/journal/utils/journalVacation";
import JournalDayVacationIndicator from "../components/JournalDayVacationIndicator.vue";

const modalStore = useJournalModalStore();
const { t } = useLocaleStore();

const modalEl = ref<HTMLElement | null>(null);
let bsModal: InstanceType<typeof Modal> | null = null;

const payload = computed(() => modalStore.filterModalPayload);

/** 선택된 메타 필터 */
interface SelectedMeta {
  metaId: string;
  metaName: string;
}
const selectedMetas = ref<SelectedMeta[]>([]);

/** 선택된 태그 필터 */
interface SelectedTag {
  tagId: string;
  tagName: string;
}
const selectedTags = ref<SelectedTag[]>([]);
const initializedSeedKey = ref<string | null>(null);

function makeSeedKey(p: NonNullable<typeof payload.value>): string {
  return `${p.seedType}:${p.seedId}`;
}

/**
 * payload 객체 참조가 바뀔 때마다(신규 오픈·연도 변경) 실행된다.
 * 같은 seed 는 최초 1회만 초기화하고, 이후 같은 seed 의 payload 재조회에서는
 * 사용자가 비워 둔 상태를 포함하여 기존 필터 상태를 그대로 유지한다.
 */
watch(
  () => payload.value,
  (p) => {
    if (!p) return;
    const seedKey = makeSeedKey(p);
    // 연도 변경 재조회 시에는 기존 필터(사용자가 제거하거나 추가한 메타·태그)를 보존한다.
    if (initializedSeedKey.value === seedKey) return;
    initializedSeedKey.value = seedKey;
    if (p.seedType === "meta") {
      /** ctgr 을 list 에서 탐색하여 displayName 구성 */
      let ctgr: string | undefined = p.seedCtgr;
      if (!ctgr) {
        for (const day of p.list ?? []) {
          const match = (day.meta?.list ?? []).find((r) => String(r.metaId ?? "") === p.seedId);
          if (match?.ctgr) { ctgr = match.ctgr; break; }
        }
      }
      const displayName = ctgr ? `[${ctgr}] ${p.seedName}` : p.seedName;
      selectedMetas.value = [{ metaId: p.seedId, metaName: displayName }];
      selectedTags.value = [];
    } else {
      const displayName = p.seedCtgr ? `[${p.seedCtgr}] ${p.seedName}` : p.seedName;
      selectedTags.value = [{ tagId: p.seedId, tagName: displayName }];
      selectedMetas.value = [];
    }
  },
  { immediate: true }
);

/**
 * 선택된 메타·태그를 모두 보유한 날짜만 필터링한다(AND 조건).
 * 선택 필터가 하나도 없으면 빈 목록을 반환한다.
 */
const filteredList = computed(() => {
  const list = payload.value?.list ?? [];
  const metaIds = selectedMetas.value.map((m) => m.metaId);
  const tagIds = selectedTags.value.map((t) => t.tagId);
  if (metaIds.length === 0 && tagIds.length === 0) return [];
  return list.filter((day) => {
    const dayMetaIds = (day.meta?.list ?? []).map((r) => String(r.metaId ?? ""));
    const dayTagIds = (day.tag?.list ?? []).map((t) => String(t.tagId));
    return metaIds.every((id) => dayMetaIds.includes(id))
        && tagIds.every((id) => dayTagIds.includes(id));
  });
});

/** 현재 필터링된 일자 수 */
const dayCount = computed(() => filteredList.value.length);

onMounted(() => {
  if (modalEl.value) {
    bsModal = new Modal(modalEl.value);
    modalEl.value.addEventListener("hidden.bs.modal", () => {
      modalStore.closeDayFilterModal();
      // 모달이 닫힐 때 필터 상태를 초기화한다.
      // 다음 오픈 시 payload watch 가 신규 시드로 재초기화할 수 있도록 비워 둔다.
      selectedMetas.value = [];
      selectedTags.value = [];
      initializedSeedKey.value = null;
      // 태그 입력 검색 상태도 함께 초기화한다. (카탈로그 캐시는 유지)
      tagInput.value = "";
      tagInputHint.value = "";
      hideTagSuggestions();
      cancelTagCategoryChoice();
    });
  }
});

watch(
  () => modalStore.filterModalOpen,
  (isOpen) => {
    if (isOpen) {
      bsModal?.show();
      void ensureTagSelectorData();
    } else {
      bsModal?.hide();
    }
  }
);

function close(): void {
  modalStore.closeDayFilterModal();
}

/** 연도 변경 시 해당 연도 데이터를 재조회한다. */
function onYyChange(event: Event): void {
  const target = event.target as HTMLSelectElement | null;
  if (!target || !payload.value) return;
  void modalStore.openDayFilterModal(
    { type: payload.value.seedType, id: payload.value.seedId, name: payload.value.seedName, ctgr: payload.value.seedCtgr },
    target.value
  );
}

/** 선택 메타 필터에 항목을 추가한다. 이미 선택된 metaId면 무시한다. */
function addMeta(metaId: string, metaName: string, ctgr?: string): void {
  if (!selectedMetas.value.some((m) => m.metaId === metaId)) {
    const displayName = ctgr ? `[${ctgr}] ${metaName}` : metaName;
    selectedMetas.value = [...selectedMetas.value, { metaId, metaName: displayName }];
  }
}

/** 선택 메타 필터에서 항목을 제거한다. */
function removeMeta(metaId: string): void {
  selectedMetas.value = selectedMetas.value.filter((m) => m.metaId !== metaId);
}

/** 선택 태그 필터에 항목을 추가한다. 이미 선택된 tagId면 무시한다. */
function addTag(tagId: string, tagName: string, ctgr?: string): void {
  if (!selectedTags.value.some((t) => t.tagId === tagId)) {
    const displayName = ctgr ? `[${ctgr}] ${tagName}` : tagName;
    selectedTags.value = [...selectedTags.value, { tagId, tagName: displayName }];
  }
}

/** 선택 태그 필터에서 항목을 제거한다. */
function removeTag(tagId: string): void {
  selectedTags.value = selectedTags.value.filter((t) => t.tagId !== tagId);
}

// ---- 태그 입력 검색 (JournalEntrySearchPage 의 태그 입력 패턴 이식; 모달에서는 datalist 대신 typeahead) ----

/** 태그 카탈로그 항목 (/api/journal/day/tags 응답) */
interface DayTagCatalogItem {
  tagId: string;
  name: string;
  ctgr: string;
}

const TAG_SUGGESTION_LIMIT = 12;

const tagInput = ref("");
const tagCatalog = ref<DayTagCatalogItem[]>([]);
const tagCategoryMap = ref<Record<string, string[]>>({});
const tagSelectorLoaded = ref(false);
const pendingTagName = ref("");
const tagCategoryChoices = ref<string[]>([]);
const tagInputHint = ref("");
const showTagSuggestions = ref(false);

/** typeahead 자동완성용 태그명 목록 (categoryMap 키 기준·정렬) */
const tagNameOptions = computed(() => Object.keys(tagCategoryMap.value).sort((a, b) => a.localeCompare(b)));

/** 입력값 기준 부분 일치 태그명 후보 (모달 내 미리보기) */
const tagSuggestions = computed(() => {
  const query = normalizeTagName(tagInput.value.replace(/^#/, ""));
  const options = tagNameOptions.value;
  if (!query) return options.slice(0, TAG_SUGGESTION_LIMIT);
  const lower = query.toLowerCase();
  return options
    .filter((name) => name.toLowerCase().includes(lower))
    .slice(0, TAG_SUGGESTION_LIMIT);
});

function normalizeCategoryMap(raw: unknown): Record<string, string[]> {
  if (!raw || typeof raw !== "object" || Array.isArray(raw)) return {};
  const out: Record<string, string[]> = {};
  for (const [tagName, categories] of Object.entries(raw as Record<string, unknown>)) {
    if (!Array.isArray(categories)) continue;
    out[tagName] = categories.map((c) => String(c ?? "")).filter((c) => c.length > 0);
  }
  return out;
}

function mergeCatalogIntoCategoryMap(
  baseMap: Record<string, string[]>,
  catalog: DayTagCatalogItem[],
): Record<string, string[]> {
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

function normalizeTagName(raw: string): string {
  return raw.trim().replace(/\s+/g, "_");
}

function findKnownTagName(input: string): string {
  const normalized = normalizeTagName(input);
  if (tagCategoryMap.value[normalized]) return normalized;
  return tagNameOptions.value.find((name) => name.toLowerCase() === normalized.toLowerCase()) ?? normalized;
}

function flattenTagCatalog(rawList: unknown): DayTagCatalogItem[] {
  if (!Array.isArray(rawList)) return [];
  const catalog: DayTagCatalogItem[] = [];
  for (const raw of rawList) {
    const item = raw as Record<string, unknown>;
    const tagId = String(item.id ?? item.tagId ?? "");
    const name = String(item.name ?? "").trim();
    if (!tagId || !name) continue;
    catalog.push({ tagId, name, ctgr: String(item.ctgr ?? "") });
  }
  return catalog;
}

/**
 * 일자 태그 categoryMap·태그 목록을 최초 1회 로드한다.
 * journalModalStore 의 dayTagCategoryMap(SSOT)과 /api/journal/day/tags 를 병합한다.
 */
async function ensureTagSelectorData(): Promise<void> {
  if (tagSelectorLoaded.value) return;
  try {
    await modalStore.preloadAllCategoryMaps();
    const res = await axios.get("/api/journal/day/tags");
    if (!res.data?.rslt) {
      console.warn("[JournalDayMetaModal] tag list rslt=false");
      return;
    }
    const catalog = flattenTagCatalog(res.data?.rsltList ?? []);
    tagCatalog.value = catalog;
    tagCategoryMap.value = mergeCatalogIntoCategoryMap(
      { ...modalStore.dayTagCategoryMap },
      catalog,
    );
    tagSelectorLoaded.value = true;
  } catch (e: unknown) {
    console.warn("[JournalDayMetaModal] tag selector data load failed.", e);
  }
}

function onTagInputFocus(): void {
  tagInputHint.value = "";
  void ensureTagSelectorData().then(() => {
    showTagSuggestions.value = true;
  });
}

function onTagInputBlur(): void {
  window.setTimeout(() => {
    showTagSuggestions.value = false;
  }, 150);
}

function onTagInputChange(): void {
  tagInputHint.value = "";
  showTagSuggestions.value = true;
}

function hideTagSuggestions(): void {
  showTagSuggestions.value = false;
}

function selectTagSuggestion(tagName: string): void {
  tagInput.value = tagName;
  tagInputHint.value = "";
  hideTagSuggestions();
  void addTagFromInput();
}

/**
 * 입력한 태그명을 categoryMap·카탈로그에서 찾아 AND 필터에 추가한다.
 * 카탈로그에 없으면 인라인 안내(모달 유지), 동명 태그는 카테고리 선택으로 분기한다.
 */
async function addTagFromInput(): Promise<void> {
  await ensureTagSelectorData();
  tagInputHint.value = "";
  const tagName = findKnownTagName(tagInput.value);
  const categories = tagCategoryMap.value[tagName] ?? [];
  if (!tagName || categories.length === 0) {
    tagInputHint.value = t("journal.entry.search.tag.select-existing");
    return;
  }
  if (categories.length === 1) {
    addTagByNameAndCategory(tagName, categories[0]);
    return;
  }
  pendingTagName.value = tagName;
  tagCategoryChoices.value = categories;
}

/** 동명 태그 카테고리 선택 버튼 클릭 처리 */
function selectTagCategory(ctgr: string): void {
  addTagByNameAndCategory(pendingTagName.value, ctgr);
}

/** 동명 태그 카테고리 선택을 취소한다. */
function cancelTagCategoryChoice(): void {
  pendingTagName.value = "";
  tagCategoryChoices.value = [];
}

/** 태그명+카테고리로 카탈로그에서 tagId 를 찾아 기존 addTag 필터 흐름에 넘긴다. */
function addTagByNameAndCategory(tagName: string, ctgr: string): void {
  const matched = tagCatalog.value.find((item) => item.name === tagName && item.ctgr === ctgr);
  if (!matched) {
    console.warn("[JournalDayMetaModal] selected tag id not found.", { tagName, ctgr });
    tagInputHint.value = t("journal.entry.search.tag.not-found");
    return;
  }
  tagInput.value = "";
  tagInputHint.value = "";
  cancelTagCategoryChoice();
  addTag(matched.tagId, matched.name, matched.ctgr || undefined);
}

/** 월 구분 헤더를 표시할 경계인지 확인한다. */
function showMonthHeader(day: JournalDayDto, index: number): boolean {
  if (index === 0) return true;
  const prev = filteredList.value[index - 1];
  return prev?.mnth !== day.mnth || prev?.yy !== day.yy;
}

/**
 * 해당 일자에서 선택된 메타 ID와 일치하는 메타 행을 반환한다.
 * selectedMetas 배열 순서를 유지하여 행마다 표시 순서가 일관되도록 한다.
 */
function selectedMetaRows(day: JournalDayDto): MetaContentItem[] {
  const metaList = day.meta?.list ?? [];
  return selectedMetas.value
    .map((m) => metaList.find((r) => String(r.metaId ?? "") === m.metaId))
    .filter((r): r is MetaContentItem => r !== undefined);
}

/** 해당 일자에서 선택되지 않은 나머지 메타 행을 반환한다. */
function otherMetaRows(day: JournalDayDto): MetaContentItem[] {
  const ids = selectedMetas.value.map((m) => m.metaId);
  return (day.meta?.list ?? []).filter((r) => !ids.includes(String(r.metaId ?? "")));
}

/** 해당 일자에서 선택된 태그 ID와 일치하는 태그를 반환한다. */
function selectedTagItems(day: JournalDayDto): TagItem[] {
  const ids = selectedTags.value.map((t) => t.tagId);
  return (day.tag?.list ?? []).filter((t) => ids.includes(String(t.tagId)));
}

/** 해당 일자에서 선택되지 않은 나머지 태그를 반환한다. */
function otherTagItems(day: JournalDayDto): TagItem[] {
  const ids = selectedTags.value.map((t) => t.tagId);
  return (day.tag?.list ?? []).filter((t) => !ids.includes(String(t.tagId)));
}

/** 일자 뷰를 새 창으로 연다. */
function openDailyView(stdrdDt: string | undefined): void {
  if (!stdrdDt) return;
  const w = Math.min(1600, window.screen.availWidth);
  const h = Math.min(1080, window.screen.availHeight);
  window.open(joinAppBasePath(`/journal/daily?stdrdDt=${stdrdDt}`), "_blank", `width=${w},height=${h}`);
}
</script>
