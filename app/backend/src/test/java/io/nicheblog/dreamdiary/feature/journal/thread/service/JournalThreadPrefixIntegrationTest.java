package io.nicheblog.dreamdiary.feature.journal.thread.service;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixContentEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.model.PrefixDto;
import io.nicheblog.dreamdiary.feature.attachable.prefix.repository.jpa.PrefixContentRepository;
import io.nicheblog.dreamdiary.feature.attachable.prefix.service.PrefixService;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadDto;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadDtoTestFactory;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntity;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntityTestFactory;
import io.nicheblog.dreamdiary.feature.user.account.repository.jpa.UserRepository;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 저널 스레드와 개인 Prefix 연결의 실제 영속·소유권 계약을 검증한다.
 *
 * @author nichefish
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestAuditConfig.class)
@Transactional
@WithMockUser(username = TestConstant.TEST_AUDITOR)
class JournalThreadPrefixIntegrationTest {

    private static final String FIXTURE_PREFIX_NAME = "Integration Thread Prefix";
    private static final String FIXTURE_REPLACEMENT_PREFIX_NAME = "Integration Replacement Thread Prefix";
    private static final String FIXTURE_OTHER_USERNAME = "prefix_other_user";
    private static final String FIXTURE_OTHER_PREFIX_NAME = "Integration Other User Thread Prefix";

    @Resource
    private JournalThreadService journalThreadService;
    @Resource
    private PrefixService prefixService;
    @Resource
    private PrefixContentRepository prefixContentRepository;
    @Resource
    private UserRepository userRepository;
    @PersistenceContext
    private EntityManager entityManager;

    /** 테스트마다 개인 Prefix Scope가 참조할 가상 계정을 준비한다. */
    @BeforeEach
    void setUp() throws Exception {
        ensureUser(TestConstant.TEST_AUDITOR, "thread-prefix-integration@example.test");
    }

    /** 스레드 등록은 JOURNAL_THREAD 참조에 선택한 개인 Prefix 연결을 저장한다. */
    @Test
    void registPersistsPersonalPrefixConnection() throws Exception {
        final PrefixDto prefix = createPrefix(FIXTURE_PREFIX_NAME);

        final JournalThreadDto registered = registThread(prefix.getId());
        entityManager.flush();
        entityManager.clear();
        final PrefixContentEntity connection = requireConnection(registered.getId());

        assertEquals(prefix.getId(), connection.getPrefixId());
        assertEquals(prefix.getId(), registered.getPrefixId());
        assertNotNull(registered.getPrefix());
        assertEquals(prefix.getId(), registered.getPrefix().getId());
    }

    /** 스레드 수정은 기존 연결을 같은 개인 Scope의 다른 활성 Prefix로 교체한다. */
    @Test
    void modifyReplacesExistingPrefixConnection() throws Exception {
        final PrefixDto originalPrefix = createPrefix(FIXTURE_PREFIX_NAME);
        final PrefixDto replacementPrefix = createPrefix(FIXTURE_REPLACEMENT_PREFIX_NAME);
        final JournalThreadDto registered = registThread(originalPrefix.getId());

        final JournalThreadDto modified = modifyThread(registered.getId(), replacementPrefix.getId());
        entityManager.flush();
        entityManager.clear();
        final PrefixContentEntity connection = requireConnection(registered.getId());

        assertEquals(replacementPrefix.getId(), connection.getPrefixId());
        assertEquals(replacementPrefix.getId(), modified.getPrefixId());
        assertEquals(replacementPrefix.getId(), modified.getPrefix().getId());
    }

    /** 스레드 수정에서 Prefix 선택을 비우면 기존 prefix_content 연결을 삭제한다. */
    @Test
    void modifyClearsExistingPrefixConnection() throws Exception {
        final PrefixDto prefix = createPrefix(FIXTURE_PREFIX_NAME);
        final JournalThreadDto registered = registThread(prefix.getId());

        final JournalThreadDto modified = modifyThread(registered.getId(), null);
        entityManager.flush();
        entityManager.clear();

        assertFalse(prefixContentRepository.findByRefIdAndRefContentType(
                registered.getId(), ContentType.JOURNAL_THREAD.key).isPresent());
        assertNull(modified.getPrefixId());
        assertNull(modified.getPrefix());
    }

