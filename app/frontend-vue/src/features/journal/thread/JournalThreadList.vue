<template>
  <!--begin::저널 스레드 목록-->
  <div class="journal-thread-list-vue">

    <!--begin::검색 카드 — 등록은 JournalThreadViewToolbar. 카드는 툴바에 붙인다(margin-top: 0).-->
    <div class="card mb-4" style="margin-top: 0 !important;">
      <div class="card-body px-4 py-3">
        <!--begin::검색 폼-->
        <form class="d-flex flex-wrap align-items-center gap-2" @submit.prevent="search">
          <select
            v-model="store.filterCategory"
            class="form-select form-select-sm form-select-solid w-auto flex-shrink-0"
            @change="search"
          >
            <option value="">{{ t("journal.thread.filter.all-categories") }}</option>
            <option v-for="category in store.categoryOptions" :key="category.code" :value="category.code">
              {{ category.codeName }}
            </option>
          </select>
          <input
            v-model.trim="store.filterKeyword"
            type="search"
            class="form-control form-control-sm form-control-solid flex-grow-1"
            style="min-width:200px;"
            :placeholder="t('journal.thread.filter.keyword.placeholder')"
          />
          <button type="submit" class="btn btn-sm btn-light-primary">{{ t("common.search") }}</button>
          <button type="button" class="btn btn-sm btn-light" @click="resetFilters">{{ t("common.reset") }}</button>
        </form>
        <!--begin::태그 필터 (엔트리 검색·일자 필터와 동형 — 멀티 AND, 배지에 [ctgr])-->
        <div class="d-flex flex-wrap align-items-center gap-2 mt-3">
          <span class="fw-bold fs-7 text-gray-700">{{ t("common.tag") }}</span>
          <input
            v-model="tagInput"
            type="text"
            class="form-control form-control-sm form-control-solid"
            style="min-width:160px; max-width:240px;"
            :placeholder="t('journal.thread.filter.tag.placeholder')"
            :title="tagInputTitle"
            maxlength="100"
            list="journal-thread-list-tag-options"
            autocomplete="off"
            :disabled="isTagCategoryChoicePending"
            @focus="ensureTagSelectorData()"
            @keydown.enter.prevent="addTagFromInput"
          />
          <datalist id="journal-thread-list-tag-options">
            <option
              v-for="tagName in tagNameOptions"
              :key="tagName"
              :value="tagName"
            />
          </datalist>
          <button
            type="button"
            class="btn btn-sm btn-light-primary"
            :disabled="isTagCategoryChoicePending"
            @click="addTagFromInput"
          >
            + {{ t("common.add") }}
          </button>
          <div v-if="store.filterTagIds.length > 0" class="d-flex flex-wrap gap-2">
            <span
              v-for="tagId in store.filterTagIds"
              :key="tagId"
              class="badge badge-light-primary fw-lighter d-flex align-items-center gap-2 px-3 py-2 text-primary cursor-pointer"
              :title="t('journal.entry.search.tag.remove.tooltip')"
              @click="removeTag(tagId)"
            >
              <!-- 카테고리·이름 모두 fs-7. 카테고리만 text-noti.
                   줄높이 차이로 어긋나지 않게 인라인 flex 세로 가운데 정렬. -->
              <span class="d-inline-flex align-items-center lh-1">
                <span class="fs-7">#</span><span
                  v-if="store.filterTagCtgrMap[tagId]"
                  class="fs-7 text-noti"
                >[{{ store.filterTagCtgrMap[tagId] }}]</span><span class="fs-7">{{ store.filterTagLabelMap[tagId] ?? tagId }}</span>
              </span>
              <i class="bi bi-x"></i>
            </span>
          </div>
        </div>
        <div class="text-muted fs-8 mt-1">{{ tagInputHint }}</div>
        <div v-if="tagCategoryChoices.length > 0" class="d-flex align-items-center gap-2 mt-2">
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
        <!--end::태그 필터-->
        <div v-if="store.categoryError" class="text-danger fs-8 mt-2">{{ store.categoryError }}</div>
        <!--end::검색 폼-->
      </div>
    </div>
    <!--end::검색 카드-->

    <div class="card post">
      <div class="card-body">

        <!--begin::로딩-->
        <div v-if="store.loading" class="d-flex justify-content-center py-10">
          <span class="spinner-border text-primary" role="status"></span>
        </div>
        <!--end::로딩-->

        <table v-else class="table align-middle table-row-dashed fs-small gy-5 table-fixed hoverTable mb-3">
          <thead>
            <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
              <th class="text-center wb-keepall w-10 hidden-table">{{ t("common.number") }}</th>
              <th class="col-lg-9 col-9 text-center wb-keepall">{{ t("common.title") }}</th>
              <th class="col-lg-1 text-center wb-keepall hidden-table">{{ t("common.attachment") }}</th>
              <th class="col-lg-1 text-center wb-keepall hidden-table">{{ t("common.manage") }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!store.threadList.length">
              <td colspan="4" class="text-center text-muted py-6 fs-7">{{ t("journal.thread.empty") }}</td>
            </tr>
            <tr
              v-for="thread in store.threadList"
              :key="thread.id"
              class="cursor-pointer"
              @click="onThreadRowClick($event, thread.id!)"
            >
              <td class="text-center text-gray-500 fs-7 hidden-table">{{ thread.rnum }}</td>
              <td class="ps-3">
                <span v-if="thread.categoryName" class="badge badge-light-primary me-2 fs-9">{{ thread.categoryName }}</span>
                <span class="fs-6">{{ thread.title }}</span>
                <span
                  v-if="membershipCountOf(thread) > 0"
                  class="text-muted fs-9 ms-2"
                >{{ formatMembershipCount(membershipCountOf(thread)) }}</span>
                <span
                  v-if="membershipPeriodOf(thread)"
                  class="text-muted fs-9 ms-2"
                >{{ membershipPeriodOf(thread) }}</span>
                <button
                  v-if="thread.comment && thread.comment.cnt"
                  type="button"
                  class="btn btn-link text-muted ms-2 fs-8 p-0 align-baseline"
                  :title="t('journal.thread.comments.tooltip')"
                  @click.stop="openCommentList(thread)"
                >[{{ thread.comment.cnt }}]</button>
                <!--begin::태그-->
                <div v-if="hasThreadTags(thread)" class="mt-1">
                  <i class="bi bi-tag fs-8 me-1"></i>
                  <span
                    v-for="tag in thread.tag?.list"
                    :key="String(tag.tagId)"
                    class="text-muted fs-9 pe-1"
                  >
                    #{{ tag.name }}
                  </span>
                </div>
                <!--end::태그-->
              </td>
              <td class="text-center hidden-table">
                <i v-if="thread.hasFiles" class="bi bi-paperclip text-muted"></i>
              </td>
              <td class="text-center">
                <!--begin::컨텍스트 메뉴
                  SSOT: 저널 일자·게시판 목록과 동일 Metronic data-kt-menu.
                  트리거에 @click.stop 을 두면 body 위임 클릭이 막혀 메뉴가 열리지 않는다.
                  행 상세 이동은 isMetronicMenuEventTarget 가드로 막는다.
                  비동기 목록 렌더 후 reinitMetronicAfterDom() 으로 재바인딩한다.
                -->
                <div class="d-flex justify-content-center">
                  <button
                    type="button"
                    class="btn btn-sm btn-icon btn-bg-light btn-active-color-primary"
                    data-kt-menu-trigger="click"
                    data-kt-menu-placement="bottom-end"
                    :title="t('common.menu')"
                  >
                    <i class="ki-solid ki-dots-horizontal fs-2x"></i>
                  </button>
                  <div
                    class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold w-200px py-3"
                    data-kt-menu="true"
                    @click.stop
                  >
                    <div class="menu-item px-3 my-1">
                      <div class="menu-link flex-stack px-3" @click="openModify(thread.id!)">
                        {{ t("common.edit") }}
                        <i class="bi bi-pencil-square fs-8"></i>
                      </div>
                    </div>
                    <div class="separator my-2"></div>
                    <div class="menu-item px-3 my-1">
                      <div class="menu-link flex-stack px-3 text-danger" @click="store.deleteThread(thread.id!)">
                        {{ t("common.delete") }}
                        <i class="bi bi-trash text-danger p-0 fs-8"></i>
                      </div>
                    </div>
                  </div>
                </div>
                <!--end::컨텍스트 메뉴-->
              </td>
            </tr>
          </tbody>
        </table>

      </div>
      <!--begin::페이지네이션-->
      <div v-if="store.totalPages > 1" class="card-footer">
        <div class="d-flex justify-content-center gap-1">
          <button
            v-for="p in store.totalPages"
            :key="p"
            type="button"
            class="btn btn-sm"
            :class="(p - 1) === store.currentPage ? 'btn-primary' : 'btn-light'"
            @click="store.fetchList(p - 1)"
          >
            {{ p }}
          </button>
        </div>
      </div>
      <!--end::페이지네이션-->
    </div>

  </div>
  <!--end::저널 스레드 목록-->
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
import { useAttachableModalStore } from "@/features/attachable/stores/attachableModal";
import type { JournalThreadDto } from "@/features/journal/stores/journalThread";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalAlert } from "@/shared/utils/swal";
import { isMetronicMenuEventTarget, reinitMetronicAfterDom } from "@/shared/utils/metronicReinit";

