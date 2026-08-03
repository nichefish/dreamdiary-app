package io.nicheblog.dreamdiary.feature.journal.thread.model;

import io.nicheblog.dreamdiary.feature.attachable.prefix.model.PrefixDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * JournalThreadPeriodSummaryDto
 * <pre>
 *  월간·주간 저널 화면에 노출할 기간별 스레드 요약 DTO.
 *  entryCount는 스레드 전체 소속 수가 아니라 요청 기간 안의 소속 엔트리 수다.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class JournalThreadPeriodSummaryDto {

    /** 스레드 ID */
    private Integer threadId;

    /** 스레드 제목 */
    private String title;

    /** 스레드에 선택된 말머리. 선택하지 않았으면 null. */
    private PrefixDto prefix;

    /** 조회 기간 안의 소속 엔트리 수 */
    private long entryCount;

    /** 조회 기간 안에서 스레드가 처음 등장한 엔트리 일자 */
    private LocalDate firstEntryDate;
}
