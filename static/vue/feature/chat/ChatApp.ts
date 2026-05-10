/**
 * ChatApp.ts
 *
 * @author nichefish
 */
import EngageBtn from './components/EngageBtn.js';
import ChatWindow from './components/ChatWindow.js';
import ChatClient from './components/ChatClient.js';

document.addEventListener("DOMContentLoaded", function(): void {
    const app = Vue.createApp({
        components: {
            EngageBtn,
            ChatWindow,
            ChatClient
        },
        data() {
            return {
                authInfo: null,
                isChatOpen: false,
                isWaitingResponse: false,
                isSessionLoading: false,
                sessions: [],
                activeSessionId: null,
                chatMessages: []
            };
        },
        methods: {
            toggleChat(): void {
                this.isChatOpen = !this.isChatOpen;
                if (this.isChatOpen) {
                    this.ensureActiveSession();
                    this.$refs.chatWindow.scrollToBottom();
                }
            },
            closeChat(): void {
                this.isChatOpen = false;
            },
            async ensureActiveSession(): Promise<void> {
                if (this.activeSessionId) return;
                if (this.sessions.length > 0) {
                    this.selectSession(this.sessions[0].id);
                    return;
                }

                await this.createSession();
            },
            async createSession(): Promise<any> {
                this.isSessionLoading = true;
                try {
                    const session = await this.$refs.chatClient.createSession();
                    if (!session) return null;

                    this.sessions = [session, ...this.sessions.filter(item => item.id !== session.id)];
                    this.selectSession(session.id);
                    return session;
                } finally {
                    this.isSessionLoading = false;
                }
            },
            selectSession(sessionId: number): void {
                if (!sessionId || this.activeSessionId === sessionId) return;

                this.activeSessionId = sessionId;
                this.isWaitingResponse = false;
                this.chatMessages = [];
                this.$refs.chatClient.subscribeToSession(sessionId);
                this.$refs.chatClient.loadMessages(sessionId);
            },
            handleConnected(): void {
                if (this.activeSessionId) {
                    this.$refs.chatClient.subscribeToSession(this.activeSessionId);
                }
            },
            handleSessionsLoaded(sessions: any[]): void {
                this.sessions = sessions || [];
                if (!this.activeSessionId && this.sessions.length > 0) {
                    this.selectSession(this.sessions[0].id);
                }
            },
            handleSessionCreated(session: any): void {
                if (!session) return;
                if (this.sessions.some(item => item.id === session.id)) return;
                this.sessions = [session, ...this.sessions];
            },
            handleSessionDeleted(sessionId: number): void {
                this.sessions = this.sessions.filter(item => item.id !== sessionId);
                if (this.activeSessionId !== sessionId) return;

                this.activeSessionId = null;
                this.chatMessages = [];
                if (this.sessions.length > 0) this.selectSession(this.sessions[0].id);
            },
            async deleteSession(sessionId: number): Promise<void> {
                if (!sessionId || this.isSessionLoading) return;

                this.isSessionLoading = true;
                try {
                    await this.$refs.chatClient.deleteSession(sessionId);
                } finally {
                    this.isSessionLoading = false;
                }
            },
            handleMessagesLoaded(messages: any[]): void {
                this.chatMessages = messages || [];
                this.$refs.chatWindow.scrollToBottom();
            },
            isAssistantMessage(message: any): boolean {
                const role = (message?.role || '').toString().toUpperCase();
                return role === 'ASSISTANT' || role === 'AI' || role === 'SYSTEM';
            },
            handleNewMessage(message: any): void {
                if (!message || message.sessionId !== this.activeSessionId) return;

                this.chatMessages.push(message);
                this.bumpActiveSession(message);
                if (this.isAssistantMessage(message) || message?.isCreatedBy === false) {
                    this.isWaitingResponse = false;
                }
                this.$refs.chatWindow.scrollToBottom();
            },
            bumpActiveSession(message: any): void {
                const session = this.sessions.find(item => item.id === this.activeSessionId);
                if (!session) return;

                session.lastMessageAt = message.createdAt || session.lastMessageAt;
                if (session.title === '새 대화' && message.role === 'USER' && message.content) {
                    const compact = message.content.replace(/\s+/g, ' ').trim();
                    session.title = compact.length > 28 ? compact.substring(0, 28) + '...' : compact;
                }
                this.sessions = [session, ...this.sessions.filter(item => item.id !== session.id)];
            },
            async handleSendMessage(message: string): Promise<void> {
                if (this.isWaitingResponse) return;
                if (!this.activeSessionId) await this.ensureActiveSession();
                if (!this.activeSessionId) return;

                this.isWaitingResponse = true;
                this.$refs.chatClient.sendMessage(this.activeSessionId, message);
            },
        },
        created(): void {
            this.authInfo = AuthInfo;
        },
        template: `
            <EngageBtn :isChatOpen="isChatOpen" @toggle-chat="toggleChat" />
            <ChatClient ref="chatClient"
                        @connected="handleConnected"
                        @new-message="handleNewMessage"
                        @messages-loaded="handleMessagesLoaded"
                        @sessions-loaded="handleSessionsLoaded"
                        @session-created="handleSessionCreated"
                        @session-deleted="handleSessionDeleted" />
            <ChatWindow ref="chatWindow"
                        :authInfo="authInfo"
                        :isChatOpen="isChatOpen"
                        :sessions="sessions"
                        :activeSessionId="activeSessionId"
                        :isSessionLoading="isSessionLoading"
                        :chatMessages="chatMessages"
                        :isWaitingResponse="isWaitingResponse"
                        @new-session="createSession"
                        @select-session="selectSession"
                        @delete-session="deleteSession"
                        @send-message="handleSendMessage"
                        @close-chat="closeChat" />
        `
    });

    app.mount('#vue-app');
});
