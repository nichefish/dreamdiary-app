package io.nicheblog.dreamdiary.feature.journal.entry.model;

import lombok.*;

import java.util.List;

/**
 * 저널 엔트리 일괄 태그 요청 DTO.
 * <p>
 * 검색 결과에서 선택한 엔트리들에 기존 태그를 일괄 추가(ADD)하거나 제거(REMOVE)한다.
 * 태그 집합 전체 교체·신규 태그 생성·태그 마스터 삭제는 범위 밖이다.
 *
 * @author nichefish
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntryBulkTagReqDto {

    /** 작업 종류. "ADD"(연결 추가) 또는 "REMOVE"(연결 제거). */
    private String operation;

    /** 대상 콘텐츠 타입. JOURNAL_DIARY 또는 JOURNAL_DREAM. */
    private String contentType;

    /** 변경 대상 엔트리 ID 목록(중복은 서버에서 제거). */
    private List<Integer> entryIds;

    /** 추가·제거할 기존 태그 ID 목록(중복은 서버에서 제거). */
    private List<Integer> tagIds;
}