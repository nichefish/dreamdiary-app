package io.nicheblog.dreamdiary.feature.journal.entry.model;

import io.nicheblog.dreamdiary.feature.attachable.state.StateKey;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class JournalEntrySearchParam extends BaseSearchParam {

    private Integer yy;
    private Integer mnth;
    private String weekStartDt;
    private Integer journalDayId;
    private String contentType;
    /** Reflection 한정 서브 facet: target 유형(JOURNAL_DIARY/JOURNAL_DREAM) 또는 독립(INDEPENDENT). REFLECTION 검색에서만 적용. */
    private String refContentType;
    private List<String> searchKeywords;
    private Integer tagId;
    private List<Integer> tagIds;

    @Builder.Default
    private String sort = "DESC";

    /** 정렬 기준 축. DATE(기본)=일자, TITLE=제목. sort(방향)와 조합한다. */
    private String sortField;

    /** 제목 전용 검색어(제목 LIKE). 키워드(제목+본문)와 구분되는 제목만 매칭 필터. */
    private String title;

    private List<String> states;

    /**
     * 검색 조건이 실질적으로 비어있는지 판별한다.
     *
     * @return 비어있으면 true
     */
    public boolean isEmpty() {
        final boolean hasKeyword = searchKeywords != null && searchKeywords.stream().anyMatch(k -> k != null && !k.trim().isEmpty());
        final boolean hasTagIds = CollectionUtils.isNotEmpty(tagIds) && tagIds.stream().anyMatch(Objects::nonNull);
        final boolean hasDate = yy != null || mnth != null || weekStartDt != null || journalDayId != null;
        final boolean hasTag = tagId != null;
        final boolean hasState = CollectionUtils.isNotEmpty(states) && states.stream().anyMatch(StringUtils::isNotEmpty);
        final boolean hasTitle = StringUtils.isNotBlank(title);

        return !(hasKeyword || hasTagIds || hasDate || hasTag || hasState || hasTitle);
    }

    /**
     * 상태 토글 입력을 states 목록으로 정규화한다.
     *
     * @param showImprtc 중요 표시 포함 여부
     * @param showRefrnc 참고 표시 포함 여부
     */
    public void resolveStates(final Boolean showImprtc, final Boolean showRefrnc) {
        final List<String> states = new ArrayList<>(2);

        if (showImprtc) states.add(StateKey.IMPRTC.key);
        if (showRefrnc) states.add(StateKey.REFRNC.key);

        this.states = states;
    }

    /**
     * 요약 캐시 키 문자열을 생성한다.
     *
     * @return 요약 캐시 키
     */
    public String toSummaryCacheKey() {
        final int keyYy = (yy != null) ? yy : 9999;
        final String stateKey = CmmUtils.normalizeStringList(states);
        final String keywordKey = CmmUtils.normalizeStringList(searchKeywords);
        final String tagIdKey = tagId != null ? String.valueOf(tagId) : "";
        final String tagIdsKey = CollectionUtils.isNotEmpty(tagIds)
                ? tagIds.stream()
                        .filter(Objects::nonNull)
                        .map(String::valueOf)
                        .sorted()
                        .collect(Collectors.joining(","))
                : "";
        final String titleKey = StringUtils.isNotBlank(title) ? title.trim() : "";
        return keyYy + "_" + stateKey + "_" + keywordKey + "_" + tagIdKey + "_" + tagIdsKey + "_" + titleKey;
    }
}
