package io.nicheblog.dreamdiary.feature.board.post.mapstruct;

import io.nicheblog.dreamdiary.feature.attachable._shared.mapstruct.BaseAttachableMapstruct;
import io.nicheblog.dreamdiary.feature.attachable.comment.mapstruct.embed.CommentEmbedMapstruct;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.model.PrefixDto;
import io.nicheblog.dreamdiary.feature.attachable.tag.mapstruct.embed.TagEmbedMapstruct;
import io.nicheblog.dreamdiary.feature.attachable.viewer.mapstruct.embed.ViewerEmbedMapstruct;
import io.nicheblog.dreamdiary.feature.board.post.entity.BoardPostEntity;
import io.nicheblog.dreamdiary.feature.board.post.entity.BoardPostSmpEntity;
import io.nicheblog.dreamdiary.feature.board.post.model.BoardPostDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * BoardPostMapstruct
 * <pre>
 *  게시판 게시물 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, MarkdownUtils.class, CommentEmbedMapstruct.class, ViewerEmbedMapstruct.class, TagEmbedMapstruct.class}, builder = @Builder(disableBuilder = true))
public interface BoardPostMapstruct
        extends BaseWriteMapstruct<BoardPostDto, BoardPostEntity>, BaseAttachableMapstruct<BoardPostDto, BoardPostEntity> {

    BoardPostMapstruct INSTANCE = Mappers.getMapper(BoardPostMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Mapping(target = "prefix", expression = "java(selectedPrefixToDto(entity.getSelectedPrefix()))")
    @Mapping(target = "prefixId", expression = "java(entity.getSelectedPrefixId())")
    @Mapping(target = "markdownContent", expression = "java(StringUtils.isEmpty(entity.getContent()) ? \"-\" : MarkdownUtils.markdown(entity.getContent()))")
    BoardPostDto toDto(final BoardPostEntity entity) throws Exception;

    /**
     * SmpEntity -> Dto 변환
     *
     * @param entity 변환할 SmpEntity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Mapping(target = "prefix", expression = "java(selectedPrefixToDto(entity.getSelectedPrefix()))")
    @Mapping(target = "prefixId", expression = "java(entity.getSelectedPrefixId())")
    @Mapping(target = "markdownContent", expression = "java(StringUtils.isEmpty(entity.getContent()) ? \"-\" : MarkdownUtils.markdown(entity.getContent()))")
    BoardPostDto toDto(final BoardPostSmpEntity entity) throws Exception;

    /**
     * prefix_content 연결에서 조립된 선택 Prefix를 표시용 DTO로 변환한다.
     *
     * @param prefix 선택된 Prefix 엔티티
     * @return 표시용 Prefix DTO. 선택이 없으면 null.
     */
    default PrefixDto selectedPrefixToDto(final PrefixEntity prefix) {
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
    BoardPostEntity toEntity(final BoardPostDto dto) throws Exception;

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
    void updateFromDto(final BoardPostDto dto, final @MappingTarget BoardPostEntity entity) throws Exception;
}
