package io.nicheblog.dreamdiary.feature.attachable.comment.entity;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
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
