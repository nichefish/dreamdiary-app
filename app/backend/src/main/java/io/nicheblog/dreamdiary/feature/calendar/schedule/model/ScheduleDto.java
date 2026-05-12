package io.nicheblog.dreamdiary.feature.calendar.schedule.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.BaseAttachableDto;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.validator.state.UpdateState;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ScheduleDto
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
public class ScheduleDto
        extends BaseAttachableDto
        implements Identifiable<Integer>, CommentCmpstnModule, TagCmpstnModule {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.SCHEDULE;
    /** 필수(Override): 글분류 코드 */
    @Builder.Default
    private static final String CTGR_CL_CD = CONTENT_TYPE.name() + "_CD";

    /** 컨텐츠 타입 */
    @Builder.Default
    private String contentType = CONTENT_TYPE.key;

       /** 제목 */
    private String title;

    /** 내용 */
    private String content;

    /** 마크다운 처리된 내용 */
    private String markdownContent;

    /** 글분류 코드 */
    @Size(max = 50)
    private String ctgrClCd;

    /** 글 분류 코드 */
    @Size(max = 50)
    private String categoryCode;

    /** 글 분류 표시명 */
    @Size(max = 50)
    private String categoryName;

    /** 글분류 존재 여부 */
    @Builder.Default
    private Boolean hasCtgrNm = false;

    /* ----- */

    /** 일정 코드 */
    private String scheduleCd;
    /** 일정 분류 코드 이름 */
    private String scheduleNm;

    /** 시작일 */
    private String bgnDt;
    /** 종료일 */
    private String endDt;

    /** 개인일정 여부 (Y/N) */
    @Builder.Default
    @Pattern(regexp = "^[YN]$", groups = UpdateState.class)
    private String privateYn = "N";

    /** 참석자 리스트  */
    private List<SchedulePrtcpntDto> prtcpntList;
    /** 참석자 목록 문자열 */
    private String prtcpntListStr;

    /* ----- */

    /**
     * Getter :: 개인일정 여부 (Y/N)
     */
    public Boolean getIsPrvt() {
        return "Y".equals(this.privateYn);
    }

    /**
     * Getter :: username이 빈 객체를 제외한 참가자 목록을 반환한다.
     *
     * @return {@link List} -- 참가자 목록
     */
    public List<SchedulePrtcpntDto> getPrtcpntList() {
        if (CollectionUtils.isEmpty(this.prtcpntList)) return this.prtcpntList;
        return this.prtcpntList.stream()
                .filter(dto -> StringUtils.isNotEmpty(dto.getUsername()))
                .collect(Collectors.toList());
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
}

