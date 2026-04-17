package io.nicheblog.dreamdiary.feature.journal.day.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.service.BaseAttachableService;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalStateMaps;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.feature.journal.day.mapstruct.JournalDayMapstruct;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDaySearchParam;
import io.nicheblog.dreamdiary.feature.journal.day.repository.jpa.JournalDayRepository;
import io.nicheblog.dreamdiary.feature.journal.day.repository.mybatis.JournalDayMapper;
import io.nicheblog.dreamdiary.feature.journal.day.service.helper.JournalDayStateMapHelper;
import io.nicheblog.dreamdiary.feature.journal.day.spec.JournalDaySpec;
import io.nicheblog.dreamdiary.feature.journal.day.type.JournalDatePrecision;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JournalDayService
 * <pre>
 *  저널 일자 관리 서비스 모듈
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalDayService
        implements BaseAttachableService<JournalDayDto, JournalDayDto, Integer, JournalDayEntity> {

    @Getter
    private final JournalDayRepository repository;
    @Getter
    private final JournalDaySpec spec;
    @Getter
    private final JournalDayMapstruct mapstruct;

    public JournalDayMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public JournalDayMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final JournalDayMapper journalDayMapper;
    private final JournalCacheEvictWorker journalCacheEvictWorker;

    private final ApplicationContext context;
    private JournalDayService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 사용자 년월 목록 조회 (dto level) :: 캐시 처리
     *
     * @param username 사용자 계정명
     * @param yy 년도
     * @param mnth 월
     * @return {@link List} -- 조회된 목록
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "journalDayYyMnthListByUser", key = "new org.springframework.cache.interceptor.SimpleKey(#username, #yy, #mnth)")
    public List<JournalDayDto> getCachedYyMnthListDtoByUser(final String username, final Integer yy, final Integer mnth) throws Exception {
        final String resolvedUsername = AuthUtils.requireUsername(username);
        final Map<String, Object> searchParamMap = new HashMap<>();
        searchParamMap.put("createdBy", resolvedUsername);
        searchParamMap.put("yy", yy);
        searchParamMap.put("mnth", mnth);
        searchParamMap.put("sort", "ASC");
        final List<JournalDayEntity> myJournalDayEntityList = this.getListEntity(searchParamMap);

        // 1) stateMap 만들기
        final JournalStateMaps maps = JournalDayStateMapHelper.makeJournalStateMaps(myJournalDayEntityList);
        // 2) stateMap 캐시에 저장
        final SimpleKey cacheKey = new SimpleKey(resolvedUsername, yy, mnth);
        EhCacheUtils.put("journalChapterStateMapByUser", cacheKey, maps.getChapterMap());
        EhCacheUtils.put("journalDiaryStateMapByUser", cacheKey, maps.getDiaryMap());
        EhCacheUtils.put("journalDreamStateMapByUser", cacheKey, maps.getDreamMap());
        EhCacheUtils.put("journalIntrptStateMapByUser", cacheKey, maps.getIntrptMap());

        return mapstruct.toDtoList(myJournalDayEntityList);
    }

    /**
     * 기준일 일자 목록 조회 (dto level)
     *
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    @Transactional(readOnly = true)
    public List<JournalDayDto> getJournalStdrdDaysByUser(final String username, final JournalDaySearchParam searchParam) throws Exception {
        if (searchParam == null) return List.of();

        searchParam.setCreatedBy(AuthUtils.requireUsername(username));
        searchParam.setSort("ASC");
        final List<JournalDayEntity> myJournalStdrdDayEntityList = this.getListEntity(searchParam);
        return mapstruct.toDtoList(myJournalStdrdDayEntityList);
    }

    /**
     * 사용자 주간 일자 목록 조회 (dto level) :: 캐시 처리
     *
     * @param username 사용자 계정명
     * @param weekStartDt 주 시작일
     * @return {@link List} -- 조회된 목록
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "journalDayWeeklyListByUser", key = "new org.springframework.cache.interceptor.SimpleKey(#username, #weekStartDt)")
    public List<JournalDayDto> getCachedWeeklyListDtoByUser(final String username, final String weekStartDt) throws Exception {
        final String resolvedUsername = AuthUtils.requireUsername(username);
        final Map<String, Object> searchParamMap = new HashMap<>();
        searchParamMap.put("createdBy", resolvedUsername);
        searchParamMap.put("weekStartDt", DateUtils.asDate(weekStartDt));
        searchParamMap.put("sort", "ASC");
        final List<JournalDayEntity> myJournalDayEntityList = this.getListEntity(searchParamMap);

        final JournalStateMaps maps = JournalDayStateMapHelper.makeJournalStateMaps(myJournalDayEntityList);
        final SimpleKey cacheKey = new SimpleKey(resolvedUsername, weekStartDt);
        EhCacheUtils.put("journalChapterWeeklyStateMapByUser", cacheKey, maps.getChapterMap());
        EhCacheUtils.put("journalDiaryWeeklyStateMapByUser", cacheKey, maps.getDiaryMap());
        EhCacheUtils.put("journalDreamWeeklyStateMapByUser", cacheKey, maps.getDreamMap());
        EhCacheUtils.put("journalIntrptWeeklyStateMapByUser", cacheKey, maps.getIntrptMap());

        return mapstruct.toDtoList(myJournalDayEntityList);
    }

    /**
     * 메타별 내 일자 목록 조회 (dto level)
     *
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    @Transactional(readOnly = true)
    public List<JournalDayDto> getListDtoByMetaIdAndUser(final String username, final JournalDaySearchParam searchParam) throws Exception {
        if (searchParam == null) return List.of();

        searchParam.setCreatedBy(AuthUtils.requireUsername(username));
        searchParam.setSort("DESC");
        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 태그별 내 일자 목록 조회 (dto level)
     *
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 검색 결과 목록
     */
    @Transactional(readOnly = true)
    public List<JournalDayDto> getListDtoByTagIdAndUser(final String username, final JournalDaySearchParam searchParam) throws Exception {
        if (searchParam == null) return List.of();

        searchParam.setCreatedBy(AuthUtils.requireUsername(username));
        searchParam.setSort("DESC");
        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param username 사용자 계정명
     * @param key 식별자
     * @return {@link JournalDayDto} -- 조회된 객체
     */
    @Cacheable(value = "journalDayDtlDtoByUser", key = "new org.springframework.cache.interceptor.SimpleKey(#username, #key)")
    public JournalDayDto getCachedDtlDtoByUser(final String username, final Integer key) throws Exception {
        AuthUtils.requireUsername(username);
        final JournalDayEntity retrievedEntity = this.getDtlEntity(key);
        final JournalDayDto retrieved = mapstruct.toDto(retrievedEntity);
        // 권한 체크
        if (!retrieved.getIsCreatedBy(username)) throw new NotAuthorizedException("msg.rslt.access-not-authorized");

        return retrieved;
    }

    /**
     * 중복 체크 (정상시 true / 중복시 false)
     *
     * @param journalDay {@link JournalDayDto} -- 중복 여부를 확인할 {@link JournalDayDto} 객체
     * @return {@link boolean} -- 정상 시 true, 중복 시 false 반환
     */
    @Transactional(readOnly = true)
    public boolean dupChckByUser(final String username, final JournalDayDto journalDay) throws Exception {
        if (journalDay.getJournalDatePrecision() != JournalDatePrecision.EXACT) return false;

        final Date journalDate = DateUtils.asDate(journalDay.getJournalDate());
        final String createdBy = AuthUtils.requireUsername(username);
        final Integer isDup = repository.countByJournalDate(journalDate, createdBy);

        return isDup > 0;
    }

    /**
     * 날짜 기준으로 중복(해당 데이터 존재)시 해당 키값 반환
     *
     * @param journalDay {@link JournalDayDto} -- 중복 여부를 확인할 {@link JournalDayDto} 객체
     * @return {@link Integer} -- 중복되는 경우 해당하는 키값 (게시글 번호)
     */
    @Transactional(readOnly = true)
    public Integer getDupKeyByUser(final String username, final JournalDayDto journalDay) throws Exception {
        final Date journalDate = DateUtils.asDate(journalDay.getJournalDate());
        final String createdBy = AuthUtils.requireUsername(username);
        final JournalDayEntity existingEntity = repository.findByJournalDate(journalDate, createdBy);

        return existingEntity.getId();
    }

    /**
     * 등록 전처리. (override)
     *
     * @param registDto 등록할 객체
     */
    @Override
    public void preRegist(final JournalDayDto registDto) throws Exception {
        // 기간 필드 세팅:: 메소드 분리
        this.setPeriodFields(registDto);
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final JournalDayDto updatedDto) throws Exception {
        // 관련 캐시 삭제
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(updatedDto), ContentType.JOURNAL_DAY);
    }

    /**
     * 수정 전처리. (override)
     *
     * @param modifyDto 수정할 객체 (dto)
     * @param modifyEntity 수정할 객체 (entity)
     */
    @Override
    public void preModify(final JournalDayDto modifyDto, final JournalDayEntity modifyEntity) throws Exception {
        if (!AuthUtils.isCreatedBy(modifyEntity.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
        modifyDto.setPrevWeekStartDt(DateUtils.asStr(modifyEntity.getWeekStartDt(), DatePtn.DATE));
        // 기간 필드 세팅:: 메소드 분리
        this.setPeriodFields(modifyDto);
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final JournalDayDto postDto, final JournalDayDto updatedDto) throws Exception {
        // 관련 캐시 삭제
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(postDto, updatedDto), ContentType.JOURNAL_DAY);
    }

    /**
     * 날짜 기반으로 기간 항목(연/월/주 시작일) 세팅 :: 메소드 분리
     *
     * @param journalDay 날짜 기반으로 기간 필드를 설정할 {@link JournalDayDto} 객체
     */
    public void setPeriodFields(final JournalDayDto journalDay) throws Exception {
        final String stdrdDt;
        journalDay.setYy(Integer.valueOf(journalDay.getJournalDate().substring(0, 4)));
        journalDay.setMnth(Integer.valueOf(journalDay.getJournalDate().substring(5, 7)));
        stdrdDt = journalDay.getJournalDate();
        journalDay.setWeekStartDt(DateUtils.getWeekStartDateStr(stdrdDt));
    }

    /**
     * 삭제 전처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void preDelete(final JournalDayDto deletedDto) throws Exception {
        if (!AuthUtils.isCreatedBy(deletedDto.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void postDelete(final JournalDayDto deletedDto) throws Exception {
        // 관련 캐시 삭제
        journalCacheEvictWorker.evictAfterCommit(JournalCacheEvictParam.of(deletedDto), ContentType.JOURNAL_DAY);
    }

    /**
     * 삭제 데이터 조회
     *
     * @param key 삭제된 데이터의 키
     * @return {@link JournalDayDto} -- 삭제된 데이터 Dto
     */
    @Transactional(readOnly = true)
    public JournalDayDto getDeletedDtlDto(final Integer key) throws Exception {
        final JournalDayDto deleted = journalDayMapper.getDeletedById(key);
        if (deleted == null) return null;
        if (!AuthUtils.isCreatedBy(deleted.getCreatedBy())) {
            throw new NotAuthorizedException("msg.rslt.access-not-authorized");
        }
        return deleted;
    }
}


