package io.nicheblog.dreamdiary.feature.jrnl.dream.model;

import io.nicheblog.dreamdiary.feature.clsf.state.StateCd;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JrnlDreamSearchParam
 * <pre>
 *  꿈 목록 검색 파라미터.
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
public class JrnlDreamSearchParam
        extends BaseSearchParam {

    /** 연도 */
    private Integer yy;
    /** 월 */
    private Integer mnth;
    /** 주 시작일 */
    private String weekStartDt;

    /** 저널 일자 번호 */
    private Integer jrnlDayNo;

    /** 콘텐츠 타입 */
    private String contentType;

    /** 꿈 검색 키워드 */
    private List<String> searchKeywords;

    /** 태그 번호 */
    private Integer tagNo;
    private List<Integer> tagNos;

    /** 정렬 */
    @Builder.Default
    private String sort = "DESC";

    /** 상태(중요, 참조..) */
    private List<String> states;

    /**
     * 파라미터 유효성 체크
     * @return 인자 존재 여부
     */
    public boolean isEmpty() {
        final boolean hasKeyword = searchKeywords != null && searchKeywords.stream().anyMatch(k -> k != null && !k.trim().isEmpty());
        final boolean hasTagNos = CollectionUtils.isNotEmpty(tagNos) && tagNos.stream().anyMatch(Objects::nonNull);
        final boolean hasDate = yy != null || mnth != null || weekStartDt != null || jrnlDayNo != null;
        final boolean hasTag = tagNo != null;
        final boolean hasState = CollectionUtils.isNotEmpty(states) && states.stream().anyMatch(StringUtils::isNotEmpty);

        return !(hasKeyword || hasTagNos || hasDate || hasTag || hasState);
    }

    /**
     * 상태 파라미터 세팅
     */
    public void resolveStates(final Boolean showImprtc, final Boolean showRefrnc) {
        final List<String> states = new ArrayList<>(2);

        if (showImprtc) states.add(StateCd.IMPRTC.key);
        if (showRefrnc) states.add(StateCd.REFRNC.key);

        this.states = states;
    }

    /**
     * 목록 캐시 key suffix
     */
    public String toListCacheKey() {
        final int keyYy = (yy != null) ? yy : 9999;
        final int keyMnth = (mnth != null) ? mnth : 99;

        return keyYy + "_" + keyMnth + "_"
                + CmmUtils.sanitize(contentType) + "_"
                + CmmUtils.sanitize(sort) + "_"
                + CmmUtils.sanitize(weekStartDt) + "_"
                + CmmUtils.nullSafeInt(jrnlDayNo) + "_"
                + CmmUtils.normalizeStringList(searchKeywords) + "_"
                + CmmUtils.nullSafeInt(tagNo) + "_"
                + CmmUtils.normalizeIntegerList(tagNos) + "_"
                + CmmUtils.normalizeStringList(states) + "_"
                + CmmUtils.sanitize(searchType) + "_"
                + CmmUtils.sanitize(searchKeyword) + "_"
                + CmmUtils.sanitize(searchStartDt) + "_"
                + CmmUtils.sanitize(searchEndDt);
    }

    /**
     * summary 목록 캐시 key suffix
     */
    public String toSummaryCacheKey() {
        final int keyYy = (yy != null) ? yy : 9999;
        return keyYy + "_" + CmmUtils.normalizeStringList(states);
    }
}
