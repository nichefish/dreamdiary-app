package io.nicheblog.dreamdiary.infrastructure.cd.mapstruct;

import io.nicheblog.dreamdiary.feature.admin.cd.mapstruct.ClCdMapstruct;
import io.nicheblog.dreamdiary.feature.admin.cd.model.ClCdDto;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseEntityTestFactoryHelper;
import io.nicheblog.dreamdiary.infrastructure.cd.entity.ClCdEntity;
import io.nicheblog.dreamdiary.infrastructure.cd.entity.ClCdEntityTestFactory;
import io.nicheblog.dreamdiary.infrastructure.cd.model.ClCdDtoTestFactory;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * ClCdMapstructTest
 * <pre>
 *  분류 코드 관리 Mapstruct 매핑 테스트 모듈.
 * </pre>
 * TODO: dtlCd 관련 체크 추가하기
 *
 * @author nichefish
 */
@ActiveProfiles("test")
@Log4j2
class ClCdMapstructTest {

    private final ClCdMapstruct clCdMapstruct = ClCdMapstruct.INSTANCE;

    /**
     * entity -> dto 검증 :: 기본 속성
     */
    @Test
    void testToDto_checkBasic() throws Exception {
        // Given::
        ClCdEntity clCdEntity = ClCdEntityTestFactory.create();
        // 글분류 코드
        clCdEntity.setClCtgrCd(TestConstant.TEST_CL_CTGR_CD);

        // When::
        ClCdDto dto = clCdMapstruct.toDto(clCdEntity);

        // Then::
        assertNotNull(dto, "변환된 dto는 null일 수 없습니다.");
        assertEquals(TestConstant.TEST_CL_CD, dto.getClCd(), "분류 코드 매핑이 제대로 이루어지지 않았습니다.");
        assertEquals(TestConstant.TEST_CL_CD_NM, dto.getClCdNm(), "분류 코드명 매핑이 제대로 이루어지지 않았습니다.");
        assertEquals(TestConstant.TEST_DC, dto.getDc(), "설명 코드명 매핑이 제대로 이루어지지 않았습니다.");
        // 글분류 코드
        assertEquals(TestConstant.TEST_CL_CTGR_CD, dto.getClCtgrCd(), "글분류 코드 매핑이 제대로 이루어지지 않았습니다.");
    }

    /**
     * entity -> dto 변환 검증 :: 등록자/수정자 정보
     */
    @Test
    void testToDto_checkAuditor() throws Exception {
        // Given::
        ClCdEntity clCdEntity = ClCdEntityTestFactory.create();
        // 등록자
        BaseEntityTestFactoryHelper.setCreatedByInfo(clCdEntity);
        // 수정자
        BaseEntityTestFactoryHelper.setUpdatedByInfo(clCdEntity);

        // When::
        ClCdDto dto = clCdMapstruct.toDto(clCdEntity);

        // Then::
        assertNotNull(dto);
        // 등록자
        assertEquals(TestConstant.TEST_REGSTR_ID, dto.getCreatedBy(), "등록자 ID가 제대로 매핑되지 않았습니다.");
        assertEquals(TestConstant.TEST_REGSTR_NM, dto.getCreatedByNm(), "등록자 이름이 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01 00:00:00", dto.getCreatedAt(), "등록일시가 제대로 매핑되지 않았습니다.");
        // 수정자
        assertEquals(TestConstant.TEST_MDFUSR_ID, dto.getUpdatedById(), "수정자 ID가 제대로 매핑되지 않았습니다.");
        assertEquals(TestConstant.TEST_MDFUSR_NM, dto.getUpdatedByNm(), "수정자 이름이 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01 00:00:00", dto.getUpdatedAt(), "수정일시가 제대로 매핑되지 않았습니다.");
    }

    /* ----- */

