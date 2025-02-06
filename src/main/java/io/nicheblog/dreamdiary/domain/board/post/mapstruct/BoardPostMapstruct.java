package io.nicheblog.dreamdiary.domain.board.post.mapstruct;

import io.nicheblog.dreamdiary.domain.board.post.entity.BoardPostEntity;
import io.nicheblog.dreamdiary.domain.board.post.entity.BoardPostSmpEntity;
import io.nicheblog.dreamdiary.domain.board.post.model.BoardPostDto;
import io.nicheblog.dreamdiary.extension.comment.mapstruct.embed.CommentEmbedMapstruct;
import io.nicheblog.dreamdiary.extension.managt.mapstruct.embed.ManagtEmbedMapstruct;
import io.nicheblog.dreamdiary.extension.tag.mapstruct.embed.TagEmbedMapstruct;
import io.nicheblog.dreamdiary.extension.viewer.mapstruct.embed.ViewerEmbedMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseClsfMapstruct;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
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
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, CmmUtils.class, CommentEmbedMapstruct.class, ViewerEmbedMapstruct.class, ManagtEmbedMapstruct.class, TagEmbedMapstruct.class}, builder = @Builder(disableBuilder = true))
public interface BoardPostMapstruct
        extends BaseClsfMapstruct<BoardPostDto.DTL, BoardPostDto.LIST, BoardPostEntity> {

    BoardPostMapstruct INSTANCE = Mappers.getMapper(BoardPostMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Mapping(target = "ctgrClCd", expression = "java((entity.getBoardDefInfo() != null) ? entity.getBoardDefInfo().getCtgrClCd() : null)")
    @Mapping(target = "markdownCn", expression = "java(StringUtils.isEmpty(entity.getCn()) ? \"-\" : CmmUtils.markdown(entity.getCn()))")
    BoardPostDto.DTL toDto(final BoardPostEntity entity) throws Exception;

    /**
     * SmpEntity -> Dto 변환
     *
     * @param entity 변환할 SmpEntity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Mapping(target = "ctgrClCd", expression = "java((entity.getBoardDefInfo() != null) ? entity.getBoardDefInfo().getCtgrClCd() : null)")
    @Mapping(target = "markdownCn", expression = "java(StringUtils.isEmpty(entity.getCn()) ? \"-\" : CmmUtils.markdown(entity.getCn()))")
    BoardPostDto.DTL toDto(final BoardPostSmpEntity entity) throws Exception;

    /**
     * Entity -> ListDto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return ListDto -- 변환된 ListDto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Mapping(target = "ctgrClCd", expression = "java((entity.getBoardDefInfo() != null) ? entity.getBoardDefInfo().getCtgrClCd() : null)")
    @Mapping(target = "markdownCn", expression = "java(StringUtils.isEmpty(entity.getCn()) ? \"-\" : CmmUtils.markdown(entity.getCn()))")
    BoardPostDto.LIST toListDto(final BoardPostEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    BoardPostEntity toEntity(final BoardPostDto.DTL dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 DTO 객체
     * @param entity 업데이트할 대상 엔티티 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final BoardPostDto.DTL dto, final @MappingTarget BoardPostEntity entity) throws Exception;
}
