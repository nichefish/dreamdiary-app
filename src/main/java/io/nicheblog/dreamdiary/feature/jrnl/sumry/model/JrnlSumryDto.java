package io.nicheblog.dreamdiary.feature.jrnl.sumry.model;

import io.nicheblog.dreamdiary.feature.clsf.ContentType;
import io.nicheblog.dreamdiary.feature.clsf._shared.model.BaseClsfDto;
import io.nicheblog.dreamdiary.feature.clsf.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.feature.clsf.sectn.model.cmpstn.SectnCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.sectn.model.cmpstn.SectnCmpstnModule;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

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
public class JrnlSumryDto
        extends BaseClsfDto
        implements Identifiable<Integer>, CommentCmpstnModule, TagCmpstnModule, SectnCmpstnModule {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private String contentType = ContentType.JRNL_SUMRY.key;

    /** 제목 */
    private String title;
    /** 내용 */
    private String cn;
    /** 마크다운 처리된 내용 */
    private String markdownCn;

    /* ----- */

    /** 결산 년도 */
    private Integer yy;

    /** 꿈 일수 */
    private Integer dreamDayCnt;
    /** 꿈 갯수 */
    private Integer dreamCnt;

    /** 꿈 기록 완료 여부 (Y/N) */
    @Builder.Default
    private String dreamComptYn = "N";

    /** 저널 결산 리뷰 목록 */
    private List<JrnlSumryReviewDto> jrnlSumryReviewList;

    /* ----- */

    @Override
    public Integer getKey() {
        return this.postNo;
    }

    /** 위임 :: 댓글 정보 모듈 */
    public CommentCmpstn comment;
    /** 위임 :: 단락 정보 모듈 */
    public SectnCmpstn sectn;
    /** 위임 :: 태그 정보 모듈 */
    public TagCmpstn tag;
}
