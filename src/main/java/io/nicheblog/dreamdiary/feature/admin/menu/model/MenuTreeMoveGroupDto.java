package io.nicheblog.dreamdiary.feature.admin.menu.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * MenuTreeMoveGroupDto
 * <pre>
 *  동일 부모 메뉴를 공유하는 형제 그룹 payload.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@NoArgsConstructor
public class MenuTreeMoveGroupDto {

    /** 부모 메뉴 번호 */
    private Integer parentMenuId;

    /** 부모 하위 형제 목록 */
    private List<MenuTreeMoveItemDto> items;
}
