package io.nicheblog.dreamdiary.domain.board.def.mapstruct;

import io.nicheblog.dreamdiary.domain.board.def.entity.BoardDefEntity;
import io.nicheblog.dreamdiary.domain.board.def.model.BoardDefDto;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseCrudMapstruct;
import io.nicheblog.dreamdiary.global.model.SiteAcsInfo;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * BoardDefMapstruct
 * <pre>
 *  게시판 정의 MapStruct 기반 Mapper 인터페이스.
 *  ※ 게시판 정의(board_def) = 게시판 분류. 게시판 게시물(board_post)을 1:N으로 관리한다.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {Url.class})
public interface BoardDefMapstruct
        extends BaseCrudMapstruct<BoardDefDto, BoardDefDto, BoardDefEntity> {

    BoardDefMapstruct INSTANCE = Mappers.getMapper(BoardDefMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toDto")
    BoardDefDto toDto(final BoardDefEntity entity) throws Exception;


    /**
     * Entity -> ListDto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return ListDto -- 변환된 ListDto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toListDto")
    BoardDefDto toListDto(final BoardDefEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    BoardDefEntity toEntity(final BoardDefDto dto) throws Exception;
    
    /** 
     * 메뉴 정보로 변환
     *
     * @param entity 게시판 정의 엔티티 객체
     * @return SiteAcsInfo -- 변환된 SiteAcsInfo 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Mapping(target = "menuNm", expression = "java(entity.getBoardNm())")
    SiteAcsInfo toMenu(final BoardDefEntity entity) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 DTO 객체
     * @param entity 업데이트할 대상 엔티티 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final BoardDefDto dto, final @MappingTarget BoardDefEntity entity) throws Exception;
}
