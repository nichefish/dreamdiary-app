package io.nicheblog.dreamdiary.feature.board.group.model;

import io.nicheblog.dreamdiary.feature.attachable.prefix.model.PrefixDto;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 게시판 관리 화면의 boardKey별 GLOBAL Prefix 목록 DTO.
 * <p>
 * 변경 전에는 같은 Scope를 참조하는 게시판 목록과 공유 여부를 함께 반환했다.
 * 변경 후에는 게시판마다 {@code GLOBAL + boardKey} 목록이 독립적이므로 관리 대상
 * 게시판 정보와 Prefix 목록만 반환하며 Scope 식별자는 계속 노출하지 않는다.
 * </p>
 *
 * @author nichefish
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardPrefixManagementDto {

    /** 관리 대상 게시판 ID */
    private Integer boardId;

    /** 관리 대상 게시판 키 */
    private String boardKey;

    /** 관리 대상 게시판 이름 */
    private String boardName;

    /** 비활성 항목을 포함한 Prefix 목록 */
    @Builder.Default
    private List<PrefixDto> prefixes = new ArrayList<>();
}
