package io.nicheblog.dreamdiary.feature.user.info.mapstruct;

import io.nicheblog.dreamdiary.feature.user.emplym.mapstruct.UserEmplymMapstruct;
import io.nicheblog.dreamdiary.feature.user.info.entity.UserAuthRoleEntity;
import io.nicheblog.dreamdiary.feature.user.info.entity.UserEntity;
import io.nicheblog.dreamdiary.feature.user.info.entity.UserStateEntity;
import io.nicheblog.dreamdiary.feature.user.info.model.UserDto;
import io.nicheblog.dreamdiary.feature.user.profile.mapstruct.UserProfileMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseReadMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

/**
 * UserMapstruct
 * <pre>
 *  사용자(계정) 정보 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, CollectionUtils.class, UserStateEntity.class, Collectors.class, UserProfileMapstruct.class, UserEmplymMapstruct.class}, builder = @Builder(disableBuilder = true))
public interface UserMapstruct
        extends BaseWriteMapstruct<UserDto, UserEntity>, BaseReadMapstruct<UserDto, UserEntity> {

    UserMapstruct INSTANCE = Mappers.getMapper(UserMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    @Mapping(target = "password", expression = "java(null)")      // Dto로 패스워드 전달하지 않음
    @Mapping(target = "emailId", expression = "java(StringUtils.isNotEmpty(entity.getEmail()) ? entity.getEmail().substring(0, entity.getEmail().indexOf('@')) : \"\")")
    @Mapping(target = "emailDomain", expression = "java(StringUtils.isNotEmpty(entity.getEmail()) ? entity.getEmail().substring(entity.getEmail().indexOf('@')+1) : \"\")")
    @Mapping(target = "authStrList", expression = "java(entity.getAuthList().stream().map(UserAuthRoleEntity::getAuthCd).collect(Collectors.toList()))")      // 접속IP tagify 문자열 세팅
    @Mapping(target = "useAllowedIp", expression = "java(\"Y\".equals(entity.getUseAllowedIpYn()))")
    @Mapping(target = "allowedIpListStr", expression = "java(CollectionUtils.isEmpty(entity.getAllowedIpStrList()) ? null : String.join(\",\", entity.getAllowedIpStrList()))")      // 접속IP tagify 문자열 세팅
    @Mapping(target = "profile", expression = "java(UserProfileMapstruct.INSTANCE.toDto(entity.getProfile()))")
    @Mapping(target = "emplym", expression = "java(UserEmplymMapstruct.INSTANCE.toDto(entity.getEmplym()))")
    UserDto toDto(final UserEntity entity) throws Exception;

    /**
     * Dto -> ListDto 변환
     *
     * @param dtl 변환할 Dto 객체
     * @return ListDto -- 변환된 ListDto 객체
     */
    UserDto dtlToList(final UserDto dtl) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    @Mapping(target = "email", expression = "java(dto.getEmailId() + \"@\" + dto.getEmailDomain())")
    @Mapping(target = "allowedIpList", expression = "java(dto.getAllowedIpListStr())")      // tagify 문자열 파싱
    @Mapping(target = "authList", expression = "java(dto.getAuthListStr())")        // multiselect 문자열 파싱
    @Mapping(target = "profile", expression = "java(UserProfileMapstruct.INSTANCE.toEntity(dto.getProfile()))")
    @Mapping(target = "emplym", expression = "java(UserEmplymMapstruct.INSTANCE.toEntity(dto.getEmplym()))")
    UserEntity toEntity(final UserDto dto) throws Exception;

    /**
     * UserDto 객체에서 UserEntity로 매핑할 때 추가 필드를 설정합니다.
     *
     * @param dto 매핑할 원본 Dto
     * @param entity 매핑 대상 엔티티
     */
    @AfterMapping
    private void mapToEntityFields(final UserDto dto, final @MappingTarget UserEntity entity) throws Exception {
        // multiselect에서 날아온 권한 문자열 세팅
        String authListStr = dto.getAuthListStr();
        if (!StringUtils.isEmpty(authListStr)) {
            List<String> authStrList = List.of(authListStr.split(","));
            entity.setAuthList(authStrList.stream()
                    .map(UserAuthRoleEntity::new).collect(Collectors.toList()));
        }
    }

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "allowedIpList", expression = "java(dto.getAllowedIpListStr())")      // tagify 문자열 파싱
    @Mapping(target = "authList", expression = "java(dto.getAuthListStr())")        // multiselect 문자열 파싱
    @Mapping(target = "profile", expression = "java(entity.getProfileUpdt(dto.getProfile()))")            // 영속성 유지를 위해 update 로직을 타야 한다.
    @Mapping(target = "emplym", expression = "java(entity.getEmplymUpdt(dto.getEmplym()))")         // 영속성 유지를 위해 update 로직을 타야 한다.
    void updateFromDto(final UserDto dto, final @MappingTarget UserEntity entity) throws Exception;
}
