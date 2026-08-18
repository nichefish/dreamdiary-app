package io.nicheblog.dreamdiary.feature.journal.setting.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.journal.setting.entity.JournalSettingEntity;
import io.nicheblog.dreamdiary.feature.journal.setting.model.JournalSettingDto;
import io.nicheblog.dreamdiary.feature.journal.setting.model.JournalUserSettingDto;
import io.nicheblog.dreamdiary.feature.journal.setting.repository.JournalSettingRepository;
import io.nicheblog.dreamdiary.feature.journal.setting.type.JournalDefaultEntryView;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * JournalSettingService
 * <pre>
 *  저널 도메인 설정 서비스.
 *  ADMIN/GLOBAL 전역 설정과 USER/username 사용자별 설정을 같은 범위 키 계약으로 관리한다.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalSettingService {

    private static final String SCOPE_ADMIN = "ADMIN";
    private static final String SCOPE_USER = "USER";
    private static final String SCOPE_KEY_GLOBAL = "GLOBAL";
    private static final JournalDefaultEntryView DEFAULT_ENTRY_VIEW = JournalDefaultEntryView.DAILY;

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
     * <p>전역 ADMIN/GLOBAL 행의 {@code embeddingEnabled}를 본다.
     * false이면 엔트리 적재, 전수 sync, 임베딩 워커를 건너뛴다.</p>
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
     * 로그인 사용자의 저널 설정을 조회한다.
     * 사용자 행이 없거나 기본 진입 화면이 비어 있으면 행을 생성하지 않고 DAILY 기본값을 반환한다.
     *
     * @return 로그인 사용자의 저널 설정 DTO
     */
    @Transactional(readOnly = true)
    public JournalUserSettingDto getMySetting() {
        final String username = AuthUtils.requireLoginUsername();
        return repository.findByScopeAndScopeKey(SCOPE_USER, username)
                .map(this::toUserDto)
                .orElseGet(() -> {
                    log.debug("Journal user setting default applied. username={}, defaultEntryView={}", username, DEFAULT_ENTRY_VIEW);
                    return defaultUserDto();
                });
    }

    /**
     * 로그인 사용자의 저널 기본 진입 화면을 저장한다.
     * 사용자 행은 최초 저장 시 생성하고 이후 같은 범위 키의 행을 갱신한다.
     *
     * @param dto 저장할 사용자 저널 설정
     * @return 저장된 사용자 저널 설정 DTO
     */
    @Transactional
    public JournalUserSettingDto updateMySetting(final JournalUserSettingDto dto) {
        final String username = AuthUtils.requireLoginUsername();
        final Optional<JournalSettingEntity> existing = repository.findByScopeAndScopeKey(SCOPE_USER, username);
        final JournalSettingEntity entity = existing
                .orElseGet(() -> JournalSettingEntity.builder()
                        .scope(SCOPE_USER)
                        .scopeKey(username)
                        .embeddingEnabled(true)
                        .createdBy(username)
                        .createdAt(LocalDateTime.now())
                        .build());
        entity.setDefaultEntryView(dto.getDefaultEntryView());
        entity.setUpdatedBy(username);
        entity.setUpdatedAt(LocalDateTime.now());
        final JournalSettingEntity saved = repository.save(entity);
        log.info("Journal user setting {}. username={}, defaultEntryView={}",
                existing.isPresent() ? "updated" : "created", username, saved.getDefaultEntryView());
        return toUserDto(saved);
    }

    /**
     * ADMIN/GLOBAL 설정 엔티티를 조회하거나, 없으면 기본값으로 생성한다.
     */
    private JournalSettingEntity getOrCreateAdminEntity() {
        return repository.findByScopeAndScopeKey(SCOPE_ADMIN, SCOPE_KEY_GLOBAL)
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

    private JournalUserSettingDto toUserDto(final JournalSettingEntity entity) {
        final JournalDefaultEntryView defaultEntryView = entity.getDefaultEntryView();
        if (defaultEntryView == null) {
            log.warn("Journal user setting has no default entry view. scopeKey={}, defaultEntryView={}",
                    entity.getScopeKey(), DEFAULT_ENTRY_VIEW);
            return defaultUserDto();
        }
        return JournalUserSettingDto.builder()
                .defaultEntryView(defaultEntryView)
                .build();
    }

    private JournalUserSettingDto defaultUserDto() {
        return JournalUserSettingDto.builder()
                .defaultEntryView(DEFAULT_ENTRY_VIEW)
                .build();
    }
}
