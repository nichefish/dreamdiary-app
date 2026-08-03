package io.nicheblog.dreamdiary.feature.journal.day.service.helper;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.jpa.JournalChapterRepository;
import io.nicheblog.dreamdiary.feature.journal.day.repository.jpa.JournalDayRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa.JournalEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * JournalDayResolvedGuard 의 Reflection 완결축 제외 계약을 고정한다. (규칙 11)
 * <p>
 * Reflection 은 일자 완결축 밖이라 어느 축 잠금 검사에도 걸리지 않으며, 일자·챕터 조회조차 하지 않는다.
 *
 * @author nichefish
 */
@ExtendWith(MockitoExtension.class)
class JournalDayResolvedGuardTest {

    @Mock
    private JournalDayRepository journalDayRepository;
    @Mock
    private JournalChapterRepository journalChapterRepository;
    @Mock
    private JournalEntryRepository journalEntryRepository;
    @InjectMocks
    private JournalDayResolvedGuard guard;

    /** 엔트리 쓰기 검증: Reflection 은 예외 없이 통과하고 일자·챕터 조회를 하지 않는다. */
    @Test
    void assertWritableForEntryExemptsReflection() {
        assertDoesNotThrow(() -> guard.assertWritableForEntry(123, ContentType.JOURNAL_REFLECTION));
        verifyNoInteractions(journalDayRepository, journalChapterRepository);
    }

    /** ref 쓰기 검증: Reflection ref 는 예외 없이 통과하고 어떤 저장소도 건드리지 않는다. */
    @Test
    void assertWritableForRefExemptsReflection() {
        assertDoesNotThrow(() -> guard.assertWritableForRef(123, ContentType.JOURNAL_REFLECTION));
        verifyNoInteractions(journalDayRepository, journalChapterRepository, journalEntryRepository);
    }
}
