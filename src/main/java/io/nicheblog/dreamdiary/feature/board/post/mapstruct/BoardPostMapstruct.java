package io.nicheblog.dreamdiary.feature.board.post.mapstruct;

import io.nicheblog.dreamdiary.feature.attachable._shared.mapstruct.BaseAttachableMapstruct;
import io.nicheblog.dreamdiary.feature.attachable.comment.mapstruct.embed.CommentEmbedMapstruct;
import io.nicheblog.dreamdiary.feature.attachable.tag.mapstruct.embed.TagEmbedMapstruct;
import io.nicheblog.dreamdiary.feature.attachable.viewer.mapstruct.embed.ViewerEmbedMapstruct;
import io.nicheblog.dreamdiary.feature.board.post.entity.BoardPostEntity;
import io.nicheblog.dreamdiary.feature.board.post.entity.BoardPostSmpEntity;
import io.nicheblog.dreamdiary.feature.board.post.model.BoardPostDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
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
    @Mapping(target = "categoryGroupCode", expression = "java((entity.getBoardInfo() != null) ? entity.getBoardInfo().getCategoryGroupCode() : null)")
    @Mapping(target = "markdownContent", expression = "java(StringUtils.isEmpty(entity.getContent()) ? \"-\" : MarkdownUtils.markdown(entity.getContent()))")
    BoardPostDto toDto(final BoardPostEntity entity) throws Exception;

    /**
     * SmpEntity -> Dto 변환
     *
     * @param entity 변환할 SmpEntity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Mapping(target = "categoryGroupCode", expression = "java((entity.getBoardInfo() != null) ? entity.getBoardInfo().getCategoryGroupCode() : null)")
    @Mapping(target = "markdownContent", expression = "java(StringUtils.isEmpty(entity.getContent()) ? \"-\" : MarkdownUtils.markdown(entity.getContent()))")
    BoardPostDto toDto(final BoardPostSmpEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    @Mapping(target = "content", expression = "java(MarkdownUtils.normalize(dto.getContent()))")
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
    void updateFromDto(final BoardPostDto dto, final @MappingTarget BoardPostEntity entity) throws Exception;
}
