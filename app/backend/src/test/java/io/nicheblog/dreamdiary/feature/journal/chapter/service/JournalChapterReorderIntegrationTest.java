package io.nicheblog.dreamdiary.feature.journal.chapter.service;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.feature.journal.JournalTestUserSupport;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.jpa.JournalChapterRepository;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.feature.journal.day.repository.jpa.JournalDayRepository;
import io.nicheblog.dreamdiary.feature.user.account.repository.jpa.UserRepository;
import io.nicheblog.dreamdiary.global.TestConstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 챕터 # 재정렬의 영속성 이음새 계약을 실제 DB(Hibernate + MyBatis)에서 검증한다.
 * <p>
 * 챕터 # 재정렬은 실제 JPA 엔티티에서 단일 패스({@code normalizeSortOrder})로 수행한다.
 * 요약·DREAM 을 버킷으로 판별해 순번 밖(0)으로 두고 일반 챕터만 1..N 을 잇는다.
 * mock 단위 테스트로는 실제 저장·재조회 결과를 검증할 수 없어, 이 통합 테스트로 그 계약을 고정한다.
 * </p>
 * <p><b>불변식</b>: 재정렬 후 일반(순번 대상) 챕터의 {@code sort_order} 는 1..N 유니크·연속이며,
 * 요약·DREAM 은 순번 밖(0)이고, 대상 챕터는 요청한 위치에 온다.</p>
 *
 * @author nichefish
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestAuditConfig.class)
@Transactional
class JournalChapterReorderIntegrationTest {

    @Resource
    private JournalChapterService journalChapterService;
    @Resource
    private JournalChapterRepository journalChapterRepository;
    @Resource
    private JournalDayRepository journalDayRepository;
    @Resource
    private UserRepository userRepository;
    @PersistenceContext
    private EntityManager entityManager;

    private Integer ownerId;
    private Integer dayId;

    @BeforeEach
    void setUpOwner() throws Exception {
        ownerId = JournalTestUserSupport.ensureUser(userRepository, TestConstant.TEST_AUDITOR);
        JournalTestUserSupport.authenticate(ownerId, TestConstant.TEST_AUDITOR);
        dayId = journalDayRepository.saveAndFlush(JournalDayEntity.builder()
                .ownerId(ownerId).journalDate(LocalDate.of(2026, 8, 13)).yy(2026).mnth(8).build()).getId();
    }

    /**
     * 요약·일반 4개·DREAM 이 있는 날에서 일반 챕터를 다른 위치로 옮기면,
     * DB 재조회 시 일반 챕터 sort_order 는 여전히 1..N 유니크·연속이어야 한다(중복·결번 금지).
     * 요약·DREAM 은 순번 밖(0), 옮긴 챕터는 요청 위치에 온다.
     */
    @Test
    void reorderKeepsUniqueContiguousSortOrder() throws Exception {
        final Integer summaryId = saveChapter("Y", ChapterType.DIARY, 0, "요약");
        final Integer aId = saveChapter("N", ChapterType.DIARY, 1, "챕터 A");
        final Integer bId = saveChapter("N", ChapterType.DIARY, 2, "챕터 B");
        final Integer cId = saveChapter("N", ChapterType.DIARY, 3, "챕터 C");
        final Integer dId = saveChapter("N", ChapterType.DIARY, 4, "챕터 D");
        final Integer dreamId = saveChapter("N", ChapterType.DREAM, 0, "꿈");

        // 세션에 managed 엔티티를 남긴 채 재정렬한다(프로덕션 postModify 경로와 동일).
        // D 를 일반 챕터 기준 #2 위치로 이동한다: 기대 순서 A(1)·D(2)·B(3)·C(4).
        journalChapterService.normalizeSortOrder(dayId, dId, 2);

        // DB 재조회(세션 캐시 우회).
        entityManager.flush();
        entityManager.clear();
        final List<JournalChapterEntity> reloaded = journalChapterRepository.findAllByJournalDayId(dayId);

        final List<Integer> normalOrders = new ArrayList<>();
        for (final JournalChapterEntity c : reloaded) {
            if ("Y".equals(c.getSummaryYn())) {
                assertEquals(0, c.getSortOrder(), "요약은 순번 밖(0)이어야 한다: id=" + c.getId());
            } else if (c.getChapterType() == ChapterType.DREAM) {
                assertEquals(0, c.getSortOrder(), "DREAM 은 순번 밖(0)이어야 한다: id=" + c.getId());
            } else {
                normalOrders.add(c.getSortOrder());
            }
        }

        // 일반 챕터: 유니크·연속 1..N (중복·결번 금지).
        final Set<Integer> unique = new HashSet<>(normalOrders);
        assertEquals(normalOrders.size(), unique.size(), "일반 챕터 sort_order 에 중복이 없어야 한다: " + normalOrders);
        assertEquals(4, normalOrders.size(), "일반 챕터 4개");
        for (int i = 1; i <= 4; i++) {
            assertTrue(unique.contains(i), "일반 챕터 sort_order 는 1.." + normalOrders.size() + " 연속이어야 한다(결번 " + i + "): " + normalOrders);
        }

        // 대상 D 는 요청한 #2 위치.
        final JournalChapterEntity movedD = findById(reloaded, dId);
        assertEquals(2, movedD.getSortOrder(), "이동한 챕터 D 는 일반 기준 #2 여야 한다");
        // 나머지 상대 순서 유지: A(1)·B(3)·C(4).
        assertEquals(1, findById(reloaded, aId).getSortOrder());
        assertEquals(3, findById(reloaded, bId).getSortOrder());
        assertEquals(4, findById(reloaded, cId).getSortOrder());
        // 미사용 경고 방지용 참조.
        assertTrue(summaryId != null && dreamId != null);
    }

    private Integer saveChapter(final String summaryYn, final ChapterType type, final int sortOrder, final String title) {
        return journalChapterRepository.saveAndFlush(JournalChapterEntity.builder()
                .chapterType(type).journalDayId(dayId).summaryYn(summaryYn).sortOrder(sortOrder).title(title).build()).getId();
    }

    private static JournalChapterEntity findById(final List<JournalChapterEntity> list, final Integer id) {
        return list.stream().filter(c -> id.equals(c.getId())).findFirst().orElseThrow();
    }
}
