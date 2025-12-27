package io.nicheblog.dreamdiary.domain.jrnl.day.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDayEntity;
import io.nicheblog.dreamdiary.domain.jrnl.day.mapstruct.JrnlDayMapstruct;
import io.nicheblog.dreamdiary.domain.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.domain.jrnl.day.model.JrnlDaySearchParam;
import io.nicheblog.dreamdiary.domain.jrnl.day.repository.jpa.JrnlDayRepository;
import io.nicheblog.dreamdiary.domain.jrnl.day.repository.mybatis.JrnlDayMapper;
import io.nicheblog.dreamdiary.domain.jrnl.day.spec.JrnlDaySpec;
import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryEntity;
import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamEntity;
import io.nicheblog.dreamdiary.domain.jrnl.dream.model.JrnlDreamDto;
import io.nicheblog.dreamdiary.domain.jrnl.entry.entity.JrnlEntryEntity;
import io.nicheblog.dreamdiary.domain.jrnl.entry.model.JrnlEntryDto;
import io.nicheblog.dreamdiary.domain.jrnl.intrpt.entity.JrnlIntrptEntity;
import io.nicheblog.dreamdiary.domain.jrnl.intrpt.model.JrnlIntrptDto;
import io.nicheblog.dreamdiary.domain.jrnl.state.JrnlState;
import io.nicheblog.dreamdiary.domain.jrnl.state.JrnlStateMaps;
import io.nicheblog.dreamdiary.extension.cache.event.JrnlCacheEvictEvent;
import io.nicheblog.dreamdiary.extension.cache.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.extension.cache.util.EhCacheUtils;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.extension.clsf.meta.event.JrnlMetaProcEvent;
import io.nicheblog.dreamdiary.extension.clsf.tag.event.JrnlTagProcEvent;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseClsfService;
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

import java.util.*;

/**
 * JrnlDayService
 * <pre>
 *  저널 일자 관리 서비스 모듈
 * </pre>
 *
 * @author nichefish
 */
