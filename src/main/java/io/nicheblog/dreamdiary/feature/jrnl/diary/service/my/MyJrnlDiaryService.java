package io.nicheblog.dreamdiary.feature.jrnl.diary.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfKey;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.related.model.RelatedContentDto;
import io.nicheblog.dreamdiary.feature.clsf.related.service.RelatedContentQueryService;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiarySearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.diary.service.JrnlDiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MyJrnlDiaryService
 * <pre>
 *  로그인 사용자 기준 저널 일기 서비스
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MyJrnlDiaryService {

    private final JrnlDiaryService jrnlDiaryService;
    private final RelatedContentQueryService relatedContentQueryService;

    /**
     * 목록 조회 (dto level) :: 캐시 처리
     *
     * @param searchParam 검색조건을 담고 있는 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    public List<JrnlDiaryDto> getMyListDto(final JrnlDiarySearchParam searchParam) throws Exception {
        final String username = AuthUtils.requireLgnUsername();
        final List<JrnlDiaryDto> listDto = jrnlDiaryService.getListDtoByUser(username, searchParam);
        this.mergeRelatedContents(username, listDto);
        return listDto;
    }

    /**
     * 특정 연도의중요 일기 목록 조회 :: 캐시 처리
     *
     * @param searchParam JrnlDiarySearchParam
     * @return {@link List} -- 해당 연도의중요 목록
     */
    public List<JrnlDiaryDto> getMySumryDiaryList(final JrnlDiarySearchParam searchParam) throws Exception {
        final String username = AuthUtils.requireLgnUsername();
        final List<JrnlDiaryDto> listDto = jrnlDiaryService.getSumryDiaryListByUser(username, searchParam);
        this.mergeRelatedContents(username, listDto);
        return listDto;
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 일련번호
     * @return {@link JrnlDiaryDto} -- 조회된 객체
     */
    public JrnlDiaryDto getMyDtlDtoWithCache(final Integer key) throws Exception {
        final String username = AuthUtils.requireLgnUsername();
        final JrnlDiaryDto retrieved = jrnlDiaryService.getDtlDtoWithCacheByUser(username, key);
        this.mergeRelatedContents(username, retrieved == null ? List.of() : List.of(retrieved));
        return retrieved;
    }

    private void mergeRelatedContents(final String username, final List<JrnlDiaryDto> listDto) throws Exception {
        if (listDto == null || listDto.isEmpty()) return;

        final List<BaseClsfKey> refKeyList = new ArrayList<>();
        listDto.stream()
                .filter(dto -> dto != null && dto.getPostNo() != null)
                .forEach(dto -> refKeyList.add(new BaseClsfKey(dto.getPostNo(), ContentType.JRNL_DIARY)));

        final Map<String, List<RelatedContentDto>> relatedMap = relatedContentQueryService.getRelatedContentMapByRefs(refKeyList, username);
        for (final JrnlDiaryDto jrnlDiary : listDto) {
            if (jrnlDiary == null || jrnlDiary.getPostNo() == null) continue;
            jrnlDiary.setRelatedContentList(
                    relatedMap.getOrDefault(String.format("%s:%d", ContentType.JRNL_DIARY.key, jrnlDiary.getPostNo()), List.of())
            );
        }
    }

}
