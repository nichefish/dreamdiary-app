package io.nicheblog.dreamdiary.feature.chat.service;

import io.nicheblog.dreamdiary.feature.attachable._shared.service.BaseAttachableService;
import io.nicheblog.dreamdiary.feature.chat.entity.ChatMessageEntity;
import io.nicheblog.dreamdiary.feature.chat.mapstruct.ChatMessageMapstruct;
import io.nicheblog.dreamdiary.feature.chat.model.ChatMessageDto;
import io.nicheblog.dreamdiary.feature.chat.repository.jpa.ChatMessageRepository;
import io.nicheblog.dreamdiary.feature.chat.spec.ChatMessageSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ChatMessageService
 * <pre>
 *  채팅 메시지 저장, 조회, AI 맥락 추출을 담당하는 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class ChatMessageService
        implements BaseAttachableService<ChatMessageDto, ChatMessageDto, Integer, ChatMessageEntity> {

    @Getter
    private final ChatMessageRepository repository;
    @Getter
    private final ChatMessageSpec spec;
    @Getter
    private final ChatMessageMapstruct mapstruct = ChatMessageMapstruct.INSTANCE;

    /**
     * 조회용 MapStruct 매퍼를 반환한다.
     *
     * @return 채팅 메시지 조회 변환 매퍼
     */
    public ChatMessageMapstruct getReadMapstruct() {
        return this.mapstruct;
    }

    /**
     * 저장용 MapStruct 매퍼를 반환한다.
     *
     * @return 채팅 메시지 저장 변환 매퍼
     */
    public ChatMessageMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    /**
     * 특정 세션의 전체 메시지를 대화 순서대로 조회한다.
     *
     * @param sessionId 조회할 채팅 세션 ID
     * @return 세션에 속한 채팅 메시지 DTO 목록
     * @throws Exception 엔티티를 DTO로 변환하는 중 예외가 발생한 경우
     */
    @Transactional(readOnly = true)
    public List<ChatMessageDto> getSessionMessages(final Integer sessionId) throws Exception {
        return this.listEntityToDto(repository.findAllBySessionIdOrderBySeqAscCreatedAtAsc(sessionId));
    }

    /**
     * AI 프롬프트 맥락에 사용할 최근 메시지를 시간순으로 조회한다.
     *
     * @param sessionId 조회할 채팅 세션 ID
     * @param recentMessageLimit 맥락에 포함할 최근 메시지 최대 개수
     * @return 비어 있지 않은 최근 메시지 DTO 목록
     * @throws Exception 엔티티를 DTO로 변환하는 중 예외가 발생한 경우
     */
    @Transactional(readOnly = true)
    public List<ChatMessageDto> getRecentContextMessages(final Integer sessionId, final Integer recentMessageLimit) throws Exception {
        final int limit = normalizeRecentMessageLimit(recentMessageLimit);
        final List<ChatMessageEntity> entityList = repository.findAllBySessionIdOrderBySeqDescCreatedAtDesc(sessionId, PageRequest.of(0, limit));
        Collections.reverse(entityList);

        return this.listEntityToDto(entityList).stream()
                .filter(message -> message.getContent() != null && !message.getContent().trim().isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 세션 내 다음 메시지 순번을 계산한다.
     *
     * @param sessionId 채팅 세션 ID
     * @return 다음 메시지 순번
     */
    @Transactional(readOnly = true)
    public Integer getNextSeq(final Integer sessionId) {
        return repository.findMaxSeqBySessionId(sessionId) + 1;
    }

    /**
     * 최근 메시지 조회 개수를 허용 범위 안으로 보정한다.
     *
     * @param recentMessageLimit 사용자 또는 관리자 설정값
     * @return 보정된 최근 메시지 조회 개수
     */
    private int normalizeRecentMessageLimit(final Integer recentMessageLimit) {
        if (recentMessageLimit == null) return 50;
        return Math.max(2, Math.min(200, recentMessageLimit));
    }
}
