<template>
  <div :class="['chat-message-row', messageRowClass(message)]">
    <div v-if="!isOwnMessage(message)" class="chat-message-avatar">
      <template v-if="isAssistantMessage(message)">
        <span>{{ messageInitial(message) }}</span>
      </template>
      <template v-else-if="authStore.user?.profileImageUrl">
        <img
          :src="authStore.user.profileImageUrl"
          alt=""
          @error="handleProfileImageError"
        />
      </template>
      <template v-else>
        <span>{{ messageInitial(message) }}</span>
      </template>
    </div>

    <div class="chat-message-stack">
      <div class="chat-message-meta">
        <span class="chat-message-name">{{ messageName(message) }}</span>
        <span v-if="messageTime(message)" class="chat-message-time">
          {{ messageTime(message) }}
        </span>
      </div>
      <div
        v-if="isAssistantMessage(message)"
        :class="['chat-message-bubble', messageBubbleClass(message), 'chat-message-bubble--md']"
        v-html="assistantHtml(message)"
      ></div>
      <div
        v-else
        :class="['chat-message-bubble', messageBubbleClass(message)]"
      >
        {{ messageText(message) }}
      </div>
      <ChatRagPanel
        v-if="isAssistantMessage(message) && messageRagMetadata(message)"
        :message="message"
      />
    </div>

    <div
      v-if="isOwnMessage(message)"
      class="chat-message-avatar chat-message-avatar--user"
    >
      <template v-if="authStore.user?.profileImageUrl">
        <img
          :src="authStore.user.profileImageUrl"
          alt=""
          @error="handleProfileImageError"
        />
      </template>
      <template v-else>
        <span>{{ messageInitial(message) }}</span>
      </template>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {
  assistantHtml,
  isAssistantMessage,
  isOwnMessage,
  messageBubbleClass,
  messageRagMetadata,
  messageRowClass,
  messageText,
  messageTime,
} from "@/features/chat/chatMessageFormat";
import type { ChatMessage } from "@/features/chat/stores/chat";
import { useAuthStore } from "@/shared/auth/stores/auth";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { handleProfileImageError } from "@/shared/utils/profileImage";
import ChatRagPanel from "@/features/chat/components/ChatRagPanel.vue";

/**
 * 채팅 메시지 한 행: 아바타·작성자/시각 메타·버블(assistant HTML / user 평문)과
 * assistant 메시지의 RAG 근거 패널을 렌더한다. RAG 패널 렌더 게이트를 이 행이 소유한다.
 */
defineProps<{ message: ChatMessage }>();

const authStore = useAuthStore();
const { t } = useLocaleStore();

/** 표시 이름: assistant 는 카탈로그 이름, 사용자는 작성자명→닉네임→기본 호칭 순. */
function messageName(chatMessage: ChatMessage): string {
  if (isAssistantMessage(chatMessage)) return t("chat.assistant.name");
  return chatMessage.createdByNm || authStore.user?.nickname || t("chat.user.self");
}

/** 아바타 이니셜: assistant 카탈로그 이니셜, 사용자는 표시 이름 첫 글자. */
function messageInitial(chatMessage: ChatMessage): string {
  if (isAssistantMessage(chatMessage)) return t("chat.assistant.initials");
  return messageName(chatMessage).slice(0, 1) || t("chat.user.initials");
}
</script>

<style lang="scss" scoped>
@import "./chatMessageRow.scss";
</style>
