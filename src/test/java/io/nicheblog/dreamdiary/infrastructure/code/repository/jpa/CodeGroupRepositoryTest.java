package io.nicheblog.dreamdiary.infrastructure.code.repository.jpa;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.config.DataSourceConfig;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.code.entity.CodeGroupEntity;
import io.nicheblog.dreamdiary.infrastructure.code.entity.CodeGroupEntityTestFactory;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(DataSourceConfig.class)
@Import(TestAuditConfig.class)
@Log4j2
class CodeGroupRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private CodeGroupRepository codeGroupRepository;

    private CodeGroupEntity entity;

    @BeforeEach
    void setUp() throws Exception {
        entity = CodeGroupEntityTestFactory.create();
    }

    @Test
    void testRegist() throws Exception {
        final CodeGroupEntity registered = codeGroupRepository.save(entity);
        final Integer id = registered.getId();
        final CodeGroupEntity retrieved = codeGroupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.registered")));

        assertNotNull(retrieved);
        assertNotNull(retrieved.getClCd());
        assertNotNull(retrieved.getCreatedAt());
        assertNotNull(retrieved.getCreatedBy());
        assertEquals(TestConstant.TEST_AUDITOR, retrieved.getCreatedBy());
    }
}
