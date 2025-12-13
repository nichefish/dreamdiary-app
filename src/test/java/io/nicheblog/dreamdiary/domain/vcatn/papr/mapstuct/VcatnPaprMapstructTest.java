package io.nicheblog.dreamdiary.domain.vcatn.papr.mapstuct;

import io.nicheblog.dreamdiary.domain.vcatn.papr.entity.VcatnPaprEntity;
import io.nicheblog.dreamdiary.domain.vcatn.papr.mapstruct.VcatnPaprMapstruct;
import io.nicheblog.dreamdiary.domain.vcatn.papr.model.VcatnPaprDto;
import io.nicheblog.dreamdiary.domain.vcatn.papr.model.VcatnPaprDtoTestFactory;
import io.nicheblog.dreamdiary.domain.vcatn.papr.model.VcatnSchdulDto;
import io.nicheblog.dreamdiary.domain.vcatn.schdul.model.VcatnSchdulDtoTestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * VcatnPaprMapstructTest
 * <pre>
 *  휴가계획서 Mapstruct 매핑 테스트 모듈.
 * </pre>
 *
 * @author nichefish
 */
@ActiveProfiles("test")
class VcatnPaprMapstructTest {

    private final VcatnPaprMapstruct vcatnPaprMapstruct = VcatnPaprMapstruct.INSTANCE;

    /**
     * dto -> entity 변환 검증
     */
    @Test
    void testToEntity_checkBasic() throws Exception {
        // Given::
        VcatnPaprDto vcatnPaprDto = VcatnPaprDtoTestFactory.createVcatnPaprDtlDto();

        // When::
        VcatnPaprEntity vcatnPaprEntity = vcatnPaprMapstruct.toEntity(vcatnPaprDto);

        // Then::
        // 일반 필드는 검증할 필요 없음. 로직이 들어가는 부분에 대하여 테스트 진행
        assertNotNull(vcatnPaprEntity);
    }

    /**
     * dto -> entity 변환 검증 :: 하위 엔티티
     */
    @Test
    void testToEntity_checkSchdulList() throws Exception {
        // Given::
        VcatnPaprDto vcatnPaprDto = VcatnPaprDtoTestFactory.createVcatnPaprDtlDto();
        // 휴가 일정 세팅
        VcatnSchdulDto aa  = VcatnSchdulDtoTestFactory.createVcatnSchdulDtlDto();
        VcatnSchdulDto bb  = VcatnSchdulDtoTestFactory.createVcatnSchdulDtlDto();
        vcatnPaprDto.setSchdulList(List.of(aa, bb));

        // When::
        VcatnPaprEntity vcatnPaprEntity = vcatnPaprMapstruct.toEntity(vcatnPaprDto);

        // Then::
        // 일반 필드는 검증할 필요 없음. 로직이 들어가는 부분에 대하여 테스트 진행
        assertNotNull(vcatnPaprEntity);
        assertNotNull(vcatnPaprEntity.getSchdulList());
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