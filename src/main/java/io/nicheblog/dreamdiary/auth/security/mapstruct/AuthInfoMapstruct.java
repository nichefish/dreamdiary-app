package io.nicheblog.dreamdiary.auth.security.mapstruct;

import io.nicheblog.dreamdiary.auth.security.entity.AuditorInfo;
import io.nicheblog.dreamdiary.auth.security.model.AuthInfo;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntity;
import io.nicheblog.dreamdiary.feature.user.profile.mapstruct.UserProfileMapstruct;
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
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, UserProfileMapstruct.class}, uses = AuthInfoRoleFillHelper.class)
public interface AuthInfoMapstruct
        extends BaseMapstruct<AuthInfo, UserEntity> {

    AuthInfoMapstruct INSTANCE = Mappers.getMapper(AuthInfoMapstruct.class);

    /**
     * Entity -> Dto 변환
     * 
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Mapping(target = "lockedYn", expression = "java(entity.acntStus.getLockedYn())")
    @Mapping(target = "lockExpiresAt", expression = "java(entity.acntStus.getLockExpiresAt())")
    @Mapping(target = "lastLoginAt", expression = "java(entity.acntStus.getLastLoginAt() != null ? entity.acntStus.getLastLoginAt() : entity.getCreatedAt())")       // 최종접속일 또는 등록일
    @Mapping(target = "passwordChangedAt", expression = "java(entity.acntStus.getPasswordChangedAt() != null ? entity.acntStus.getPasswordChangedAt() : entity.getCreatedAt())")          // 최종비밀번호변경일 또는 등록일
    @Mapping(target = "needsPasswordReset", expression = "java(entity.acntStus.getNeedsPasswordReset())")
    @Mapping(target = "passwordResetTokenIssuedAt", expression = "java(entity.acntStus.getPasswordResetTokenIssuedAt())")
    @Mapping(target = "profile", expression = "java(UserProfileMapstruct.INSTANCE.toDto(entity.getProfile()))")
    @Mapping(target = "userProfileId", expression = "java(entity.getProfile() != null ? entity.getProfile().getUserProfileId() : null)")
    @Mapping(target = "roles", ignore = true)
    AuthInfo toDto(final UserEntity entity) throws Exception;

    /**
     * toAuditorInfo
     */
    AuditorInfo toAuditorInfo(final UserEntity userEntity);
}
