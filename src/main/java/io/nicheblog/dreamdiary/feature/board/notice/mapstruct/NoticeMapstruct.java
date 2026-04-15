package io.nicheblog.dreamdiary.feature.board.notice.mapstruct;

import io.nicheblog.dreamdiary.feature.board.notice.entity.NoticeEntity;
import io.nicheblog.dreamdiary.feature.board.notice.model.NoticeDto;
import io.nicheblog.dreamdiary.feature.board.notice.model.NoticeXlsxDto;
import io.nicheblog.dreamdiary.feature.clsf._shared.mapstruct.BaseClsfMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.code.utils.CodeUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * NoticeMapstruct
 * <pre>
 *  공지사항 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, MarkdownUtils.class, CodeUtils.class}, builder = @Builder(disableBuilder = true))
public interface NoticeMapstruct
        extends BaseWriteMapstruct<NoticeDto, NoticeEntity>, BaseClsfMapstruct<NoticeDto, NoticeEntity> {

    NoticeMapstruct INSTANCE = Mappers.getMapper(NoticeMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    @Mapping(target = "ctgrNm", expression = "java(CodeUtils.getDtlCdNm(\"NOTICE_CTGR_CD\", entity.getCtgrCd()))")
    @Mapping(target = "markdownContent", expression = "java(StringUtils.isEmpty(entity.getContent()) ? \"-\" : MarkdownUtils.markdown(entity.getContent()))")
    NoticeDto toDto(final NoticeEntity entity) throws Exception;

    /**
     * Entity -> XlsxDto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return XlsxDto - 변환된 XlsxDto 객체
     */
    @Named("toXlsxDto")
    NoticeXlsxDto toXlsxDto(final NoticeEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    @Mapping(target = "content", expression = "java(MarkdownUtils.normalize(dto.getContent()))")
    NoticeEntity toEntity(final NoticeDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "content", expression = "java(MarkdownUtils.normalize(dto.getContent()))")
    void updateFromDto(final NoticeDto dto, final @MappingTarget NoticeEntity entity) throws Exception;
}
