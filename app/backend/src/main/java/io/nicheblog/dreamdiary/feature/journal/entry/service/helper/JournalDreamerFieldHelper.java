package io.nicheblog.dreamdiary.feature.journal.entry.service.helper;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryPostDto;
import org.apache.commons.lang3.StringUtils;

/**
 * 꿈 엔트리의 지정 꿈꾼 이름을 정규화하고 타인 꿈 여부를 판별한다.
 * <p>{@code dreamer_name}이 비어 있지 않으면 타인 꿈이며, 별도 여부 값은 저장하지 않는다.</p>
 */
public final class JournalDreamerFieldHelper {

    private JournalDreamerFieldHelper() {
    }

    /**
     * 꿈꾼 이름이 비어 있지 않은지(트림 후) 판별한다.
     *
     * @param dreamerName 꿈꾼 이름
     * @return 이름이 있으면 true
     */
    public static boolean hasDreamerName(final String dreamerName) {
        return StringUtils.isNotBlank(normalizeDreamerName(dreamerName));
    }

    /**
     * 꿈꾼 이름을 트림하고, 빈 문자열이면 null 로 정규화한다.
     *
     * @param raw 원본 이름
     * @return 정규화된 이름 또는 null
     */
    public static String normalizeDreamerName(final String raw) {
        return StringUtils.trimToNull(raw);
    }

    /**
     * 조회 DTO 기준 타인 꿈(지정 꿈꾼) 여부.
     *
     * @param entry 엔트리 DTO
     * @return 타인 꿈이면 true
     */
    public static boolean isOtherDreamEntry(final JournalEntryDto entry) {
        return entry != null && hasDreamerName(entry.getDreamerName());
    }

    /**
     * 등록/수정 DTO 의 꿈꾼 이름을 정규화한다.
     *
     * @param postDto 등록·수정 DTO
     * @param contentType 콘텐츠 타입
     */
    public static void applyDreamerNameFromPost(final JournalEntryPostDto postDto, final ContentType contentType) {
        if (postDto == null) return;
        postDto.setDreamerName(contentType == ContentType.JOURNAL_DREAM
                ? normalizeDreamerName(postDto.getDreamerName())
                : null);
    }

    /**
     * 저장 직전 엔티티에 꿈꾼 이름을 반영한다.
     *
     * @param entity 엔트리 엔티티
     */
    public static void applyDreamerNameToEntity(final JournalEntryEntity entity) {
        if (entity == null) return;
        entity.setDreamerName(ContentType.JOURNAL_DREAM.key.equals(entity.getContentType())
                ? normalizeDreamerName(entity.getDreamerName())
                : null);
    }
}
