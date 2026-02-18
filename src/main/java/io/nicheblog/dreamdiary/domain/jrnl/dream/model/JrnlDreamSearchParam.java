package io.nicheblog.dreamdiary.domain.jrnl.dream.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Objects;

/**
 * JrnlDreamSearchParam
 * <pre>
 *  저널 꿈 목록 검색 파라미터.
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

    /** 년도 */
    private Integer yy;

    /** 월 */
    private Integer mnth;

    /** 저널 일자 번호 */
    private Integer jrnlDayNo;

    /** 컨텐츠 타입 */
    private String contentType;

    /** 꿈 검색 키워드 */
    private List<String> searchKeywords;

    /** 태그 번호 */
    private Integer tagNo;
    private List<Integer> tagNos;

    /** 중요 여부 **/
    private String state;

    /**
     * 파라미터 부재 판별
     * @return 인자 존재 여부
     */
    public boolean isEmpty() {
        boolean hasKeyword = searchKeywords != null && searchKeywords.stream().anyMatch(k -> k != null && !k.trim().isEmpty());
        boolean hasTagNos = tagNos != null && tagNos.stream().anyMatch(Objects::nonNull);
        boolean hasDate = yy != null || mnth != null || jrnlDayNo != null;
        boolean hasTag = tagNo != null;
        boolean hasState = state != null && !state.isBlank();

        return !(hasKeyword || hasTagNos || hasDate || hasTag || hasState);
    }
}
