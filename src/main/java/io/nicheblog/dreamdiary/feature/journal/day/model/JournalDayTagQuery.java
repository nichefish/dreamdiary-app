package io.nicheblog.dreamdiary.feature.journal.day.model;

import org.apache.commons.lang3.StringUtils;

/**
 * JournalDayTagQuery
 * <pre>
 *  Journal day tag period query.
 * </pre>
 */
public record JournalDayTagQuery(
        Integer yy,
        Integer mnth,
        String weekStartDt
) {

    public static JournalDayTagQuery of(final Integer yy, final Integer mnth) {
        return new JournalDayTagQuery(yy, mnth, null);
    }

    public static JournalDayTagQuery weekly(final String weekStartDt) {
        return new JournalDayTagQuery(null, null, weekStartDt);
    }

    public boolean hasWeekStartDt() {
        return StringUtils.isNotBlank(weekStartDt);
    }

    public boolean hasYyMnth() {
        return yy != null && mnth != null;
    }
}
