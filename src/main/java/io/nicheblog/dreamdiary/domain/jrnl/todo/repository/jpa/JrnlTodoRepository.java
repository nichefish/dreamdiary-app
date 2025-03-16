package io.nicheblog.dreamdiary.domain.jrnl.todo.repository.jpa;

import io.nicheblog.dreamdiary.domain.jrnl.todo.entity.JrnlTodoEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.util.Optional;

/**
 * JrnlTodoRepository
 * <pre>
 *  저널 할일 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("jrnlTodoRepository")
public interface JrnlTodoRepository
        extends BaseStreamRepository<JrnlTodoEntity, Integer> {

    /**
     * 해당 일자에서 꿈 마지막 인덱스 조회
     *
     * @param yy 년도
     * @param mnth 월
     * @return {@link Optional} -- 해당 일자에서 꿈의 마지막 인덱스
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT MAX(todo.idx) " +
            "FROM JrnlTodoEntity todo " +
            "WHERE todo.yy = :yy AND NOT(todo.mnth = :mnth)")
    Optional<Integer> findLastIndexByYyMnth(final @Param("yy") Integer yy, final @Param("mnth") Integer mnth);
}