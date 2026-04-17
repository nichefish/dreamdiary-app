package io.nicheblog.dreamdiary.feature.journal.diary.mapstuct;

import io.nicheblog.dreamdiary.feature.journal.diary.entity.JournalDiaryEntity;
import io.nicheblog.dreamdiary.feature.journal.diary.entity.JournalDiaryEntityTestFactory;
import io.nicheblog.dreamdiary.feature.journal.diary.mapstruct.JournalDiaryMapstruct;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryDto;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseEntityTestFactoryHelper;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * JournalDiaryMapstructTest
 * <pre>
 *  저널 일기 Mapstruct 매핑 테스트 모듈
 * </pre>
 *
 * @author nichefish
 */
@ActiveProfiles("test")
@RequiredArgsConstructor
class JournalDiaryMapstructTest {

    private final JournalDiaryMapstruct journalDiaryMapstruct;
    private JournalDiaryEntity journalDiaryEntity;

    /**
     * 각 테스트 시작 전 세팅 초기화.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통적으로 사용할 JournalDiaryEntity 초기화
        journalDiaryEntity = JournalDiaryEntityTestFactory.createWithJournalDt("2000-01-01");    // 2000년 1월 1일, 툐요일.
    }

    /**
     * entity -> dto 검증
     */
    @Test
    void testToDto_checkBasic() throws Exception {
        // Given::

        // When::
        final JournalDiaryDto journalDiaryDto = journalDiaryMapstruct.toDto(journalDiaryEntity);

        // Then::
        assertNotNull(journalDiaryDto, "변환된 저널 일기 Dto는 null일 수 없습니다.");
        assertEquals(DateUtils.asStr(journalDiaryEntity.getJournalChapter().getJournalDay().getJournalDt(), DatePtn.DATE), journalDiaryDto.getStdrdDt(), "기준일자가 제대로 산정되지 않았습니다.");
    }

    /**
     * entity -> dto 검증 :: 등록자/수정자 정보 매핑 체크
     */
    @Test
    void testToDto_checkAuditor() throws Exception {
        // Given::
        // 등록자 / 수정자
        BaseEntityTestFactoryHelper.setCreatedByInfo(journalDiaryEntity);
        BaseEntityTestFactoryHelper.setUpdatedByInfo(journalDiaryEntity);

        // When::
        final JournalDiaryDto journalDiaryDto = journalDiaryMapstruct.toDto(journalDiaryEntity);

        // Then::
        assertNotNull(journalDiaryDto, "변환된 저널 일기 Dto는 null일 수 없습니다.");
        // 등록자
        assertEquals(TestConstant.TEST_REGSTR_ID, journalDiaryDto.getCreatedBy(), "등록자 ID가 제대로 매핑되지 않았습니다.");
        assertEquals(TestConstant.TEST_REGSTR_NM, journalDiaryDto.getCreatedByNm(), "등록자 이름이 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01 00:00:00", journalDiaryDto.getCreatedAt(), "등록일시가 제대로 매핑되지 않았습니다.");
        // 수정자
        assertEquals(TestConstant.TEST_MDFUSR_ID, journalDiaryDto.getUpdatedBy(), "수정자 ID가 제대로 매핑되지 않았습니다.");
        assertEquals(TestConstant.TEST_MDFUSR_NM, journalDiaryDto.getUpdatedByNm(), "수정자 이름이 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01 00:00:00", journalDiaryDto.getUpdatedAt(), "수정일시가 제대로 매핑되지 않았습니다.");
    }

    /* ----- */

    /**
     * updateFromDto 검증 :: 기본 속성
     * TODO
     */
    @Test
    void testUpdateFromDto_checkBasic() throws Exception {
        //
    }
}

