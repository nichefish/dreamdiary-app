package io.nicheblog.dreamdiary.feature.journal.chapter.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.service.BaseAttachableService;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.mapstruct.JournalChapterMapstruct;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterSearchParam;
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.jpa.JournalChapterRepository;
import io.nicheblog.dreamdiary.feature.journal.chapter.repository.mybatis.JournalChapterMapper;
import io.nicheblog.dreamdiary.feature.journal.chapter.spec.JournalChapterSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * JournalChapterService
 * <pre>
 *  저널 챕터 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalChapterService
        implements BaseAttachableService<JournalChapterDto, JournalChapterDto, Integer, JournalChapterEntity> {

    /** 동일 일자 내 첫 항목 등록 시 기본 카테고리 코드 */
    private static final String FIRST_CHAPTER_CTGR_CD = "SUMMARY";

    @Getter
    private final JournalChapterRepository repository;
    @Getter
    private final JournalChapterSpec spec;
    @Getter
    private final JournalChapterMapstruct mapstruct;

    public JournalChapterMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public JournalChapterMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final JournalChapterMapper journalChapterMapper;
    private final JournalCacheEvictWorker journalCacheEvictWorker;

    private final ApplicationContext context;
    private JournalChapterService getSelf() {
        return context.getBean(this.getClass());
    }

    public List<JournalChapterDto> getListDtoByUser(final String username, final JournalChapterSearchParam searchParam) throws Exception {
        searchParam.setCreatedBy(AuthUtils.requireUsername(username));
        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param username 사용자 계정명
     * @param key 일련번호
     * @return {@link JournalChapterDto} -- 조회된 객체
     */
    @Cacheable(value="journalChapterDtlDtoByUser", key="new org.springframework.cache.interceptor.SimpleKey(#username, #key)")
    public JournalChapterDto getDtlDtoWithCacheByUser(final String username, final Integer key) throws Exception {
        final JournalChapterEntity retrievedEntity = this.getSelf().getDtlEntity(key);
        final JournalChapterDto retrieved = mapstruct.toDto(retrievedEntity);
        if (!retrieved.getIsCreatedBy(AuthUtils.requireUsername(username))) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
        return retrieved;
    }

    /**
     * 등록 전처리. (override)
     *
     * @param registDto 등록할 객체
     */
    @Override
    public void preRegist(final JournalChapterDto registDto) throws Exception {
        // 정렬 순서 처리
        final int lastSortOrder = repository.findLastIndexByJournalDay(registDto.getJournalDayId()).orElse(0);
        if (lastSortOrder == 0 && StringUtils.isBlank(registDto.getCategoryCode())) {
            registDto.setCategoryCode(FIRST_CHAPTER_CTGR_CD);
        }
        registDto.setSortOrder(lastSortOrder + 1);
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final JournalChapterDto updatedDto) throws Exception {
        // 관련 캐시 삭제
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_CHAPTER);
    }
    
    /**
     * 수정 전처리. (override)
     *
     * @param modifyDto - 수정할 객체
     * @param modifyEntity - 수정할 객체
     */
    @Override
    public void preModify(final JournalChapterDto modifyDto, final JournalChapterEntity modifyEntity) throws Exception {
        if (!AuthUtils.isCreatedBy(modifyEntity.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
        final boolean isSortOrderChanged = !Objects.equals(modifyDto.getSortOrder(), modifyEntity.getSortOrder());
        modifyDto.setIsSortOrderChanged(isSortOrderChanged);
    }
    
    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final JournalChapterDto postDto, final JournalChapterDto updatedDto) throws Exception {
        // 정렬 순서 재조정
        if (updatedDto.getIsSortOrderChanged()) this.getSelf().reorderSortOrder(updatedDto);

        // 관련 캐시 삭제
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_CHAPTER);
    }

    /**
     * 삭제 전처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void preDelete(final JournalChapterDto deletedDto) throws Exception {
        if (!AuthUtils.isCreatedBy(deletedDto.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */

    @Override
    public void postDelete(final JournalChapterDto deletedDto) throws Exception {
        // 정렬 순서 재조정
        this.getSelf().reorderSortOrder(deletedDto);

        // 관련 캐시 삭제
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(deletedDto), ContentType.JOURNAL_CHAPTER);
    }

    /**
     * 삭제 데이터 조회
     *
     * @param key 삭제된 데이터의 키
     * @return {@link JournalChapterDto} -- 삭제된 데이터 Dto
     */
    @Transactional(readOnly = true)
    public JournalChapterDto getDeletedDtlDto(final Integer key) throws Exception {
        final JournalChapterDto deleted = journalChapterMapper.getDeletedById(key);
        if (deleted == null) return null;
        if (!AuthUtils.isCreatedBy(deleted.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
        return deleted;
    }
    
    /**
     * 해당 그룹 전체를 sortOrder = 1부터 다시 정렬한다.
     *
     * @param journalDayId 정렬을 수행할 상위 키
     */
    @Transactional
    public void normalizeSortOrder(final Integer journalDayId) {
        final List<JournalChapterDto> list = journalChapterMapper.findAllForReorder(journalDayId);
        if (CollectionUtils.isEmpty(list)) return;

        int sortOrder = 1;
        for (final JournalChapterDto e : list) {
            e.setSortOrder(sortOrder++);
        }

        journalChapterMapper.batchUpdateIdx(list);
    }
    
    /**
     * 대상 상위 키에 엔티티를 특정 위치에 삽입 후 재정렬한다.
     *
     * @param journalDayId 정렬을 수행할 상위 키
     * @param id 게시물 PK
     * @param targetSortOrder 삽입할 목표 위치(1-based). null이면 맨 뒤에 삽입됨
     */
    @Transactional
    public void insert(final Integer journalDayId, final Integer id, Integer targetSortOrder) throws Exception {
        final List<JournalChapterDto> list = journalChapterMapper.findAllForReorder(journalDayId);

        // target 조회
        final JournalChapterEntity targetEntity = findDtlEntity(id);
        final JournalChapterDto target = mapstruct.toDto(targetEntity);
        if (target == null) return;

        // 혹시 이미 포함되어 있으면 제거
        list.removeIf(e -> Objects.equals(e.getId(), id));

        // chapterNo 변경
        target.setJournalDayId(journalDayId);

        // targetSortOrder 보정 (upper bound)
        final int maxIdx = list.size() + 1;
        final int normalizedIdx = Math.min(targetSortOrder == null ? maxIdx : targetSortOrder, maxIdx);
        // 삽입 위치 계산
        int pos = normalizedIdx - 1;
        pos = Math.min(pos, list.size());
        list.add(pos, target);

        // sortOrder 재정렬
        int sortOrder = 1;
        for (final JournalChapterDto e : list) {
            e.setSortOrder(sortOrder++);
        }

        journalChapterMapper.batchUpdateIdx(list);
    }

    /**
     * 정렬 순서 변경 시 관련 순서를 업데이트
     *
     * @param updatedDto 업데이트된 객체
     */
    @Transactional
    public void reorderSortOrder(final JournalChapterDto updatedDto) throws Exception {
        // 1단계: 현재 chapter 그룹 정리 (기존 sortOrder 값을 normalize하여 안정화)
        normalizeSortOrder(updatedDto.getJournalDayId());
        // 2단계: 해당 group에 새 위치로 target 삽입
        insert(updatedDto.getJournalDayId(), updatedDto.getId(), updatedDto.getSortOrder());
    }
}


