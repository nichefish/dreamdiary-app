<template>
  <Teleport to="body">
    <div
      v-if="store.visible"
      ref="menuEl"
      class="journal-tag-context-menu"
      :style="{ left: store.x + 'px', top: store.y + 'px' }"
      @click.stop
    >
      <button type="button" class="journal-tag-ctx-btn journal-tag-ctx-btn--search" @click="onSearch">
        <i class="bi bi-search"></i>
        <span>{{ t("common.search") }}</span>
      </button>
      <button
        v-if="isInSearchPopup"
        type="button"
        class="journal-tag-ctx-btn journal-tag-ctx-btn--search-add"
        @click="onSearchAdd"
      >
        <i class="bi bi-plus-lg"></i>
        <span>{{ t("journal.tag.search-add") }}</span>
      </button>
      <button type="button" class="journal-tag-ctx-btn journal-tag-ctx-btn--configure" @click="onConfigure">
        <i class="bi bi-sliders2"></i>
        <span>{{ t("journal.tag.settings") }}</span>
      </button>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import axios from "axios";
import { useTagContextMenuStore, type TagContextMenuPayload } from "@/features/journal/stores/tagContextMenu";
import { useJournalModalStore } from "@/features/journal/stores/journalModal";
import { useAttachableModalStore } from "@/features/attachable/stores/attachableModal";
import { assertAuthenticatedBeforePopup } from "@/shared/auth/popupAuth";
import { joinAppBasePath } from "@/shared/utils/appPath";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const store = useTagContextMenuStore();
const journalModalStore = useJournalModalStore();
const attachableStore = useAttachableModalStore();
const { t } = useLocaleStore();
const route = useRoute();
const router = useRouter();

const menuEl = ref<HTMLElement | null>(null);

/** 엔트리 검색 팝업 안인지 여부 — "검색에 추가"(다중) 버튼 노출 조건. 밖에서는 단일 "검색"이 곧 새 검색이라 노출하지 않는다. */
const isInSearchPopup = computed(() => route.name === "journal-entry-search");

/** 검색(단일): 검색 팝업 안이면 tagIds 를 이 태그 하나로 교체, 밖이면 이 태그로 새 팝업을 연다. */
async function onSearch(): Promise<void> {
  const payload = { ...store.payload };
  store.close();

  if (payload.contentType === "JOURNAL_DAY") {
    void journalModalStore.openDayFilterModal({ type: "tag", id: payload.tagId, name: payload.name, ctgr: payload.ctgr });
    return;
  }

  await openEntrySearchPopup(payload, "single");
}

/** 검색에 추가(다중): 검색 팝업 안에서 기존 tagIds 에 이 태그를 더한다(AND, 중복 무시). */
async function onSearchAdd(): Promise<void> {
  const payload = { ...store.payload };
  store.close();
  await openEntrySearchPopup(payload, "add");
}

async function openEntrySearchPopup(
  payload: TagContextMenuPayload,
  mode: "single" | "add",
): Promise<void> {
  const newType = payload.contentType === "JOURNAL_DREAM" ? "DREAM" : "DIARY";
  const newTagId = String(payload.tagId);

  if (route.name === "journal-entry-search") {
    /*
     * 팝업 내부: mode 에 따라 tagIds 를 구성한다. type·sort·키워드 등 다른 검색 축은 유지한다.
     * - "single": tagIds 를 이 태그 하나로 교체(새 단일태그 검색).
     * - "add": 기존 tagIds 에 이 태그를 더한다(AND). 이미 있으면 무시.
     */
    const existingIds = normalizeList(route.query.tagIds);
    let tagIds: string[];
    if (mode === "add") {
      if (existingIds.includes(newTagId)) return;
      tagIds = [...existingIds, newTagId];
    } else {
      if (existingIds.length === 1 && existingIds[0] === newTagId) return;
      tagIds = [newTagId];
    }
    const nextQuery: Record<string, string | string[]> = {
      type: String(route.query.type ?? newType).toUpperCase(),
    };
    const sortVal = String(route.query.sort ?? "").toLowerCase();
    if (sortVal === "asc") nextQuery.sort = "asc";
    const searchKeywords = normalizeList(route.query.searchKeywords);
    if (searchKeywords.length > 0) nextQuery.searchKeywords = searchKeywords;
    void router.replace({
      name: "journal-entry-search",
      query: {
        ...nextQuery,
        tagIds,
      },
    });
    return;
  }

  if (!await assertAuthenticatedBeforePopup(router, route)) return;

  /* 외부: 새 팝업 창 열기 */
  const params = new URLSearchParams({ type: newType, tagIds: newTagId });
  const url = joinAppBasePath(`/journal/entry/search?${params.toString()}`);
  const popupName = payload.contentType === "JOURNAL_DREAM" ? "journal-entry-search-DREAM" : "journal-entry-search-DIARY";
  const popup = window.open(url, popupName, "width=1960,height=1440,top=0,left=270");
  popup?.focus();
}

/** route.query 의 단일값·배열값을 string[] 로 정규화 */
function normalizeList(value: unknown): string[] {
  const raw = Array.isArray(value) ? value : [value];
  return raw.flatMap((v) => String(v ?? "").split(",")).map((v) => v.trim()).filter(Boolean);
}

async function onConfigure(): Promise<void> {
  const payload = { ...store.payload };
  store.close();

  try {
    const res = await axios.get(`/api/tags/${payload.tagId}/profile`, {
      params: { contentType: payload.contentType },
    });
    attachableStore.openTagProfile({
      ...(res.data?.rsltObj ?? {}),
      tagId: String(payload.tagId),
      name: payload.name,
      ctgr: payload.ctgr,
      contentType: payload.contentType,
      contentTypeLabel: getContentTypeLabel(payload.contentType),
    });
  } catch {
    attachableStore.openTagProfile({
      tagId: String(payload.tagId),
      name: payload.name,
      ctgr: payload.ctgr,
      contentType: payload.contentType,
      contentTypeLabel: getContentTypeLabel(payload.contentType),
    });
  }
}

function getContentTypeLabel(contentType: string): string {
  switch (contentType) {
    case "JOURNAL_DAY":
      return t("common.date");
    case "JOURNAL_DIARY":
      return t("common.diary");
    case "JOURNAL_DREAM":
      return t("common.dream");
    default:
      return contentType;
  }
}

function onDocumentClick(evt: MouseEvent): void {
  if (menuEl.value && menuEl.value.contains(evt.target as Node)) return;
  store.close();
}

function onKeydown(evt: KeyboardEvent): void {
  if (evt.key === "Escape") store.close();
}

function onScrollOrResize(): void {
  store.close();
}

onMounted(() => {
  document.addEventListener("click", onDocumentClick);
  document.addEventListener("keydown", onKeydown);
  window.addEventListener("resize", onScrollOrResize);
  window.addEventListener("scroll", onScrollOrResize, true);
});

onUnmounted(() => {
  document.removeEventListener("click", onDocumentClick);
  document.removeEventListener("keydown", onKeydown);
  window.removeEventListener("resize", onScrollOrResize);
  window.removeEventListener("scroll", onScrollOrResize, true);
});
</script>
