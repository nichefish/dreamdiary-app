package io.nicheblog.dreamdiary.domain.jrnl.dream.service.impl;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.domain.jrnl.dream.mapstruct.JrnlDreamMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.dream.model.JrnlDreamDto;
import io.nicheblog.dreamdiary.domain.jrnl.dream.model.JrnlDreamSearchParam;
import io.nicheblog.dreamdiary.domain.jrnl.dream.repository.jpa.JrnlDreamRepository;
import io.nicheblog.dreamdiary.domain.jrnl.dream.repository.mybatis.JrnlDreamMapper;
import io.nicheblog.dreamdiary.domain.jrnl.dream.service.JrnlDreamService;
import io.nicheblog.dreamdiary.domain.jrnl.dream.spec.JrnlDreamSpec;
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
 * JrnlDreamService
 * <pre>
 *  저널 꿈 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("jrnlDreamService")
@RequiredArgsConstructor
@Log4j2
public class JrnlDreamServiceImpl
        implements JrnlDreamService {

    @Getter
    private final JrnlDreamRepository repository;
    @Getter
    private final JrnlDreamSpec spec;
    @Getter
    private final JrnlDreamMapstruct mapstruct = JrnlDreamMapstruct.INSTANCE;

    private final JrnlDreamMapper jrnlDreamMapper;
    private final ApplicationEventPublisherWrapper publisher;

    private final ApplicationContext context;
    private JrnlDreamService getSelf() {
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
    @Cacheable(value="myJrnlDreamList", key="T(io.nicheblog.dreamdiary.auth.security.util.AuthUtils).getLgnUserId() + \"_\" + #searchParam.hashCode()")
    public List<JrnlDreamDto> getListDtoWithCache(final BaseSearchParam searchParam) throws Exception {
        searchParam.setRegstrId(AuthUtils.getLgnUserId());
        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 특정 년도의 중요 꿈 목록 조회 :: 캐시 처리
     *
     * @param yy 조회할 년도
     * @return {@link List} -- 해당 년도의 중요 목록
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    @Override
    @Cacheable(value="myImprtcDreamList", key="T(io.nicheblog.dreamdiary.auth.security.util.AuthUtils).getLgnUserId() + \"_\" + #yy")
    public List<JrnlDreamDto> getImprtcDreamList(final Integer yy) throws Exception {
        final JrnlDreamSearchParam searchParam = JrnlDreamSearchParam.builder().yy(yy).imprtcYn("Y").build();
        final List<JrnlDreamDto> imprtcDreamList = this.getSelf().getListDto(searchParam);
        Collections.sort(imprtcDreamList);

        return imprtcDreamList;
    }

    /**
     * 특정 태그의 관련 꿈 목록 조회 :: 캐시 처리
     *
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 검색 결과 목록
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    @Override
    @Cacheable(value="myJrnlDreamTagDtl", key="T(io.nicheblog.dreamdiary.auth.security.util.AuthUtils).getLgnUserId() + \"_\" + #searchParam.getTagNo()")
    public List<JrnlDreamDto> jrnlDreamTagDtl(final JrnlDreamSearchParam searchParam) throws Exception {
        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 등록 전처리. (override)
     *
     * @param registDto 등록할 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void preRegist(final JrnlDreamDto registDto) throws Exception {
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
    public void postRegist(final JrnlDreamDto updatedDto) throws Exception {
        // 관련 캐시 삭제
        publisher.publishEvent(new JrnlCacheEvictEvent(this, JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_DREAM));
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishEvent(new JrnlTagProcEvent(this, updatedDto.getClsfKey(), updatedDto.getYy(), updatedDto.getMnth(), updatedDto.tag));
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 식별자
     * @return {@link JrnlDreamDto} -- 조회된 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    @Cacheable(value="myJrnlDreamDtlDto", key="T(io.nicheblog.dreamdiary.auth.security.util.AuthUtils).getLgnUserId() + \"_\" + #key")
    public JrnlDreamDto getDtlDtoWithCache(final Integer key) throws Exception {
        final JrnlDreamDto retrieved = this.getSelf().getDtlDto(key);
        // 권한 체크
        if (!retrieved.getIsRegstr()) throw new NotAuthorizedException(MessageUtils.getMessage("common.rslt.access-not-authorized"));
        return retrieved;
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void postModify(final JrnlDreamDto updatedDto) throws Exception {
        // 관련 캐시 삭제
        publisher.publishEvent(new JrnlCacheEvictEvent(this, JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_DREAM));
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishEvent(new JrnlTagProcEvent(this, updatedDto.getClsfKey(), updatedDto.getYy(), updatedDto.getMnth(), updatedDto.tag));
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void postDelete(final JrnlDreamDto deletedDto) throws Exception {
        // 관련 캐시 삭제
        publisher.publishEvent(new JrnlCacheEvictEvent(this, JrnlCacheEvictParam.of(deletedDto), ContentType.JRNL_DREAM));
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishEvent(new JrnlTagProcEvent(this, deletedDto.getClsfKey(), deletedDto.getYy(), deletedDto.getMnth()));
    }

    /**
     * 삭제 데이터 조회
     *
     * @param key 삭제된 데이터의 키
     * @return {@link JrnlDreamDto} -- 삭제된 데이터 Dto
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    @Transactional(readOnly = true)
    public JrnlDreamDto getDeletedDtlDto(final Integer key) throws Exception {
        return jrnlDreamMapper.getDeletedByPostNo(key);
    }
}