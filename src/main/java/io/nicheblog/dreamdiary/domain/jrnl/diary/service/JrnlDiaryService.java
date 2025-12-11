package io.nicheblog.dreamdiary.domain.jrnl.diary.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.domain.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryEntity;
import io.nicheblog.dreamdiary.domain.jrnl.diary.mapstruct.JrnlDiaryMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiarySearchParam;
import io.nicheblog.dreamdiary.domain.jrnl.diary.repository.jpa.JrnlDiaryRepository;
import io.nicheblog.dreamdiary.domain.jrnl.diary.repository.mybatis.JrnlDiaryMapper;
import io.nicheblog.dreamdiary.domain.jrnl.diary.spec.JrnlDiarySpec;
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
public class JrnlDiaryService
        implements BaseClsfService<JrnlDiaryDto, JrnlDiaryDto, Integer, JrnlDiaryEntity, JrnlDiaryRepository, JrnlDiarySpec, JrnlDiaryMapstruct> {

    @Getter
    private final JrnlDiaryRepository repository;
    @Getter
    private final JrnlDiarySpec spec;
    @Getter
    private final JrnlDiaryMapstruct mapstruct = JrnlDiaryMapstruct.INSTANCE;
    @Getter
    private final JrnlDiaryMapper mapper;

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
     */
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
     */
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
     */
    @Cacheable(value="myJrnlDiaryTagDtl", key="T(io.nicheblog.dreamdiary.auth.security.util.AuthUtils).getLgnUserId() + \"_\" + #searchParam.getTagNo()")
    @SuppressWarnings("unchecked")
    public List<JrnlDiaryDto> jrnlDiaryTagDtl(final JrnlDiarySearchParam searchParam) throws Exception {
        List<JrnlDiaryDto> jrnlDiaryList = this.getSelf().getListDto(searchParam);
        // 공휴일 정보 세팅
        final Map<String, List<String>> hldyMap = (Map<String, List<String>>) EhCacheUtils.getObjectFromCache("hldyMap");
        for (JrnlDiaryDto jrnlDiary : jrnlDiaryList) {
            setHldyInfo(jrnlDiary, hldyMap);
        }

        return jrnlDiaryList;
    }

    /**
     * 등록 전처리. (override)
     *
     * @param registDto 등록할 객체
     */
    @Override
    public void preRegist(final JrnlDiaryDto registDto) throws Exception {
        // 인덱스(정렬순서) 처리
        final Integer lastIndex = repository.findLastIndexByJrnlDay(registDto.getJrnlEntryNo()).orElse(0);
        registDto.setIdx(lastIndex + 1);
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final JrnlDiaryDto updatedDto) throws Exception {
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishCustomEvent(new JrnlTagProcEvent(this, updatedDto.getClsfKey(), updatedDto.getYy(), updatedDto.getMnth(), updatedDto.tag));
        // 관련 캐시 삭제
        publisher.publishCustomEvent(new JrnlCacheEvictEvent(this, JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_DIARY));
    }

    /**
     * 수정 전처리. (override)
     *
     * @param modifyDto - 수정할 객체
     * @param modifyEntity - 수정할 객체
     */
    @Override
    public void preModify(final JrnlDiaryDto modifyDto, final JrnlDiaryEntity modifyEntity) throws Exception {
        boolean isIdxChanged = !Objects.equals(modifyDto.getIdx(), modifyEntity.getIdx());
        modifyDto.setIsIdxChanged(isIdxChanged);
        boolean isEntryChanged = !Objects.equals(modifyDto.getJrnlEntryNo(), modifyEntity.getJrnlEntryNo());
        modifyDto.setIsEntryChanged(isEntryChanged);
        if (isEntryChanged) {
            modifyDto.setPrevJrnlEntryNo(modifyEntity.getJrnlEntryNo());
        }
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final JrnlDiaryDto updatedDto) throws Exception {
        // 인덱스 재조정 ('이동' 포함)
        if (updatedDto.getIsEntryChanged()) {
            this.getSelf().reorderWhenEntryChanged(updatedDto);
        } else if (updatedDto.getIsIdxChanged()) {
            this.getSelf().reorderIdx(updatedDto);
        }

        // 태그 처리 :: 메인 로직과 분리
        publisher.publishCustomEvent(new JrnlTagProcEvent(this, updatedDto.getClsfKey(), updatedDto.getYy(), updatedDto.getMnth(), updatedDto.tag));
        // 관련 캐시 삭제
        publisher.publishCustomEvent(new JrnlCacheEvictEvent(this, JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_DIARY));
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 식별자
     * @return {@link JrnlDiaryDto} -- 조회된 객체
     */
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
     */
    @Override
    public void postDelete(final JrnlDiaryDto deletedDto) throws Exception {
        // 인덱스 재조정
        this.getSelf().reorderIdx(deletedDto);

        // 태그 처리 :: 메인 로직과 분리
        publisher.publishCustomEvent(new JrnlTagProcEvent(this, deletedDto.getClsfKey(), deletedDto.getYy(), deletedDto.getMnth()));
        // 관련 캐시 삭제
        publisher.publishCustomEvent(new JrnlCacheEvictEvent(this, JrnlCacheEvictParam.of(deletedDto), ContentType.JRNL_DIARY));
    }

    /**
     * 삭제 데이터 조회
     *
     * @param key 삭제된 데이터의 키
     * @return {@link JrnlDiaryDto} -- 삭제된 데이터 Dto
     */
    @Transactional(readOnly = true)
    public JrnlDiaryDto getDeletedDtlDto(final Integer key) throws Exception {
        return mapper.getDeletedByPostNo(key);
    }

    /**
     * 해당 그룹 전체를 idx = 1부터 다시 정렬한다.
     *
     * @param jrnlEntryNo 정렬을 수행할 상위 키
     */
    @Transactional
    public void normalize(final Integer jrnlEntryNo) {
        final List<JrnlDiaryDto> list = mapper.findAllForReorder(jrnlEntryNo);
        if (CollectionUtils.isEmpty(list) || list.size() == 1) return;

        int idx = 1;
        for (final JrnlDiaryDto e : list) {
            e.setIdx(idx++);
            EhCacheUtils.evictCache("myJrnlDiaryDtlDto", e.getPostNo());
        }

        mapper.batchUpdateIdx(list);
    }

    /**
     * 대상 상위 키에 엔티티를 특정 위치에 삽입 후 재정렬한다.
     *
     * @param jrnlEntryNo 정렬을 수행할 상위 키
     * @param postNo 게시물 PK
     * @param targetIdx 삽입할 목표 위치(1-based). null이면 맨 뒤에 삽입됨
     */
    @Transactional
    public void insert(final Integer jrnlEntryNo, final Integer postNo, Integer targetIdx) throws Exception {
        final List<JrnlDiaryDto> list = mapper.findAllForReorder(jrnlEntryNo);

        // target 조회
        final JrnlDiaryDto target = findDtlDto(postNo);
        if (target == null) return;

        // 혹시 이미 포함되어 있으면 제거
        list.removeIf(e -> Objects.equals(e.getPostNo(), postNo));

        // entryNo 변경
        target.setJrnlEntryNo(jrnlEntryNo);

        // targetIdx 보정 (upper bound)
        final int maxIdx = list.size() + 1;
        final int normalizedIdx = Math.min(targetIdx == null ? maxIdx : targetIdx, maxIdx);
        // 삽입 위치 계산
        int pos = normalizedIdx - 1;
        pos = Math.min(pos, list.size());
        list.add(pos, target);

        // idx 재정렬
        int idx = 1;
        for (final JrnlDiaryDto e : list) {
            e.setIdx(idx++);
            EhCacheUtils.evictCache("myJrnlDiaryDtlDto", e.getPostNo());
        }

        mapper.batchUpdateIdx(list);
    }

    /**
     * entryNo가 바뀌었을 때 엔트리 이동 + 정렬 처리
     *
     * @param updatedDto 업데이트된 객체
     */
    @Transactional
    public void reorderWhenEntryChanged(final JrnlDiaryDto updatedDto) throws Exception {
        // 1) 기존 entry 그룹 정리 (삭제처리와 동일한 효과)
        normalize(updatedDto.getPrevJrnlEntryNo());
        // 2) 새 entry 그룹에 삽입
        insert(updatedDto.getJrnlEntryNo(), updatedDto.getPostNo(), updatedDto.getIdx());
    }

    /**
     * 인덱스 변경시 관련 인덱스 업데이트
     *
     * @param updatedDto 업데이트된 객체
     */
    @Transactional
    public void reorderIdx(final JrnlDiaryDto updatedDto) throws Exception {
        // 1단계: 현재 entry 그룹 정리 (기존 idx 값을 normalization하여 안정화)
        normalize(updatedDto.getJrnlEntryNo());
        // 2단계: 해당 group에 새 위치로 target 삽입
        insert(updatedDto.getJrnlEntryNo(), updatedDto.getPostNo(), updatedDto.getIdx());
    }

    /**
     * 주어진 {@link JrnlDayDto} 객체에 공휴일 및 주말 여부 정보를 설정한다.
     *
     * @param jrnlDiary 공휴일 및 주말 정보를 설정할 대상 DTO
     * @param hldyMap 날짜(String: yyyy-MM-dd) → 공휴일 이름 목록 매핑 정보
     */
    private void setHldyInfo(final JrnlDiaryDto jrnlDiary, final Map<String, List<String>> hldyMap) throws Exception {
        if (jrnlDiary == null || hldyMap == null) return;

        final String stdrdDt = jrnlDiary.getStdrdDt();
        final boolean isHldy = hldyMap.containsKey(stdrdDt);
        final boolean isWeekend = DateUtils.isWeekend(stdrdDt);
        jrnlDiary.setIsHldy(isHldy || isWeekend);
        if (isHldy) {
            final String concatHldyNm = String.join(", ", hldyMap.get(stdrdDt));
            jrnlDiary.setHldyNm(concatHldyNm);
        }
    }

    /**
     * collapse 상태를 설정한다.
     *
     * @param postNo 대상 게시물 PK
     * @param collapseYn 접힘 상태(Y/N)
     * @return collapseYn 반영 성공 여부를 담은 ServiceResponse
     */
    @Transactional
    @SuppressWarnings("unchecked")
    public ServiceResponse setCollapse(final Integer postNo, final String collapseYn) throws Exception {
        final JrnlDiaryEntity entity = getDtlEntity(postNo);
        entity.setCollapseYn(collapseYn);
        final JrnlDiaryEntity updatedEntity = repository.save(entity);

        final Integer yy = updatedEntity.getJrnlEntry().getJrnlDay().getYy();
        final Integer mnth = updatedEntity.getJrnlEntry().getJrnlDay().getMnth();
        final String cacheKey = AuthUtils.getLgnUserId() + "_" + yy + "_" + mnth;

        final Map<Integer, JrnlState> diaryMap = (Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("myDiaryStateMap", cacheKey);
        if (diaryMap != null) {
            final JrnlState state = diaryMap.get(postNo);
            if (state != null) {
                state.setCollapseYn(collapseYn);
                EhCacheUtils.put("myDiaryStateMap", cacheKey, diaryMap);
            }
        }

        return ServiceResponse.builder()
                .rslt(true)
                .build();
    }

    /**
     * resolved 상태를 설정한다.
     *
     * @param postNo 대상 게시물 PK
     * @param resolvedYn 접힘 상태(Y/N)
     * @return collapseYn 반영 성공 여부를 담은 ServiceResponse
     */
    @Transactional
    @SuppressWarnings("unchecked")
    public ServiceResponse resolve(final Integer postNo, final String resolvedYn) throws Exception {
        final JrnlDiaryEntity entity = getDtlEntity(postNo);
        entity.setResolvedYn(resolvedYn);
        if ("Y".equals(resolvedYn)) entity.setCollapseYn("Y");
        final JrnlDiaryEntity updatedEntity = repository.save(entity);

        final Integer yy = updatedEntity.getJrnlEntry().getJrnlDay().getYy();
        final Integer mnth = updatedEntity.getJrnlEntry().getJrnlDay().getMnth();
        final String cacheKey = AuthUtils.getLgnUserId() + "_" + yy + "_" + mnth;

        final Map<Integer, JrnlState> diaryMap = (Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("myDiaryStateMap", cacheKey);
        if (diaryMap != null) {
            final JrnlState state = diaryMap.get(postNo);
            if (state != null) {
                state.setResolvedYn(resolvedYn);
                if ("Y".equals(resolvedYn)) state.setCollapseYn("Y");
                EhCacheUtils.put("myDiaryStateMap", cacheKey, diaryMap);
            }
        }

        return ServiceResponse.builder()
                .rslt(true)
                .build();
    }

    /**
     * resolved 상태를 설정한다.
     *
     * @param postNo 대상 게시물 PK
     * @param imprtcYn 접힘 상태(Y/N)
     * @return collapseYn 반영 성공 여부를 담은 ServiceResponse
     */
    @Transactional
    @SuppressWarnings("unchecked")
    public ServiceResponse imprtc(final Integer postNo, final String imprtcYn) throws Exception {
        final JrnlDiaryEntity entity = getDtlEntity(postNo);
        entity.setImprtcYn(imprtcYn);
        final JrnlDiaryEntity updatedEntity = repository.save(entity);

        final Integer yy = updatedEntity.getJrnlEntry().getJrnlDay().getYy();
        final Integer mnth = updatedEntity.getJrnlEntry().getJrnlDay().getMnth();
        final String cacheKey = AuthUtils.getLgnUserId() + "_" + yy + "_" + mnth;

        final Map<Integer, JrnlState> diaryMap = (Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("myDiaryStateMap", cacheKey);
        if (diaryMap != null) {
            final JrnlState state = diaryMap.get(postNo);
            if (state != null) {
                state.setImprtcYn(imprtcYn);
                EhCacheUtils.put("myDiaryStateMap", cacheKey, diaryMap);
            }
        }

        return ServiceResponse.builder()
                .rslt(true)
                .build();
    }
}