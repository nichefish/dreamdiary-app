package io.nicheblog.dreamdiary.domain.vcatn.papr.model;

import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * VcatnPaprDtoTestFactory
 * <pre>
 *  휴가계획서 테스트 Dto 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class VcatnPaprDtoTestFactory {

    /**
     * 휴가계획서 상세 Dto 생성
     */
    public static VcatnPaprDto createVcatnPaprDtlDto() throws Exception {
        return VcatnPaprDto.builder()
                .build();
    }
}
