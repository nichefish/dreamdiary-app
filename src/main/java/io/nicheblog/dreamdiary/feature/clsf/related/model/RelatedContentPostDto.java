package io.nicheblog.dreamdiary.feature.clsf.related.model;

import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.related.type.RelationType;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * RelatedContentPostDto
 * <pre>
 *  관련글 등록 요청 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class RelatedContentPostDto {

    @NotNull
    @Positive
    private Integer srcPostNo;

    @NotNull
    @Builder.Default
    private String srcContentType = ContentType.JRNL_DIARY.key;

    @NotNull
    @Positive
    private Integer targetPostNo;

    @NotNull
    @Builder.Default
    private String targetContentType = ContentType.JRNL_DREAM.key;

    @NotNull
    @Builder.Default
    @Size(max = 30)
    private String relationType = RelationType.REFERENCE.key;

    @Size(max = 255)
    private String reason;
}
