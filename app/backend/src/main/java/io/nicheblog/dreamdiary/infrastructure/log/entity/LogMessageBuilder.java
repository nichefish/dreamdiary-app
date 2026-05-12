package io.nicheblog.dreamdiary.infrastructure.log.entity;

import io.nicheblog.dreamdiary.infrastructure.log.model.LogParam;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

public final class LogMessageBuilder {

    private LogMessageBuilder() {
    }

    public static String messageBody(final LogParam param) {
        return joinNonBlank(param.getRsltMsg(), param.getContent());
    }

    private static String joinNonBlank(final String first, final String second) {
        return Stream.of(StringUtils.trimToNull(first), StringUtils.trimToNull(second))
                .filter(StringUtils::isNotBlank)
                .reduce((a, b) -> a + "\n" + b)
                .orElse(null);
    }
}