/** 태그 후보 API 응답 항목 */
interface SearchTagDto {
  id?: number | string;
  tagId?: number | string;
  name?: string;
  ctgr?: string;
}

const store = useJournalThreadStore();
const attachableStore = useAttachableModalStore();
const { t } = useLocaleStore();
const router = useRouter();

/**
 * 목록 렌더가 끝나면 Metronic 컨텍스트 메뉴를 재바인딩한다.
 * 행 액션이 `data-kt-menu` 드롭다운이라, 비동기로 교체된 DOM 에는 핸들러가 붙어 있지 않다.
 */
watch(
  () => store.loading,
  (loading, wasLoading) => {
    if (wasLoading && !loading) void reinitMetronicAfterDom();
  }
);


const tagInput = ref("");
const tagCategoryMap = ref<Record<string, string[]>>({});
const tagCatalog = ref<SearchTagDto[]>([]);
const tagSelectorLoaded = ref(false);
const tagCategoryChoices = ref<string[]>([]);
const pendingTagName = ref("");

const isTagCategoryChoicePending = computed(() => tagCategoryChoices.value.length > 0);
const tagNameOptions = computed(() => Object.keys(tagCategoryMap.value).sort((a, b) => a.localeCompare(b)));
const tagInputHint = computed(() => (isTagCategoryChoicePending.value
  ? t("journal.entry.search.tag.category.pending")
  : t("journal.thread.filter.tag.hint")));
