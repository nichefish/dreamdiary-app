package io.nicheblog.dreamdiary.feature.jrnl.dream.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfKey;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.related.model.RelatedContentDto;
import io.nicheblog.dreamdiary.feature.clsf.related.service.RelatedContentQueryService;
import io.nicheblog.dreamdiary.feature.jrnl.dream.model.JrnlDreamDto;
import io.nicheblog.dreamdiary.feature.jrnl.dream.model.JrnlDreamSearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.dream.service.JrnlDreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MyJrnlDreamService
 * <pre>
 *  로그인 사용자 기준 저널 꿈 서비스
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MyJrnlDreamService {

    private final JrnlDreamService jrnlDreamService;
    private final RelatedContentQueryService relatedContentQueryService;

    /**
     * 목록 조회 (dto level) :: 캐시 처리
     *
     * @param searchParam 검색조건을 담고 있는 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    public List<JrnlDreamDto> getMyListDto(final JrnlDreamSearchParam searchParam) throws Exception {
        final String username = AuthUtils.requireLgnUsername();
        final List<JrnlDreamDto> listDto = jrnlDreamService.getListDtoByUser(username, searchParam);
        this.mergeRelatedContents(username, listDto);
        return listDto;
    }

    /**
     * 특정 연도의중요 꿈 목록 조회 :: 캐시 처리
     *
     * @param searchParam JrnlDreamSearchParam
     * @return {@link List} -- 해당 연도의중요 목록
     */
    public List<JrnlDreamDto> getMySumryDreamList(final JrnlDreamSearchParam searchParam) throws Exception {
        final String username = AuthUtils.requireLgnUsername();
        final List<JrnlDreamDto> listDto = jrnlDreamService.getSumryDreamListByUser(username, searchParam);
        this.mergeRelatedContents(username, listDto);
        return listDto;
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 일련번호
     * @return {@link JrnlDreamDto} -- 조회된 객체
     */
    public JrnlDreamDto getMyDtlDtoWithCache(final Integer key) throws Exception {
        final String username = AuthUtils.requireLgnUsername();
        final JrnlDreamDto retrieved = jrnlDreamService.getDtlDtoWithCacheByUser(username, key);
        this.mergeRelatedContents(username, retrieved == null ? List.of() : List.of(retrieved));
        return retrieved;
    }

    private void mergeRelatedContents(final String username, final List<JrnlDreamDto> listDto) throws Exception {
        if (listDto == null || listDto.isEmpty()) return;

        final List<BaseClsfKey> refKeyList = new ArrayList<>();
        listDto.stream()
                .filter(dto -> dto != null && dto.getPostNo() != null)
                .forEach(dto -> refKeyList.add(new BaseClsfKey(dto.getPostNo(), ContentType.JRNL_DREAM)));

        final Map<String, List<RelatedContentDto>> relatedMap = relatedContentQueryService.getRelatedContentMapByRefs(refKeyList, username);
        for (final JrnlDreamDto jrnlDream : listDto) {
            if (jrnlDream == null || jrnlDream.getPostNo() == null) continue;
            jrnlDream.setRelatedContentList(
                    relatedMap.getOrDefault(String.format("%s:%d", ContentType.JRNL_DREAM.key, jrnlDream.getPostNo()), List.of())
            );
        }
    }
}
