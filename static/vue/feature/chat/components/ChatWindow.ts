/**
 * ChatWindow.ts
 *
 * @author nichefish
 */
export default {
    name: 'ChatWindow',
    data() {
        return {
            message: '',
            memoryOptions: [10, 20, 40, 80]
        };
    },
    props: {
        chatMessages: {
            type: Array,
            default: () => []
        },
        sessions: {
            type: Array,
            default: () => []
        },
        activeSessionId: {
            type: Number,
            default: null
        },
        authInfo: {
            type: Object,
            default: null
        },
        chatSetting: {
            type: Object,
            default: () => ({ recentMessageLimit: 20 })
        },
        isChatOpen: {
            type: Boolean,
            required: true
        },
        isWaitingResponse: {
            type: Boolean,
            default: false
        },
        isSessionLoading: {
            type: Boolean,
            default: false
        },
        isSettingSaving: {
            type: Boolean,
            default: false
        }
    },
    watch: {
        chatMessages: {
            handler(): void {
                this.scrollToBottom();
            },
            deep: true
        },
        isWaitingResponse(): void {
            this.scrollToBottom();
        }
    },
    methods: {
        cancelMessage(): void {
            this.$emit('cancel-message');
        },
        sendMessage(): void {
            if (this.isWaitingResponse) return;
            if (!this.message.trim()) return;

            this.$emit('send-message', this.message.trim());
            this.message = '';
        },
        closeChat(): void {
            this.$emit('close-chat');
        },
        newSession(): void {
            if (this.isSessionLoading) return;
            this.$emit('new-session');
        },
        selectSession(session: any): void {
            if (!session?.id) return;
            this.$emit('select-session', session.id);
        },
        deleteSession(session: any, event: Event): void {
            event.stopPropagation();
            if (!session?.id) return;
            this.$emit('delete-session', session.id);
        },
        sessionTitle(session: any): string {
            return session?.title || '새 대화';
        },
        sessionTime(session: any): string {
            return session?.lastMessageAt || session?.createdAt || '';
        },
        isActiveSession(session: any): boolean {
            return session?.id === this.activeSessionId;
        },
        currentMemoryLimit(): number {
            return Number(this.chatSetting?.recentMessageLimit || 20);
        },
        updateMemoryLimit(event: Event): void {
            const target = event.target as HTMLSelectElement;
            const recentMessageLimit = Number(target.value || 20);
            this.$emit('update-setting', {
                ...this.chatSetting,
                recentMessageLimit
            });
        },
        scrollToBottom(): void {
            this.$nextTick(() => {
                const messageList = this.$refs.messageList as HTMLElement;
                if (messageList) messageList.scrollTop = messageList.scrollHeight;
            });
        },
        handleImageError(event: Event): void {
            const target = event.target as HTMLImageElement;
            target.src = '/metronic/assets/media/avatars/avatar_blank.png';
        },
        messageRole(msg: any): string {
            return (msg?.role || '').toString().toUpperCase();
        },
        isAssistantMessage(msg: any): boolean {
            const role = this.messageRole(msg);
            return role === 'ASSISTANT' || role === 'AI' || role === 'SYSTEM';
        },
        isOwnMessage(msg: any): boolean {
            return !this.isAssistantMessage(msg) && msg?.isCreatedBy === true;
        },
        messageName(msg: any): string {
            if (this.isAssistantMessage(msg)) return 'Dreamdiary AI';
            return msg?.createdByNm || this.authInfo?.nickname || '나';
        },
        messageInitial(msg: any): string {
            return this.isAssistantMessage(msg) ? 'AI' : (this.messageName(msg).slice(0, 1) || 'U');
        },
        messageTime(msg: any): string {
            return msg?.createdAt || '';
        },
        messageText(msg: any): string {
            return msg?.content || '';
        },
        messageRowClass(msg: any): string {
            if (this.isAssistantMessage(msg)) return 'chat-message-row--assistant';
            return this.isOwnMessage(msg) ? 'chat-message-row--own' : 'chat-message-row--other';
        },
        messageBubbleClass(msg: any): string {
            if (this.isAssistantMessage(msg)) return 'chat-message-bubble--assistant';
            return this.isOwnMessage(msg) ? 'chat-message-bubble--own' : 'chat-message-bubble--other';
        }
    },
    template: `
        <div v-if="isChatOpen" class="app-chat-drawer bg-body drawer drawer-end drawer-on">
            <div class="chat-shell" id="chat_messenger">
                <div class="chat-shell__header">
                    <div class="chat-shell__identity">
                        <div class="chat-ai-avatar">
                            <i class="ki-duotone ki-abstract-26 fs-2">
                                <span class="path1"></span>
                                <span class="path2"></span>
                            </i>
                        </div>
                        <div class="chat-shell__title-wrap">
                            <div class="chat-shell__title">Dreamdiary AI</div>
                            <div class="chat-shell__status">
                                <span class="chat-shell__status-dot"></span>
                                <span>온라인</span>
                            </div>
                        </div>
                    </div>
                    <button class="chat-icon-btn" id="kt_drawer_chat_close" type="button" title="닫기" @click="closeChat">
                        <i class="ki-duotone ki-cross fs-2">
                            <span class="path1"></span>
                            <span class="path2"></span>
                        </i>
                    </button>
                </div>

                <div class="chat-session-bar">
                    <button class="chat-session-add-btn"
                            type="button"
                            title="새 대화"
                            :disabled="isSessionLoading"
                            @click="newSession">
                        <i class="ki-duotone ki-plus fs-2"></i>
                    </button>
                    <div class="chat-session-list">
                        <button v-for="session in sessions"
                                :key="session.id"
                                :class="['chat-session-chip', { 'chat-session-chip--active': isActiveSession(session) }]"
                                type="button"
                                @click="selectSession(session)">
                            <span class="chat-session-chip__body">
                                <span class="chat-session-chip__title">{{ sessionTitle(session) }}</span>
                                <span v-if="sessionTime(session)" class="chat-session-chip__time">{{ sessionTime(session) }}</span>
                            </span>
                            <span class="chat-session-chip__delete"
                                  title="삭제"
                                  @click="deleteSession(session, $event)">
                                <i class="ki-duotone ki-trash fs-5">
                                    <span class="path1"></span>
                                    <span class="path2"></span>
                                    <span class="path3"></span>
                                    <span class="path4"></span>
                                    <span class="path5"></span>
                                </i>
                            </span>
                        </button>
                        <div v-if="sessions.length === 0" class="chat-session-empty">
                            {{ isSessionLoading ? '대화 준비 중' : '새 대화' }}
                        </div>
                    </div>
                </div>

                <div ref="messageList" class="chat-message-list" id="chat_messenger_window">
                    <div v-if="!chatMessages || chatMessages.length === 0" class="chat-empty-state">
                        <div class="chat-empty-state__mark">
                            <i class="ki-duotone ki-message-text-2 fs-1">
                                <span class="path1"></span>
                                <span class="path2"></span>
                                <span class="path3"></span>
                            </i>
                        </div>
                        <div class="chat-empty-state__title">무엇부터 정리해볼까요?</div>
                    </div>

                    <div v-for="(msg, index) in chatMessages"
                         :key="msg?.id || index"
                         :class="['chat-message-row', messageRowClass(msg)]">
                        <div class="chat-message-avatar" v-if="!isOwnMessage(msg)">
                            <template v-if="isAssistantMessage(msg)">
                                <span>{{ messageInitial(msg) }}</span>
                            </template>
                            <template v-else-if="authInfo?.profileImageUrl">
                                <img :src="authInfo?.profileImageUrl" alt="" @error="handleImageError" />
                            </template>
                            <template v-else>
                                <span>{{ messageInitial(msg) }}</span>
                            </template>
                        </div>

                        <div class="chat-message-stack">
                            <div class="chat-message-meta">
                                <span class="chat-message-name">{{ messageName(msg) }}</span>
                                <span v-if="messageTime(msg)" class="chat-message-time">{{ messageTime(msg) }}</span>
                            </div>
                            <div :class="['chat-message-bubble', messageBubbleClass(msg)]">
                                {{ messageText(msg) }}
                            </div>
                        </div>

                        <div class="chat-message-avatar chat-message-avatar--user" v-if="isOwnMessage(msg)">
                            <template v-if="authInfo?.profileImageUrl">
                                <img :src="authInfo?.profileImageUrl" alt="" @error="handleImageError" />
                            </template>
                            <template v-else>
                                <span>{{ messageInitial(msg) }}</span>
                            </template>
                        </div>
                    </div>

                    <div v-if="isWaitingResponse" class="chat-message-row chat-message-row--assistant chat-message-row--pending">
                        <div class="chat-message-avatar">
                            <span>AI</span>
                        </div>
                        <div class="chat-message-stack">
                            <div class="chat-message-meta">
                                <span class="chat-message-name">Dreamdiary AI</span>
                            </div>
                            <div class="chat-message-bubble chat-message-bubble--assistant chat-message-bubble--typing">
                                <span class="chat-typing-dot"></span>
                                <span class="chat-typing-dot"></span>
                                <span class="chat-typing-dot"></span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="chat-composer">
                    <div class="chat-composer__main">
                        <textarea class="chat-composer__input"
                                  rows="2"
                                  v-model="message"
                                  @keydown.enter.exact.prevent="sendMessage"
                                  placeholder="메시지를 입력하세요"></textarea>
                        <div class="chat-composer__settings">
                            <label class="chat-memory-select">
                                <span>기억</span>
                                <select :value="currentMemoryLimit()" :disabled="isSettingSaving" @change="updateMemoryLimit">
                                    <option v-for="option in memoryOptions" :key="option" :value="option">최근 {{ option }}개</option>
                                </select>
                            </label>
                        </div>
                    </div>
                    <button :class="['chat-send-btn', { 'chat-send-btn--waiting': isWaitingResponse }]"
                            type="button"
                            @click="isWaitingResponse ? cancelMessage() : sendMessage()"
                            :disabled="!isWaitingResponse && !message.trim()">
                        <i class="ki-duotone ki-send fs-2">
                            <span class="path1"></span>
                            <span class="path2"></span>
                        </i>
                        <span>{{ isWaitingResponse ? '중단' : '전송' }}</span>
                    </button>
                </div>
            </div>
        </div>
    `
};
