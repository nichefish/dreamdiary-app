package io.nicheblog.dreamdiary.feature.admin.code.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.nicheblog.dreamdiary.auth.intrfc.model.BaseAuditDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodeGroupDto
        extends BaseAuditDto
        implements Identifiable<Integer> {

    private Long rnum;
    private Integer id;
    private String clCd;
    private String clCdNm;
    private String description;
    @Builder.Default
    private String protectedYn = "N";
    private String clCtgrCd;
    private String clCtgrNm;
    List<CodeItemDto> dtlCdList;
    @Builder.Default
    private String useYn = "N";
    @Builder.Default
    private Integer dtlCdCnt = 0;
    @Builder.Default
    private String regYn = "N";

    @Override
    public Integer getKey() {
        return this.id;
    }
}
