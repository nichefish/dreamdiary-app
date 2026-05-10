package io.nicheblog.dreamdiary.feature.chat.repository.jpa;

import io.nicheblog.dreamdiary.feature.chat.entity.ChatSettingEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ChatSettingRepository
 * <pre>
 *  AI 채팅 설정 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface ChatSettingRepository
        extends BaseStreamRepository<ChatSettingEntity, Integer> {

    Optional<ChatSettingEntity> findFirstByScopeAndScopeKeyOrderByCreatedAtDesc(String scope, String scopeKey);
}
