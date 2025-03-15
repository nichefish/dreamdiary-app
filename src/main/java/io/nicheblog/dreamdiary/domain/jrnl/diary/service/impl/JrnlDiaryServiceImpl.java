package io.nicheblog.dreamdiary.domain.jrnl.diary.service.impl;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.domain.jrnl.diary.mapstruct.JrnlDiaryMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiarySearchParam;
import io.nicheblog.dreamdiary.domain.jrnl.diary.repository.jpa.JrnlDiaryRepository;
import io.nicheblog.dreamdiary.domain.jrnl.diary.repository.mybatis.JrnlDiaryMapper;
import io.nicheblog.dreamdiary.domain.jrnl.diary.service.JrnlDiaryService;
import io.nicheblog.dreamdiary.domain.jrnl.diary.spec.JrnlDiarySpec;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * JrnlDiaryService
 * <pre>
 *  저널 일기 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("jrnlDiaryService")
@RequiredArgsConstructor
@Log4j2
public class JrnlDiaryServiceImpl
        implements JrnlDiaryService {

    @Getter
    private final JrnlDiaryRepository repository;
    @Getter
    private final JrnlDiarySpec spec;
    @Getter
    private final JrnlDiaryMapstruct mapstruct = JrnlDiaryMapstruct.INSTANCE;

    private final JrnlDiaryMapper jrnlDiaryMapper;
    private final ApplicationEventPublisherWrapper publisher;

    private final ApplicationContext context;
    private JrnlDiaryService getSelf() {
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
    @Cacheable(value="myJrnlDiaryList", key="T(io.nicheblog.dreamdiary.auth.security.util.AuthUtils).getLgnUserId() + \"_\" + #searchParam.hashCode()")
    public List<JrnlDiaryDto> getListDtoWithCache(final BaseSearchParam searchParam) throws Exception {
        searchParam.setRegstrId(AuthUtils.getLgnUserId());
        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 특정 년도의 중요 일기 목록 조회 :: 캐시 처리
     *
     * @param yy 조회할 년도
     * @return {@link List} -- 해당 년도의 중요 목록
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    @Override
    @Cacheable(value="myImprtcDiaryList", key="T(io.nicheblog.dreamdiary.auth.security.util.AuthUtils).getLgnUserId() + \"_\" + #yy")
    public List<JrnlDiaryDto> getImprtcDiaryList(final Integer yy) throws Exception {
        final JrnlDiarySearchParam searchParam = JrnlDiarySearchParam.builder().yy(yy).imprtcYn("Y").build();
        final List<JrnlDiaryDto> imprtcDiaryList = this.getSelf().getListDto(searchParam);
        Collections.sort(imprtcDiaryList);

        return imprtcDiaryList;
    }

    /**
     * 특정 태그의 관련 일기 목록 조회 :: 캐시 처리
     *
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 검색 결과 목록
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    @Override
    @Cacheable(value="myJrnlDiaryTagDtl", key="T(io.nicheblog.dreamdiary.auth.security.util.AuthUtils).getLgnUserId() + \"_\" + #searchParam.getTagNo()")
    public List<JrnlDiaryDto> jrnlDiaryTagDtl(final JrnlDiarySearchParam searchParam) throws Exception {
        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 등록 전처리. (override)
     *
     * @param registDto 등록할 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void preRegist(final JrnlDiaryDto registDto) throws Exception {
        // 인덱스(정렬순서) 처리
        final Integer lastIndex = repository.findLastIndexByJrnlDay(registDto.getJrnlDayNo()).orElse(0);
        registDto.setIdx(lastIndex + 1);
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void postRegist(final JrnlDiaryDto updatedDto) throws Exception {
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishEvent(new JrnlTagProcEvent(this, updatedDto.getClsfKey(), updatedDto.getYy(), updatedDto.getMnth(), updatedDto.tag));
        // 관련 캐시 삭제
        publisher.publishEvent(new JrnlCacheEvictEvent(this, JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_DIARY));
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void postModify(final JrnlDiaryDto updatedDto) throws Exception {
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishEvent(new JrnlTagProcEvent(this, updatedDto.getClsfKey(), updatedDto.getYy(), updatedDto.getMnth(), updatedDto.tag));
        // 관련 캐시 삭제
        publisher.publishEvent(new JrnlCacheEvictEvent(this, JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_DIARY));
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 식별자
     * @return {@link JrnlDiaryDto} -- 조회된 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    @Cacheable(value="myJrnlDiaryDtlDto", key="T(io.nicheblog.dreamdiary.auth.security.util.AuthUtils).getLgnUserId() + \"_\" + #key")
    public JrnlDiaryDto getDtlDtoWithCache(final Integer key) throws Exception {
        final JrnlDiaryDto retrieved = this.getSelf().getDtlDto(key);
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
    public void postDelete(final JrnlDiaryDto deletedDto) throws Exception {
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishEvent(new JrnlTagProcEvent(this, deletedDto.getClsfKey(), deletedDto.getYy(), deletedDto.getMnth()));
        // 관련 캐시 삭제
        publisher.publishEvent(new JrnlCacheEvictEvent(this, JrnlCacheEvictParam.of(deletedDto), ContentType.JRNL_DIARY));
    }

    /**
     * 삭제 데이터 조회
     *
     * @param key 삭제된 데이터의 키
     * @return {@link JrnlDiaryDto} -- 삭제된 데이터 Dto
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    @Transactional(readOnly = true)
    public JrnlDiaryDto getDeletedDtlDto(final Integer key) throws Exception {
        return jrnlDiaryMapper.getDeletedByPostNo(key);
    }
}