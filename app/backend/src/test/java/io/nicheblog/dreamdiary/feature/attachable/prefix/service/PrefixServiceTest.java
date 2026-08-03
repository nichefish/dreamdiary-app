package io.nicheblog.dreamdiary.feature.attachable.prefix.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixScopeEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.model.PrefixDto;
import io.nicheblog.dreamdiary.feature.attachable.prefix.repository.jpa.PrefixRepository;
import io.nicheblog.dreamdiary.feature.attachable.prefix.repository.jpa.PrefixScopeRepository;
import io.nicheblog.dreamdiary.feature.attachable.prefix.type.PrefixScopeType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Prefix 관리 서비스의 소유권·중복·활성 상태 계약 테스트.
 *
 * @author nichefish
 */
@ExtendWith(MockitoExtension.class)
class PrefixServiceTest {

    private static final String FIXTURE_OWNER = "prefix-owner";
    private static final String FIXTURE_CONTENT_TYPE = "JOURNAL_THREAD";
    private static final String FIXTURE_NAME = "가상 말머리";
    private static final Integer FIXTURE_SCOPE_ID = 20;
    private static final Integer FIXTURE_OTHER_SCOPE_ID = 30;

    @Mock
    private PrefixRepository repository;
    @Mock
    private PrefixScopeRepository scopeRepository;

    private MockedStatic<AuthUtils> authUtils;
    private PrefixService service;

    @BeforeEach
    void setUp() {
        authUtils = mockStatic(AuthUtils.class);
        authUtils.when(AuthUtils::requireLoginUsername).thenReturn(FIXTURE_OWNER);
        lenient().when(scopeRepository.findPersonalScope(FIXTURE_OWNER, FIXTURE_CONTENT_TYPE))
                .thenReturn(Optional.of(scope(FIXTURE_SCOPE_ID)));
        service = new PrefixService(repository, scopeRepository);
    }

    @AfterEach
    void tearDown() {
        authUtils.close();
    }

    @Test
    void createNormalizesColorAndPersistsFlatPrefix() {
        when(repository.existsByScopeIdAndNameIgnoreCase(FIXTURE_SCOPE_ID, FIXTURE_NAME)).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> {
            final PrefixEntity entity = invocation.getArgument(0);
            entity.setId(10);
            return entity;
        });

        final PrefixDto result = service.create(FIXTURE_CONTENT_TYPE, PrefixDto.builder()
                .name(FIXTURE_NAME)
                .color("#a1b2c3")
                .sortOrder(1)
                .build());

