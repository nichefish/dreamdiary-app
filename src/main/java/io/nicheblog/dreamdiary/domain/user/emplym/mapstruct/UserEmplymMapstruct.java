package io.nicheblog.dreamdiary.domain.user.emplym.mapstruct;

import io.nicheblog.dreamdiary.domain.user.emplym.entity.UserEmplymEntity;
import io.nicheblog.dreamdiary.domain.user.info.model.emplym.UserEmplymDto;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseMapstruct;
import io.nicheblog.dreamdiary.global.util.crypto.CryptoUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * UserEmplymMapstruct
 * <pre>
 *  사용자 인사정보 MapStruct 기반 Mapper 인터페이스
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {Constant.class, DateUtils.class, StringUtils.class, DatePtn.class, CryptoUtils.class})
public interface UserEmplymMapstruct
        extends BaseMapstruct<UserEmplymDto, UserEmplymEntity> {

    UserEmplymMapstruct INSTANCE = Mappers.getMapper(UserEmplymMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Mapping(target = "ecnyDt", expression = "java(DateUtils.asStr(entity.getEcnyDt(), DatePtn.DATE))")
    @Mapping(target = "retireDt", expression = "java(DateUtils.asStr(entity.getRetireDt(), DatePtn.DATE))")
    @Mapping(target = "emplymEmailId", expression = "java(StringUtils.isNotEmpty(entity.getEmplymEmail()) ? entity.getEmplymEmail().substring(0, entity.getEmplymEmail().indexOf('@')) : \"\")")
    @Mapping(target = "emplymEmailDomain", expression = "java(StringUtils.isNotEmpty(entity.getEmplymEmail()) ? entity.getEmplymEmail().substring(entity.getEmplymEmail().indexOf('@')+1) : \"\")")
    @Mapping(target = "acntNo", expression = "java(StringUtils.isNotEmpty(entity.getAcntNo()) ? CryptoUtils.AES128.decrypt(entity.getAcntNo()) : null)")
    UserEmplymDto toDto(final UserEmplymEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Mapping(target = "apntcYn", expression = "java(Constant.RANK_STAFF.equals(dto.getRankCd()) ? dto.getApntcYn() : null)")
    @Mapping(target = "ecnyDt", expression = "java(DateUtils.asDate(dto.getEcnyDt()))")
    @Mapping(target = "retireDt", expression = "java(\"Y\".equals(dto.getRetireYn()) ? DateUtils.asDate(dto.getRetireDt()) : null)")
    @Mapping(target = "emplymEmail", expression = "java(dto.getEmplymEmailId() + \"@\" + dto.getEmplymEmailDomain())")
    @Mapping(target = "acntNo", expression = "java(StringUtils.isNotEmpty(dto.getAcntNo()) ? CryptoUtils.AES128.encrypt(dto.getAcntNo()) : null)")
    UserEmplymEntity toEntity(final UserEmplymDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "apntcYn", expression = "java(\"STAFF\".equals(dto.getRankCd()) ? dto.getApntcYn() : null)")
    @Mapping(target = "ecnyDt", expression = "java(DateUtils.asDate(dto.getEcnyDt()))")
    @Mapping(target = "retireDt", expression = "java(DateUtils.asDate(dto.getRetireDt()))")
    @Mapping(target = "emplymEmail", expression = "java(dto.getEmplymEmailId() + \"@\" + dto.getEmplymEmailDomain())")
    @Mapping(target = "acntNo", expression = "java(StringUtils.isNotEmpty(dto.getAcntNo()) ?  CryptoUtils.AES128.encrypt(dto.getAcntNo()) : null)")
    void updateFromDto(final UserEmplymDto dto, final @MappingTarget UserEmplymEntity entity) throws Exception;
}