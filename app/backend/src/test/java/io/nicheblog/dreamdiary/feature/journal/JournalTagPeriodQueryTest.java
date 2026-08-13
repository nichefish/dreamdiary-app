package io.nicheblog.dreamdiary.feature.journal;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagSearchParam;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayTagQuery;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryTagQuery;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 일간 태그클라우드의 기준일자 기간 계약을 검증한다.
 */
class JournalTagPeriodQueryTest {

    private static final String FIXTURE_STDRD_DT = "2026-01-15";

    /** 일자 태그 질의는 기준일자만 보유한다. */
    @Test
    void journalDayDailyQueryUsesOnlyStdrdDt() {
        final JournalDayTagQuery query = JournalDayTagQuery.daily(FIXTURE_STDRD_DT);

        assertEquals(FIXTURE_STDRD_DT, query.stdrdDt());
        assertTrue(query.hasStdrdDt());
        assertFalse(query.hasWeekStartDt());
        assertFalse(query.hasYyMnth());
    }

    /** 엔트리 태그 질의는 콘텐츠 타입과 기준일자를 하나의 기간 키로 보유한다. */
    @Test
    void journalEntryDailyQueryUsesContentTypeAndStdrdDt() {
        final JournalEntryTagQuery query = JournalEntryTagQuery.daily(
                ContentType.JOURNAL_DIARY,
                FIXTURE_STDRD_DT
        );

        assertEquals(ContentType.JOURNAL_DIARY, query.contentType());
        assertEquals(FIXTURE_STDRD_DT, query.stdrdDt());
        assertTrue(query.hasStdrdDt());
        assertTrue(query.hasPeriod());
        assertFalse(query.hasWeekStartDt());
        assertFalse(query.hasYyMnth());
    }

    /** HTTP 검색 파라미터는 공백이 아닌 기준일자만 일간 조건으로 인정한다. */
    @Test
    void tagSearchParamRecognizesNonBlankStdrdDt() {
        final TagSearchParam searchParam = TagSearchParam.builder()
                .stdrdDt(FIXTURE_STDRD_DT)
                .build();

        assertTrue(searchParam.hasStdrdDt());
        searchParam.setStdrdDt(" ");
        assertFalse(searchParam.hasStdrdDt());
    }
}
