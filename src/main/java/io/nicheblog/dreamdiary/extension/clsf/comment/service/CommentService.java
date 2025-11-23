package io.nicheblog.dreamdiary.extension.clsf.comment.service;

import io.nicheblog.dreamdiary.extension.cache.event.CommentCacheEvictEvent;
import io.nicheblog.dreamdiary.extension.clsf.comment.entity.CommentEntity;
import io.nicheblog.dreamdiary.extension.clsf.comment.mapstruct.CommentMapstruct;
import io.nicheblog.dreamdiary.extension.clsf.comment.model.CommentDto;
import io.nicheblog.dreamdiary.extension.clsf.comment.repository.jpa.CommentRepository;
import io.nicheblog.dreamdiary.extension.clsf.comment.spec.CommentSpec;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseMultiCrudService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        implements BaseMultiCrudService<CommentDto, CommentDto, Integer, CommentEntity, CommentRepository, CommentSpec, CommentMapstruct> {

    @Getter
    private final CommentRepository repository;
    @Getter
    private final CommentSpec spec;
    @Getter
    private final CommentMapstruct mapstruct = CommentMapstruct.INSTANCE;
    private final ApplicationEventPublisherWrapper publisher;

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final CommentDto updatedDto) throws Exception {
        publisher.publishEvent(new CommentCacheEvictEvent(this, updatedDto.getRefPostNo(), updatedDto.getRefContentType()));
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final CommentDto updatedDto) throws Exception {
        publisher.publishEvent(new CommentCacheEvictEvent(this, updatedDto.getRefPostNo(), updatedDto.getRefContentType()));
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void postDelete(final CommentDto deletedDto) throws Exception {
        publisher.publishEvent(new CommentCacheEvictEvent(this, deletedDto.getRefPostNo(), deletedDto.getRefContentType()));
    }
}