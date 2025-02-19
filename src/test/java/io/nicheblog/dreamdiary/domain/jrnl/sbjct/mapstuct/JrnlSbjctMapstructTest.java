package io.nicheblog.dreamdiary.domain.jrnl.sbjct.mapstuct;

import io.nicheblog.dreamdiary.domain.jrnl.sbjct.entity.JrnlSbjctEntity;
import io.nicheblog.dreamdiary.domain.jrnl.sbjct.entity.JrnlSbjctEntityTestFactory;
import io.nicheblog.dreamdiary.domain.jrnl.sbjct.mapstruct.JrnlSbjctMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.sbjct.model.JrnlSbjctDto;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseEntityTestFactoryHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * JrnlSbjctMapstructTest
 * <pre>
 *  저널 주제  Mapstruct 매핑 테스트 모듈
 * </pre>
 *
 * @author nichefish
 */
@ActiveProfiles("test")
class JrnlSbjctMapstructTest {

    private final JrnlSbjctMapstruct jrnlSbjctMapstruct = JrnlSbjctMapstruct.INSTANCE;

    private JrnlSbjctEntity jrnlSbjctEntity;

    /**
     * 각 테스트 시작 전 세팅 초기화.
     * @throws Exception 발생할 수 있는 예외.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통적으로 사용할 JrnlSbjctEntity 초기화
        jrnlSbjctEntity = JrnlSbjctEntityTestFactory.create();    // 2000년 1월 1일, 툐요일.
    }

    /**
     * entity -> dto 검증
     */
    @Test
    void testToDto_checkBasic() throws Exception {
        // Given::

        // When::
        final JrnlSbjctDto jrnlSbjctDto = jrnlSbjctMapstruct.toDto(jrnlSbjctEntity);

        // Then::
        assertNotNull(jrnlSbjctDto, "변환된 저널 주제 Dto는 null일 수 없습니다.");
    }

    /**
     * entity -> dto 검증 :: 등록자/수정자 정보 매핑 체크
     */
    @Test
    void testToDto_checkAuditor() throws Exception {
        // Given::
        // 등록자 / 수정자
        BaseEntityTestFactoryHelper.setRegstrInfo(jrnlSbjctEntity);
        BaseEntityTestFactoryHelper.setMdfusrInfo(jrnlSbjctEntity);

        // When::
        final JrnlSbjctDto jrnlSbjctDto = jrnlSbjctMapstruct.toDto(jrnlSbjctEntity);

        // Then::
        assertNotNull(jrnlSbjctDto, "변환된 저널 일기 Dto는 null일 수 없습니다.");
        // 등록자
        assertEquals(TestConstant.TEST_REGSTR_ID, jrnlSbjctDto.getRegstrId(), "등록자 ID가 제대로 매핑되지 않았습니다.");
        assertEquals(TestConstant.TEST_REGSTR_NM, jrnlSbjctDto.getRegstrNm(), "등록자 이름이 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01 00:00:00", jrnlSbjctDto.getRegDt(), "등록일시가 제대로 매핑되지 않았습니다.");
        // 수정자
        assertEquals(TestConstant.TEST_MDFUSR_ID, jrnlSbjctDto.getMdfusrId(), "수정자 ID가 제대로 매핑되지 않았습니다.");
        assertEquals(TestConstant.TEST_MDFUSR_NM, jrnlSbjctDto.getMdfusrNm(), "수정자 이름이 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01 00:00:00", jrnlSbjctDto.getMdfDt(), "수정일시가 제대로 매핑되지 않았습니다.");
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