@Service("jrnlDayService")
@RequiredArgsConstructor
@Log4j2
public class JrnlDayService
        implements BaseClsfService<JrnlDayDto, JrnlDayDto, Integer, JrnlDayEntity> {

    @Getter
    private final JrnlDayRepository repository;
    @Getter
    private final JrnlDaySpec spec;
    @Getter
    private final JrnlDayMapstruct mapstruct = JrnlDayMapstruct.INSTANCE;

    public JrnlDayMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public JrnlDayMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final JrnlDayMapper jrnlDayMapper;
    private final ApplicationEventPublisherWrapper publisher;

    private final ApplicationContext context;
    private JrnlDayService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 내 목록 조회 (dto level) :: 캐시 처리
     *
     * @param lgnUserId 사용자 ID
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    @Cacheable(value="myJrnlDayList", key="#lgnUserId + \"_\" + #searchParam.getYy() + \"_\" + #searchParam.getMnth()")
    public List<JrnlDayDto> getMyListDtoByYyMnth(final String lgnUserId, final JrnlDaySearchParam searchParam) throws Exception {
        searchParam.setRegstrId(lgnUserId);

        final List<JrnlDayEntity> myJrnlDayEntityList = this.getSelf().getListEntity(searchParam);

        // 1) stateMap 만들기
        final JrnlStateMaps maps = makeJrnlStateMaps(myJrnlDayEntityList, searchParam);

        // 2) 캐시에 저장
        final String cacheKey = AuthUtils.getLgnUserId() + "_" + searchParam.getYy() + "_" + searchParam.getMnth();
        EhCacheUtils.put("myEntryStateMap", cacheKey, maps.getEntryMap());
        EhCacheUtils.put("myDiaryStateMap", cacheKey, maps.getDiaryMap());
        EhCacheUtils.put("myDreamStateMap", cacheKey, maps.getDreamMap());
        EhCacheUtils.put("myIntrptStateMap", cacheKey, maps.getIntrptMap());

        return mapstruct.toDtoList(myJrnlDayEntityList);
    }

    /**
     * 내 목록 조회 (dto level) :: 캐시 처리
     *
     * @param lgnUserId 사용자 ID
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    @SuppressWarnings("unchecked")
    public List<JrnlDayDto> getMyListDtoByMetaNoWithHldy(String lgnUserId, JrnlDaySearchParam searchParam) throws Exception {
        searchParam.setRegstrId(lgnUserId);
        searchParam.setSort("DESC");

        final List<JrnlDayDto> listDto = this.getSelf().getListDto(searchParam);

        // 공휴일 정보 세팅
        final Map<String, List<String>> hldyMap = (Map<String, List<String>>) EhCacheUtils.getObjectFromCache("hldyMap");
        this.setHldyInfo(listDto, hldyMap);

        // resolved/collapse 상태 merge
        this.mergeStates(listDto, searchParam);

        return listDto;
    }

    /**
     * 내 기준일자 조회 (dto level) :: 캐시 처리
     *
     * @param lgnUserId 사용자 ID
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    public List<JrnlDayDto> getMyJrnlStdrdDays(final String lgnUserId, final JrnlDaySearchParam searchParam) throws Exception {
        searchParam.setRegstrId(lgnUserId);

        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 내 목록 조회 (dto level) + 공휴일 정보 추가
     *
     * @param lgnUserId 사용자 ID
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    @SuppressWarnings("unchecked")
    public List<JrnlDayDto> getMyListDtoByYyMnthWithHldy(final String lgnUserId, final JrnlDaySearchParam searchParam) throws Exception {
        final List<JrnlDayDto> listDto = this.getSelf().getMyListDtoByYyMnth(lgnUserId, searchParam);

        // 공휴일 정보 세팅
        final Map<String, List<String>> hldyMap = (Map<String, List<String>>) EhCacheUtils.getObjectFromCache("hldyMap");
        this.setHldyInfo(listDto, hldyMap);

        // resolved/collapse 상태 merge
        this.mergeStates(listDto, searchParam);

        return listDto;
    }

    /**
     * 각 게시물 타입(일기/꿈/해석)의 상태(resolvedYn, collapsedYn)를 별도의 Map(postNo → JrnlState)으로 구성해 반환한다.
     * @param myJrnlDayEntityList 조회된 JrnlDayEntity 전체 목록
     * @param searchParam 조회 조건(연도/월 등). 캐시 키 생성은 호출부에서 수행하며, 본 메서드는 단순히 상태맵 생성만 담당한다.
     * @return {@link JrnlStateMaps}
     *  diaryMap: 일기(postNo → JrnlState)
     *  dreamMap: 꿈(postNo → JrnlState)
     *  intrptMap: 해석(postNo → JrnlState)
     */
    private JrnlStateMaps makeJrnlStateMaps(final List<JrnlDayEntity> myJrnlDayEntityList, final JrnlDaySearchParam searchParam) {
        final Map<Integer, JrnlState> entryMap = new HashMap<>();
        final Map<Integer, JrnlState> diaryMap = new HashMap<>();
        final Map<Integer, JrnlState> dreamMap = new HashMap<>();
        final Map<Integer, JrnlState> intrptMap = new HashMap<>();

        for (final JrnlDayEntity day : myJrnlDayEntityList) {
            final List<JrnlEntryEntity> myJrnlEntryList = day.getJrnlEntryList();
            if (CollectionUtils.isNotEmpty(myJrnlEntryList)) {
                for (final JrnlEntryEntity entry : myJrnlEntryList) {
                    entryMap.put(entry.getPostNo(), JrnlState.of(entry.getCollapsedYn(), entry.getCollapsedYn(), entry.getCollapsedYn()));

                    final List<JrnlDiaryEntity> myJrnlDiaryList = entry.getJrnlDiaryList();
                    if (CollectionUtils.isNotEmpty(myJrnlDiaryList)) {
                        for (final JrnlDiaryEntity diary : myJrnlDiaryList) {
                            diaryMap.put(diary.getPostNo(), JrnlState.of(diary.getResolvedYn(), diary.getCollapsedYn(), diary.getImprtcYn()));
                        }
                    }
                }
            }

            final List<JrnlDreamEntity> myJrnlDreamList = day.getJrnlDreamList();
            if (CollectionUtils.isNotEmpty(myJrnlDreamList)) {
                for (final JrnlDreamEntity dream : myJrnlDreamList) {
                    dreamMap.put(dream.getPostNo(), JrnlState.of(dream.getResolvedYn(), dream.getCollapsedYn(), dream.getImprtcYn()));

                    final List<JrnlIntrptEntity> myJrnlIntrptList = dream.getJrnlIntrptList();
                    if (CollectionUtils.isNotEmpty(myJrnlIntrptList)) {
                        for (final JrnlIntrptEntity intrpt : myJrnlIntrptList) {
                            intrptMap.put(intrpt.getPostNo(), JrnlState.of(intrpt.getResolvedYn(), intrpt.getCollapsedYn(), intrpt.getImprtcYn()));
                        }
                    }
                }
            }
        }
        return JrnlStateMaps.builder().entryMap(entryMap).diaryMap(diaryMap).dreamMap(dreamMap).intrptMap(intrptMap).build();
    }

    /**
     * 내 기준일자 조회 (dto level) + 공휴일 정보 추가
     *
     * @param lgnUserId 사용자 ID
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    @SuppressWarnings("unchecked")
    public List<JrnlDayDto> getMyStdrdDtoWithHldy(final String lgnUserId, final JrnlDaySearchParam searchParam) throws Exception {
        final List<JrnlDayDto> listDto = this.getSelf().getMyJrnlStdrdDays(lgnUserId, searchParam);

        // 공휴일 정보 세팅
        final Map<String, List<String>> hldyMap = (Map<String, List<String>>) EhCacheUtils.getObjectFromCache("hldyMap");
        this.setHldyInfo(listDto, hldyMap);

        return listDto;
    }

    @SuppressWarnings("unchecked")
    private void mergeStates(final List<JrnlDayDto> listDto, final JrnlDaySearchParam searchParam) {
        if (CollectionUtils.isEmpty(listDto) || searchParam == null) return;

        final String cacheKey = AuthUtils.getLgnUserId()
                   + "_" + searchParam.getYy()
                   + "_" + searchParam.getMnth();

        Map<Integer, JrnlState> entryMap  = (Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("myEntryStateMap",  cacheKey);
        Map<Integer, JrnlState> diaryMap  = (Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("myDiaryStateMap",  cacheKey);
        Map<Integer, JrnlState> dreamMap  = (Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("myDreamStateMap",  cacheKey);
        Map<Integer, JrnlState> intrptMap = (Map<Integer, JrnlState>) EhCacheUtils.getObjectFromCache("myIntrptStateMap", cacheKey);

        entryMap = entryMap  == null ? Collections.emptyMap() : entryMap;
        diaryMap = diaryMap  == null ? Collections.emptyMap() : diaryMap;
        dreamMap = dreamMap  == null ? Collections.emptyMap() : dreamMap;
        intrptMap = intrptMap == null ? Collections.emptyMap() : intrptMap;

        applyStates(listDto, entryMap, diaryMap, dreamMap, intrptMap);
    }

    private void applyStates(
        final List<JrnlDayDto> listDto,
        final Map<Integer, JrnlState> entryMap,
        final Map<Integer, JrnlState> diaryMap,
        final Map<Integer, JrnlState> dreamMap,
        final Map<Integer, JrnlState> intrptMap
    ) {
        for (JrnlDayDto day : listDto) {

            if (CollectionUtils.isNotEmpty(day.getEntryList())) {
                for (final JrnlEntryDto entry : day.getJrnlEntryList()) {

                    final JrnlState s = entryMap.get(entry.getPostNo());
                    if (s != null) {
                        entry.setCollapsedYn(s.getCollapsedYn());
                    }

                    if (CollectionUtils.isEmpty(entry.getJrnlDiaryList())) continue;
                    for (final JrnlDiaryDto diary : entry.getJrnlDiaryList()) {
                        final JrnlState d = diaryMap.get(diary.getPostNo());
                        if (d != null) {
                            diary.setCollapsedYn(d.getCollapsedYn());
                            diary.setResolvedYn(d.getResolvedYn());
                            diary.setImprtcYn(d.getImprtcYn());
                        }
                    }
                }
            }

            if (CollectionUtils.isNotEmpty(day.getJrnlDreamList())) {
                for (final JrnlDreamDto dream : day.getJrnlDreamList()) {

                    final JrnlState s = dreamMap.get(dream.getPostNo());
                    if (s != null) {
                        dream.setCollapsedYn(s.getCollapsedYn());
                        dream.setResolvedYn(s.getResolvedYn());
                        dream.setImprtcYn(s.getImprtcYn());
                    }

                    if (CollectionUtils.isEmpty(dream.getJrnlIntrptList())) continue;
                    for (final JrnlIntrptDto intrpt : dream.getJrnlIntrptList()) {
                        final JrnlState d = intrptMap.get(intrpt.getPostNo());
                        if (d != null) {
                            intrpt.setCollapsedYn(d.getCollapsedYn());
                            intrpt.setResolvedYn(d.getResolvedYn());
                            intrpt.setImprtcYn(d.getImprtcYn());
                        }
                    }
                }
            }
        }
    }

    /**
     * 중복 체크 (정상시 true / 중복시 false)
     *
     * @param jrnlDay {@link JrnlDayDto} -- 중복 여부를 확인할 {@link JrnlDayDto} 객체
     * @return {@link boolean} -- 정상 시 true, 중복 시 false 반환
     */
    @Transactional(readOnly = true)
    public boolean dupChck(final JrnlDayDto jrnlDay) throws Exception {
        final boolean isDtUnknown = "Y".equals(jrnlDay.getDtUnknownYn());
        if (isDtUnknown) return false;

        final Date jrnlDt = DateUtils.asDate(jrnlDay.getJrnlDt());
        final String regstrId = AuthUtils.getLgnUserId();
        final Integer isDup = repository.countByJrnlDt(jrnlDt, regstrId);

        return isDup > 0;
    }

    /**
     * 날짜 기준으로 중복(해당 데이터 존재)시 해당 키값 반환
     *
     * @param jrnlDay {@link JrnlDayDto} -- 중복 여부를 확인할 {@link JrnlDayDto} 객체
     * @return {@link Integer} -- 중복되는 경우 해당하는 키값 (게시글 번호)
     */
    @Transactional(readOnly = true)
    public Integer getDupKey(final JrnlDayDto jrnlDay) throws Exception {
        final Date jrnlDt = DateUtils.asDate(jrnlDay.getJrnlDt());
        final String regstrId = AuthUtils.getLgnUserId();
        final JrnlDayEntity existingEntity = repository.findByJrnlDt(jrnlDt, regstrId);

        return existingEntity.getPostNo();
    }


    /**
     * 특정 태그의 관련 일자 목록 조회
     *
     * @param searchParam 검색 조건이 담긴 파라미터 객체
     * @return {@link List} -- 검색 결과 목록
     */
    @Cacheable(value="myJrnlDayTagDtl", key="T(io.nicheblog.dreamdiary.auth.security.util.AuthUtils).getLgnUserId() + \"_\" + #searchParam.getTagNo()")
    public List<JrnlDayDto> jrnlDayTagDtl(final JrnlDaySearchParam searchParam) throws Exception {
        searchParam.setSort("DESC");

        return this.getSelf().getListDto(searchParam);
    }

    /**
     * 등록 전처리. (override)
     *
     * @param registDto 등록할 객체
     */
    @Override
    public void preRegist(final JrnlDayDto registDto) throws Exception {
        // 년도/월 세팅:: 메소드 분리
        this.setYyMnth(registDto);
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final JrnlDayDto updatedDto) throws Exception {
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishCustomEvent(new JrnlTagProcEvent(this, updatedDto.getClsfKey(), updatedDto.getYy(), updatedDto.getMnth(), updatedDto.tag));
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishCustomEvent(new JrnlMetaProcEvent(this, updatedDto.getClsfKey(), updatedDto.getYy(), updatedDto.getMnth(), updatedDto.meta));
        // 관련 캐시 삭제
        publisher.publishCustomEvent(new JrnlCacheEvictEvent(this, JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_DAY));
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 식별자
     * @return {@link JrnlDiaryDto} -- 조회된 객체
     */
    @Cacheable(value="myJrnlDayDtlDto", key="T(io.nicheblog.dreamdiary.auth.security.util.AuthUtils).getLgnUserId() + \"_\" + #key")
    public JrnlDayDto getDtlDtoWithCache(final Integer key) throws Exception {
        final JrnlDayEntity retrievedEntity = this.getSelf().getDtlEntity(key);
        final JrnlDayDto retrieved = mapstruct.toDto(retrievedEntity);
        // 권한 체크
        if (!retrieved.getIsRegstr()) throw new NotAuthorizedException(MessageUtils.getMessage("msg.rslt.access-not-authorized"));

        return retrieved;
    }

    /**
     * 상세 조회 (dto level) :: 공휴일 정보 추가
     *
     * @param key 식별자
     * @return {@link JrnlDiaryDto} -- 조회된 객체
     */
    @SuppressWarnings("unchecked")
    public JrnlDayDto getDtlDtoWithCacheWithHldy(final Integer key) throws Exception {
        final JrnlDayDto retrieved = this.getSelf().getDtlDtoWithCache(key);

        // 공휴일 정보 세팅
        final Map<String, List<String>> hldyMap = (Map<String, List<String>>) EhCacheUtils.getObjectFromCache("hldyMap");
        this.setHldyInfo(retrieved, hldyMap);

        return retrieved;
    }

    /**
     * 수정 전처리. (override)
     *
     * @param modifyDto 등록할 객체
     */
    @Override
    public void preModify(final JrnlDayDto modifyDto) throws Exception {
        // 년도/월 세팅:: 메소드 분리
        this.setYyMnth(modifyDto);
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final JrnlDayDto postDto, final JrnlDayDto updatedDto) throws Exception {
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishCustomEvent(new JrnlTagProcEvent(this, updatedDto.getClsfKey(), updatedDto.getYy(), updatedDto.getMnth(), updatedDto.tag));
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishCustomEvent(new JrnlMetaProcEvent(this, updatedDto.getClsfKey(), updatedDto.getYy(), updatedDto.getMnth(), updatedDto.meta));
        // 관련 캐시 삭제
        publisher.publishCustomEvent(new JrnlCacheEvictEvent(this, JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_DAY));
    }

    /**
     * 날짜 기반으로 년도/월 항목 세팅 :: 메소드 분리
     *
     * @param jrnlDay 날짜 기반으로 년도와 월을 설정할 {@link JrnlDayDto} 객체
     */
    public void setYyMnth(final JrnlDayDto jrnlDay) throws Exception {
        // 날짜미상여부 N시 대략일자 무효화
        if ("Y".equals(jrnlDay.getDtUnknownYn())) {
            jrnlDay.setJrnlDt("");
            jrnlDay.setYy(Integer.valueOf(jrnlDay.getAprxmtDt().substring(0, 4)));
            jrnlDay.setMnth(Integer.valueOf(jrnlDay.getAprxmtDt().substring(5, 7)));
        }
        if ("N".equals(jrnlDay.getDtUnknownYn())) {
            jrnlDay.setAprxmtDt("");
            jrnlDay.setYy(Integer.valueOf(jrnlDay.getJrnlDt().substring(0, 4)));
            jrnlDay.setMnth(Integer.valueOf(jrnlDay.getJrnlDt().substring(5, 7)));
        }
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void postDelete(final JrnlDayDto deletedDto) throws Exception {
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishCustomEvent(new JrnlTagProcEvent(this, deletedDto.getClsfKey(), deletedDto.getYy(), deletedDto.getMnth()));
        // 관련 캐시 삭제
        publisher.publishCustomEvent(new JrnlCacheEvictEvent(this, JrnlCacheEvictParam.of(deletedDto), ContentType.JRNL_DAY));
    }

    /**
     * 삭제 데이터 조회
     *
     * @param key 삭제된 데이터의 키
     * @return {@link JrnlDayDto} -- 삭제된 데이터 Dto
     */
    @Transactional(readOnly = true)
    public JrnlDayDto getDeletedDtlDto(final Integer key) throws Exception {
        return jrnlDayMapper.getDeletedByPostNo(key);
    }

    /**
     * 주어진 {@link JrnlDayDto} 객체에 공휴일 및 주말 여부 정보를 설정한다.
     *
     * @param jrnlDayList 공휴일 및 주말 정보를 설정할 대상 DTO
     * @param hldyMap 날짜(String: yyyy-MM-dd) → 공휴일 이름 목록 매핑 정보
     */
    private void setHldyInfo(final List<JrnlDayDto> jrnlDayList, final Map<String, List<String>> hldyMap) throws Exception {
        if (CollectionUtils.isEmpty(jrnlDayList) || hldyMap == null) return;

        for (final JrnlDayDto jrnlDay : jrnlDayList) {
            setHldyInfo(jrnlDay, hldyMap);
        }
    }

    /**
     * 주어진 {@link JrnlDayDto} 객체에 공휴일 및 주말 여부 정보를 설정한다.
     *
     * @param jrnlDay 공휴일 및 주말 정보를 설정할 대상 DTO
     * @param hldyMap 날짜(String: yyyy-MM-dd) → 공휴일 이름 목록 매핑 정보
     */
    private void setHldyInfo(final JrnlDayDto jrnlDay, final Map<String, List<String>> hldyMap) throws Exception {
        if (jrnlDay == null || hldyMap == null) return;

        final String stdrdDt = jrnlDay.getStdrdDt();
        final boolean isHldy = hldyMap.containsKey(stdrdDt);
        final boolean isWeekend = DateUtils.isWeekend(stdrdDt);
        jrnlDay.setIsHldy(isHldy || isWeekend);
        if (isHldy) {
            final String concatHldyNm = String.join(", ", hldyMap.get(stdrdDt));
            jrnlDay.setHldyNm(concatHldyNm);
        }
    }
}