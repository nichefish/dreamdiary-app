package io.nicheblog.dreamdiary.domain.jrnl.day.mapstuct;

import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDayEntity;
import io.nicheblog.dreamdiary.domain.jrnl.day.mapstruct.JrnlDayMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.domain.jrnl.day.model.JrnlDayDtoTestFactory;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * JrnlDayToEntityMapstructTest
 * <pre>
 *  저널 일자 Mapstruct 매핑 테스트 모듈
 * </pre>
 *
 * @author nichefish
 */
@ActiveProfiles("test")
@DisplayName("저널 일자 Mapstruct - toEntity 매핑 테스트")
class JrnlDayToEntityMapstructTest {

    private final JrnlDayMapstruct jrnlDayMapstruct = JrnlDayMapstruct.INSTANCE;

    private JrnlDayDto jrnlDayDto;

    /**
     * 각 테스트 시작 전 세팅 초기화.
     * @throws Exception 발생할 수 있는 예외.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통적으로 사용할 JrnlDayDto 초기화
        jrnlDayDto = JrnlDayDtoTestFactory.create();
        jrnlDayDto.setJrnlDt("2000-01-01");
    }

    /**
     * dto -> entity 변환 검증 :: 기본 속성
     */
    @Test
    void testToEntity_checkBasic() throws Exception {
        // Given::
        jrnlDayDto.setJrnlDt("2000-01-01");

        // When::
        JrnlDayEntity jrnlDayEntity = jrnlDayMapstruct.toEntity(jrnlDayDto);

        // Then::
        assertNotNull(jrnlDayEntity, "변환된 저널 일자 Entity는 null일 수 없습니다.");
        // 일반 필드는 검증할 필요 없음. 로직이 들어가는 부분에 대하여 테스트 진행
        assertEquals(DateUtils.asDate(jrnlDayDto.getJrnlDt()), jrnlDayEntity.getJrnlDt(), "저널 일자가 제대로 매핑되지 않았습니다.");
    }

    /**
     * dto -> entity 변환 검증 :: 대락일자
     */
    @Test
    void testToEntity_checkAprxmtDt() throws Exception {
        // Given::
        // 대략일자 세팅
        jrnlDayDto.setDtUnknownYn("Y");
        jrnlDayDto.setAprxmtDt("2000-01-01");

        // When::
        JrnlDayEntity jrnlDayEntity = jrnlDayMapstruct.toEntity(jrnlDayDto);

        // Then::
        assertNotNull(jrnlDayEntity, "변환된 저널 일자 Entity는 null일 수 없습니다.");
        // 일반 필드는 검증할 필요 없음. 로직이 들어가는 부분에 대하여 테스트 진행
        // 대략일자 여부
        assertEquals(jrnlDayDto.getDtUnknownYn(), jrnlDayEntity.getDtUnknownYn(), "날짜미상여부가 제대로 매핑되지 않았습니다.");
        assertEquals(DateUtils.asDate(jrnlDayDto.getAprxmtDt()), jrnlDayEntity.getAprxmtDt(), "대략일자가 예측된 값이 아닙니다.");
    }

    /**
     * entity -> dto 검증 :: 하위 태그 목록 (tag)
     */
    @Test
    void testToDto_checkTagModule() throws Exception {
        // Given::
        TagCmpstn tag = new TagCmpstn();
        jrnlDayDto.setTag(tag);

        // When::
        JrnlDayEntity jrnlDayEntity = jrnlDayMapstruct.toEntity(jrnlDayDto);

        // Then::
        assertNotNull(jrnlDayEntity, "변환된 저널 일자 Entity는 null일 수 없습니다.");
        // 일반 필드는 검증할 필요 없음. 로직이 들어가는 부분에 대하여 테스트 진행
        assertNotNull(jrnlDayEntity.getTag(), "태그 모듈이 제대로 매핑되지 않았습니다.");
    }

    /* ----- */

    /**
     * updateFromDto 검증 :: 기본 속성
     */
    @Test
    void testUpdateFromDto_checkBasic() throws Exception {
        // Given::
        jrnlDayDto.setJrnlDt("2000-01-01");

        // When::
        JrnlDayEntity jrnlDayEntity = new JrnlDayEntity();
        jrnlDayMapstruct.updateFromDto(jrnlDayDto, jrnlDayEntity);

        // Then::
        assertNotNull(jrnlDayEntity, "변환된 저널 일자 Entity는 null일 수 없습니다.");
        // 일반 필드는 검증할 필요 없음. 로직이 들어가는 부분에 대하여 테스트 진행
        assertEquals(DateUtils.asDate(jrnlDayDto.getJrnlDt()), jrnlDayEntity.getJrnlDt(), "저널 일자가 제대로 매핑되지 않았습니다.");
    }
}