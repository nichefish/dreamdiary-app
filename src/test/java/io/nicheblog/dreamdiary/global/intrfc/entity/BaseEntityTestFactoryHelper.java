package io.nicheblog.dreamdiary.global.intrfc.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditEntity;
import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditRegEntity;
import io.nicheblog.dreamdiary.auth.security.entity.AuditorInfo;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * BaseEntityTestHelper
 * <pre>
 *  테스트 Entity 기본속성 세팅 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class BaseEntityTestFactoryHelper {

    /**
     * 등록자 정보 세팅
     */
    public static void setCreatedByInfo(Object obj) throws Exception {
        if (!(obj instanceof BaseAuditRegEntity)) return;

        ((BaseAuditRegEntity) obj).setCreatedAt(DateUtils.asDate("2000-01-01"));
        ((BaseAuditRegEntity) obj).setCreatedBy(TestConstant.TEST_REGSTR_ID);
        ((BaseAuditRegEntity) obj).setCreatedByInfo(AuditorInfo.builder()
                .username(TestConstant.TEST_REGSTR_ID)
                .nickNm(TestConstant.TEST_REGSTR_NM)
                .build());
    }

    /**
     * 수정자 정보 세팅
     */
    public static void setUpdatedByInfo(Object obj) throws Exception {
        if (!(obj instanceof BaseAuditEntity)) return;

        ((BaseAuditEntity) obj).setUpdatedAt(DateUtils.asDate("2000-01-01"));
        ((BaseAuditEntity) obj).setUpdatedBy(TestConstant.TEST_MDFUSR_ID);
        ((BaseAuditEntity) obj).setUpdatedByInfo(AuditorInfo.builder()
                .username(TestConstant.TEST_MDFUSR_ID)
                .nickNm(TestConstant.TEST_MDFUSR_NM)
                .build());
    }
}
