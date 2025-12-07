package io.nicheblog.dreamdiary.domain.jrnl.entry.mapstruct;

import io.nicheblog.dreamdiary.domain.jrnl.diary.mapstruct.JrnlDiaryMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.entry.entity.JrnlEntryEntity;
import io.nicheblog.dreamdiary.domain.jrnl.entry.model.JrnlEntryDto;
import io.nicheblog.dreamdiary.domain.jrnl.entry.model.JrnlEntrySmpDto;
import io.nicheblog.dreamdiary.extension.cd.utils.CdUtils;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BasePostMapstruct;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

/**
 * JrnlEntryMapstruct
 * <pre>
 *  저널 항목 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, DatePtn.class, MarkdownUtils.class, CdUtils.class}, builder = @Builder(disableBuilder = true))
public interface JrnlEntryMapstruct
        extends BasePostMapstruct<JrnlEntryDto, JrnlEntryDto, JrnlEntryEntity> {

    JrnlEntryMapstruct INSTANCE = Mappers.getMapper(JrnlEntryMapstruct.class);
    JrnlDiaryMapstruct jrnlDiaryMapstruct = JrnlDiaryMapstruct.INSTANCE;

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    @Mapping(target = "stdrdDt", expression = "java(entity.getJrnlDay() != null ? DateUtils.asStr(\"Y\".equals(entity.getJrnlDay().getDtUnknownYn()) ? entity.getJrnlDay().getAprxmtDt() : entity.getJrnlDay().getJrnlDt(), DatePtn.DATE) : null)")
    @Mapping(target = "jrnlDtWeekDay", expression = "java(entity.getJrnlDay() != null && entity.getJrnlDay().getJrnlDt() != null ? DateUtils.getDayOfWeekChinese(entity.getJrnlDay().getJrnlDt()) : null)")
    @Mapping(target = "yy", expression = "java(entity.getJrnlDay() != null ? entity.getJrnlDay().getYy() : null)")
    @Mapping(target = "mnth", expression = "java(entity.getJrnlDay() != null ? entity.getJrnlDay().getMnth() : null)")
    @Mapping(target = "jrnlDiaryList", expression = "java(jrnlDiaryMapstruct.toDtoList(entity.getJrnlDiaryList()))")
    JrnlEntryDto toDto(final JrnlEntryEntity entity) throws Exception;

    /**
     * Entity -> ListDto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return ListDto -- 변환된 ListDto 객체
     */
    @Named("toSmpDto")
    JrnlEntrySmpDto toSmpDto(final JrnlEntryEntity entity) throws Exception;

    /**
     * Entity -> ListDto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return ListDto -- 변환된 ListDto 객체
     */
    @Override
    @Named("toListDto")
    @Mapping(target = "stdrdDt", expression = "java(entity.getJrnlDay() != null ? DateUtils.asStr(\"Y\".equals(entity.getJrnlDay().getDtUnknownYn()) ? entity.getJrnlDay().getAprxmtDt() : entity.getJrnlDay().getJrnlDt(), DatePtn.DATE) : null)")
    @Mapping(target = "jrnlDtWeekDay", expression = "java(entity.getJrnlDay() != null && entity.getJrnlDay().getJrnlDt() != null ? DateUtils.getDayOfWeekChinese(entity.getJrnlDay().getJrnlDt()) : null)")
    @Mapping(target = "yy", expression = "java(entity.getJrnlDay() != null ? entity.getJrnlDay().getYy() : null)")
    @Mapping(target = "mnth", expression = "java(entity.getJrnlDay() != null ? entity.getJrnlDay().getMnth() : null)")
    @Mapping(target = "jrnlDiaryList", expression = "java(jrnlDiaryMapstruct.toDtoList(entity.getJrnlDiaryList()))")
    JrnlEntryDto toListDto(final JrnlEntryEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    JrnlEntryEntity toEntity(final JrnlEntryDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final JrnlEntryDto dto, final @MappingTarget JrnlEntryEntity entity) throws Exception;

    /**
     * EntityList to DtoList
     *
     * @param entityList 변환할 Entity 목록
     * @return {@link List} -- 변환된 Dto 목록
     */
    default List<JrnlEntrySmpDto> toSmpDtoList(final List<JrnlEntryEntity> entityList) {
        if (CollectionUtils.isEmpty(entityList)) return null;
        return entityList.stream()
                .map(entity -> {
                    try {
                        return this.toSmpDto(entity);
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }
}
