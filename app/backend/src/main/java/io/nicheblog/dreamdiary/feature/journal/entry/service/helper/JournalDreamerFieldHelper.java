package io.nicheblog.dreamdiary.feature.journal.entry.service.helper;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryPostDto;
import org.apache.commons.lang3.StringUtils;

/**
 * 꿈 엔트리의 꿈꾼 이름·타인 꿈 여부(else_dream_yn) 정규화.
 * <p>수렴 방향: {@code dreamer_name}(현재 {@code elseDreamerNm}) 비어 있지 않으면 타인 꿈으로 분류하고
 * {@code else_dream_yn} 은 저장 시 여기서만 Y/N 을 맞춘다. UI 에서 Y/N 을 직접 보내지 않는다.</p>
 */
public final class JournalDreamerFieldHelper {

    private JournalDreamerFieldHelper() {
    }

    /**
     * 꿈꾼 이름이 비어 있지 않은지(트림 후) 판별한다.
     *
     * @param elseDreamerNm 꿈꾼 이름
     * @return 이름이 있으면 true
     */
    public static boolean hasDreamerName(final String elseDreamerNm) {
        return StringUtils.isNotBlank(normalizeDreamerName(elseDreamerNm));
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
     * 정규화된 꿈꾼 이름으로 {@code else_dream_yn} 값을 파생한다.
     *
     * @param normalizedDreamerNm 정규화된 꿈꾼 이름
     * @return Y 또는 N
     */
    public static String deriveElseDreamYn(final String normalizedDreamerNm) {
        return hasDreamerName(normalizedDreamerNm) ? "Y" : "N";
    }

    /**
     * 조회 DTO 기준 타인 꿈(지정 꿈꾼) 여부.
     *
     * @param entry 엔트리 DTO
     * @return 타인 꿈이면 true
     */
    public static boolean isOtherDreamEntry(final JournalEntryDto entry) {
        return entry != null && hasDreamerName(entry.getElseDreamerNm());
    }

    /**
     * 등록/수정 DTO 의 꿈꾼 이름을 정규화한다.
     *
     * @param postDto 등록·수정 DTO
     * @param contentType 콘텐츠 타입
     */
    public static void applyDreamerFieldsFromPost(final JournalEntryPostDto postDto, final ContentType contentType) {
        if (postDto == null || contentType != ContentType.JOURNAL_DREAM) {
            return;
        }
        postDto.setElseDreamerNm(normalizeDreamerName(postDto.getElseDreamerNm()));
    }

    /**
     * 저장 직전 엔티티에 꿈꾼 이름·else_dream_yn 을 반영한다.
     *
     * @param entity 엔트리 엔티티
     */
    public static void applyDreamerFieldsToEntity(final JournalEntryEntity entity) {
        if (entity == null || !ContentType.JOURNAL_DREAM.key.equals(entity.getContentType())) {
            return;
        }
        final String normalized = normalizeDreamerName(entity.getElseDreamerNm());
        entity.setElseDreamerNm(normalized);
        entity.setElseDreamYn(deriveElseDreamYn(normalized));
    }
}
