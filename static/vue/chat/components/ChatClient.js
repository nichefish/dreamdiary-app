/**
 * ChatClient.js
 *
 * @author nichefish
 */
export default {
    data() {
        return {
            stompClient: null,  // STOMP 클라이언트
            chatMessages: [],       // 채팅 메시지 배열
            message: '',        // 사용자 입력 메시지
        };
    },
    methods: {
        // WebSocket 연결 설정
        connectWebSocket() {
            // STOMP 클라이언트 생성
            const brokerUrl = "http://localhost:18081/chat";
            this.stompClient = Stomp.client(brokerUrl);
            const successCallback = () => {
                // 메세지 구독
                this.subscribeToMessages();
                // 세션 만료 구독
                this.subscribeToSessionInvalid();
            };
            const errorCallback = (error) => {
                console.error('WebSocket Error:', error);
            };
            // 연결 생성
            this.stompClient.connect({}, successCallback, errorCallback);
        },

        // 메시지 구독
        subscribeToMessages() {
            if (!this.stompClient || !this.stompClient.connected) return;

            // "/topic/chat"을 구독하여 메시지를 수신
            this.stompClient.subscribe('/topic/chat', (message) => {
                console.log('Received Message:', message.body);
                if (!message.body) return;
                try {
                    const messageObject = JSON.parse(message.body); // 메시지를 JSON 객체로 파싱
                    this.$emit('new-message', messageObject.rsltObj);  // 메시지 로딩 완료 후 상위에 전달
                } catch (e) {
                    console.error('Error parsing message:', e);
                }
            });
        },

        // 로그아웃 감지
        subscribeToSessionInvalid() {
            if (!this.stompClient || !this.stompClient.connected) return;

            // 세션 만료 구독
            this.stompClient.subscribe('/topic/session-invalid', function(message) {
                console.log(message.body); // "Your session has expired, please log in again."
                // 쿠키에서 JWT 토큰 삭제
                document.cookie = "jwtToken=; expires=Thu, 01 Jan 1970 00:00:00 GMT";
            });
        },

        // 메시지 전송
        sendMessage(message) {
            if (!this.stompClient || !this.stompClient.connected) return;
            if (!message) return;

            this.stompClient.send('/app/chat/send', {}, message);
        },

        // DB에서 기존 메시지 로드 (페이지 로딩 시)
        loadMessages() {
            console.log("fetching messages...");
            fetch('/chat/messages')  // 기존 메시지 요청
                .then(response => response.json())
                .then(data => {
                    if (!data.rslt) {
                        console.error('Error loading messages:', data.msg);
                        cF.util.swalOrAlert('error', 'Error loading messages:', data.msg);
                        return;
                    }
                    if (data.rsltList) this.$emit('messages-loaded', data.rsltList);  // 메시지 로딩 완료 후 상위에 전달
                })
                .catch(error => {
                    console.error('Error loading messages:', error);
                });
        },

        // 연결 종료
        disconnectWebSocket() {
            if (!this.stompClient) return;

            this.stompClient.deactivate();  // WebSocket 연결 종료
        },
    },
    mounted() {
        this.connectWebSocket();  // 컴포넌트가 마운트되면 자동으로 WebSocket 연결
        this.loadMessages();      // DB에서 메시지 로드. (이후에는 실시간 메세지 갱신)
    },
    beforeDestroy() {
        this.disconnectWebSocket();  // 컴포넌트가 파괴될 때 WebSocket 연결 종료
    },
};