package io.nicheblog.dreamdiary.feature.journal.setting.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.journal.setting.entity.JournalSettingEntity;
import io.nicheblog.dreamdiary.feature.journal.setting.model.JournalUserSettingDto;
import io.nicheblog.dreamdiary.feature.journal.setting.repository.JournalSettingRepository;
import io.nicheblog.dreamdiary.feature.journal.setting.type.JournalDefaultEntryView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 사용자별 저널 설정의 조회 기본값과 저장 범위 경계 테스트.
 *
 * @author nichefish
 */
@ExtendWith(MockitoExtension.class)
class JournalSettingServiceTest {

    private static final String FIXTURE_USERNAME = "alice";

    @Mock
    private JournalSettingRepository repository;

    private JournalSettingService service;

    @BeforeEach
    void setUp() {
        service = new JournalSettingService(repository);
    }

    /** 사용자 설정 행이 없으면 DB 쓰기 없이 DAILY 기본값을 반환한다. */
    @Test
    void getMySettingReturnsDailyWithoutCreatingRow() {
        when(repository.findByScopeAndScopeKey("USER", FIXTURE_USERNAME)).thenReturn(Optional.empty());

        try (final MockedStatic<AuthUtils> auth = mockStatic(AuthUtils.class)) {
            auth.when(AuthUtils::requireLoginUsername).thenReturn(FIXTURE_USERNAME);

            final JournalUserSettingDto result = service.getMySetting();

            assertThat(result.getDefaultEntryView()).isEqualTo(JournalDefaultEntryView.DAILY);
        }

        verify(repository).findByScopeAndScopeKey("USER", FIXTURE_USERNAME);
        verify(repository, never()).save(any());
    }

    /** 최초 저장은 USER/username 범위 행을 만들고 요청한 기본 화면을 보존한다. */
    @Test
    void updateMySettingCreatesUserScopedRow() {
        when(repository.findByScopeAndScopeKey("USER", FIXTURE_USERNAME)).thenReturn(Optional.empty());
        when(repository.save(any(JournalSettingEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        try (final MockedStatic<AuthUtils> auth = mockStatic(AuthUtils.class)) {
            auth.when(AuthUtils::requireLoginUsername).thenReturn(FIXTURE_USERNAME);

            final JournalUserSettingDto result = service.updateMySetting(request(JournalDefaultEntryView.WEEKLY));

            assertThat(result.getDefaultEntryView()).isEqualTo(JournalDefaultEntryView.WEEKLY);
        }

        final ArgumentCaptor<JournalSettingEntity> captor = ArgumentCaptor.forClass(JournalSettingEntity.class);
        verify(repository).save(captor.capture());
        final JournalSettingEntity saved = captor.getValue();
        assertThat(saved.getScope()).isEqualTo("USER");
        assertThat(saved.getScopeKey()).isEqualTo(FIXTURE_USERNAME);
        assertThat(saved.getDefaultEntryView()).isEqualTo(JournalDefaultEntryView.WEEKLY);
        assertThat(saved.getEmbeddingEnabled()).isTrue();
        assertThat(saved.getCreatedBy()).isEqualTo(FIXTURE_USERNAME);
        assertThat(saved.getUpdatedBy()).isEqualTo(FIXTURE_USERNAME);
    }

    /** 기존 사용자 행 저장은 같은 행을 갱신하며 범위 키를 바꾸지 않는다. */
    @Test
    void updateMySettingUpdatesExistingScopedRow() {
        final JournalSettingEntity existing = JournalSettingEntity.builder()
                .id(21)
                .scope("USER")
                .scopeKey(FIXTURE_USERNAME)
                .embeddingEnabled(true)
                .defaultEntryView(JournalDefaultEntryView.DAILY)
                .createdBy(FIXTURE_USERNAME)
                .build();
        when(repository.findByScopeAndScopeKey("USER", FIXTURE_USERNAME)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        try (final MockedStatic<AuthUtils> auth = mockStatic(AuthUtils.class)) {
            auth.when(AuthUtils::requireLoginUsername).thenReturn(FIXTURE_USERNAME);

            final JournalUserSettingDto result = service.updateMySetting(request(JournalDefaultEntryView.MONTHLY));

            assertThat(result.getDefaultEntryView()).isEqualTo(JournalDefaultEntryView.MONTHLY);
        }

        assertThat(existing.getScope()).isEqualTo("USER");
        assertThat(existing.getScopeKey()).isEqualTo(FIXTURE_USERNAME);
        assertThat(existing.getDefaultEntryView()).isEqualTo(JournalDefaultEntryView.MONTHLY);
        verify(repository).save(existing);
    }

    private JournalUserSettingDto request(final JournalDefaultEntryView view) {
        return JournalUserSettingDto.builder()
                .defaultEntryView(view)
                .build();
    }
}
