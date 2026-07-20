package io.nicheblog.dreamdiary.feature.attachable.related.service.my;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.related.model.RelatedContentDto;
import io.nicheblog.dreamdiary.feature.attachable.related.model.RelatedContentFlowDto;
import io.nicheblog.dreamdiary.feature.attachable.related.model.RelatedContentPostDto;
import io.nicheblog.dreamdiary.feature.attachable.related.service.RelatedContentFlowService;
import io.nicheblog.dreamdiary.feature.attachable.related.service.RelatedContentService;
import io.nicheblog.dreamdiary.feature.attachable.related.type.RelationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MyRelatedContentService
 * <pre>
 *  로그인 사용자 기준 관련글 서비스.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
public class MyRelatedContentService {

    private final RelatedContentService relatedContentService;
    private final RelatedContentFlowService relatedContentFlowService;

    public List<RelatedContentDto> getMyListDto(final ContentType contentType, final Integer id) throws Exception {
        return relatedContentService.getListDtoByRef(id, contentType);
    }

    public RelatedContentDto saveMyRelation(final RelatedContentPostDto postDto) throws Exception {
        return relatedContentService.saveManualRelation(
                postDto.getSrcId(),
                ContentType.get(postDto.getSrcContentType()),
                postDto.getTargetId(),
                ContentType.get(postDto.getTargetContentType()),
                RelationType.from(postDto.getRelationType()),
                postDto.getReason()
        );
    }

    /**
     * 로그인 사용자의 FLOW 연결 컴포넌트를 조회한다.
     *
     * @param contentType 앵커 콘텐츠 타입
     * @param id 앵커 엔트리 ID
     * @return FLOW 조회 결과
     * @throws Exception 조회 중 예외
     */
    public RelatedContentFlowDto getMyFlowDto(final ContentType contentType, final Integer id) throws Exception {
        return relatedContentFlowService.getFlow(contentType, id);
    }

    public boolean deleteMyRelation(final Integer relatedContentId) {
        return relatedContentService.delete(relatedContentId);
    }
}
