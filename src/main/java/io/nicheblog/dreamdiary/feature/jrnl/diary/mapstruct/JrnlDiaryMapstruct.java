package io.nicheblog.dreamdiary.feature.jrnl.diary.mapstruct;

import io.nicheblog.dreamdiary.feature.clsf._shared.mapstruct.BaseClsfMapstruct;
import io.nicheblog.dreamdiary.feature.jrnl.diary.entity.JrnlDiaryEntity;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiaryPostDto;
import io.nicheblog.dreamdiary.feature.jrnl.chapter.entity.JrnlChapterEntity;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.cd.utils.CdUtils;
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
    @Mapping(target = "jrnlChapter", source = "jrnlChapterNo", qualifiedByName = "mapJrnlChapter")
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
    @Mapping(target = "jrnlChapter", source = "jrnlChapterNo", qualifiedByName = "mapJrnlChapter")
    public abstract void updateFromDto(final JrnlDiaryPostDto dto, final @MappingTarget JrnlDiaryEntity entity) throws Exception;

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Named("toDto")
    @Mapping(target = "jrnlChapterNo", source = "jrnlChapter.postNo")
    @Mapping(target = "jrnlDayNo", source = "jrnlChapter.jrnlDayNo")
    @Mapping(target = "stdrdDt", expression = "java(entity.getJrnlChapter().getJrnlDay() != null ? DateUtils.asStr(\"Y\".equals(entity.getJrnlChapter().getJrnlDay().getDtUnknownYn()) ? entity.getJrnlChapter().getJrnlDay().getAprxmtDt() : entity.getJrnlChapter().getJrnlDay().getJrnlDt(), DatePtn.DATE) : null)")
    @Mapping(target = "dtUnknownYn", expression = "java(entity.getJrnlChapter().getJrnlDay() != null ? entity.getJrnlChapter().getJrnlDay().getDtUnknownYn() : \"N\")")
    @Mapping(target = "jrnlDtWeekDay", expression = "java(entity.getJrnlChapter().getJrnlDay() != null && entity.getJrnlChapter().getJrnlDay().getJrnlDt() != null ? DateUtils.getDayOfWeekChinese(entity.getJrnlChapter().getJrnlDay().getJrnlDt()) : null)")
    @Mapping(target = "yy", source = "jrnlChapter.jrnlDay.yy")
    @Mapping(target = "mnth", source = "jrnlChapter.jrnlDay.mnth")
    @Mapping(target = "markdownCn", expression = "java(StringUtils.isEmpty(entity.getCn()) ? \"-\" : MarkdownUtils.markdown(entity.getCn()))")
    public abstract JrnlDiaryDto toDto(final JrnlDiaryEntity entity) throws Exception;

    /**
     * jrnlChapterNo로부터 JrnlChapterEntity 객체 생성
     * @param jrnlChapterNo jrnlChapterNo
     * @return JrnlChapterEntity
     */
    @Named("mapJrnlChapter")
    protected JrnlChapterEntity mapJrnlChapter(final Integer jrnlChapterNo) {
        if (jrnlChapterNo == null) return null;
        return em.getReference(JrnlChapterEntity.class, jrnlChapterNo);
    }
}
