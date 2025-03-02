package io.nicheblog.dreamdiary.domain.jrnl.dream.model;

import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.extension.clsf.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.extension.clsf.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.BasePostDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.NotNull;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * JrnlDreamDto
 * <pre>
 *  저널 꿈 Dto.
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
public class JrnlDreamDto
        extends BasePostDto
        implements Identifiable<Integer>, CommentCmpstnModule, TagCmpstnModule, Comparable<JrnlDreamDto> {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.JRNL_DREAM;
    /** 필수(Override): 글분류 코드 */
    @Builder.Default
    private static final String CTGR_CL_CD = CONTENT_TYPE.name() + "_CTGR_CD";

    /** 컨텐츠 타입 */
    @Builder.Default
    private String contentType = CONTENT_TYPE.key;

    /* ----- */

    /** 저널 일자 번호 */
    private Integer jrnlDayNo;
    /** 저널 기준일자 */
    private String stdrdDt;
    /** 저널 일자 요일 */
    private String jrnlDtWeekDay;
    /** 저널 기준일자 */
    private Integer yy;
    /** 저널 기준일자 */
    private Integer mnth;
    /** 순번 */
    private Integer idx;

    /** 악몽 여부 (Y/N) */
    @Builder.Default
    @Size(min = 1, max = 1)
    @Pattern(regexp = "^[YN]$")
    private String nhtmrYn = "N";

    /** 입면환각 여부 (Y/N) */
    @Builder.Default
    @Size(min = 1, max = 1)
    @Pattern(regexp = "^[YN]$")
    private String hallucYn = "N";

    /** 타인 꿈 여부 (Y/N) */
    @Builder.Default
    @Size(min = 1, max = 1)
    @Pattern(regexp = "^[YN]$")
    private String elseDreamYn = "N";

    /** 꿈꾼이(타인) 이름 */
    private String elseDreamerNm;

    /* ----- */

    /**
     * 날짜 오름차순 정렬
     *
     * @param other - 비교할 객체
     * @return 양수: 현재 객체가 더 큼, 음수: 현재 객체가 더 작음, 0: 두 객체가 같음
     */
    @SneakyThrows
    @Override
    public int compareTo(final @NotNull JrnlDreamDto other) {
        Date thisDate = DateUtils.asDate(this.getStdrdDt());
        if (thisDate == null) return -1;

        Date otherDate = DateUtils.asDate(other.getStdrdDt());
        return thisDate.compareTo(otherDate);
    }

    @Override
    public Integer getKey() {
        return this.postNo;
    }

    /** 위임 :: 댓글 정보 모듈 */
    public CommentCmpstn comment;
    /** 위임 :: 태그 정보 모듈 */
    public TagCmpstn tag;
}
