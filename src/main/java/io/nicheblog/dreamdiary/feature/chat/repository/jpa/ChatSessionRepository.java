package io.nicheblog.dreamdiary.feature.chat.repository.jpa;

import io.nicheblog.dreamdiary.feature.chat.entity.ChatSessionEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ChatSessionRepository
 * <pre>
 *  AI 채팅 세션 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface ChatSessionRepository
        extends BaseStreamRepository<ChatSessionEntity, Integer> {

    List<ChatSessionEntity> findAllByCreatedByOrderByLastMessageAtDescCreatedAtDesc(String createdBy);
}
