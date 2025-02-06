package io.nicheblog.dreamdiary.extension.comment.entity;

import io.nicheblog.dreamdiary.extension.ContentType;
import io.nicheblog.dreamdiary.extension.comment.entity.CommentEntity;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * CommentEntityTestFactory
 * <pre>
 *  댓글 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class CommentEntityTestFactory {

    /**
     * 테스트용 댓글 Entity 생성
     */
    public static CommentEntity create() throws Exception {
        return CommentEntity.builder()
                .contentType(ContentType.COMMENT.key)
                .build();
    }
}
