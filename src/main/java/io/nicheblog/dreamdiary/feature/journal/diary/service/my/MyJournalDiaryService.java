package io.nicheblog.dreamdiary.feature.journal.diary.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.related.model.RelatedContentDto;
import io.nicheblog.dreamdiary.feature.attachable.related.service.RelatedContentQueryService;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryDto;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiarySearchParam;
import io.nicheblog.dreamdiary.feature.journal.diary.service.JournalDiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MyJournalDiaryService
 * <pre>
 *  로그인 사용자 기준 저널 일기 서비스
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MyJournalDiaryService {

    private final JournalDiaryService journalDiaryService;
    private final RelatedContentQueryService relatedContentQueryService;

    /**
     * 목록 조회 (dto level) :: 캐시 처리
     *
     * @param searchParam 검색조건을 담고 있는 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    public List<JournalDiaryDto> getMyListDto(final JournalDiarySearchParam searchParam) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        final List<JournalDiaryDto> listDto = journalDiaryService.getListDtoByUser(username, searchParam);
        this.mergeRelatedContents(username, listDto);
        return listDto;
    }

    /**
     * 특정 연도의중요 일기 목록 조회 :: 캐시 처리
     *
     * @param searchParam JournalDiarySearchParam
     * @return {@link List} -- 해당 연도의중요 목록
     */
    public List<JournalDiaryDto> getMyAnnualDiaryList(final JournalDiarySearchParam searchParam) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        final List<JournalDiaryDto> listDto = journalDiaryService.getAnnualDiaryListByUser(username, searchParam);
        this.mergeRelatedContents(username, listDto);
        return listDto;
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 일련번호
     * @return {@link JournalDiaryDto} -- 조회된 객체
     */
    public JournalDiaryDto getMyDtlDtoWithCache(final Integer key) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        final JournalDiaryDto retrieved = journalDiaryService.getDtlDtoWithCacheByUser(username, key);
        this.mergeRelatedContents(username, retrieved == null ? List.of() : List.of(retrieved));
        return retrieved;
    }

    private void mergeRelatedContents(final String username, final List<JournalDiaryDto> listDto) throws Exception {
        if (listDto == null || listDto.isEmpty()) return;

        final List<BaseAttachableKey> refKeyList = new ArrayList<>();
        listDto.stream()
                .filter(dto -> dto != null && dto.getId() != null)
                .forEach(dto -> refKeyList.add(new BaseAttachableKey(dto.getId(), ContentType.JOURNAL_DIARY)));

        final Map<String, List<RelatedContentDto>> relatedMap = relatedContentQueryService.getRelatedContentMapByRefs(refKeyList, username);
        for (final JournalDiaryDto journalDiary : listDto) {
            if (journalDiary == null || journalDiary.getId() == null) continue;
            journalDiary.setRelatedContentList(
                    relatedMap.getOrDefault(String.format("%s:%d", ContentType.JOURNAL_DIARY.key, journalDiary.getId()), List.of())
            );
        }
    }

}

