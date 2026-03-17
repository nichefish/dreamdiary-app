package io.nicheblog.dreamdiary.domain.jrnl.sumry.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.domain.jrnl.sumry.entity.JrnlSumryReviewEntity;
import io.nicheblog.dreamdiary.domain.jrnl.sumry.mapstruct.JrnlSumryReviewMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.sumry.model.JrnlSumryReviewDto;
import io.nicheblog.dreamdiary.domain.jrnl.sumry.repository.jpa.JrnlSumryReviewRepository;
import io.nicheblog.dreamdiary.domain.jrnl.sumry.spec.JrnlSumryReviewSpec;
import io.nicheblog.dreamdiary.infrastructure.cache.event.JrnlCacheEvictEvent;
import io.nicheblog.dreamdiary.infrastructure.cache.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.domain.clsf.ContentType;
import io.nicheblog.dreamdiary.domain.clsf.tag.event.TagProcEvent;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseClsfService;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseMultipartWritableService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * JrnlSumryReviewService
 * <pre>
 *  저널 결산 리뷰 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("jrnlSumryReviewService")
@RequiredArgsConstructor
@Log4j2
public class JrnlSumryReviewService
        implements BaseClsfService<JrnlSumryReviewDto, JrnlSumryReviewDto, Integer, JrnlSumryReviewEntity> , BaseMultipartWritableService<JrnlSumryReviewDto, JrnlSumryReviewDto, Integer, JrnlSumryReviewEntity> {

    @Getter
    private final JrnlSumryReviewRepository repository;
    @Getter
    private final JrnlSumryReviewSpec spec;
    @Getter
    private final JrnlSumryReviewMapstruct mapstruct;

    public JrnlSumryReviewMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public JrnlSumryReviewMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final ApplicationEventPublisherWrapper publisher;

    private final String JRNL_SUMRY = ContentType.JRNL_SUMRY.key;

    private final ApplicationContext context;
    private JrnlSumryReviewService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 저널 결산 정뵤 목록 조회 :: 캐시 사용 위해 구현체로 pullUp
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link List<JrnlSumryReviewDto>} -- 검색 조건에 맞는 결산 목록 Dto 리스트
     */
    public List<JrnlSumryReviewDto> getMyListDto(final BaseSearchParam searchParam) throws Exception {
        searchParam.setRegstrId(AuthUtils.getLgnUserId());

        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final JrnlSumryReviewDto updatedDto) throws Exception {
        // 태그 처리
        publisher.publishEvent(new TagProcEvent(this, updatedDto.getClsfKey(), updatedDto.tag));
        // 관련 캐시 삭제
        publisher.publishCustomEvent(new JrnlCacheEvictEvent(this, JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_SUMRY_REVIEW));
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final JrnlSumryReviewDto postDto, final JrnlSumryReviewDto updatedDto) throws Exception {
        // 태그 처리
        publisher.publishEvent(new TagProcEvent(this, updatedDto.getClsfKey(), updatedDto.tag));
        // 관련 캐시 삭제
        publisher.publishCustomEvent(new JrnlCacheEvictEvent(this, JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_SUMRY_REVIEW));
    }
}
