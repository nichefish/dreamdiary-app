package io.nicheblog.dreamdiary.feature.attachable.state.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.state.StateKey;
import io.nicheblog.dreamdiary.feature.attachable.state.model.StateToggleDto;
import io.nicheblog.dreamdiary.feature.attachable.state.repository.jpa.StateRepository;
import io.nicheblog.dreamdiary.feature.attachable.state.spec.StateSpec;
import io.nicheblog.dreamdiary.feature.journal._shared.security.JournalContentOwnershipGuard;
import io.nicheblog.dreamdiary.feature.journal.day.service.helper.JournalDayResolvedGuard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * 상태 토글의 원본 콘텐츠 소유권 경계 테스트.
 *
 * @author nichefish
 */
@ExtendWith(MockitoExtension.class)
class StateServiceTest {

    private static final Integer FIXTURE_CONTENT_ID = 201;
    private static final ContentType FIXTURE_CONTENT_TYPE = ContentType.JOURNAL_DIARY;

    @Mock
    private StateRepository repository;
    @Mock
    private StateSpec spec;
    @Mock
    private JournalContentOwnershipGuard journalContentOwnershipGuard;
    @Mock
    private JournalDayResolvedGuard journalDayResolvedGuard;
    @Mock
    private ApplicationContext context;

    private StateService service;

    @BeforeEach
    void setUp() {
        service = new StateService(
                repository,
                spec,
                List.of(),
                journalContentOwnershipGuard,
                journalDayResolvedGuard,
                context
        );
    }

    /** 원본 콘텐츠 소유권 검증 실패는 state 조회·저장·캐시와 일자 잠금 검사 전에 전파한다. */
    @Test
    void toggleRejectsUnownedContentBeforePersistence() {
        doThrow(new NotAuthorizedException("common.result.access-not-authorized"))
                .when(journalContentOwnershipGuard)
                .assertOwned(FIXTURE_CONTENT_ID, FIXTURE_CONTENT_TYPE);

        assertThrows(NotAuthorizedException.class, () -> service.toggle(request()));

        verifyNoInteractions(repository, journalDayResolvedGuard, context);
    }

    private StateToggleDto request() {
        return StateToggleDto.builder()
                .id(FIXTURE_CONTENT_ID)
                .contentType(FIXTURE_CONTENT_TYPE)
                .stateKey(StateKey.IMPRTC)
                .build();
    }
}
