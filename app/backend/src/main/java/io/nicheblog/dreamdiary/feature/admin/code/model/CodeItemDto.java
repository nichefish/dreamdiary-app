package io.nicheblog.dreamdiary.feature.admin.code.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.nicheblog.dreamdiary.auth.intrfc.model.BaseAuditDto;
import io.nicheblog.dreamdiary.global.intrfc.entity.Sortable;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodeItemDto
        extends BaseAuditDto
        implements Identifiable<Integer>, Sortable {

    private Long rnum;
    private Integer id;
    private String groupCode;
    private String code;
    /** 코드명 (한국어 기본) */
    private String codeName;
    /**
     * 다국어 번역명 (locale → 번역명).
     * 한국어(ko)는 {@link #codeName} 이 단일 기준값이므로 이 맵에 포함하지 않는다.
     * 번역이 없는 locale 은 조회 시 {@link #codeName} 으로 fallback 된다.
     * <p>
     * 변경 전: 영문 전용 {@code codeNameEn} 단일 필드였다. 변경 후: locale 무관 맵으로 일반화해
     * 읽기 모델({@code CodeLookupItem.i18nNames})과 같은 모양을 갖는다.
     * 폼 바인딩은 {@code i18nNames[en]=...} 형식을 사용한다.
     */
    @Builder.Default
    private Map<String, String> i18nNames = new LinkedHashMap<>();
    private String description;
    @Builder.Default
    private String protectedYn = "N";
    @Builder.Default
    private String useYn = "N";
    @Builder.Default
    private Integer sortOrder = 0;
    @Builder.Default
    private String regYn = "N";

    @Override
    public Integer getKey() {
        return this.id;
    }
}