const tagInputTitle = computed(() => (isTagCategoryChoicePending.value
  ? t("journal.entry.search.tag.category.pending")
  : t("journal.thread.filter.tag.placeholder")));

onMounted(() => {
  void store.fetchList(0);
});

function search(): void {
  void store.fetchList(0);
}

function resetFilters(): void {
  tagInput.value = "";
  cancelTagCategoryChoice();
  void store.resetFilters();
}

function onThreadRowClick(event: MouseEvent, id: number): void {
  if (isMetronicMenuEventTarget(event.target)) return;
  void router.push({ name: "thread-detail", params: { id } });
}

function openModify(id: number): void {
  void router.push({ name: "thread-edit", params: { id } });
}

/** 스레드 태그 보유 여부 */
function hasThreadTags(thread: JournalThreadDto): boolean {
  return Array.isArray(thread.tag?.list) && thread.tag!.list!.length > 0;
}

/** 활성 소속 엔트리 수. 없거나 비정상이면 0. */
function membershipCountOf(thread: JournalThreadDto): number {
  const count = thread.membershipCount;
  return typeof count === "number" && Number.isFinite(count) && count > 0 ? count : 0;
}

/** 기간 요약과 동일한 `{n}건` 포맷. */
function formatMembershipCount(count: number): string {
  return t("journal.thread.period-summary.entry-count").replace("{0}", String(count));
}

/**
 * 소속 엔트리 기준일 기간 라벨.
 * 같은 날이면 단일 일자, 범위면 `{0} ~ {1}`. 유효 일자가 없으면 빈 문자열(숨김).
 */
function membershipPeriodOf(thread: JournalThreadDto): string {
  const first = normalizeThreadEntryDate(thread.firstEntryDate);
  const last = normalizeThreadEntryDate(thread.lastEntryDate);
  if (!first || !last) return "";
  if (first === last) return first;
  return t("journal.thread.list.membership-period")
    .replace("{0}", first)
    .replace("{1}", last);
}

