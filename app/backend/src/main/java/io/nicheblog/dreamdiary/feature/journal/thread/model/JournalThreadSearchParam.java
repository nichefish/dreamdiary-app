package io.nicheblog.dreamdiary.feature.journal.thread.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.param.BaseAttachableSearchParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Size;

import java.util.List;

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

    /** 단일 말머리 필터. */
    private Integer prefixId;

    /**
     * 소속 엔트리 태그 필터 (멀티, AND).
     * <p>
     * 스레드는 자체 태그를 소유하지 않으므로, 소속 엔트리 태그 합집합이
     * 선택한 tagId 를 모두 포함하는 스레드만 남긴다.
     * </p>
     */
    private List<Integer> tagIds;

    /**
     * 스레드 라이프사이클 필터 ({@code OPEN}/{@code PENDING}/{@code RESOLVED}).
     * <p>
     * 비우면 전체. {@code OPEN}은 lifecycle 행이 없는 스레드를 뜻한다.
     * </p>
     */
    @Size(max = 50)
    private String lifecycleKey;
}
