package io.nicheblog.dreamdiary.domain.jrnl.sbjct.repository.jpa;

import io.nicheblog.dreamdiary.domain.jrnl.sbjct.entity.JrnlSbjctEntity;
import io.nicheblog.dreamdiary.domain.jrnl.sbjct.entity.JrnlSbjctEntityTestFactory;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.config.DataSourceConfig;
import io.nicheblog.dreamdiary.global.config.TestAuditConfig;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;
import javax.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JrnlSbjctRepositoryTest
 * <pre>
 *  저널 주제 (JPA) Repository 테스트 모듈
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
class JrnlSbjctRepositoryTest {

    @Resource
    private JrnlSbjctRepository jrnlSbjctRepository;

    private JrnlSbjctEntity jrnlSbjctEntity;

    /**
     * 각 테스트 시작 전 세팅 초기화.
     * @throws Exception 발생할 수 있는 예외.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통적으로 사용할 jrnlDayEntity 초기화
        jrnlSbjctEntity = JrnlSbjctEntityTestFactory.create();
    }

    /**
     * regist 테스트
     */
    @Test
    public void testRegist() throws Exception {
        // Given::

        // When::
        final JrnlSbjctEntity registered = jrnlSbjctRepository.save(jrnlSbjctEntity);
        final Integer key = registered.getPostNo();
        final JrnlSbjctEntity retrieved = jrnlSbjctRepository.findById(key).orElseThrow(() -> new EntityNotFoundException("등록한 데이터를 찾을 수 없습니다."));

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
        JrnlSbjctEntity registered = jrnlSbjctRepository.save(jrnlSbjctEntity);
        Integer key = registered.getPostNo();

        // When::
        JrnlSbjctEntity toModify = jrnlSbjctRepository.findById(key).orElseThrow(() -> new EntityNotFoundException("수정할 데이터를 찾을 수 없습니다."));
        toModify.setCn("modified");
        JrnlSbjctEntity modified = jrnlSbjctRepository.save(toModify);

        // Then::
        assertNotNull(modified, "저장한 데이터를 조회할 수 없습니다.");
        assertNotNull(modified.getPostNo(), "저장된 엔티티의 key 값이 없습니다.");
        // audit
        assertNotNull(modified.getMdfDt(), "수정일자 audit 처리가 되지 않았습니다.");
        assertNotNull(modified.getMdfusrId(),  "수정자 audit 처리가 되지 않았습니다.");
        assertEquals(TestConstant.TEST_AUDITOR, modified.getMdfusrId(), "수정자가 예상 값과 일치하지 않습니다.");
        // value
        assertEquals("modified", modified.getCn(), "값이 정상적으로 수정되지 않았습니다.");
    }

    /**
     * delete 테스트
     */
    @Test
    public void testDelete() throws Exception {
        // Given::
        final JrnlSbjctEntity registered = jrnlSbjctRepository.save(jrnlSbjctEntity);
        final Integer key = registered.getPostNo();

        // When::
        final JrnlSbjctEntity toDelete = jrnlSbjctRepository.findById(key).orElseThrow(() -> new EntityNotFoundException("삭제할 데이터를 찾을 수 없습니다."));
        jrnlSbjctRepository.delete(toDelete);

        final JrnlSbjctEntity retrieved = jrnlSbjctRepository.findById(key).orElse(null);

        // Then::
        assertNull(retrieved, "삭제가 제대로 이루어지지 않았습니다.");
    }
}