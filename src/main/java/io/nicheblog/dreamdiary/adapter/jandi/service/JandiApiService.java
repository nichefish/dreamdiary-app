package io.nicheblog.dreamdiary.adapter.jandi.service;

import io.nicheblog.dreamdiary.adapter.jandi.JandiTopic;
import io.nicheblog.dreamdiary.adapter.jandi.model.JandiApiSndMsgDto;
import io.nicheblog.dreamdiary.adapter.jandi.model.JandiParam;
import org.springframework.http.HttpEntity;

/**
 * JandiApiService
 * <pre>
 *  API:: JANDI:: (incoming/outgoing) webhook 서비스 인터페이스
 * </pre>
 *
 * @author nichefish
 */
public interface JandiApiService {

    /**
     * API:: JANDI:: 잔디로 incoming webhook 전송
     *
     * @param jandiParam 잔디 파라미터
     */
    Boolean sendMsg(final JandiParam jandiParam) throws Exception;

    /**
     * API:: JANDI:: 잔디로 incoming webhook 전송
     *
     * @param trgetTopic: 메세지 발송 대상 토픽
     * @param msg: 메세지 (메세지로 기본 JandiMsg 객체 생성하여 주입)
     */
    Boolean sendMsg(final JandiTopic trgetTopic, final String msg) throws Exception;

    /**
     * API:: JANDI:: 잔디로 incoming webhook 전송
     *
     * @param trgetTopic: 메세지 발송 대상 토픽
     * @param jandiMsg: 외부에서 JandiMsg 객체를 직접 주입
     */
    Boolean sendMsg(final JandiTopic trgetTopic, final JandiApiSndMsgDto jandiMsg) throws Exception;

    /**
     * API:: JANDI:: 잔디로 incoming webhook 전송
     *
     * @param trgetTopic: 메세지 발송 대상 토픽
     * @param msg: 메세지
     * @param title: 글제목 (connectorInfo title)
     * @param url: url (connectorInfo contents)
     */
    Boolean sendMsg(final JandiTopic trgetTopic, final String msg, final String title, final String url) throws Exception;

    /**
     * API:: JANDI:: 대상 토픽에 따라 incoming webhook 요청 URL 세팅 (메소드 분리)
     *
     * @param trgetTopic: 메세지 발송 대상 토픽
     */
    String setRequestUrlParam(final JandiTopic trgetTopic);

    /**
     * API:: JANDI (incoming/outgoing)webhook :: 요청 헤더 세팅 (메소드 분리)
     *
     * @param jandiMsg: jandiMsg 객체
     */
    HttpEntity<JandiApiSndMsgDto> setRequestHeader(final JandiApiSndMsgDto jandiMsg) throws Exception;

}