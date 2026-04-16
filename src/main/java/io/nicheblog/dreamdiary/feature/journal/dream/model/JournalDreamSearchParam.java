package io.nicheblog.dreamdiary.feature.journal.dream.model;

import io.nicheblog.dreamdiary.feature.attachable.state.StateCd;
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
 * JournalDreamSearchParam
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
public class JournalDreamSearchParam
        extends BaseSearchParam {

    /** 연도 */
    private Integer yy;
    /** 월 */
    private Integer mnth;
    /** 주 시작일 */
    private String weekStartDt;

    /** 저널 일자 번호 */
    private Integer journalDayId;

    /** 콘텐츠 타입 */
    private String contentType;

    /** 꿈 검색 키워드 */
    private List<String> searchKeywords;

    /** 태그 ID */
    private Integer tagId;
    private List<Integer> tagIds;

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
        final boolean hasTagIds = CollectionUtils.isNotEmpty(tagIds) && tagIds.stream().anyMatch(Objects::nonNull);
        final boolean hasDate = yy != null || mnth != null || weekStartDt != null || journalDayId != null;
        final boolean hasTag = tagId != null;
        final boolean hasState = CollectionUtils.isNotEmpty(states) && states.stream().anyMatch(StringUtils::isNotEmpty);

        return !(hasKeyword || hasTagIds || hasDate || hasTag || hasState);
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
     * summary 목록 캐시 key suffix
     */
    public String toSummaryCacheKey() {
        final int keyYy = (yy != null) ? yy : 9999;
        return keyYy + "_" + CmmUtils.normalizeStringList(states);
    }
}

