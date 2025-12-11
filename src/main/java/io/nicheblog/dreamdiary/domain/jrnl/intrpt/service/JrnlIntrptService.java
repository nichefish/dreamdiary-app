package io.nicheblog.dreamdiary.domain.jrnl.intrpt.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.domain.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.domain.jrnl.intrpt.entity.JrnlIntrptEntity;
import io.nicheblog.dreamdiary.domain.jrnl.intrpt.mapstruct.JrnlIntrptMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.intrpt.model.JrnlIntrptDto;
import io.nicheblog.dreamdiary.domain.jrnl.intrpt.model.JrnlIntrptSearchParam;
import io.nicheblog.dreamdiary.domain.jrnl.intrpt.repository.jpa.JrnlIntrptRepository;
import io.nicheblog.dreamdiary.domain.jrnl.intrpt.repository.mybatis.JrnlIntrptMapper;
import io.nicheblog.dreamdiary.domain.jrnl.intrpt.spec.JrnlIntrptSpec;
import io.nicheblog.dreamdiary.domain.jrnl.state.JrnlState;
import io.nicheblog.dreamdiary.extension.cache.event.JrnlCacheEvictEvent;
import io.nicheblog.dreamdiary.extension.cache.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.extension.cache.util.EhCacheUtils;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.extension.clsf.tag.event.JrnlTagProcEvent;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseClsfService;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * JrnlIntrptService
 * <pre>
 *  저널 일기 관리 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("jrnlIntrptService")
