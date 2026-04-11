package io.nicheblog.dreamdiary.feature.jrnl.todo.entity;

import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * JrnlTodoEntityTestFactory
 * <pre>
 *  Factory helpers for JrnlTodo test Entity objects.
 * </pre>
 *
 * @author nichefish
 */
@UtilityClass
@ActiveProfiles("test")
public class JrnlTodoEntityTestFactory {

    /**
     * Build a default Entity for tests.
     */
    public static JrnlTodoEntity create() throws Exception {
        return JrnlTodoEntity.builder()
                .contentType(ContentType.JRNL_TODO.key)
                .title("test_title")
                .cn("test_cn")
                .ctgrCd("test_ctgr_cd")
                .yy(2000)
                .mnth(1)
                .idx(1)
                .build();
    }
}
