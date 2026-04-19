package io.nicheblog.dreamdiary.feature.admin.code.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CodeItemParam extends BaseParam {
    private List<CodeItemDto> sortOrders;
}
