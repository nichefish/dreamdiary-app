package io.nicheblog.dreamdiary.domain.jrnl.day.mapstuct;

import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDayEntity;
import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDayEntityTestFactory;
import io.nicheblog.dreamdiary.domain.jrnl.day.mapstruct.JrnlDayMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryEntity;
import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryEntityTestFactory;
import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamEntity;
import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamEntityTestFactory;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.extension.clsf.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseEntityTestFactoryHelper;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * JrnlDayToDtoMapstructTest
 * <pre>
 *  저널 일자 Mapstruct 매핑 테스트 모듈
 * </pre>
 *
 * @author nichefish
 */
@ActiveProfiles("test")
@DisplayName("저널 일자 Mapstruct - toDto 매핑 테스트")
class JrnlDayToDtoMapstructTest {

    private final JrnlDayMapstruct jrnlDayMapstruct = JrnlDayMapstruct.INSTANCE;

    private JrnlDayEntity jrnlDayEntity;

    /**
     * 각 테스트 시작 전 세팅 초기화.
     * @throws Exception 발생할 수 있는 예외.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통적으로 사용할 JrnlDayEntity 초기화
        jrnlDayEntity = JrnlDayEntityTestFactory.createWithJrnlDt("2000-01-01");    // 2000년 1월 1일, 툐요일.
    }

    /**
     * entity -> dto 검증 :: 기본 속성
     */
    @Test
    void testToDto_checkBasic() throws Exception {
        // Given::

        // When::
        final JrnlDayDto jrnlDayDto = jrnlDayMapstruct.toDto(jrnlDayEntity);

        // Then::
        assertNotNull(jrnlDayDto, "변환된 저널 일자 Dto는 null일 수 없습니다.");
        // 날짜.
        assertEquals(DateUtils.asDate(jrnlDayDto.getJrnlDt()), jrnlDayEntity.getJrnlDt(), "저널 일자가 제대로 매핑되지 않았습니다.");
        // 주일
        assertNotNull(jrnlDayDto.getJrnlDtWeekDay(), "요일은 null이 될 수 없습니다.");
        assertEquals("土", jrnlDayDto.getJrnlDtWeekDay(), "요일이 예측된 값이 아닙니다.");
        assertEquals(jrnlDayDto.getJrnlDt(), jrnlDayDto.getStdrdDt(), "기준일자가 제대로 산정되지 않았습니다.");
    }

    /**
     * entity -> dto 검증 :: 등록자/수정자 정보 매핑 체크
     */
    @Test
    void testToDto_checkAuditor() throws Exception {
        // Given::
        // 등록자 / 수정자
        BaseEntityTestFactoryHelper.setRegstrInfo(jrnlDayEntity);
        BaseEntityTestFactoryHelper.setMdfusrInfo(jrnlDayEntity);

        // When::
        final JrnlDayDto jrnlDayDto = jrnlDayMapstruct.toDto(jrnlDayEntity);

        // Then::
        assertNotNull(jrnlDayDto, "변환된 저널 일자 Dto는 null일 수 없습니다.");
        // 등록자
        assertEquals(TestConstant.TEST_REGSTR_ID, jrnlDayDto.getRegstrId(), "등록자 ID가 제대로 매핑되지 않았습니다.");
        assertEquals(TestConstant.TEST_REGSTR_NM, jrnlDayDto.getRegstrNm(), "등록자 이름이 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01 00:00:00", jrnlDayDto.getRegDt(), "등록일시가 제대로 매핑되지 않았습니다.");
        // 수정자
        assertEquals(TestConstant.TEST_MDFUSR_ID, jrnlDayDto.getMdfusrId(), "수정자 ID가 제대로 매핑되지 않았습니다.");
        assertEquals(TestConstant.TEST_MDFUSR_NM, jrnlDayDto.getMdfusrNm(), "수정자 이름이 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01 00:00:00", jrnlDayDto.getMdfDt(), "수정일시가 제대로 매핑되지 않았습니다.");
    }

