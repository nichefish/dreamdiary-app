package io.nicheblog.dreamdiary.feature.calendar.schedule.entity;

import io.nicheblog.dreamdiary.feature.calendar.schedule.entity.SchedulePrtcpntEntity;
import org.junit.jupiter.api.Test;

import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ScheduleParticipantMappingTest {

    @Test
    void participantEntityMapsToRenamedTable() {
        final Table table = SchedulePrtcpntEntity.class.getAnnotation(Table.class);
        assertNotNull(table);
        assertEquals("schedule_participant", table.name());
    }

    @Test
    void participantJoinColumnUsesScheduleId() throws NoSuchFieldException {
        final Field scheduleField = SchedulePrtcpntEntity.class.getDeclaredField("schedule");
        final JoinColumn joinColumn = scheduleField.getAnnotation(JoinColumn.class);
        assertNotNull(joinColumn);
        assertEquals("schedule_id", joinColumn.name());
    }
}

