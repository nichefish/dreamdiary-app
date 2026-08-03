package io.nicheblog.dreamdiary.feature.attachable.prefix.service;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixScopeEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.model.PrefixDto;
import io.nicheblog.dreamdiary.feature.attachable.prefix.repository.jpa.PrefixRepository;
import io.nicheblog.dreamdiary.feature.attachable.prefix.repository.jpa.PrefixScopeRepository;
import io.nicheblog.dreamdiary.feature.attachable.prefix.type.PrefixScopeType;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntity;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntityTestFactory;
import io.nicheblog.dreamdiary.feature.user.account.repository.jpa.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 개인 Prefix 관리의 Scope 생성·격리·조회 규칙을 실제 DB에서 검증한다.
 *
 * @author nichefish
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestAuditConfig.class)
@Transactional
@WithMockUser(username = PrefixManagementIntegrationTest.FIXTURE_OWNER_USERNAME)
class PrefixManagementIntegrationTest {

    static final String FIXTURE_OWNER_USERNAME = "prefix_scope_owner";
    private static final String FIXTURE_OTHER_USERNAME = "prefix_scope_other";
    private static final String FIXTURE_THREAD_TYPE = ContentType.JOURNAL_THREAD.key;
    private static final String FIXTURE_DIARY_TYPE = ContentType.JOURNAL_DIARY.key;
    private static final String FIXTURE_FIRST_NAME = "Integration Personal Prefix";
    private static final String FIXTURE_SECOND_NAME = "Integration Secondary Prefix";

    @Resource
    private PrefixService prefixService;
    @Resource
    private PrefixRepository prefixRepository;
    @Resource
    private PrefixScopeRepository prefixScopeRepository;
    @Resource
    private UserRepository userRepository;
    @PersistenceContext
    private EntityManager entityManager;

    /** 테스트마다 전용 개인 Prefix 소유 계정을 준비한다. */
    @BeforeEach
    void setUp() throws Exception {
        ensureUser(FIXTURE_OWNER_USERNAME, "prefix-scope-owner@example.test");
    }

    /** 첫 등록은 PERSONAL Scope를 lazy 생성하고 같은 목록의 후속 등록은 그 Scope를 재사용한다. */
    @Test
    void firstCreateLazilyCreatesPersonalScopeAndNextCreateReusesIt() {
        assertTrue(prefixScopeRepository.findPersonalScope(
                FIXTURE_OWNER_USERNAME,
                FIXTURE_THREAD_TYPE
        ).isEmpty());

        final PrefixDto first = createPrefix(FIXTURE_THREAD_TYPE, FIXTURE_FIRST_NAME, 1);
        final PrefixDto second = createPrefix(FIXTURE_THREAD_TYPE, FIXTURE_SECOND_NAME, 2);
        entityManager.flush();
        entityManager.clear();

        final PrefixScopeEntity scope = prefixScopeRepository.findPersonalScope(
                FIXTURE_OWNER_USERNAME,
                FIXTURE_THREAD_TYPE
        ).orElseThrow();
        final PrefixEntity firstEntity = prefixRepository.findById(first.getId()).orElseThrow();
        final PrefixEntity secondEntity = prefixRepository.findById(second.getId()).orElseThrow();

        assertEquals(PrefixScopeType.PERSONAL, scope.getScopeType());
        assertEquals(userRepository.findByUsername(FIXTURE_OWNER_USERNAME).orElseThrow().getId(), scope.getUserId());
        assertEquals(scope.getId(), firstEntity.getScope().getId());
        assertEquals(scope.getId(), secondEntity.getScope().getId());
    }

    /** 같은 사용자라도 contentType별 Prefix 목록은 독립 Scope로 분리된다. */
    @Test
    void contentTypesUseIndependentPersonalScopes() {
        final PrefixDto threadPrefix = createPrefix(FIXTURE_THREAD_TYPE, FIXTURE_FIRST_NAME, 1);
        final PrefixDto diaryPrefix = createPrefix(FIXTURE_DIARY_TYPE, FIXTURE_FIRST_NAME, 1);
        entityManager.flush();
        entityManager.clear();

        final PrefixScopeEntity threadScope = prefixScopeRepository.findPersonalScope(
                FIXTURE_OWNER_USERNAME,
                FIXTURE_THREAD_TYPE
        ).orElseThrow();
        final PrefixScopeEntity diaryScope = prefixScopeRepository.findPersonalScope(
                FIXTURE_OWNER_USERNAME,
                FIXTURE_DIARY_TYPE
        ).orElseThrow();

        assertNotEquals(threadScope.getId(), diaryScope.getId());
        assertEquals(List.of(threadPrefix.getId()), prefixService.getMine(FIXTURE_THREAD_TYPE).stream()
                .map(PrefixDto::getId)
                .toList());
        assertEquals(List.of(diaryPrefix.getId()), prefixService.getMine(FIXTURE_DIARY_TYPE).stream()
                .map(PrefixDto::getId)
                .toList());
    }

