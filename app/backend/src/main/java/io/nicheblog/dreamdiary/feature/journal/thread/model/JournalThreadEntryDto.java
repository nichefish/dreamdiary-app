package io.nicheblog.dreamdiary.feature.journal.thread.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * JournalThreadEntryDto
 * <pre>
 *  저널 스레드-엔트리 소속 DTO.
 *
 *  스레드 상세에서는 소속 엔트리를 보여주고, 엔트리 쪽에서는 소속 스레드를 보여주기 때문에
 *  양쪽 표시에 필요한 최소 정보(제목·일자·콘텐츠 유형)를 함께 담는다.
 *  본문은 담지 않는다 — 목록 표시가 목적이고, 본문이 필요하면 각 상세 API 를 쓴다.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class JournalThreadEntryDto {

    /** 소속 ID */
    private Integer id;

    /** 소속 스레드 ID */
    private Integer threadId;

    /** 소속 엔트리 ID */
    private Integer entryId;

    /** 스레드 내 표시 순서. NULL 이면 엔트리 일자순으로 정렬한다. */
    private Integer sortOrder;

    /** 스레드 제목 (엔트리 쪽에서 소속 스레드를 표시할 때 사용) */
    private String threadTitle;

    /**
     * 엔트리 콘텐츠 유형: JOURNAL_DIARY / JOURNAL_DREAM / JOURNAL_NOTE
     * <p>
     * 엔트리 제목·본문은 담지 않는다. 소속 조회의 목적은 "무엇이 묶여 있는가"이고,
     * 표시에 필요한 엔트리 상세는 기존 엔트리 조회 API 로 가져간다.
     */
    private String entryContentType;
}
