package io.nicheblog.dreamdiary.extension.clsf.meta.handler;

import io.nicheblog.dreamdiary.extension.cache.event.EhCacheEvictEvent;
import io.nicheblog.dreamdiary.extension.clsf.meta.entity.MetaContentEntity;
import io.nicheblog.dreamdiary.extension.clsf.meta.entity.MetaEntity;
import io.nicheblog.dreamdiary.extension.clsf.meta.event.JrnlMetaProcEvent;
import io.nicheblog.dreamdiary.extension.clsf.meta.event.MetaProcEvent;
import io.nicheblog.dreamdiary.extension.clsf.meta.model.MetaDto;
import io.nicheblog.dreamdiary.extension.clsf.meta.model.cmpstn.MetaCmpstn;
import io.nicheblog.dreamdiary.extension.clsf.meta.service.MetaContentService;
import io.nicheblog.dreamdiary.extension.clsf.meta.service.MetaService;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * MetaProcWorker
 * <pre>
 *  메타 처리 Worker
 * </pre>
 *
 * @author nichefish
 * @see MetaProcEventListener
 **/
@Component
@RequiredArgsConstructor
@Log4j2
public class MetaProcWorker {

    private final MetaService metaService;
    private final MetaContentService metaContentService;
    private final ApplicationEventPublisherWrapper publisher;

    /**
     * 메타 처리
     *
     * @param event MetaProcEvent
     */
    @Transactional
    public void handle(final MetaProcEvent event) throws Exception {
        // 이벤트로부터 securityContext를 가져온다.
        SecurityContextHolder.setContext(event.getSecurityContext());

        // 메타 객체 없이 키값만 넘어오면? 컨텐츠 삭제.
        final boolean isContentDelete = (event.getMetaCmpstn() == null);
        final BaseClsfKey clsfKey = event.getClsfKey();
        if (isContentDelete) {
            // 기존 메타-컨텐츠 전부 삭제
            delExistingMetaContents(event);
        } else {
            // 메타 처리
            procMetas(event);
        }
        // 관련 캐시 클리어 (저널 캐시는 따로 처리)
        if (!(event instanceof JrnlMetaProcEvent)) {
            publisher.publishEvent(new EhCacheEvictEvent(this, clsfKey.getPostNo(), clsfKey.getContentType()));
        }

        // 메타 테이블 refresh (연관관계 없는 메인 메타 삭제)
        metaService.deleteNoRefMetas();
    }

    /**
     * 특정 게시물에 대해 기존 콘텐츠 메타를 모두 삭제합니다.
     *
     * @param event 메타 처리 이벤트
     */
    @Transactional
    public void delExistingMetaContents(final MetaProcEvent event) throws Exception {
        final BaseClsfKey clsfKey = event.getClsfKey();
        // 2. 글번호 + 메타번호를 받아와서 기존 메타 목록 조회
        final Map<String, Object> searchParamMap = new HashMap<>() {{
            put("refPostNo", clsfKey.getPostNo());
            put("refContentType", clsfKey.getContentType());
        }};
        final List<MetaContentEntity> entityList = metaContentService.getListEntity(searchParamMap);
        metaContentService.deleteAll(entityList);
    }

    /**
     * 메타-컨텐츠 처리
     * 새로운 메타를 추가하고, 더 이상 필요하지 않은 메타를 삭제합니다. 메타 목록이 동일한 경우에는 처리하지 않고 리턴합니다.
     *
     * @param event 메타 처리 이벤트
     */
    @Transactional
    public void procMetas(final MetaProcEvent event) throws Exception {
        final MetaCmpstn metaCmpstn = event.getMetaCmpstn();
        // 메타 객체가 넘어오지 않았으면? 리턴.
        if (metaCmpstn == null) return;

        // 기존 메타와 메타-컨텐츠가 동일하면 리턴
        final BaseClsfKey clsfKey = event.getClsfKey();
        final List<MetaDto> existingMetaList = metaContentService.getMetaStrListByClsfKey(clsfKey);
        final List<MetaDto> newMetaList = metaCmpstn.getParsedMetaList();
        final boolean isSame = newMetaList.size() == existingMetaList.size() && new HashSet<>(newMetaList).containsAll(existingMetaList);
        if (isSame) return;

        // 1, 추가해야 할 마스터 메타 처리 (메소드 분리)
        // 새로운 메타 목록에서 기존 메타 목록을 빼면 추가해야 할 메타들이 나옴
        final Set<MetaDto> newMetaSet = new HashSet<>(newMetaList);
        existingMetaList.forEach(newMetaSet::remove);
        final List<MetaEntity> createdMetaList = CollectionUtils.isNotEmpty(newMetaSet)
                ? metaService.addMasterMeta(new ArrayList<>(newMetaSet))
                : new ArrayList<>();

        // 2. 삭제해야 할 메타 삭제
        // 기존 메타 목록에서 새로운 메타 목록을 빼면 삭제해야 할 메타들이 나옴
        final Set<MetaDto> obsoleteMetaSet = new HashSet<>(existingMetaList);
        newMetaList.forEach(obsoleteMetaSet::remove);
        if (CollectionUtils.isNotEmpty(obsoleteMetaSet)) {
            metaContentService.delObsoleteMetaContents(clsfKey, new ArrayList<>(obsoleteMetaSet));
        }

        // 3. 추가해야 할 메타-컨텐츠를 처리해준다.
        if (CollectionUtils.isNotEmpty(createdMetaList)) {
            // 메타 등록
            metaContentService.addMetaContents(clsfKey, createdMetaList);
        }
    }
}
