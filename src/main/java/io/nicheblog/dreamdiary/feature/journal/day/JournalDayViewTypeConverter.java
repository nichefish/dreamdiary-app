package io.nicheblog.dreamdiary.feature.journal.day;

import io.nicheblog.dreamdiary.feature.journal.day.type.JournalDayViewType;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * JournalDayViewTypeConverter
 *
 * @author nichefish
 */
@Component
public class JournalDayViewTypeConverter
        implements Converter<String, JournalDayViewType> {

    @Override
    public JournalDayViewType convert(final @NotNull String source) {
        return JournalDayViewType.from(source);
    }
}
