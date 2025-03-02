package io.nicheblog.dreamdiary.domain.jrnl.diary.service;

import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryEntity;
import io.nicheblog.dreamdiary.domain.jrnl.diary.mapstruct.JrnlDiaryMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiarySearchParam;
import io.nicheblog.dreamdiary.domain.jrnl.diary.repository.jpa.JrnlDiaryRepository;
import io.nicheblog.dreamdiary.domain.jrnl.diary.spec.JrnlDiarySpec;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseClsfService;

import java.util.List;

/**
 * JrnlDiaryService
 * <pre>
 *  저널 일기 관리 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface JrnlDiaryService
        extends BaseClsfService<JrnlDiaryDto, JrnlDiaryDto, Integer, JrnlDiaryEntity, JrnlDiaryRepository, JrnlDiarySpec, JrnlDiaryMapstruct> {

    /**
     * 목록 조회 (dto level) :: 캐시 처리
     *
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 조회된 목록
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    List<JrnlDiaryDto> getListDtoWithCache(final BaseSearchParam searchParam) throws Exception;

    /**
     * 특정 년도의 중요 일기 목록 조회 :: 캐시 처리
     *
     * @param yy 조회할 년도
     * @return {@link List} -- 해당 년도의 중요 목록
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    List<JrnlDiaryDto> getImprtcDiaryList(final Integer yy) throws Exception;

    /**
     * 특정 태그의 관련 일기 목록 조회 :: 캐시 처리
     *
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 검색 결과 목록
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    List<JrnlDiaryDto> jrnlDiaryTagDtl(final JrnlDiarySearchParam searchParam) throws Exception;

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 식별자
     * @return {@link JrnlDiaryDto} -- 조회된 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    JrnlDiaryDto getDtlDtoWithCache(final Integer key) throws Exception;

    /**
     * 삭제 데이터 조회
     *
     * @param key 삭제된 데이터의 키
     * @return {@link JrnlDiaryDto} -- 삭제된 데이터 Dto
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    JrnlDiaryDto getDeletedDtlDto(final Integer key) throws Exception;
}