package io.nicheblog.dreamdiary.feature.journal.thread.repository.jpa;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.entity.LifecycleEntity;
import io.nicheblog.dreamdiary.feature.journal.thread.entity.JournalThreadEntity;
import io.nicheblog.dreamdiary.feature.journal.thread.entity.JournalThreadEntityTestFactory;
import io.nicheblog.dreamdiary.feature.journal.thread.entity.JournalThreadEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadCandidateProjection;
import io.nicheblog.dreamdiary.feature.journal.thread.spec.JournalThreadSpec;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.config.DataSourceConfig;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;
import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JournalThreadRepositoryTest
 * <pre>
 *  저널 스레드 (JPA) Repository 테스트 모듈
 *  "@Transactional 환경에서는 flush가 의도한 대로 작동하지 않을 수 있다."
 * </pre>
 *
 * @author nichefish
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(DataSourceConfig.class)
@Import({TestAuditConfig.class, JournalThreadSpec.class})
@Log4j2
class JournalThreadRepositoryTest {

    @Resource
    private JournalThreadRepository journalThreadRepository;
    @Resource
    private JournalThreadEntryRepository journalThreadEntryRepository;
    @Resource
    private JournalThreadSpec journalThreadSpec;
    @Resource
    private TestEntityManager entityManager;

    private JournalThreadEntity journalThreadEntity;

    /**
     * 각 테스트 시작 전 세팅 초기화.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통적으로 사용할 journalDayEntity 초기화
        journalThreadEntity = JournalThreadEntityTestFactory.create();
    }

    /**
     * regist 테스트
     */
    @Test
    public void testRegist() throws Exception {
        // Given::

        // When::
        final JournalThreadEntity registered = journalThreadRepository.save(journalThreadEntity);
        final Integer key = registered.getId();
        final JournalThreadEntity retrieved = journalThreadRepository.findById(key).orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.registered")));

        // Then::
        assertNotNull(retrieved, "저장한 데이터를 조회할 수 없습니다.");
        assertNotNull(retrieved.getId(), "저장된 엔티티의 key 값이 없습니다.");
        // audit
        assertNotNull(retrieved.getCreatedAt(), "등록일자 audit 처리가 되지 않았습니다.");
        assertNotNull(retrieved.getCreatedBy(),  "등록자 audit 처리가 되지 않았습니다.");
        assertEquals(TestConstant.TEST_AUDITOR, retrieved.getCreatedBy(), "등록자가 예상 값과 일치하지 않습니다.");
    }

    /**
     * modify 테스트
     */
    @Test
    public void testModify() throws Exception {
        // Given::
        JournalThreadEntity registered = journalThreadRepository.save(journalThreadEntity);
        Integer key = registered.getId();

        // When::
        JournalThreadEntity toModify = journalThreadRepository.findById(key).orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.to-modify")));
        toModify.setContent("modified");
        JournalThreadEntity modified = journalThreadRepository.save(toModify);

        // Then::
        assertNotNull(modified, "저장한 데이터를 조회할 수 없습니다.");
        assertNotNull(modified.getId(), "저장된 엔티티의 key 값이 없습니다.");
        // audit
        assertNotNull(modified.getUpdatedAt(), "수정일자 audit 처리가 되지 않았습니다.");
        assertNotNull(modified.getUpdatedBy(),  "수정자 audit 처리가 되지 않았습니다.");
        assertEquals(TestConstant.TEST_AUDITOR, modified.getUpdatedBy(), "수정자가 예상 값과 일치하지 않습니다.");
        // value
        assertEquals("modified", modified.getContent(), "값이 정상적으로 수정되지 않았습니다.");
    }

    /**
     * delete 테스트
     */
    @Test
    public void testDelete() throws Exception {
        // Given::
        final JournalThreadEntity registered = journalThreadRepository.save(journalThreadEntity);
        final Integer key = registered.getId();

        // When::
        final JournalThreadEntity toDelete = journalThreadRepository.findById(key).orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.to-delete")));
        journalThreadRepository.delete(toDelete);

        final JournalThreadEntity retrieved = journalThreadRepository.findById(key).orElse(null);

        // Then::
        assertNull(retrieved, "삭제가 제대로 이루어지지 않았습니다.");
    }

