package io.nicheblog.dreamdiary.feature.journal.sbjct.mapstuct;

import io.nicheblog.dreamdiary.feature.journal.sbjct.entity.JournalSbjctEntity;
import io.nicheblog.dreamdiary.feature.journal.sbjct.entity.JournalSbjctEntityTestFactory;
import io.nicheblog.dreamdiary.feature.journal.sbjct.mapstruct.JournalSbjctMapstruct;
import io.nicheblog.dreamdiary.feature.journal.sbjct.model.JournalSbjctDto;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseEntityTestFactoryHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * JournalSbjctMapstructTest
 * <pre>
 *  저널 주제  Mapstruct 매핑 테스트 모듈
 * </pre>
 *
 * @author nichefish
 */
@ActiveProfiles("test")
class JournalSbjctMapstructTest {

    private final JournalSbjctMapstruct journalSbjctMapstruct = JournalSbjctMapstruct.INSTANCE;

    private JournalSbjctEntity journalSbjctEntity;

    /**
     * 각 테스트 시작 전 세팅 초기화.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통적으로 사용할 JournalSbjctEntity 초기화
        journalSbjctEntity = JournalSbjctEntityTestFactory.create();    // 2000년 1월 1일, 툐요일.
    }

    /**
     * entity -> dto 검증
     */
    @Test
    void testToDto_checkBasic() throws Exception {
        // Given::

        // When::
        final JournalSbjctDto journalSbjctDto = journalSbjctMapstruct.toDto(journalSbjctEntity);

        // Then::
        assertNotNull(journalSbjctDto, "변환된 저널 주제 Dto는 null일 수 없습니다.");
    }

    /**
     * entity -> dto 검증 :: 등록자/수정자 정보 매핑 체크
     */
    @Test
    void testToDto_checkAuditor() throws Exception {
        // Given::
        // 등록자 / 수정자
        BaseEntityTestFactoryHelper.setCreatedByInfo(journalSbjctEntity);
        BaseEntityTestFactoryHelper.setUpdatedByInfo(journalSbjctEntity);

        // When::
        final JournalSbjctDto journalSbjctDto = journalSbjctMapstruct.toDto(journalSbjctEntity);

        // Then::
        assertNotNull(journalSbjctDto, "변환된 저널 일기 Dto는 null일 수 없습니다.");
        // 등록자
        assertEquals(TestConstant.TEST_REGSTR_ID, journalSbjctDto.getCreatedBy(), "등록자 ID가 제대로 매핑되지 않았습니다.");
        assertEquals(TestConstant.TEST_REGSTR_NM, journalSbjctDto.getCreatedByNm(), "등록자 이름이 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01 00:00:00", journalSbjctDto.getCreatedAt(), "등록일시가 제대로 매핑되지 않았습니다.");
        // 수정자
        assertEquals(TestConstant.TEST_MDFUSR_ID, journalSbjctDto.getUpdatedBy(), "수정자 ID가 제대로 매핑되지 않았습니다.");
        assertEquals(TestConstant.TEST_MDFUSR_NM, journalSbjctDto.getUpdatedByNm(), "수정자 이름이 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01 00:00:00", journalSbjctDto.getUpdatedAt(), "수정일시가 제대로 매핑되지 않았습니다.");
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
