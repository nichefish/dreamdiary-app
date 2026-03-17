package io.nicheblog.dreamdiary.domain.jrnl.diary.model;

import io.nicheblog.dreamdiary.domain.clsf.state.StateCd;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JrnlDiarySearchParam
 * <pre>
 *  저널 일기 목록 검색 파라미터.
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
public class JrnlDiarySearchParam
        extends BaseSearchParam {

    /** 년도 */
    private Integer yy;
    /** 월 */
    private Integer mnth;

    /** 저널 일자 번호 */
    private Integer jrnlDayNo;

    /** 컨텐츠 타입 */
    private String contentType;

    /** 일기 검색 키워드 */
    private List<String> searchKeywords;

    /** 태그 번호 */
    private Integer tagNo;
    private List<Integer> tagNos;

    /** 정렬 */
    @Builder.Default
    private String sort = "DESC";

    /** 상태(중요, 참조..) **/
    private List<String> states;

    /**
     * 파라미터 부재 판별
     * @return 인자 존재 여부
     */
    public boolean isEmpty() {
        final boolean hasKeyword = searchKeywords != null && searchKeywords.stream().anyMatch(k -> k != null && !k.trim().isEmpty());
        final boolean hasTagNos = CollectionUtils.isNotEmpty(tagNos) && tagNos.stream().anyMatch(Objects::nonNull);
        final boolean hasDate = yy != null || mnth != null || jrnlDayNo != null;
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
}
