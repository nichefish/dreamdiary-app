package io.nicheblog.dreamdiary.feature.user.signup.mapstruct;

import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntity;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserRoleEntity;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserStateEntity;
import io.nicheblog.dreamdiary.feature.user.emplym.mapstruct.UserEmplymMapstruct;
import io.nicheblog.dreamdiary.feature.user.profile.mapstruct.UserProfileMapstruct;
import io.nicheblog.dreamdiary.feature.user.signup.model.UserSignupRequestDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.stream.Collectors;

/**
 * UserSignupRequestMapstruct
 * <pre>
 *  사용자 신규계정 신청 정보 MapStruct 기반 Mapper 인터페이스 (`UserSignupRequestDto` ↔ 영속 모델).
 * </pre>
 *
 * 명명 규약: `user_signup_request` 대응 매핑만 {@code UserSignupRequest*} 접두사를 쓴다.
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, CollectionUtils.class, Collectors.class, UserStateEntity.class, UserProfileMapstruct.class, UserEmplymMapstruct.class, UserRoleEntity.class}, builder = @Builder(disableBuilder = true))
public interface UserSignupRequestMapstruct
        extends BaseMapstruct<UserSignupRequestDto, UserEntity> {

    UserSignupRequestMapstruct INSTANCE = Mappers.getMapper(UserSignupRequestMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Mapping(target = "password", expression = "java(null)")      // Dto로 패스워드 전달하지 않음
    @Mapping(target = "emailId", expression = "java(StringUtils.isNotEmpty(entity.getEmail()) ? entity.getEmail().substring(0, entity.getEmail().indexOf('@')) : \"\")")
    @Mapping(target = "emailDomain", expression = "java(StringUtils.isNotEmpty(entity.getEmail()) ? entity.getEmail().substring(entity.getEmail().indexOf('@')+1) : \"\")")
    @Mapping(target = "roleKeyList", expression = "java(CollectionUtils.isEmpty(entity.getUserRoles()) ? java.util.List.of() : entity.getUserRoles().stream().map(UserRoleEntity::getRoleKey).collect(Collectors.toList()))")
    @Mapping(target = "allowedIpListStr", expression = "java(CollectionUtils.isEmpty(entity.getAllowedIpStrList()) ? null : String.join(\",\", entity.getAllowedIpStrList()))")      // 접속IP tagify 문자열 세팅
    UserSignupRequestDto toDto(final UserEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Mapping(target = "email", expression = "java(dto.getEmailId() + \"@\" + dto.getEmailDomain())")
    @Mapping(target = "allowedIpList", expression = "java(dto.getAllowedIpListStr())")      // tagify 문자열 파싱
    @Mapping(target = "profile", expression = "java(UserProfileMapstruct.INSTANCE.toEntity(dto.getProfile()))")
    @Mapping(target = "emplym", expression = "java(UserEmplymMapstruct.INSTANCE.toEntity(dto.getEmplym()))")
    UserEntity toEntity(final UserSignupRequestDto dto) throws Exception;
}
