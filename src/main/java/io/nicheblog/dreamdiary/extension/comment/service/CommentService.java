package io.nicheblog.dreamdiary.extension.comment.service;

import io.nicheblog.dreamdiary.extension.comment.entity.CommentEntity;
import io.nicheblog.dreamdiary.extension.comment.mapstruct.CommentMapstruct;
import io.nicheblog.dreamdiary.extension.comment.model.CommentDto;
import io.nicheblog.dreamdiary.extension.comment.repository.jpa.CommentRepository;
import io.nicheblog.dreamdiary.extension.comment.spec.CommentSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseMultiCrudService;

/**
 * CommentService
 * <pre>
 *  댓글 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface CommentService
        extends BaseMultiCrudService<CommentDto, CommentDto, Integer, CommentEntity, CommentRepository, CommentSpec, CommentMapstruct> {

    //
}