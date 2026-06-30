package io.nicheblog.dreamdiary.feature.journal.day.model;

import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 일자 화면용 꿈 가상 섹션 (Phase 1: DB 챕터 분리 없이 UI·API 표현만).
 * <p>내 꿈({@code sectionKey=own})과 꿈꾼 이름별 블록을 담는다. 동일 철자(트림 후)는 한 섹션.</p>
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class JournalDreamSectionDto {

    /** 접힘·등록 초기값 키 ({@code own} 또는 {@code dreamer:{name}}) */
    private String sectionKey;

    /**
     * 헤더 라벨 (예: 꿈, 형 꿈).
     * <p>요청 locale에 따라 서버 메시지 카탈로그에서 조립한다.</p>
     */
    private String title;

    /** 꿈꾼 이름 — 내 꿈 섹션은 null */
    private String dreamerName;

    /** 섹션 소속 꿈 엔트리 */
    private List<JournalEntryDto> entries;
}
