package io.nicheblog.dreamdiary.feature.journal.entry.service.helper;

import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDreamSectionDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 꿈 가상 섹션 조립·순회.
 * <p>화면 제목은 요청 locale의 메시지 카탈로그에서 조립한다.</p>
 */
@UtilityClass
public class JournalDreamSectionHelper {

    private static final String OWN_SECTION_KEY = "own";
    private static final String OWN_SECTION_TITLE_KEY = "common.dream";
    private static final String NAMED_SECTION_TITLE_KEY = "journal.dream.section.named";
    private static final Collator KOREAN_COLLATOR = Collator.getInstance(Locale.KOREAN);

    /**
     * 내 꿈·타인 꿈 목록을 화면 섹션 목록으로 묶는다.
     *
     * @param ownDreamEntries 꿈꾼 이름 없는 엔트리
     * @param elseDreamEntries 꿈꾼 이름 있는 엔트리
     * @return 섹션 목록, 비어 있으면 null
     */
    public static List<JournalDreamSectionDto> buildSections(
            final List<JournalEntryDto> ownDreamEntries,
            final List<JournalEntryDto> elseDreamEntries
    ) {
        final List<JournalDreamSectionDto> sections = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ownDreamEntries)) {
            sections.add(JournalDreamSectionDto.builder()
                    .sectionKey(OWN_SECTION_KEY)
                    .title(MessageUtils.getMessage(OWN_SECTION_TITLE_KEY, null))
                    .dreamerName(null)
                    .entries(new ArrayList<>(ownDreamEntries))
                    .build());
        }

        final Map<String, List<JournalEntryDto>> byDreamerName = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(elseDreamEntries)) {
            for (final JournalEntryDto entry : elseDreamEntries) {
                if (entry == null) continue;
                final String dreamerName = JournalDreamerFieldHelper.normalizeDreamerName(entry.getElseDreamerNm());
                if (StringUtils.isBlank(dreamerName)) continue;
                byDreamerName.computeIfAbsent(dreamerName, key -> new ArrayList<>()).add(entry);
            }
        }

        byDreamerName.keySet().stream()
                .sorted(KOREAN_COLLATOR)
                .forEach(dreamerName -> sections.add(JournalDreamSectionDto.builder()
                        .sectionKey("dreamer:" + dreamerName)
                        .title(MessageUtils.getMessage(NAMED_SECTION_TITLE_KEY, new Object[]{dreamerName}))
                        .dreamerName(dreamerName)
                        .entries(byDreamerName.get(dreamerName))
                        .build()));

        return CollectionUtils.isEmpty(sections) ? null : sections;
    }

    /**
     * 일자 DTO 의 모든 꿈 엔트리를 순회한다.
     *
     * @param day 일자 DTO
     * @param consumer 엔트리 소비자
     */
    public static void forEachDreamEntry(final JournalDayDto day, final Consumer<JournalEntryDto> consumer) {
        if (day == null || consumer == null || CollectionUtils.isEmpty(day.getJournalDreamSectionList())) {
            return;
        }
        for (final JournalDreamSectionDto section : day.getJournalDreamSectionList()) {
            if (section == null || CollectionUtils.isEmpty(section.getEntries())) continue;
            for (final JournalEntryDto entry : section.getEntries()) {
                if (entry != null) consumer.accept(entry);
            }
        }
    }

    /**
     * 내 꿈 섹션 엔트리만 반환한다 (캘린더 등).
     *
     * @param day 일자 DTO
     * @return 내 꿈 엔트리, 없으면 null
     */
    public static List<JournalEntryDto> getOwnDreamEntries(final JournalDayDto day) {
        if (day == null || CollectionUtils.isEmpty(day.getJournalDreamSectionList())) {
            return null;
        }
        for (final JournalDreamSectionDto section : day.getJournalDreamSectionList()) {
            if (section == null || !OWN_SECTION_KEY.equals(section.getSectionKey())) continue;
            return CollectionUtils.isEmpty(section.getEntries()) ? null : section.getEntries();
        }
        return null;
    }

    /**
     * 꿈 섹션 보유 여부.
     *
     * @param day 일자 DTO
     * @return 섹션이 있으면 true
     */
    public static boolean hasDreamSections(final JournalDayDto day) {
        return day != null && CollectionUtils.isNotEmpty(day.getJournalDreamSectionList());
    }
}
