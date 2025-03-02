package io.nicheblog.dreamdiary.extension.clsf.tag.handler;

import com.nimbusds.oauth2.sdk.util.MapUtils;
import io.nicheblog.dreamdiary.extension.cache.event.EhCacheEvictEvent;
import io.nicheblog.dreamdiary.extension.cache.handler.EhCacheEvictEventListner;
import io.nicheblog.dreamdiary.extension.clsf.tag.entity.ContentTagEntity;
import io.nicheblog.dreamdiary.extension.clsf.tag.entity.TagEntity;
import io.nicheblog.dreamdiary.extension.clsf.tag.event.JrnlTagCacheUpdtEvent;
import io.nicheblog.dreamdiary.extension.clsf.tag.event.JrnlTagProcEvent;
import io.nicheblog.dreamdiary.extension.clsf.tag.event.TagProcEvent;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.extension.clsf.tag.service.ContentTagService;
import io.nicheblog.dreamdiary.extension.clsf.tag.service.TagService;
import io.nicheblog.dreamdiary.global.config.AsyncConfig;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TagEventListener
 * <pre>
 *  태그 관련 이벤트 처리 핸들러.
 * </pre>
 *
 * @author nichefish
 * @see AsyncConfig
 */
@Component
@RequiredArgsConstructor
public class TagProcEventListener {

    private final TagService tagService;
    private final ContentTagService contentTagService;
    private final ApplicationEventPublisherWrapper publisher;

    private final CacheManager cacheManager;

    private final ApplicationContext context;
    private TagProcEventListener getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 태그 이벤트를 처리한다.
     *
     * @param event 처리할 이벤트 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     * @see EhCacheEvictEventListner
     */
    @EventListener
    public void handleTagProcEvent(final TagProcEvent event) throws Exception {
        // 🔥 이벤트 발생 당시의 SecurityContext 복원
        SecurityContextHolder.setContext(event.getSecurityContext());

        // 태그 객체 없이 키값만 넘어오면? 컨텐츠 삭제.
        final boolean isContentDelete = (event.getTagCmpstn() == null);
        final BaseClsfKey clsfKey = event.getClsfKey();
        if (isContentDelete) {
            // 기존 컨텐츠-태그 전부 삭제
            this.getSelf().delExistingContentTags(event);
        } else {
            // 태그 처리
            this.getSelf().procTags(event);
        }
        // 관련 캐시 클리어
        publisher.publishAsyncEvent(new EhCacheEvictEvent(this, clsfKey.getPostNo(), clsfKey.getContentType()));
        // 태그 테이블 refresh (연관관계 없는 메인 태그 삭제)
        tagService.deleteNoRefTags();
    }

    /**
     * 특정 게시물에 대해 기존 콘텐츠 태그를 모두 삭제합니다.
     *
     * @param event 태그 처리 이벤트
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Transactional
    public void delExistingContentTags(final TagProcEvent event) throws Exception {
        final BaseClsfKey clsfKey = event.getClsfKey();
        // 2. 글번호 + 태그번호를 받아와서 기존 태그 목록 조회
        final Map<String, Object> searchParamMap = new HashMap<>() {{
            put("refPostNo", clsfKey.getPostNo());
            put("refContentType", clsfKey.getContentType());
        }};
        final List<ContentTagEntity> entityList = contentTagService.getListEntity(searchParamMap);
        contentTagService.deleteAll(entityList);

        // 3. 태그 개수 캐시 업데이트 (이벤트 발행)
        if (event instanceof JrnlTagProcEvent jrnlTagProcEvent) {
            final Integer yy = jrnlTagProcEvent.getYy();
            final Integer mnth = jrnlTagProcEvent.getMnth();
            final Map<Integer, Integer> tagCntChangeMap = entityList.stream()
                    .collect(Collectors.toMap(ContentTagEntity::getRefTagNo, tagNo -> -1));
            publisher.publishEvent(new JrnlTagCacheUpdtEvent(this, clsfKey, yy, mnth, tagCntChangeMap));
        }
    }

    /**
     * 컨텐츠 태그 처리
     * 새로운 태그를 추가하고, 더 이상 필요하지 않은 태그를 삭제합니다. 태그 목록이 동일한 경우에는 처리하지 않고 리턴합니다.
     *
     * @param event 태그 처리 이벤트
     * @throws Exception 태그 처리 중 발생할 수 있는 예외
     */
    @Transactional
    public void procTags(final TagProcEvent event) throws Exception {
        final TagCmpstn tagCmpstn = event.getTagCmpstn();
        // 태그 객체가 넘어오지 않았으면? 리턴.
        if (tagCmpstn == null) return;

        // 기존 태그와 컨텐츠 태그가 동일하면 리턴
        final BaseClsfKey clsfKey = event.getClsfKey();
        final List<TagDto> existingTagList = contentTagService.getTagStrListByClsfKey(clsfKey);
        final List<TagDto> newTagList = tagCmpstn.getParsedTagList();
        final boolean isSame = newTagList.size() == existingTagList.size() && new HashSet<>(newTagList).containsAll(existingTagList);
        if (isSame) return;

        // 1, 추가해야 할 마스터 태그 처리 (메소드 분리)
        // 새로운 태그 목록에서 기존 태그 목록을 빼면 추가해야 할 태그들이 나옴
        final Set<TagDto> newTagSet = new HashSet<>(newTagList);
        existingTagList.forEach(newTagSet::remove);
        final List<TagEntity> rsList = CollectionUtils.isNotEmpty(newTagSet)
                ? tagService.addMasterTag(new ArrayList<>(newTagSet))
                : new ArrayList<>();

        // 태그 갯수 카운트 변화를 저장할 맵 선언
        final Map<Integer, Integer> tagCntChangeMap = new HashMap<>();

        // 2. 삭제해야 할 태그 삭제
        // 기존 태그 목록에서 새로운 태그 목록을 빼면 삭제해야 할 태그들이 나옴
        final Set<TagDto> obsoleteTagSet = new HashSet<>(existingTagList);
        newTagList.forEach(obsoleteTagSet::remove);
        if (CollectionUtils.isNotEmpty(obsoleteTagSet)) {
            contentTagService.delObsoleteContentTags(clsfKey, new ArrayList<>(obsoleteTagSet));

            for (TagDto tag : obsoleteTagSet) {
                tagCntChangeMap.put(tag.getTagNo(), -1);
            }
        }

        // 3. 추가해야 할 컨텐츠-태그를 처리해준다.
        if (CollectionUtils.isNotEmpty(rsList)) {
            // 태그 등록
            final List<ContentTagEntity> registeredList = contentTagService.addContentTags(clsfKey, rsList);

            for (ContentTagEntity tag : registeredList) {
                tagCntChangeMap.put(tag.getRefTagNo(), 1);
            }
        }

        // 캐시 처리
        if (MapUtils.isNotEmpty(tagCntChangeMap)) {
            // 저널 태그 처리
            if (event instanceof JrnlTagProcEvent jrnlTagProcEvent) {
                publisher.publishEvent(new JrnlTagCacheUpdtEvent(this, clsfKey, jrnlTagProcEvent.getYy(), jrnlTagProcEvent.getMnth(), tagCntChangeMap));
            }
        }
    }
}
