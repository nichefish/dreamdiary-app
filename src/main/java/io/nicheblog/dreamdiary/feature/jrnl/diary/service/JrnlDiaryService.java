package io.nicheblog.dreamdiary.feature.jrnl.diary.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf._shared.service.BaseClsfService;
import io.nicheblog.dreamdiary.feature.clsf._shared.service.helper.BaseClsfHistoryHelper;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.file.service.BaseMultipartWritableService;
import io.nicheblog.dreamdiary.feature.clsf.history.HistoryType;
import io.nicheblog.dreamdiary.feature.jrnl._shared.handler.JrnlCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.jrnl._shared.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.feature.jrnl.diary.entity.JrnlDiaryEntity;
import io.nicheblog.dreamdiary.feature.jrnl.diary.mapstruct.JrnlDiaryMapstruct;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiaryPostDto;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiarySearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.diary.repository.jpa.JrnlDiaryRepository;
import io.nicheblog.dreamdiary.feature.jrnl.diary.repository.mybatis.JrnlDiaryMapper;
import io.nicheblog.dreamdiary.feature.jrnl.diary.spec.JrnlDiarySpec;
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
        implements BaseClsfService<JrnlDiaryPostDto, JrnlDiaryDto, Integer, JrnlDiaryEntity>, BaseMultipartWritableService<JrnlDiaryPostDto, JrnlDiaryDto, Integer, JrnlDiaryEntity> {

    @Getter
    private final JrnlDiaryRepository repository;
    @Getter
    private final JrnlDiarySpec spec;
    @Getter
    private final JrnlDiaryMapstruct mapstruct;
    @Getter
    private final JrnlDiaryMapper mapper;

    public JrnlDiaryMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public JrnlDiaryMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final JrnlCacheEvictWorker jrnlCacheEvictWorker;

    private final ApplicationContext context;
    private JrnlDiaryService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 사용자별 특정 년도의 일기 목록 조회 :: 캐시 처리
     *
     * @param userId String
     * @param searchParam JrnlDiarySearchParam
     * @return {@link List} -- 해당 년도의 중요 목록
     */
    @Cacheable(value="jrnlDiaryYySumryStatedListByUser", key="new org.springframework.cache.interceptor.SimpleKey(#userId, #searchParam.toSummaryCacheKey())")
    public List<JrnlDiaryDto> getSumryDiaryListByUser(final String userId, final JrnlDiarySearchParam searchParam) throws Exception {
        searchParam.setRegstrId(AuthUtils.requireUserId(userId));
        final List<JrnlDiaryDto> jrnlDiaryYySumryStatedListByUser = this.getSelf().getListDto(searchParam);
        Collections.sort(jrnlDiaryYySumryStatedListByUser);

        return jrnlDiaryYySumryStatedListByUser;
    }

    public List<JrnlDiaryDto> getListDtoByUser(final String userId, final JrnlDiarySearchParam searchParam) throws Exception {
        searchParam.setRegstrId(AuthUtils.requireUserId(userId));
        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 등록 전처리. (override)
     *
     * @param registDto 등록할 객체
     */
    @Override
    public void preRegist(final JrnlDiaryPostDto registDto) throws Exception {
        // 인덱스(정렬순서) 처리
        final Integer lastIndex = repository.findLastIndexByJrnlChapter(registDto.getJrnlChapterNo()).orElse(0);
        registDto.setIdx(lastIndex + 1);
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final JrnlDiaryDto updatedDto) throws Exception {
        // 관련 캐시 삭제
        jrnlCacheEvictWorker.evictAfterCommit(JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_DIARY);
    }

    /**
     * 수정 전처리. (override)
     *
     * @param modifyDto - 수정할 객체
     * @param modifyEntity - 수정할 객체
     */
    @Override
    public void preModify(final JrnlDiaryPostDto modifyDto, final JrnlDiaryEntity modifyEntity) throws Exception {
        if (!AuthUtils.isRegstr(modifyEntity.getRegstrId())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }

        final boolean isIdxChanged = !Objects.equals(modifyDto.getIdx(), modifyEntity.getIdx());
        modifyDto.setIsIdxChanged(isIdxChanged);
        final boolean isChapterChanged = !Objects.equals(modifyDto.getJrnlChapterNo(), modifyEntity.getJrnlChapter().getPostNo());
        modifyDto.setIsChapterChanged(isChapterChanged);
        if (isChapterChanged) {
            modifyDto.setPrevJrnlChapterNo(modifyEntity.getJrnlChapter().getPostNo());
        }
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final JrnlDiaryPostDto postDto, final JrnlDiaryDto updatedDto) throws Exception {
        // 인덱스 재조정 ('이동' 포함)
        if (postDto.getIsChapterChanged()) {
            this.getSelf().reorderWhenChapterChanged(postDto);
        } else if (postDto.getIsIdxChanged()) {
            this.getSelf().reorderIdx(postDto);
        }

        // 관련 캐시 삭제
        jrnlCacheEvictWorker.evictAfterCommit(JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_DIARY);
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 식별자
     * @return {@link JrnlDiaryDto} -- 조회된 객체
     */
    @Cacheable(value="jrnlDiaryDtlDtoByUser", key="new org.springframework.cache.interceptor.SimpleKey(#userId, #key)")
    public JrnlDiaryDto getDtlDtoWithCacheByUser(final String userId, final Integer key) throws Exception {
        final JrnlDiaryEntity retrievedEntity = this.getSelf().getDtlEntity(key);
        final JrnlDiaryDto retrieved = mapstruct.toDto(retrievedEntity);
        // 권한 체크
        if (!retrieved.getIsRegstr(AuthUtils.requireUserId(userId))) throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        return retrieved;
    }

    @Transactional
    public JrnlDiaryDto updtCn(
            final Integer key,
            final String updatedCn,
            final HistoryType historyType,
            final Integer fromHistoryNo
    ) throws Exception {
        final JrnlDiaryEntity restoreEntity = this.getSelf().getDtlEntity(key);
        final JrnlDiaryEntity historySnapshot = BaseClsfHistoryHelper.isHistoryModule(restoreEntity)
                ? restoreEntity.toBuilder().build()
                : null;

        restoreEntity.setCn(updatedCn);
        BaseClsfHistoryHelper.applyModifyHistory(historySnapshot, restoreEntity);

        final JrnlDiaryEntity updatedEntity = getRepository().saveAndFlush(restoreEntity);
        BaseClsfHistoryHelper.publishHistoryEventIfSupported(this, historySnapshot, updatedEntity, historyType, fromHistoryNo);

        final JrnlDiaryDto updatedDto = getReadMapstruct().toDto(updatedEntity);
        jrnlCacheEvictWorker.evictAfterCommit(JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_DIARY);
        return updatedDto;
    }

    /**
     * 삭제 전처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void preDelete(final JrnlDiaryDto deletedDto) throws Exception {
        if (!AuthUtils.isRegstr(deletedDto.getRegstrId())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void postDelete(final JrnlDiaryDto deletedDto) throws Exception {
        // 인덱스 재조정
        this.getSelf().normalize(deletedDto.getJrnlChapterNo());
        
        // 관련 캐시 삭제
        jrnlCacheEvictWorker.evictAfterCommit(JrnlCacheEvictParam.of(deletedDto), ContentType.JRNL_DIARY);
    }

    /**
     * 삭제 데이터 조회
     *
     * @param key 삭제된 데이터의 키
     * @return {@link JrnlDiaryDto} -- 삭제된 데이터 Dto
     */
    @Transactional(readOnly = true)
    public JrnlDiaryDto getDeletedDtlDto(final Integer key) throws Exception {
        final JrnlDiaryDto deleted = mapper.getDeletedByPostNo(key);
        if (deleted == null) return null;
        if (!AuthUtils.isRegstr(deleted.getRegstrId())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
        return deleted;
    }

    /**
     * 해당 그룹 전체를 idx = 1부터 다시 정렬한다.
     *
     * @param jrnlChapterNo 정렬을 수행할 상위 키
     */
    @Transactional
    public void normalize(final Integer jrnlChapterNo) {
        final List<JrnlDiaryDto> list = mapper.findAllForReorder(jrnlChapterNo);
        if (CollectionUtils.isEmpty(list)) return;

        int idx = 1;
        for (final JrnlDiaryDto e : list) {
            e.setIdx(idx++);
            EhCacheUtils.evictUserCacheByKey("jrnlDiaryDtlDtoByUser", e.getRegstrId(), e.getPostNo());
        }

        mapper.batchUpdateIdx(list);
    }

    /**
     * 대상 상위 키에 엔티티를 특정 위치에 삽입 후 재정렬한다.
     *
     * @param jrnlChapterNo 정렬을 수행할 상위 키
     * @param postNo 게시물 PK
     * @param targetIdx 삽입할 목표 위치(1-based). null이면 맨 뒤에 삽입됨
     */
    @Transactional
    public void insert(final Integer jrnlChapterNo, final Integer postNo, Integer targetIdx) throws Exception {
        final List<JrnlDiaryDto> list = mapper.findAllForReorder(jrnlChapterNo);

        // target 조회
        final JrnlDiaryEntity targetEntity = getDtlEntity(postNo);
        final JrnlDiaryDto target = mapstruct.toDto(targetEntity);
        if (target == null) return;

        // 혹시 이미 포함되어 있으면 제거
        list.removeIf(e -> Objects.equals(e.getPostNo(), postNo));

        // chapterNo 변경
        target.setJrnlChapterNo(jrnlChapterNo);

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
            EhCacheUtils.evictUserCacheByKey("jrnlDiaryDtlDtoByUser", e.getRegstrId(), e.getPostNo());
        }

        mapper.batchUpdateIdx(list);
    }

    /**
     * chapterNo가 바뀌었을 때 챕터 이동 + 정렬 처리
     *
     * @param updatedDto 업데이트된 객체
     */
    @Transactional
    public void reorderWhenChapterChanged(final JrnlDiaryPostDto updatedDto) throws Exception {
        // 1) 기존 chapter 그룹 정리 (삭제처리와 동일한 효과)
        normalize(updatedDto.getPrevJrnlChapterNo());
        // 2) 새 chapter 그룹에 삽입
        insert(updatedDto.getJrnlChapterNo(), updatedDto.getPostNo(), updatedDto.getIdx());
    }

    /**
     * 인덱스 변경시 관련 인덱스 업데이트
     *
     * @param updatedDto 업데이트된 객체
     */
    @Transactional
    public void reorderIdx(final JrnlDiaryPostDto updatedDto) throws Exception {
        // 1단계: 현재 chapter 그룹 정리 (기존 idx 값을 normalization하여 안정화)
        normalize(updatedDto.getJrnlChapterNo());
        // 2단계: 해당 group에 새 위치로 target 삽입
        insert(updatedDto.getJrnlChapterNo(), updatedDto.getPostNo(), updatedDto.getIdx());
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
}