    /** 같은 contentType과 이름을 사용해도 사용자별 Prefix 목록은 서로 노출되지 않는다. */
    @Test
    void usersUseIndependentPersonalScopes() throws Exception {
        final PrefixDto ownerPrefix = createPrefix(FIXTURE_THREAD_TYPE, FIXTURE_FIRST_NAME, 1);
        ensureUser(FIXTURE_OTHER_USERNAME, "prefix-scope-other@example.test");
        final PrefixDto otherPrefix = runAs(
                FIXTURE_OTHER_USERNAME,
                () -> createPrefix(FIXTURE_THREAD_TYPE, FIXTURE_FIRST_NAME, 1)
        );
        entityManager.flush();
        entityManager.clear();

        assertEquals(List.of(ownerPrefix.getId()), prefixService.getMine(FIXTURE_THREAD_TYPE).stream()
                .map(PrefixDto::getId)
                .toList());
        assertEquals(List.of(otherPrefix.getId()), runAs(
                FIXTURE_OTHER_USERNAME,
                () -> prefixService.getMine(FIXTURE_THREAD_TYPE).stream()
                        .map(PrefixDto::getId)
                        .toList()
        ));
    }

    /** 관리 목록은 비활성 Prefix를 정렬 유지하고 활성 선택지는 비활성 Prefix를 제외한다. */
    @Test
    void inactivePrefixRemainsInManagementListButNotActiveOptions() {
        final PrefixDto later = createPrefix(FIXTURE_THREAD_TYPE, FIXTURE_FIRST_NAME, 20);
        final PrefixDto earlier = createPrefix(FIXTURE_THREAD_TYPE, FIXTURE_SECOND_NAME, 10);
        prefixService.setActive(FIXTURE_THREAD_TYPE, later.getId(), false);
        entityManager.flush();
        entityManager.clear();

        final List<PrefixDto> all = prefixService.getMine(FIXTURE_THREAD_TYPE);
        final List<PrefixDto> active = prefixService.getActiveMine(FIXTURE_THREAD_TYPE);

        assertEquals(List.of(earlier.getId(), later.getId()), all.stream().map(PrefixDto::getId).toList());
        assertEquals("N", all.get(1).getActiveYn());
        assertEquals(List.of(earlier.getId()), active.stream().map(PrefixDto::getId).toList());
    }

    /** 같은 Scope에서는 대소문자만 다른 Prefix 이름도 중복으로 거부한다. */
    @Test
    void duplicateNameIsRejectedCaseInsensitivelyWithinScope() {
        final PrefixDto first = createPrefix(FIXTURE_THREAD_TYPE, FIXTURE_FIRST_NAME, 1);

        assertThrows(IllegalArgumentException.class, () -> createPrefix(
                FIXTURE_THREAD_TYPE,
                FIXTURE_FIRST_NAME.toLowerCase(Locale.ROOT),
                2
        ));

        final List<PrefixDto> stored = prefixService.getMine(FIXTURE_THREAD_TYPE);
        assertEquals(1, stored.size());
        assertEquals(first.getId(), stored.get(0).getId());
    }

    private PrefixDto createPrefix(final String contentType, final String name, final Integer sortOrder) {
        return prefixService.create(contentType, PrefixDto.builder()
                .name(name)
                .color("#6B7280")
                .sortOrder(sortOrder)
                .build());
    }

    /** 지정 가상 사용자 인증으로 작업하고 원래 인증을 복원한다. */
    private <T> T runAs(final String username, final Supplier<T> action) {
        final Authentication original = SecurityContextHolder.getContext().getAuthentication();
        final UserDetails principal = User.withUsername(username)
                .password("fixture-password")
                .roles("USER")
                .build();
        try {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
            return action.get();
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
}
