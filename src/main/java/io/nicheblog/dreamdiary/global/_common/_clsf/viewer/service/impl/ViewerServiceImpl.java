package io.nicheblog.dreamdiary.global._common._clsf.viewer.service.impl;

import io.nicheblog.dreamdiary.auth.util.AuthUtils;
import io.nicheblog.dreamdiary.global._common._clsf.viewer.entity.ViewerEntity;
import io.nicheblog.dreamdiary.global._common._clsf.viewer.repository.jpa.ViewerRepository;
import io.nicheblog.dreamdiary.global._common._clsf.viewer.service.ViewerService;
import io.nicheblog.dreamdiary.global._common._clsf.viewer.spec.ViewerSpec;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * ViewerService
 * <pre>
 *  컨텐츠 열람자 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("ViewerService")
@RequiredArgsConstructor
public class ViewerServiceImpl
        implements ViewerService {

    private final ViewerRepository viewerRepository;
    private final ViewerSpec viewerSpec;

    /**
     * 열람자 존재 여부(기 방문 여부)를 체크합니다.
     *
     * @param refKey 글 번호와 컨텐츠 타입을 포함하는 참조 복합키 객체
     * @return 이미 방문한 경우 true, 그렇지 않은 경우 false
     */
    @Transactional(readOnly = true)
    public ViewerEntity getViewerByHasVisitedChk(final BaseClsfKey refKey) {
        Map<String, Object> searchParamMap = new HashedMap<>() {{
            put("regstrId", AuthUtils.getLgnUserId());
            put("refPostNo", refKey.getPostNo());
            put("refContentType", refKey.getContentType());
        }};
        List<ViewerEntity> viewerList = viewerRepository.findAll(viewerSpec.searchWith(searchParamMap));
        return CollectionUtils.isNotEmpty(viewerList) ? viewerList.get(0) : new ViewerEntity(refKey);
    }

    /**
     * 열람자를 등록합니다.
     * 
     * @param refKey 글 번호와 컨텐츠 타입을 포함하는 참조 복합키 객체
     */
    @Transactional
    public void addViewer(final BaseClsfKey refKey) {
        ViewerEntity viewer = this.getViewerByHasVisitedChk(refKey);
        viewerRepository.save(viewer);
    }
}
