package io.nicheblog.dreamdiary.infrastructure.notify.mqtt.util;

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
     * @param serverUri
     * @return IMqttClient 객체
     */
    public IMqttClient getConnection(final String serverUri) throws MqttException {
        // 기본 옵션 설정
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        return getConnection(serverUri, options);
    }

    /**
     * Mqtt 브로커 서버에 연결
     * @param serverUri
     * @param options
     * @return IMqttClient 객체
     */
    public IMqttClient getConnection(final String serverUri, final MqttConnectOptions options) throws MqttException {
        String clientId = UUID.randomUUID().toString();
        IMqttClient client = new MqttClient(serverUri, clientId);
        client.connect(options);
        return client;
    }

    /**
     * Mqtt 브로커 서버에 연결
     * @param serverUri
     * @param options
     * @param getConnection
     * @return IMqttClient 객체
     */
    public IMqttClient getConnection(final String serverUri, final MqttConnectOptions options, final Boolean getConnection) throws MqttException {
        String clientId = UUID.randomUUID().toString();
        IMqttClient client = new MqttClient(serverUri, clientId);
        if (getConnection) client.connect(options);
        return client;
    }

    /**
     * connection
     */
    public void doConnect(final IMqttClient client, final MqttConnectOptions options) throws MqttException {
        client.connect(options);
    }

    /**
     * connection
     */
    public void doConnect(final IMqttClient client) throws MqttException {
        MqttConnectOptions options = this.getDefaultOption();
        doConnect(client, options);
    }

    /**
     * Mqtt 기본 옵션 반환
     * TODO: 옵션이 뭐가 있는지 좀 더 자세히 보기
     */
    public MqttConnectOptions getDefaultOption() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        return options;
    }

    /**
     * Mqtt 메세지 발행
     */
    public void doPublish(final IMqttClient client, final String topic, final String message) throws Exception {
        if (!client.isConnected()) return;
        MqttMessage msg = getMqttMessage(message);
        // TODO :: 옵션 살펴보기
        msg.setQos(0);
        msg.setRetained(true);
        client.publish(topic, msg);
    }

    /** 문자열에서 바이트 페이로드 생성 */
    private MqttMessage getMqttMessage(final String message) {
        byte[] payload = message.getBytes();
        return new MqttMessage(payload);
    }
}
