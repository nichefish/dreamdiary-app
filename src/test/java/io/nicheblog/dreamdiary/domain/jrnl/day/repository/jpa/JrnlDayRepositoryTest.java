package io.nicheblog.dreamdiary.domain.jrnl.day.repository.jpa;

import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDayEntity;
import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDayEntityTestFactory;
import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryEntity;
import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryEntityTestFactory;
import io.nicheblog.dreamdiary.domain.jrnl.diary.repository.jpa.JrnlDiaryRepository;
import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamEntity;
import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamEntityTestFactory;
import io.nicheblog.dreamdiary.domain.jrnl.dream.repository.jpa.JrnlDreamRepository;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.config.DataSourceConfig;
import io.nicheblog.dreamdiary.global.config.TestAuditConfig;
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
 * JrnlDayRepositoryTest
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
class JrnlDayRepositoryTest {

    @Resource
    private TestEntityManager testEntityManager;

    @Resource
    private JrnlDayRepository jrnlDayRepository;
    @Resource
    private JrnlDreamRepository jrnlDreamRepository;
    @Resource
    private JrnlDiaryRepository jrnlDiaryRepository;

    private JrnlDayEntity jrnlDayEntity;

    // TODO: 경계값, 예외값 테스트하기
    // TODO: Assertion 세분화하기
    // TODO: Parameterized Test 사용
    // JUnit의 @ParameterizedTest를 사용하여 같은 동작을 여러 가지 데이터로 반복 테스트할 때 유용합니다. 예를 들어, 여러 날짜에 대해 중복 검사를 하는 테스트라면 매번 테스트 메서드를 작성하는 대신, 파라미터화된 테스트로 통합할 수 있습니다.
    // @ParameterizedTest
    // @ValueSource(strings = {"2000-01-01", "2000-01-02"})

    /**
     * 각 테스트 시작 전 세팅 초기화.
     * @throws Exception 발생할 수 있는 예외.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통적으로 사용할 jrnlDayEntity 초기화
        jrnlDayEntity = JrnlDayEntityTestFactory.createWithJrnlDt("2000-01-01");
    }

    /**
     * regist 테스트
     */
    @Test
    @DisplayName("저널 날짜가 정상적으로 등록되는지 테스트합니다.")
    public void testRegist() throws Exception {
        // Given::

        // When:: 데이터를 등록한다.
        JrnlDayEntity registered = jrnlDayRepository.save(jrnlDayEntity);
        Integer key = registered.getPostNo();
        JrnlDayEntity retrieved = jrnlDayRepository.findById(key).orElseThrow(() -> new EntityNotFoundException("등록한 데이터를 찾을 수 없습니다."));

        // Then::
        assertNotNull(retrieved, "저장한 데이터를 조회할 수 없습니다.");
        assertNotNull(retrieved.getPostNo(), "저장된 엔티티의 key 값이 없습니다.");
        // audit
        assertNotNull(retrieved.getRegDt(), "등록일자 audit 처리가 되지 않았습니다.");
        assertNotNull(retrieved.getRegstrId(),  "등록자 audit 처리가 되지 않았습니다.");
        assertEquals(TestConstant.TEST_AUDITOR, retrieved.getRegstrId(), "등록자가 예상 값과 일치하지 않습니다.");
    }

    /**
     * modify 테스트
     */
    @Test
    public void testModify() throws Exception {
        // Given:: 수정할 데이터를 등록한다.
        JrnlDayEntity registered = jrnlDayRepository.save(jrnlDayEntity);
        Integer key = registered.getPostNo();

        // When:: 등록된 데이터를 조회해서, 값을 변경하여 저장한다.
        JrnlDayEntity toModify = jrnlDayRepository.findById(key).orElseThrow(() -> new EntityNotFoundException("수정할 데이터를 찾을 수 없습니다."));
        toModify.setJrnlDt(DateUtils.asDate("2020-12-31"));
        JrnlDayEntity modified = jrnlDayRepository.save(toModify);

        // Then::
        assertNotNull(modified, "저장한 데이터를 조회할 수 없습니다.");
        assertNotNull(modified.getPostNo(), "저장된 엔티티의 key 값이 없습니다.");
        // audit
        assertNotNull(modified.getMdfDt(), "수정일자 audit 처리가 되지 않았습니다.");
        assertNotNull(modified.getMdfusrId(),  "수정자 audit 처리가 되지 않았습니다.");
        assertEquals(TestConstant.TEST_AUDITOR, modified.getMdfusrId(), "수정자가 예상 값과 일치하지 않습니다.");
        // value
        assertEquals(DateUtils.asDate("2020-12-31"), modified.getJrnlDt(), "값이 정상적으로 수정되지 않았습니다.");
    }

