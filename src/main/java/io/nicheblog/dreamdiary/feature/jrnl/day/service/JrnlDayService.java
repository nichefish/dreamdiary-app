package io.nicheblog.dreamdiary.feature.jrnl.day.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf.ContentType;
import io.nicheblog.dreamdiary.feature.clsf._shared.service.BaseClsfService;
import io.nicheblog.dreamdiary.feature.jrnl._shared.handler.JrnlCacheEvictWorker;
import io.nicheblog.dreamdiary.feature.jrnl._shared.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.feature.jrnl._shared.state.JrnlStateMaps;
import io.nicheblog.dreamdiary.feature.jrnl.day.entity.JrnlDayEntity;
import io.nicheblog.dreamdiary.feature.jrnl.day.mapstruct.JrnlDayMapstruct;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDaySearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.day.repository.jpa.JrnlDayRepository;
import io.nicheblog.dreamdiary.feature.jrnl.day.repository.mybatis.JrnlDayMapper;
import io.nicheblog.dreamdiary.feature.jrnl.day.service.helper.JrnlDayViewHelper;
import io.nicheblog.dreamdiary.feature.jrnl.day.spec.JrnlDaySpec;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private final JrnlDayMapstruct mapstruct;

    public JrnlDayMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public JrnlDayMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final JrnlDayMapper jrnlDayMapper;
    private final JrnlCacheEvictWorker jrnlCacheEvictWorker;

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
        final JrnlStateMaps maps = JrnlDayViewHelper.makeJrnlStateMaps(myJrnlDayEntityList, searchParam);

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
        JrnlDayViewHelper.setHldyInfo(listDto, hldyMap);

        // resolved/collapse 상태 merge
        JrnlDayViewHelper.mergeStates(listDto, searchParam);

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
        JrnlDayViewHelper.setHldyInfo(listDto, hldyMap);

        // 상태state merge
        JrnlDayViewHelper.mergeStates(listDto, searchParam);
        // 접힌 entry에 태그 요약 표시
        JrnlDayViewHelper.applyEntryTagSummary(listDto, searchParam);

        return listDto;
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
        JrnlDayViewHelper.setHldyInfo(listDto, hldyMap);

        // 상태state merge
        JrnlDayViewHelper.mergeStates(listDto, searchParam);
        // 접힌 entry에 태그 요약 표시
        JrnlDayViewHelper.applyEntryTagSummary(listDto, searchParam);

        return listDto;
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
        // 관련 캐시 삭제
        jrnlCacheEvictWorker.evictAfterCommit(JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_DAY);
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
        JrnlDayViewHelper.setHldyInfo(retrieved, hldyMap);

        // resolved/collapse 상태 merge
        JrnlDayViewHelper.mergeStates(retrieved);

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
        // 관련 캐시 삭제
        jrnlCacheEvictWorker.evictAfterCommit(JrnlCacheEvictParam.of(updatedDto), ContentType.JRNL_DAY);
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
        // 관련 캐시 삭제
        jrnlCacheEvictWorker.evictAfterCommit(JrnlCacheEvictParam.of(deletedDto), ContentType.JRNL_DAY);
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
}
