package io.nicheblog.dreamdiary.global;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UrlScheduleNamingTest {

    @Test
    void scheduleAppUrlUsesRenamedPath() {
        assertEquals("/app/schedule/cal.do", Url.SCHEDULE_CAL);
        assertTrue(Url.SCHEDULE_CAL.contains("/schedule/"));
    }

    @Test
    void scheduleApiUrlsUseRenamedPath() {
        assertTrue(Url.SCHEDULE_CAL_LIST_AJAX.startsWith("/api/schedule/"));
        assertTrue(Url.SCHEDULE_REG_AJAX.startsWith("/api/schedule/"));
        assertTrue(Url.SCHEDULE_DTL_AJAX.startsWith("/api/schedule/"));
        assertTrue(Url.SCHEDULE_MDF_AJAX.startsWith("/api/schedule/"));
        assertTrue(Url.SCHEDULE_DEL_AJAX.startsWith("/api/schedule/"));
    }

    @Test
    void legacyScheduleAliasesStillPointToScheduleUrls() {
        assertEquals(Url.SCHEDULE_CAL, Url.SCHEDULE_CAL);
        assertEquals(Url.SCHEDULE_CAL_LIST_AJAX, Url.SCHEDULE_CAL_LIST_AJAX);
        assertEquals(Url.SCHEDULE_REG_AJAX, Url.SCHEDULE_REG_AJAX);
        assertEquals(Url.SCHEDULE_DTL_AJAX, Url.SCHEDULE_DTL_AJAX);
        assertEquals(Url.SCHEDULE_MDF_AJAX, Url.SCHEDULE_MDF_AJAX);
        assertEquals(Url.SCHEDULE_DEL_AJAX, Url.SCHEDULE_DEL_AJAX);
    }
}
