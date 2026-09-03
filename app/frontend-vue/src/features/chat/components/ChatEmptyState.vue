<template>
  <div class="chat-empty-state">
    <div class="chat-empty-state__mark">
      <i class="ki-duotone ki-message-text-2 fs-1">
        <span class="path1"></span>
        <span class="path2"></span>
        <span class="path3"></span>
      </i>
    </div>
    <div class="chat-empty-state__title">
      {{ t("chat.empty.prompt") }}
    </div>
    <div class="chat-empty-state__seeds" role="list">
      <button
        v-for="seed in emptySeedPrompts"
        :key="seed.key"
        type="button"
        class="chat-empty-seed"
        role="listitem"
        :disabled="chat.isWaitingResponse"
        @click="emit('seed', seed.text)"
      >
        {{ seed.text }}
      </button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed } from "vue";
import { useChatStore } from "@/features/chat/stores/chat";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

/**
 * 빈 세션 안내 상태. 시드 질문 목록을 노출하고, 클릭 시 원문 텍스트를 상위(AppChat)로 seed 이벤트로 올린다.
 * 실제 전송과 스크롤은 messageList ref 를 소유한 AppChat 이 처리한다.
 */
const chat = useChatStore();
const { t } = useLocaleStore();

const emit = defineEmits<{ (e: "seed", text: string): void }>();

const EMPTY_SEED_KEYS = [
  "chat.empty.seed.1",
  "chat.empty.seed.2",
  "chat.empty.seed.3",
  "chat.empty.seed.4",
] as const;

/** 카탈로그에 실제 문구가 등록된 시드만 노출한다(키=문구인 미등록 항목 제외). */
const emptySeedPrompts = computed(() =>
  EMPTY_SEED_KEYS.map((key) => ({
    key,
    text: t(key),
  })).filter((seed) => !!seed.text && seed.text !== seed.key)
);
</script>

<style lang="scss" scoped>
.chat-empty-state {
  display: flex;
  flex: 1 1 auto;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 220px;
  color: #64748b;
  text-align: center;
}

.chat-empty-state__mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  margin-bottom: 12px;
  border-radius: 18px;
  background: #ffffff;
  color: #1596a6;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
}

.chat-empty-state__title {
  color: #334155;
  font-size: 14px;
  font-weight: 700;
}

.chat-empty-state__seeds {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  max-width: 420px;
  margin-top: 16px;
}

.chat-empty-seed {
  max-width: 100%;
  padding: 8px 12px;
  border: 1px solid #dbe4f0;
  border-radius: 999px;
  background: #ffffff;
  color: #334155;
  font-size: 12px;
  font-weight: 600;
  line-height: 1.4;
  text-align: left;
  cursor: pointer;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.05);
  transition: border-color 0.15s ease, background 0.15s ease, color 0.15s ease;
}

.chat-empty-seed:hover:not(:disabled) {
  border-color: #9ad0d8;
  background: #f4fbfd;
  color: #0f766e;
}

.chat-empty-seed:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}
</style>
