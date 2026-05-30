package io.nicheblog.dreamdiary.feature.journal.thread.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.param.BaseAttachableSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Size;

/**
 * JournalThreadSearchParam
 * <pre>
 *  저널 스레드 목록 검색 파라미터.
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
public class JournalThreadSearchParam
        extends BaseAttachableSearchParam {

    /** 글 분류 코드 (목록 필터) */
    @Size(max = 50)
    private String categoryCode;
}
