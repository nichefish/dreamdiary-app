package io.nicheblog.dreamdiary.domain.jrnl.dream.mapstuct;

import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamEntity;
import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamEntityTestFactory;
import io.nicheblog.dreamdiary.domain.jrnl.dream.mapstruct.JrnlDreamMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.dream.model.JrnlDreamDto;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseEntityTestFactoryHelper;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * JrnlDreamMapstructTest
 * <pre>
 *  저널 꿈 Mapstruct 매핑 테스트 모듈
 * </pre>
 *
 * @author nichefish
 */
@ActiveProfiles("test")
class JrnlDreamMapstructTest {

    private final JrnlDreamMapstruct jrnlDreamMapstruct = JrnlDreamMapstruct.INSTANCE;

    private JrnlDreamEntity jrnlDreamEntity;

    /**
     * 각 테스트 시작 전 세팅 초기화.
     * @throws Exception 발생할 수 있는 예외.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통적으로 사용할 JrnlDreamEntity 초기화
        jrnlDreamEntity = JrnlDreamEntityTestFactory.createWithJrnlDt("2000-01-01");    // 2000년 1월 1일, 툐요일.
    }

    /**
     * entity -> dto 검증
     */
    @Test
    void testToDto_checkBasic() throws Exception {
        // Given::

        // When::
        final JrnlDreamDto jrnlDreamDto = jrnlDreamMapstruct.toDto(jrnlDreamEntity);

        // Then::
        assertNotNull(jrnlDreamDto, "변환된 저널 꿈 Dto는 null일 수 없습니다.");
        assertEquals(jrnlDreamDto.getStdrdDt(), DateUtils.asStr(jrnlDreamEntity.getJrnlDay().getJrnlDt(), DatePtn.DATE), "기준일자가 제대로 산정되지 않았습니다.");
    }

    /**
     * entity -> dto 검증 :: 등록자/수정자 정보 매핑 체크
     */
    @Test
    void testToDto_checkAuditor() throws Exception {
        // Given::
        // 등록자 / 수정자
        BaseEntityTestFactoryHelper.setRegstrInfo(jrnlDreamEntity);
        BaseEntityTestFactoryHelper.setMdfusrInfo(jrnlDreamEntity);

        // When::
        final JrnlDreamDto jrnlDreamDto = jrnlDreamMapstruct.toDto(jrnlDreamEntity);

        // Then::
        assertNotNull(jrnlDreamDto, "변환된 저널 꿈 Dto는 null일 수 없습니다.");
        // 등록자
        assertEquals(TestConstant.TEST_REGSTR_ID, jrnlDreamDto.getRegstrId(), "등록자 ID가 제대로 매핑되지 않았습니다.");
        assertEquals(TestConstant.TEST_REGSTR_NM, jrnlDreamDto.getRegstrNm(), "등록자 이름이 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01 00:00:00", jrnlDreamDto.getRegDt(), "등록일시가 제대로 매핑되지 않았습니다.");
        // 수정자
        assertEquals(TestConstant.TEST_MDFUSR_ID, jrnlDreamDto.getMdfusrId(), "수정자 ID가 제대로 매핑되지 않았습니다.");
        assertEquals(TestConstant.TEST_MDFUSR_NM, jrnlDreamDto.getMdfusrNm(), "수정자 이름이 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01 00:00:00", jrnlDreamDto.getMdfDt(), "수정일시가 제대로 매핑되지 않았습니다.");
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