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
            // 변경 전: createdByInfo 가 없으면 createdBy·isCreatedBy 를 DTO에 넣지 않아 화면에서 소유 여부를 알 수 없음.
            // 변경 후: 등록자 ID·소유 여부는 AuditorInfo 유무와 무관하게 항상 매핑한다.
            baseRegDto.setCreatedBy(baseRegEntity.getCreatedBy());
            baseRegDto.setIsCreatedBy(baseRegEntity.isCreatedBy());
            if (baseRegEntity.getCreatedAt() != null) {
                baseRegDto.setCreatedAt(DateUtils.asStr(baseRegEntity.getCreatedAt(), DatePtn.DATETIME));
            }
            final AuditorInfo createdByInfo = baseRegEntity.getCreatedByInfo();
            if (createdByInfo != null) {
                baseRegDto.setCreatedByNm(createdByInfo.getNickname());
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
