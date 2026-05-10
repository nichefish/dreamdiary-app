package io.nicheblog.dreamdiary.feature.chat.repository.jpa;

import io.nicheblog.dreamdiary.feature.chat.entity.ChatMessageEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ChatMessageRepository
 * <pre>
 *  채팅 메세지 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface ChatMessageRepository
        extends BaseStreamRepository<ChatMessageEntity, Integer> {

    List<ChatMessageEntity> findAllBySessionIdOrderBySeqAscCreatedAtAsc(Integer sessionId);

    List<ChatMessageEntity> findTop20BySessionIdOrderBySeqDescCreatedAtDesc(Integer sessionId);

    @Query("SELECT COALESCE(MAX(message.seq), 0) FROM ChatMessageEntity message WHERE message.sessionId = :sessionId")
    Integer findMaxSeqBySessionId(final @Param("sessionId") Integer sessionId);
}
