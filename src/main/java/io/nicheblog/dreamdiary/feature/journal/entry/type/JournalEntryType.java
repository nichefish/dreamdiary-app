package io.nicheblog.dreamdiary.feature.journal.entry.type;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * JournalDayViewType
 *
 * @author nichefish
 */
@Getter
@RequiredArgsConstructor
public enum JournalEntryType {
    DIARY("JOURNAL_DIARY", "DIARY"),
    DREAM("JOURNAL_DREAM", "DREAM");

    private final String contentType;
    private final Set<String> aliases;

    JournalEntryType(final String contentType, final String... aliases) {
        this.contentType = contentType;
        final Set<String> aliasSet = new LinkedHashSet<>();
        aliasSet.add(contentType);
        aliasSet.add(name());
        if (aliases != null) {
            aliasSet.addAll(Arrays.asList(aliases));
        }
        this.aliases = aliasSet.stream()
                .map(v -> StringUtils.upperCase(v, Locale.ROOT))
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
    }

    /**
     * 경로 타입 문자열을 enum으로 변환한다.
     *
     * @param type 경로 타입 문자열
     * @return 엔트리 타입 enum
     */
    public static JournalEntryType from(final String type) {
        if (StringUtils.isBlank(type)) {
            throw new IllegalArgumentException("type is required.");
        }
        final String normalized = StringUtils.upperCase(type.trim(), Locale.ROOT);
        return Arrays.stream(values())
                .filter(entryType -> entryType.aliases.contains(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("invalid type: " + type));
    }

    /**
     * 엔트리 타입 enum을 ContentType enum으로 변환한다.
     *
     * @return 콘텐츠 타입 enum
     */
    public ContentType toContentType() {
        return ContentType.get(contentType);
    }
}
