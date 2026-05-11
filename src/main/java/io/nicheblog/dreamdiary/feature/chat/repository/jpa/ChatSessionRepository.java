package io.nicheblog.dreamdiary.feature.chat.repository.jpa;

import io.nicheblog.dreamdiary.feature.chat.entity.ChatSessionEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ChatSessionRepository
 * <pre>
 *  AI 채팅 세션 엔티티를 조회하고 저장하는 JPA Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface ChatSessionRepository
        extends BaseStreamRepository<ChatSessionEntity, Integer> {

    /**
     * 특정 사용자가 생성한 세션을 최근 대화 시각 기준으로 조회한다.
     *
     * @param createdBy 세션 생성자 계정 ID
     * @return 사용자 채팅 세션 엔티티 목록
     */
    List<ChatSessionEntity> findAllByCreatedByOrderByLastMessageAtDescCreatedAtDesc(String createdBy);
}