    /**
     * delete 테스트
     */
    @Test
    public void testDelete() throws Exception {
        // Given:: 삭제할 데이터를 등록한다.
        JrnlDayEntity registered = jrnlDayRepository.save(jrnlDayEntity);
        Integer key = registered.getPostNo();

        // When:: 삭제할 데이터를 조회해서, 삭제한다.
        JrnlDayEntity toDelete = jrnlDayRepository.findById(key).orElseThrow(() -> new EntityNotFoundException("삭제할 데이터를 찾을 수 없습니다."));
        jrnlDayRepository.delete(toDelete);

        JrnlDayEntity retrieved = jrnlDayRepository.findById(key).orElse(null);

        // Then::
        assertNull(retrieved, "삭제가 제대로 이루어지지 않았습니다.");
    }

    /**
     * 해당 날짜 중복 체크(countByJrnlDt) 테스트
     */
    @Test
    public void testCountByJrnlDt() throws Exception {
        // Given:: 중복 체크할 날짜로 데이터를 등록한다.
        jrnlDayRepository.save(jrnlDayEntity);

        // When:: 해당 날짜로 데이터를 조회한다.
        Integer count = jrnlDayRepository.countByJrnlDt(DateUtils.asDate("2000-01-01"), TestConstant.TEST_AUDITOR);

        // Then::
        assertNotNull(count, "중복 체크 메소드가 제대로 실행되지 않았습니다.");
        assertTrue(count >= 1, "해당 날짜에 대한 중복 체크가 실패하였습니다. 저장된 데이터가 1개 이상이어야 합니다.");
    }

    /**
     * 날짜로 저널 일자 조회 테스트
     */
    @Test
    public void testFindByJrnlDt() throws Exception {
        // Given::
        JrnlDayEntity result = jrnlDayRepository.save(jrnlDayEntity);

        // When::
        JrnlDayEntity retrieved = jrnlDayRepository.findByJrnlDt(DateUtils.asDate("2000-01-01"), TestConstant.TEST_AUDITOR);

        // Then::
        assertNotNull(retrieved, "메소드가 제대로 실행되지 않았습니다.");
        assertEquals(result.getPostNo(), retrieved.getPostNo(), "날짜를 이용한 조회에 실패했습니다.");
    }

    /**
     * jrnlDream subentity select 테스트
     * 1. 메인엔티티 등록, 2. 서브엔티티 등록 후 3. 메인엔티티 재조회
     */
    @Test
    public void testGetDreamList() throws Exception {
        // Given::
        JrnlDayEntity registered = jrnlDayRepository.save(jrnlDayEntity);
        Integer jrnlDayNo = registered.getPostNo();

        testEntityManager.clear();

        // When::
        // 저널 꿈 regist
        JrnlDreamEntity jrnlDream = JrnlDreamEntityTestFactory.create();
        jrnlDream.setJrnlDayNo(jrnlDayNo);
        jrnlDreamRepository.save(jrnlDream);

        JrnlDayEntity retrieved = jrnlDayRepository.findById(jrnlDayNo).orElseThrow(() -> new EntityNotFoundException("저널 일자를 찾을 수 없습니다."));
        List<JrnlDreamEntity> dreamList = retrieved.getJrnlDreamList();

        // Then::
        assertNotNull(retrieved);
        assertNotNull(jrnlDayNo);
        // jrnlDream
        assertNotNull(retrieved.getJrnlDreamList());
    }

    /**
     * jrnlDiary subentity select 테스트
     * 1. 메인엔티티 등록, 2. 서브엔티티 등록 후 3. 메인엔티티 재조회
     */
    @Test
    public void testGetDiaryList() throws Exception {
        // Given::
        JrnlDayEntity registered = jrnlDayRepository.save(jrnlDayEntity);
        Integer jrnlDayNo = registered.getPostNo();

        testEntityManager.clear();

        // When::
        // 저널 꿈 regist
        JrnlDiaryEntity jrnlDiary = JrnlDiaryEntityTestFactory.create();
        jrnlDiary.setJrnlDayNo(jrnlDayNo);
        jrnlDiaryRepository.save(jrnlDiary);

        JrnlDayEntity retrieved = jrnlDayRepository.findById(jrnlDayNo).orElseThrow(() -> new EntityNotFoundException("저널 일자를 찾을 수 없습니다."));
        List<JrnlDreamEntity> dreamList = retrieved.getJrnlDreamList();

        // Then::
        assertNotNull(retrieved);
        assertNotNull(jrnlDayNo);
        // jrnlDiary
        assertNotNull(retrieved.getJrnlDiaryList());
    }
}