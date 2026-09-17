<template>
  <div class="chat-message-row chat-message-row--assistant chat-message-row--pending">
    <div class="chat-message-avatar">
      <span>{{ t("chat.assistant.initials") }}</span>
    </div>
    <div class="chat-message-stack">
      <div class="chat-message-meta">
        <span class="chat-message-name">{{ t("chat.assistant.name") }}</span>
      </div>
      <div
        v-if="chat.streamingContent"
        class="chat-message-bubble chat-message-bubble--assistant chat-message-bubble--streaming"
      >
        {{ chat.streamingContent }}
      </div>
      <div
        v-else
        class="chat-message-bubble chat-message-bubble--assistant chat-message-bubble--typing"
      >
        <span class="chat-typing-dot"></span>
        <span class="chat-typing-dot"></span>
        <span class="chat-typing-dot"></span>
        <span v-if="waitingPhaseLabel()" class="chat-typing-phase">
          {{ waitingPhaseLabel() }}
        </span>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useChatStore } from "@/features/chat/stores/chat";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

/**
 * 응답 대기/스트리밍 인디케이터 행. 서버 PROGRESS 단계(SEARCHING/GENERATING)를 typing
 * 인디케이터 옆에 표시하고, streamingContent 가 있으면 임시 assistant 버블로 평문 누적을 보인다.
 * 표시 게이트(chat.isWaitingResponse)는 상위 호출부가 소유한다.
 */
const chat = useChatStore();
const { t } = useLocaleStore();

/** 대기 단계 라벨: 서버 responsePhase 를 카탈로그 문구로 매핑(그 외 빈 문자열). */
function waitingPhaseLabel(): string {
  const phase = chat.responsePhase;
  if (phase === "SEARCHING") return t("chat.waiting.searching");
  if (phase === "GENERATING") return t("chat.waiting.generating");
  return "";
}
</script>

<style lang="scss" scoped>
@import "./chatMessageRow.scss";
</style>
