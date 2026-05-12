package io.nicheblog.dreamdiary.feature.admin.menu.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * MenuTreeMoveItemDto
 * <pre>
 *  메뉴 트리 이동 시 형제 순서를 전달하는 dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@NoArgsConstructor
public class MenuTreeMoveItemDto {

    /** 메뉴 번호 */
    private Integer id;

    /** 정렬 순서 */
    private Integer sortOrder;
}
