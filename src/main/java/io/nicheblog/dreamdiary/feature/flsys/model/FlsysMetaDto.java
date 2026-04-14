package io.nicheblog.dreamdiary.feature.flsys.model;

import io.nicheblog.dreamdiary.feature.clsf._shared.model.BaseClsfDto;
import io.nicheblog.dreamdiary.feature.clsf.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.comment.model.cmpstn.CommentCmpstnModule;
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

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

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
        extends BaseClsfDto
        implements Identifiable<Integer>, CommentCmpstnModule, TagCmpstnModule, ManagtCmpstnModule, ViewerCmpstnModule {

    /** 글 번호 */
    @Positive
    private Integer id;

    /** 파일절대경로 */
    private String filePath;

    /** 상위파일절대경로 */
    private String upperFilePath;

       /** 제목 */
    private String title;

    /** 내용 */
    private String cn;

    /** 마크다운 처리된 내용 */
    private String markdownCn;

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

    /** 수정권한 */
    @Builder.Default
    @Size(max = 50)
    protected String mdfable = Code.MDFABLE_REGSTR;

    /** 수정 가능 여부 */
    @Builder.Default
    protected Boolean isMdfable = false;

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

    @Override
    public Integer getKey() {
        return this.id;
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
