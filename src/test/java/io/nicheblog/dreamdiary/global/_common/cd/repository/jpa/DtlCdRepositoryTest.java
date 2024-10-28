package io.nicheblog.dreamdiary.global._common.cd.repository.jpa;

import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global._common.cd.entity.DtlCdEntity;
import io.nicheblog.dreamdiary.global._common.cd.entity.DtlCdEntityTestFactory;
import io.nicheblog.dreamdiary.global._common.cd.entity.DtlCdKey;
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
 * DtlCdRepositoryTest
 * <pre>
 *  상세 코드 (JPA) Repository 테스트 모듈
 *  "@Transactional 환경에서는 flush가 의도한 대로 작동하지 않을 수 있다."
 * </pre>
 *
 * @author nichefish
 */
@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(DataSourceConfig.class)
@Import(TestAuditConfig.class)
@Log4j2
class DtlCdRepositoryTest {
    
    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private DtlCdRepository dtlCdRepository;
    
    private DtlCdEntity dtlCdEntity;

    /**
     * 각 테스트 시작 전 세팅 초기화.
     * @throws Exception 발생할 수 있는 예외.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통적으로 사용할 dtlCdEntity 초기화
        dtlCdEntity = DtlCdEntityTestFactory.create();
    }

    /**
     * regist 테스트
     */
    @Test
    public void testRegist() throws Exception {
        // Given::

        // When::
        DtlCdEntity registered = dtlCdRepository.save(dtlCdEntity);
        DtlCdKey key = registered.getKey();
        DtlCdEntity retrieved = dtlCdRepository.findById(key).orElseThrow(() -> new EntityNotFoundException("등록한 데이터를 찾을 수 없습니다."));

        // Then::
        assertNotNull(retrieved, "저장한 데이터를 조회할 수 없습니다.");
        assertNotNull(retrieved.getClCd(), "저장된 엔티티의 key 값이 없습니다.");
        // audit
        assertNotNull(retrieved.getRegDt(), "등록일자 audit 처리가 되지 않았습니다.");
        assertNotNull(retrieved.getRegstrId(),  "등록자 audit 처리가 되지 않았습니다.");
        assertEquals(retrieved.getRegstrId(), TestConstant.TEST_AUDITOR, "등록자가 예상 값과 일치하지 않습니다.");
    }

    /**
     * modify 테스트
     */
    @Test
    public void testModify() throws Exception {
        // Given::
        DtlCdEntity registered = dtlCdRepository.save(dtlCdEntity);
        DtlCdKey key = registered.getKey();

        // When::
        DtlCdEntity toModify = dtlCdRepository.findById(key).orElseThrow(() -> new EntityNotFoundException("수정할 데이터를 찾을 수 없습니다."));
        toModify.setDc("modified");
        DtlCdEntity modified = dtlCdRepository.save(toModify);

        // Then::
        assertNotNull(modified, "저장한 데이터를 조회할 수 없습니다.");
        assertNotNull(modified.getDtlCd(), "저장된 엔티티의 key 값이 없습니다.");
        // audit
        assertNotNull(modified.getMdfDt(), "수정일자 audit 처리가 되지 않았습니다.");
        assertNotNull(modified.getMdfusrId(),  "수정자 audit 처리가 되지 않았습니다.");
        assertEquals(modified.getMdfusrId(), TestConstant.TEST_AUDITOR, "수정자가 예상 값과 일치하지 않습니다.");
        // value
        assertEquals("modified", modified.getDc(), "값이 정상적으로 수정되지 않았습니다.");
    }

    /**
     * delete 테스트
     */
    @Test
    public void testDelete() throws Exception {
        // Given::
        DtlCdEntity registered = dtlCdRepository.save(dtlCdEntity);
        DtlCdKey key = registered.getKey();

        // When::
        DtlCdEntity toDelete = dtlCdRepository.findById(key).orElseThrow(() -> new EntityNotFoundException("삭제할 데이터를 찾을 수 없습니다."));
        dtlCdRepository.delete(toDelete);

        DtlCdEntity retrieved = dtlCdRepository.findById(key).orElse(null);

        // Then::
        assertNull(retrieved, "삭제가 제대로 이루어지지 않았습니다.");
    }
}