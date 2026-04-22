package io.nicheblog.dreamdiary.feature.journal.entry.service.policy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.jpa.JournalChapterRepository;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa.JournalEntryRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.type.JournalEntryType;
import io.nicheblog.dreamdiary.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JournalEntryTypeResolver {

    private final JournalChapterRepository journalChapterRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryPolicyResolver policyResolver;

    /**
     * 챕터 ID에서 엔트리 콘텐츠 타입을 해석한다.
     *
     * @param journalChapterId 챕터 ID
     * @return 엔트리 콘텐츠 타입
     */
    public ContentType resolveByChapterId(final Integer journalChapterId) {
        final JournalChapterEntity chapter = journalChapterRepository.findById(journalChapterId)
                .orElseThrow(() -> new BusinessException("msg.journal.chapter.not-found"));
        if (chapter.getChapterType() == ChapterType.NOTE) {
            return ContentType.JOURNAL_DIARY;
        }
        return Arrays.stream(JournalEntryTypePolicy.values())
                .filter(policy -> policy.expectedChapterType == chapter.getChapterType())
                .findFirst()
                .map(policy -> policy.contentType)
                .orElseThrow(() -> new BusinessException("msg.journal.entry.invalid-chapter-type"));
    }

    /**
     * 엔트리 ID에서 엔트리 콘텐츠 타입을 해석한다.
     *
     * @param entryId 엔트리 ID
     * @return 엔트리 콘텐츠 타입
     */
    public ContentType resolveByEntryId(final Integer entryId) {
        final JournalEntryEntity entity = journalEntryRepository.findById(entryId)
                .orElseThrow(() -> new EntityNotFoundException("exception.EntityNotFoundException"));
        final ContentType contentType = ContentType.get(entity.getContentType());
        if (!policyResolver.isEntryType(contentType.key)) {
            throw new BusinessException("msg.journal.entry.invalid-chapter-type");
        }
        return contentType;
    }

    /**
     * 요청 문자열 타입을 엔트리 콘텐츠 타입으로 해석한다.
     *
     * @param rawType 타입 파라미터 문자열
     * @return 엔트리 콘텐츠 타입
     */
    public ContentType resolveByRawType(final String rawType) {
        return JournalEntryType.from(normalizeRawType(rawType)).toContentType();
    }

    /**
     * 타입 파라미터 우선, 없으면 fallback 문자열로 타입을 해석한다.
     *
     * @param rawType 타입 파라미터 문자열
     * @param fallbackContentType fallback 콘텐츠 타입 문자열
     * @return 엔트리 콘텐츠 타입
     */
    public ContentType resolveByRawTypeOrFallback(final String rawType, final String fallbackContentType) {
        final String source = StringUtils.isNotBlank(rawType) ? rawType : fallbackContentType;
        return resolveByRawType(source);
    }

    /**
     * 공백/대소문자 편차를 정규화하고 필수값을 검증한다.
     *
     * @param rawType 타입 파라미터 문자열
     * @return 정규화된 타입 문자열
     */
    private String normalizeRawType(final String rawType) {
        if (StringUtils.isBlank(rawType)) {
            throw new IllegalArgumentException("type is required.");
        }
        return rawType.trim().toUpperCase();
    }
}
