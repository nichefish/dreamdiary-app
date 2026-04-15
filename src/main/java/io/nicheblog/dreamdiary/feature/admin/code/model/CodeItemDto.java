package io.nicheblog.dreamdiary.feature.admin.code.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.nicheblog.dreamdiary.auth.intrfc.model.BaseAuditDto;
import io.nicheblog.dreamdiary.global.intrfc.entity.Sortable;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.infrastructure.code.entity.CodeItemKey;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodeItemDto
        extends BaseAuditDto
        implements Identifiable<CodeItemKey>, Sortable {

    private String dtlCd;
    private String dtlCdNm;
    private String description;
    private String clCd;
    private Integer sortOrder;

    @Builder.Default
    private String protectedYn = "N";
    @Builder.Default
    private String useYn = "N";

    @Override
    public CodeItemKey getKey() {
        return new CodeItemKey(this.clCd, this.dtlCd);
    }
}