    /** 기존 스레드의 동일한 비활성 Prefix는 다른 필드 수정에서도 유지할 수 있다. */
    @Test
    void modifyKeepsSameInactiveHistoricalPrefix() throws Exception {
        final PrefixDto prefix = createPrefix(FIXTURE_PREFIX_NAME);
        final JournalThreadDto registered = registThread(prefix.getId());
        prefixService.setActive(ContentType.JOURNAL_THREAD.key, prefix.getId(), false);

        final JournalThreadDto modified = modifyThread(registered.getId(), prefix.getId());
        entityManager.flush();
        entityManager.clear();
        final PrefixContentEntity connection = requireConnection(registered.getId());

        assertEquals(prefix.getId(), connection.getPrefixId());
        assertEquals(prefix.getId(), modified.getPrefixId());
        assertEquals("N", modified.getPrefix().getActiveYn());
    }

    /** 스레드 수정에서도 다른 비활성 Prefix를 새로 선택할 수 없다. */
    @Test
    void modifyRejectsDifferentInactivePrefix() throws Exception {
        final PrefixDto originalPrefix = createPrefix(FIXTURE_PREFIX_NAME);
        final PrefixDto inactivePrefix = createPrefix(FIXTURE_REPLACEMENT_PREFIX_NAME);
        final JournalThreadDto registered = registThread(originalPrefix.getId());
        prefixService.setActive(ContentType.JOURNAL_THREAD.key, inactivePrefix.getId(), false);

        assertThrows(IllegalStateException.class,
                () -> modifyThread(registered.getId(), inactivePrefix.getId()));
    }

    /** 다른 사용자의 JOURNAL_THREAD Prefix는 현재 사용자의 스레드에 선택할 수 없다. */
    @Test
    void registRejectsPrefixFromDifferentPersonalScope() throws Exception {
        createPrefix(FIXTURE_PREFIX_NAME);
        ensureUser(FIXTURE_OTHER_USERNAME, "thread-prefix-other@example.test");
        final PrefixDto otherPrefix = createPrefixAs(FIXTURE_OTHER_USERNAME, FIXTURE_OTHER_PREFIX_NAME);

        assertThrows(NotAuthorizedException.class, () -> registThread(otherPrefix.getId()));
    }

    private JournalThreadDto registThread(final Integer prefixId) throws Exception {
        final JournalThreadDto request = JournalThreadDtoTestFactory.create();
        request.setContent("Integration thread content");
        request.setPrefixId(prefixId);
        final ServiceResponse response = journalThreadService.regist(request);
        return (JournalThreadDto) response.getRsltObj();
    }

    private JournalThreadDto modifyThread(final Integer threadId, final Integer prefixId) throws Exception {
        final JournalThreadDto request = JournalThreadDtoTestFactory.createWithKey(threadId);
        request.setContent("Modified integration thread content");
        request.setPrefixId(prefixId);
        final ServiceResponse response = journalThreadService.modify(request);
        return (JournalThreadDto) response.getRsltObj();
    }

    private PrefixDto createPrefix(final String name) {
        return prefixService.create(ContentType.JOURNAL_THREAD.key, PrefixDto.builder()
                .name(name)
                .color("#6B7280")
                .sortOrder(1)
                .build());
    }

    /** 지정 가상 사용자로 개인 Prefix를 생성하고 원래 인증을 복원한다. */
    private PrefixDto createPrefixAs(final String username, final String name) {
        final Authentication original = SecurityContextHolder.getContext().getAuthentication();
        final UserDetails otherUser = User.withUsername(username)
                .password("fixture-password")
                .roles("USER")
                .build();
        try {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(otherUser, null, otherUser.getAuthorities()));
            return createPrefix(name);
        } finally {
            SecurityContextHolder.getContext().setAuthentication(original);
        }
    }

    private void ensureUser(final String username, final String email) throws Exception {
        if (userRepository.findByUsername(username).isPresent()) return;
        final UserEntity user = UserEntityTestFactory.create();
        user.setUsername(username);
        user.setEmail(email);
        userRepository.saveAndFlush(user);
    }

    private PrefixContentEntity requireConnection(final Integer threadId) {
        final PrefixContentEntity connection = prefixContentRepository
                .findByRefIdAndRefContentType(threadId, ContentType.JOURNAL_THREAD.key)
                .orElse(null);
        assertNotNull(connection);
        return connection;
    }
}
