package io.nicheblog.dreamdiary.adapter.jandi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nicheblog.dreamdiary.adapter.jandi.JandiProperty;
import io.nicheblog.dreamdiary.adapter.jandi.JandiTopic;
import io.nicheblog.dreamdiary.adapter.jandi.model.JandiApiRespnsDto;
import io.nicheblog.dreamdiary.adapter.jandi.model.JandiApiSndMsgConnectInfoDto;
import io.nicheblog.dreamdiary.adapter.jandi.model.JandiApiSndMsgDto;
import io.nicheblog.dreamdiary.adapter.jandi.model.JandiParam;
import io.nicheblog.dreamdiary.adapter.jandi.service.JandiApiService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.config.HttpClientConfig;
import io.nicheblog.dreamdiary.global.util.JsonRestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * JandiApiService
 * <pre>
 *  API:: JANDI:: (incoming/outgoing) webhook 서비스 모듈
 * </pre>
 *
 * @author nichefish
 */
@Service("jandiApiService")
@RequiredArgsConstructor
@Log4j2
public class JandiApiServiceImpl
        implements JandiApiService {

    private final JandiProperty jandiProperty;

    /**
     * API:: JANDI:: 잔디로 incoming webhook 전송
     *
     * @param jandiParam 잔디 파라미터
     */
    @Override
    public Boolean sendMsg(final JandiParam jandiParam) throws Exception {
        JandiTopic topic = jandiParam.getTrgetTopic();
        return this.sendMsg(topic, jandiParam.getMsg());
    }

    /**
     * API:: JANDI:: 잔디로 incoming webhook 전송
     *
     * @param trgetTopic: 메세지 발송 대상 토픽
     * @param msg: 메세지 (메세지로 기본 JandiMsg 객체 생성하여 주입)
     */
    @Override
    public Boolean sendMsg(final JandiTopic trgetTopic, final String msg) throws Exception {
        JandiApiSndMsgDto jandiMsg = new JandiApiSndMsgDto(msg);
        return this.sendMsg(trgetTopic, jandiMsg);
    }

    /**
     * API:: JANDI:: 잔디로 incoming webhook 전송
     *
     * @param trgetTopic: 메세지 발송 대상 토픽
     * @param jandiMsg: 외부에서 JandiMsg 객체를 직접 주입
     */
    @Override
    public Boolean sendMsg(
            final JandiTopic trgetTopic,
            final JandiApiSndMsgDto jandiMsg
    ) throws Exception {

        // https로 연결시 SSL/TLS 인증서 필요 :: SSL인증서 무시하도록 생성
        // POST 전송시 URI_ENCODING 끄도록 생성자에서 설정
        JsonRestTemplate restTemplate = new JsonRestTemplate(HttpClientConfig.trustRequestFactory(), Constant.URL_ENC_FALSE);

        // parameter로 요청 URL 세팅 (메소드 분리)
        String postUrl = this.setRequestUrlParam(trgetTopic);
        log.debug("postUrl: {}", postUrl);
        if (postUrl == null) return false;
        // 헤더 세팅 :: 메소드 분리
        HttpEntity<JandiApiSndMsgDto> httpEntity = this.setRequestHeader(jandiMsg);
        log.debug("httpEntity: {}", httpEntity);

        ResponseEntity<JandiApiRespnsDto> response = restTemplate.exchange(postUrl, HttpMethod.POST, httpEntity, JandiApiRespnsDto.class);
        log.debug(response);

        return true;
    }

    /**
     * API:: JANDI:: 잔디로 incoming webhook 전송
     *
     * @param trgetTopic: 메세지 발송 대상 토픽
     * @param msg: 메세지
     * @param title: 글제목 (connectorInfo title)
     * @param url: url (connectorInfo contents)
     */
    @Override
    public Boolean sendMsg(
            final JandiTopic trgetTopic,
            final String msg,
            final String title,
            final String url
    ) throws Exception {
        JandiApiSndMsgDto jandiMsg = new JandiApiSndMsgDto(msg);
        jandiMsg.addConnMsg(new JandiApiSndMsgConnectInfoDto(title, url, null));
        return this.sendMsg(trgetTopic, jandiMsg);
    }

    /**
     * API:: JANDI:: 대상 토픽에 따라 incoming webhook 요청 URL 세팅 (메소드 분리)
     *
     * @param trgetTopic: 메세지 발송 대상 토픽
     */
    @Override
    public String setRequestUrlParam(final JandiTopic trgetTopic) {
        final String trgetTopicNm = trgetTopic.name();
        return Url.JANDI_CONNECT_WH + "/" + jandiProperty.getTeamId() + "/" + jandiProperty.getId(trgetTopicNm);
    }

    /**
     * API:: JANDI (incoming/outgoing)webhook :: 요청 헤더 세팅 (메소드 분리)
     *
     * @param jandiMsg: jandiMsg 객체
     */
    @Override
    public HttpEntity<JandiApiSndMsgDto> setRequestHeader(final JandiApiSndMsgDto jandiMsg) throws Exception {
        log.debug("jandiMsg: {}", jandiMsg);
        String jsonStr = new ObjectMapper().writeValueAsString(jandiMsg);
        log.debug("jsonStr: {}", jsonStr);

        final HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.tosslab.jandi-v2+json");
        headers.set("Content-Type", "application/json; charset=utf-8");

        return new HttpEntity<>(jandiMsg, headers);
    }

}