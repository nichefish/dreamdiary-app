package io.nicheblog.dreamdiary.feature.jrnl.diary.model;

import io.nicheblog.dreamdiary.feature.clsf.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.feature.clsf.shared.model.BaseClsfDto;
import io.nicheblog.dreamdiary.feature.clsf.state.model.cmpstn.StateCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.state.model.cmpstn.StateCmpstnModule;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.intrfc.model.cmpstn.AtchFileCmpstn;
import io.nicheblog.dreamdiary.global.intrfc.model.cmpstn.AtchFileCmpstnModule;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * JrnlDiaryDto
 * <pre>
 *  저널 일기 조회용 Dto.
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
public class JrnlDiaryDto
        extends BaseClsfDto
        implements Identifiable<Integer>, AtchFileCmpstnModule, CommentCmpstnModule, TagCmpstnModule, StateCmpstnModule, Comparable<JrnlDiaryDto>  {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private String contentType = ContentType.JRNL_DIARY.key;

   /** 제목 */
    private String title;
    /** 내용 */
    private String cn;
    /** 마크다운 처리된 내용 */
    private String markdownCn;

    /* ----- */

    /** 저널 일자 번호 */
    private Integer jrnlDayNo;
    /** 저널 항목 번호 */
    private Integer jrnlEntryNo;
    /** 저널 기준일자 */
    private String stdrdDt;
    /** 저널 기준일자 */
    private String dtUnknownYn;
    /** 저널 일자 요일 */
    private String jrnlDtWeekDay;

    /** 저널 기준일자 */
    private Integer yy;
    /** 저널 기준일자 */
    private Integer mnth;

    /** 공휴일 여부 */
    private Boolean isHldy;
    /** 공휴일 이름 */
    private String hldyNm;

    /** 순번 */
    private Integer idx;

    /* ----- */

    /** 인덱스 변경 여부 */
    @Builder.Default
    private Boolean isIdxChanged = false;
    /** 저널 항목 변경 여부 */
    @Builder.Default
    private Boolean isEntryChanged = false;
    /** 이전 저널 항목 번호 */
    private Integer prevJrnlEntryNo;

    /* ----- */

    /**
     * 날짜 오름차순 정렬
     *
     * @param other - 비교할 객체
     * @return 양수: 현재 객체가 더 큼, 음수: 현재 객체가 더 작음, 0: 두 객체가 같음
     */
    @SneakyThrows
    @Override
    public int compareTo(final @NotNull JrnlDiaryDto other) {
        Date thisDate = DateUtils.asDate(this.getStdrdDt());
        if (thisDate == null) return -1;

        Date otherDate = DateUtils.asDate(other.getStdrdDt());
        return thisDate.compareTo(otherDate);
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
    /** 위임 :: 태그 정보 모듈 */
    public TagCmpstn tag;
    /** 위임 :: 상태 정보 모듈 */
    public StateCmpstn state;
}
