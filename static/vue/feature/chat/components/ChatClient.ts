/**
 * ChatClient.ts
 *
 * @author nichefish
 */
export default {
    data() {
        return {
            stompClient: null,
            chatSubscription: null,
            sessionInvalidSubscription: null,
            serverInfo: {
                domain: '',
                port: ''
            },
        };
    },
    methods: {
        async fetchServerInfo(): Promise<void> {
            try {
                const response: Response = await fetch('/cmm/get-server-info.do');
                const data: Record<string, any> = await response.json();
                const rsltObj: Record<string, any> = data.rsltObj;
                this.serverInfo.domain = rsltObj.domain;
                this.serverInfo.port = rsltObj.port;
            } catch (error) {
                console.error('Error fetching profile:', error);
            }
        },
        async connectWebSocket(): Promise<void> {
            await this.fetchServerInfo();

            // @ts-ignore
            const brokerUrl: string = `http://${this.serverInfo.domain}:${this.serverInfo.port}/chat`;
            // @ts-ignore
            this.stompClient = Stomp.client(brokerUrl);
            const successCallback = (): void => {
                this.subscribeToSessionInvalid();
                this.$emit('connected');
            };
            const errorCallback = (error: any): void => {
                console.error('WebSocket Error:', error);
            };

            this.stompClient.connect({}, successCallback, errorCallback);
        },
        subscribeToSession(sessionId: number): void {
            if (!this.stompClient || !this.stompClient.connected || !sessionId) return;
            if (this.chatSubscription) this.chatSubscription.unsubscribe();

            this.chatSubscription = this.stompClient.subscribe(`/topic/chat/session/${sessionId}`, (message: any): void => {
                if (!message.body) return;
                try {
                    const messageObject = JSON.parse(message.body);
                    this.$emit('new-message', messageObject.rsltObj);
                } catch (e) {
                    console.error('Error parsing message:', e);
                }
            });
        },
        subscribeToSessionInvalid(): void {
            if (!this.stompClient || !this.stompClient.connected || this.sessionInvalidSubscription) return;

            this.sessionInvalidSubscription = this.stompClient.subscribe('/topic/session-invalid', function(): void {
                document.cookie = "jwtToken=; expires=Thu, 01 Jan 1970 00:00:00 GMT";
            });
        },
        async loadSessions(): Promise<any[]> {
            const response = await fetch('/chat/sessions');
            const data = await response.json();
            if (!data.rslt) {
                cF.ui.swalOrAlert('error', 'Error loading chat sessions:', data.msg);
                return [];
            }
            const sessions = data.rsltList || [];
            this.$emit('sessions-loaded', sessions);
            return sessions;
        },
        async loadSetting(): Promise<any> {
            const response = await fetch('/chat/settings');
            const data = await response.json();
            if (!data.rslt) {
                cF.ui.swalOrAlert('error', 'Error loading chat setting:', data.msg);
                return null;
            }
            this.$emit('setting-loaded', data.rsltObj);
            return data.rsltObj;
        },
        async updateSetting(setting: any): Promise<any> {
            const response = await fetch('/chat/settings', {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(setting || {})
            });
            const data = await response.json();
            if (!data.rslt) {
                cF.ui.swalOrAlert('error', 'Error saving chat setting:', data.msg);
                return null;
            }
            this.$emit('setting-updated', data.rsltObj);
            return data.rsltObj;
        },
        async createSession(): Promise<any> {
            const response = await fetch('/chat/sessions', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({})
            });
            const data = await response.json();
            if (!data.rslt) {
                cF.ui.swalOrAlert('error', 'Error creating chat session:', data.msg);
                return null;
            }
            this.$emit('session-created', data.rsltObj);
            return data.rsltObj;
        },
        async deleteSession(sessionId: number): Promise<boolean> {
            const response = await fetch(`/chat/sessions/${sessionId}`, {
                method: 'DELETE'
            });
            const data = await response.json();
            if (!data.rslt) {
                cF.ui.swalOrAlert('error', 'Error deleting chat session:', data.msg);
                return false;
            }
            this.$emit('session-deleted', sessionId);
            return true;
        },
        async loadMessages(sessionId: number): Promise<void> {
            if (!sessionId) {
                this.$emit('messages-loaded', []);
                return;
            }

            fetch(`/chat/sessions/${sessionId}/messages`)
                .then(response => response.json())
                .then(data => {
                    if (!data.rslt) {
                        console.error('Error loading messages:', data.msg);
                        cF.ui.swalOrAlert('error', 'Error loading messages:', data.msg);
                        return;
                    }
                    this.$emit('messages-loaded', data.rsltList || []);
                })
                .catch(error => {
                    console.error('Error loading messages:', error);
                });
        },
        sendMessage(sessionId: number, message: string): void {
            if (!this.stompClient || !this.stompClient.connected) return;
            if (!sessionId || !message) return;

            this.stompClient.send(`/app/chat/session/${sessionId}/send`, {}, message);
        },
        disconnectWebSocket(): void {
            if (this.chatSubscription) this.chatSubscription.unsubscribe();
            if (this.sessionInvalidSubscription) this.sessionInvalidSubscription.unsubscribe();
            if (!this.stompClient) return;

            if (typeof this.stompClient.deactivate === 'function') {
                this.stompClient.deactivate();
            } else if (typeof this.stompClient.disconnect === 'function') {
                this.stompClient.disconnect();
            }
        },
    },
    mounted(): void {
        this.connectWebSocket();
        this.loadSetting();
        this.loadSessions();
    },
    beforeDestroy(): void {
        this.disconnectWebSocket();
    },
    beforeUnmount(): void {
        this.disconnectWebSocket();
    },

    template: `<div style="display:none;"></div>`
};
