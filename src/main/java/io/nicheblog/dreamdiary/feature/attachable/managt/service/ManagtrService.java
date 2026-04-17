package io.nicheblog.dreamdiary.feature.attachable.managt.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import io.nicheblog.dreamdiary.feature.attachable.managt.entity.ManagtrEntity;
import io.nicheblog.dreamdiary.feature.attachable.managt.repository.jpa.ManagtrRepository;
import io.nicheblog.dreamdiary.feature.attachable.managt.spec.ManagtrSpec;
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
@Service
@RequiredArgsConstructor
@Log4j2
public class ManagtrService {

    private final ManagtrRepository managtrRepository;
    private final ManagtrSpec managtrSpec;

    /**
     * 게시물 조치자 존재여부 (기 방문여부) 체크.
     *
     * @param refKey 글 번호와 컨텐츠 타입을 포함하는 참조 복합키 객체
     * @return {@link Boolean} -- 사용자가 이미 방문했으면 true, 그렇지 않으면 false
     */
    @Transactional(readOnly = true)
    public Boolean hasAlreadyVisited(final BaseAttachableKey refKey) {
        final Map<String, Object> searchParamMap = new HashedMap<>() {{
            put("createdBy", AuthUtils.getLoginUsername());
            put("refId", refKey.getId());
            put("refContentType", refKey.getContentType());
        }};
        final List<ManagtrEntity> managtrList = managtrRepository.findAll(managtrSpec.searchWith(searchParamMap));
        return CollectionUtils.isNotEmpty(managtrList);
    }

    /**
     * 게시물 조치자 등록.
     *
     * @param refKey 글 번호와 컨텐츠 타입을 포함하는 참조 복합키 객체
     */
    @Transactional
    public void addManagtr(final BaseAttachableKey refKey) {
        if (this.hasAlreadyVisited(refKey)) return;

        ManagtrEntity managtr = new ManagtrEntity(refKey);
        managtrRepository.save(managtr);
    }
}

