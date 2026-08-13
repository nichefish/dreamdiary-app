package io.nicheblog.dreamdiary.feature.journal.setting.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.journal.setting.entity.JournalSettingEntity;
import io.nicheblog.dreamdiary.feature.journal.setting.model.JournalSettingDto;
import io.nicheblog.dreamdiary.feature.journal.setting.repository.JournalSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * JournalSettingService
 * <pre>
 *  저널 도메인 설정 서비스. ADMIN/GLOBAL 단일 행 기반.
 *  TODO: 멀티유저 시 scope=USER, scope_key=username row 추가 + ADMIN 기본값 위에 USER override 로직 확장
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalSettingService {

    private static final String SCOPE_ADMIN = "ADMIN";
    private static final String SCOPE_KEY_GLOBAL = "GLOBAL";

    private final JournalSettingRepository repository;

    /**
     * 전역 설정을 조회한다. 없으면 기본값으로 생성한다.
     *
     * @return 전역 설정 DTO
     */
    @Transactional(readOnly = true)
    public JournalSettingDto getAdminSetting() {
        return toDto(getOrCreateAdminEntity());
    }

    /**
     * AI 임베딩이 활성화되어 있는지 반환한다.
     *
     * @return true=활성, false=비활성
     */
    public boolean isEmbeddingEnabled() {
        final JournalSettingEntity entity = getOrCreateAdminEntity();
        return Boolean.TRUE.equals(entity.getEmbeddingEnabled());
    }

    /**
     * 전역 설정을 갱신한다.
     *
     * @param dto 갱신할 설정 DTO
     * @return 갱신된 설정 DTO
     */
    @Transactional
    public JournalSettingDto updateAdminSetting(final JournalSettingDto dto) {
        final JournalSettingEntity entity = getOrCreateAdminEntity();
        if (dto.getEmbeddingEnabled() != null) {
            entity.setEmbeddingEnabled(dto.getEmbeddingEnabled());
        }
        entity.setUpdatedBy(AuthUtils.getLoginUsernameOrDefault());
        entity.setUpdatedAt(LocalDateTime.now());
        final JournalSettingEntity saved = repository.save(entity);
        return toDto(saved);
    }

    /**
     * ADMIN/GLOBAL 설정 엔티티를 조회하거나, 없으면 기본값으로 생성한다.
     */
    private JournalSettingEntity getOrCreateAdminEntity() {
        return repository.findFirstByScopeAndScopeKey(SCOPE_ADMIN, SCOPE_KEY_GLOBAL)
                .orElseGet(() -> {
                    final JournalSettingEntity newEntity = JournalSettingEntity.builder()
                            .scope(SCOPE_ADMIN)
                            .scopeKey(SCOPE_KEY_GLOBAL)
                            .embeddingEnabled(true)
                            .createdBy("system")
                            .createdAt(LocalDateTime.now())
                            .build();
                    return repository.save(newEntity);
                });
    }

    private JournalSettingDto toDto(final JournalSettingEntity entity) {
        return JournalSettingDto.builder()
                .embeddingEnabled(entity.getEmbeddingEnabled())
                .build();
    }
}
