package io.nicheblog.dreamdiary.feature.board.post.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.BaseAttachableDto;
import io.nicheblog.dreamdiary.feature.attachable.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.viewer.model.cmpstn.ViewerCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.viewer.model.cmpstn.ViewerCmpstnModule;
import io.nicheblog.dreamdiary.feature.file.model.cmpstn.FileCmpstn;
import io.nicheblog.dreamdiary.feature.file.model.cmpstn.FileCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
        extends BaseAttachableDto
        implements Identifiable<Integer>, FileCmpstnModule, CommentCmpstnModule, TagCmpstnModule, ViewerCmpstnModule {

    /** 제목 */
    private String title;

    /** 내용 */
    private String content;

    /** 마크다운 처리된 내용 */
    private String markdownContent;

    /** 글분류 그룹 코드 */
    @Size(max = 50)
    private String categoryGroupCode;

    /** 글 분류 코드 */
    @Size(max = 50)
    private String categoryCode;

    /** 글분류 코드 이름 */
    @Size(max = 50)
    private String ctgrNm;

    /** 글분류 존재 여부 */
    @Builder.Default
    private Boolean hasCtgrNm = false;

    /**
     * 게시판 게시물 상세 (DTL) Dto.
     */
    @Getter
    @Setter
    @SuperBuilder(toBuilder = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class DTL extends BoardPostDto {
        /** 노션 페이지 참조 ID :: UUID */
        // private String notionPageId;
    }

    /**
     * 게시판 게시물 목록 조회 (LIST) Dto.
     */
    @Getter
    @Setter
    @SuperBuilder(toBuilder = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class LIST extends BoardPostDto {
        //
    }

    /* ----- */

    @Override
    public Integer getKey() {
        return this.id;
    }

    /** 위임 :: 첨부파일 모듈 */
    public FileCmpstn file;
    /** 위임 :: 댓글 정보 모듈 */
    public CommentCmpstn comment;
    /** 위임 :: 태그 정보 모듈 */
    public TagCmpstn tag;
    /** 위임 :: 열람 정보 모듈 */
    public ViewerCmpstn viewer;
}
