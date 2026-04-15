package io.nicheblog.dreamdiary.feature.board.post.model;

import io.nicheblog.dreamdiary.feature.clsf._shared.model.BaseClsfDto;
import io.nicheblog.dreamdiary.feature.clsf.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.feature.clsf.file.model.cmpstn.AtchFileCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.file.model.cmpstn.AtchFileCmpstnModule;
import io.nicheblog.dreamdiary.feature.clsf.managt.model.cmpstn.ManagtCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.managt.model.cmpstn.ManagtCmpstnModule;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.feature.clsf.viewer.model.cmpstn.ViewerCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.viewer.model.cmpstn.ViewerCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.infrastructure.cd.Code;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

/**
 * BoardPostDto
 * <pre>
 *  게시판 게시물 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BoardPostDto
        extends BaseClsfDto
        implements Identifiable<Integer>, AtchFileCmpstnModule, CommentCmpstnModule, TagCmpstnModule, ManagtCmpstnModule, ViewerCmpstnModule {

    /** 게시판 정의 */
    private String boardDef;

       /** 제목 */
    private String title;

    /** 내용 */
    private String content;

    /** 마크다운 처리된 내용 */
    private String markdownContent;

    /** 글분류 코드 */
    @Size(max = 50)
    private String ctgrClCd;

    /** 글분류 코드 */
    @Size(max = 50)
    private String ctgrCd;

    /** 글분류 코드 이름 */
    @Size(max = 50)
    private String ctgrNm;

    /** 글분류 존재 여부 */
    @Builder.Default
    private Boolean hasCtgrNm = false;

    /** 중요 여부 (Y/N) */
    @Builder.Default
    private String imprtcYn = "N";

    /** 상단고정 여부 (Y/N) */
    @Builder.Default
    private String fxdYn = "N";

    /** 조회수 */
    @Builder.Default
    @Min(value = 0)
    private Integer hitCnt = 0;

    /** 수정권한 */
    @Builder.Default
    @Size(max = 50)
    protected String mdfable = Code.MDFABLE_REGSTR;

    /** 수정 가능 여부 */
    @Builder.Default
    protected Boolean isMdfable = false;

    /**
     * 게시판 게시물 상세 (DTL) Dto.
     */
    @Getter
    @Setter
    @SuperBuilder(toBuilder = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class DTL
            extends BoardPostDto {
        /** 노션 페이지 참조 ID :: UUID */
        // private String notionPageId;

        /** 파일시스템 참조 목록 */
        // private List<FlsysRefDto> flsysRefList;
    }

    /**
     * 게시판 게시물 목록 조회 (LIST) Dto.
     */
    @Getter
    @Setter
    @SuperBuilder(toBuilder = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class LIST
            extends BoardPostDto {
        //
    }

    /* ----- */

    @Override
    public Integer getKey() {
        return this.id;
    }

    /** 위임 :: 첨부파일 모듈 */
    public AtchFileCmpstn file;
    /** 위임 :: 댓글 정보 모듈 */
    public CommentCmpstn comment;
    /** 위임 :: 태그 정보 모듈 */
    public TagCmpstn tag;
    /** 위임 :: 조치 정보 모듈 */
    public ManagtCmpstn managt;
    /** 위임 :: 열람 정보 모듈 */
    public ViewerCmpstn viewer;
}
