package io.nicheblog.dreamdiary.feature.journal.entry.model;

import lombok.*;

import java.util.List;

/**
 * 저널 엔트리 일괄 태그 Undo 요청 DTO.
 * <p>
 * 직전 일괄 작업의 원 작업 종류(operation)와 서버가 응답한 실제 변경 연결 쌍(pairs)을
 * 그대로 전달한다. 서버는 원 ADD 면 pairs 를 제거, 원 REMOVE 면 pairs 를 복원해 역연산한다.
 *
 * @author nichefish
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntryBulkTagUndoReqDto {

    /** 되돌릴 원 작업 종류. "ADD" 또는 "REMOVE". */
    private String operation;

    /** 대상 콘텐츠 타입. JOURNAL_DIARY 또는 JOURNAL_DREAM. */
    private String contentType;

    /** 직전 작업에서 실제로 변경된 (엔트리, 태그) 연결 쌍. */
    private List<JournalEntryBulkTagResDto.ChangedPair> pairs;
}