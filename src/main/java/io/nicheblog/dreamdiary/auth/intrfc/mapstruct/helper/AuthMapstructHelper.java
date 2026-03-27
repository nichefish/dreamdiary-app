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
            final AuditorInfo regstrInfo = baseRegEntity.getRegstrInfo();
            if (regstrInfo != null) {
                baseRegDto.setRegstrNm(regstrInfo.getNickNm());
                baseRegDto.setRegDt(DateUtils.asStr(baseRegEntity.getRegDt(), DatePtn.DATETIME));
                baseRegDto.setIsRegstr(baseRegEntity.isRegstr());
            }
        }

        if (entity instanceof BaseAuditEntity baseAuditEntity && dto instanceof BaseAuditDto baseAuditDto) {
            final AuditorInfo mdfusrInfo = baseAuditEntity.getMdfusrInfo();
            if (mdfusrInfo != null) {
                baseAuditDto.setMdfusrNm(mdfusrInfo.getNickNm());
                baseAuditDto.setMdfDt(DateUtils.asStr(baseAuditEntity.getMdfDt(), DatePtn.DATETIME));
                baseAuditDto.setIsMdfusr(baseAuditEntity.isMdfusr());
            }
        }
    }
}
