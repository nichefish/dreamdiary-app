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
                authInfo: null,      // 사용자 인증 정보
                isChatOpen: false,   // 채팅 창의 상태 관리
                chatMessages: []     // 채팅 메시지 배열
            };
        },
        methods: {
            // 사용자 인증 정보 로드
            async loadAuthInfo() {
                const authUrl = '/api/auth/getAuthInfo.do';
                try {
                    const response = await fetch(authUrl);
                    if (!response.ok) throw new Error("Failed to fetch authInfo");
                    const res = await response.json();
                    this.authInfo = res.rsltObj;
                    console.log("authInfo:", this.authInfo);
                } catch (error) {
                    console.error("Error loading auth info:", error);
                }
            },
            // 채팅 창 열기/닫기
            toggleChat() {
                this.isChatOpen = !this.isChatOpen;
                if (this.isChatOpen) {
                    this.$refs.chatWindow.scrollToBottom(); // 채팅 창 열릴 때 스크롤 이동
                }
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
                this.$refs.chatWindow.scrollToBottom();
            },
            // ChatWindow :: 메시지 전송
            handleSendMessage(message) {
                this.$refs.chatClient.sendMessage(message);
            },
        },
        created() {
            // 앱 시작 시 authInfo 로드
            this.loadAuthInfo();
        },
        template: `
            <EngageBtn :isChatOpen="isChatOpen" @toggle-chat="toggleChat" />
            <ChatClient ref="chatClient" @new-message="handleNewMessage" @messages-loaded="handleMessagesLoaded" />
            <ChatWindow ref="chatWindow" :authInfo="authInfo" :isChatOpen="isChatOpen" :chatMessages="chatMessages" @send-message="handleSendMessage" @close-chat="closeChat" />
        `
    });

    app.mount('#vue-app');
});