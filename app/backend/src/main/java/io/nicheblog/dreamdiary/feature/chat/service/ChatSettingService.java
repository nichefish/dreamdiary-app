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
    private static final int DEFAULT_RECENT_MESSAGE_LIMIT = 50;
    private static final int MIN_RECENT_MESSAGE_LIMIT = 2;
    private static final int MAX_RECENT_MESSAGE_LIMIT = 200;
    private static final boolean DEFAULT_RAG_ENABLED = true;
    private static final int DEFAULT_RAG_TOP_K = 5;
    private static final int MIN_RAG_TOP_K = 1;
    private static final int MAX_RAG_TOP_K = 50;
    private static final int DEFAULT_RAG_SUMMARY_TOP_K = 12;
    private static final int DEFAULT_RAG_SYNTHESIS_TOP_K = 25;
    private static final int DEFAULT_RAG_STANCE_TOP_K = 50;
    private static final int MAX_RAG_WIDE_TOP_K = 100;
    private static final double DEFAULT_RAG_MIN_SCORE = 0.35D;
    private static final double MIN_RAG_MIN_SCORE = 0.05D;
    private static final double MAX_RAG_MIN_SCORE = 0.95D;
    private static final double DEFAULT_RAG_SYNTHESIS_MIN_SCORE = 0.25D;

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
        applyRagSettings(entity, dto);

        return toDto(repository.saveAndFlush(entity));
    }

    /**
     * 관리자 전역 RAG 기본값을 반환한다. ChatAIService가 매 요청 조회한다.
     *
     * @return RAG enabled / intent별 topK·minScore 스냅샷
     */
    @Transactional
    public RagAdminSettings getAdminRagSettings() {
        final ChatSettingEntity entity = getOrCreateAdminEntity();
        return new RagAdminSettings(
                entity.getRagEnabled() == null ? DEFAULT_RAG_ENABLED : entity.getRagEnabled(),
                clampRagTopK(entity.getRagTopK() == null ? DEFAULT_RAG_TOP_K : entity.getRagTopK()),
                clampRagMinScore(entity.getRagMinScore() == null ? DEFAULT_RAG_MIN_SCORE : entity.getRagMinScore()),
                clampRagWideTopK(entity.getRagSummaryTopK() == null ? DEFAULT_RAG_SUMMARY_TOP_K : entity.getRagSummaryTopK()),
                clampRagWideTopK(entity.getRagSynthesisTopK() == null ? DEFAULT_RAG_SYNTHESIS_TOP_K : entity.getRagSynthesisTopK()),
                clampRagWideTopK(entity.getRagStanceTopK() == null ? DEFAULT_RAG_STANCE_TOP_K : entity.getRagStanceTopK()),
                clampRagMinScore(entity.getRagSynthesisMinScore() == null
                        ? DEFAULT_RAG_SYNTHESIS_MIN_SCORE : entity.getRagSynthesisMinScore())
        );
    }

    /**
     * 관리자 RAG 기본 설정 스냅샷.
     *
     * <p>{@code topK} = LOOKUP, {@code minScore} = LOOKUP/SUMMARY.
     * {@code summaryTopK}/{@code synthesisTopK}/{@code stanceTopK}/{@code synthesisMinScore}는 intent별 값.</p>
     */
    public record RagAdminSettings(
            boolean enabled,
            int topK,
            double minScore,
            int summaryTopK,
            int synthesisTopK,
            int stanceTopK,
            double synthesisMinScore
    ) {
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
                .ragEnabled(DEFAULT_RAG_ENABLED)
                .ragTopK(DEFAULT_RAG_TOP_K)
                .ragMinScore(DEFAULT_RAG_MIN_SCORE)
                .ragSummaryTopK(DEFAULT_RAG_SUMMARY_TOP_K)
                .ragSynthesisTopK(DEFAULT_RAG_SYNTHESIS_TOP_K)
                .ragStanceTopK(DEFAULT_RAG_STANCE_TOP_K)
                .ragSynthesisMinScore(DEFAULT_RAG_SYNTHESIS_MIN_SCORE)
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
     * 관리자 DTO에서 RAG 필드만 선택적으로 엔티티에 반영한다.
     *
     * <p>null 필드는 기존값을 유지한다. topK/minScore는 허용 범위로 clamp한다.</p>
     *
     * @param entity 대상 설정 엔티티
     * @param dto 관리자 입력 (null이면 no-op)
     */
    private void applyRagSettings(final ChatSettingEntity entity, final ChatSettingDto dto) {
        if (dto == null) return;
        if (dto.getRagEnabled() != null) {
            entity.setRagEnabled(dto.getRagEnabled());
        }
        if (dto.getRagTopK() != null) {
            entity.setRagTopK(clampRagTopK(dto.getRagTopK()));
        }
        if (dto.getRagMinScore() != null) {
            entity.setRagMinScore(clampRagMinScore(dto.getRagMinScore()));
        }
        if (dto.getRagSummaryTopK() != null) {
            entity.setRagSummaryTopK(clampRagWideTopK(dto.getRagSummaryTopK()));
        }
        if (dto.getRagSynthesisTopK() != null) {
            entity.setRagSynthesisTopK(clampRagWideTopK(dto.getRagSynthesisTopK()));
        }
        if (dto.getRagStanceTopK() != null) {
            entity.setRagStanceTopK(clampRagWideTopK(dto.getRagStanceTopK()));
        }
        if (dto.getRagSynthesisMinScore() != null) {
            entity.setRagSynthesisMinScore(clampRagMinScore(dto.getRagSynthesisMinScore()));
        }
        log.info("Admin RAG settings updated. enabled={}, topK={}, minScore={}, summaryTopK={}, synthesisTopK={}, stanceTopK={}, synthesisMinScore={}",
                entity.getRagEnabled(), entity.getRagTopK(), entity.getRagMinScore(),
                entity.getRagSummaryTopK(), entity.getRagSynthesisTopK(),
                entity.getRagStanceTopK(), entity.getRagSynthesisMinScore());
    }

    /**
     * LOOKUP RAG top-K를 허용 범위(1..50)로 보정한다.
     *
     * @param value 보정할 값
     * @return clamp된 top-K
     */
    private int clampRagTopK(final int value) {
        return Math.max(MIN_RAG_TOP_K, Math.min(MAX_RAG_TOP_K, value));
    }

    /**
     * SUMMARY/SYNTHESIS/STANCE top-K를 허용 범위(1..100)로 보정한다.
     *
     * @param value 보정할 값
     * @return clamp된 top-K
     */
    private int clampRagWideTopK(final int value) {
        return Math.max(MIN_RAG_TOP_K, Math.min(MAX_RAG_WIDE_TOP_K, value));
    }

    /**
     * LOOKUP/SUMMARY 벡터 최소 점수를 허용 범위(0.05..0.95)로 보정한다.
     *
     * @param value 보정할 값
     * @return clamp된 min-score
     */
    private double clampRagMinScore(final double value) {
        return Math.max(MIN_RAG_MIN_SCORE, Math.min(MAX_RAG_MIN_SCORE, value));
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
                .ragEnabled(entity.getRagEnabled() == null ? DEFAULT_RAG_ENABLED : entity.getRagEnabled())
                .ragTopK(entity.getRagTopK() == null ? DEFAULT_RAG_TOP_K : entity.getRagTopK())
                .ragMinScore(entity.getRagMinScore() == null ? DEFAULT_RAG_MIN_SCORE : entity.getRagMinScore())
                .ragSummaryTopK(entity.getRagSummaryTopK() == null ? DEFAULT_RAG_SUMMARY_TOP_K : entity.getRagSummaryTopK())
                .ragSynthesisTopK(entity.getRagSynthesisTopK() == null ? DEFAULT_RAG_SYNTHESIS_TOP_K : entity.getRagSynthesisTopK())
                .ragStanceTopK(entity.getRagStanceTopK() == null ? DEFAULT_RAG_STANCE_TOP_K : entity.getRagStanceTopK())
                .ragSynthesisMinScore(entity.getRagSynthesisMinScore() == null
                        ? DEFAULT_RAG_SYNTHESIS_MIN_SCORE : entity.getRagSynthesisMinScore())
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
    private String formatDate(final Object date) {
        try {
            return DateUtils.asStr(date, DatePtn.DATETIME);
        } catch (final Exception e) {
            return null;
        }
    }
}
