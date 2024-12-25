/**
 * App.js
 *
 * @author nichefish
 */
import EngageBtn from './components/EngageBtn.js';
import ChatWindow from './components/ChatWindow.js';
import ChatClient from './components/ChatClient.js';

document.addEventListener("DOMContentLoaded", function() {
    const app = Vue.createApp({
        components: {
            EngageBtn,
            ChatWindow,
            ChatClient
        },
        data() {
            return {
                isChatOpen: false,   // 채팅 창의 상태 관리
                chatMessages: []     // 채팅 메시지 배열
            };
        },
        methods: {
            // 채팅 창 열기/닫기
            toggleChat() {
                this.isChatOpen = !this.isChatOpen;
            },
            // 채팅 창 닫기
            closeChat() {
                this.isChatOpen = false;  // 채팅 창 닫기
            },
            // 메시지 로딩 후 완료 처리
            handleMessagesLoaded(messages) {
                this.chatMessages = messages;
                console.log("this.chatMessages:", this.chatMessages);
            },
            // 새 메시지 처리 (ChatClient에서 새 메시지를 받음)
            handleNewMessage(message) {
                console.log("message:", message);
                this.chatMessages.push(message);
                console.log("this.chatMessages:", this.chatMessages);
            },
            // ChatWindow :: 메시지 전송
            handleSendMessage(message) {
                this.$refs.chatClient.sendMessage(message);
            },
        },
        template: `
            <EngageBtn :isChatOpen="isChatOpen" @toggle-chat="toggleChat" />
            <ChatClient ref="chatClient" @new-message="handleNewMessage" @messages-loaded="handleMessagesLoaded" />
            <ChatWindow ref="chatWindow" :isChatOpen="isChatOpen" :chatMessages="chatMessages" @send-message="handleSendMessage" @close-chat="closeChat" />
        `
    });

    app.mount('#vue-app');
});