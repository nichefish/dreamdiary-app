package io.nicheblog.dreamdiary.domain.jrnl.todo.service.impl;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.domain.jrnl.todo.mapstruct.JrnlTodoMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.todo.model.JrnlTodoDto;
import io.nicheblog.dreamdiary.domain.jrnl.todo.model.JrnlTodoSearchParam;
import io.nicheblog.dreamdiary.domain.jrnl.todo.repository.jpa.JrnlTodoRepository;
import io.nicheblog.dreamdiary.domain.jrnl.todo.service.JrnlTodoService;
import io.nicheblog.dreamdiary.domain.jrnl.todo.spec.JrnlTodoSpec;
import io.nicheblog.dreamdiary.extension.cache.event.JrnlCacheEvictEvent;
import io.nicheblog.dreamdiary.extension.cache.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.extension.clsf.tag.event.JrnlTagProcEvent;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
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
public class JrnlTodoServiceImpl
        implements JrnlTodoService {

    @Getter
    private final JrnlTodoRepository repository;
    @Getter
    private final JrnlTodoSpec spec;
    @Getter
    private final JrnlTodoMapstruct mapstruct = JrnlTodoMapstruct.INSTANCE;

    private final ApplicationEventPublisherWrapper publisher;

    private final ApplicationContext context;
    private JrnlTodoService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 목록 조회 (dto level) :: 캐시 처리
     *
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 조회된 목록
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    @Override
    @Cacheable(value="myJrnlTodoList", key="T(io.nicheblog.dreamdiary.auth.security.util.AuthUtils).getLgnUserId() + \"_\" + #searchParam.getYy() + \"_\" + #searchParam.getMnth()")
    public List<JrnlTodoDto> getListDtoWithCache(final BaseSearchParam searchParam) throws Exception {
        searchParam.setRegstrId(AuthUtils.getLgnUserId());
        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 특정 태그의 관련 일기 목록 조회 :: 캐시 처리
     *
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 검색 결과 목록
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    @Override
    @Cacheable(value="myJrnlTodoTagDtl", key="T(io.nicheblog.dreamdiary.auth.security.util.AuthUtils).getLgnUserId() + \"_\" + #searchParam.getTagNo()")
    public List<JrnlTodoDto> jrnlTodoTagDtl(final JrnlTodoSearchParam searchParam) throws Exception {
        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 등록 전처리. (override)
     *
     * @param registDto 등록할 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void preRegist(final JrnlTodoDto registDto) throws Exception {
        // 인덱스(정렬순서) 처리
        final Integer lastIndex = repository.findLastIndexByYyMnth(registDto.getYy(), registDto.getMnth()).orElse(0);
        registDto.setIdx(lastIndex + 1);
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void postRegist(final JrnlTodoDto updatedDto) throws Exception {
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishEvent(new JrnlTagProcEvent(this, updatedDto.getClsfKey(), updatedDto.getYy(), updatedDto.getMnth(), updatedDto.tag));
        // 관련 캐시 삭제
        publisher.publishEvent(new JrnlCacheEvictEvent(this, JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_TODO));
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void postModify(final JrnlTodoDto updatedDto) throws Exception {
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishEvent(new JrnlTagProcEvent(this, updatedDto.getClsfKey(), updatedDto.getYy(), updatedDto.getMnth(), updatedDto.tag));
        // 관련 캐시 삭제
        publisher.publishEvent(new JrnlCacheEvictEvent(this, JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_TODO));
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 식별자
     * @return {@link JrnlTodoDto} -- 조회된 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    @Cacheable(value="myJrnlTodoDtlDto", key="T(io.nicheblog.dreamdiary.auth.security.util.AuthUtils).getLgnUserId() + \"_\" + #key")
    public JrnlTodoDto getDtlDtoWithCache(final Integer key) throws Exception {
        final JrnlTodoDto retrieved = this.getSelf().getDtlDto(key);
        // 권한 체크
        if (!retrieved.getIsRegstr()) throw new NotAuthorizedException(MessageUtils.getMessage("common.rslt.access-not-authorized"));
        return retrieved;
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void postDelete(final JrnlTodoDto deletedDto) throws Exception {
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishEvent(new JrnlTagProcEvent(this, deletedDto.getClsfKey(), deletedDto.getYy(), deletedDto.getMnth(), deletedDto.tag));
        // 관련 캐시 삭제
        publisher.publishEvent(new JrnlCacheEvictEvent(this, JrnlCacheEvictParam.of(deletedDto), ContentType.JRNL_TODO));
    }
}