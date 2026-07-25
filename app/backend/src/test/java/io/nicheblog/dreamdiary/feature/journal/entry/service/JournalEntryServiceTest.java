package io.nicheblog.dreamdiary.feature.journal.entry.service;

import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.jpa.JournalChapterRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * JournalEntryService 정렬 헬퍼 계약 검증.
 * <p>
 * sortByChapterAndEntryOrder 가 같은 일자 안에서 챕터 sortOrder → 원본 엔트리 sortOrder 순으로
 * 정렬해, 챕터별로 1부터 매겨지는 엔트리 sortOrder가 챕터를 가로질러 몰리지 않게 하는지 고정한다.
 */
@ExtendWith(MockitoExtension.class)
class JournalEntryServiceTest {

    @Mock
    private JournalChapterRepository journalChapterRepository;

    @InjectMocks
    private JournalEntryService service;

    /** 같은 일자에 두 챕터가 있으면 챕터 순서로 그룹핑한 뒤 각 챕터 안에서 엔트리 순서를 따른다. */
    @Test
    void sortByChapterAndEntryOrderGroupsByChapterThenEntryOrder() {
        when(journalChapterRepository.findAllById(any())).thenReturn(List.of(
                chapter(100, 1),
                chapter(200, 2)
        ));
        final List<JournalEntryDto> entries = new ArrayList<>(List.of(
                entry(1, "2026-07-14", 200, 1),
                entry(2, "2026-07-14", 100, 1),
                entry(3, "2026-07-14", 100, 2),
                entry(4, "2026-07-14", 200, 2)
        ));

        service.sortByChapterAndEntryOrder(entries);

        // 챕터 100(#1,#2) 뒤에 챕터 200(#1,#2): id [2, 3, 1, 4]
        assertEquals(List.of(2, 3, 1, 4), entries.stream().map(JournalEntryDto::getId).toList());
    }

    /** 일자가 다르면 일자 오름차순이 챕터·엔트리 순서보다 우선한다. */
    @Test
    void sortByChapterAndEntryOrderOrdersByDateFirst() {
        when(journalChapterRepository.findAllById(any())).thenReturn(List.of(
                chapter(100, 1)
        ));
        final List<JournalEntryDto> entries = new ArrayList<>(List.of(
                entry(1, "2026-07-15", 100, 1),
                entry(2, "2026-07-14", 100, 2)
        ));

        service.sortByChapterAndEntryOrder(entries);

        assertEquals(List.of(2, 1), entries.stream().map(JournalEntryDto::getId).toList());
    }

    private JournalChapterEntity chapter(final int id, final int sortOrder) {
        return JournalChapterEntity.builder()
                .id(id)
                .sortOrder(sortOrder)
                .build();
    }

    private JournalEntryDto entry(final int id, final String stdrdDt, final int chapterId, final int sortOrder) {
        return JournalEntryDto.builder()
                .id(id)
                .stdrdDt(stdrdDt)
                .journalChapterId(chapterId)
                .sortOrder(sortOrder)
                .build();
    }
}
