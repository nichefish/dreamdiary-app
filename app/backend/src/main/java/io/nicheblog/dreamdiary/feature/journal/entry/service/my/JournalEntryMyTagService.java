package io.nicheblog.dreamdiary.feature.journal.entry.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryTagQuery;
import io.nicheblog.dreamdiary.feature.journal.entry.service.JournalEntryTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class JournalEntryMyTagService {

    private final JournalEntryTagService journalEntryTagService;

    /**
     * 로그인 사용자의 태그 목록을 조회한다.
     *
     * @param contentType 콘텐츠 타입
     * @return 태그 목록
     * @throws Exception 조회 중 예외
     */
    public List<TagDto> getMyTagList(final ContentType contentType) throws Exception {
        return journalEntryTagService.getTagListByUser(AuthUtils.requireLoginUsername(), contentType);
    }

    /**
     * 로그인 사용자의 크기 반영 태그 목록을 조회한다.
     *
     * @param query 태그 조회 조건
     * @return 크기 반영 태그 목록
     * @throws Exception 조회 중 예외
     */
    public List<TagDto> getMySizedTagList(final JournalEntryTagQuery query) throws Exception {
        return journalEntryTagService.getSizedTagListByUser(AuthUtils.requireLoginUsername(), query);
    }

    /**
     * 로그인 사용자의 크기 반영 태그 그룹 맵을 조회한다.
     *
     * @param query 태그 조회 조건
     * @return 카테고리별 태그 맵
     * @throws Exception 조회 중 예외
     */
    public Map<String, List<TagDto>> getMySizedTagGroupMap(final JournalEntryTagQuery query) throws Exception {
        return journalEntryTagService.getSizedTagGroupMapByUser(AuthUtils.requireLoginUsername(), query);
    }

    /**
     * 로그인 사용자의 태그 카테고리 맵을 조회한다.
     *
     * @param contentType 콘텐츠 타입
     * @return 태그 카테고리 맵
     * @throws Exception 조회 중 예외
     */
    public Map<String, List<String>> getMyTagCtgrMap(final ContentType contentType) throws Exception {
        return journalEntryTagService.getTagCtgrMapByUser(AuthUtils.requireLoginUsername(), contentType);
    }
}
