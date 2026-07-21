package io.nicheblog.dreamdiary.feature.calendar.schedule.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.service.AuthService;
import io.nicheblog.dreamdiary.auth.security.util.AuditorUtils;
import io.nicheblog.dreamdiary.feature.calendar.schedule.model.ScheduleDto;
import io.nicheblog.dreamdiary.feature.calendar.schedule.entity.ScheduleEntity;
import io.nicheblog.dreamdiary.feature.calendar.schedule.entity.SchedulePrtcpntEntity;
import io.nicheblog.dreamdiary.feature.calendar.schedule.repository.jpa.ScheduleRepository;
import io.nicheblog.dreamdiary.feature.calendar.schedule.spec.ScheduleSpec;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import io.nicheblog.dreamdiary.infrastructure.code.service.CodeLookupService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 일정 저장 전 날짜 범위와 휴가 구분 계약 검증.
 */
@ExtendWith(MockitoExtension.class)
class ScheduleServiceValidationTest {

    private static final String FIXTURE_OWNER = "fixture_owner";
    private static final String FIXTURE_PARTICIPANT = "fixture_participant";
    private static final String FIXTURE_OUTSIDER = "fixture_outsider";
    private static final String FIXTURE_TITLE = "휴가 일정";

    @Mock
    private ScheduleRepository repository;
    @Mock
    private ScheduleSpec spec;
    @Mock
    private ApplicationEventPublisherWrapper publisher;

    @Mock
    private CodeLookupService codeLookupService;
    @Mock
    private ApplicationContext context;
    @Mock
    private AuthService authService;

    @InjectMocks
    private ScheduleService scheduleService;

    @BeforeEach
    void initializeAuditorUtils() {
        ReflectionTestUtils.setField(AuditorUtils.class, "authService", authService);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
        ReflectionTestUtils.setField(AuditorUtils.class, "authService", null);
    }

    @Test
    void vacationRequiresActiveVacationType() {
        final ScheduleDto dto = schedule(Code.SCHEDULE_VCATN, "2026-07-21", "2026-07-21");

        assertThrows(IllegalArgumentException.class, () -> scheduleService.preRegist(dto));
    }

    @Test
    void validVacationTypeAndMissingEndDateAreNormalized() throws Exception {
        final ScheduleDto dto = schedule(Code.SCHEDULE_VCATN, "2026-07-21", null);
        dto.setVcatnCd("ANNUAL");
        when(codeLookupService.getCodeName(Code.VCATN_CD, "ANNUAL")).thenReturn("연차");

        scheduleService.preRegist(dto);

        assertEquals("2026-07-21", dto.getEndDt());
        assertEquals("ANNUAL", dto.getVcatnCd());
    }

    @Test
    void endDateBeforeStartDateIsRejected() {
        final ScheduleDto dto = schedule(Code.SCHEDULE_ETC, "2026-07-21", "2026-07-20");

        assertThrows(IllegalArgumentException.class, () -> scheduleService.preRegist(dto));
    }

    @Test
    void holydayRemainsSingleDateAndClearsVacationType() throws Exception {
        final ScheduleDto dto = schedule(Code.SCHEDULE_HOLYDAY, "2026-07-21", "2026-07-23");
        dto.setVcatnCd("ANNUAL");

        scheduleService.preRegist(dto);

        assertEquals("2026-07-21", dto.getEndDt());
        assertNull(dto.getVcatnCd());
    }

    @Test
    void participantCanViewPrivateSchedule() throws Exception {
        authenticate(FIXTURE_PARTICIPANT);
        final ScheduleEntity entity = privateScheduleEntity();
        when(repository.findById(1)).thenReturn(Optional.of(entity));

        final ScheduleDto result = scheduleService.getDtlDto(1);

        assertEquals(1, result.getId());
    }

    @Test
    void unrelatedUserCannotViewPrivateSchedule() {
        authenticate(FIXTURE_OUTSIDER);
        when(repository.findById(1)).thenReturn(Optional.of(privateScheduleEntity()));

        assertThrows(NotAuthorizedException.class, () -> scheduleService.getDtlDto(1));
    }

    @Test
    void nonOwnerCannotModifySchedule() {
        authenticate(FIXTURE_OUTSIDER);
        final ScheduleDto dto = schedule(Code.SCHEDULE_ETC, "2026-07-21", "2026-07-21");
        dto.setId(1);
        when(repository.findById(1)).thenReturn(Optional.of(privateScheduleEntity()));

        assertThrows(NotAuthorizedException.class, () -> scheduleService.modify(dto));
        verify(repository, never()).save(any());
    }

    @Test
    void onlyOwnerCanDeleteSchedule() {
        final ScheduleDto dto = schedule(Code.SCHEDULE_ETC, "2026-07-21", "2026-07-21");
        dto.setCreatedBy(FIXTURE_OWNER);

        authenticate(FIXTURE_OUTSIDER);
        assertThrows(NotAuthorizedException.class, () -> scheduleService.preDelete(dto));

        authenticate(FIXTURE_OWNER);
        assertDoesNotThrow(() -> scheduleService.preDelete(dto));
    }

    private ScheduleDto schedule(final String scheduleCd, final String bgnDt, final String endDt) {
        return ScheduleDto.builder()
                .scheduleCd(scheduleCd)
                .title(FIXTURE_TITLE)
                .bgnDt(bgnDt)
                .endDt(endDt)
                .privateYn("N")
                .build();
    }

    private ScheduleEntity privateScheduleEntity() {
        return ScheduleEntity.builder()
                .id(1)
                .scheduleCd(Code.SCHEDULE_VCATN)
                .vcatnCd("ANNUAL")
                .title(FIXTURE_TITLE)
                .bgnDt(LocalDateTime.of(2026, 7, 21, 0, 0))
                .endDt(LocalDateTime.of(2026, 7, 21, 0, 0))
                .privateYn("Y")
                .createdBy(FIXTURE_OWNER)
                .prtcpntList(List.of(SchedulePrtcpntEntity.builder().username(FIXTURE_PARTICIPANT).build()))
                .build();
    }

    private void authenticate(final String username) {
        final User principal = new User(username, "fixture_password", List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities())
        );
    }
}
