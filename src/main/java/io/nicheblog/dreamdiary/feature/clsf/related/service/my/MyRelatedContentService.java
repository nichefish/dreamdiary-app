package io.nicheblog.dreamdiary.feature.clsf.related.service.my;

import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.related.model.RelatedContentDto;
import io.nicheblog.dreamdiary.feature.clsf.related.model.RelatedContentPostDto;
import io.nicheblog.dreamdiary.feature.clsf.related.service.RelatedContentService;
import io.nicheblog.dreamdiary.feature.clsf.related.type.RelationType;
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

    public List<RelatedContentDto> getMyListDto(final ContentType contentType, final Integer postNo) throws Exception {
        return relatedContentService.getListDtoByRef(postNo, contentType);
    }

    public RelatedContentDto saveMyRelation(final RelatedContentPostDto postDto) throws Exception {
        return relatedContentService.saveManualRelation(
                postDto.getSrcPostNo(),
                ContentType.get(postDto.getSrcContentType()),
                postDto.getTargetPostNo(),
                ContentType.get(postDto.getTargetContentType()),
                RelationType.from(postDto.getRelationType()),
                postDto.getReason()
        );
    }

    public boolean deleteMyRelation(final Integer relatedContentNo) {
        return relatedContentService.delete(relatedContentNo);
    }
}
