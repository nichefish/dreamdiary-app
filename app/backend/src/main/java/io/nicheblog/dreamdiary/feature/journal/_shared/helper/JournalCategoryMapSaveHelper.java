package io.nicheblog.dreamdiary.feature.journal._shared.helper;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.day.service.my.MyJournalDayMetaService;
import io.nicheblog.dreamdiary.feature.journal.day.service.my.MyJournalDayTagService;
import io.nicheblog.dreamdiary.feature.journal.entry.service.my.JournalEntryMyTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 저널 태그/메타 저장 직후 클라이언트 categoryMap 동기화용 맵을 조립한다.
 * (EhCache evict 이후 DB 기준 최신 map — 추가 GET 없이 save 응답에 실음)
 */
@Component
@RequiredArgsConstructor
public class JournalCategoryMapSaveHelper {

    public static final String KEY_DAY_TAG_CATEGORY_MAP = "dayTagCategoryMap";
    public static final String KEY_DAY_META_CATEGORY_MAP = "dayMetaCategoryMap";
    public static final String KEY_ENTRY_TAG_CATEGORY_MAP = "entryTagCategoryMap";

    private final MyJournalDayTagService myJournalDayTagService;
    private final MyJournalDayMetaService myJournalDayMetaService;
    private final JournalEntryMyTagService journalEntryMyTagService;

    /**
     * 일자 저장 성공 응답용: 일자 태그·메타 categoryMap.
     */
    public Map<String, Object> buildDaySaveCategoryMaps() throws Exception {
        final Map<String, Object> maps = new HashMap<>();
        maps.put(KEY_DAY_TAG_CATEGORY_MAP, myJournalDayTagService.getMyTagCategoryMap());
        maps.put(KEY_DAY_META_CATEGORY_MAP, myJournalDayMetaService.getMyMetaCategoryMap());
        return maps;
    }

    /**
     * 엔트리 저장 성공 응답용: DIARY/DREAM 태그 categoryMap (태그 없는 NOTE 등은 비움).
     */
    public Map<String, Object> buildEntrySaveCategoryMaps(final ContentType contentType) throws Exception {
        final Map<String, Object> maps = new HashMap<>();
        if (ContentType.JOURNAL_DIARY.equals(contentType) || ContentType.JOURNAL_DREAM.equals(contentType)) {
            maps.put(KEY_ENTRY_TAG_CATEGORY_MAP, journalEntryMyTagService.getMyTagCategoryMap(contentType));
        }
        return maps;
    }
}
