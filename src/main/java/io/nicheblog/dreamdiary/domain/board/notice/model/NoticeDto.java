package io.nicheblog.dreamdiary.domain.board.notice.model;

import io.nicheblog.dreamdiary.domain.clsf.ContentType;
import io.nicheblog.dreamdiary.domain.clsf.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.domain.clsf.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.domain.clsf.managt.model.cmpstn.ManagtCmpstn;
import io.nicheblog.dreamdiary.domain.clsf.managt.model.cmpstn.ManagtCmpstnModule;
import io.nicheblog.dreamdiary.domain.clsf.sectn.model.cmpstn.SectnCmpstn;
import io.nicheblog.dreamdiary.domain.clsf.sectn.model.cmpstn.SectnCmpstnModule;
import io.nicheblog.dreamdiary.domain.clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.domain.clsf.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.domain.clsf.viewer.model.cmpstn.ViewerCmpstn;
import io.nicheblog.dreamdiary.domain.clsf.viewer.model.cmpstn.ViewerCmpstnModule;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseClsfDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.intrfc.model.cmpstn.AtchFileCmpstn;
import io.nicheblog.dreamdiary.global.intrfc.model.cmpstn.AtchFileCmpstnModule;
import io.nicheblog.dreamdiary.global.validator.state.UpdateState;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * NoticeDto
 * <pre>
 *  공지사항 Dto.
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
public class NoticeDto
        extends BaseClsfDto
        implements Identifiable<Integer>, AtchFileCmpstnModule, CommentCmpstnModule, SectnCmpstnModule, TagCmpstnModule, ManagtCmpstnModule, ViewerCmpstnModule {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.NOTICE;
    /** 필수(Override): 글분류 코드 */
    @Builder.Default
    private static final String CTGR_CL_CD = CONTENT_TYPE.name() + "_CTGR_CD";

    /** 컨텐츠 타입 */
    @Builder.Default
    private String contentType = CONTENT_TYPE.key;

       /** 제목 */
    protected String title;

    /** 내용 */
    protected String cn;

    /** 마크다운 처리된 내용 */
    protected String markdownCn;

    /** 글분류 코드 */
    @Size(max = 50)
    protected String ctgrClCd;

    /** 글분류 코드 */
    @Size(max = 50)
    protected String ctgrCd;

    /** 글분류 코드 이름 */
    @Size(max = 50)
    protected String ctgrNm;

    /** 글분류 존재 여부 */
    @Builder.Default
    protected Boolean hasCtgrNm = false;

    /** 중요 여부 (Y/N) */
    @Builder.Default
    protected String imprtcYn = "N";

    /** 상단고정 여부 (Y/N) */
    @Builder.Default
    protected String fxdYn = "N";

    /** 조회수 */
    @Builder.Default
    @Min(value = 0)
    protected Integer hitCnt = 0;

    /** 수정권한 */
    @Builder.Default
    @Size(max = 50)
    protected String mdfable = Constant.MDFABLE_REGSTR;

    /** 수정 가능 여부 */
    @Builder.Default
    protected Boolean isMdfable = false;

    /* ----- */

    /**
     * 내부 값들 합쳐서 풀 타이틀 반환
     */
    public String getFullTitle() {
        String title = this.title;
        if (StringUtils.isNotEmpty(this.ctgrNm)) title = "[" + this.ctgrNm + "] " + title;
        if ("Y".equals(this.imprtcYn)) title = "[중요] " + title;
        return title;
    }

    /* ----- */

    /**
     * 공지사항 상세 (DTL) Dto.
     */
    @Getter
    @Setter
    @SuperBuilder(toBuilder = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class DTL
            extends NoticeDto {

        /** 팝업공지 여부 (Y/N) */
        @Builder.Default
        @Pattern(regexp = "^[YN]$", groups = UpdateState.class)
        private String popupYn = "N";

        /** 파일시스템 참조 목록 */
        // private List<FlsysRefDto> flsysRefList;
    }

    /**
     * 공지사항 목록 조회 (LIST) Dto.
     */
    @Getter
    @Setter
    @SuperBuilder(toBuilder = true)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class LIST
            extends NoticeDto {
        //
    }

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
    /** 위임 :: 조치 정보 모듈 */
    public ManagtCmpstn managt;
    /** 위임 :: 열람 정보 모듈 */
    public ViewerCmpstn viewer;
}
