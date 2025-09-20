package io.nicheblog.dreamdiary.domain.jrnl.entry.service;

import io.nicheblog.dreamdiary.domain.jrnl.entry.entity.JrnlEntryEntity;
import io.nicheblog.dreamdiary.domain.jrnl.entry.mapstruct.JrnlEntryMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.entry.model.JrnlEntryDto;
import io.nicheblog.dreamdiary.domain.jrnl.entry.model.JrnlEntrySearchParam;
import io.nicheblog.dreamdiary.domain.jrnl.entry.repository.jpa.JrnlEntryRepository;
import io.nicheblog.dreamdiary.domain.jrnl.entry.spec.JrnlEntrySpec;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseClsfService;

import java.util.List;

/**
 * JrnlEntryService
 * <pre>
 *  저널 항목 관리 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface JrnlEntryService
        extends BaseClsfService<JrnlEntryDto, JrnlEntryDto, Integer, JrnlEntryEntity, JrnlEntryRepository, JrnlEntrySpec, JrnlEntryMapstruct> {

    /**
     * 특정 태그의 관련 항목 목록 조회 :: 캐시 처리
     *
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 검색 결과 목록
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    List<JrnlEntryDto> jrnlEntryTagDtl(final JrnlEntrySearchParam searchParam) throws Exception;
}