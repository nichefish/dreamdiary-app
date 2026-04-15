package io.nicheblog.dreamdiary.feature.admin.menu.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * MenuParam
 * <pre>
 *  메뉴 인자 파라미터.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MenuParam
        extends BaseParam {

    /** 정렬 순서 배열 */
    private List<MenuSortOrderDto> sortOrders;
}
