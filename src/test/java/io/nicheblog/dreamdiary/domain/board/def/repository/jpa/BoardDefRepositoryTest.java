package io.nicheblog.dreamdiary.domain.board.def.repository.jpa;

import io.nicheblog.dreamdiary.domain.board.def.entity.BoardDefEntity;
import io.nicheblog.dreamdiary.domain.board.def.entity.BoardDefEntityTestFactory;
import io.nicheblog.dreamdiary.global.TestConstant;
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
 * BoardDefRepositoryTest
 * <pre>
 *  게시판 정의 (JPA) Repository 테스트 모듈.
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
class BoardDefRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private BoardDefRepository boardDefRepository;

    private BoardDefEntity boardDefEntity;

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
        // 공통적으로 사용할 boardDefEntity 초기화
        boardDefEntity = BoardDefEntityTestFactory.create("CMPY_LIFE");
    }

    /**
     * regist 테스트
     */
    @Test
    public void testRegist() throws Exception {
        // Given::

        // When::
        BoardDefEntity registered = boardDefRepository.save(boardDefEntity);
        String boardDef = registered.getBoardDef();
        BoardDefEntity retrieved = boardDefRepository.findById(boardDef).orElseThrow(() -> new EntityNotFoundException("등록한 데이터를 찾을 수 없습니다."));

        // Then::
        assertNotNull(retrieved, "저장한 데이터를 조회할 수 없습니다.");
        assertNotNull(retrieved.getBoardDef(), "저장된 엔티티의 key 값이 없습니다.");
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
        BoardDefEntity registered = boardDefRepository.save(boardDefEntity);
        String boardDef = registered.getBoardDef();

        // When::
        BoardDefEntity toModify = boardDefRepository.findById(boardDef).orElseThrow(() -> new EntityNotFoundException("수정할 데이터를 찾을 수 없습니다."));
        toModify.setDc("modified");
        BoardDefEntity modified = boardDefRepository.save(toModify);

        // Then::
        assertNotNull(modified, "저장한 데이터를 조회할 수 없습니다.");
        assertNotNull(modified.getBoardDef(), "저장된 엔티티의 key 값이 없습니다.");
        // audit
        assertNotNull(modified.getMdfDt(), "수정일자 audit 처리가 되지 않았습니다.");
        assertNotNull(modified.getMdfusrId(),  "수정자 audit 처리가 되지 않았습니다.");
        assertEquals(TestConstant.TEST_AUDITOR, modified.getMdfusrId(), "수정자가 예상 값과 일치하지 않습니다.");
        // value
        assertEquals("modified", modified.getDc(), "값이 정상적으로 수정되지 않았습니다.");
    }

    /**
     * delete 테스트
     */
    @Test
    public void testDelete() throws Exception {
        // Given::
        BoardDefEntity registered = boardDefRepository.save(boardDefEntity);
        String boardDef = registered.getBoardDef();

        // When::
        BoardDefEntity toDelete = boardDefRepository.findById(boardDef).orElseThrow(() -> new EntityNotFoundException("삭제할 데이터를 찾을 수 없습니다."));
        boardDefRepository.delete(toDelete);

        BoardDefEntity retrieved = boardDefRepository.findById(boardDef).orElse(null);

        // Then::
        assertNull(retrieved, "삭제가 제대로 이루어지지 않았습니다.");
    }
}