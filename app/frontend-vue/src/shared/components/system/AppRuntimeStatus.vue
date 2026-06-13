<template>
  <div v-if="status.error" class="app-runtime-status app-runtime-status--error" role="alert">
    <div class="app-runtime-status__panel">
      <div class="app-runtime-status__eyebrow">{{ status.error.source ?? "runtime" }}</div>
      <h1 class="app-runtime-status__title">{{ status.error.title }}</h1>
      <p class="app-runtime-status__message">{{ status.error.message }}</p>
      <pre v-if="status.error.detail" class="app-runtime-status__detail">{{ status.error.detail }}</pre>
      <div class="app-runtime-status__actions">
        <button type="button" class="btn btn-sm btn-light-primary" @click="reload">
          <i class="bi bi-arrow-clockwise"></i>새로고침
        </button>
        <button type="button" class="btn btn-sm btn-light" @click="goHome">
          <i class="bi bi-house"></i>메인으로
        </button>
      </div>
    </div>
  </div>
  <div v-else-if="showPending" class="app-runtime-status app-runtime-status--pending" role="status">
    <div class="app-runtime-status__pending">
      <span class="spinner-border spinner-border-sm text-primary" aria-hidden="true"></span>
      <span>{{ status.pendingLabel || "화면을 준비하고 있습니다." }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { appRuntimeStatus as status, clearRuntimeError } from "@/shared/utils/appRuntimeStatus";

const router = useRouter();
const showPending = ref(false);
let pendingTimer: number | undefined;

watch(
  () => status.pending,
  (pending) => {
    window.clearTimeout(pendingTimer);
    showPending.value = false;
    if (pending) {
      pendingTimer = window.setTimeout(() => {
        showPending.value = true;
      }, 500);
    }
  },
  { immediate: true }
);

onBeforeUnmount(() => {
  window.clearTimeout(pendingTimer);
});

const homeTarget = computed(() => ({ path: "/" }));

function reload() {
  window.location.reload();
}

function goHome() {
  clearRuntimeError();
  void router.push(homeTarget.value);
}
</script>

<style scoped>
.app-runtime-status {
  position: fixed;
  z-index: 12000;
  left: 0;
  right: 0;
  pointer-events: none;
}

.app-runtime-status--error {
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(248, 250, 252, 0.92);
  pointer-events: auto;
}

.app-runtime-status__panel {
  width: min(760px, 100%);
  max-height: min(720px, calc(100vh - 48px));
  overflow: auto;
  border: 1px solid #e4e6ef;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.16);
  padding: 28px;
}

.app-runtime-status__eyebrow {
  color: #7e8299;
  font-size: 12px;
  font-weight: 700;
  margin-bottom: 8px;
  text-transform: uppercase;
}

.app-runtime-status__title {
  color: #181c32;
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 10px;
}

.app-runtime-status__message {
  color: #5e6278;
  font-size: 14px;
  margin: 0 0 16px;
  white-space: pre-wrap;
}

.app-runtime-status__detail {
  max-height: 260px;
  overflow: auto;
  border-radius: 6px;
  background: #f5f8fa;
  color: #3f4254;
  font-size: 12px;
  line-height: 1.45;
  padding: 14px;
  white-space: pre-wrap;
}

.app-runtime-status__actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 18px;
}

.app-runtime-status--pending {
  top: 16px;
  display: flex;
  justify-content: center;
}

.app-runtime-status__pending {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: 1px solid #e4e6ef;
  border-radius: 6px;
  background: #fff;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.12);
  color: #5e6278;
  font-size: 13px;
  font-weight: 600;
  padding: 8px 12px;
  pointer-events: auto;
}
</style>
