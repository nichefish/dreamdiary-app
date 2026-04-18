package io.nicheblog.dreamdiary.feature.board.group.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BoardSearchParam extends BaseSearchParam {
    //
}
