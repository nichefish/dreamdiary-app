package io.nicheblog.dreamdiary.feature.board.def.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * BoardDefParam
 * <pre>
 *  게시판 정의 파라미터.
 *  ※게시판 정의(board_def) = 게시판 분류. 게시판 게시물(board_post)을 1:N으로 관리한다.
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
public class BoardDefParam
        extends BaseParam {

    /** 정렬순서 목록 */
    List<BoardDefDto> sortOrders;
}
