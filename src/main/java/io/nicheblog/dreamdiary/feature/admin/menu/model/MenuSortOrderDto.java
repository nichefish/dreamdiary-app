package io.nicheblog.dreamdiary.feature.admin.menu.model;

import io.nicheblog.dreamdiary.global.intrfc.entity.Sortable;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * MenuSortOrderDto
 * <pre>
 *  메뉴 정렬용 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@NoArgsConstructor
public class MenuSortOrderDto
        extends BaseCrudDto
        implements Identifiable<Integer>, Sortable {

    /** 메뉴 번호 */
    private Integer id;
    /** 상위 메뉴 번호 (계층 보호/검증용) */
    private Integer upperMenuId;
    /** 정렬 순서 */
    private Integer sortOrder;

    @Override
    public Integer getKey() {
        return this.id;
    }
}
