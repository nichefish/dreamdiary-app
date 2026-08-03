package io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa;

import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface JournalEntryRepository
        extends BaseStreamRepository<JournalEntryEntity, Integer> {

    /**
     * ID와 콘텐츠 타입으로 엔트리를 조회한다.
     *
     * @param id 엔트리 ID
     * @param contentType 콘텐츠 타입
     * @return 조회 결과
     */
    Optional<JournalEntryEntity> findByIdAndContentType(Integer id, String contentType);

    /**
     * ID/콘텐츠 타입 집합으로 엔트리 목록을 조회한다.
     *
     * @param idList 엔트리 ID 목록
     * @param contentTypeList 콘텐츠 타입 목록
     * @return 엔트리 목록
     */
    List<JournalEntryEntity> findAllByIdInAndContentTypeIn(Collection<Integer> idList, Collection<String> contentTypeList);

    /**
     * 콘텐츠 타입과 target 참조 ID 집합으로 엔트리를 조회한다.
     * Reflection 역참조 로드에 쓴다 (target 을 가리키는 REFLECTION 행 조회).
     *
     * @param contentType 콘텐츠 타입 (REFLECTION)
     * @param refIdList target 엔트리 ID 목록
     * @return 엔트리 목록
     */
    List<JournalEntryEntity> findAllByContentTypeAndRefIdIn(String contentType, Collection<Integer> refIdList);


    /**
     * 챕터/콘텐츠 타입 기준으로 순번 오름차순 목록을 조회한다.
     *
     * @param journalChapterId 챕터 ID
     * @param contentType 콘텐츠 타입
     * @return 엔트리 목록
     */
    List<JournalEntryEntity> findAllByJournalChapterIdAndContentTypeOrderBySortOrderAsc(Integer journalChapterId, String contentType);

    /**
     * 챕터/콘텐츠 타입 기준 가장 마지막 순번 엔트리를 조회한다.
     *
     * @param journalChapterId 챕터 ID
     * @param contentType 콘텐츠 타입
     * @return 조회 결과
     */
    Optional<JournalEntryEntity> findFirstByJournalChapterIdAndContentTypeOrderBySortOrderDesc(Integer journalChapterId, String contentType);

}
