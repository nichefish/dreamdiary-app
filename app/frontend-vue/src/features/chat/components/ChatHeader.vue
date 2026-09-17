<template>
  <div class="chat-shell__header">
    <div class="chat-shell__identity">
      <div class="chat-ai-avatar">
        <i class="ki-duotone ki-abstract-26 fs-2">
          <span class="path1"></span>
          <span class="path2"></span>
        </i>
      </div>
      <div class="chat-shell__title-wrap">
        <div class="chat-shell__title">{{ t("chat.assistant.name") }}</div>
        <div class="chat-shell__status">
          <span class="chat-shell__status-dot"></span>
          <span>{{ chat.isConnected ? t("chat.status.online") : t("chat.status.connecting") }}</span>
        </div>
      </div>
    </div>
    <button
      class="chat-icon-btn"
      id="kt_drawer_chat_close"
      type="button"
      :title="t('chat.action.close')"
      @click="chat.close()"
    >
      <i class="ki-duotone ki-cross fs-2">
        <span class="path1"></span>
        <span class="path2"></span>
      </i>
    </button>
  </div>
</template>

<script lang="ts" setup>
import { useChatStore } from "@/features/chat/stores/chat";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

/**
 * 챗봇 드로어 헤더. AI 정체성(아바타·이름)·연결 상태 표시와 닫기 버튼을 담당한다.
 * 닫기 버튼 id(kt_drawer_chat_close)는 Metronic 드로어 계약을 위해 보존한다.
 */
const chat = useChatStore();
const { t } = useLocaleStore();
</script>

<style lang="scss" scoped>
.chat-shell__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 18px;
  border-bottom: 1px solid rgba(30, 41, 59, 0.08);
  background: #ffffff;
}

.chat-shell__identity {
  display: flex;
  align-items: center;
  min-width: 0;
  gap: 12px;
}

.chat-ai-avatar {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: #e8f7f8;
  color: #0b7280;
  width: 42px;
  height: 42px;
  border-radius: 12px;
}

.chat-shell__title-wrap {
  min-width: 0;
}

.chat-shell__title {
  overflow: hidden;
  color: #111827;
  font-size: 16px;
  font-weight: 800;
  line-height: 1.2;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-shell__status {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
}

.chat-shell__status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #17c653;
  box-shadow: 0 0 0 4px rgba(23, 198, 83, 0.12);
}

.chat-icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: 0;
  border-radius: 10px;
  background: transparent;
  color: #64748b;
  transition: background 0.16s ease, color 0.16s ease;

  &:hover {
    background: #f1f5f9;
    color: #0f172a;
  }
}

@media (max-width: 575.98px) {
  .chat-shell__header {
    padding: 14px;
  }
}
</style>
