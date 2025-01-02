/**
 * mqttws31.ts
 * @namespace: cF.mqttws31
 * @author: nichefish
 * 공통 - mqtt 웹소켓 클라이언트 함수 모듈
 * (노출식 모듈 패턴 적용 :: cF.util.enterKey("#userId") 이런식으로 사용)
 */
if (typeof cF === 'undefined') { let cF = {}; }
cF.mqttws31 = (function() {
    return {
        /**
         * MQTT 연결을 시작하고 MQTT 클라이언트 인스턴스를 반환합니다.
         * @param {string} componentId - MQTT 통신을 위한 컴포넌트 ID.
         * @param {object} options - MQTT 클라이언트 설정 옵션.
         * @param {function} onSuccFunc - 연결 성공시 호출할 콜백 함수.
         * @returns {object} mqttClient - 생성된 MQTT 클라이언트 인스턴스.
         */
        start: function(componentId: string, options: object, onSuccFunc: Function) {
            // MQTT 클라이언트 생성 :: 메소드 분리
            const mqttClient = cF.mqttws31.initClient(componentId, options);
            // MQTT 연결 시도 및 콜백 함수 등록 :: 메소드 분리
            cF.mqttws31.connect(mqttClient, options, onSuccFunc);
            return mqttClient;
        },

        /**
         * MQTT 클라이언트 객체를 생성하고 반환합니다.
         * @param {string} componentId - MQTT 통신을 위한 컴포넌트 ID.
         * @param {object} options - MQTT 클라이언트 설정 옵션.
         * @returns {Paho.MQTT.Client} mqttClient - 생성된 MQTT 클라이언트 인스턴스.
         */
        initClient: function(componentId: string, options: object) {
            const mqttHost = options.host;
            const mqttPort = options.port;
            const mqttClientId = options.title + "_client_" + componentId + Math.floor(Math.random() * 10000000);

            const mqttClient = new Paho.MQTT.Client(mqttHost, mqttPort, mqttClientId);

            // 연결이 끊어졌을 때의 재시도 로직
            mqttClient.onConnectionLost = function() {
                // 연결 재시도
                options.connRetryCnt = (options.connRetryCnt || 0) + 1;
                if (options.connRetryCnt < 5) {
                    if (options.reconnTimer) clearTimeout(options.reconnTimer);
                    options.reconnTimer = setTimeout(function() {
                        cF.mqttws31.connect(mqttClient)
                    }, options.reconnTimeout);
                }
            }

            // 메시지 수신 시 콜백 설정
            if (typeof options.fnMqttMsg === 'function') {
                mqttClient.onMessageArrived = function(message, options: object) {
                    options.fnMqttMsg(message);
                }
            }

            return mqttClient;
        },

        /**
         * MQTT 클라이언트를 지정된 옵션으로 연결하고, 성공시 콜백 함수를 실행합니다.
         * @param {Paho.MQTT.Client} mqttClient - MQTT 클라이언트 인스턴스.
         * @param {object} options - 연결 설정 옵션.
         * @param {function} onSuccFunc - 연결 성공시 호출할 콜백 함수.
         */
        connect: function(mqttClient, options = {}, onSuccFunc: Function) {
            const isLogging = options.logging || false;
            const topics = Array.isArray(options.topics) ? options.topics : [];

            mqttClient.connect({
                userName: options.userName,
                password: options.password,
                timeout: 5,
                useSSL: options.useSSL || false,
                keepAliveInterval: 30,

                // 연결 성공시
                onSuccess: function() {
                    if (isLogging) console.log(mqttClient.clientId + " // connect: onConnect...");

                    if (topics.length > 0) {
                        topics.forEach(function(topic) {
                            mqttClient.subscribe(topic);
                        });
                    }
                    if (typeof onSuccFunc === 'function') onSuccFunc();
                },

                // 연결 실패 시
                onFailure: function() {
                    if (isLogging) console.log(mqttClient.clientId + " // connect: onFailure...");
                }
            });
        },

        /**
         * MQTT 클라이언트에 메시지를 발행하고, 옵션에 따라 로그를 출력합니다.
         * @param {Paho.MQTT.Client} mqttClient - MQTT 클라이언트 인스턴스.
         * @param {object} options - 발행 옵션 (logging 설정 포함).
         * @param {string} topic - 메시지를 발행할 MQTT 주제.
         * @param {string} payloadStr - 발행할 메시지 내용.
         */
        publish: function(mqttClient, options: object, topic: string, payloadStr: string) {
            const isLogging = options.logging || false;

            try {
                // MQTT 메시지 생성 및 발행
                const message = new Paho.MQTT.Message(payloadStr);
                message.destinationName = topic;
                mqttClient.send(message);

                if (isLogging) console.log(mqttClient.clientId + " // message sent:: " + topic + " // " + payloadStr);
            } catch (error) {
                console.error("Failed to publish message:", error);
                if (isLogging) console.log(mqttClient.clientId + " // message publish failed: " + error.message);
            }
        }
    }
})();