@RequiredArgsConstructor
@Log4j2
public class JrnlIntrptService
        implements BaseClsfService<JrnlIntrptDto, JrnlIntrptDto, Integer, JrnlIntrptEntity, JrnlIntrptRepository, JrnlIntrptSpec, JrnlIntrptMapstruct> {

    @Getter
    private final JrnlIntrptRepository repository;
    @Getter
    private final JrnlIntrptSpec spec;
    @Getter
    private final JrnlIntrptMapstruct mapstruct = JrnlIntrptMapstruct.INSTANCE;
    @Getter
    private final JrnlIntrptMapper mapper;
    
    private final ApplicationEventPublisherWrapper publisher;

    private final ApplicationContext context;
    private JrnlIntrptService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 목록 조회 (dto level) :: 캐시 처리
     *
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    @Cacheable(value="myJrnlIntrptList", key="T(io.nicheblog.dreamdiary.auth.security.util.AuthUtils).getLgnUserId() + \"_\" + #searchParam.hashCode()")
    public List<JrnlIntrptDto> getListDtoWithCache(final BaseSearchParam searchParam) throws Exception {
        searchParam.setRegstrId(AuthUtils.getLgnUserId());
        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 특정 년도의 중요 일기 목록 조회 :: 캐시 처리
     *
     * @param yy 조회할 년도
     * @return {@link List} -- 해당 년도의 중요 목록
     */
    @Cacheable(value="myImprtcIntrptList", key="T(io.nicheblog.dreamdiary.auth.security.util.AuthUtils).getLgnUserId() + \"_\" + #yy")
    public List<JrnlIntrptDto> getImprtcIntrptList(final Integer yy) throws Exception {
        final JrnlIntrptSearchParam searchParam = JrnlIntrptSearchParam.builder().yy(yy).imprtcYn("Y").build();
        final List<JrnlIntrptDto> imprtcIntrptList = this.getSelf().getListDto(searchParam);
        Collections.sort(imprtcIntrptList);

        return imprtcIntrptList;
    }

    /**
     * 특정 태그의 관련 일기 목록 조회 :: 캐시 처리
     *
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 검색 결과 목록
     */
    @Cacheable(value="myJrnlIntrptTagDtl", key="T(io.nicheblog.dreamdiary.auth.security.util.AuthUtils).getLgnUserId() + \"_\" + #searchParam.getTagNo()")
    @SuppressWarnings("unchecked")
    public List<JrnlIntrptDto> jrnlIntrptTagDtl(final JrnlIntrptSearchParam searchParam) throws Exception {
        List<JrnlIntrptDto> jrnlIntrptList = this.getSelf().getListDto(searchParam);
        // 공휴일 정보 세팅
        final Map<String, List<String>> hldyMap = (Map<String, List<String>>) EhCacheUtils.getObjectFromCache("hldyMap");
        for (JrnlIntrptDto jrnlIntrpt : jrnlIntrptList) {
            setHldyInfo(jrnlIntrpt, hldyMap);
        }

        return jrnlIntrptList;
    }

    /**
     * 등록 전처리. (override)
     *
     * @param registDto 등록할 객체
     */
    @Override
    public void preRegist(final JrnlIntrptDto registDto) throws Exception {
        // 인덱스(정렬순서) 처리
        final Integer lastIndex = repository.findLastIndexByJrnlDay(registDto.getJrnlDreamNo()).orElse(0);
        registDto.setIdx(lastIndex + 1);
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final JrnlIntrptDto updatedDto) throws Exception {
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishCustomEvent(new JrnlTagProcEvent(this, updatedDto.getClsfKey(), updatedDto.getYy(), updatedDto.getMnth(), updatedDto.tag));
        // 관련 캐시 삭제
        publisher.publishCustomEvent(new JrnlCacheEvictEvent(this, JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_INTRPT));
    }
    
    /**
     * 수정 전처리. (override)
     *
     * @param modifyDto - 수정할 객체
     * @param modifyEntity - 수정할 객체
     */
    @Override
    public void preModify(final JrnlIntrptDto modifyDto, final JrnlIntrptEntity modifyEntity) throws Exception {
        boolean isIdxChanged = !Objects.equals(modifyDto.getIdx(), modifyEntity.getIdx());
        modifyDto.setIsIdxChanged(isIdxChanged);
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final JrnlIntrptDto updatedDto) throws Exception {
        // 인덱스 재조정
        if (updatedDto.getIsIdxChanged()) this.getSelf().reorderIdx(updatedDto);

        // 태그 처리 :: 메인 로직과 분리
        publisher.publishCustomEvent(new JrnlTagProcEvent(this, updatedDto.getClsfKey(), updatedDto.getYy(), updatedDto.getMnth(), updatedDto.tag));
        // 관련 캐시 삭제
        publisher.publishCustomEvent(new JrnlCacheEvictEvent(this, JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_INTRPT));
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 식별자
     * @return {@link JrnlIntrptDto} -- 조회된 객체
     */
    @Cacheable(value="myJrnlIntrptDtlDto", key="T(io.nicheblog.dreamdiary.auth.security.util.AuthUtils).getLgnUserId() + \"_\" + #key")
    public JrnlIntrptDto getDtlDtoWithCache(final Integer key) throws Exception {
        final JrnlIntrptDto retrieved = this.getSelf().getDtlDto(key);
        // 권한 체크
        if (!retrieved.getIsRegstr()) throw new NotAuthorizedException(MessageUtils.getMessage("common.rslt.access-not-authorized"));
        return retrieved;
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void postDelete(final JrnlIntrptDto deletedDto) throws Exception {
        // 인덱스 재조정
        this.getSelf().reorderIdx(deletedDto);

        // 태그 처리 :: 메인 로직과 분리
        publisher.publishCustomEvent(new JrnlTagProcEvent(this, deletedDto.getClsfKey(), deletedDto.getYy(), deletedDto.getMnth()));
        // 관련 캐시 삭제
        publisher.publishCustomEvent(new JrnlCacheEvictEvent(this, JrnlCacheEvictParam.of(deletedDto), ContentType.JRNL_INTRPT));
    }

    /**
     * 삭제 데이터 조회
     *
     * @param key 삭제된 데이터의 키
     * @return {@link JrnlIntrptDto} -- 삭제된 데이터 Dto
     */
    @Transactional(readOnly = true)
    public JrnlIntrptDto getDeletedDtlDto(final Integer key) throws Exception {
        return mapper.getDeletedByPostNo(key);
    }
    
    /**
     * 해당 그룹 전체를 idx = 1부터 다시 정렬한다.
     */
    @Transactional
    public void normalize(final Integer jrnlDreamNo) {
        final List<JrnlIntrptDto> list = mapper.findAllForReorder(jrnlDreamNo);
        if (CollectionUtils.isEmpty(list) || list.size() == 1) return;

        int idx = 1;
        for (final JrnlIntrptDto e : list) {
            e.setIdx(idx++);
            EhCacheUtils.evictCache("myJrnlIntrptDtlDto", e.getPostNo());
        }

        mapper.batchUpdateIdx(list);
    }
    
    /**
     * 대상 상위 키에 엔티티를 특정 위치에 삽입 후 재정렬한다.
     *
     * @param jrnlDreamNo 정렬을 수행할 상위 키
     * @param postNo 게시물 PK
     * @param targetIdx 삽입할 목표 위치(1-based). null이면 맨 뒤에 삽입됨
     */
    @Transactional
    public void insert(final Integer jrnlDreamNo, final Integer postNo, Integer targetIdx) throws Exception {
        final List<JrnlIntrptDto> list = mapper.findAllForReorder(jrnlDreamNo);

        // target 조회
        final JrnlIntrptDto target = findDtlDto(postNo);
        if (target == null) return;

        // 혹시 이미 포함되어 있으면 제거
        list.removeIf(e -> Objects.equals(e.getPostNo(), postNo));

        // entryNo 변경
        target.setJrnlDreamNo(jrnlDreamNo);

        // targetIdx 보정 (upper bound)
        final int maxIdx = list.size() + 1;
        final int normalizedIdx = Math.min(targetIdx == null ? maxIdx : targetIdx, maxIdx);
        // 삽입 위치 계산
        int pos = normalizedIdx - 1;
        pos = Math.min(pos, list.size());
        list.add(pos, target);

        // idx 재정렬
        int idx = 1;
        for (final JrnlIntrptDto e : list) {
            e.setIdx(idx++);
            EhCacheUtils.evictCache("myJrnlIntrptDtlDto", e.getPostNo());
        }

        mapper.batchUpdateIdx(list);
    }

    /**
     * 인덱스 변경시 관련 인덱스 업데이트
     *
     * @param updatedDto 업데이트된 객체
     */
    @Transactional
    public void reorderIdx(final JrnlIntrptDto updatedDto) throws Exception {
        // 1단계: 현재 entry 그룹 정리 (기존 idx 값을 normalization하여 안정화)
        normalize(updatedDto.getJrnlDreamNo());
        // 2단계: 해당 group에 새 위치로 target 삽입
        insert(updatedDto.getJrnlDreamNo(), updatedDto.getPostNo(), updatedDto.getIdx());
    }

    /**
     * 주어진 {@link JrnlDayDto} 객체에 공휴일 및 주말 여부 정보를 설정한다.
     *
     * @param jrnlIntrpt 공휴일 및 주말 정보를 설정할 대상 DTO
     * @param hldyMap 날짜(String: yyyy-MM-dd) → 공휴일 이름 목록 매핑 정보
     */
    private void setHldyInfo(final JrnlIntrptDto jrnlIntrpt, final Map<String, List<String>> hldyMap) throws Exception {
        if (jrnlIntrpt == null || hldyMap == null) return;

        final String stdrdDt = jrnlIntrpt.getStdrdDt();
        final boolean isHldy = hldyMap.containsKey(stdrdDt);
        final boolean isWeekend = DateUtils.isWeekend(stdrdDt);
        jrnlIntrpt.setIsHldy(isHldy || isWeekend);
        if (isHldy) {
            final String concatHldyNm = String.join(", ", hldyMap.get(stdrdDt));
            jrnlIntrpt.setHldyNm(concatHldyNm);
        }
    }
    
    /**
     * collapse 상태를 설정한다.
     *
     * @param postNo 대상 게시물 PK
     * @param collapsedYn 접힘 상태(Y/N)
     * @return collapsedYn 반영 성공 여부를 담은 ServiceResponse
     */
    @Transactional
    @SuppressWarnings("unchecked")
    public ServiceResponse setCollapse(final Integer postNo, final String collapsedYn) throws Exception {
        final JrnlIntrptEntity entity = getDtlEntity(postNo);
        entity.setCollapsedYn(collapsedYn);
        final JrnlIntrptEntity updatedEntity = repository.save(entity);

        final Integer yy = updatedEntity.getJrnlDream().getJrnlDay().getYy();
        final Integer mnth = updatedEntity.getJrnlDream().getJrnlDay().getMnth();
        final String cacheKey = AuthUtils.getLgnUserId() + "_" + yy + "_" + mnth;

        final Map<Integer, JrnlState> intrptMap = (Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("myIntrptStateMap", cacheKey);
        if (intrptMap != null) {
            final JrnlState state = intrptMap.get(postNo);
            if (state != null) {
                state.setCollapsedYn(collapsedYn);
                EhCacheUtils.put("myIntrptStateMap", cacheKey, intrptMap);
            }
        }

        return ServiceResponse.builder()
                .rslt(true)
                .build();
    }
}