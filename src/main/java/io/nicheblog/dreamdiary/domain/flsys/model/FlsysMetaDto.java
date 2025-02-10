package io.nicheblog.dreamdiary.domain.flsys.model;

import io.nicheblog.dreamdiary.extension.clsf.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.extension.clsf.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.extension.clsf.managt.model.cmpstn.ManagtCmpstn;
import io.nicheblog.dreamdiary.extension.clsf.managt.model.cmpstn.ManagtCmpstnModule;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.extension.clsf.viewer.model.cmpstn.ViewerCmpstn;
import io.nicheblog.dreamdiary.extension.clsf.viewer.model.cmpstn.ViewerCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.BasePostDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Positive;

/**
 * FlsysMetaDto
 * <pre>
 *  파일시스템 메타 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(of = {"filePath"}, callSuper = false)
public class FlsysMetaDto
        extends BasePostDto
        implements Identifiable<Integer>, CommentCmpstnModule, TagCmpstnModule, ManagtCmpstnModule, ViewerCmpstnModule {

    /** 글 번호 */
    @Positive
    private Integer postNo;

    /** 파일절대경로 */
    private String filePath;

    /** 상위파일절대경로 */
    private String upperFilePath;

    /* ----- */

    /**
     * 생성자.
     *
     * @param filePath 파일 경로 (String)
     */
    public FlsysMetaDto(final String filePath) {
        this.filePath = filePath;
    }

    /* ----- */
    /**
     * 파일시스템 메타 상세 (DTL) Dto.
     */
    @Getter
    @Setter
    @SuperBuilder(toBuilder = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class DTL
            extends FlsysMetaDto {
        //
    }

    /**
     * 파일시스템 메타 목록 조회 (LIST) Dto.
     */
    @Getter
    @Setter
    @SuperBuilder(toBuilder = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @ToString
    public static class LIST
            extends FlsysMetaDto {
        //
    }

    /* ----- */

    @Override
    public Integer getKey() {
        return this.postNo;
    }

    /** 위임 :: 댓글 정보 모듈 */
    public CommentCmpstn comment;
    /** 위임 :: 태그 정보 모듈 */
    public TagCmpstn tag;
    /** 위임 :: 조치 정보 모듈 */
    public ManagtCmpstn managt;
    /** 위임 :: 열람 정보 모듈 */
    public ViewerCmpstn viewer;
}
