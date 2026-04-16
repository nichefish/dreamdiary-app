package io.nicheblog.dreamdiary.feature.journal.sbjct.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.param.BaseAttachableSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * JournalSbjctSearchParam
 * <pre>
 *  저널 주제 목록 검색 파라미터.
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
public class JournalSbjctSearchParam
        extends BaseAttachableSearchParam {
    //
}
