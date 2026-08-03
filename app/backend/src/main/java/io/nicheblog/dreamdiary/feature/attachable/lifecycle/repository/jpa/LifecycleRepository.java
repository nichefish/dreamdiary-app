package io.nicheblog.dreamdiary.feature.attachable.lifecycle.repository.jpa;

import io.nicheblog.dreamdiary.feature.attachable.lifecycle.entity.LifecycleEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 부착 가능 컨텐츠 라이프사이클 row 저장소.
 *
 * <p>주요 조회는 유니크 기준인 {@code refContentType + refId}와 목록 렌더링용 보조 맵 일괄 조회에 맞춰져 있다.</p>
 */
@Repository
public interface LifecycleRepository
        extends BaseStreamRepository<LifecycleEntity, Integer> {

    /**
     * 컨텐츠 하나의 현재 라이프사이클 row를 조회한다.
     *
     * @param refId 컨텐츠 ID
     * @param refContentType 컨텐츠 타입 key
     * @return 현재 라이프사이클 row. 없으면 empty
     */
    Optional<LifecycleEntity> findByRefIdAndRefContentType(final Integer refId, final String refContentType);

    /**
     * 특정 컨텐츠 타입의 목록에 필요한 라이프사이클 row를 일괄 조회한다.
     *
     * @param refContentType 컨텐츠 타입 key
     * @param refIds 조회할 컨텐츠 ID 목록
     * @return 요청 ID에 해당하는 라이프사이클 row 목록
     */
    List<LifecycleEntity> findAllByRefContentTypeAndRefIdIn(final String refContentType, final Collection<Integer> refIds);

    /**
     * 컨텐츠의 라이프사이클 row를 물리 삭제한다.
     *
     * <p>{@code OPEN}은 row 부재로 표현하며, 현재값 테이블의 유니크 키를 다시 사용할 수 있도록
     * 엔티티 soft-delete가 아닌 물리 삭제를 수행한다.</p>
     *
     * @param refId 컨텐츠 ID
     * @param refContentType 컨텐츠 타입 key
     * @return 삭제한 row 수
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM lifecycle WHERE ref_id = :refId AND ref_content_type = :refContentType", nativeQuery = true)
    int deleteCurrentByRef(
            @Param("refId") final Integer refId,
            @Param("refContentType") final String refContentType
    );
}
