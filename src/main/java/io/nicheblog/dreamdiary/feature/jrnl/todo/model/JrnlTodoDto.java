package io.nicheblog.dreamdiary.feature.jrnl.todo.model;

import io.nicheblog.dreamdiary.feature.clsf.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.feature.clsf.shared.model.BaseClsfDto;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.infrastructure.Constant;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

/**
 * JrnlTodoDto
 * <pre>
 *  저널 할일 Dto.
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
public class JrnlTodoDto
        extends BaseClsfDto
        implements Identifiable<Integer>, CommentCmpstnModule, TagCmpstnModule {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.JRNL_DIARY;
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

    /** 년도 */
    private Integer yy;
    /** 월 */
    private Integer mnth;
    /** 순번 */
    private Integer idx;

    /* ----- */

    @Override
    public Integer getKey() {
        return this.postNo;
    }

    /** 위임 :: 댓글 정보 모듈 */
    public CommentCmpstn comment;
    /** 위임 :: 태그 정보 모듈 */
    public TagCmpstn tag;
}
