package io.nicheblog.dreamdiary.extension.clsf.comment.service;

import io.nicheblog.dreamdiary.extension.cache.event.CommentCacheEvictEvent;
import io.nicheblog.dreamdiary.extension.clsf.comment.entity.CommentEntity;
import io.nicheblog.dreamdiary.extension.clsf.comment.mapstruct.CommentMapstruct;
import io.nicheblog.dreamdiary.extension.clsf.comment.model.CommentDto;
import io.nicheblog.dreamdiary.extension.clsf.comment.repository.jpa.CommentRepository;
import io.nicheblog.dreamdiary.extension.clsf.comment.spec.CommentSpec;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseMultiCrudService;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * CommentService
 * <pre>
 *  댓글 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("commentService")
@RequiredArgsConstructor
public class CommentService
        implements BaseMultiCrudService<CommentDto, CommentDto, Integer, CommentEntity> {

    @Getter
    private final CommentRepository repository;
    @Getter
    private final CommentSpec spec;
    @Getter
    private final CommentMapstruct mapstruct = CommentMapstruct.INSTANCE;

    public CommentMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public CommentMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final ApplicationEventPublisherWrapper publisher;

    private final ApplicationContext context;
    private CommentService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * default: 항목 페이징 목록 조회 (dto level)
     *
     * @param searchParam 검색 조건 파라미터
     * @param pageable 페이징 정보
     * @return {@link Page} -- 페이징 처리된 목록 (dto level)
     */
    @Transactional(readOnly = true)
    public Page<CommentDto> getPageDto(final BaseSearchParam searchParam, final Pageable pageable) throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(searchParam);

        return this.getPageDto(searchParamMap, pageable);
    }

    /**
     * default: 항목 페이징 목록 조회 (dto level)
     *
     * @param searchParamMap 검색 조건 파라미터 맵
     * @param pageable 페이징 정보
     * @return {@link Page} -- 페이징 처리된 목록 (dto level)
     */
    @Transactional(readOnly = true)
    public Page<CommentDto> getPageDto(final Map<String, Object> searchParamMap, final Pageable pageable) throws Exception {
        // searchParamMap에서 빈 값들 및 쓸모없는 값들 정리
        final Map<String, Object> filteredSearchKey = CmmUtils.Param.filterParamMap(searchParamMap);

        Page<CommentEntity> entityList = this.getSelf().getPageEntity(searchParamMap, pageable);
        return mapstruct.toDtoPage(entityList);
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final CommentDto updatedDto) throws Exception {
        publisher.publishCustomEvent(new CommentCacheEvictEvent(this, updatedDto.getRefPostNo(), updatedDto.getRefContentType()));
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final CommentDto postDto, final CommentDto updatedDto) throws Exception {
        publisher.publishCustomEvent(new CommentCacheEvictEvent(this, updatedDto.getRefPostNo(), updatedDto.getRefContentType()));
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void postDelete(final CommentDto deletedDto) throws Exception {
        publisher.publishCustomEvent(new CommentCacheEvictEvent(this, deletedDto.getRefPostNo(), deletedDto.getRefContentType()));
    }
}