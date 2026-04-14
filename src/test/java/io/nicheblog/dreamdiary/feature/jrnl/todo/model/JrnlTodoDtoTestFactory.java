package io.nicheblog.dreamdiary.feature.jrnl.todo.model;

import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * JrnlTodoDtoTestFactory
 * <pre>
 *  Factory helpers for JrnlTodo test DTO objects.
 * </pre>
 *
 * @author nichefish
 */
@UtilityClass
@ActiveProfiles("test")
public class JrnlTodoDtoTestFactory {

    /**
     * Build a default DTO for tests.
     */
    public static JrnlTodoDto create() throws Exception {
        return JrnlTodoDto.builder()
                .contentType(ContentType.JRNL_TODO.key)
                .title("test_title")
                .cn("test_cn")
                .ctgrCd("test_ctgr_cd")
                .yy(2000)
                .mnth(1)
                .build();
    }

    /**
     * Build a DTO with an existing key for update/delete tests.
     *
     * @param key primary key
     */
    public static JrnlTodoDto createWithKey(final Integer key) throws Exception {
        return JrnlTodoDto.builder()
                .id(key)
                .contentType(ContentType.JRNL_TODO.key)
                .title("test_title")
                .cn("test_cn")
                .ctgrCd("test_ctgr_cd")
                .yy(2000)
                .mnth(1)
                .build();
    }
}
