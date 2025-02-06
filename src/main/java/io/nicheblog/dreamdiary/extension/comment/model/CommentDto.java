package io.nicheblog.dreamdiary.extension.comment.model;

import io.nicheblog.dreamdiary.extension.ContentType;
import io.nicheblog.dreamdiary.extension.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.extension.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.extension.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.extension.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.BasePostDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * CommentDto
 * <pre>
 *  댓글 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CommentDto
        extends BasePostDto
        implements Identifiable<Integer>, CommentCmpstnModule, TagCmpstnModule {

    /** 필수: 컨텐츠 타입 */
    private static final String CONTENT_TYPE = ContentType.COMMENT.key;
    /** 필수(Override): 글분류 코드 */
    private static final String CTGR_CL_CD = CONTENT_TYPE + "_CTGR_CD";

    /** 컨텐츠 타입 */
    @Builder.Default
    @Size(max = 50)
    private String contentType = CONTENT_TYPE;

    /* ----- */

    /** 원글 번호 */
    @Positive
    private Integer refPostNo;

    /** 원글 컨텐츠 타입 */
    @Size(max = 50)
    private String refContentType;

    /* ----- */

    @Override
    public Integer getKey() {
        return this.postNo;
    }

    /** 위임 :: 댓글 정보 모듈 */
    public CommentCmpstn comment;
    /** 위임 :: 태그 정보 모듈 */
    public TagCmpstn tag;
}
