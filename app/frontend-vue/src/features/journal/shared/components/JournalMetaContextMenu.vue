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
      <button
        type="button"
        class="journal-tag-ctx-btn journal-tag-ctx-btn--graph"
        :disabled="alreadyOnGraph"
        @click="onViewGraph"
      >
        <i class="bi bi-graph-up"></i>
        <span>{{ alreadyOnGraph ? "그래프에 표시 중" : "그래프로 보기" }}</span>
      </button>
      <button type="button" class="journal-tag-ctx-btn journal-tag-ctx-btn--configure" @click="onSettings">
        <i class="bi bi-gear"></i>
        <span>메타 설정</span>
      </button>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRoute } from "vue-router";
import { useMetaContextMenuStore } from "@/features/journal/stores/metaContextMenu";
import { useJournalStore } from "@/features/journal/stores/journal";
import { useJournalModalStore } from "@/features/journal/stores/journalModal";
import { swalAlert } from "@/shared/utils/swal";

const store = useMetaContextMenuStore();
const journalStore = useJournalStore();
const journalModalStore = useJournalModalStore();
const route = useRoute();

const menuEl = ref<HTMLElement | null>(null);

const alreadyOnGraph = computed(() => {
  const id = store.payload.metaId;
  return journalStore.selectedMetas.some((m) => String(m.id) === String(id));
});

function onSearch(): void {
  const payload = { ...store.payload };
  store.close();
  void journalModalStore.openDayFilterModal({
    type: "meta",
    id: payload.metaId,
    name: payload.name,
    ctgr: payload.ctgr,
  });
}

function onViewGraph(): void {
  const payload = { ...store.payload };
  store.close();
  if (route.name !== "journal-meta") return;
  const added = journalStore.addMetaToGraph({
    id: Number(payload.metaId),
    name: payload.name,
    ctgr: payload.ctgr,
    unit: payload.unit,
  });
  if (!added) {
    void swalAlert("그래프에는 최대 2개의 메타만 표시할 수 있습니다. 목록 옆 X로 하나를 제거한 뒤 다시 시도하세요.");
  }
}

function onSettings(): void {
  const payload = { ...store.payload };
  store.close();
  void journalModalStore.openMetaProfile({
    id: payload.metaId,
    name: payload.name,
    ctgr: payload.ctgr,
    unit: payload.unit,
  });
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
