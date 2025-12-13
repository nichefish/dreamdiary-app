package io.nicheblog.dreamdiary.domain.board.post.mapstruct;

import io.nicheblog.dreamdiary.domain.board.post.entity.BoardPostEntity;
import io.nicheblog.dreamdiary.domain.board.post.entity.BoardPostSmpEntity;
import io.nicheblog.dreamdiary.domain.board.post.model.BoardPostDto;
import io.nicheblog.dreamdiary.extension.clsf.comment.mapstruct.embed.CommentEmbedMapstruct;
import io.nicheblog.dreamdiary.extension.clsf.managt.mapstruct.embed.ManagtEmbedMapstruct;
import io.nicheblog.dreamdiary.extension.clsf.tag.mapstruct.embed.TagEmbedMapstruct;
import io.nicheblog.dreamdiary.extension.clsf.viewer.mapstruct.embed.ViewerEmbedMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseClsfMapstruct;
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
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, MarkdownUtils.class, CommentEmbedMapstruct.class, ViewerEmbedMapstruct.class, ManagtEmbedMapstruct.class, TagEmbedMapstruct.class}, builder = @Builder(disableBuilder = true))
public interface BoardPostMapstruct
        extends BaseWriteMapstruct<BoardPostDto, BoardPostEntity>, BaseClsfMapstruct<BoardPostDto, BoardPostEntity> {

    BoardPostMapstruct INSTANCE = Mappers.getMapper(BoardPostMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Mapping(target = "ctgrClCd", expression = "java((entity.getBoardDefInfo() != null) ? entity.getBoardDefInfo().getCtgrClCd() : null)")
    @Mapping(target = "markdownCn", expression = "java(StringUtils.isEmpty(entity.getCn()) ? \"-\" : MarkdownUtils.markdown(entity.getCn()))")
    BoardPostDto toDto(final BoardPostEntity entity) throws Exception;

    /**
     * SmpEntity -> Dto 변환
     *
     * @param entity 변환할 SmpEntity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Mapping(target = "ctgrClCd", expression = "java((entity.getBoardDefInfo() != null) ? entity.getBoardDefInfo().getCtgrClCd() : null)")
    @Mapping(target = "markdownCn", expression = "java(StringUtils.isEmpty(entity.getCn()) ? \"-\" : MarkdownUtils.markdown(entity.getCn()))")
    BoardPostDto toDto(final BoardPostSmpEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    @Mapping(target = "cn", expression = "java(MarkdownUtils.normalize(dto.getCn()))")
    BoardPostEntity toEntity(final BoardPostDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "cn", expression = "java(MarkdownUtils.normalize(dto.getCn()))")
    void updateFromDto(final BoardPostDto dto, final @MappingTarget BoardPostEntity entity) throws Exception;
}
