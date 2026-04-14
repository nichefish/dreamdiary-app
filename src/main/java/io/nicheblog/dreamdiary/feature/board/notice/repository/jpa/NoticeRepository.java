package io.nicheblog.dreamdiary.feature.board.notice.repository.jpa;

import io.nicheblog.dreamdiary.feature.board.notice.entity.NoticeEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.util.Date;

/**
 * NoticeRepository
 * <pre>
 *  공지사항 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("noticeRepository")
public interface NoticeRepository
        extends BaseStreamRepository<NoticeEntity, Integer> {

    /**
     * 최종수정일이 조회기준일자 이내이고, 최종수정자(또는 작성자)가 내가 아니고, 내가 (수정 이후로) 조회하지 않은 글 갯수를 조회한다.
     * @param username 사용자 계정명
     * @param stdrdDt 조회기준일자 (ex.1주일)
     * @return Integer
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT COUNT(notice.id) " +
            "FROM NoticeEntity notice " +
            "INNER JOIN ViewerEntity viewer ON viewer.refContentType = 'NOTICE' AND notice.id = viewer.refId " +
            "WHERE COALESCE(notice.updatedAt, notice.createdAt) >= :stdrdDt " +
            " AND COALESCE(notice.updatedBy, notice.createdBy) != :username " +
            " AND ( " +
            "  viewer.createdBy != :username " +
            "  OR (viewer.createdBy = :username and viewer.lastVisitedAt < COALESCE(notice.updatedAt, notice.createdAt)) " +
            ")")
    Integer getUnreadCnt(final @Param("username") String username, final @Param("stdrdDt") Date stdrdDt);
}
