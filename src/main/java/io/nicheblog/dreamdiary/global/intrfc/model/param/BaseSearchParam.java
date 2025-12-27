package io.nicheblog.dreamdiary.global.intrfc.model.param;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * BaseSearchParam
 * <pre>
 *  (공통/상속) 목록 검색 파라미터.
 *  "All classes in the hierarchy must be annotated with @SuperBuilder."
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BaseSearchParam
        extends BaseParam {

    public static final Integer DEFAULT_PAGE_NO = 1;
    public static final Integer DEFAULT_PAGE_SIZE = 10;

    /** 페이지 번호 (기본값 : 1) */
    @Builder.Default
    @Positive
    protected Integer pageNo = 1;

    /** 페이지 크기 (기본값 : 10) */
    @Builder.Default
    @Positive
    protected Integer pageSize = 10;

    /** 검색 유형 (검색 키워드와 한 세트) */
    protected String searchType;

    /** 검색 키워드 (검색 유형과 한 세트) */
    protected String searchKeyword;

    /** 검색 시작일자 */
    protected String searchStartDt;

    /** 검색 종료일자 */
    protected String searchEndDt;

    /** 등록자 ID */
    @Size(max = 20)
    protected String regstrId;

    /** 등록자 이름 */
    @Size(max = 50)
    protected String regstrNm;

    /** 조치자 ID */
    @Size(max = 20)
    protected String managtrId;

    /** 조치자 이름 */
    @Size(max = 50)
    protected String managtrNm;

    /** 로그인 유저 ID */
    protected String lgnUserId;

    /* ----- */

    /**
     * Getter :: 페이지 번호.
     *
     * @return {@link Integer} -- 페이지 번호 (기본값: 1)
     */
    public Integer getPageNo() {
        if (this.pageNo == null) this.pageNo = DEFAULT_PAGE_NO;
        return this.pageNo;
    }
    /**
     * Getter :: 페이지 크기.
     *
     * @return {@link Integer} -- 페이지 크기 (기본값: 10)
     */
    public Integer getPageSize() {
        if (this.pageSize == null) this.pageSize = DEFAULT_PAGE_SIZE;
        return this.pageSize;
    }

}
