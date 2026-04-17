package io.nicheblog.dreamdiary.infrastructure.code.repository.jpa;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.config.DataSourceConfig;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.code.entity.CodeItemEntity;
import io.nicheblog.dreamdiary.infrastructure.code.entity.CodeItemEntityTestFactory;
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
class CodeItemRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private CodeItemRepository codeItemRepository;

    private CodeItemEntity entity;

    @BeforeEach
    void setUp() throws Exception {
        entity = CodeItemEntityTestFactory.create();
    }

    @Test
    void testRegist() throws Exception {
        final CodeItemEntity registered = codeItemRepository.save(entity);
        final Integer id = registered.getId();
        final CodeItemEntity retrieved = codeItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MessageUtils.getMessage("exception.EntityNotFoundException.registered")));

        assertNotNull(retrieved);
        assertNotNull(retrieved.getClCd());
        assertNotNull(retrieved.getCreatedAt());
        assertNotNull(retrieved.getCreatedBy());
        assertEquals(TestConstant.TEST_AUDITOR, retrieved.getCreatedBy());
    }
}
