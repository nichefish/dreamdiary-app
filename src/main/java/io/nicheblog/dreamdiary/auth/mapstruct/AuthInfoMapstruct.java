package io.nicheblog.dreamdiary.auth.mapstruct;

import io.nicheblog.dreamdiary.auth.entity.AuditorInfo;
import io.nicheblog.dreamdiary.domain.user.info.entity.UserEntity;
import io.nicheblog.dreamdiary.domain.user.profl.mapstruct.UserProflMapstruct;
import io.nicheblog.dreamdiary.auth.model.AuthInfo;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * AuthInfoMapstruct
 * <pre>
 *  사용자 인증 정보 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, UserProflMapstruct.class})
public interface AuthInfoMapstruct
        extends BaseMapstruct<AuthInfo, UserEntity> {

    AuthInfoMapstruct INSTANCE = Mappers.getMapper(AuthInfoMapstruct.class);

    /**
     * Entity -> Dto 변환
     * 
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Mapping(target = "lockedYn", expression = "java(entity.acntStus.getLockedYn())")
    @Mapping(target = "lstLgnDt", expression = "java(entity.acntStus.getLstLgnDt() != null ? entity.acntStus.getLstLgnDt() : entity.getRegDt())")       // 최종접속일 또는 등록일
    @Mapping(target = "pwChgDt", expression = "java(entity.acntStus.getPwChgDt() != null ? entity.acntStus.getPwChgDt() : entity.getRegDt())")          // 최종비밀번호변경일 또는 등록일
    @Mapping(target = "needsPwReset", expression = "java(entity.acntStus.getNeedsPwReset())")
    @Mapping(target = "cfYn", expression = "java(entity.acntStus.getCfYn())")
    @Mapping(target = "profl", expression = "java(UserProflMapstruct.INSTANCE.toDto(entity.getProfl()))")
    @Mapping(target = "userProflNo", expression = "java(entity.getProfl() != null ? entity.getProfl().getUserProflNo() : null)")
    AuthInfo toDto(final UserEntity entity) throws Exception;

    /**
     * toAuditorInfo
     *
     * @param userEntity
     * @return
     */
    AuditorInfo toAuditorInfo(UserEntity userEntity);
}
