package io.nicheblog.dreamdiary.feature.admin.code.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.apache.tika.utils.StringUtils;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class CodeGroupPatchDto {
    private String useYn;

    public boolean isAllNull() {
        return StringUtils.isEmpty(useYn);
    }
}
