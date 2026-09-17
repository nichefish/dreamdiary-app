<template>
  <div class="chat-session-bar">
    <button
      class="chat-session-add-btn"
      type="button"
      :title="t('chat.action.new-chat')"
      :disabled="chat.isSessionLoading"
      @click="chat.createSession()"
    >
      <i class="ki-duotone ki-plus fs-2"></i>
    </button>
    <div class="chat-session-list">
      <button
        v-for="session in chat.sessions"
        :key="session.id"
        :class="[
          'chat-session-chip',
          {
            'chat-session-chip--active':
              session.id === chat.activeSessionId,
            'chat-session-chip--renaming':
              renamingSessionId === session.id,
          },
        ]"
        type="button"
        @click="onSessionChipClick(session)"
      >
        <span class="chat-session-chip__body">
          <input
            v-if="renamingSessionId === session.id"
            :ref="(el) => setRenameInputRef(session.id, el)"
            v-model="renameDraft"
            class="chat-session-chip__title-input"
            type="text"
            maxlength="200"
            :placeholder="t('chat.session.rename.placeholder')"
            :disabled="isRenamingSaving"
            @click.stop
            @dblclick.stop
            @keydown.enter.prevent="commitRename(session)"
            @keydown.esc.prevent="cancelRename"
            @blur="commitRename(session)"
          />
          <span
            v-else
            class="chat-session-chip__title"
            :title="t('chat.session.rename.hint')"
            @dblclick.stop="startRename(session)"
          >
            {{ sessionTitle(session) }}
          </span>
          <span
            v-if="sessionTime(session) && renamingSessionId !== session.id"
            class="chat-session-chip__time"
          >
            {{ sessionTime(session) }}
          </span>
        </span>
        <span
          class="chat-session-chip__delete"
          :title="t('chat.action.delete')"
          @click.stop="chat.deleteSession(session.id)"
        >
          <i class="ki-duotone ki-trash fs-5">
            <span class="path1"></span>
            <span class="path2"></span>
            <span class="path3"></span>
            <span class="path4"></span>
            <span class="path5"></span>
          </i>
        </span>
      </button>
      <div v-if="chat.sessions.length === 0" class="chat-session-empty">
        {{ chat.isSessionLoading ? t("chat.session.preparing") : t("chat.session.empty") }}
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { type ChatSession, useChatStore } from "@/features/chat/stores/chat";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { useSessionRename } from "@/features/chat/useSessionRename";

/**
 * 세션 칩 바. 세션 목록 표시·선택·생성·삭제와 칩 제목 더블클릭 인라인 이름변경을 담당한다.
 * 인라인 편집 상태·커밋/취소는 useSessionRename 컴포저블(chat·t 주입)에 위임한다.
 */
const chat = useChatStore();
const { t } = useLocaleStore();

const {
  renamingSessionId,
  renameDraft,
  isRenamingSaving,
  setRenameInputRef,
  startRename,
  cancelRename,
  commitRename,
} = useSessionRename(chat, t);

/** 세션 표시 제목: 사용자가 지정한 title 이 없으면 기본 제목 카탈로그를 쓴다. */
function sessionTitle(session: ChatSession): string {
  return session.title || t("chat.session.default-title");
}

/** 칩 클릭으로 세션 전환. 단, 해당 칩이 이름변경 편집 중이면 무시한다. */
function onSessionChipClick(session: ChatSession): void {
  if (renamingSessionId.value === session.id) return;
  chat.selectSession(session.id);
}

/** 칩 보조 표기 시각: 마지막 메시지 시각, 없으면 생성 시각. */
function sessionTime(session: ChatSession): string {
  return session.lastMessageAt || session.createdAt || "";
}
</script>

<style lang="scss" scoped>
.chat-session-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-bottom: 1px solid rgba(30, 41, 59, 0.08);
  background: #ffffff;
}

.chat-session-add-btn {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: 0;
  border-radius: 10px;
  background: #1596a6;
  color: #ffffff;
  transition: background 0.16s ease, opacity 0.16s ease;

  &:hover:not(:disabled) {
    background: #0b7280;
  }

  &:disabled {
    opacity: 0.5;
  }
}

.chat-session-list {
  flex: 1 1 auto;
  display: flex;
  gap: 8px;
  min-width: 0;
  overflow-x: auto;
  scrollbar-width: thin;
}

.chat-session-chip {
  flex: 0 0 154px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  min-width: 0;
  padding: 8px 10px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #f8fafc;
  color: #334155;
  text-align: left;
  transition: border-color 0.16s ease, background 0.16s ease,
    color 0.16s ease;

  &:hover {
    border-color: rgba(21, 150, 166, 0.35);
    background: #f0fbfc;
  }
}

.chat-session-chip__body {
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-session-chip--active {
  border-color: rgba(21, 150, 166, 0.58);
  background: #e8f7f8;
  color: #0b7280;
}

.chat-session-chip__title,
.chat-session-chip__time {
  display: block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-session-chip__title {
  font-size: 12px;
  font-weight: 800;
}

.chat-session-chip__title-input {
  width: 100%;
  min-width: 0;
  padding: 0;
  border: 0;
  border-radius: 4px;
  background: #ffffff;
  color: #0f172a;
  font-size: 12px;
  font-weight: 800;
  line-height: 1.3;
  outline: 1px solid rgba(21, 150, 166, 0.55);
}

.chat-session-chip--renaming {
  border-color: rgba(21, 150, 166, 0.58);
  background: #e8f7f8;
}

.chat-session-chip__time {
  margin-top: 3px;
  color: #94a3b8;
  font-size: 10px;
  font-weight: 600;
}

.chat-session-chip__delete {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 8px;
  color: #94a3b8;
  opacity: 0;
  transition: background 0.16s ease, color 0.16s ease, opacity 0.16s ease;
}

.chat-session-chip:hover .chat-session-chip__delete,
.chat-session-chip--active .chat-session-chip__delete {
  opacity: 1;
}

.chat-session-chip__delete:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
}

.chat-session-empty {
  display: flex;
  align-items: center;
  height: 34px;
  color: #94a3b8;
  font-size: 12px;
  font-weight: 700;
}
</style>
