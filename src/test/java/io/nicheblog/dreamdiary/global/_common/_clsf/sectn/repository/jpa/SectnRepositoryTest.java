package io.nicheblog.dreamdiary.global._common._clsf.sectn.repository.jpa;

import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global._common._clsf.sectn.entity.SectnEntity;
import io.nicheblog.dreamdiary.global._common._clsf.sectn.entity.SectnEntityTestFactory;
import io.nicheblog.dreamdiary.global.config.DataSourceConfig;
import io.nicheblog.dreamdiary.global.config.TestAuditConfig;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SectnRepositoryTest
 * <pre>
 *  단락 (JPA) Repository 테스트 모듈.
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
class SectnRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private SectnRepository sectnRepository;

    private SectnEntity sectnEntity;

    // 4. Parameterized Test 사용
    // JUnit의 @ParameterizedTest를 사용하여 같은 동작을 여러 가지 데이터로 반복 테스트할 때 유용합니다. 예를 들어, 여러 날짜에 대해 중복 검사를 하는 테스트라면 매번 테스트 메서드를 작성하는 대신, 파라미터화된 테스트로 통합할 수 있습니다.
    // @ParameterizedTest
    // @ValueSource(strings = {"2000-01-01", "2000-01-02"})

    /**
     * 각 테스트 시작 전 세팅 초기화.
     * @throws Exception 발생할 수 있는 예외.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통적으로 사용할 sectnEntity 초기화
        sectnEntity = SectnEntityTestFactory.create();
    }

    /**
     * regist 테스트
     */
    @Test
    public void testRegist() throws Exception {
        // Given::

        // When::
        final SectnEntity registered = sectnRepository.save(sectnEntity);
        final Integer key = registered.getPostNo();
        final SectnEntity retrieved = sectnRepository.findById(key).orElseThrow(() -> new EntityNotFoundException("등록한 데이터를 찾을 수 없습니다."));

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
        // Given::
        SectnEntity registered = sectnRepository.save(sectnEntity);
        Integer key = registered.getPostNo();

        // When::
        SectnEntity toModify = sectnRepository.findById(key).orElseThrow(() -> new EntityNotFoundException("수정할 데이터를 찾을 수 없습니다."));
        toModify.setCn("modified.");
        SectnEntity modified = sectnRepository.save(toModify);

        // Then::
        assertNotNull(modified, "저장한 데이터를 조회할 수 없습니다.");
        assertNotNull(modified.getPostNo(), "저장된 엔티티의 key 값이 없습니다.");
        // audit
        assertNotNull(modified.getMdfDt(), "수정일자 audit 처리가 되지 않았습니다.");
        assertNotNull(modified.getMdfusrId(),  "수정자 audit 처리가 되지 않았습니다.");
        assertEquals(TestConstant.TEST_AUDITOR, modified.getMdfusrId(), "수정자가 예상 값과 일치하지 않습니다.");
        // value
        assertEquals("modified.", modified.getCn(), "값이 정상적으로 수정되지 않았습니다.");
    }

    /**
     * delete 테스트
     */
    @Test
    public void testDelete() throws Exception {
        // Given::
        final SectnEntity registered = sectnRepository.save(sectnEntity);
        final Integer key = registered.getPostNo();

        // When::
        final SectnEntity toDelete = sectnRepository.findById(key).orElseThrow(() -> new EntityNotFoundException("삭제할 데이터를 찾을 수 없습니다."));
        sectnRepository.delete(toDelete);

        final SectnEntity retrieved = sectnRepository.findById(key).orElse(null);

        // Then::
        assertNull(retrieved, "삭제가 제대로 이루어지지 않았습니다.");
    }
}