package io.nicheblog.dreamdiary.feature.journal.thread.mapstuct;

import io.nicheblog.dreamdiary.feature.journal.thread.entity.JournalThreadEntity;
import io.nicheblog.dreamdiary.feature.journal.thread.entity.JournalThreadEntityTestFactory;
import io.nicheblog.dreamdiary.feature.journal.thread.mapstruct.JournalThreadMapstruct;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadDto;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseEntityTestFactoryHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * JournalThreadMapstructTest
 * <pre>
 *  저널 스레드  Mapstruct 매핑 테스트 모듈
 * </pre>
 *
 * @author nichefish
 */
@ActiveProfiles("test")
class JournalThreadMapstructTest {

    private final JournalThreadMapstruct journalThreadMapstruct = JournalThreadMapstruct.INSTANCE;

    private JournalThreadEntity journalThreadEntity;

    /**
     * 각 테스트 시작 전 세팅 초기화.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통적으로 사용할 JournalThreadEntity 초기화
        journalThreadEntity = JournalThreadEntityTestFactory.create();    // 2000년 1월 1일, 툐요일.
    }

    /**
     * entity -> dto 검증
     */
    @Test
    void testToDto_checkBasic() throws Exception {
        // Given::

        // When::
        final JournalThreadDto journalThreadDto = journalThreadMapstruct.toDto(journalThreadEntity);

        // Then::
        assertNotNull(journalThreadDto, "변환된 저널 스레드 Dto는 null일 수 없습니다.");
    }

    /**
     * entity -> dto 검증 :: 등록자/수정자 정보 매핑 체크
     */
    @Test
    void testToDto_checkAuditor() throws Exception {
        // Given::
        // 등록자 / 수정자
        BaseEntityTestFactoryHelper.setCreatedByInfo(journalThreadEntity);
        BaseEntityTestFactoryHelper.setUpdatedByInfo(journalThreadEntity);

        // When::
        final JournalThreadDto journalThreadDto = journalThreadMapstruct.toDto(journalThreadEntity);

        // Then::
        assertNotNull(journalThreadDto, "변환된 저널 일기 Dto는 null일 수 없습니다.");
        // 등록자
        assertEquals(TestConstant.TEST_REGSTR_ID, journalThreadDto.getCreatedBy(), "등록자 ID가 제대로 매핑되지 않았습니다.");
        assertEquals(TestConstant.TEST_REGSTR_NM, journalThreadDto.getCreatedByNm(), "등록자 이름이 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01 00:00:00", journalThreadDto.getCreatedAt(), "등록일시가 제대로 매핑되지 않았습니다.");
        // 수정자
        assertEquals(TestConstant.TEST_MDFUSR_ID, journalThreadDto.getUpdatedBy(), "수정자 ID가 제대로 매핑되지 않았습니다.");
        assertEquals(TestConstant.TEST_MDFUSR_NM, journalThreadDto.getUpdatedByNm(), "수정자 이름이 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01 00:00:00", journalThreadDto.getUpdatedAt(), "수정일시가 제대로 매핑되지 않았습니다.");
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
