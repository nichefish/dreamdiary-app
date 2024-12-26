package io.nicheblog.dreamdiary.global._common._clsf.managt.service.impl;

import io.nicheblog.dreamdiary.auth.util.AuthUtils;
import io.nicheblog.dreamdiary.global._common._clsf.managt.entity.ManagtrEntity;
import io.nicheblog.dreamdiary.global._common._clsf.managt.repository.jpa.ManagtrRepository;
import io.nicheblog.dreamdiary.global._common._clsf.managt.service.ManagtrService;
import io.nicheblog.dreamdiary.global._common._clsf.managt.spec.ManagtrSpec;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * ManagtrService
 * <pre>
 *  조치자 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("managtrService")
@RequiredArgsConstructor
@Log4j2
public class ManagtrServiceImpl
        implements ManagtrService {

    private final ManagtrRepository managtrRepository;
    private final ManagtrSpec managtrSpec;

    /**
     * 게시물 조치자 존재여부 (기 방문여부) 체크.
     *
     * @param refKey 글 번호와 컨텐츠 타입을 포함하는 참조 복합키 객체
     * @return {@link Boolean} -- 사용자가 이미 방문했으면 true, 그렇지 않으면 false
     */
    @Override
    @Transactional(readOnly = true)
    public Boolean hasAlreadyVisited(final BaseClsfKey refKey) {
        Map<String, Object> searchParamMap = new HashedMap<>() {{
            put("regstrId", AuthUtils.getLgnUserId());
            put("refPostNo", refKey.getPostNo());
            put("refContentType", refKey.getContentType());
        }};
        List<ManagtrEntity> managtrList = managtrRepository.findAll(managtrSpec.searchWith(searchParamMap));
        return CollectionUtils.isNotEmpty(managtrList);
    }

    /**
     * 게시물 조치자 등록.
     *
     * @param refKey 글 번호와 컨텐츠 타입을 포함하는 참조 복합키 객체
     */
    @Override
    @Transactional
    public void addManagtr(final BaseClsfKey refKey) {
        if (this.hasAlreadyVisited(refKey)) return;
        
        ManagtrEntity managtr = new ManagtrEntity(refKey);
        managtrRepository.save(managtr);
    }
}
