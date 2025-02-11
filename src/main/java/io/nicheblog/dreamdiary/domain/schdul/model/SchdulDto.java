package io.nicheblog.dreamdiary.domain.schdul.model;

import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.extension.clsf.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.extension.clsf.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.BasePostDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

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
        extends BasePostDto
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
    @Size(min = 1, max = 1)
    @Pattern(regexp = "^[YN]$")
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