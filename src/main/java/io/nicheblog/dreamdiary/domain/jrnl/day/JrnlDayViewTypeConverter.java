package io.nicheblog.dreamdiary.domain.jrnl.day;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * JrnlDayViewTypeConverter
 *
 * @author nichefish
 */
@Component
public class JrnlDayViewTypeConverter
        implements Converter<String, JrnlDayViewType> {

    @Override
    public JrnlDayViewType convert(final @NotNull String source) {
        return JrnlDayViewType.from(source);
    }
}