    /**
     * entity -> listdto 검증 :: 기본 속성
     */
    @Test
    void testToListDto_checkBasic() throws Exception {
        // Given::
        ClCdEntity clCdEntity = ClCdEntityTestFactory.create();
        // 글분류 코드
        clCdEntity.setClCtgrCd(TestConstant.TEST_CL_CTGR_CD);
        
        // When::
        ClCdDto dto = clCdMapstruct.toDto(clCdEntity);

        // Then::
        assertNotNull(dto, "변환된 dto는 null일 수 없습니다.");
        assertEquals(TestConstant.TEST_CL_CD, dto.getClCd(), "분류 코드 매핑이 제대로 이루어지지 않았습니다.");
        assertEquals(TestConstant.TEST_CL_CD_NM, dto.getClCdNm(), "분류 코드명 매핑이 제대로 이루어지지 않았습니다.");
        assertEquals(TestConstant.TEST_DC, dto.getDc(), "설명 코드명 매핑이 제대로 이루어지지 않았습니다.");
        // 글분류 코드
        assertEquals(TestConstant.TEST_CL_CTGR_CD, dto.getClCtgrCd(), "글분류 코드 매핑이 제대로 이루어지지 않았습니다.");
    }

    /**
     * entity -> dto 변환 검증 :: 등록자/수정자 정보
     */
    @Test
    void testListToDto_checkAuditor() throws Exception {
        // Given::
        ClCdEntity clCdEntity = ClCdEntityTestFactory.create();
        // 등록자
        BaseEntityTestFactoryHelper.setCreatedByInfo(clCdEntity);
        // 수정자
        BaseEntityTestFactoryHelper.setUpdatedByInfo(clCdEntity);

        // When::
        ClCdDto dto = clCdMapstruct.toDto(clCdEntity);

        // Then::
        assertNotNull(dto);
        // 등록자
        assertEquals(TestConstant.TEST_REGSTR_ID, dto.getCreatedBy(), "등록자 ID가 제대로 매핑되지 않았습니다.");
        assertEquals(TestConstant.TEST_REGSTR_NM, dto.getCreatedByNm(), "등록자 이름이 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01 00:00:00", dto.getCreatedAt(), "등록일시가 제대로 매핑되지 않았습니다.");
        // 수정자
        assertEquals(TestConstant.TEST_MDFUSR_ID, dto.getUpdatedById(), "수정자 ID가 제대로 매핑되지 않았습니다.");
        assertEquals(TestConstant.TEST_MDFUSR_NM, dto.getUpdatedByNm(), "수정자 이름이 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01 00:00:00", dto.getUpdatedAt(), "수정일시가 제대로 매핑되지 않았습니다.");
    }

    /* ----- */

    /**
     * dto -> entity 변환 검증 :: 기본 체크
     */
    @Test
    void testToEntity_checkBasic() throws Exception {
        // Given::
        ClCdDto clCdDto = ClCdDtoTestFactory.createClCdDtlDto();
        clCdDto.setClCtgrCd(TestConstant.TEST_CL_CTGR_CD);

        // When::
        ClCdEntity entity = clCdMapstruct.toEntity(clCdDto);

        // Then::
        assertNotNull(entity, "변환된 entity는 null일 수 없습니다.");
        // 글분류 코드
        assertEquals(TestConstant.TEST_CL_CTGR_CD, entity.getClCtgrCd(), "글분류 코드 매핑이 제대로 이루어지지 않았습니다.");
    }


    /* ----- */

    /**
     * update entity from dto 검증
     */
    @Test
    void testUpdateFromDto_checkBasic() throws Exception {
        // Given::
        ClCdEntity entity = ClCdEntityTestFactory.create();
        ClCdDto dto = ClCdDtoTestFactory.createClCdDtlDto_1();
        dto.setClCtgrCd(TestConstant.TEST_CL_CTGR_CD_1);

        // When::
        clCdMapstruct.updateFromDto(dto, entity);

        // Then::
        assertNotNull(entity, "업데이트된 entity는 null일 수 없습니다.");
        assertEquals(TestConstant.TEST_CL_CD_1, dto.getClCd(), "분류 코드 업데이트가 제대로 이루어지지 않았습니다.");
        assertEquals(TestConstant.TEST_CL_CD_NM_1, dto.getClCdNm(), "분류 코드명 업데이트가 제대로 이루어지지 않았습니다.");
        assertEquals(TestConstant.TEST_DC_1, dto.getDc(), "설명 코드명 업데이트가 제대로 이루어지지 않았습니다.");
        // 글분류 코드
        assertEquals(TestConstant.TEST_CL_CTGR_CD_1, dto.getClCtgrCd(), "글분류 코드 업데이트가 제대로 이루어지지 않았습니다.");
    }
}
