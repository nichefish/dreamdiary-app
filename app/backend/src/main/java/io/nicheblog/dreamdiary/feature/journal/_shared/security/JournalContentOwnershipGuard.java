package io.nicheblog.dreamdiary.feature.journal._shared.security;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.jpa.JournalChapterRepository;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.feature.journal.day.repository.jpa.JournalDayRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa.JournalEntryRepository;
import io.nicheblog.dreamdiary.feature.journal.reflection.entity.JournalReflectionEntity;
import io.nicheblog.dreamdiary.feature.journal.reflection.repository.jpa.JournalReflectionRepository;
import io.nicheblog.dreamdiary.feature.journal.thread.repository.jpa.JournalThreadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 부착 상태·라이프사이클 변경 대상 저널 콘텐츠의 존재와 소유권을 검증한다.
 *
 * <p>라이프사이클 대상인 엔트리·해석·스레드와 상태 대상인 일자·챕터·엔트리·해석이
 * 같은 원본 소유권 계약을 사용하도록 공통 경계에서 검증한다.</p>
 *
 * <p>해석({@code JOURNAL_REFLECTION}) 쓰기는 대상(About-A)이 속한 {@code journal_day.owner_id}와
 * 인증 {@code userId}를 비교한다. {@code created_by}는 감사 스냅샷이며 인가 기준이 아니다.
 * 응답 DTO의 {@code isOwnedBy}는 뷰어가 그 대상 일자를 소유하는가다. {@code isCreatedBy}는 감사 작성자 일치다.</p>
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
     * 일자와 해석은 {@code journal_day.owner_id}를, 그 외 지원 타입은 원본 {@code created_by}를 사용한다.
     *
     * @param refId 대상 콘텐츠 ID
     * @param contentType 대상 콘텐츠 타입
     */
    public void assertOwned(final Integer refId, final ContentType contentType) {
        if (ContentType.JOURNAL_DAY.equals(contentType) || ContentType.JOURNAL_REFLECTION.equals(contentType)) {
            final Integer loginUserId = AuthUtils.requireLoginUserId();
            final Integer ownerId = ContentType.JOURNAL_DAY.equals(contentType)
                    ? findDayOwnerId(refId)
                    : findReflectionTargetDayOwnerId(refId);
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

    /**
     * Reflection 표시 DTO의 {@code isOwnedBy}를 대상 일자 소유 여부로 채운다.
     * {@code createdBy}/{@code isCreatedBy} 감사 축은 유지한다. 일자를 해석할 수 없으면 잠근다.
     *
     * @param dtos Reflection 표시 DTO 목록
     */
    public void applyReflectionViewerOwnership(final Collection<JournalEntryDto> dtos) {
        if (dtos == null || dtos.isEmpty()) return;

        final Integer loginUserId = AuthUtils.requireLoginUserId();
        final Map<String, Integer> dayIdByTarget = new HashMap<>();
        final Set<Integer> dayIds = new HashSet<>();
        for (final JournalEntryDto dto : dtos) {
            if (dto == null) continue;
            final String key = targetKey(dto.getRefId(), dto.getRefContentType());
            final Integer dayId = dayIdByTarget.computeIfAbsent(
                    key, k -> resolveTargetJournalDayId(dto.getRefId(), dto.getRefContentType())
            );
            if (dayId != null) dayIds.add(dayId);
        }

        final Map<Integer, Integer> ownerByDayId = new HashMap<>();
        if (!dayIds.isEmpty()) {
            for (final JournalDayEntity day : journalDayRepository.findAllById(dayIds)) {
                if (day == null || day.getId() == null) continue;
                ownerByDayId.put(day.getId(), day.getOwnerId());
            }
        }

        for (final JournalEntryDto dto : dtos) {
            if (dto == null) continue;
            final Integer dayId = dayIdByTarget.get(targetKey(dto.getRefId(), dto.getRefContentType()));
            final Integer ownerId = dayId == null ? null : ownerByDayId.get(dayId);
            dto.setIsOwnedBy(Objects.equals(loginUserId, ownerId));
        }
    }

    /**
     * About-A 대상이 속한 {@code journal_day.id}를 해석한다.
     * R→R 은 한 단계 위 대상까지 따라간다. 해석 불가면 {@code null}.
     *
     * @param refId 대상 엔티티 번호
     * @param refContentType 대상 콘텐츠 타입
     * @return 대상 일자 ID. 해석 불가 시 {@code null}
     */
    public Integer resolveTargetJournalDayId(final Integer refId, final ContentType refContentType) {
        if (refId == null || refContentType == null) return null;
        return switch (refContentType) {
            case JOURNAL_DAY -> refId;
            case JOURNAL_CHAPTER -> journalChapterRepository.findById(refId)
                    .map(entity -> entity.getJournalDayId())
                    .orElse(null);
            case JOURNAL_DIARY, JOURNAL_DREAM, JOURNAL_NOTE -> resolveEntryJournalDayId(refId);
            case JOURNAL_REFLECTION -> resolveParentReflectionTargetDayId(refId);
            default -> null;
        };
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
            case JOURNAL_THREAD -> journalThreadRepository.findById(refId)
                    .map(entity -> entity.getCreatedBy())
                    .orElse(null);
            default -> null;
        };
    }

    /** 일자 원본의 {@code owner_id}를 조회한다. 없으면 {@code null}. */
    private Integer findDayOwnerId(final Integer dayId) {
        if (dayId == null) return null;
        return journalDayRepository.findById(dayId)
                .map(JournalDayEntity::getOwnerId)
                .orElse(null);
    }

    /**
     * Reflection 은 journal_reflection 테이블에 독립 영속된다. journal_entry 가 아니다.
     * 쓰기는 대상(About-A)이 속한 일자의 {@code owner_id}로 인가한다.
     */
    private Integer findReflectionTargetDayOwnerId(final Integer reflectionId) {
        if (reflectionId == null) return null;
        final JournalReflectionEntity reflection = journalReflectionRepository.findById(reflectionId).orElse(null);
        if (reflection == null) return null;
        final Integer dayId = resolveTargetJournalDayId(reflection.getRefId(), reflection.getRefContentType());
        return findDayOwnerId(dayId);
    }

    /** 엔트리의 소속 챕터에서 일자 ID를 읽는다. 챕터 연관이 비면 챕터 ID로 한 번 더 조회한다. */
    private Integer resolveEntryJournalDayId(final Integer entryId) {
        return journalEntryRepository.findById(entryId)
                .map(this::dayIdOfEntry)
                .orElse(null);
    }

    /** 엔트리의 소속 일자 ID. 챕터 연관의 {@code journalDayId}를 우선한다. */
    private Integer dayIdOfEntry(final JournalEntryEntity entity) {
        if (entity.getJournalChapter() != null && entity.getJournalChapter().getJournalDayId() != null) {
            return entity.getJournalChapter().getJournalDayId();
        }
        if (entity.getJournalChapterId() == null) return null;
        return journalChapterRepository.findById(entity.getJournalChapterId())
                .map(chapter -> chapter.getJournalDayId())
                .orElse(null);
    }

    /**
     * R→R: 부모 Reflection 의 대상을 일자로 한 단계 해석한다.
     * 부모 대상이 다시 Reflection 이면 여기서 멈춘다.
     */
    private Integer resolveParentReflectionTargetDayId(final Integer parentReflectionId) {
        final JournalReflectionEntity parent = journalReflectionRepository.findById(parentReflectionId).orElse(null);
        if (parent == null) return null;
        final Integer refId = parent.getRefId();
        final ContentType refContentType = parent.getRefContentType();
        if (refId == null || refContentType == null || refContentType == ContentType.JOURNAL_REFLECTION) return null;
        return resolveTargetJournalDayId(refId, refContentType);
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

    private static String targetKey(final Integer refId, final ContentType refContentType) {
        return String.valueOf(refId) + ":" + refContentType;
    }
}
