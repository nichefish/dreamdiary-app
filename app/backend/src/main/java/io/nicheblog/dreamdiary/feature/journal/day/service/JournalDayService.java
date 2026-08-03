package io.nicheblog.dreamdiary.feature.journal.day.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.service.BaseAttachableService;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.service.LifecycleService;
import io.nicheblog.dreamdiary.feature.attachable.state.StateKey;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.journal._shared.lifecycle.JournalLifecycleCacheRegistry;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalState;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalStateCacheRegistry;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalStateMaps;
import io.nicheblog.dreamdiary.feature.journal.reflection.entity.JournalReflectionEntity;
import io.nicheblog.dreamdiary.feature.journal.reflection.repository.jpa.JournalReflectionRepository;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.feature.journal.day.mapstruct.JournalDayMapstruct;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDaySearchParam;
import io.nicheblog.dreamdiary.feature.journal.day.repository.jpa.JournalDayRepository;
import io.nicheblog.dreamdiary.feature.journal.day.repository.mybatis.JournalDayMapper;
import io.nicheblog.dreamdiary.feature.journal.day.service.helper.JournalDayStateMapHelper;
import io.nicheblog.dreamdiary.feature.journal.day.spec.JournalDaySpec;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.global.exception.BusinessException;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final LifecycleService lifecycleService;
    private final JournalDayBootstrapService journalDayBootstrapService;
    private final JournalReflectionRepository journalReflectionRepository;

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
        final Map<Integer, String> diaryLifecycleMap = this.getEntryLifecycleMap(ContentType.JOURNAL_DIARY, myJournalDayEntityList);
        final Map<Integer, String> dreamLifecycleMap = this.getEntryLifecycleMap(ContentType.JOURNAL_DREAM, myJournalDayEntityList);
        final ReflectionStateMaps reflectionMaps = this.buildReflectionStateMaps(myJournalDayEntityList);
        // 2) stateMap 캐시에 저장
        final SimpleKey cacheKey = new SimpleKey(resolvedUsername, yy, mnth);
        EhCacheUtils.put(JournalStateCacheRegistry.monthlyMapCacheName(ContentType.JOURNAL_CHAPTER), cacheKey, maps.getChapterMap());
        EhCacheUtils.put(JournalStateCacheRegistry.monthlyMapCacheName(ContentType.JOURNAL_DIARY), cacheKey, maps.getDiaryMap());
        EhCacheUtils.put(JournalStateCacheRegistry.monthlyMapCacheName(ContentType.JOURNAL_DREAM), cacheKey, maps.getDreamMap());
        EhCacheUtils.put(JournalStateCacheRegistry.monthlyMapCacheName(ContentType.JOURNAL_REFLECTION), cacheKey, reflectionMaps.stateMap());
        EhCacheUtils.put(JournalLifecycleCacheRegistry.monthlyMapCacheName(ContentType.JOURNAL_DIARY), cacheKey, diaryLifecycleMap);
        EhCacheUtils.put(JournalLifecycleCacheRegistry.monthlyMapCacheName(ContentType.JOURNAL_DREAM), cacheKey, dreamLifecycleMap);
        EhCacheUtils.put(JournalLifecycleCacheRegistry.monthlyMapCacheName(ContentType.JOURNAL_REFLECTION), cacheKey, reflectionMaps.lifecycleMap());

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
        final Map<Integer, String> diaryLifecycleMap = this.getEntryLifecycleMap(ContentType.JOURNAL_DIARY, myJournalDayEntityList);
        final Map<Integer, String> dreamLifecycleMap = this.getEntryLifecycleMap(ContentType.JOURNAL_DREAM, myJournalDayEntityList);
        final ReflectionStateMaps reflectionMaps = this.buildReflectionStateMaps(myJournalDayEntityList);
        final SimpleKey cacheKey = new SimpleKey(resolvedUsername, weekStartDt);
        EhCacheUtils.put(JournalStateCacheRegistry.weeklyMapCacheName(ContentType.JOURNAL_CHAPTER), cacheKey, maps.getChapterMap());
        EhCacheUtils.put(JournalStateCacheRegistry.weeklyMapCacheName(ContentType.JOURNAL_DIARY), cacheKey, maps.getDiaryMap());
        EhCacheUtils.put(JournalStateCacheRegistry.weeklyMapCacheName(ContentType.JOURNAL_DREAM), cacheKey, maps.getDreamMap());
        EhCacheUtils.put(JournalStateCacheRegistry.weeklyMapCacheName(ContentType.JOURNAL_REFLECTION), cacheKey, reflectionMaps.stateMap());
        EhCacheUtils.put(JournalLifecycleCacheRegistry.weeklyMapCacheName(ContentType.JOURNAL_DIARY), cacheKey, diaryLifecycleMap);
        EhCacheUtils.put(JournalLifecycleCacheRegistry.weeklyMapCacheName(ContentType.JOURNAL_DREAM), cacheKey, dreamLifecycleMap);
        EhCacheUtils.put(JournalLifecycleCacheRegistry.weeklyMapCacheName(ContentType.JOURNAL_REFLECTION), cacheKey, reflectionMaps.lifecycleMap());

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
        if (!retrieved.getIsCreatedBy(username)) throw new NotAuthorizedException("common.result.access-not-authorized");

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
        if (StringUtils.isBlank(journalDay.getJournalDate())) return false;

        final LocalDate journalDate = DateUtils.asLocalDate(journalDay.getJournalDate());
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
        final LocalDate journalDate = DateUtils.asLocalDate(journalDay.getJournalDate());
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
        final String username = AuthUtils.getLoginUsername();
        if (StringUtils.isNotBlank(username) && this.dupChckByUser(username, registDto)) {
            throw new IllegalStateException("journal.day.duplicate");
        }
        // 기간 필드 세팅:: 메소드 분리
        this.setPeriodFields(registDto);
        registDto.setDiaryResolvedYn(normalizeYn(registDto.getDiaryResolvedYn()));
        registDto.setDreamResolvedYn(normalizeYn(registDto.getDreamResolvedYn()));
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final JournalDayDto updatedDto) throws Exception {
        journalDayBootstrapService.ensureDefaultSummaryDiary(updatedDto.getId());
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
            throw new NotAuthorizedException("common.result.access-not-authorized");
        }
        modifyDto.setPrevWeekStartDt(DateUtils.asStr(modifyEntity.getWeekStartDt(), DatePtn.DATE));
        // 기간 필드 세팅:: 메소드 분리
        this.setPeriodFields(modifyDto);
        modifyDto.setDiaryResolvedYn(normalizeYn(modifyDto.getDiaryResolvedYn()));
        modifyDto.setDreamResolvedYn(normalizeYn(modifyDto.getDreamResolvedYn()));
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
            throw new NotAuthorizedException("common.result.access-not-authorized");
        }
        if ("Y".equals(normalizeYn(deletedDto.getDiaryResolvedYn()))
                || "Y".equals(normalizeYn(deletedDto.getDreamResolvedYn()))) {
            throw new BusinessException("journal.day.resolved-delete-locked");
        }
    }

    /**
     * 일자 축별 완결 플래그만 갱신한다. 비어 있지 않은 파라미터만 반영한다.
     *
     * @param id 저널 일자 ID
     * @param diaryResolvedYn 일기 축 완결 (선택)
     * @param dreamResolvedYn 꿈 축 완결 (선택)
     * @return 갱신 결과
     */
    @Transactional
    public ServiceResponse updateResolvedFlags(
            final Integer id,
            final String diaryResolvedYn,
            final String dreamResolvedYn
    ) throws Exception {
        final JournalDayEntity entity = this.getDtlEntity(id);
        if (!AuthUtils.isCreatedBy(entity.getCreatedBy())) {
            throw new NotAuthorizedException("common.result.access-not-authorized");
        }

        final JournalDayDto postDto = mapstruct.toDto(entity);
        if (StringUtils.isNotBlank(diaryResolvedYn)) {
            entity.setDiaryResolvedYn(normalizeYn(diaryResolvedYn));
        }
        if (StringUtils.isNotBlank(dreamResolvedYn)) {
            entity.setDreamResolvedYn(normalizeYn(dreamResolvedYn));
        }

        final JournalDayEntity updatedEntity = repository.saveAndFlush(entity);
        final JournalDayDto updatedDto = mapstruct.toDto(updatedEntity);
        this.postModify(postDto, updatedDto);

        final ServiceResponse response = new ServiceResponse();
        response.setRslt(updatedDto.getId() != null);
        response.setRsltObj(updatedDto);
        return response;
    }

    /**
     * Y/N 플래그를 Y 또는 N으로 정규화한다. blank/null 은 N.
     *
     * @param yn 원본 값
     * @return Y 또는 N
     */
    private static String normalizeYn(final String yn) {
        return "Y".equalsIgnoreCase(StringUtils.trimToEmpty(yn)) ? "Y" : "N";
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
            throw new NotAuthorizedException("common.result.access-not-authorized");
        }
        return deleted;
    }

    /**
     * 조회된 저널 일자 아래의 일기 라이프사이클 맵을 조회한다.
     *
     * @param contentType 수집할 일기 컨텐츠 타입
     * @param journalDayEntityList 저널 일자 엔티티 그래프
     * @return 일기 ID 기준 라이프사이클 키 맵
     */
    private Map<Integer, String> getEntryLifecycleMap(
            final ContentType contentType,
            final List<JournalDayEntity> journalDayEntityList
    ) {
        return lifecycleService.getLifecycleMap(contentType, this.collectEntryIds(contentType, journalDayEntityList));
    }

    /**
     * 저널 일자/챕터 그래프에서 특정 컨텐츠 타입의 일기 ID를 수집한다.
     *
     * @param targetContentType 수집 대상 컨텐츠 타입
     * @param journalDayEntityList journal day 엔티티 그래프
     * @return 라이프사이클을 병합할 일기 ID 목록
     */
    private List<Integer> collectEntryIds(
            final ContentType targetContentType,
            final List<JournalDayEntity> journalDayEntityList
    ) {
        final List<Integer> entryIdList = new java.util.ArrayList<>();
        if (journalDayEntityList == null || journalDayEntityList.isEmpty()) return entryIdList;

        for (final JournalDayEntity journalDayEntity : journalDayEntityList) {
            if (journalDayEntity == null || journalDayEntity.getJournalChapterList() == null) continue;

            for (final JournalChapterEntity journalChapterEntity : journalDayEntity.getJournalChapterList()) {
                if (journalChapterEntity == null || journalChapterEntity.getJournalEntryList() == null) continue;

                for (final JournalEntryEntity journalEntryEntity : journalChapterEntity.getJournalEntryList()) {
                    if (journalEntryEntity == null || journalEntryEntity.getId() == null) continue;
                    if (ContentType.get(journalEntryEntity.getContentType()) != targetContentType) continue;
                    entryIdList.add(journalEntryEntity.getId());
                }
            }
        }
        return entryIdList;
    }

    /**
     * 일자 그래프가 포함한 대상 엔트리에 달린 Reflection 의 state·lifecycle 맵을 구성한다.
     *
     * <p>Reflection 은 별도 Aggregate(journal_reflection)이라 일자 트리에서 순회할 수 없다. 대상 엔트리
     * ID 로 역참조 로드(refId IN)한 뒤, Reflection 자신의 ID 를 키로 state 는 attachable state embed 에서,
     * lifecycle 은 {@code JOURNAL_REFLECTION} 축 조회로 구성한다. 소비측(일/주간 뷰·월별 검색)은 기존
     * {@code journalReflectionStateMapByUser}/lifecycle 캐시명을 그대로 읽는다.</p>
     *
     * @param journalDayEntityList 저널 일자 엔티티 그래프
     * @return Reflection state·lifecycle 맵 묶음 (reflection ID 기준)
     */
    private ReflectionStateMaps buildReflectionStateMaps(final List<JournalDayEntity> journalDayEntityList) {
        final List<Integer> targetIds = this.collectAllEntryIds(journalDayEntityList);
        if (targetIds.isEmpty()) return ReflectionStateMaps.empty();

        final List<JournalReflectionEntity> reflections =
                journalReflectionRepository.findAllByRefIdInOrderByCreatedAtAsc(targetIds);
        final Map<Integer, JournalState> stateMap = new HashMap<>();
        final List<Integer> reflectionIds = new java.util.ArrayList<>();
        for (final JournalReflectionEntity reflection : reflections) {
            if (reflection == null || reflection.getId() == null) continue;
            reflectionIds.add(reflection.getId());
            stateMap.put(
                    reflection.getId(),
                    JournalState.builder()
                            .collapsed(reflection.state.hasState(StateKey.COLLAPSED))
                            .imprtc(reflection.state.hasState(StateKey.IMPRTC))
                            .refrnc(reflection.state.hasState(StateKey.REFRNC))
                            .build()
            );
        }
        final Map<Integer, String> lifecycleMap =
                lifecycleService.getLifecycleMap(ContentType.JOURNAL_REFLECTION, reflectionIds);
        return new ReflectionStateMaps(stateMap, lifecycleMap);
    }

    /**
     * 저널 일자/챕터 그래프에서 모든 엔트리 ID 를 타입 구분 없이 수집한다.
     * Reflection 역참조 대상(일기·꿈·노트)을 한 번에 훑기 위한 수집이다.
     *
     * @param journalDayEntityList journal day 엔티티 그래프
     * @return 엔트리 ID 목록
     */
    private List<Integer> collectAllEntryIds(final List<JournalDayEntity> journalDayEntityList) {
        final List<Integer> entryIdList = new java.util.ArrayList<>();
        if (journalDayEntityList == null || journalDayEntityList.isEmpty()) return entryIdList;

        for (final JournalDayEntity journalDayEntity : journalDayEntityList) {
            if (journalDayEntity == null || journalDayEntity.getJournalChapterList() == null) continue;

            for (final JournalChapterEntity journalChapterEntity : journalDayEntity.getJournalChapterList()) {
                if (journalChapterEntity == null || journalChapterEntity.getJournalEntryList() == null) continue;

                for (final JournalEntryEntity journalEntryEntity : journalChapterEntity.getJournalEntryList()) {
                    if (journalEntryEntity == null || journalEntryEntity.getId() == null) continue;
                    entryIdList.add(journalEntryEntity.getId());
                }
            }
        }
        return entryIdList;
    }

    /**
     * Reflection state·lifecycle 캐시 맵 묶음. 키는 Reflection 자신의 ID 다.
     *
     * @param stateMap reflection ID 기준 state 맵
     * @param lifecycleMap reflection ID 기준 lifecycle 키 맵
     */
    private record ReflectionStateMaps(
            Map<Integer, JournalState> stateMap,
            Map<Integer, String> lifecycleMap
    ) {
        /**
         * 대상이 없을 때 쓰는 빈 묶음.
         *
         * @return 빈 state·lifecycle 맵 묶음
         */
        private static ReflectionStateMaps empty() {
            return new ReflectionStateMaps(new HashMap<>(), new HashMap<>());
        }
    }

}
