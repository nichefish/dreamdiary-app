package io.nicheblog.dreamdiary.feature.journal.day.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * JournalDaySearchParam
 * <pre>
 *  저널 일자 목록 검색 파라미터.
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
public class JournalDaySearchParam
        extends BaseSearchParam {

    /** 저널 일자 소유 사용자 영속 ID */
    private Integer ownerId;

    /** 년도 */
    private Integer yy;
    /** 월 */
    private Integer mnth;
    /** 기준일자 */
    private String stdrdDt;
    /** 주 시작일자 */
    private String weekStartDt;

    /** 컨텐츠 타입 */
    private String contentType;

    /** 정렬 (ASC/DESC) */
    @Builder.Default
    private String sort = "ASC";

    /** 태그 ID */
    private Integer tagId;
    /** 메타 ID */
    private Integer metaId;

    /** 일기 렌더링 여부 */
    private boolean showDiaries;
    /** 꿈 렌더링 여부 */
    private boolean showDreams;
    /** 태그클라우드 렌더링 여부 */
    private boolean showTagCloud;

    /** 일기 키워드 */
    private String diaryKeyword;
    /** 꿈 키워드 */
    private String dreamKeyword;
    /** 일기 라이프사이클 키 */
    private String diaryLifecycleKey;
    /** 꿈 라이프사이클 키 */
    private String dreamLifecycleKey;
    /** 챕터 Prefix ID */
    private List<Integer> chapterPrefixIds;

    /**
     * 저널 일자 목록 Cache key suffix
     */
    public String toYyMnthKey() {
        return yy + "_" + mnth;
    }

    /**
     * 기본 파라미터 객체 반환
     *
     * @param ownerId 소유 사용자 영속 ID
     * @param username 부착 데이터 조회용 사용자 계정명
     * @param yy 년도
     * @param mnth 월
     * @return {@link JournalDaySearchParam}
     */
    public static JournalDaySearchParam getBaseParam(final Integer ownerId, final String username, final Integer yy, final Integer mnth) {
        return JournalDaySearchParam.builder()
                .ownerId(ownerId)
                .loginUsername(username)
                .yy(yy)
                .mnth(mnth)
                .sort("ASC")
                .build();
    }

    /**
     * 기간 기준 기본 파라미터 객체 반환
     *
     * @param ownerId 소유 사용자 영속 ID
     * @param username 부착 데이터 조회용 사용자 계정명
     * @param weekStartDt 주 시작일
     * @return {@link JournalDaySearchParam}
     */
    public static JournalDaySearchParam getBaseParam(final Integer ownerId, final String username, final String weekStartDt) {
        return JournalDaySearchParam.builder()
                .ownerId(ownerId)
                .loginUsername(username)
                .weekStartDt(weekStartDt)
                .sort("ASC")
                .build();
    }
}
