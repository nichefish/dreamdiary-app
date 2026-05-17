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
        <span>검색</span>
      </button>
      <button type="button" class="journal-tag-ctx-btn journal-tag-ctx-btn--configure" @click="onConfigure">
        <i class="bi bi-sliders2"></i>
        <span>태그 설정</span>
      </button>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import axios from "axios";
import { useTagContextMenuStore, type TagContextMenuPayload } from "@/stores/tagContextMenu";
import { useJournalModalStore } from "@/stores/journalModal";
import { useAttachableModalStore } from "@/stores/attachableModal";

const store = useTagContextMenuStore();
const journalModalStore = useJournalModalStore();
const attachableStore = useAttachableModalStore();
const route = useRoute();
const router = useRouter();

const menuEl = ref<HTMLElement | null>(null);

function onSearch(): void {
  const payload = { ...store.payload };
  store.close();

  if (payload.contentType === "JOURNAL_DAY") {
    void journalModalStore.openTagDtl(payload.tagId, payload.name);
    return;
  }

  openEntrySearchPopup(payload);
}

function openEntrySearchPopup(payload: TagContextMenuPayload): void {
  const newType = payload.contentType === "JOURNAL_DREAM" ? "DREAM" : "DIARY";
  const newTagId = String(payload.tagId);
  const newTagName = payload.name ?? "";

  if (route.name === "journal-entry-search") {
    /*
     * 팝업 내부: 기존 tagIds 에 추가 (AND 검색). 중복 무시.
     * tagNames 는 검색 페이지가 캐시하므로 새 태그 이름만 전달한다.
     * 페이지의 watch 가 tagNames 를 소비해 캐시에 저장 후 URL 에서 제거한다.
     */
    const existingIds = normalizeList(route.query.tagIds);
    if (existingIds.includes(newTagId)) return;
    void router.replace({
      name: "journal-entry-search",
      query: {
        ...route.query,
        tagIds: [...existingIds, newTagId],
        tagNames: newTagName,
      },
    });
    return;
  }

  /* 외부: 새 팝업 창 열기 */
  const params = new URLSearchParams({ type: newType, tagIds: newTagId, tagNames: newTagName });
  const basePath = `${import.meta.env.BASE_URL.replace(/\/$/, "")}/journal/entry/search`;
  const url = `${basePath}?${params.toString()}`;
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
      return "일자";
    case "JOURNAL_DIARY":
      return "일기";
    case "JOURNAL_DREAM":
      return "꿈";
    case "JOURNAL_INTERPRETATION":
      return "해석";
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
