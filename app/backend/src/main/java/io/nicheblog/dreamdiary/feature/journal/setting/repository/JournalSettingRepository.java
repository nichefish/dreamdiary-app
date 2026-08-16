package io.nicheblog.dreamdiary.feature.journal.setting.repository;

import io.nicheblog.dreamdiary.feature.journal.setting.entity.JournalSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JournalSettingRepository
 * <pre>
 *  저널 설정 Repository.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface JournalSettingRepository
        extends JpaRepository<JournalSettingEntity, Integer> {

    /**
     * scope + scopeKey 로 설정 row 조회.
     *
     * @param scope 설정 범위
     * @param scopeKey 범위 키
     * @return 설정 엔티티
     */
    Optional<JournalSettingEntity> findByScopeAndScopeKey(String scope, String scopeKey);
}
