package io.nicheblog.dreamdiary.domain.board.def.model;

import io.nicheblog.dreamdiary.extension.state.model.cmpstn.StateCmpstn;
import io.nicheblog.dreamdiary.extension.state.model.cmpstn.StateCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseAuditDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * BoardDefDto
 * <pre>
 *  게시판 정의 정보 Dto.
 *  ※게시판 정의(board_def) = 게시판 분류. 게시판 게시물(board_post)을 1:N으로 관리한다.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BoardDefDto
        extends BaseAuditDto
        implements Identifiable<String>, StateCmpstnModule {

    /** 게시판 정의 */
    @NotBlank
    @Size(max = 50)
    private String boardDef;

    /** 게시판 이름 */
    @NotBlank
    private String boardNm;

    /** 글분류 분류 코드 */
    @Size(max = 50)
    private String ctgrClCd;

    /** 설명 */
    private String dc;

    /* ----- */

    @Override
    public String getKey() {
        return this.boardDef;
    }

    /** 위임 :: 상태 관리 모듈 */
    public StateCmpstn state;
}