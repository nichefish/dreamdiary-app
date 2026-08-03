package io.nicheblog.dreamdiary.feature.journal.thread.mapstruct;

import io.nicheblog.dreamdiary.feature.attachable._shared.mapstruct.BaseAttachableMapstruct;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.model.PrefixDto;
import io.nicheblog.dreamdiary.feature.journal.thread.entity.JournalThreadEntity;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * JournalThreadMapstruct
 * <pre>
 *  공지사항 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, MarkdownUtils.class}, builder = @Builder(disableBuilder = true))
public interface JournalThreadMapstruct
        extends BaseWriteMapstruct<JournalThreadDto, JournalThreadEntity>, BaseAttachableMapstruct<JournalThreadDto, JournalThreadEntity> {

    JournalThreadMapstruct INSTANCE = Mappers.getMapper(JournalThreadMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    @Mapping(target = "markdownContent", expression = "java(StringUtils.isEmpty(entity.getContent()) ? \"-\" : MarkdownUtils.markdown(entity.getContent()))")
    @Mapping(target = "prefix", expression = "java(selectedPrefixToDto(entity))")
    @Mapping(target = "prefixId", expression = "java(entity.getSelectedPrefixId())")
    JournalThreadDto toDto(final JournalThreadEntity entity) throws Exception;

    /**
     * 스레드가 선택한 말머리(prefix_content 연결)를 표시용 DTO로 변환한다.
     * 콘텐츠는 prefix FK를 직접 들지 않으므로 PrefixEmbed의 선택 연결에서 조립한다.
     *
     * @param entity 저널 스레드 엔티티
     * @return 선택 말머리 DTO. 선택이 없으면 {@code null}.
     */
    default PrefixDto selectedPrefixToDto(final JournalThreadEntity entity) {
        final PrefixEntity prefix = entity.getSelectedPrefix();
        if (prefix == null) return null;
        return PrefixDto.builder()
                .id(prefix.getId())
                .name(prefix.getName())
                .color(prefix.getColor())
                .sortOrder(prefix.getSortOrder())
                .activeYn(prefix.getActiveYn())
                .build();
    }

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    @Mapping(target = "content", expression = "java(MarkdownUtils.normalize(dto.getContent()))")
    @Mapping(target = "prefix", ignore = true)
    JournalThreadEntity toEntity(final JournalThreadDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "content", expression = "java(MarkdownUtils.normalize(dto.getContent()))")
    @Mapping(target = "prefix", ignore = true)
    void updateFromDto(final JournalThreadDto dto, final @MappingTarget JournalThreadEntity entity) throws Exception;
}
