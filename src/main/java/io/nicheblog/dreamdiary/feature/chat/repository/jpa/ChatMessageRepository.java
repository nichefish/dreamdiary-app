package io.nicheblog.dreamdiary.feature.chat.repository.jpa;

import io.nicheblog.dreamdiary.feature.chat.entity.ChatMessageEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ChatMessageRepository
 * <pre>
 *  채팅 메시지 엔티티를 조회하고 저장하는 JPA Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface ChatMessageRepository
        extends BaseStreamRepository<ChatMessageEntity, Integer> {

    /**
     * 세션에 속한 모든 메시지를 대화 순서대로 조회한다.
     *
     * @param sessionId 조회할 채팅 세션 ID
     * @return 세션 메시지 엔티티 목록
     */
    List<ChatMessageEntity> findAllBySessionIdOrderBySeqAscCreatedAtAsc(Integer sessionId);

    /**
     * 세션에 속한 메시지를 최신순으로 페이지 조회한다.
     *
     * @param sessionId 조회할 채팅 세션 ID
     * @param pageable 조회 개수와 페이지 조건
     * @return 최신순 세션 메시지 엔티티 목록
     */
    List<ChatMessageEntity> findAllBySessionIdOrderBySeqDescCreatedAtDesc(Integer sessionId, Pageable pageable);

    /**
     * 세션 내 현재 최대 메시지 순번을 조회한다.
     *
     * @param sessionId 조회할 채팅 세션 ID
     * @return 세션 내 최대 메시지 순번, 메시지가 없으면 {@code 0}
     */
    @Query("SELECT COALESCE(MAX(message.seq), 0) FROM ChatMessageEntity message WHERE message.sessionId = :sessionId")
    Integer findMaxSeqBySessionId(final @Param("sessionId") Integer sessionId);
}
