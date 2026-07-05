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
      <button type="button" class="journal-tag-ctx-btn journal-tag-ctx-btn--configure" @click="onConfigure">
        <i class="bi bi-sliders2"></i>
        <span>{{ t("journal.tag.settings") }}</span>
      </button>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from "vue";
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

async function onSearch(): Promise<void> {
  const payload = { ...store.payload };
  store.close();

  if (payload.contentType === "JOURNAL_DAY") {
    void journalModalStore.openDayFilterModal({ type: "tag", id: payload.tagId, name: payload.name, ctgr: payload.ctgr });
    return;
  }

  await openEntrySearchPopup(payload);
}

async function openEntrySearchPopup(payload: TagContextMenuPayload): Promise<void> {
  const newType = payload.contentType === "JOURNAL_DREAM" ? "DREAM" : "DIARY";
  const newTagId = String(payload.tagId);

  if (route.name === "journal-entry-search") {
    /*
     * 팝업 내부: 기존 tagIds 에 추가 (AND 검색). 중복 무시.
     */
    const existingIds = normalizeList(route.query.tagIds);
    if (existingIds.includes(newTagId)) return;
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
        tagIds: [...existingIds, newTagId],
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
    case "JOURNAL_INTERPRETATION":
      return t("common.interpretation");
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