/** 목록 API 기준일을 YYYY-MM-DD 로 정규화한다. */
function normalizeThreadEntryDate(value?: string | null): string {
  if (!value) return "";
  const trimmed = value.trim();
  return trimmed.length >= 10 ? trimmed.slice(0, 10) : trimmed;
}

function openCommentList(thread: JournalThreadDto): void {
  const contentType = thread.contentType ?? "JOURNAL_THREAD";
  if (!thread.id || !contentType) return;
  void attachableStore.openCommentList(thread.id, contentType);
}

/**
 * 일기·꿈 태그 카탈로그를 합쳐 후보로 쓴다.
 * 스레드 소속 엔트리는 유형이 섞이므로 검색 팝업의 단일 type 과 달리 합집합이 필요하다.
 */
async function ensureTagSelectorData(): Promise<void> {
  if (tagSelectorLoaded.value) return;
  try {
    const types = ["DIARY", "DREAM"] as const;
    const responses = await Promise.all(types.flatMap((type) => [
      axios.get("/api/journal/entry/tag/categories", { params: { type } }),
      axios.get("/api/journal/entry/tags", { params: { type } }),
    ]));
    const nextMap: Record<string, string[]> = {};
    const nextCatalog: SearchTagDto[] = [];
    for (let i = 0; i < types.length; i++) {
      const categoryRes = responses[i * 2];
      const tagRes = responses[i * 2 + 1];
      const catalog = (tagRes.data?.rsltList ?? []) as SearchTagDto[];
      nextCatalog.push(...catalog);
      const merged = mergeCatalogIntoCategoryMap(
        normalizeCategoryMap(categoryRes.data?.rsltMap ?? categoryRes.data?.rsltObj),
        catalog,
      );
      for (const [name, categories] of Object.entries(merged)) {
        const existing = nextMap[name] ? [...nextMap[name]] : [];
        for (const ctgr of categories) {
          if (!existing.includes(ctgr)) existing.push(ctgr);
        }
        nextMap[name] = existing;
      }
    }
    tagCatalog.value = nextCatalog;
    tagCategoryMap.value = nextMap;
    nextCatalog.forEach((tag) => store.cacheFilterTagLabel(tag.id ?? tag.tagId, tag.name));
    tagSelectorLoaded.value = true;
  } catch (e: unknown) {
    console.warn("[JournalThreadList] tag selector data load failed.", e);
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

async function addTagFromInput(): Promise<void> {
  await ensureTagSelectorData();
  const tagName = findKnownTagName(tagInput.value);
  const categories = tagCategoryMap.value[tagName] ?? [];
  if (!tagName || categories.length === 0) {
    void swalAlert(t("journal.entry.search.tag.select-existing"));
    return;
  }
  if (categories.length === 1) {
    await addTagByNameAndCategory(tagName, categories[0]);
    return;
  }
  pendingTagName.value = tagName;
  tagCategoryChoices.value = categories;
}

function selectTagCategory(ctgr: string): void {
  void addTagByNameAndCategory(pendingTagName.value, ctgr);
}

function cancelTagCategoryChoice(): void {
  pendingTagName.value = "";
  tagCategoryChoices.value = [];
}

async function addTagByNameAndCategory(tagName: string, ctgr: string): Promise<void> {
  const matchedTag = tagCatalog.value.find((tag) =>
    String(tag.name ?? "") === tagName && String(tag.ctgr ?? "") === ctgr,
  );
  const tagId = matchedTag?.id ?? matchedTag?.tagId;
  if (tagId === undefined || tagId === null) {
    console.warn("[JournalThreadList] selected tag id not found.", { tagName, ctgr });
    void swalAlert(t("journal.entry.search.tag.not-found"));
    return;
  }
  tagInput.value = "";
  cancelTagCategoryChoice();
  // 일자 필터·엔트리 태그와 동일하게 카테고리를 함께 캐시해 배지에 `[ctgr]` 를 표시한다.
  const added = store.addFilterTag(tagId, tagName, ctgr || undefined);
  if (!added) {
    void swalAlert(t("journal.entry.search.tag.duplicate"));
    return;
  }
  await store.fetchList(0);
}

function removeTag(tagId: string): void {
  store.removeFilterTag(tagId);
  void store.fetchList(0);
}
</script>