        assertEquals(10, result.getId());
        assertEquals("#A1B2C3", result.getColor());
    }

    @Test
    void createRejectsDuplicateOwnerName() {
        when(repository.existsByScopeIdAndNameIgnoreCase(FIXTURE_SCOPE_ID, FIXTURE_NAME)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.create(FIXTURE_CONTENT_TYPE, PrefixDto.builder()
                .name(FIXTURE_NAME)
                .sortOrder(0)
                .build()));
        verify(repository, never()).save(any());
    }

    @Test
    void setActiveRejectsDifferentScope() {
        when(repository.findById(10)).thenReturn(Optional.of(prefix(FIXTURE_OTHER_SCOPE_ID, "Y")));

        assertThrows(NotAuthorizedException.class, () -> service.setActive(FIXTURE_CONTENT_TYPE, 10, false));
        verify(repository, never()).save(any());
    }

    @Test
    void requireSelectableRejectsInactivePrefix() {
        when(repository.findById(10)).thenReturn(Optional.of(prefix(FIXTURE_SCOPE_ID, "N")));

        assertThrows(IllegalStateException.class, () -> service.requireSelectable(FIXTURE_CONTENT_TYPE, 10));
    }

    @Test
    void requireSelectableKeepsSameInactiveHistoricalPrefix() {
        when(repository.findById(10)).thenReturn(Optional.of(prefix(FIXTURE_SCOPE_ID, "N")));

        assertEquals(10, service.requireSelectable(FIXTURE_CONTENT_TYPE, 10, 10).getId());
    }

    @Test
    void getMineReturnsEmptyWhenPersonalScopeMissing() {
        when(scopeRepository.findPersonalScope(FIXTURE_OWNER, FIXTURE_CONTENT_TYPE)).thenReturn(Optional.empty());

        assertTrue(service.getMine(FIXTURE_CONTENT_TYPE).isEmpty());
        verify(repository, never()).findAllByScopeIdOrderBySortOrderAscIdAsc(anyInt());
    }

    @Test
    void getActiveByScopeUsesScopeBoundaryAndActiveState() {
        when(repository.findAllByScopeIdAndActiveYnOrderBySortOrderAscIdAsc(FIXTURE_SCOPE_ID, "Y"))
                .thenReturn(List.of(prefix(FIXTURE_SCOPE_ID, "Y")));

        final List<PrefixDto> result = service.getActiveByScope(FIXTURE_SCOPE_ID);

        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getId());
    }

    @Test
    void getActiveGlobalReturnsEmptyBeforeFirstPrefix() {
        when(scopeRepository.findByScopeTypeAndContentType(PrefixScopeType.GLOBAL, "FIXTURE_BOARD"))
                .thenReturn(Optional.empty());

        assertTrue(service.getActiveGlobal("FIXTURE_BOARD").isEmpty());
        verify(repository, never()).findAllByScopeIdAndActiveYnOrderBySortOrderAscIdAsc(anyInt(), anyString());
    }

    @Test
    void createGlobalLazilyCreatesTypedScope() {
        when(scopeRepository.findByScopeTypeAndContentType(PrefixScopeType.GLOBAL, "FIXTURE_BOARD"))
                .thenReturn(Optional.empty());
        when(scopeRepository.save(any())).thenAnswer(invocation -> {
            final PrefixScopeEntity scope = invocation.getArgument(0);
            scope.setId(41);
            return scope;
        });
        when(repository.existsByScopeIdAndNameIgnoreCase(41, FIXTURE_NAME)).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> {
            final PrefixEntity entity = invocation.getArgument(0);
            entity.setId(12);
            return entity;
        });

        final PrefixDto result = service.createGlobal(
                "FIXTURE_BOARD",
                PrefixDto.builder().name(FIXTURE_NAME).sortOrder(0).build(),
                "board-admin:1:FIXTURE_BOARD"
        );

        assertEquals(12, result.getId());
        final ArgumentCaptor<PrefixScopeEntity> captor = ArgumentCaptor.forClass(PrefixScopeEntity.class);
        verify(scopeRepository).save(captor.capture());
        assertEquals(PrefixScopeType.GLOBAL, captor.getValue().getScopeType());
        assertNull(captor.getValue().getUserId());
        assertEquals("FIXTURE_BOARD", captor.getValue().getContentType());
    }

    @Test
    void createInScopeUsesExplicitScopeWithoutPersonalLookup() {
        when(repository.existsByScopeIdAndNameIgnoreCase(FIXTURE_SCOPE_ID, FIXTURE_NAME)).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> {
            final PrefixEntity entity = invocation.getArgument(0);
            entity.setId(11);
            return entity;
        });

        final PrefixDto result = service.createInScope(
                scope(FIXTURE_SCOPE_ID),
                PrefixDto.builder().name(FIXTURE_NAME).sortOrder(0).build(),
                "board-admin:1:FIXTURE"
        );

        assertEquals(11, result.getId());
        verify(scopeRepository, never()).findPersonalScope(anyString(), anyString());
    }

    @Test
    void updateInScopeRejectsPrefixFromDifferentScope() {
        when(repository.findById(10)).thenReturn(Optional.of(prefix(FIXTURE_OTHER_SCOPE_ID, "Y")));

        assertThrows(NotAuthorizedException.class, () -> service.updateInScope(
                10,
                FIXTURE_SCOPE_ID,
                PrefixDto.builder().name(FIXTURE_NAME).sortOrder(0).build(),
                "board-admin:1:FIXTURE"
        ));
        verify(repository, never()).save(any());
    }

    @Test
    void setActiveInScopeRejectsMissingScopeId() {
        assertThrows(IllegalStateException.class, () ->
                service.setActiveInScope(10, null, false, "board-admin:1:FIXTURE"));
        verify(repository, never()).findById(anyInt());
    }

    @Test
    void requireSelectableInScopeRejectsDifferentScopeWithoutPersonalLookup() {
        when(repository.findById(10)).thenReturn(Optional.of(prefix(FIXTURE_OTHER_SCOPE_ID, "Y")));

        assertThrows(NotAuthorizedException.class, () ->
                service.requireSelectableInScope(10, FIXTURE_SCOPE_ID, null, "board:FIXTURE"));
    }

    private PrefixEntity prefix(final Integer scopeId, final String activeYn) {
        return PrefixEntity.builder()
                .id(10)
                .scope(scope(scopeId))
                .name(FIXTURE_NAME)
                .sortOrder(0)
                .activeYn(activeYn)
                .build();
    }

    private PrefixScopeEntity scope(final Integer id) {
        return PrefixScopeEntity.builder()
                .id(id)
                .scopeType(PrefixScopeType.PERSONAL)
                .build();
    }
}
