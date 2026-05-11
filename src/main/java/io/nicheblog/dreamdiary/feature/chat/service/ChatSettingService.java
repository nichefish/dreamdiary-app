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

    /**
     * 로그인 사용자의 채팅 설정을 조회하고, 없으면 관리자 기본값으로 생성한다.
     *
     * @return 로그인 사용자의 채팅 설정 DTO
     */
    @Transactional
    public ChatSettingDto getMySetting() {
        final ChatSettingEntity entity = getOrCreateUserEntity();

        return toDto(entity);
    }

    /**
     * 로그인 사용자의 채팅 설정을 수정한다.
     *
     * @param dto 변경할 채팅 설정 값
     * @return 저장된 사용자 채팅 설정 DTO
     */
    @Transactional
    public ChatSettingDto modifyMySetting(final ChatSettingDto dto) {
        final ChatSettingEntity entity = getOrCreateUserEntity();

        if (dto != null && dto.getRecentMessageLimit() != null) {
            entity.setRecentMessageLimit(clampRecentMessageLimit(dto.getRecentMessageLimit()));
        }

        return toDto(repository.saveAndFlush(entity));
    }

    /**
     * 로그인 사용자의 최근 대화 맥락 포함 개수를 반환한다.
     *
     * @return 최근 대화 맥락 포함 개수
     */
    public int getMyRecentMessageLimit() {
        return getMySetting().getRecentMessageLimit();
    }

    /**
     * 관리자 전역 채팅 기본 설정을 조회하고, 없으면 기본값으로 생성한다.
     *
     * @return 관리자 전역 채팅 설정 DTO
     */
    @Transactional
    public ChatSettingDto getAdminSetting() {
        return toDto(getOrCreateAdminEntity());
    }

    /**
     * 관리자 전역 채팅 기본 설정을 수정한다.
     *
     * @param dto 변경할 전역 기본 설정 값
     * @return 저장된 관리자 전역 채팅 설정 DTO
     */
    @Transactional
    public ChatSettingDto modifyAdminSetting(final ChatSettingDto dto) {
        final ChatSettingEntity entity = getOrCreateAdminEntity();

        if (dto != null && dto.getRecentMessageLimit() != null) {
            entity.setRecentMessageLimit(clampRecentMessageLimit(dto.getRecentMessageLimit()));
        }

        return toDto(repository.saveAndFlush(entity));
    }

    /**
     * 로그인 사용자 범위의 설정 엔티티를 조회하거나 생성한다.
     *
     * @return 사용자 채팅 설정 엔티티
     */
    private ChatSettingEntity getOrCreateUserEntity() {
        final String username = AuthUtils.getLoginUsername();

        return repository.findFirstByScopeAndScopeKeyOrderByCreatedAtDesc(SCOPE_USER, username)
                .orElseGet(() -> createDefaultEntity(SCOPE_USER, username, getAdminDefaultRecentMessageLimit()));
    }

    /**
     * 관리자 전역 범위의 설정 엔티티를 조회하거나 생성한다.
     *
     * @return 관리자 전역 채팅 설정 엔티티
     */
    private ChatSettingEntity getOrCreateAdminEntity() {
        return repository.findFirstByScopeAndScopeKeyOrderByCreatedAtDesc(SCOPE_ADMIN, ADMIN_SCOPE_KEY)
                .orElseGet(() -> createDefaultEntity(SCOPE_ADMIN, ADMIN_SCOPE_KEY, DEFAULT_RECENT_MESSAGE_LIMIT));
    }

    /**
     * 사용자 설정을 새로 만들 때 적용할 관리자 기본 최근 메시지 개수를 조회한다.
     *
     * @return 관리자 기본 최근 메시지 개수
     */
    private int getAdminDefaultRecentMessageLimit() {
        return repository.findFirstByScopeAndScopeKeyOrderByCreatedAtDesc(SCOPE_ADMIN, ADMIN_SCOPE_KEY)
                .map(ChatSettingEntity::getRecentMessageLimit)
                .orElse(DEFAULT_RECENT_MESSAGE_LIMIT);
    }

    /**
     * 지정한 범위와 키로 기본 채팅 설정 엔티티를 생성한다.
     *
     * @param scope 설정 범위
     * @param scopeKey 설정 범위 식별자
     * @param recentMessageLimit 최근 대화 맥락 포함 개수
     * @return 저장된 채팅 설정 엔티티
     */
    private ChatSettingEntity createDefaultEntity(final String scope, final String scopeKey, final int recentMessageLimit) {
        return repository.saveAndFlush(ChatSettingEntity.builder()
                .scope(scope)
                .scopeKey(scopeKey)
                .recentMessageLimit(clampRecentMessageLimit(recentMessageLimit))
                .build());
    }

    /**
     * 최근 대화 맥락 포함 개수를 허용 범위 안으로 보정한다.
     *
     * @param value 보정할 설정값
     * @return 허용 범위 안으로 보정된 설정값
     */
    private int clampRecentMessageLimit(final int value) {
        return Math.max(MIN_RECENT_MESSAGE_LIMIT, Math.min(MAX_RECENT_MESSAGE_LIMIT, value));
    }

    /**
     * 채팅 설정 엔티티를 화면 응답용 DTO로 변환한다.
     *
     * @param entity 변환할 채팅 설정 엔티티
     * @return 채팅 설정 DTO
     */
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

    /**
     * 날짜 값을 화면 표시용 문자열로 변환한다.
     *
     * @param date 변환할 날짜 값
     * @return 날짜 문자열, 변환할 수 없으면 {@code null}
     */
    private String formatDate(final Date date) {
        try {
            return DateUtils.asStr(date, DatePtn.DATETIME);
        } catch (final Exception e) {
            return null;
        }
    }
}
