package io.nicheblog.dreamdiary.feature.admin.tmplat.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.nicheblog.dreamdiary.auth.intrfc.model.BaseAuditDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * TmplatDto
 * <pre>
 *  저널 엔트리 작성용 사전입력 템플릿 DTO.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmplatDto
        extends BaseAuditDto
        implements Identifiable<Integer> {

    /** 순번 (페이징 목록 표시용) */
    private Long rnum;
    /** 템플릿 ID */
    private Integer id;
    /** 제목 (드롭다운 표시명) */
    private String title;
    /** 내용 (에디터에 삽입되는 HTML 본문) */
    private String content;
    /** 정렬 순서 */
    private Integer sortOrder;
    /** 사용 여부 (Y/N) */
    @Builder.Default
    private String useYn = "Y";

    @Override
    public Integer getKey() {
        return this.id;
    }
}