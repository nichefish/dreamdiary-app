package io.nicheblog.dreamdiary.feature.journal.entry.model;

import lombok.*;

import java.util.List;

/**
 * 저널 엔트리 일괄 태그 응답 DTO.
 * <p>
 * 실제로 변경된 (엔트리, 태그) 연결 쌍과 집계를 담는다. 일회성 Undo(B2)는
 * {@code changedPairs} 만 역연산 대상으로 삼는다.
 *
 * @author nichefish
 */
@Getter
@Builder
public class JournalEntryBulkTagResDto {

    /** 수행한 작업 종류(ADD/REMOVE). */
    private String operation;

    /** 요청한 엔트리 수(중복 제거 후). */
    private int requestedEntryCount;

    /** 요청한 태그 수(중복 제거 후). */
    private int requestedTagCount;

    /** 실제로 변경(추가·제거)된 연결 수. */
    private int changedLinkCount;

    /** 이미 원하는 상태라 변경하지 않은 연결 수. */
    private int unchangedLinkCount;

    /** 하나 이상의 연결이 실제로 바뀐 엔트리 수. */
    private int affectedEntryCount;

    /** 실제로 변경된 (엔트리, 태그) 연결 쌍 목록. */
    private List<ChangedPair> changedPairs;

    /** 변경된 연결 한 쌍. Undo 요청 payload 로도 재사용하므로 역직렬화 가능하게 둔다. */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangedPair {
        private Integer entryId;
        private Integer tagId;
    }
}