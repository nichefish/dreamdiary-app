package io.nicheblog.dreamdiary.feature.schdul.model;

import io.nicheblog.dreamdiary.feature.clsf.ContentType;
import io.nicheblog.dreamdiary.feature.clsf._shared.model.BaseClsfDto;
import io.nicheblog.dreamdiary.feature.clsf.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.validator.state.UpdateState;
import io.nicheblog.dreamdiary.infrastructure.cd.Code;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SchdulDto
 * <pre>
 *  일정 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SchdulDto
        extends BaseClsfDto
        implements Identifiable<Integer>, CommentCmpstnModule, TagCmpstnModule {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.SCHDUL;
    /** 필수(Override): 글분류 코드 */
    @Builder.Default
    private static final String CTGR_CL_CD = CONTENT_TYPE.name() + "_CD";

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
    protected String mdfable = Code.MDFABLE_REGSTR;

    /** 수정 가능 여부 */
    @Builder.Default
    protected Boolean isMdfable = false;

    /* ----- */

    /** 일정 코드 */
    private String schdulCd;
    /** 일정 분류 코드 이름 */
    private String schdulNm;

    /** 시작일 */
    private String bgnDt;
    /** 종료일 */
    private String endDt;

    /** 개인일정 여부 (Y/N) */
    @Builder.Default
    @Pattern(regexp = "^[YN]$", groups = UpdateState.class)
    private String prvtYn = "N";

    /** 참석자 리스트  */
    private List<SchdulPrtcpntDto> prtcpntList;
    /** 참석자 목록 문자열 */
    private String prtcpntListStr;

    /* ----- */

    /**
     * Getter :: 개인일정 여부 (Y/N)
     */
    public Boolean getIsPrvt() {
        return "Y".equals(this.prvtYn);
    }

    /**
     * Getter :: userId가 빈 객체를 제외한 참가자 목록을 반환한다.
     *
     * @return {@link List} -- 참가자 목록
     */
    public List<SchdulPrtcpntDto> getPrtcpntList() {
        if (CollectionUtils.isEmpty(this.prtcpntList)) return this.prtcpntList;
        return this.prtcpntList.stream()
                .filter(dto -> StringUtils.isNotEmpty(dto.getUserId()))
                .collect(Collectors.toList());
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
}
