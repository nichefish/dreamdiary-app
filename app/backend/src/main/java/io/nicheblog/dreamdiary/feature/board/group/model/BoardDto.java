package io.nicheblog.dreamdiary.feature.board.group.model;

import io.nicheblog.dreamdiary.auth.intrfc.model.BaseAuditDto;
import io.nicheblog.dreamdiary.global.intrfc.entity.Sortable;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BoardDto extends BaseAuditDto implements Identifiable<Integer>, Sortable {

    private Integer id;

    @NotBlank
    @Size(max = 30)
    private String boardKey;

    @NotBlank
    @Size(max = 120)
    private String boardName;

    @Size(max = 2000)
    private String description;

    @Builder.Default
    private Integer sortOrder = 0;

    @Builder.Default
    private String useYn = "Y";

    /**
     * 목록 화면 전용: {@code board_post.content_type} 이 {@link #boardKey} 와 일치하는 행 개수.
     * 등록·수정 요청 본문에서는 채우지 않으며, 서버 목록 조회 시에만 세팅한다.
     */
    private Long postCount;

    @Override
    public Integer getKey() {
        return this.id;
    }
}
