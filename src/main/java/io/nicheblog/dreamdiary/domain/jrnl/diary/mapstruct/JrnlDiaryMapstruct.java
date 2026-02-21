package io.nicheblog.dreamdiary.domain.jrnl.diary.mapstruct;

import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryEntity;
import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiaryPostDto;
import io.nicheblog.dreamdiary.domain.jrnl.entry.entity.JrnlEntryEntity;
import io.nicheblog.dreamdiary.extension.cd.utils.CdUtils;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseClsfMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * JrnlDiaryMapstruct
 * <pre>
 *  저널 일기 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    imports = { DateUtils.class, StringUtils.class, DatePtn.class, MarkdownUtils.class, CdUtils.class },
    builder = @Builder(disableBuilder = true)
)
public abstract class JrnlDiaryMapstruct
        implements BaseWriteMapstruct<JrnlDiaryPostDto, JrnlDiaryEntity>, BaseClsfMapstruct<JrnlDiaryDto, JrnlDiaryEntity> {

    @PersistenceContext
    protected EntityManager em;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    @Mapping(target = "cn", expression = "java(MarkdownUtils.normalize(dto.getCn()))")
    @Mapping(target = "jrnlEntry", source = "jrnlEntryNo", qualifiedByName = "mapJrnlEntry")
    public abstract JrnlDiaryEntity toEntity(final JrnlDiaryPostDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "cn", expression = "java(MarkdownUtils.normalize(dto.getCn()))")
    @Mapping(target = "jrnlEntry", source = "jrnlEntryNo", qualifiedByName = "mapJrnlEntry")
    public abstract void updateFromDto(final JrnlDiaryPostDto dto, final @MappingTarget JrnlDiaryEntity entity) throws Exception;

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Named("toDto")
    @Mapping(target = "jrnlEntryNo", source = "jrnlEntry.postNo")
    @Mapping(target = "jrnlDayNo", source = "jrnlEntry.jrnlDayNo")
    @Mapping(target = "stdrdDt", expression = "java(entity.getJrnlEntry().getJrnlDay() != null ? DateUtils.asStr(\"Y\".equals(entity.getJrnlEntry().getJrnlDay().getDtUnknownYn()) ? entity.getJrnlEntry().getJrnlDay().getAprxmtDt() : entity.getJrnlEntry().getJrnlDay().getJrnlDt(), DatePtn.DATE) : null)")
    @Mapping(target = "dtUnknownYn", expression = "java(entity.getJrnlEntry().getJrnlDay() != null ? entity.getJrnlEntry().getJrnlDay().getDtUnknownYn() : \"N\")")
    @Mapping(target = "jrnlDtWeekDay", expression = "java(entity.getJrnlEntry().getJrnlDay() != null && entity.getJrnlEntry().getJrnlDay().getJrnlDt() != null ? DateUtils.getDayOfWeekChinese(entity.getJrnlEntry().getJrnlDay().getJrnlDt()) : null)")
    @Mapping(target = "yy", source = "jrnlEntry.jrnlDay.yy")
    @Mapping(target = "mnth", source = "jrnlEntry.jrnlDay.mnth")
    @Mapping(target = "markdownCn", expression = "java(StringUtils.isEmpty(entity.getCn()) ? \"-\" : MarkdownUtils.markdown(entity.getCn()))")
    public abstract JrnlDiaryDto toDto(final JrnlDiaryEntity entity) throws Exception;

    /**
     * jrnlEntryNo로부터 JrnlEntryEntity 객체 생성
     * @param jrnlEntryNo jrnlEntryNo
     * @return JrnlEntryEntity
     */
    @Named("mapJrnlEntry")
    protected JrnlEntryEntity mapJrnlEntry(final Integer jrnlEntryNo) {
        if (jrnlEntryNo == null) return null;
        return em.getReference(JrnlEntryEntity.class, jrnlEntryNo);
    }
}
