<template>
  <div class="app-chat-engage">
    <button
      class="app-chat-engage-btn"
      type="button"
      :title="t('chat.engage.tooltip')"
      @click="chat.toggle()"
    >
      <i class="ki-duotone ki-messages fs-1 pt-1 mb-2">
        <span class="path1"></span>
        <span class="path2"></span>
        <span class="path3"></span>
        <span class="path4"></span>
        <span class="path5"></span>
      </i>
      <span
        :class="[
          'bullet bullet-dot h-6px w-6px position-absolute translate-middle',
          chat.isConnected ? 'bg-success animation-blink' : 'bg-secondary',
        ]"
      ></span>
      {{ t("chat.engage.label") }}
    </button>
  </div>
</template>

<script lang="ts" setup>
import { useChatStore } from "@/features/chat/stores/chat";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

/**
 * 우하단 고정 챗봇 실행 런처 버튼.
 * 연결 상태 표시(bullet)와 드로어 토글만 담당한다. 인증 게이트는 상위(AppChat)가 소유한다.
 */
const chat = useChatStore();
const { t } = useLocaleStore();
</script>

<style lang="scss" scoped>
.app-chat-engage {
  position: fixed;
  right: 18px;
  bottom: 28px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  z-index: 601;
}

.app-chat-engage-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-width: 112px;
  height: 48px;
  padding: 0 16px;
  border: 1px solid rgba(30, 41, 59, 0.1);
  border-radius: 999px;
  background: #ffffff;
  color: #1f2a44;
  box-shadow: 0 14px 34px rgba(31, 42, 68, 0.22);
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0;
  transition: transform 0.16s ease, box-shadow 0.16s ease,
    border-color 0.16s ease;

  &:hover {
    color: #0b5c6b;
    border-color: rgba(21, 150, 166, 0.35);
    box-shadow: 0 16px 42px rgba(31, 42, 68, 0.28);
    transform: translateY(-2px);
  }

  i {
    color: #1596a6;
    font-size: 22px;
  }

  .bullet {
    top: 11px !important;
    right: 14px !important;
  }
}

@media (max-width: 575.98px) {
  .app-chat-engage {
    right: 14px;
    bottom: 18px;
  }
}
</style>
