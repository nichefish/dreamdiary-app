/**
 * ChatClient.ts
 *
 * @author nichefish
 */
export default {
    data() {
        return {
            stompClient: null,  // STOMP 클라이언트
            chatMessages: [],       // 채팅 메시지 배열
            message: '',        // 사용자 입력 메시지
            serverInfo: {       // 서버에서 가져올 프로필 정보
                domain: '',
                port: ''
            },
        };
    },
    methods: {
        // 서버 정보 조회
        async fetchServerInfo(): Promise<void> {
            try {
                const response: Response = await fetch('/cmm/getServerInfo.do');
                const data: Record<string, any> = await response.json();
                const rsltObj: Record<string, any> = data.rsltObj;
                this.serverInfo.domain = rsltObj.domain;
                this.serverInfo.port = rsltObj.port;
            } catch (error) {
                console.error('Error fetching profile:', error);
            }
        },
        
        // WebSocket 연결 설정
        async connectWebSocket(): Promise<void> {
            await this.fetchServerInfo();  // 서버에서 도메인 & 포트 정보 가져오기

            // @ts-ignore  // STOMP 클라이언트 생성
            const brokerUrl: string = `http://${this.serverInfo.domain}:${this.serverInfo.port}/chat`;
            // @ts-ignore
            this.stompClient = Stomp.client(brokerUrl);
            const successCallback = (): void => {
                // 메세지 구독
                this.subscribeToMessages();
                // 세션 만료 구독
                this.subscribeToSessionInvalid();
            };
            const errorCallback = (error: any): void => {
                console.error('WebSocket Error:', error);
            };
            // 연결 생성
            this.stompClient.connect({}, successCallback, errorCallback);
        },

        // 메시지 구독
        subscribeToMessages(): void {
            if (!this.stompClient || !this.stompClient.connected) return;

            // "/topic/chat"을 구독하여 메시지를 수신
            this.stompClient.subscribe('/topic/chat', (message: any): void => {
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
        subscribeToSessionInvalid(): void {
            if (!this.stompClient || !this.stompClient.connected) return;

            // 세션 만료 구독
            this.stompClient.subscribe('/topic/session-invalid', function(message: any): void {
                console.log(message.body); // "Your session has expired, please log in again."
                // 쿠키에서 JWT 토큰 삭제
                document.cookie = "jwtToken=; expires=Thu, 01 Jan 1970 00:00:00 GMT";
            });
        },

        // 메시지 전송
        sendMessage(message: string): void {
            if (!this.stompClient || !this.stompClient.connected) return;
            if (!message) return;

            this.stompClient.send('/app/chat/send', {}, message);
        },

        // DB에서 기존 메시지 로드 (페이지 로딩 시)
        loadMessages(): void {
            console.log("fetching messages...");
            fetch('/chat/messages')  // 기존 메시지 요청
                .then(response => response.json())
                .then(data => {
                    if (!data.rslt) {
                        console.error('Error loading messages:', data.msg);
                        cF.ui.swalOrAlert('error', 'Error loading messages:', data.msg);
                        return;
                    }
                    if (data.rsltList) this.$emit('messages-loaded', data.rsltList);  // 메시지 로딩 완료 후 상위에 전달
                })
                .catch(error => {
                    console.error('Error loading messages:', error);
                });
        },

        // 연결 종료
        disconnectWebSocket(): void {
            if (!this.stompClient) return;

            this.stompClient.deactivate();  // WebSocket 연결 종료
        },
    },
    mounted(): void {
        this.connectWebSocket();  // 컴포넌트가 마운트되면 자동으로 WebSocket 연결
        this.loadMessages();      // DB에서 메시지 로드. (이후에는 실시간 메세지 갱신)
    },
    beforeDestroy(): void {
        this.disconnectWebSocket();  // 컴포넌트가 파괴될 때 WebSocket 연결 종료
    },
};