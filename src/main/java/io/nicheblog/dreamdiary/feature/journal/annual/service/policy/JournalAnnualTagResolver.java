package io.nicheblog.dreamdiary.feature.journal.annual.service.policy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.journal.annual.type.JournalAnnualTagType;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayTagQuery;
import io.nicheblog.dreamdiary.feature.journal.day.service.my.MyJournalDayTagService;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryTagQuery;
import io.nicheblog.dreamdiary.feature.journal.entry.service.my.JournalEntryMyTagService;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class JournalAnnualTagResolver {

    private static final int MAX_ANNUAL_TAG_SIZE = 99;

    private final MyJournalDayTagService myJournalDayTagService;
    private final JournalEntryMyTagService journalEntryMyTagService;
    private final Map<JournalAnnualTagType, AnnualTagFetcher> fetchers;

    public JournalAnnualTagResolver(
            final MyJournalDayTagService myJournalDayTagService,
            final JournalEntryMyTagService journalEntryMyTagService
    ) {
        this.myJournalDayTagService = myJournalDayTagService;
        this.journalEntryMyTagService = journalEntryMyTagService;
        this.fetchers = new EnumMap<>(JournalAnnualTagType.class);
        this.fetchers.put(JournalAnnualTagType.DAY, yy -> myJournalDayTagService.getMySizedTagList(JournalDayTagQuery.of(yy, MAX_ANNUAL_TAG_SIZE)));
        this.fetchers.put(JournalAnnualTagType.DIARY, yy -> journalEntryMyTagService.getMySizedTagList(
                JournalEntryTagQuery.of(ContentType.JOURNAL_DIARY, yy, MAX_ANNUAL_TAG_SIZE)
        ));
        this.fetchers.put(JournalAnnualTagType.DREAM, yy -> journalEntryMyTagService.getMySizedTagList(
                JournalEntryTagQuery.of(ContentType.JOURNAL_DREAM, yy, MAX_ANNUAL_TAG_SIZE)
        ));
    }

    /**
     * 연간 태그 타입에 맞는 태그 목록을 조회한다.
     *
     * @param yy 연도
     * @param type 연간 태그 타입
     * @return 태그 목록
     * @throws Exception 조회 중 예외
     */
    public List<TagDto> resolveTagList(final Integer yy, final JournalAnnualTagType type) throws Exception {
        final AnnualTagFetcher fetcher = fetchers.get(type);
        if (fetcher == null) {
            throw new IllegalArgumentException("unsupported annual tag type: " + type);
        }
        return fetcher.fetch(yy);
    }

    @FunctionalInterface
    private interface AnnualTagFetcher {
        List<TagDto> fetch(Integer yy) throws Exception;
    }
}
