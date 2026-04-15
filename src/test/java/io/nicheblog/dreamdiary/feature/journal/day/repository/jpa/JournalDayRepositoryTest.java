package io.nicheblog.dreamdiary.feature.journal.day.repository.jpa;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.feature.journal.diary.entity.JournalDiaryEntity;
import io.nicheblog.dreamdiary.feature.journal.dream.repository.jpa.JournalDreamRepository;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntityTestFactory;
import io.nicheblog.dreamdiary.feature.journal.diary.entity.JournalDiaryEntityTestFactory;
import io.nicheblog.dreamdiary.feature.journal.diary.repository.jpa.JournalDiaryRepository;
import io.nicheblog.dreamdiary.feature.journal.dream.entity.JournalDreamEntity;
import io.nicheblog.dreamdiary.feature.journal.dream.entity.JournalDreamEntityTestFactory;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.config.DataSourceConfig;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;
import javax.persistence.EntityNotFoundException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.testng.Assert.assertTrue;

/**
 * JournalDayRepositoryTest
 * <pre>
 *  저널 일자 (JPA) Repository 테스트 모듈.
 *  "@Transactional 환경에서는 flush가 의도한 대로 작동하지 않을 수 있다."
 * </pre>
 *
 * @author nichefish
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(DataSourceConfig.class)
@Import(TestAuditConfig.class)
@Log4j2
class JournalDayRepositoryTest {

    @Resource
    private TestEntityManager testEntityManager;

    @Resource
    private JournalDayRepository journalDayRepository;
    @Resource
    private JournalDreamRepository journalDreamRepository;
    @Resource
    private JournalDiaryRepository journalDiaryRepository;

    private JournalDayEntity journalDayEntity;

    // TODO: 경계값, 예외값 테스트하기
    // TODO: Assertion 세분화하기
    // TODO: Parameterized Test 사용
    // JUnit의 @ParameterizedTest를 사용하여 같은 동작을 여러 가지 데이터로 반복 테스트할 때 유용합니다. 예를 들어, 여러 날짜에 대해 중복 검사를 하는 테스트라면 매번 테스트 메서드를 작성하는 대신, 파라미터화된 테스트로 통합할 수 있습니다.
    // @ParameterizedTest
    // @ValueSource(strings = {"2000-01-01", "2000-01-02"})

    /**
     * 각 테스트 시작 전 세팅 초기화.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통적으로 사용할 journalDayEntity 초기화
        journalDayEntity = JournalDayEntityTestFactory.createWithJournalDt("2000-01-01");
    }

    /**
     * regist 테스트
     */
    @Test
    @DisplayName("저널 날짜가 정상적으로 등록되는지 테스트합니다.")
    public void testRegist() throws Exception {
        // Given::

        // When:: 데이터를 등록한다.
        final JournalDayEntity registered = journalDayRepository.save(journalDayEntity);
        final Integer key = registered.getId();
        final JournalDayEntity retrieved = journalDayRepository.findById(key).orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.registered")));

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
        // Given:: 수정할 데이터를 등록한다.
        final JournalDayEntity registered = journalDayRepository.save(journalDayEntity);
        final Integer key = registered.getId();

        // When:: 등록된 데이터를 조회해서, 값을 변경하여 저장한다.
        final JournalDayEntity toModify = journalDayRepository.findById(key).orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.to-modify")));
        toModify.setJournalDt(DateUtils.asDate("2020-12-31"));
        final JournalDayEntity modified = journalDayRepository.save(toModify);

        // Then::
        assertNotNull(modified, "저장한 데이터를 조회할 수 없습니다.");
        assertNotNull(modified.getId(), "저장된 엔티티의 key 값이 없습니다.");
        // audit
        assertNotNull(modified.getUpdatedAt(), "수정일자 audit 처리가 되지 않았습니다.");
        assertNotNull(modified.getUpdatedBy(),  "수정자 audit 처리가 되지 않았습니다.");
        assertEquals(TestConstant.TEST_AUDITOR, modified.getUpdatedBy(), "수정자가 예상 값과 일치하지 않습니다.");
        // value
        assertEquals(DateUtils.asDate("2020-12-31"), modified.getJournalDt(), "값이 정상적으로 수정되지 않았습니다.");
    }

    /**
     * delete 테스트
     */
    @Test
    public void testDelete() throws Exception {
        // Given:: 삭제할 데이터를 등록한다.
        final JournalDayEntity registered = journalDayRepository.save(journalDayEntity);
        final Integer key = registered.getId();

        // When:: 삭제할 데이터를 조회해서, 삭제한다.
        final JournalDayEntity toDelete = journalDayRepository.findById(key).orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.to-delete")));
        journalDayRepository.delete(toDelete);

        final JournalDayEntity retrieved = journalDayRepository.findById(key).orElse(null);

        // Then::
        assertNull(retrieved, "삭제가 제대로 이루어지지 않았습니다.");
    }

    /**
     * 해당 날짜 중복 체크(countByJournalDt) 테스트
     */
    @Test
    public void testCountByJournalDt() throws Exception {
        // Given:: 중복 체크할 날짜로 데이터를 등록한다.
        journalDayRepository.save(journalDayEntity);

        // When:: 해당 날짜로 데이터를 조회한다.
        final Integer count = journalDayRepository.countByJournalDt(DateUtils.asDate("2000-01-01"), TestConstant.TEST_AUDITOR);

        // Then::
        assertNotNull(count, "중복 체크 메소드가 제대로 실행되지 않았습니다.");
        assertTrue(count >= 1, "해당 날짜에 대한 중복 체크가 실패하였습니다. 저장된 데이터가 1개 이상이어야 합니다.");
    }

    /**
     * 날짜로 저널 일자 조회 테스트
     */
    @Test
    public void testFindByJournalDt() throws Exception {
        // Given::
        final JournalDayEntity result = journalDayRepository.save(journalDayEntity);

        // When::
        final JournalDayEntity retrieved = journalDayRepository.findByJournalDt(DateUtils.asDate("2000-01-01"), TestConstant.TEST_AUDITOR);

        // Then::
        assertNotNull(retrieved, "메소드가 제대로 실행되지 않았습니다.");
        assertEquals(result.getId(), retrieved.getId(), "날짜를 이용한 조회에 실패했습니다.");
    }

    /**
     * journalDream subentity select 테스트
     * 1. 메인엔티티 등록, 2. 서브엔티티 등록 후 3. 메인엔티티 재조회
     */
    @Test
    public void testGetDreamList() throws Exception {
        // Given::
        final JournalDayEntity registered = journalDayRepository.save(journalDayEntity);
        final Integer journalDayId = registered.getId();

        testEntityManager.clear();

        // When::
        // 저널 꿈 regist
        final JournalDreamEntity journalDream = JournalDreamEntityTestFactory.create();
        journalDream.setJournalDayId(journalDayId);
        journalDreamRepository.save(journalDream);

        final JournalDayEntity retrieved = journalDayRepository.findById(journalDayId).orElseThrow(() -> new EntityNotFoundException("저널 일자를 찾을 수 없습니다."));
        final List<JournalChapterEntity> chapterList = retrieved.getJournalChapterList();

        // Then::
        assertNotNull(retrieved);
        assertNotNull(journalDayId);
        // journalDream
        assertNotNull(retrieved.getJournalDreamList());
    }

    /**
     * journalDiary subentity select 테스트
     * 1. 메인엔티티 등록, 2. 서브엔티티 등록 후 3. 메인엔티티 재조회
     */
    @Test
    public void testGetDiaryList() throws Exception {
        // Given::
        final JournalDayEntity registered = journalDayRepository.save(journalDayEntity);
        final Integer journalDayId = registered.getId();

        testEntityManager.clear();

        // When::
        // 저널 꿈 regist
        final JournalChapterEntity journalChapter = testEntityManager.persistFlushFind(
                JournalChapterEntity.builder().journalDayId(journalDayId).title("test_entry").idx(1).build()
        );
        final JournalDiaryEntity journalDiary = JournalDiaryEntityTestFactory.create();
        journalDiary.setJournalChapter(journalChapter);
        journalDiaryRepository.save(journalDiary);

        final JournalDayEntity retrieved = journalDayRepository.findById(journalDayId).orElseThrow(() -> new EntityNotFoundException("저널 일자를 찾을 수 없습니다."));
        final List<JournalChapterEntity> chapterList = retrieved.getJournalChapterList();

        // Then::
        assertNotNull(retrieved);
        assertNotNull(journalDayId);
        // journalDiary
        assertNotNull(chapterList);
    }
}

