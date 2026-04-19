package io.nicheblog.dreamdiary.auth.intrfc.mapstruct.helper;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditEntity;
import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditRegEntity;
import io.nicheblog.dreamdiary.auth.intrfc.model.BaseAuditDto;
import io.nicheblog.dreamdiary.auth.intrfc.model.BaseAuditRegDto;
import io.nicheblog.dreamdiary.auth.security.entity.AuditorInfo;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.experimental.UtilityClass;

/**
 * AuthMapstructHelper
 * <pre>
 *  auth 계층 audit 필드 매핑 전용 Helper.
 * </pre>
 */
@UtilityClass
public final class AuthMapstructHelper {

    /**
     * Map audit fields (entity -> dto).
     */
    public static void mapAuditFields(final Object entity, final Object dto) throws Exception {
        if (entity instanceof BaseAuditRegEntity baseRegEntity && dto instanceof BaseAuditRegDto baseRegDto) {
            final AuditorInfo createdByInfo = baseRegEntity.getCreatedByInfo();
            if (createdByInfo != null) {
                baseRegDto.setCreatedByNm(createdByInfo.getNickname());
                baseRegDto.setCreatedAt(DateUtils.asStr(baseRegEntity.getCreatedAt(), DatePtn.DATETIME));
                baseRegDto.setIsCreatedBy(baseRegEntity.isCreatedBy());
            }
        }

        if (entity instanceof BaseAuditEntity baseAuditEntity && dto instanceof BaseAuditDto baseAuditDto) {
            final AuditorInfo updatedByInfo = baseAuditEntity.getUpdatedByInfo();
            if (updatedByInfo != null) {
                baseAuditDto.setUpdatedByNm(updatedByInfo.getNickname());
                baseAuditDto.setUpdatedAt(DateUtils.asStr(baseAuditEntity.getUpdatedAt(), DatePtn.DATETIME));
                baseAuditDto.setIsUpdatedBy(baseAuditEntity.isUpdatedBy());
            }
        }
    }
}
