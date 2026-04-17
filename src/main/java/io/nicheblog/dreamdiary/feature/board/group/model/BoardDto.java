package io.nicheblog.dreamdiary.feature.board.group.model;

import io.nicheblog.dreamdiary.auth.intrfc.model.BaseAuditDto;
import io.nicheblog.dreamdiary.global.intrfc.entity.Sortable;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    @Size(max = 30)
    private String categoryGroupCode;

    @Size(max = 2000)
    private String description;

    @Builder.Default
    private Integer sortOrder = 0;

    @Builder.Default
    private String useYn = "Y";

    @Override
    public Integer getKey() {
        return this.id;
    }
}
