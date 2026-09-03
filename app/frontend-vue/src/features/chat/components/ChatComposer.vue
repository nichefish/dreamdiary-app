<template>
  <div class="chat-composer">
    <div class="chat-composer__main">
      <textarea
        v-model="message"
        class="chat-composer__input"
        rows="2"
        :placeholder="t('chat.composer.placeholder')"
        @keydown.enter.exact.prevent="submit"
      ></textarea>
      <div class="chat-composer__settings">
        <label class="chat-memory-select">
          <span>{{ t("chat.memory.label") }}</span>
          <select
            v-model.number="chat.setting.recentMessageLimit"
            :disabled="chat.isSettingSaving"
            @change="updateMemoryLimit"
          >
            <option
              v-for="option in MEMORY_LIMIT_OPTIONS"
              :key="option"
              :value="option"
            >
              {{ tf("chat.memory.recent-count", option) }}
            </option>
          </select>
        </label>
        <span v-if="chat.lastError" class="chat-error">
          {{ chat.lastError }}
        </span>
      </div>
    </div>
    <button
      :class="[
        'chat-send-btn',
        { 'chat-send-btn--waiting': chat.isWaitingResponse },
      ]"
      type="button"
      :disabled="!chat.isWaitingResponse && !message.trim()"
      @click="chat.isWaitingResponse ? chat.cancelMessage() : submit()"
    >
      <i class="ki-duotone ki-send fs-2">
        <span class="path1"></span>
        <span class="path2"></span>
      </i>
      <span>{{ chat.isWaitingResponse ? t("chat.action.stop") : t("chat.action.send") }}</span>
    </button>
  </div>
</template>

<script lang="ts" setup>
import { computed } from "vue";
import { MEMORY_LIMIT_OPTIONS, useChatStore } from "@/features/chat/stores/chat";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { tf as tfOf } from "@/features/chat/chatMessageFormat";

/**
 * 입력 컴포저: 메시지 입력·최근 메모리 개수 설정·전송/중단 버튼을 담당한다.
 * 입력 텍스트는 v-model(modelValue)로 상위(AppChat)가 소유하고 전송은 send 이벤트로 위임한다
 * (상위가 message.value 를 읽어 전송·초기화; 빈 세션 시드 전송의 입력 초기화 계약을 보존).
 * 대기 중에는 같은 버튼이 중단(chat.cancelMessage)으로 동작한다.
 */
const props = defineProps<{ modelValue: string }>();
const emit = defineEmits<{
  (e: "update:modelValue", value: string): void;
  (e: "send"): void;
}>();

const chat = useChatStore();
const { t } = useLocaleStore();

/** catalog 메시지의 {0}.. placeholder 를 순서대로 치환한다. (모듈 tf 에 로케일 t 주입) */
function tf(key: string, ...args: (string | number)[]): string {
  return tfOf(t, key, ...args);
}

/** 입력 텍스트 v-model: 실제 소유는 상위(AppChat)의 message ref. */
const message = computed({
  get: () => props.modelValue,
  set: (value: string) => emit("update:modelValue", value),
});

/** 전송/엔터: 상위에 위임(상위가 message.value 로 전송·초기화). */
function submit(): void {
  emit("send");
}

/** 최근 메모리 개수 select 변경을 서버 설정으로 반영한다(기본 50). */
function updateMemoryLimit(event: Event): void {
  const target = event.target as HTMLSelectElement;
  chat.updateSetting({
    ...chat.setting,
    recentMessageLimit: Number(target.value || 50),
  });
}
</script>

<style lang="scss" scoped>
.chat-composer {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  padding: 14px;
  border-top: 1px solid rgba(30, 41, 59, 0.08);
  background: #ffffff;
}

.chat-composer__main {
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  min-width: 0;
  gap: 8px;
}

.chat-composer__input {
  width: 100%;
  min-width: 0;
  max-height: 116px;
  padding: 12px 14px;
  resize: none;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  outline: none;
  color: #111827;
  background: #f8fafc;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.45;
  transition: border-color 0.16s ease, background 0.16s ease,
    box-shadow 0.16s ease;

  &:focus {
    border-color: rgba(21, 150, 166, 0.55);
    background: #ffffff;
    box-shadow: 0 0 0 4px rgba(21, 150, 166, 0.1);
  }
}

.chat-composer__settings {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 28px;
}

.chat-memory-select {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  max-width: 100%;
  color: #64748b;
  font-size: 11px;
  font-weight: 800;
}

.chat-memory-select select {
  height: 28px;
  max-width: 132px;
  padding: 0 26px 0 10px;
  border: 1px solid #e2e8f0;
  border-radius: 999px;
  background: #f8fafc;
  color: #334155;
  font-size: 11px;
  font-weight: 800;
  outline: none;
}

.chat-memory-select select:focus {
  border-color: rgba(21, 150, 166, 0.55);
  box-shadow: 0 0 0 3px rgba(21, 150, 166, 0.1);
}

.chat-error {
  min-width: 0;
  overflow: hidden;
  color: #dc2626;
  font-size: 11px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-send-btn {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-width: 86px;
  height: 44px;
  padding: 0 14px;
  border: 0;
  border-radius: 12px;
  background: #1596a6;
  color: #ffffff;
  font-size: 13px;
  font-weight: 800;
  transition: background 0.16s ease, opacity 0.16s ease,
    transform 0.16s ease;

  &:hover:not(:disabled) {
    background: #0b7280;
    transform: translateY(-1px);
  }

  &:disabled {
    cursor: not-allowed;
    opacity: 0.55;
  }
}

.chat-send-btn--waiting {
  background: #64748b;
}

@media (max-width: 575.98px) {
  .chat-composer {
    padding: 12px;
  }

  .chat-memory-select span {
    display: none;
  }

  .chat-send-btn {
    min-width: 48px;
    padding: 0 12px;
  }

  .chat-send-btn span {
    display: none;
  }
}
</style>
