package io.nicheblog.dreamdiary.feature.jrnl.sumry.model;

import io.nicheblog.dreamdiary.feature.clsf.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.feature.clsf.sectn.model.cmpstn.SectnCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.sectn.model.cmpstn.SectnCmpstnModule;
import io.nicheblog.dreamdiary.feature.clsf.shared.model.BaseClsfDto;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.intrfc.model.cmpstn.AtchFileCmpstn;
import io.nicheblog.dreamdiary.global.intrfc.model.cmpstn.AtchFileCmpstnModule;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * JrnlSumryDto
 * <pre>
 *  저널 결산 Dto.
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
public class JrnlSumryReviewDto
        extends BaseClsfDto
        implements Identifiable<Integer>, CommentCmpstnModule, TagCmpstnModule, SectnCmpstnModule, AtchFileCmpstnModule {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private String contentType = ContentType.JRNL_SUMRY_REVIEW.key;

    /** 제목 */
    protected String title;
    /** 내용 */
    protected String cn;
    /** 마크다운 처리된 내용 */
    private String markdownCn;

    /* ----- */

    /** 저널 결산 번호  */
    private Integer jrnlSumryNo;
    /** 결산 년도 */
    private Integer yy;

    /* ----- */

    @Override
    public Integer getKey() {
        return this.postNo;
    }

    /** 위임 :: 첨부파일 모듈 */
    public AtchFileCmpstn file;
    /** 위임 :: 댓글 정보 모듈 */
    public CommentCmpstn comment;
    /** 위임 :: 단락 정보 모듈 */
    public SectnCmpstn sectn;
    /** 위임 :: 태그 정보 모듈 */
    public TagCmpstn tag;
}
