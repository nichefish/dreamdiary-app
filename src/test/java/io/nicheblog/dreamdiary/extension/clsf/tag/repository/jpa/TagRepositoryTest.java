package io.nicheblog.dreamdiary.extension.clsf.tag.repository.jpa;

import io.nicheblog.dreamdiary.extension.clsf.tag.entity.TagEntity;
import io.nicheblog.dreamdiary.extension.clsf.tag.entity.TagEntityTestFactory;
import io.nicheblog.dreamdiary.global.config.DataSourceConfig;
import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * TagRepositoryTest
 * <pre>
 *  태그 (JPA) Repository 테스트 모듈.
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
class TagRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private TagRepository tagRepository;

    private TagEntity tagEntity;

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
        // 공통적으로 사용할 tagEntity 초기화
        tagEntity = TagEntityTestFactory.create();
    }

    /**
     * regist 테스트
     */
    @Test
    public void testRegist() throws Exception {
        // Given::

        // When::
        final TagEntity registered = tagRepository.save(tagEntity);
        final Integer key = registered.getTagNo();
        final TagEntity retrieved = tagRepository.findById(key).orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.registered")));

        // Then::
        assertNotNull(retrieved, "저장한 데이터를 조회할 수 없습니다.");
        assertNotNull(retrieved.getTagNo(), "저장된 엔티티의 key 값이 없습니다.");
    }
}