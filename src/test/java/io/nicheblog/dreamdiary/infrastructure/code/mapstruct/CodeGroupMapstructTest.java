package io.nicheblog.dreamdiary.infrastructure.code.mapstruct;

import io.nicheblog.dreamdiary.feature.admin.code.mapstruct.CodeGroupMapstruct;
import io.nicheblog.dreamdiary.feature.admin.code.model.CodeGroupDto;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseEntityTestFactoryHelper;
import io.nicheblog.dreamdiary.infrastructure.code.entity.CodeGroupEntity;
import io.nicheblog.dreamdiary.infrastructure.code.entity.CodeGroupEntityTestFactory;
import io.nicheblog.dreamdiary.infrastructure.code.model.CodeGroupDtoTestFactory;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@Log4j2
class CodeGroupMapstructTest {

    private final CodeGroupMapstruct codeGroupMapstruct = CodeGroupMapstruct.INSTANCE;

    @Test
    void testToDto_checkBasic() throws Exception {
        CodeGroupEntity entity = CodeGroupEntityTestFactory.create();
        entity.setClCtgrCd(TestConstant.TEST_CL_CTGR_CD);

        CodeGroupDto dto = codeGroupMapstruct.toDto(entity);

        assertNotNull(dto);
        assertEquals(TestConstant.TEST_CL_CD, dto.getClCd());
        assertEquals(TestConstant.TEST_CL_CD_NM, dto.getClCdNm());
        assertEquals(TestConstant.TEST_DC, dto.getDc());
        assertEquals(TestConstant.TEST_CL_CTGR_CD, dto.getClCtgrCd());
    }

    @Test
    void testToDto_checkAuditor() throws Exception {
        CodeGroupEntity entity = CodeGroupEntityTestFactory.create();
        BaseEntityTestFactoryHelper.setCreatedByInfo(entity);
        BaseEntityTestFactoryHelper.setUpdatedByInfo(entity);

        CodeGroupDto dto = codeGroupMapstruct.toDto(entity);

        assertNotNull(dto);
        assertEquals(TestConstant.TEST_REGSTR_ID, dto.getCreatedBy());
        assertEquals(TestConstant.TEST_REGSTR_NM, dto.getCreatedByNm());
        assertEquals("2000-01-01 00:00:00", dto.getCreatedAt());
        assertEquals(TestConstant.TEST_MDFUSR_ID, dto.getUpdatedBy());
        assertEquals(TestConstant.TEST_MDFUSR_NM, dto.getUpdatedByNm());
        assertEquals("2000-01-01 00:00:00", dto.getUpdatedAt());
    }

    @Test
    void testToEntity_checkBasic() throws Exception {
        CodeGroupDto dto = CodeGroupDtoTestFactory.createCodeGroupDto();
        dto.setClCtgrCd(TestConstant.TEST_CL_CTGR_CD);

        CodeGroupEntity entity = codeGroupMapstruct.toEntity(dto);
        assertNotNull(entity);
        assertEquals(TestConstant.TEST_CL_CTGR_CD, entity.getClCtgrCd());
    }

    @Test
    void testUpdateFromDto_checkBasic() throws Exception {
        CodeGroupEntity entity = CodeGroupEntityTestFactory.create();
        CodeGroupDto dto = CodeGroupDtoTestFactory.createCodeGroupDto_1();
        dto.setClCtgrCd(TestConstant.TEST_CL_CTGR_CD_1);

        codeGroupMapstruct.updateFromDto(dto, entity);

        assertNotNull(entity);
        assertEquals(TestConstant.TEST_CL_CD_1, dto.getClCd());
        assertEquals(TestConstant.TEST_CL_CD_NM_1, dto.getClCdNm());
        assertEquals(TestConstant.TEST_DC_1, dto.getDc());
        assertEquals(TestConstant.TEST_CL_CTGR_CD_1, dto.getClCtgrCd());
    }
}
