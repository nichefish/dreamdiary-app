package io.nicheblog.dreamdiary.feature.journal.dream.mapstuct;

import io.nicheblog.dreamdiary.feature.journal.dream.entity.JournalDreamEntity;
import io.nicheblog.dreamdiary.feature.journal.dream.entity.JournalDreamEntityTestFactory;
import io.nicheblog.dreamdiary.feature.journal.dream.mapstruct.JournalDreamMapstruct;
import io.nicheblog.dreamdiary.feature.journal.dream.model.JournalDreamDto;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseEntityTestFactoryHelper;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * JournalDreamMapstructTest
 * <pre>
 *  저널 꿈 Mapstruct 매핑 테스트 모듈
 * </pre>
 *
 * @author nichefish
 */
@ActiveProfiles("test")
class JournalDreamMapstructTest {

    private final JournalDreamMapstruct journalDreamMapstruct = Mappers.getMapper(JournalDreamMapstruct.class);

    private JournalDreamEntity journalDreamEntity;

    /**
     * 각 테스트 시작 전 세팅 초기화.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통적으로 사용할 JournalDreamEntity 초기화
        journalDreamEntity = JournalDreamEntityTestFactory.createWithJournalDt("2000-01-01");    // 2000년 1월 1일, 툐요일.
    }

    /**
     * entity -> dto 검증
     */
    @Test
    void testToDto_checkBasic() throws Exception {
        // Given::

        // When::
        final JournalDreamDto journalDreamDto = journalDreamMapstruct.toDto(journalDreamEntity);

        // Then::
        assertNotNull(journalDreamDto, "변환된 저널 꿈 Dto는 null일 수 없습니다.");
        assertEquals(journalDreamDto.getStdrdDt(), DateUtils.asStr(journalDreamEntity.getJournalDay().getJournalDt(), DatePtn.DATE), "기준일자가 제대로 산정되지 않았습니다.");
    }

    /**
     * entity -> dto 검증 :: 등록자/수정자 정보 매핑 체크
     */
    @Test
    void testToDto_checkAuditor() throws Exception {
        // Given::
        // 등록자 / 수정자
        BaseEntityTestFactoryHelper.setCreatedByInfo(journalDreamEntity);
        BaseEntityTestFactoryHelper.setUpdatedByInfo(journalDreamEntity);

        // When::
        final JournalDreamDto journalDreamDto = journalDreamMapstruct.toDto(journalDreamEntity);

        // Then::
        assertNotNull(journalDreamDto, "변환된 저널 꿈 Dto는 null일 수 없습니다.");
        // 등록자
        assertEquals(TestConstant.TEST_REGSTR_ID, journalDreamDto.getCreatedBy(), "등록자 ID가 제대로 매핑되지 않았습니다.");
        assertEquals(TestConstant.TEST_REGSTR_NM, journalDreamDto.getCreatedByNm(), "등록자 이름이 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01 00:00:00", journalDreamDto.getCreatedAt(), "등록일시가 제대로 매핑되지 않았습니다.");
        // 수정자
        assertEquals(TestConstant.TEST_MDFUSR_ID, journalDreamDto.getUpdatedBy(), "수정자 ID가 제대로 매핑되지 않았습니다.");
        assertEquals(TestConstant.TEST_MDFUSR_NM, journalDreamDto.getUpdatedByNm(), "수정자 이름이 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01 00:00:00", journalDreamDto.getUpdatedAt(), "수정일시가 제대로 매핑되지 않았습니다.");
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

