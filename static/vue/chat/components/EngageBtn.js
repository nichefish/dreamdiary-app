/**
 * EngageBtn.js
 *
 * @author nichefish
 */
export default {
    name: "EngageBtn",
    props: {
        // 채팅 열기/닫기 상태를 상위 컴포넌트로부터 받습니다.
        isChatOpen: {
            type: Boolean,
            default: false
        }
    },
    methods: {
        toggleChat() {
            // 채팅을 열거나 닫는 동작
            this.$emit("toggle-chat"); // 상위 컴포넌트에 이벤트 전달
        }
    },
    template: `
        <div class="app-chat-engage">
            <a href="javascript:void(0);" class="app-chat-engage-btn hover-success" @click="toggleChat">
                <i class="ki-duotone ki-messages fs-1 pt-1 mb-2">
                    <span class="path1"></span>
                    <span class="path2"></span>
                    <span class="path3"></span>
                    <span class="path4"></span>
                    <span class="path5"></span>
                </i>
                <span class="bullet bullet-dot bg-success h-6px w-6px position-absolute translate-middle animation-blink"></span>
                Chat
            </a>
        </div>
    `,
    computed: {
        // 클릭 시 채팅 상태를 반영하는 computed 속성
        chatStatus() {
            return this.isChatOpen ? "Chat Opened" : "Chat Closed";
        }
    },
};