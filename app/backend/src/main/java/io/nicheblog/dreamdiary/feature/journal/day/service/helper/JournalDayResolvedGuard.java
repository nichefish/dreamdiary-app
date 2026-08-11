package io.nicheblog.dreamdiary.feature.journal.day.service.helper;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.jpa.JournalChapterRepository;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.feature.journal.day.repository.jpa.JournalDayRepository;
import io.nicheblog.dreamdiary.feature.journal.day.type.JournalDayResolvedAxis;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa.JournalEntryRepository;
import io.nicheblog.dreamdiary.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * JournalDayResolvedGuard
 * <pre>
 *  저널 일자 diaryResolvedYn / dreamResolvedYn 쓰기 잠금 가드.
 *  변경 전: 일자 완결 플래그가 없어 엔트리 RESOLVED 만으로 완료를 표현했다.
 *  변경 후: 축별 수동 완결이 해당 축의 구조·본문·해석·댓글·관련·lifecycle·state 쓰기를 막는다.
 *  일자 날씨·태그·메타 수정과 완결 플래그 자체 토글, 읽기·복사는 허용한다.
 * </pre>
 *
 * @author nichefish
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class JournalDayResolvedGuard {

    private final JournalDayRepository journalDayRepository;
    private final JournalChapterRepository journalChapterRepository;
    private final JournalEntryRepository journalEntryRepository;

    /**
     * 일자 ID + 축으로 쓰기 가능 여부를 검증한다.
     *
     * @param journalDayId 저널 일자 ID
     * @param axis 완결 축
     */
    public void assertWritable(final Integer journalDayId, final JournalDayResolvedAxis axis) {
        if (journalDayId == null || axis == null) return;
        final JournalDayEntity day = journalDayRepository.findById(journalDayId).orElse(null);
        if (day == null) {
            log.warn("[JournalDayResolvedGuard] day not found for lock check. journalDayId={}, axis={}", journalDayId, axis);
            return;
        }
        if (axis == JournalDayResolvedAxis.DIARY && isY(day.getDiaryResolvedYn())) {
            log.info("[JournalDayResolvedGuard] diary axis locked. journalDayId={}", journalDayId);
            throw new BusinessException("journal.day.diary-resolved-locked");
        }
        if (axis == JournalDayResolvedAxis.DREAM && isY(day.getDreamResolvedYn())) {
            log.info("[JournalDayResolvedGuard] dream axis locked. journalDayId={}", journalDayId);
            throw new BusinessException("journal.day.dream-resolved-locked");
        }
    }

    /**
     * 챕터 ID로 축을 판정해 쓰기를 검증한다. DREAM 챕터는 DREAM 축, 그 외(DIARY/NOTE)는 DIARY 축.
     *
     * @param journalChapterId 챕터 ID
     */
    public void assertWritableForChapter(final Integer journalChapterId) {
        if (journalChapterId == null) return;
        final JournalChapterEntity chapter = journalChapterRepository.findById(journalChapterId).orElse(null);
        if (chapter == null) return;
        final JournalDayResolvedAxis axis = chapter.getChapterType() == ChapterType.DREAM
                ? JournalDayResolvedAxis.DREAM
                : JournalDayResolvedAxis.DIARY;
        assertWritable(chapter.getJournalDayId(), axis);
    }

    /**
     * 엔트리 contentType + 챕터로 축을 판정해 쓰기를 검증한다.
     *
     * @param journalChapterId 챕터 ID
     * @param contentType 엔트리 컨텐츠 타입
     */
    public void assertWritableForEntry(final Integer journalChapterId, final ContentType contentType) {
        // Reflection 은 일자 완결축 밖이라 어느 축 잠금에도 걸리지 않는다. (규칙 11)
        if (contentType == ContentType.JOURNAL_REFLECTION) return;
        if (contentType == ContentType.JOURNAL_DREAM) {
            if (journalChapterId == null) return;
            final Integer dayId = journalChapterRepository.findById(journalChapterId)
                    .map(JournalChapterEntity::getJournalDayId)
                    .orElse(null);
            assertWritable(dayId, JournalDayResolvedAxis.DREAM);
            return;
        }
        if (contentType == ContentType.JOURNAL_DIARY || contentType == ContentType.JOURNAL_NOTE) {
            if (journalChapterId == null) return;
            final Integer dayId = journalChapterRepository.findById(journalChapterId)
                    .map(JournalChapterEntity::getJournalDayId)
                    .orElse(null);
            assertWritable(dayId, JournalDayResolvedAxis.DIARY);
        }
    }

    /**
     * refId + refContentType 으로 상위 일자를 찾아 축별 쓰기를 검증한다.
     * JOURNAL_DAY 자체 state(접힘·중요)는 잠금 대상이 아니다.
     *
     * @param refId 참조 ID
     * @param refContentType 참조 컨텐츠 타입
     */
    public void assertWritableForRef(final Integer refId, final ContentType refContentType) {
        if (refId == null || refContentType == null) return;
        if (refContentType == ContentType.JOURNAL_DAY) return;

        switch (refContentType) {
            case JOURNAL_CHAPTER -> assertWritableForChapter(refId);
            case JOURNAL_DIARY, JOURNAL_NOTE, JOURNAL_DREAM -> {
                final JournalEntryEntity entry = journalEntryRepository.findById(refId).orElse(null);
                if (entry == null || entry.getJournalChapter() == null) return;
                assertWritableForEntry(entry.getJournalChapter().getId(), refContentType);
            }
            case JOURNAL_REFLECTION -> { /* Reflection 은 완결축 밖: 잠금 없음 (규칙 11) */ }
            case JOURNAL_THREAD -> { /* 스레드는 일자 완결축 밖: 잠금 없음 (thread-relation 설계 §5) */ }
            default -> { /* 저널 외·일자 자체는 무시 */ }
        }
    }

    /**
     * 문자열 contentType 으로 호출하는 오버로드.
     *
     * @param refId 참조 ID
     * @param refContentTypeKey contentType key
     */
    public void assertWritableForRef(final Integer refId, final String refContentTypeKey) {
        if (StringUtils.isBlank(refContentTypeKey)) return;
        final ContentType type;
        try {
            type = ContentType.valueOf(refContentTypeKey);
        } catch (final IllegalArgumentException e) {
            log.warn("[JournalDayResolvedGuard] unknown contentType, write guard skipped. refId={}, contentType={}", refId, refContentTypeKey);
            return;
        }
        assertWritableForRef(refId, type);
    }

    private static boolean isY(final String yn) {
        return "Y".equalsIgnoreCase(StringUtils.trimToEmpty(yn));
    }
}
