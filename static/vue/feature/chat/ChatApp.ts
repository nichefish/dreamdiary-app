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
                chatMessages: [],
                isWaitingResponse: false
            };
        },
        methods: {
            toggleChat(): void {
                this.isChatOpen = !this.isChatOpen;
                if (this.isChatOpen) {
                    this.$refs.chatWindow.scrollToBottom();
                }
            },
            closeChat(): void {
                this.isChatOpen = false;
            },
            handleMessagesLoaded(messages: any): void {
                this.chatMessages = messages;
            },
            isAssistantMessage(message: any): boolean {
                const role = (message?.role || '').toString().toUpperCase();
                return role === 'ASSISTANT' || role === 'AI' || role === 'SYSTEM';
            },
            handleNewMessage(message: any): void {
                this.chatMessages.push(message);
                if (this.isAssistantMessage(message) || message?.isCreatedBy === false) {
                    this.isWaitingResponse = false;
                }
                this.$refs.chatWindow.scrollToBottom();
            },
            handleSendMessage(message: any): void {
                if (this.isWaitingResponse) return;
                this.isWaitingResponse = true;
                this.$refs.chatClient.sendMessage(message);
            },
        },
        created(): void {
            this.authInfo = AuthInfo;
        },
        template: `
            <EngageBtn :isChatOpen="isChatOpen" @toggle-chat="toggleChat" />
            <ChatClient ref="chatClient" @new-message="handleNewMessage" @messages-loaded="handleMessagesLoaded" />
            <ChatWindow ref="chatWindow" :authInfo="authInfo" :isChatOpen="isChatOpen" :chatMessages="chatMessages" :isWaitingResponse="isWaitingResponse" @send-message="handleSendMessage" @close-chat="closeChat" />
        `
    });

    app.mount('#vue-app');
});
