package io.nicheblog.dreamdiary.feature.admin.menu.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * MenuTreeMoveParam
 * <pre>
 *  메뉴 트리 이동 요청 payload.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MenuTreeMoveParam
        extends BaseParam {

    /** 이동한 메뉴 번호 */
    private Integer movedMenuNo;

    /** 기존 부모 메뉴 번호 */
    private Integer sourceUpperMenuNo;

    /** 변경 대상 부모 메뉴 번호 */
    private Integer targetUpperMenuNo;

    /** 영향 받은 형제 그룹 목록 */
    private List<MenuTreeMoveGroupDto> groups;
}
