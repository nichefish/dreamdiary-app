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
      <button type="button" class="journal-tag-ctx-btn journal-tag-ctx-btn--copy" @click="onCopy">
        <i class="bi bi-copy"></i>
        <span>{{ t("common.copy") }}</span>
      </button>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useSelectionContextMenuStore } from "@/features/journal/stores/selectionContextMenu";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalFire } from "@/shared/utils/swal";
import { assertAuthenticatedBeforePopup } from "@/shared/auth/popupAuth";
import { joinAppBasePath } from "@/shared/utils/appPath";

const store = useSelectionContextMenuStore();
const { t } = useLocaleStore();
const route = useRoute();
const router = useRouter();

const menuEl = ref<HTMLElement | null>(null);

/**
 * 본문 선택 우클릭 판정.
 * 다음을 모두 충족할 때만 네이티브 메뉴를 가로채 커스텀 메뉴를 연다:
 *  1) 선택된 평문이 존재하고,
 *  2) 선택 시작 노드가 `.journal-content`(엔트리·리플렉션 본문) 안에 있으며,
 *  3) 상위 엔트리에서 일기/꿈 도메인(`[data-journal-domain]` = diary|dream)이 파생된다.
 * 하나라도 어긋나면 preventDefault 하지 않아 브라우저 기본 메뉴를 그대로 둔다.
 * 리플렉션 본문은 자체 마커가 없어 `.closest`가 부모 엔트리 도메인을 상속한다.
 */
function onContextMenu(event: MouseEvent): void {
  const selection = window.getSelection();
  const text = selection ? selection.toString().trim() : "";
  if (!text) return;
  const anchor = selection?.anchorNode ?? null;
  const anchorEl = anchor instanceof Element ? anchor : anchor?.parentElement ?? null;
  const contentEl = anchorEl?.closest(".journal-content");
  if (!contentEl) return;
  const domainEl = contentEl.closest("[data-journal-domain]");
  const domain = domainEl?.getAttribute("data-journal-domain");
  if (domain !== "diary" && domain !== "dream") return;
  event.preventDefault();
  store.open(event, { text, type: domain === "dream" ? "DREAM" : "DIARY" });
}

/**
 * 선택 조각을 키워드로 전체검색을 새 창으로 연다.
 * 진입 방식은 툴바 전체검색(openSearchTab)과 동일한 팝업 계약을 따른다.
 */
async function onSearch(): Promise<void> {
  const { text, type } = store.payload;
  store.close();
  if (!await assertAuthenticatedBeforePopup(router, route)) return;
  const params = new URLSearchParams({ type });
  params.set("searchKeywords", text);
  const popup = window.open(
    joinAppBasePath(`/journal/entry/search?${params.toString()}`),
    `journal-entry-search-${type}`,
    "width=1960,height=1440,top=0,left=270",
  );
  if (popup) popup.focus();
}

/**
 * 선택한 평문을 그대로 클립보드에 복사한다(드래그한 텍스트 그대로).
 */
async function onCopy(): Promise<void> {
  const { text } = store.payload;
  store.close();
  try {
    await navigator.clipboard.writeText(text);
    void swalFire({ icon: "success", text: t("common.copy.success") });
  } catch (error: unknown) {
    console.error("[journal-selection] clipboard copy failed", error);
    void swalFire({ icon: "error", text: t("common.copy.failure") });
  }
}

/** 메뉴 바깥 클릭 시 닫는다(메뉴 내부 클릭은 유지). */
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
  document.addEventListener("contextmenu", onContextMenu);
  document.addEventListener("click", onDocumentClick);
  document.addEventListener("keydown", onKeydown);
  window.addEventListener("resize", onScrollOrResize);
  window.addEventListener("scroll", onScrollOrResize, true);
});

onUnmounted(() => {
  document.removeEventListener("contextmenu", onContextMenu);
  document.removeEventListener("click", onDocumentClick);
  document.removeEventListener("keydown", onKeydown);
  window.removeEventListener("resize", onScrollOrResize);
  window.removeEventListener("scroll", onScrollOrResize, true);
});
</script>