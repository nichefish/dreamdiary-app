package io.nicheblog.dreamdiary.feature.journal.todo.model;

import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * JournalTodoDtoTestFactory
 * <pre>
 *  Factory helpers for JournalTodo test DTO objects.
 * </pre>
 *
 * @author nichefish
 */
@UtilityClass
@ActiveProfiles("test")
public class JournalTodoDtoTestFactory {

    /**
     * Build a default DTO for tests.
     */
    public static JournalTodoDto create() throws Exception {
        return JournalTodoDto.builder()
                .contentType(ContentType.JOURNAL_TODO.key)
                .title("test_title")
                .content("test_cn")
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
    public static JournalTodoDto createWithKey(final Integer key) throws Exception {
        return JournalTodoDto.builder()
                .id(key)
                .contentType(ContentType.JOURNAL_TODO.key)
                .title("test_title")
                .content("test_cn")
                .ctgrCd("test_ctgr_cd")
                .yy(2000)
                .mnth(1)
                .build();
    }
}

