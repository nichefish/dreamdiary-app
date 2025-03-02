package io.nicheblog.dreamdiary.domain.jrnl.day.mapstruct;

import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDayEntity;
import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDaySmpEntity;
import io.nicheblog.dreamdiary.domain.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.domain.jrnl.diary.mapstruct.JrnlDiaryMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.dream.mapstruct.JrnlDreamMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseClsfMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * JrnlDayMapstruct
 * <pre>
 *  저널 일자 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, DatePtn.class, StringUtils.class}, builder = @Builder(disableBuilder = true))
public interface JrnlDayMapstruct
        extends BaseClsfMapstruct<JrnlDayDto, JrnlDayDto, JrnlDayEntity> {

    JrnlDayMapstruct INSTANCE = Mappers.getMapper(JrnlDayMapstruct.class);
    JrnlDiaryMapstruct jrnlDiaryMapstruct = JrnlDiaryMapstruct.INSTANCE;
    JrnlDreamMapstruct jrnlDreamMapstruct = JrnlDreamMapstruct.INSTANCE;

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toDto")
    @Mapping(target = "jrnlDt", expression = "java(DateUtils.asStr(entity.getJrnlDt(), DatePtn.DATE))")
    @Mapping(target = "jrnlDtWeekDay", expression = "java(entity.getJrnlDt() != null ? DateUtils.getDayOfWeekChinese(entity.getJrnlDt()) : null)")
    @Mapping(target = "aprxmtDt", expression = "java(DateUtils.asStr(entity.getAprxmtDt(), DatePtn.DATE))")
    @Mapping(target = "stdrdDt", expression = "java(DateUtils.asStr(\"Y\".equals(entity.getDtUnknownYn()) ? entity.getAprxmtDt() : entity.getJrnlDt(), DatePtn.DATE))")
    @Mapping(target = "jrnlDiaryList", expression = "java(jrnlDiaryMapstruct.toDtoList(entity.getJrnlDiaryList()))")
    @Mapping(target = "jrnlDreamList", expression = "java(jrnlDreamMapstruct.toDtoList(entity.getJrnlDreamList()))")
    @Mapping(target = "jrnlElseDreamList", expression = "java(jrnlDreamMapstruct.toDtoList(entity.getJrnlElseDreamList()))")
    JrnlDayDto toDto(final JrnlDayEntity entity) throws Exception;

    /**
     * Entity -> ListDto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return ListDto -- 변환된 ListDto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toListDto")
    @Mapping(target = "jrnlDt", expression = "java(DateUtils.asStr(entity.getJrnlDt(), DatePtn.DATE))")
    @Mapping(target = "jrnlDtWeekDay", expression = "java(entity.getJrnlDt() != null ? DateUtils.getDayOfWeekChinese(entity.getJrnlDt()) : null)")
    @Mapping(target = "aprxmtDt", expression = "java(DateUtils.asStr(entity.getAprxmtDt(), DatePtn.DATE))")
    @Mapping(target = "stdrdDt", expression = "java(DateUtils.asStr(\"Y\".equals(entity.getDtUnknownYn()) ? entity.getAprxmtDt() : entity.getJrnlDt(), DatePtn.DATE))")
    @Mapping(target = "jrnlDiaryList", expression = "java(jrnlDiaryMapstruct.toDtoList(entity.getJrnlDiaryList()))")
    @Mapping(target = "jrnlDreamList", expression = "java(jrnlDreamMapstruct.toDtoList(entity.getJrnlDreamList()))")
    @Mapping(target = "jrnlElseDreamList", expression = "java(jrnlDreamMapstruct.toDtoList(entity.getJrnlElseDreamList()))")
    JrnlDayDto toListDto(final JrnlDayEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toEntity")
    @Mapping(target = "jrnlDt", expression = "java(DateUtils.asDate(dto.getJrnlDt()))")
    @Mapping(target = "aprxmtDt", expression = "java(DateUtils.asDate(dto.getAprxmtDt()))")
    JrnlDayEntity toEntity(final JrnlDayDto dto) throws Exception;

    /**
     * 일반 엔티티를 간소화 엔티티로 변환
     * @param entity 변환할 entity 객체
     * @return SmpEntity -- 변환된 간소화 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    JrnlDaySmpEntity asSmp(final JrnlDayEntity entity) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @Override
    @Mapping(target = "jrnlDt", expression = "java(DateUtils.asDate(dto.getJrnlDt()))")
    @Mapping(target = "aprxmtDt", expression = "java(DateUtils.asDate(dto.getAprxmtDt()))")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final JrnlDayDto dto, final @MappingTarget JrnlDayEntity entity) throws Exception;
}
