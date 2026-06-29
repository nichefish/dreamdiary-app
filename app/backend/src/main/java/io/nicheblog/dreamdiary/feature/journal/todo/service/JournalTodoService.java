package io.nicheblog.dreamdiary.feature.journal.todo.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.service.BaseAttachableService;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.feature.journal.todo.entity.JournalTodoEntity;
import io.nicheblog.dreamdiary.feature.journal.todo.mapstruct.JournalTodoMapstruct;
import io.nicheblog.dreamdiary.feature.journal.todo.model.JournalTodoDto;
import io.nicheblog.dreamdiary.feature.journal.todo.model.JournalTodoSearchParam;
import io.nicheblog.dreamdiary.feature.journal.todo.repository.jpa.JournalTodoRepository;
import io.nicheblog.dreamdiary.feature.journal.todo.spec.JournalTodoSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * JournalTodoService
 * <pre>
 *  저널 일기 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalTodoService
        implements BaseAttachableService<JournalTodoDto, JournalTodoDto, Integer, JournalTodoEntity> {

    @Getter
    private final JournalTodoRepository repository;
    @Getter
    private final JournalTodoSpec spec;
    @Getter
    private final JournalTodoMapstruct mapstruct = JournalTodoMapstruct.INSTANCE;

    public JournalTodoMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public JournalTodoMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final JournalCacheEvictWorker journalCacheEvictWorker;

    private final ApplicationContext context;
    private JournalTodoService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 목록 조회 (dto level) :: 캐시 처리
     *
     * @param username 사용자 계정명
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    @Cacheable(value="journalTodoListByUser", key="new org.springframework.cache.interceptor.SimpleKey(#username, #searchParam.getYy(), #searchParam.getMnth())")
    public List<JournalTodoDto> getListDtoWithCacheByUser(final String username, final JournalTodoSearchParam searchParam) throws Exception {
        searchParam.setCreatedBy(AuthUtils.requireUsername(username));

        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 등록 전처리. (override)
     *
     * @param registDto 등록할 객체
     */
    @Override
    public void preRegist(final JournalTodoDto registDto) throws Exception {
        // 정렬 순서 처리
        final Integer lastSortOrder = repository.findLastIndexByYyMnth(registDto.getYy(), registDto.getMnth()).orElse(0);
        registDto.setSortOrder(lastSortOrder + 1);
    }

    /**
     * 수정 전처리. (override)
     *
     * @param modifyDto 수정할 객체 (dto)
     * @param modifyEntity 수정할 객체 (entity)
     */
    @Override
    public void preModify(final JournalTodoDto modifyDto, final JournalTodoEntity modifyEntity) throws Exception {
        if (!AuthUtils.isCreatedBy(modifyEntity.getCreatedBy())) {
            throw new NotAuthorizedException("common.result.access-not-authorized");
        }
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final JournalTodoDto updatedDto) throws Exception {
        // 관련 캐시 삭제
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_TODO);
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final JournalTodoDto postDto, final JournalTodoDto updatedDto) throws Exception {
        // 관련 캐시 삭제
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_TODO);
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 식별자
     * @return {@link JournalTodoDto} -- 조회된 객체
     */
    @Cacheable(value="journalTodoDetailDtoByUser", key="new org.springframework.cache.interceptor.SimpleKey(#username, #key)")
    public JournalTodoDto getDetailDtoWithCacheByUser(final String username, final Integer key) throws Exception {
        final JournalTodoEntity retrievedEntity = this.getSelf().getDtlEntity(key);
        final JournalTodoDto retrieved = mapstruct.toDto(retrievedEntity);
        // 권한 체크
        if (!retrieved.getIsCreatedBy(AuthUtils.requireUsername(username))) throw new NotAuthorizedException("common.result.access-not-authorized");
        return retrieved;
    }

    /**
     * 삭제 전처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void preDelete(final JournalTodoDto deletedDto) throws Exception {
        if (!AuthUtils.isCreatedBy(deletedDto.getCreatedBy())) {
            throw new NotAuthorizedException("common.result.access-not-authorized");
        }
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void postDelete(final JournalTodoDto deletedDto) throws Exception {
        // 관련 캐시 삭제
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(deletedDto), ContentType.JOURNAL_TODO);
    }
}

