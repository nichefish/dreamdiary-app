package io.nicheblog.dreamdiary.feature.calendar.schedule.spec;

import io.nicheblog.dreamdiary.feature.calendar.schedule.entity.ScheduleEntity;
import io.nicheblog.dreamdiary.feature.calendar.schedule.entity.SchedulePrtcpntEntity;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 일정 필터가 실제 Criteria 조건으로 변환되는 계약을 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class ScheduleSpecTest {

    private static final String FIXTURE_USERNAME = "fixture_user";

    @Mock
    private Root<ScheduleEntity> root;
    @Mock
    private CriteriaQuery<?> query;
    @Mock
    private CriteriaBuilder builder;
    @Mock
    private Path<LocalDateTime> endDtExpression;
    @Mock
    private Path<LocalDateTime> bgnDtExpression;
    @Mock
    private Path<String> privateYnExpression;
    @Mock
    private Path<String> scheduleCdExpression;
    @Mock
    private Path<String> usernameExpression;
    @Mock
    private Join<ScheduleEntity, SchedulePrtcpntEntity> participantJoin;

    @InjectMocks
    private ScheduleSpec scheduleSpec;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void visibleFiltersBecomeParticipantAndVacationPredicates() throws Exception {
        authenticate(FIXTURE_USERNAME);
        when(root.<LocalDateTime>get("endDt")).thenReturn(endDtExpression);
        when(root.<LocalDateTime>get("bgnDt")).thenReturn(bgnDtExpression);
        when(root.<String>get("privateYn")).thenReturn(privateYnExpression);
        when(root.<String>get("scheduleCd")).thenReturn(scheduleCdExpression);
        when(root.<ScheduleEntity, SchedulePrtcpntEntity>join("prtcpntList", JoinType.LEFT))
                .thenReturn(participantJoin);
        when(root.<ScheduleEntity, SchedulePrtcpntEntity>join("prtcpntList", JoinType.INNER))
                .thenReturn(participantJoin);
        when(participantJoin.<String>get("username")).thenReturn(usernameExpression);
        final Map<String, Object> searchParams = new LinkedHashMap<>();
        searchParams.put("prvtChked", "Y");
        searchParams.put("vcatnChked", "N");
        searchParams.put("myPaprChked", "Y");

        final List<?> predicates = scheduleSpec.getPredicateWithParams(searchParams, root, query, builder);

        assertEquals(3, predicates.size());
        verify(builder).notEqual(scheduleCdExpression, Code.SCHEDULE_VCATN);
        verify(builder, times(2)).equal(usernameExpression, FIXTURE_USERNAME);
        verify(query, times(2)).distinct(true);
    }

    private void authenticate(final String username) {
        final User principal = new User(username, "fixture_password", List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities())
        );
    }
}
