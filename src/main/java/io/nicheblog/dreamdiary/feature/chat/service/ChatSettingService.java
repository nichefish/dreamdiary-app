package io.nicheblog.dreamdiary.feature.chat.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.chat.entity.ChatSettingEntity;
import io.nicheblog.dreamdiary.feature.chat.model.ChatSettingDto;
import io.nicheblog.dreamdiary.feature.chat.repository.jpa.ChatSettingRepository;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * ChatSettingService
 * <pre>
 *  AI 채팅 설정 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class ChatSettingService {

    private static final String SCOPE_USER = "USER";
    private static final String SCOPE_ADMIN = "ADMIN";
    private static final String ADMIN_SCOPE_KEY = "GLOBAL";
    private static final int DEFAULT_RECENT_MESSAGE_LIMIT = 20;
    private static final int MIN_RECENT_MESSAGE_LIMIT = 2;
    private static final int MAX_RECENT_MESSAGE_LIMIT = 100;

    private final ChatSettingRepository repository;

    @Transactional
    public ChatSettingDto getMySetting() {
        final ChatSettingEntity entity = getOrCreateUserEntity();

        return toDto(entity);
    }

    @Transactional
    public ChatSettingDto modifyMySetting(final ChatSettingDto dto) {
        final ChatSettingEntity entity = getOrCreateUserEntity();

        if (dto != null && dto.getRecentMessageLimit() != null) {
            entity.setRecentMessageLimit(clampRecentMessageLimit(dto.getRecentMessageLimit()));
        }

        return toDto(repository.saveAndFlush(entity));
    }

    public int getMyRecentMessageLimit() {
        return getMySetting().getRecentMessageLimit();
    }

    @Transactional
    public ChatSettingDto getAdminSetting() {
        return toDto(getOrCreateAdminEntity());
    }

    @Transactional
    public ChatSettingDto modifyAdminSetting(final ChatSettingDto dto) {
        final ChatSettingEntity entity = getOrCreateAdminEntity();

        if (dto != null && dto.getRecentMessageLimit() != null) {
            entity.setRecentMessageLimit(clampRecentMessageLimit(dto.getRecentMessageLimit()));
        }

        return toDto(repository.saveAndFlush(entity));
    }

    private ChatSettingEntity getOrCreateUserEntity() {
        final String username = AuthUtils.getLoginUsername();

        return repository.findFirstByScopeAndScopeKeyOrderByCreatedAtDesc(SCOPE_USER, username)
                .orElseGet(() -> createDefaultEntity(SCOPE_USER, username, getAdminDefaultRecentMessageLimit()));
    }

    private ChatSettingEntity getOrCreateAdminEntity() {
        return repository.findFirstByScopeAndScopeKeyOrderByCreatedAtDesc(SCOPE_ADMIN, ADMIN_SCOPE_KEY)
                .orElseGet(() -> createDefaultEntity(SCOPE_ADMIN, ADMIN_SCOPE_KEY, DEFAULT_RECENT_MESSAGE_LIMIT));
    }

    private int getAdminDefaultRecentMessageLimit() {
        return repository.findFirstByScopeAndScopeKeyOrderByCreatedAtDesc(SCOPE_ADMIN, ADMIN_SCOPE_KEY)
                .map(ChatSettingEntity::getRecentMessageLimit)
                .orElse(DEFAULT_RECENT_MESSAGE_LIMIT);
    }

    private ChatSettingEntity createDefaultEntity(final String scope, final String scopeKey, final int recentMessageLimit) {
        return repository.saveAndFlush(ChatSettingEntity.builder()
                .scope(scope)
                .scopeKey(scopeKey)
                .recentMessageLimit(clampRecentMessageLimit(recentMessageLimit))
                .build());
    }

    private int clampRecentMessageLimit(final int value) {
        return Math.max(MIN_RECENT_MESSAGE_LIMIT, Math.min(MAX_RECENT_MESSAGE_LIMIT, value));
    }

    private ChatSettingDto toDto(final ChatSettingEntity entity) {
        final ChatSettingDto dto = ChatSettingDto.builder()
                .id(entity.getId())
                .scope(entity.getScope())
                .scopeKey(entity.getScopeKey())
                .recentMessageLimit(entity.getRecentMessageLimit())
                .createdBy(entity.getCreatedBy())
                .createdByNm(entity.getCreatedByInfo() == null ? null : entity.getCreatedByInfo().getNickname())
                .createdAt(formatDate(entity.getCreatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(formatDate(entity.getUpdatedAt()))
                .build();

        dto.setIsCreatedBy(entity.isCreatedBy());
        dto.setIsUpdatedBy(entity.isUpdatedBy());
        return dto;
    }

    private String formatDate(final Date date) {
        try {
            return DateUtils.asStr(date, DatePtn.DATETIME);
        } catch (final Exception e) {
            return null;
        }
    }
}
