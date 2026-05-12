package io.nicheblog.dreamdiary.infrastructure.messaging.mqtt.util;

import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * MqttUtils
 * Mqtt 관련 유틸리티 모듈
 *
 * @author nichefish
 */
@Component
@Log4j2
public class MqttUtils {

    /**
     * Mqtt 브로커 서버에 연결
     * @param serverUri String
     * @return IMqttClient 객체
     */
    public IMqttClient getConnection(final String serverUri) throws MqttException {
        // 기본 옵션 설정
        final MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        return getConnection(serverUri, options);
    }

    /**
     * Mqtt 브로커 서버에 연결
     * @param serverUri String
     * @param options MqttConnectOptions
     * @return IMqttClient 객체
     */
    public IMqttClient getConnection(final String serverUri, final MqttConnectOptions options) throws MqttException {
        final String clientId = UUID.randomUUID().toString();
        final IMqttClient client = new MqttClient(serverUri, clientId);
        client.connect(options);
        return client;
    }

    /**
     * Mqtt 브로커 서버에 연결
     * @param serverUri String
     * @param options MqttConnectOptions
     * @param getConnection Boolean
     * @return IMqttClient 객체
     */
    public IMqttClient getConnection(final String serverUri, final MqttConnectOptions options, final Boolean getConnection) throws MqttException {
        final String clientId = UUID.randomUUID().toString();
        IMqttClient client = new MqttClient(serverUri, clientId);
        if (getConnection) client.connect(options);
        return client;
    }

    /**
     * doConnect
     * @param client IMqttClient
     * @param options MqttConnectOptions
     */
    public void doConnect(final IMqttClient client, final MqttConnectOptions options) throws MqttException {
        client.connect(options);
    }

    /**
     * doConnect
     * @param client IMqttClient
     */
    public void doConnect(final IMqttClient client) throws MqttException {
        final MqttConnectOptions options = this.getDefaultOption();
        doConnect(client, options);
    }

    /**
     * Mqtt 기본 옵션 반환
     * TODO: 옵션이 뭐가 있는지 좀 더 자세히 보기
     */
    public MqttConnectOptions getDefaultOption() {
        final MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        return options;
    }

    /**
     * Mqtt 메세지 발행
     * @param client IMqttClient
     * @param topic String
     * @param message String
     */
    public void doPublish(final IMqttClient client, final String topic, final String message) throws Exception {
        if (!client.isConnected()) return;

        final MqttMessage msg = getMqttMessage(message);
        // TODO :: 옵션 살펴보기
        msg.setQos(0);
        msg.setRetained(true);
        client.publish(topic, msg);
    }

    /**
     * 문자열에서 바이트 페이로드 생성
     * @param message String
     */
    private MqttMessage getMqttMessage(final String message) {
        final byte[] payload = message.getBytes();
        return new MqttMessage(payload);
    }
}
