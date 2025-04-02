package io.nicheblog.dreamdiary.domain.jrnl.todo.service;

import io.nicheblog.dreamdiary.domain.jrnl.todo.entity.JrnlTodoEntity;
import io.nicheblog.dreamdiary.domain.jrnl.todo.mapstruct.JrnlTodoMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.todo.model.JrnlTodoDto;
import io.nicheblog.dreamdiary.domain.jrnl.todo.model.JrnlTodoSearchParam;
import io.nicheblog.dreamdiary.domain.jrnl.todo.repository.jpa.JrnlTodoRepository;
import io.nicheblog.dreamdiary.domain.jrnl.todo.spec.JrnlTodoSpec;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseClsfService;

import java.util.List;

/**
 * JrnlTodoService
 * <pre>
 *  저널 할일 관리 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface JrnlTodoService
        extends BaseClsfService<JrnlTodoDto, JrnlTodoDto, Integer, JrnlTodoEntity, JrnlTodoRepository, JrnlTodoSpec, JrnlTodoMapstruct> {

    /**
     * 목록 조회 (dto level) :: 캐시 처리
     *
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 조회된 목록
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    List<JrnlTodoDto> getListDtoWithCache(final BaseSearchParam searchParam) throws Exception;

    /**
     * 특정 태그의 관련 일기 목록 조회 :: 캐시 처리
     *
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 검색 결과 목록
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    List<JrnlTodoDto> jrnlTodoTagDtl(final JrnlTodoSearchParam searchParam) throws Exception;

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 식별자
     * @return {@link JrnlTodoDto} -- 조회된 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    JrnlTodoDto getDtlDtoWithCache(final Integer key) throws Exception;
}