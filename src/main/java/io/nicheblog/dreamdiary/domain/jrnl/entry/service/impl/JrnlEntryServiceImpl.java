package io.nicheblog.dreamdiary.domain.jrnl.entry.service.impl;

import io.nicheblog.dreamdiary.domain.jrnl.entry.mapstruct.JrnlEntryMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.entry.model.JrnlEntryDto;
import io.nicheblog.dreamdiary.domain.jrnl.entry.model.JrnlEntrySearchParam;
import io.nicheblog.dreamdiary.domain.jrnl.entry.repository.jpa.JrnlEntryRepository;
import io.nicheblog.dreamdiary.domain.jrnl.entry.service.JrnlEntryService;
import io.nicheblog.dreamdiary.domain.jrnl.entry.spec.JrnlEntrySpec;
import io.nicheblog.dreamdiary.extension.cache.event.JrnlCacheEvictEvent;
import io.nicheblog.dreamdiary.extension.cache.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.extension.clsf.tag.event.JrnlTagProcEvent;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * JrnlEntryService
 * <pre>
 *  저널 항목 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("jrnlEntryService")
@RequiredArgsConstructor
@Log4j2
public class JrnlEntryServiceImpl
        implements JrnlEntryService {

    @Getter
    private final JrnlEntryRepository repository;
    @Getter
    private final JrnlEntrySpec spec;
    @Getter
    private final JrnlEntryMapstruct mapstruct = JrnlEntryMapstruct.INSTANCE;

    private final ApplicationEventPublisherWrapper publisher;

    private final ApplicationContext context;
    private JrnlEntryService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 특정 태그의 관련 항목 목록 조회 :: 캐시 처리
     *
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 검색 결과 목록
     */
    @Override
    @Cacheable(value="myJrnlEntryTagDtl", key="T(io.nicheblog.dreamdiary.auth.security.util.AuthUtils).getLgnUserId() + \"_\" + #searchParam.getTagNo()")
    public List<JrnlEntryDto> jrnlEntryTagDtl(final JrnlEntrySearchParam searchParam) throws Exception {
        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 등록 전처리. (override)
     *
     * @param registDto 등록할 객체
     */
    @Override
    public void preRegist(final JrnlEntryDto registDto) throws Exception {
        // 인덱스(정렬순서) 처리
        final Integer lastIndex = repository.findLastIndexByJrnlDay(registDto.getJrnlDayNo()).orElse(0);
        registDto.setIdx(lastIndex + 1);
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final JrnlEntryDto updatedDto) throws Exception {
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishEvent(new JrnlTagProcEvent(this, updatedDto.getClsfKey(), updatedDto.getYy(), updatedDto.getMnth(), updatedDto.tag));
        // 관련 캐시 삭제
        publisher.publishEvent(new JrnlCacheEvictEvent(this, JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_ENTRY));
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final JrnlEntryDto updatedDto) throws Exception {
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishEvent(new JrnlTagProcEvent(this, updatedDto.getClsfKey(), updatedDto.getYy(), updatedDto.getMnth(), updatedDto.tag));
        // 관련 캐시 삭제
        publisher.publishEvent(new JrnlCacheEvictEvent(this, JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_ENTRY));
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void postDelete(final JrnlEntryDto deletedDto) throws Exception {
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishEvent(new JrnlTagProcEvent(this, deletedDto.getClsfKey(), deletedDto.getYy(), deletedDto.getMnth()));
        // 관련 캐시 삭제
        publisher.publishEvent(new JrnlCacheEvictEvent(this, JrnlCacheEvictParam.of(deletedDto), ContentType.JRNL_ENTRY));
    }
}