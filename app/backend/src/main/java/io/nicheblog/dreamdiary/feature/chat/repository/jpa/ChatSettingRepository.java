package io.nicheblog.dreamdiary.feature.chat.repository.jpa;

import io.nicheblog.dreamdiary.feature.chat.entity.ChatSettingEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ChatSettingRepository
 * <pre>
 *  AI 채팅 설정 엔티티를 조회하고 저장하는 JPA Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface ChatSettingRepository
        extends BaseStreamRepository<ChatSettingEntity, Integer> {

    /**
     * 설정 범위와 범위 키에 해당하는 최신 설정을 조회한다.
     *
     * @param scope 설정 범위
     * @param scopeKey 설정 범위 식별자
     * @return 조건에 맞는 최신 채팅 설정 엔티티
     */
    Optional<ChatSettingEntity> findFirstByScopeAndScopeKeyOrderByCreatedAtDesc(String scope, String scopeKey);
}
