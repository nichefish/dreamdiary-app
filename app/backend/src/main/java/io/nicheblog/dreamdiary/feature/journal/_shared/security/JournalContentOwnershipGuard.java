package io.nicheblog.dreamdiary.feature.journal._shared.security;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.jpa.JournalChapterRepository;
import io.nicheblog.dreamdiary.feature.journal.day.repository.jpa.JournalDayRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa.JournalEntryRepository;
import io.nicheblog.dreamdiary.feature.journal.reflection.repository.jpa.JournalReflectionRepository;
import io.nicheblog.dreamdiary.feature.journal.thread.repository.jpa.JournalThreadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 부착 상태·라이프사이클 변경 대상 저널 콘텐츠의 존재와 소유권을 검증한다.
 *
 * <p>라이프사이클 대상인 엔트리·해석·스레드와 상태 대상인 일자·챕터·엔트리·해석이
 * 같은 원본 소유권 계약을 사용하도록 공통 경계에서 검증한다.</p>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JournalContentOwnershipGuard {

    private final JournalDayRepository journalDayRepository;
    private final JournalChapterRepository journalChapterRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final JournalReflectionRepository journalReflectionRepository;
    private final JournalThreadRepository journalThreadRepository;

    /**
     * 현재 로그인 사용자가 상태·라이프사이클 대상 원본 콘텐츠의 소유자인지 검증한다.
     * 원본이 없거나 소유자가 다르면 같은 권한 오류로 거부해 대상 존재 여부를 노출하지 않는다.
     *
     * @param refId 대상 콘텐츠 ID
     * @param contentType 대상 콘텐츠 타입
     */
    public void assertOwned(final Integer refId, final ContentType contentType) {
        if (ContentType.JOURNAL_DAY.equals(contentType)) {
            final Integer loginUserId = AuthUtils.requireLoginUserId();
            final Integer ownerId = refId == null ? null : journalDayRepository.findById(refId)
                    .map(entity -> entity.getOwnerId())
                    .orElse(null);
            if (Objects.equals(loginUserId, ownerId)) return;

            log.warn(
                    "[JournalContentOwnership] 변경 거부. contentType={}, refId={}, loginUserId={}, ownerPresent={}",
                    contentType,
                    refId,
                    loginUserId,
                    ownerId != null
            );
            throw new NotAuthorizedException("common.result.access-not-authorized");
        }

        final String username = AuthUtils.requireLoginUsername();
        final String owner = findOwner(refId, contentType);
        if (username.equals(owner)) return;

        log.warn(
                "[JournalContentOwnership] 변경 거부. contentType={}, refId={}, username={}, ownerPresent={}",
                contentType,
                refId,
                username,
                owner != null
        );
        throw new NotAuthorizedException("common.result.access-not-authorized");
    }

    /** 작성자 감사 문자열을 소유 경계로 사용하는 콘텐츠 타입의 원본 엔티티에서 작성자 ID를 조회한다. */
    private String findOwner(final Integer refId, final ContentType contentType) {
        if (refId == null || contentType == null) return null;
        return switch (contentType) {
            case JOURNAL_CHAPTER -> journalChapterRepository.findById(refId)
                    .map(entity -> entity.getCreatedBy())
                    .orElse(null);
            case JOURNAL_DIARY, JOURNAL_NOTE, JOURNAL_DREAM -> journalEntryRepository.findById(refId)
                    .filter(entity -> contentType.equals(resolveEntryContentType(entity)))
                    .map(JournalEntryEntity::getCreatedBy)
                    .orElse(null);
            // Reflection 은 journal_reflection 테이블에 독립 영속된다. journal_entry 가 아니다.
            case JOURNAL_REFLECTION -> journalReflectionRepository.findById(refId)
                    .map(entity -> entity.getCreatedBy())
                    .orElse(null);
            case JOURNAL_THREAD -> journalThreadRepository.findById(refId)
                    .map(entity -> entity.getCreatedBy())
                    .orElse(null);
            default -> null;
        };
    }

    /** 엔트리의 소속 챕터 유형을 상태·라이프사이클 요청에 사용하는 논리 콘텐츠 타입으로 변환한다. */
    private ContentType resolveEntryContentType(final JournalEntryEntity entity) {
        if (entity.getJournalChapter() == null || entity.getJournalChapter().getChapterType() == null) return null;
        return switch (entity.getJournalChapter().getChapterType()) {
            case DIARY -> ContentType.JOURNAL_DIARY;
            case NOTE -> ContentType.JOURNAL_NOTE;
            case DREAM -> ContentType.JOURNAL_DREAM;
        };
    }
}
