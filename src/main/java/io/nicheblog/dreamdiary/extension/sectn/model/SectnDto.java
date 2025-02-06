package io.nicheblog.dreamdiary.extension.sectn.model;

import io.nicheblog.dreamdiary.extension.ContentType;
import io.nicheblog.dreamdiary.extension.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.extension.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.extension.state.model.cmpstn.StateCmpstn;
import io.nicheblog.dreamdiary.extension.state.model.cmpstn.StateCmpstnModule;
import io.nicheblog.dreamdiary.extension.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.extension.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.BasePostDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * SectnDto
 * <pre>
 *  단락 Dto.
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
public class SectnDto
        extends BasePostDto
        implements Identifiable<Integer>, CommentCmpstnModule, TagCmpstnModule, StateCmpstnModule {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final String CONTENT_TYPE = ContentType.SECTN.key;
    /** 필수(Override): 글분류 코드 */
    @Builder.Default
    private static final String CTGR_CL_CD = CONTENT_TYPE + "_CTGR_CD";

    /** 컨텐츠 타입 */
    @Builder.Default
    private String contentType = CONTENT_TYPE;

    /* ----- */

    /** 원글 번호 */
    @Positive
    private Integer refPostNo;

    /** 원글 컨텐츠 타입 */
    @Size(max = 50)
    private String refContentType;

    /** 만료 여부 (Y/N) */
    @Builder.Default
    @Size(min = 1, max = 1)
    @Pattern(regexp = "^[YN]$")
    private String deprcYn = "N";

    /* ----- */

    @Override
    public Integer getKey() {
        return this.postNo;
    }

    /** 위임 :: 댓글 정보 모듈 */
    public CommentCmpstn comment;
    /** 위임 :: 태그 정보 모듈 */
    public TagCmpstn tag;
    /** 위임 :: 상태 관리 모듈 */
    public StateCmpstn state;
}
