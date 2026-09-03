<template>
  <template v-if="authStore.isAuthenticated">
    <ChatLauncher />

    <div
      v-if="chat.isOpen"
      class="app-chat-drawer bg-body drawer drawer-end drawer-on"
    >
      <div class="chat-shell" id="chat_messenger">
        <ChatHeader />

        <ChatSessionBar />

        <div ref="messageList" class="chat-message-list">
          <ChatEmptyState v-if="!chat.messages || chat.messages.length === 0" @seed="sendSeedPrompt" />

          <ChatMessageRow
            v-for="(message, index) in chat.messages"
            :key="message.id || index"
            :message="message"
          />

          <ChatPendingRow v-if="chat.isWaitingResponse" />
        </div>

        <ChatComposer v-model="message" @send="sendMessage" />
      </div>
    </div>
  </template>
</template>

<script lang="ts" setup>
import { nextTick, onBeforeUnmount, ref, watch } from "vue";
import { useAuthStore } from "@/shared/auth/stores/auth";
import {
  useChatStore,
} from "@/features/chat/stores/chat";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import ChatLauncher from "@/features/chat/components/ChatLauncher.vue";
import ChatHeader from "@/features/chat/components/ChatHeader.vue";
import ChatEmptyState from "@/features/chat/components/ChatEmptyState.vue";
import ChatSessionBar from "@/features/chat/components/ChatSessionBar.vue";
import ChatComposer from "@/features/chat/components/ChatComposer.vue";
import ChatMessageRow from "@/features/chat/components/ChatMessageRow.vue";
import ChatPendingRow from "@/features/chat/components/ChatPendingRow.vue";

const authStore = useAuthStore();
const { t } = useLocaleStore();

const chat = useChatStore();
const message = ref("");
const messageList = ref<HTMLElement | null>(null);

watch(
  () => authStore.isAuthenticated,
  async (isAuthenticated) => {
    if (isAuthenticated) {
      try {
        await chat.initialize();
      } catch (error) {
        chat.lastError =
          error instanceof Error ? error.message : t("chat.error.init-failure");
      }
      return;
    }

    chat.reset();
  },
  { immediate: true }
);

watch(
  () => [chat.messages.length, chat.isWaitingResponse, chat.isOpen, chat.streamingContent],
  () => {
    scrollToBottom();
  }
);

onBeforeUnmount(() => {
  chat.reset();
});

function scrollToBottom(): void {
  nextTick(() => {
    if (!messageList.value) return;
    messageList.value.scrollTop = messageList.value.scrollHeight;
  });
}

async function sendMessage(): Promise<void> {
  const nextMessage = message.value.trim();
  if (!nextMessage) return;

  message.value = "";
  await chat.sendMessage(nextMessage);
  scrollToBottom();
}

/**
 * 빈 세션 시드 질문을 즉시 전송한다. composer 입력값은 비운다.
 */
async function sendSeedPrompt(seedText: string): Promise<void> {
  const nextMessage = seedText.trim();
  if (!nextMessage || chat.isWaitingResponse) return;
  message.value = "";
  await chat.sendMessage(nextMessage);
  scrollToBottom();
}

</script>

<style lang="scss" scoped>
.app-chat-drawer {
  position: fixed !important;
  top: 78px !important;
  right: 18px !important;
  bottom: 22px !important;
  width: min(440px, calc(100vw - 28px)) !important;
  height: calc(100vh - 100px) !important;
  z-index: 6002;
  overflow: hidden;
  border: 1px solid rgba(30, 41, 59, 0.08);
  border-radius: 14px;
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.32);
}

.chat-shell {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  background: #f8fafc;
}

.chat-message-list {
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 0;
  padding: 20px 18px;
  overflow-y: auto;
}

@media (max-width: 575.98px) {
  .app-chat-drawer {
    top: 64px !important;
    right: 10px !important;
    bottom: 10px !important;
    width: calc(100vw - 20px) !important;
    height: calc(100vh - 74px) !important;
    border-radius: 12px;
  }

  .chat-message-list {
    padding: 16px 14px;
  }

}
</style>
