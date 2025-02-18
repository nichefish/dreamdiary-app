package io.nicheblog.dreamdiary.domain.user.reqst.mapstruct;

import io.nicheblog.dreamdiary.domain.user.emplym.mapstruct.UserEmplymMapstruct;
import io.nicheblog.dreamdiary.domain.user.info.entity.UserEntity;
import io.nicheblog.dreamdiary.domain.user.info.entity.UserStusEmbed;
import io.nicheblog.dreamdiary.domain.user.profl.mapstruct.UserProflMapstruct;
import io.nicheblog.dreamdiary.domain.user.reqst.model.UserReqstDto;
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
 * UserReqstMapstruct
 * <pre>
 *  사용자 신규계정 신청 정보 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, CollectionUtils.class, Collectors.class, UserStusEmbed.class, UserProflMapstruct.class, UserEmplymMapstruct.class}, builder = @Builder(disableBuilder = true))
public interface UserReqstMapstruct
        extends BaseMapstruct<UserReqstDto, UserEntity> {

    UserReqstMapstruct INSTANCE = Mappers.getMapper(UserReqstMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Mapping(target = "password", expression = "java(null)")      // Dto로 패스워드 전달하지 않음
    @Mapping(target = "emailId", expression = "java(StringUtils.isNotEmpty(entity.getEmail()) ? entity.getEmail().substring(0, entity.getEmail().indexOf('@')) : \"\")")
    @Mapping(target = "emailDomain", expression = "java(StringUtils.isNotEmpty(entity.getEmail()) ? entity.getEmail().substring(entity.getEmail().indexOf('@')+1) : \"\")")
    @Mapping(target = "authStrList", expression = "java(entity.getAuthList().stream().map(UserAuthRoleEntity::getAuthCd).collect(Collectors.toList()))")      // 접속IP tagify 문자열 세팅
    @Mapping(target = "acsIpListStr", expression = "java(CollectionUtils.isEmpty(entity.getAcsIpStrList()) ? null : String.join(\",\", entity.getAcsIpStrList()))")      // 접속IP tagify 문자열 세팅
    UserReqstDto toDto(final UserEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Mapping(target = "email", expression = "java(dto.getEmailId() + \"@\" + dto.getEmailDomain())")
    @Mapping(target = "acsIpList", expression = "java(dto.getAcsIpListStr())")      // tagify 문자열 파싱
    @Mapping(target = "profl", expression = "java(UserProflMapstruct.INSTANCE.toEntity(dto.getProfl()))")
    @Mapping(target = "emplym", expression = "java(UserEmplymMapstruct.INSTANCE.toEntity(dto.getEmplym()))")
    UserEntity toEntity(final UserReqstDto dto) throws Exception;
}
