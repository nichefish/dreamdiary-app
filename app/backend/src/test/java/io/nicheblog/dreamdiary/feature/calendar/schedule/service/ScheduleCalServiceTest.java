package io.nicheblog.dreamdiary.feature.calendar.schedule.service;

import io.nicheblog.dreamdiary.feature.calendar.schedule.entity.ScheduleEntity;
import io.nicheblog.dreamdiary.feature.calendar.schedule.model.ScheduleSearchParam;
import io.nicheblog.dreamdiary.feature.calendar.schedule.repository.jpa.ScheduleRepository;
import io.nicheblog.dreamdiary.feature.calendar.schedule.spec.ScheduleSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * 일정 달력 조회가 공개/개인 일정을 단일 가시성 쿼리로 수렴하는지 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class ScheduleCalServiceTest {

    @Mock
    private ScheduleSpec scheduleSpec;
    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private ScheduleCalService scheduleCalService;

    @Test
    @SuppressWarnings("unchecked")
    void totalCalendarUsesOneQueryAndPreservesVisibleFilters() throws Exception {
        final Specification<ScheduleEntity> specification = (root, query, builder) -> null;
        when(scheduleSpec.searchWith(anyMap())).thenReturn(specification);
        when(scheduleRepository.findAll(specification)).thenReturn(Collections.emptyList());
        final ScheduleSearchParam searchParam = ScheduleSearchParam.builder()
                .vcatnChked("N")
                .myPaprChked("Y")
                .prvtChked("Y")
                .build();

        scheduleCalService.getScheduleTotalCalList(searchParam);

        final ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(scheduleSpec).searchWith(captor.capture());
        verify(scheduleRepository).findAll(specification);
        verifyNoMoreInteractions(scheduleRepository);
        assertEquals("N", captor.getValue().get("vcatnChked"));
        assertEquals("Y", captor.getValue().get("myPaprChked"));
        assertEquals("Y", captor.getValue().get("prvtChked"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void missingPrivateFilterDefaultsToPublicOnly() throws Exception {
        final Specification<ScheduleEntity> specification = (root, query, builder) -> null;
        when(scheduleSpec.searchWith(anyMap())).thenReturn(specification);
        when(scheduleRepository.findAll(specification)).thenReturn(Collections.emptyList());

        scheduleCalService.getScheduleTotalCalList(ScheduleSearchParam.builder().build());

        final ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(scheduleSpec).searchWith(captor.capture());
        assertEquals("N", captor.getValue().get("prvtChked"));
    }
}
