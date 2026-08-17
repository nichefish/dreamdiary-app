package io.nicheblog.dreamdiary.feature.journal.entry.service.helper;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryPostDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 지정 꿈꾼 이름 정규화와 타인 꿈 파생 계약 테스트.
 */
class JournalDreamerFieldHelperTest {

    private static final String FIXTURE_DREAMER = "Alice";

    /** 꿈꾼 이름은 트림하고 빈 문자열을 null로 정규화한다. */
    @Test
    void normalizeDreamerName_trimsAndNullifiesBlankValues() {
        assertEquals(FIXTURE_DREAMER, JournalDreamerFieldHelper.normalizeDreamerName("  " + FIXTURE_DREAMER + "  "));
        assertNull(JournalDreamerFieldHelper.normalizeDreamerName("   "));
        assertNull(JournalDreamerFieldHelper.normalizeDreamerName(null));
    }

    /** 타인 꿈 여부는 조회 DTO의 지정 꿈꾼 이름 존재 여부에서 파생한다. */
    @Test
    void isOtherDreamEntry_derivesFromDreamerName() {
        assertTrue(JournalDreamerFieldHelper.isOtherDreamEntry(
                JournalEntryDto.builder().dreamerName(FIXTURE_DREAMER).build()));
        assertFalse(JournalDreamerFieldHelper.isOtherDreamEntry(
                JournalEntryDto.builder().dreamerName(" ").build()));
    }

    /** 등록·수정 요청은 꿈 이름을 정규화하고 비꿈 요청의 이름을 비운다. */
    @Test
    void applyDreamerNameFromPost_enforcesDreamOnlyName() {
        final JournalEntryPostDto postDto = JournalEntryPostDto.builder()
                .dreamerName("  " + FIXTURE_DREAMER + "  ")
                .build();

        JournalDreamerFieldHelper.applyDreamerNameFromPost(postDto, ContentType.JOURNAL_DREAM);
        assertEquals(FIXTURE_DREAMER, postDto.getDreamerName());

        JournalDreamerFieldHelper.applyDreamerNameFromPost(postDto, ContentType.JOURNAL_DIARY);
        assertNull(postDto.getDreamerName());
    }

    /** 저장 엔티티는 꿈 이름을 정규화하고 비꿈 엔트리의 이름을 비운다. */
    @Test
    void applyDreamerNameToEntity_enforcesDreamOnlyName() {
        final JournalEntryEntity entity = JournalEntryEntity.builder()
                .contentType(ContentType.JOURNAL_DREAM.key)
                .dreamerName("  " + FIXTURE_DREAMER + "  ")
                .build();

        JournalDreamerFieldHelper.applyDreamerNameToEntity(entity);
        assertEquals(FIXTURE_DREAMER, entity.getDreamerName());

        entity.setContentType(ContentType.JOURNAL_DIARY.key);
        JournalDreamerFieldHelper.applyDreamerNameToEntity(entity);
        assertNull(entity.getDreamerName());
    }
}
