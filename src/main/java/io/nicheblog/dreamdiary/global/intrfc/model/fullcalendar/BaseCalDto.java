package io.nicheblog.dreamdiary.global.intrfc.model.fullcalendar;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * BaseCalDto
 * <pre>
 *  (공통/상속) 전체일정 Full-Calendar Dto.
 * </pre>
 *
 * @author nichefish
 * @see "https://fullcalendar.io/docs/event-object"
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Log4j2
public class BaseCalDto
        extends BaseCrudDto
        implements Comparable<BaseCalDto> {

    // overlap: false,
    // editable:
    // url
    // backgroundColor
    // borderColor
    // textColor

    /** 일정 고유 번호 (PK) */
    protected String id;

    /** 일정 분류 코드 이름 */
    protected String title;

    /** 일정 그룹 ID */
    protected String groupId;

    /** 칸 전체 표시 여부 */
    protected String display;

    /** 칸 전체 표시시 색깔 여부 */
    protected String color;

    /** 일정 시작일 */
    protected String start;

    /** 일정 종료일 */
    protected String end;

    /** 하루종일 여부 */
    protected Boolean allDay;

    /** CSS 클래스 지정 */
    protected String className;

    /** 일정 설명 (사유) */
    protected String description;

    /* ----- */

    /**
     * 일정일자 기준 정렬 (오름차순. 일자가 같으면? => 제목순 정렬)
     *
     * @param other 비교할 객체
     * @return 양수: 현재 객체가 더 큼, 음수: 현재 객체가 더 작음, 0: 두 객체가 같음
     */
    @SneakyThrows
    @Override
    public int compareTo(final @NotNull BaseCalDto other) {
        // 1. 날짜로 우선 비교
        Date thisDate = DateUtils.asDate(this.start);
        if (thisDate == null) return -1;

        Date compareDt = DateUtils.asDate(other.getStart());
        return thisDate.compareTo(compareDt);
    }

    /**
     * 날짜 지났는지 여부 체크
     *
     * @return {@link Boolean} -- 날짜가 지났다면 true, 그렇지 않다면 false를 반환합니다.
     * @throws Exception 날짜 비교 중 발생할 수 있는 예외를 던질 수 있습니다.
     */
    @JsonIgnore
    public Boolean hasPassed() throws Exception {
        return DateUtils.getCurrDate()
                        .compareTo(DateUtils.asDate(this.end)) > 0;
    }
}
