package io.nicheblog.dreamdiary.feature.jrnl.dream.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf.ContentType;
import io.nicheblog.dreamdiary.feature.clsf._shared.service.BaseClsfService;
import io.nicheblog.dreamdiary.feature.clsf.file.service.BaseMultipartWritableService;
import io.nicheblog.dreamdiary.feature.jrnl._shared.handler.JrnlCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.jrnl._shared.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.feature.jrnl.dream.entity.JrnlDreamEntity;
import io.nicheblog.dreamdiary.feature.jrnl.dream.mapstruct.JrnlDreamMapstruct;
import io.nicheblog.dreamdiary.feature.jrnl.dream.model.JrnlDreamDto;
import io.nicheblog.dreamdiary.feature.jrnl.dream.model.JrnlDreamPostDto;
import io.nicheblog.dreamdiary.feature.jrnl.dream.model.JrnlDreamSearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.dream.repository.jpa.JrnlDreamRepository;
import io.nicheblog.dreamdiary.feature.jrnl.dream.repository.mybatis.JrnlDreamMapper;
import io.nicheblog.dreamdiary.feature.jrnl.dream.spec.JrnlDreamSpec;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
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
public class JrnlDreamService
        implements BaseClsfService<JrnlDreamPostDto, JrnlDreamDto, Integer, JrnlDreamEntity>, BaseMultipartWritableService<JrnlDreamPostDto, JrnlDreamDto, Integer, JrnlDreamEntity> {

    @Getter
    private final JrnlDreamRepository repository;
    @Getter
    private final JrnlDreamSpec spec;
    @Getter
    private final JrnlDreamMapstruct mapstruct;
    @Getter
    private final JrnlDreamMapper mapper;

    public JrnlDreamMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public JrnlDreamMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final JrnlCacheEvictWorker jrnlCacheEvictWorker;

    private final ApplicationContext context;
    private JrnlDreamService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 사용자별 특정 년도의 중요 꿈 목록 조회 :: 캐시 처리
     *
     * @param userId 사용자 ID
     * @param searchParam JrnlDreamSearchParam
     * @return {@link List} -- 해당 년도의 중요 목록
     */
    @Cacheable(value="jrnlDreamYySumryStatedListByUser", key="new org.springframework.cache.interceptor.SimpleKey(#userId, #searchParam.toSummaryCacheKey())")
    public List<JrnlDreamDto> getSumryDreamListByUser(final String userId, final JrnlDreamSearchParam searchParam) throws Exception {
        searchParam.setRegstrId(AuthUtils.requireUserId(userId));
        final List<JrnlDreamDto> jrnlDreamYySumryStatedListByUser = this.getSelf().getListDto(searchParam);
        Collections.sort(jrnlDreamYySumryStatedListByUser);

        return jrnlDreamYySumryStatedListByUser;
    }

    public List<JrnlDreamDto> getListDtoByUser(final String userId, final JrnlDreamSearchParam searchParam) throws Exception {
        searchParam.setRegstrId(AuthUtils.requireUserId(userId));
        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 등록 전처리. (override)
     *
     * @param registDto 등록할 객체
     */
    @Override
    public void preRegist(final JrnlDreamPostDto registDto) throws Exception {
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
    public void postRegist(final JrnlDreamDto updatedDto) throws Exception {
        // 관련 캐시 삭제
        jrnlCacheEvictWorker.evictAfterCommit(JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_DREAM);
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 식별자
     * @return {@link JrnlDreamDto} -- 조회된 객체
     */
    @Cacheable(value="jrnlDreamDtlDtoByUser", key="new org.springframework.cache.interceptor.SimpleKey(#userId, #key)")
    public JrnlDreamDto getDtlDtoWithCacheByUser(final String userId, final Integer key) throws Exception {
        final JrnlDreamEntity retrievedEntity = this.getSelf().getDtlEntity(key);
        final JrnlDreamDto retrieved = mapstruct.toDto(retrievedEntity);
        // 권한 체크
        if (!retrieved.getIsRegstr(AuthUtils.requireUserId(userId))) throw new NotAuthorizedException(MessageUtils.getMessage("msg.rslt.access-not-authorized"));

        return retrieved;
    }

    /**
     * 수정 전처리. (override)
     *
     * @param modifyDto - 수정할 객체
     * @param modifyEntity - 수정할 객체
     */
    @Override
    public void preModify(final JrnlDreamPostDto modifyDto, final JrnlDreamEntity modifyEntity) throws Exception {
        if (!AuthUtils.isRegstr(modifyEntity.getRegstrId())) {
            throw new NotAuthorizedException(MessageUtils.getMessage("msg.rslt.access-not-authorized"));
        }
        final boolean isIdxChanged = !Objects.equals(modifyDto.getIdx(), modifyEntity.getIdx());
        modifyDto.setIsIdxChanged(isIdxChanged);
    }
    
    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final JrnlDreamPostDto postDto, final JrnlDreamDto updatedDto) throws Exception {
        // 인덱스 재조정
        if (updatedDto.getIsIdxChanged()) this.getSelf().reorderIdx(updatedDto);
        
        // 관련 캐시 삭제
        jrnlCacheEvictWorker.evictAfterCommit(JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_DREAM);
    }

    /**
     * 삭제 전처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void preDelete(final JrnlDreamDto deletedDto) throws Exception {
        if (!AuthUtils.isRegstr(deletedDto.getRegstrId())) {
            throw new NotAuthorizedException(MessageUtils.getMessage("msg.rslt.access-not-authorized"));
        }
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void postDelete(final JrnlDreamDto deletedDto) throws Exception {
        // 인덱스 재조정
        this.getSelf().reorderIdx(deletedDto);

        // 관련 캐시 삭제
        jrnlCacheEvictWorker.evictAfterCommit(JrnlCacheEvictParam.of(deletedDto), ContentType.JRNL_DREAM);
    }

    /**
     * 삭제 데이터 조회
     *
     * @param key 삭제된 데이터의 키
     * @return {@link JrnlDreamDto} -- 삭제된 데이터 Dto
     */
    @Transactional(readOnly = true)
    public JrnlDreamDto getDeletedDtlDto(final Integer key) throws Exception {
        final JrnlDreamDto deleted = mapper.getDeletedByPostNo(key);
        if (deleted == null) return null;
        if (!AuthUtils.isRegstr(deleted.getRegstrId())) {
            throw new NotAuthorizedException(MessageUtils.getMessage("msg.rslt.access-not-authorized"));
        }
        return deleted;
    }

    /**
     * 해당 그룹 전체를 idx = 1부터 다시 정렬한다.
     *
     * @param jrnlDayNo 정렬을 수행할 상위 키
     */
    @Transactional
    public void normalize(final Integer jrnlDayNo) {
        final List<JrnlDreamDto> list = mapper.findAllForReorder(jrnlDayNo);
        if (CollectionUtils.isEmpty(list)) return;

        int idx = 1;
        for (final JrnlDreamDto e : list) {
            e.setIdx(idx++);
            EhCacheUtils.evictUserCacheByKey("jrnlDreamDtlDtoByUser", e.getRegstrId(), e.getPostNo());
        }

        mapper.batchUpdateIdx(list);
    }

    /**
     * 대상 상위 키에 엔티티를 특정 위치에 삽입 후 재정렬한다.
     *
     * @param jrnlDayNo 정렬을 수행할 상위 키
     * @param postNo 게시물 PK
     * @param targetIdx 삽입할 목표 위치(1-based). null이면 맨 뒤에 삽입됨
     */
    @Transactional
    public void insert(final Integer jrnlDayNo, final Integer postNo, Integer targetIdx) throws Exception {
        final List<JrnlDreamDto> list = mapper.findAllForReorder(jrnlDayNo);

        // target 조회
        final JrnlDreamEntity targetEntity = findDtlEntity(postNo);
        final JrnlDreamDto target = mapstruct.toDto(targetEntity);
        if (target == null) return;

        // 혹시 이미 포함되어 있으면 제거
        list.removeIf(e -> Objects.equals(e.getPostNo(), postNo));

        // entryNo 변경
        target.setJrnlDayNo(jrnlDayNo);

        // targetIdx 보정 (upper bound)
        final int maxIdx = list.size() + 1;
        final int normalizedIdx = Math.min(targetIdx == null ? maxIdx : targetIdx, maxIdx);
        // 삽입 위치 계산
        int pos = normalizedIdx - 1;
        pos = Math.min(pos, list.size());
        list.add(pos, target);

        // idx 재정렬
        int idx = 1;
        for (final JrnlDreamDto e : list) {
            e.setIdx(idx++);
            EhCacheUtils.evictUserCacheByKey("jrnlDreamDtlDtoByUser", e.getRegstrId(), e.getPostNo());
        }

        mapper.batchUpdateIdx(list);
    }

    /**
     * 인덱스 변경시 관련 인덱스 업데이트
     *
     * @param updatedDto 업데이트된 객체
     */
    @Transactional
    public void reorderIdx(final JrnlDreamDto updatedDto) throws Exception {
        // 1단계: 현재 entry 그룹 정리 (기존 idx 값을 normalization하여 안정화)
        normalize(updatedDto.getJrnlDayNo());
        // 2단계: 해당 group에 새 위치로 target 삽입
        insert(updatedDto.getJrnlDayNo(), updatedDto.getPostNo(), updatedDto.getIdx());
    }

    /**
     * 주어진 {@link JrnlDayDto} 객체에 공휴일 및 주말 여부 정보를 설정한다.
     *
     * @param jrnlDream 공휴일 및 주말 정보를 설정할 대상 DTO
     * @param hldyMap 날짜(String: yyyy-MM-dd) → 공휴일 이름 목록 매핑 정보
     */
    private void setHldyInfo(final JrnlDreamDto jrnlDream, final Map<String, List<String>> hldyMap) throws Exception {
        if (jrnlDream == null || hldyMap == null) return;

        final String stdrdDt = jrnlDream.getStdrdDt();
        final boolean isHldy = hldyMap.containsKey(stdrdDt);
        final boolean isWeekend = DateUtils.isWeekend(stdrdDt);
        jrnlDream.setIsHldy(isHldy || isWeekend);
        if (isHldy) {
            final String concatHldyNm = String.join(", ", hldyMap.get(stdrdDt));
            jrnlDream.setHldyNm(concatHldyNm);
        }
    }
}
