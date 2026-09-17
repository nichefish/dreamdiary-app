package io.nicheblog.dreamdiary.feature.journal.todo.repository.jpa;

import io.nicheblog.dreamdiary.feature.journal.todo.entity.JournalTodoEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.util.Optional;

/**
 * JournalTodoRepository
 * <pre>
 *  저널 할일 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface JournalTodoRepository
        extends BaseStreamRepository<JournalTodoEntity, Integer> {

    /**
     * 사용자의 마지막 할일 순번 조회 (월 무관 전역).
     *
     * @param createdBy 등록자 ID
     * @return {@link Optional} -- 해당 사용자의 최대 sortOrder
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT MAX(todo.sortOrder) " +
            "FROM JournalTodoEntity todo " +
            "WHERE todo.createdBy = :createdBy")
    Optional<Integer> findLastIndexByCreatedBy(final @Param("createdBy") String createdBy);
}


