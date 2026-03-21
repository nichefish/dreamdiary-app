package io.nicheblog.dreamdiary.feature.jrnl.intrpt.mapstruct;

import io.nicheblog.dreamdiary.feature.jrnl.dream.entity.JrnlDreamEntity;
import io.nicheblog.dreamdiary.feature.jrnl.intrpt.entity.JrnlIntrptEntity;
import io.nicheblog.dreamdiary.feature.jrnl.intrpt.model.JrnlIntrptDto;
import io.nicheblog.dreamdiary.infrastructure.cd.utils.CdUtils;
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
 * JrnlIntrptMapstruct
 * <pre>
 *  저널 해석 MapStruct 기반 Mapper 인터페이스.
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
public abstract class JrnlIntrptMapstruct
        implements BaseWriteMapstruct<JrnlIntrptDto, JrnlIntrptEntity>, BaseClsfMapstruct<JrnlIntrptDto, JrnlIntrptEntity> {

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
    @Mapping(target = "jrnlDream", source = "jrnlDreamNo", qualifiedByName = "mapJrnlDream")
    public abstract JrnlIntrptEntity toEntity(final JrnlIntrptDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "cn", expression = "java(MarkdownUtils.normalize(dto.getCn()))")
    @Mapping(target = "jrnlDream", source = "jrnlDreamNo", qualifiedByName = "mapJrnlDream")
    public abstract void updateFromDto(final JrnlIntrptDto dto, final @MappingTarget JrnlIntrptEntity entity) throws Exception;

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    @Mapping(target = "jrnlDreamNo", source = "jrnlDream.postNo")
    @Mapping(target = "jrnlDayNo", source = "jrnlDream.jrnlDayNo")
    @Mapping(target = "stdrdDt", expression = "java((entity.getJrnlDream() != null && entity.getJrnlDream().getJrnlDay() != null) ? DateUtils.asStr(\"Y\".equals(entity.getJrnlDream().getJrnlDay().getDtUnknownYn()) ? entity.getJrnlDream().getJrnlDay().getAprxmtDt() : entity.getJrnlDream().getJrnlDay().getJrnlDt(), DatePtn.DATE) : null)")
    @Mapping(target = "jrnlDtWeekDay", expression = "java((entity.getJrnlDream() != null && entity.getJrnlDream().getJrnlDay() != null) && entity.getJrnlDream().getJrnlDay().getJrnlDt() != null ? DateUtils.getDayOfWeekChinese(entity.getJrnlDream().getJrnlDay().getJrnlDt()) : null)")
    @Mapping(target = "yy", source = "jrnlDream.jrnlDay.yy")
    @Mapping(target = "mnth", source = "jrnlDream.jrnlDay.mnth")
    @Mapping(target = "markdownCn", expression = "java(StringUtils.isEmpty(entity.getCn()) ? \"-\" : MarkdownUtils.markdown(entity.getCn()))")
    public abstract JrnlIntrptDto toDto(final JrnlIntrptEntity entity) throws Exception;

    /**
     * jrnlDreamNo로부터 JrnlDreamEntity 객체 생성
     * @param jrnlDreamNo JrnlDreamNo
     * @return JrnlDreamEntity
     */
    @Named("mapJrnlDream")
    protected JrnlDreamEntity mapJrnlDream(final Integer jrnlDreamNo) {
        if (jrnlDreamNo == null) return null;
        return em.getReference(JrnlDreamEntity.class, jrnlDreamNo);
    }
}
