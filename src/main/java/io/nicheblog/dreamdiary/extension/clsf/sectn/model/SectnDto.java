package io.nicheblog.dreamdiary.extension.clsf.sectn.model;

import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.extension.clsf.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.extension.clsf.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.extension.clsf.state.model.cmpstn.StateCmpstn;
import io.nicheblog.dreamdiary.extension.clsf.state.model.cmpstn.StateCmpstnModule;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseClsfDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.intrfc.model.cmpstn.AtchFileCmpstn;
import io.nicheblog.dreamdiary.global.intrfc.model.cmpstn.AtchFileCmpstnModule;
import io.nicheblog.dreamdiary.global.validator.state.UpdateState;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * SectnDto
 * <pre>
 *  단락 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class SectnDto
        extends BaseClsfDto
        implements Identifiable<Integer>, AtchFileCmpstnModule, CommentCmpstnModule, TagCmpstnModule, StateCmpstnModule {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final String CONTENT_TYPE = ContentType.SECTN.key;
    /** 필수(Override): 글분류 코드 */
    @Builder.Default
    private static final String CTGR_CL_CD = CONTENT_TYPE + "_CTGR_CD";

    /** 컨텐츠 타입 */
    @Builder.Default
    private String contentType = CONTENT_TYPE;

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

    /** 원글 번호 */
    @Positive
    private Integer refPostNo;

    /** 원글 컨텐츠 타입 */
    @Size(max = 50)
    private String refContentType;

    /** 만료 여부 (Y/N) */
    @Builder.Default
    @Pattern(regexp = "^[YN]$", groups = UpdateState.class)
    private String deprcYn = "N";

    /* ----- */

    @Override
    public Integer getKey() {
        return this.postNo;
    }

    /** 위임 :: 첨부파일 모듈 */
    public AtchFileCmpstn file;
    /** 위임 :: 댓글 정보 모듈 */
    public CommentCmpstn comment;
    /** 위임 :: 태그 정보 모듈 */
    public TagCmpstn tag;
    /** 위임 :: 상태 관리 모듈 */
    public StateCmpstn state;
}
