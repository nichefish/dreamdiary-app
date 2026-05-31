package io.nicheblog.dreamdiary.feature.journal.day.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayTagQuery;
import io.nicheblog.dreamdiary.feature.journal.day.service.JournalDayTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * MyJournalDayTagService
 * <pre>
 *  로그인 사용자 기준 저널 일자 태그 서비스 모듈 (facade)
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MyJournalDayTagService {

    private final JournalDayTagService journalDayTagService;

    public List<Integer> getMyYyListByTagId(final Integer tagId) {
        return journalDayTagService.getYyListByTagIdAndUser(tagId, AuthUtils.requireLoginUsername());
    }

    public List<TagDto> getMySizedTagList(final JournalDayTagQuery query) throws Exception {
        return journalDayTagService.getSizedTagListByUser(AuthUtils.requireLoginUsername(), query);
    }

    public Map<String, List<TagDto>> getMySizedTagGroupMap(final JournalDayTagQuery query) throws Exception {
        return journalDayTagService.getSizedTagGroupMapByUser(AuthUtils.requireLoginUsername(), query);
    }

    public Map<String, List<String>> getMyTagCategoryMap() throws Exception {
        return journalDayTagService.getTagCategoryMapByUser(AuthUtils.requireLoginUsername());
    }
}
