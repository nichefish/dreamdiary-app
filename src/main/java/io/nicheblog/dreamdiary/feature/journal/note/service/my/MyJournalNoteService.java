package io.nicheblog.dreamdiary.feature.journal.note.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.related.model.RelatedContentDto;
import io.nicheblog.dreamdiary.feature.attachable.related.service.RelatedContentQueryService;
import io.nicheblog.dreamdiary.feature.journal.note.model.JournalNoteDto;
import io.nicheblog.dreamdiary.feature.journal.note.model.JournalNoteSearchParam;
import io.nicheblog.dreamdiary.feature.journal.note.service.JournalNoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MyJournalNoteService
 * <pre>
 *  로그인 사용자 기준 저널 노트 서비스
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MyJournalNoteService {

    private final JournalNoteService journalNoteService;
    private final RelatedContentQueryService relatedContentQueryService;

    /**
     * 목록 조회 (dto level) :: 캐시 처리
     *
     * @param searchParam 검색조건을 담고 있는 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    public List<JournalNoteDto> getMyListDto(final JournalNoteSearchParam searchParam) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        final List<JournalNoteDto> listDto = journalNoteService.getListDtoByUser(username, searchParam);
        this.mergeRelatedContents(username, listDto);
        return listDto;
    }

    /**
     * 특정 연도의 노트 목록 조회 :: 캐시 처리
     *
     * @param searchParam JournalNoteSearchParam
     * @return {@link List} -- 해당 연도의 노트 목록
     */
    public List<JournalNoteDto> getMyAnnualNoteList(final JournalNoteSearchParam searchParam) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        final List<JournalNoteDto> listDto = journalNoteService.getAnnualNoteListByUser(username, searchParam);
        this.mergeRelatedContents(username, listDto);
        return listDto;
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 일련번호
     * @return {@link JournalNoteDto} -- 조회된 객체
     */
    public JournalNoteDto getMyDtlDtoWithCache(final Integer key) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        final JournalNoteDto retrieved = journalNoteService.getDtlDtoWithCacheByUser(username, key);
        this.mergeRelatedContents(username, retrieved == null ? List.of() : List.of(retrieved));
        return retrieved;
    }

    private void mergeRelatedContents(final String username, final List<JournalNoteDto> listDto) throws Exception {
        if (listDto == null || listDto.isEmpty()) return;

        final List<BaseAttachableKey> refKeyList = new ArrayList<>();
        listDto.stream()
                .filter(dto -> dto != null && dto.getId() != null)
                .forEach(dto -> refKeyList.add(new BaseAttachableKey(dto.getId(), ContentType.JOURNAL_NOTE)));

        final Map<String, List<RelatedContentDto>> relatedMap = relatedContentQueryService.getRelatedContentMapByRefs(refKeyList, username);
        for (final JournalNoteDto journalNote : listDto) {
            if (journalNote == null || journalNote.getId() == null) continue;
            journalNote.setRelatedContentList(
                    relatedMap.getOrDefault(String.format("%s:%d", ContentType.JOURNAL_NOTE.key, journalNote.getId()), List.of())
            );
        }
    }

}

