package io.nicheblog.dreamdiary.feature.journal.reflection.repository.jpa;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.reflection.entity.JournalReflectionEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Reflection(Commentary) Repository.
 *
 * <p>Reflection 은 별도 Aggregate Root 이므로 Entry 와 분리된 저장소를 갖는다. 대상 역참조 로드(어떤
 * Entry 를 가리키는 Reflection 조회)를 제공한다. 임베드 표시 정렬은 같은 대상 아래 {@code sortOrder}
 * 이다.</p>
 *
 * @author nichefish
 */
@Repository
public interface JournalReflectionRepository
        extends BaseStreamRepository<JournalReflectionEntity, Integer> {

    /**
     * 대상(target) 참조 ID 집합으로 Reflection 목록을 순번·ID 오름차순으로 조회한다.
     * 대상 Entry 에 달린 Reflection 역참조 로드(R3 읽기 경로)에 쓴다.
     *
     * @param refIdList 대상 엔트리 번호 목록
     * @return Reflection 목록 (sortOrder, id asc)
     */
    List<JournalReflectionEntity> findAllByRefIdInOrderBySortOrderAscIdAsc(Collection<Integer> refIdList);

    /**
     * 같은 대상 아래 가장 큰 순번 행을 조회한다. 다음 순번 계산에 쓴다.
     *
     * @param refId 대상 엔티티 번호
     * @param refContentType 대상 콘텐츠 타입
     * @return 가장 큰 순번 행
     */
    Optional<JournalReflectionEntity> findFirstByRefIdAndRefContentTypeOrderBySortOrderDesc(
            Integer refId,
            ContentType refContentType
    );

    /**
     * 같은 대상 아래 Reflection 목록을 순번·ID 오름차순으로 조회한다. 재배치에 쓴다.
     *
     * @param refId 대상 엔티티 번호
     * @param refContentType 대상 콘텐츠 타입
     * @return Reflection 목록 (sortOrder, id asc)
     */
    List<JournalReflectionEntity> findAllByRefIdAndRefContentTypeOrderBySortOrderAscIdAsc(
            Integer refId,
            ContentType refContentType
    );

    /**
     * 대상(target)을 가리키는 활성 Reflection 이 하나라도 존재하는지 확인한다.
     * 대상 삭제 Block 가드(참조 Reflection 이 있으면 대상 삭제 거부)에 쓴다. soft-delete 행은
     * 엔티티 {@code @Where(deleted_at IS NULL)} 로 자동 제외된다.
     *
     * @param refId 대상 엔티티 번호
     * @param refContentType 대상 콘텐츠 타입
     * @return 참조 Reflection 존재 여부
     */
    boolean existsByRefIdAndRefContentType(Integer refId, ContentType refContentType);

    /**
     * 대상(target)을 가리키는 Reflection 목록을 조회한다.
     * primary RESOLVED → 딸린 Reflection RESOLVED 연쇄에 쓴다.
     *
     * @param refId 대상 엔티티 번호
     * @param refContentType 대상 콘텐츠 타입
     * @return Reflection 목록
     */
    List<JournalReflectionEntity> findAllByRefIdAndRefContentType(Integer refId, ContentType refContentType);

    /**
     * 대상 ID 집합·콘텐츠 타입 집합을 가리키는 활성 Reflection 이 하나라도 있는지 확인한다.
     * 챕터 삭제 Block(챕터 내 엔트리에 달린 Reflection 존재 검사)에 쓴다.
     *
     * @param refIdList 대상 엔티티 번호 목록
     * @param refContentTypeList 대상 콘텐츠 타입 목록
     * @return 참조 Reflection 존재 여부
     */
    boolean existsByRefIdInAndRefContentTypeIn(
            Collection<Integer> refIdList,
            Collection<ContentType> refContentTypeList
    );
}
