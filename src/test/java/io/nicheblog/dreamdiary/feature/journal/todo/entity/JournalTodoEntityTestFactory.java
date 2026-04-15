package io.nicheblog.dreamdiary.feature.journal.todo.entity;

import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * JournalTodoEntityTestFactory
 * <pre>
 *  Factory helpers for JournalTodo test Entity objects.
 * </pre>
 *
 * @author nichefish
 */
@UtilityClass
@ActiveProfiles("test")
public class JournalTodoEntityTestFactory {

    /**
     * Build a default Entity for tests.
     */
    public static JournalTodoEntity create() throws Exception {
        return JournalTodoEntity.builder()
                .contentType(ContentType.JOURNAL_TODO.key)
                .title("test_title")
                .cn("test_cn")
                .ctgrCd("test_ctgr_cd")
                .yy(2000)
                .mnth(1)
                .idx(1)
                .build();
    }
}