    /**
     * 소속 후보 정렬·제목 검색·분류 필터 계약 검증.
     */
    @Test
    void findCandidatesRanksMembershipContextAndAppliesFilters() {
        final String username = "candidate_user";
        final int currentEntryId = 7001;
        final JournalThreadEntity current = candidateThread(
                "현재 소속 스레드", "CASE", username, LocalDateTime.of(2026, 7, 1, 10, 0));
        final JournalThreadEntity recent = candidateThread(
                "최근 사용 스레드", "CASE", username, LocalDateTime.of(2026, 7, 2, 10, 0));
        final JournalThreadEntity frequent = candidateThread(
                "자주 사용한 스레드", "REVIEW", username, LocalDateTime.of(2026, 7, 3, 10, 0));
        final JournalThreadEntity unused = candidateThread(
                "미사용 스레드", "CASE", username, LocalDateTime.of(2026, 7, 4, 10, 0));
        journalThreadRepository.saveAll(List.of(current, recent, frequent, unused));
        journalThreadRepository.flush();

        journalThreadEntryRepository.saveAll(List.of(
                candidateMembership(current.getId(), currentEntryId, username, LocalDateTime.of(2026, 7, 5, 10, 0)),
                candidateMembership(recent.getId(), 7002, username, LocalDateTime.of(2026, 7, 20, 10, 0)),
                candidateMembership(frequent.getId(), 7003, username, LocalDateTime.of(2026, 7, 10, 10, 0)),
                candidateMembership(frequent.getId(), 7004, username, LocalDateTime.of(2026, 7, 11, 10, 0))
        ));
        journalThreadEntryRepository.flush();

        final List<JournalThreadCandidateProjection> ranked = journalThreadRepository.findCandidates(
                username, currentEntryId, "", "", "N",
                PageRequest.of(0, 10));
        assertEquals(
                List.of("현재 소속 스레드", "최근 사용 스레드", "자주 사용한 스레드", "미사용 스레드"),
                ranked.stream().map(JournalThreadCandidateProjection::getTitle).toList()
        );
        assertEquals(1L, ranked.get(0).getCurrentEntryMembershipCount().longValue());
        assertEquals(2L, ranked.get(2).getMembershipCount().longValue());

        final List<JournalThreadCandidateProjection> titleFiltered = journalThreadRepository.findCandidates(
                username, currentEntryId, "사용", "", "N",
                PageRequest.of(0, 10));
        assertEquals(
                List.of("최근 사용 스레드", "자주 사용한 스레드", "미사용 스레드"),
                titleFiltered.stream().map(JournalThreadCandidateProjection::getTitle).toList()
        );

        final List<JournalThreadCandidateProjection> categoryFiltered = journalThreadRepository.findCandidates(
                username, currentEntryId, "", "CASE", "N",
                PageRequest.of(0, 10));
        assertEquals(
                List.of("현재 소속 스레드", "최근 사용 스레드", "미사용 스레드"),
                categoryFiltered.stream().map(JournalThreadCandidateProjection::getTitle).toList()
        );
    }

    /**
     * 메인 스레드 목록 Specification의 분류 equal 필터 계약 검증.
     */
    @Test
    void findAllWithSpecAppliesCategoryFilter() {
        final JournalThreadEntity caseThread = candidateThread(
                "이슈 추적 스레드", "CASE", TestConstant.TEST_AUDITOR, LocalDateTime.of(2026, 7, 1, 10, 0));
        final JournalThreadEntity reviewThread = candidateThread(
                "회고 스레드", "REVIEW", TestConstant.TEST_AUDITOR, LocalDateTime.of(2026, 7, 2, 10, 0));
        journalThreadRepository.saveAll(List.of(caseThread, reviewThread));
        journalThreadRepository.flush();

        final Page<JournalThreadEntity> result = journalThreadRepository.findAll(
                journalThreadSpec.searchWith(Map.of("categoryCode", "CASE")),
                PageRequest.of(0, 10)
        );

        assertEquals(List.of("이슈 추적 스레드"), result.stream().map(JournalThreadEntity::getTitle).toList());
    }


