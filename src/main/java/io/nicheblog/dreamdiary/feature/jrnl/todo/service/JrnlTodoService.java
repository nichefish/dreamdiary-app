package io.nicheblog.dreamdiary.feature.jrnl.todo.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf._shared.service.BaseClsfService;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.jrnl._shared.handler.JrnlCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.jrnl._shared.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.feature.jrnl.todo.entity.JrnlTodoEntity;
import io.nicheblog.dreamdiary.feature.jrnl.todo.mapstruct.JrnlTodoMapstruct;
import io.nicheblog.dreamdiary.feature.jrnl.todo.model.JrnlTodoDto;
import io.nicheblog.dreamdiary.feature.jrnl.todo.model.JrnlTodoSearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.todo.repository.jpa.JrnlTodoRepository;
import io.nicheblog.dreamdiary.feature.jrnl.todo.spec.JrnlTodoSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * JrnlTodoService
 * <pre>
 *  저널 일기 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("jrnlTodoService")
@RequiredArgsConstructor
@Log4j2
public class JrnlTodoService
        implements BaseClsfService<JrnlTodoDto, JrnlTodoDto, Integer, JrnlTodoEntity> {

    @Getter
    private final JrnlTodoRepository repository;
    @Getter
    private final JrnlTodoSpec spec;
    @Getter
    private final JrnlTodoMapstruct mapstruct = JrnlTodoMapstruct.INSTANCE;

    public JrnlTodoMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public JrnlTodoMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final JrnlCacheEvictWorker jrnlCacheEvictWorker;

    private final ApplicationContext context;
    private JrnlTodoService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 목록 조회 (dto level) :: 캐시 처리
     *
     * @param userId 사용자 ID
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    @Cacheable(value="jrnlTodoListByUser", key="new org.springframework.cache.interceptor.SimpleKey(#userId, #searchParam.getYy(), #searchParam.getMnth())")
    public List<JrnlTodoDto> getListDtoWithCacheByUser(final String userId, final JrnlTodoSearchParam searchParam) throws Exception {
        searchParam.setRegstrId(AuthUtils.requireUserId(userId));

        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 등록 전처리. (override)
     *
     * @param registDto 등록할 객체
     */
    @Override
    public void preRegist(final JrnlTodoDto registDto) throws Exception {
        // 인덱스(정렬순서) 처리
        final Integer lastIndex = repository.findLastIndexByYyMnth(registDto.getYy(), registDto.getMnth()).orElse(0);
        registDto.setIdx(lastIndex + 1);
    }

    /**
     * 수정 전처리. (override)
     *
     * @param modifyDto 수정할 객체 (dto)
     * @param modifyEntity 수정할 객체 (entity)
     */
    @Override
    public void preModify(final JrnlTodoDto modifyDto, final JrnlTodoEntity modifyEntity) throws Exception {
        if (!AuthUtils.isRegstr(modifyEntity.getRegstrId())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final JrnlTodoDto updatedDto) throws Exception {
        // 관련 캐시 삭제
        jrnlCacheEvictWorker.evictAfterCommit(JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_TODO);
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final JrnlTodoDto postDto, final JrnlTodoDto updatedDto) throws Exception {
        // 관련 캐시 삭제
        jrnlCacheEvictWorker.evictAfterCommit(JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_TODO);
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 식별자
     * @return {@link JrnlTodoDto} -- 조회된 객체
     */
    @Cacheable(value="jrnlTodoDtlDtoByUser", key="new org.springframework.cache.interceptor.SimpleKey(#userId, #key)")
    public JrnlTodoDto getDtlDtoWithCacheByUser(final String userId, final Integer key) throws Exception {
        final JrnlTodoEntity retrievedEntity = this.getSelf().getDtlEntity(key);
        final JrnlTodoDto retrieved = mapstruct.toDto(retrievedEntity);
        // 권한 체크
        if (!retrieved.getIsRegstr(AuthUtils.requireUserId(userId))) throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        return retrieved;
    }

    /**
     * 삭제 전처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void preDelete(final JrnlTodoDto deletedDto) throws Exception {
        if (!AuthUtils.isRegstr(deletedDto.getRegstrId())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void postDelete(final JrnlTodoDto deletedDto) throws Exception {
        // 관련 캐시 삭제
        jrnlCacheEvictWorker.evictAfterCommit(JrnlCacheEvictParam.of(deletedDto), ContentType.JRNL_TODO);
    }
}
