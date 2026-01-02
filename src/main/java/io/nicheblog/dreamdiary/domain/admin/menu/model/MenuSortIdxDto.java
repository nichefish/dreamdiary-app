package io.nicheblog.dreamdiary.domain.admin.menu.model;

import io.nicheblog.dreamdiary.global.intrfc.entity.Sortable;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * MenuSortIdxDto
 * <pre>
 *  메뉴 idx 정렬용 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@NoArgsConstructor
public class MenuSortIdxDto
        extends BaseCrudDto
        implements Identifiable<Integer>, Sortable {

    /** 메뉴 번호 */
    private Integer menuNo;
    /** 상위 메뉴 번호 (계층 보호/검증용) */
    private Integer upperMenuNo;
    /** idx */
    private Integer idx;

    @Override
    public Integer getKey() {
        return this.menuNo;
    }
}