    /**
     * 후보 API는 기본으로 RESOLVED 를 숨기고, includeResolved=Y 일 때만 포함한다.
     * 라이프사이클 행이 없으면 OPEN 으로 취급한다.
     */
    @Test
    void findCandidatesExcludesResolvedUnlessRequested() {
        final String username = "lifecycle_candidate_user";
        final int currentEntryId = 7101;
        final JournalThreadEntity openThread = candidateThread(
                "열린 스레드", "CASE", username, LocalDateTime.of(2026, 7, 1, 10, 0));
        final JournalThreadEntity resolvedThread = candidateThread(
                "완료 스레드", "CASE", username, LocalDateTime.of(2026, 7, 2, 10, 0));
        journalThreadRepository.saveAll(List.of(openThread, resolvedThread));
        journalThreadRepository.flush();

        entityManager.persist(LifecycleEntity.builder()
                .refId(resolvedThread.getId())
                .refContentType("JOURNAL_THREAD")
                .lifecycleKey("RESOLVED")
                .build());
        entityManager.flush();

        final List<JournalThreadCandidateProjection> defaultCandidates = journalThreadRepository.findCandidates(
                username, currentEntryId, "", "", "N",
                PageRequest.of(0, 10));
        assertEquals(List.of("열린 스레드"), defaultCandidates.stream().map(JournalThreadCandidateProjection::getTitle).toList());
        assertEquals("OPEN", defaultCandidates.get(0).getLifecycleKey());

        final List<JournalThreadCandidateProjection> withResolved = journalThreadRepository.findCandidates(
                username, currentEntryId, "", "", "Y",
                PageRequest.of(0, 10));
        assertEquals(
                List.of("완료 스레드", "열린 스레드"),
                withResolved.stream().map(JournalThreadCandidateProjection::getTitle).toList()
        );
        assertEquals("RESOLVED", withResolved.get(0).getLifecycleKey());
    }

    /**
     * 목록 Spec 의 lifecycleKey 필터. OPEN 은 행 없음을 포함한다.
     */
    @Test
    void findAllWithSpecAppliesLifecycleKeyFilter() {
        final JournalThreadEntity openThread = candidateThread(
                "필터 열린 스레드", "CASE", TestConstant.TEST_AUDITOR, LocalDateTime.of(2026, 7, 1, 10, 0));
        final JournalThreadEntity pendingThread = candidateThread(
                "필터 보류 스레드", "CASE", TestConstant.TEST_AUDITOR, LocalDateTime.of(2026, 7, 2, 10, 0));
        final JournalThreadEntity resolvedThread = candidateThread(
                "필터 완료 스레드", "CASE", TestConstant.TEST_AUDITOR, LocalDateTime.of(2026, 7, 3, 10, 0));
        journalThreadRepository.saveAll(List.of(openThread, pendingThread, resolvedThread));
        journalThreadRepository.flush();

        entityManager.persist(LifecycleEntity.builder()
                .refId(pendingThread.getId())
                .refContentType("JOURNAL_THREAD")
                .lifecycleKey("PENDING")
                .build());
        entityManager.persist(LifecycleEntity.builder()
                .refId(resolvedThread.getId())
                .refContentType("JOURNAL_THREAD")
                .lifecycleKey("RESOLVED")
                .build());
        entityManager.flush();

        final Page<JournalThreadEntity> openOnly = journalThreadRepository.findAll(
                journalThreadSpec.searchWith(Map.of("lifecycleKey", "OPEN")),
                PageRequest.of(0, 10)
        );
        assertEquals(List.of("필터 열린 스레드"), openOnly.stream().map(JournalThreadEntity::getTitle).toList());

        final Page<JournalThreadEntity> pendingOnly = journalThreadRepository.findAll(
                journalThreadSpec.searchWith(Map.of("lifecycleKey", "PENDING")),
                PageRequest.of(0, 10)
        );
        assertEquals(List.of("필터 보류 스레드"), pendingOnly.stream().map(JournalThreadEntity::getTitle).toList());
    }

    /** 후보 쿼리용 가상 스레드 픽스처를 만든다. */
    private JournalThreadEntity candidateThread(
            final String title,
            final String categoryCode,
            final String username,
            final LocalDateTime createdAt
    ) {
        return JournalThreadEntity.builder()
                .contentType("JOURNAL_THREAD")
                .title(title)
                .categoryCode(categoryCode)
                .createdBy(username)
                .createdAt(createdAt)
                .build();
    }

    /** 후보 쿼리용 가상 소속 픽스처를 만든다. */
    private JournalThreadEntryEntity candidateMembership(
            final Integer threadId,
            final Integer entryId,
            final String username,
            final LocalDateTime createdAt
    ) {
        return JournalThreadEntryEntity.builder()
                .threadId(threadId)
                .entryId(entryId)
                .createdBy(username)
                .createdAt(createdAt)
                .build();
    }
}