    /**
     * entity -> dto 검증 :: 대략일자
     */
    @Test
    void testToDto_checkAprxmtDt() throws Exception {
        // Given::
        // 대략일자 세팅 (저널 일자 무효화)
        jrnlDayEntity.setDtUnknownYn("Y");
        jrnlDayEntity.setAprxmtDt(DateUtils.asDate("2000-01-01"));
        jrnlDayEntity.setJrnlDt(null);

        // When::
        final JrnlDayDto jrnlDayDto = jrnlDayMapstruct.toDto(jrnlDayEntity);

        // Then::
        assertNotNull(jrnlDayDto, "변환된 저널 일자 Dto는 null일 수 없습니다.");
        assertEquals(jrnlDayEntity.getDtUnknownYn(), jrnlDayDto.getDtUnknownYn(), "날짜미상여부가 제대로 매핑되지 않았습니다.");
        assertEquals(DateUtils.asStr(jrnlDayEntity.getAprxmtDt(), DatePtn.DATE), jrnlDayDto.getAprxmtDt(), "대략일자가 예측된 값이 아닙니다.");
        assertEquals(DateUtils.asStr(jrnlDayEntity.getAprxmtDt(), DatePtn.DATE), jrnlDayDto.getStdrdDt(), "기준일자가 제대로 산정되지 않았습니다.");
    }

    /**
     * entity -> dto 검증 :: 하위 일기 목록 (diaryList)
     */
    @Test
    void testToDto_checkDiaryList() throws Exception {
        // Given::
        final JrnlDiaryEntity aa = JrnlDiaryEntityTestFactory.createWithJrnlDay(jrnlDayEntity);
        final JrnlDiaryEntity bb = JrnlDiaryEntityTestFactory.createWithJrnlDay(jrnlDayEntity);
        jrnlDayEntity.setJrnlDiaryList(List.of(aa, bb));

        // When::
        final JrnlDayDto jrnlDayDto = jrnlDayMapstruct.toDto(jrnlDayEntity);

        // Then::
        assertNotNull(jrnlDayDto, "변환된 저널 일자 Dto는 null일 수 없습니다.");
        assertNotNull(jrnlDayDto.getJrnlDiaryList(), "저널 일기 목록이 제대로 매핑되지 않았습니다.");
    }

    /**
     * entity -> dto 검증 :: 하위 꿈 목록 (dreamList)
     */
    @Test
    void testToDto_checkDreamList() throws Exception {
        // Given::
        final JrnlDreamEntity aa = JrnlDreamEntityTestFactory.createWithJrnlDay(jrnlDayEntity);
        final JrnlDreamEntity bb = JrnlDreamEntityTestFactory.createWithJrnlDay(jrnlDayEntity);
        jrnlDayEntity.setJrnlDreamList(List.of(aa, bb));

        // When::
        final JrnlDayDto jrnlDayDto = jrnlDayMapstruct.toDto(jrnlDayEntity);

        // Then::
        assertNotNull(jrnlDayDto, "변환된 저널 일자 Dto는 null일 수 없습니다.");
        assertNotNull(jrnlDayDto.getJrnlDreamList(), "저널 꿈 목록이 제대로 매핑되지 않았습니다.");
    }

    /**
     * entity -> dto 검증 :: 타인 꿈 목록 (elseDreamList)
     */
    @Test
    void testToDto_checkElseDreamList() throws Exception {
        // Given::
        final JrnlDreamEntity aa = JrnlDreamEntityTestFactory.createWithJrnlDay(jrnlDayEntity);
        final JrnlDreamEntity bb = JrnlDreamEntityTestFactory.createWithJrnlDay(jrnlDayEntity);
        jrnlDayEntity.setJrnlElseDreamList(List.of(aa, bb));

        // When::
        final JrnlDayDto jrnlDayDto = jrnlDayMapstruct.toDto(jrnlDayEntity);

        // Then::
        assertNotNull(jrnlDayDto, "변환된 저널 일자 Dto는 null일 수 없습니다.");
        assertNotNull(jrnlDayDto.getJrnlElseDreamList(), "타인 꿈 목록이 제대로 매핑되지 않았습니다.");
    }

    /**
     * entity -> dto 검증 :: 하위 태그 목록 (tag)
     */
    @Test
    void testToDto_checkTagModule() throws Exception {
        // Given::
        final TagEmbed tag = new TagEmbed();
        jrnlDayEntity.setTag(tag);

        // When::
        final JrnlDayDto jrnlDayDto = jrnlDayMapstruct.toDto(jrnlDayEntity);

        // Then::
        assertNotNull(jrnlDayDto, "변환된 저널 일자 Dto는 null일 수 없습니다.");
        assertNotNull(jrnlDayDto.getTag(), "태그 모듈이 제대로 매핑되지 않았습니다.");
    }
}