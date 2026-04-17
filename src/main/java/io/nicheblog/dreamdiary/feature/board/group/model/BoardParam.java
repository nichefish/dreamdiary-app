package io.nicheblog.dreamdiary.feature.board.group.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseParam;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BoardParam extends BaseParam {

    List<BoardDto> sortOrders;
}
