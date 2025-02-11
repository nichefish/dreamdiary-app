package io.nicheblog.dreamdiary.domain.jrnl.sbjct.model;

import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.extension.clsf.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.extension.clsf.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.extension.clsf.sectn.model.cmpstn.SectnCmpstn;
import io.nicheblog.dreamdiary.extension.clsf.sectn.model.cmpstn.SectnCmpstnModule;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.BasePostDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * JrnlSbjctDto
 * <pre>
 *  저널 주제 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JrnlSbjctDto
        extends BasePostDto
        implements Identifiable<Integer>, CommentCmpstnModule, TagCmpstnModule, SectnCmpstnModule {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.JRNL_SBJCT;
    /** 필수(Override): 글분류 코드 */
    @Builder.Default
    private static final String CTGR_CL_CD = CONTENT_TYPE.name() + "_CTGR_CD";

    /** 컨텐츠 타입 */
    @Builder.Default
    private String contentType = CONTENT_TYPE.key;

    /* ----- */

    /**
     * 내부 값들 합쳐서 풀 타이틀 반환
     */
    // public String getFullTitle() {
    //     String title = this.title;
    //     if (StringUtils.isNotEmpty(this.ctgrNm)) title = "[" + this.ctgrNm + "] " + title;
    //     return title;
    // }

    /* ----- */

    /**
     * 저널 주제 상세 (DTL) Dto.
     */
    @Getter
    @Setter
    @SuperBuilder(toBuilder = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class DTL
            extends JrnlSbjctDto {
        //
    }

    /**
     * 저널 주제 목록 조회 (LIST) Dto.
     */
    @Getter
    @Setter
    @SuperBuilder(toBuilder = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class LIST
            extends JrnlSbjctDto {
        //
    }

    /* ----- */

    @Override
    public Integer getKey() {
        return this.postNo;
    }

    /** 위임 :: 댓글 정보 모듈 */
    public CommentCmpstn comment;
    /** 단락 정보 모듈 (위임) */
    public SectnCmpstn sectn;
    /** 위임 :: 태그 정보 모듈 */
    public TagCmpstn tag;
}
