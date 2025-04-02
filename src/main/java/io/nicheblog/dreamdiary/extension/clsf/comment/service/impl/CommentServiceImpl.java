package io.nicheblog.dreamdiary.extension.clsf.comment.service.impl;

import io.nicheblog.dreamdiary.extension.cache.event.CommentCacheEvictEvent;
import io.nicheblog.dreamdiary.extension.clsf.comment.mapstruct.CommentMapstruct;
import io.nicheblog.dreamdiary.extension.clsf.comment.model.CommentDto;
import io.nicheblog.dreamdiary.extension.clsf.comment.repository.jpa.CommentRepository;
import io.nicheblog.dreamdiary.extension.clsf.comment.service.CommentService;
import io.nicheblog.dreamdiary.extension.clsf.comment.spec.CommentSpec;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
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
public class CommentServiceImpl
        implements CommentService {

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
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void postRegist(final CommentDto updatedDto) throws Exception {
        publisher.publishEvent(new CommentCacheEvictEvent(this, updatedDto.getRefPostNo(), updatedDto.getRefContentType()));
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void postModify(final CommentDto updatedDto) throws Exception {
        publisher.publishEvent(new CommentCacheEvictEvent(this, updatedDto.getRefPostNo(), updatedDto.getRefContentType()));
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void postDelete(final CommentDto deletedDto) throws Exception {
        publisher.publishEvent(new CommentCacheEvictEvent(this, deletedDto.getRefPostNo(), deletedDto.getRefContentType()));
    }
}