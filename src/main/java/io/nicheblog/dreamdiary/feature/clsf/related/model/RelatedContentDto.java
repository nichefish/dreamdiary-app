package io.nicheblog.dreamdiary.feature.clsf.related.model;

import io.nicheblog.dreamdiary.auth.intrfc.model.BaseAuditRegDto;
import io.nicheblog.dreamdiary.feature.clsf.related.type.RelationOriginType;
import io.nicheblog.dreamdiary.feature.clsf.related.type.RelationType;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * RelatedContentDto
 * <pre>
 *  관련글 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class RelatedContentDto
        extends BaseAuditRegDto
        implements Identifiable<Integer> {

    @Positive
    private Integer id;

    @Positive
    private Integer leftPostNo;

    @Size(max = 30)
    private String leftContentType;

    @Positive
    private Integer rightPostNo;

    @Size(max = 30)
    private String rightContentType;

    @Builder.Default
    @Size(max = 30)
    private String relationType = RelationType.REFERENCE.key;

    @Size(max = 255)
    private String reason;

    @Builder.Default
    @Size(max = 20)
    private String originType = RelationOriginType.MANUAL.key;

    private Integer targetPostNo;
    private String targetContentType;
    private String targetTitle;

    @Override
    public Integer getKey() {
        return this.id;
    }
}
