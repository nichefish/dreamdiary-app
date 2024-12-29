package io.nicheblog.dreamdiary.domain.admin.menu.mapstruct;

import io.nicheblog.dreamdiary.domain.admin.menu.entity.MenuEntity;
import io.nicheblog.dreamdiary.domain.admin.menu.model.MenuDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseCrudMapstruct;
import io.nicheblog.dreamdiary.global.model.SiteAcsInfo;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * MenuMapstruct
 * <pre>
 *  메뉴 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class}, builder = @Builder(disableBuilder = true))
public interface MenuMapstruct
        extends BaseCrudMapstruct<MenuDto, MenuDto, MenuEntity> {

    MenuMapstruct INSTANCE = Mappers.getMapper(MenuMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toDto")
    @Mapping(target = "subMenuList", expression = "java(toDtoList(entity.getSubMenuList()))")
    @Mapping(target = "upperMenuNm", expression = "java(entity.getUpperMenu() != null ? entity.getUpperMenu().getMenuNm() : null)")
    @Mapping(target = "upperMenuTyCd", expression = "java(entity.getUpperMenu() != null ? entity.getUpperMenu().getMenuTyCd() : null)")
    MenuDto toDto(final MenuEntity entity) throws Exception;

    /**
     * Entity -> ListDto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return ListDto -- 변환된 ListDto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toListDto")
    @Mapping(target = "subMenuList", expression = "java(toDtoList(entity.getSubMenuList()))")
    @Mapping(target = "upperMenuNm", expression = "java(entity.getUpperMenu() != null ? entity.getUpperMenu().getMenuNm() : null)")
    @Mapping(target = "upperMenuTyCd", expression = "java(entity.getUpperMenu() != null ? entity.getUpperMenu().getMenuTyCd() : null)")
    MenuDto toListDto(final MenuEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    MenuEntity toEntity(final MenuDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 DTO 객체
     * @param entity 업데이트할 대상 엔티티 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final MenuDto dto, final @MappingTarget MenuEntity entity) throws Exception;

    /**
     * 메뉴를 사이트 접근 정보로 변환
     *
     * @param menu 메뉴 정보
     * @return SiteAcsInfo -- 변환된 SiteAcsInfo 객체
     */
    SiteAcsInfo toSiteAcsInfo(final MenuDto menu);
}
