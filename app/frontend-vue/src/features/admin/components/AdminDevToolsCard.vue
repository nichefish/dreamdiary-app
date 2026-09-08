<template>
  <section class="card post admin-dev-tools-card">
    <div class="card-body">
      <h3 class="admin-section-title">{{ t('admin.page.section.dev-tools') }}</h3>

      <div class="admin-tool-row">
        <div>
          <div class="fw-bold">{{ t('admin.page.dev-tools.debug-collapse.title') }}</div>
          <div class="text-muted fs-8">{{ t('admin.page.dev-tools.debug-collapse.desc') }}</div>
        </div>
        <div class="admin-tool-actions">
          <label class="form-check form-switch mb-0">
            <input
              v-model="debugCollapseEnabled"
              class="form-check-input"
              type="checkbox"
            />
            <span class="form-check-label">{{ t('admin.page.dev-tools.debug-collapse.label') }}</span>
          </label>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

/**
 * 개발 도구 카드: 저널 접힘 상태 디버그 표시 토글(general 탭).
 * 렌더 게이트(activeTab === "general")는 상위 AdminPage 가 소유한다.
 */
const { t } = useLocaleStore();

/** localStorage("debug_collapse") 토글 — 저널 접힘 상태 디버그 표시 */
const debugCollapseEnabled = ref(localStorage.getItem("debug_collapse") === "true");
watch(debugCollapseEnabled, (enabled) => {
  if (enabled) {
    localStorage.setItem("debug_collapse", "true");
  } else {
    localStorage.removeItem("debug_collapse");
  }
});
</script>

<style scoped>
@import "./adminCards.scss";
</style>
