package io.nicheblog.dreamdiary.feature.journal.sumry.model;

import io.nicheblog.dreamdiary.feature.clsf._shared.model.BaseClsfDto;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.feature.clsf.file.model.cmpstn.AtchFileCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.file.model.cmpstn.AtchFileCmpstnModule;
import io.nicheblog.dreamdiary.feature.clsf.sectn.model.cmpstn.SectnCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.sectn.model.cmpstn.SectnCmpstnModule;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * JournalSumryDto
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
public class JournalSumryReviewDto
        extends BaseClsfDto
        implements Identifiable<Integer>, CommentCmpstnModule, TagCmpstnModule, SectnCmpstnModule, AtchFileCmpstnModule {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private String contentType = ContentType.JOURNAL_SUMRY_REVIEW.key;

    /** 제목 */
    private String title;
    /** 내용 */
    private String cn;
    /** 마크다운 처리된 내용 */
    private String markdownCn;

    /* ----- */

    /** 저널 결산 번호  */
    private Integer journalSumryId;
    /** 결산 년도 */
    private Integer yy;

    /* ----- */

    @Override
    public Integer getKey() {
        return this.id;
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

