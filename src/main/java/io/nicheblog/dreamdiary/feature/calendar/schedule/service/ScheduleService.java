package io.nicheblog.dreamdiary.feature.calendar.schedule.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.service.BaseAttachableService;
import io.nicheblog.dreamdiary.feature.calendar.schedule.entity.ScheduleEntity;
import io.nicheblog.dreamdiary.feature.calendar.schedule.mapstruct.ScheduleMapstruct;
import io.nicheblog.dreamdiary.feature.calendar.schedule.model.ScheduleDto;
import io.nicheblog.dreamdiary.feature.calendar.schedule.model.SchedulePrtcpntDto;
import io.nicheblog.dreamdiary.feature.calendar.schedule.model.ScheduleSearchParam;
import io.nicheblog.dreamdiary.feature.calendar.schedule.repository.jpa.ScheduleRepository;
import io.nicheblog.dreamdiary.feature.calendar.schedule.spec.ScheduleSpec;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ScheduleService
 * <pre>
 *  일정 관리 서비스 모듈
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class ScheduleService
        implements BaseAttachableService<ScheduleDto, ScheduleDto, Integer, ScheduleEntity> {

    @Getter
    private final ScheduleRepository repository;
    @Getter
    private final ScheduleSpec spec;
    @Getter
    private final ScheduleMapstruct mapstruct = ScheduleMapstruct.INSTANCE;

    public ScheduleMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public ScheduleMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final ApplicationEventPublisherWrapper publisher;

    @Resource(name="jCacheManager")
    private CacheManager cacheManager;

    private final ApplicationContext context;
    private ScheduleService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 등록 전처리. (override)
     *
     * @param registDto 등록할 객체
     */
    @Override
    public void preRegist(final ScheduleDto registDto) throws Exception {
        // 종료일자 없을시 자동으로 시작일자와 같게 처리
        if (StringUtils.isEmpty(registDto.getEndDt())) registDto.setEndDt(registDto.getBgnDt());

        // 개인 일정시 = '나' 자동으로 넣어줌 :: 메소드 분리
        if (registDto.getIsPrvt()) this.setMeToSchedule(registDto);
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final ScheduleDto updatedDto) throws Exception {
        // 잔디 메세지 발송 :: 메인 로직과 분리
        // if (isSuccess && "Y".equals(jandiYn)) {
        //     String jandiRsltMsg = notifyService.notifyScheduleReg(trgetTopic, result, logParam);
        //     rsltMsg = rsltMsg + "\n" + jandiRsltMsg;
        // }
    }

    /**
     * 수정 전처리. (override)
     *
     * @param modifyDto 수정할 객체
     */
    @Override
    public void preModify(final ScheduleDto modifyDto) throws Exception {
        // 종료일자 없을시 자동으로 시작일자와 같게 처리
        if (StringUtils.isEmpty(modifyDto.getEndDt())) modifyDto.setEndDt(modifyDto.getBgnDt());

        // 개인 일정시 = '나' 자동으로 넣어줌 :: 메소드 분리
        if (modifyDto.getIsPrvt()) this.setMeToSchedule(modifyDto);
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final ScheduleDto postDto, final ScheduleDto updatedDto) throws Exception {
        // 잔디 메세지 발송 :: 메인 로직과 분리
        // if (isSuccess && "Y".equals(jandiYn)) {
        //     String jandiRsltMsg = notifyService.notifyScheduleReg(trgetTopic, result, logParam);
        //     rsltMsg = rsltMsg + "\n" + jandiRsltMsg;
        // }
    }

    /**
     * 스케줄에 '나' 포함시키기 :: 메소드 분리
     *
     * @param scheduleDto 적용할 객체
     */
    public void setMeToSchedule(final ScheduleDto scheduleDto) {
        List<SchedulePrtcpntDto> prtcpntList = scheduleDto.getPrtcpntList();
        if (CollectionUtils.isEmpty(prtcpntList)) prtcpntList = new ArrayList<>();
        // 내이름 있는지 체크
        final SchedulePrtcpntDto isMe = new SchedulePrtcpntDto(AuthUtils.getLoginUsername());
        if (!prtcpntList.contains(isMe)) prtcpntList.add(isMe);
        scheduleDto.setPrtcpntList(prtcpntList);
    }

    /**
     * 일정관리 > 일정 수정
     * Clears Cache : holydayEntityList, isHolyday, isHolydayOrWeekend
     */
    @Override
    @Transactional
    public ServiceResponse modify(final ScheduleDto modifyDto) throws Exception {
        // 수정 전처리
        this.preModify(modifyDto);

        final ScheduleEntity modifyEntity = this.getDtlEntity(modifyDto.getKey());       // Entity 레벨 조회
        final boolean wasSingleDate = DateUtils.isSameDay(modifyEntity.getBgnDt(), modifyEntity.getEndDt());
        final boolean isInvalidEndDate = modifyDto.getBgnDt()
                                            .compareTo(modifyDto.getEndDt()) > 0;
        if (wasSingleDate || isInvalidEndDate) modifyDto.setEndDt(modifyDto.getBgnDt());
        mapstruct.updateFromDto(modifyDto, modifyEntity);
        // update
        final ScheduleEntity updatedEntity = this.updt(modifyEntity);
        final ScheduleDto updatedDto = mapstruct.toDto(updatedEntity);

        return ServiceResponse.builder()
                .rslt(updatedEntity.getId() != null)
                .rsltObj(updatedDto)
                .build();
    }

    /**
     * DB에 저장된 공휴일 정보 조회
     */
    @Cacheable(cacheNames = "holydayEntityList")
    public List<ScheduleEntity> getHolydayEntityList() throws Exception {
        final Map<String, Object> searchParamMap = new HashMap<>() {{
            put("scheduleCd", Code.SCHEDULE_HOLYDAY);
        }};

        return this.getListEntity(searchParamMap);
    }

    /**
     * 공휴일여부 반환
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "isHolyday", key = "#date")
    public Boolean isHolyday(final Object date) throws Exception {
        // 공휴일 여부 체크
        final Date asDate = DateUtils.asDate(date);
        final Date sDate = DateUtils.Parser.sDateParse(asDate);
        final Optional<ScheduleEntity> scheduleDetailWrapper = repository.findByScheduleCdAndBgnDt(Code.SCHEDULE_HOLYDAY, sDate);

        return scheduleDetailWrapper.isPresent();
    }

    /**
     * 공휴일 또는 주말여부 반환
     */
    @Cacheable(cacheNames = "isHolydayOrWeekend", key = "#date")
    public Boolean isHolydayOrWeekend(final Object date) throws Exception {
        return (this.getSelf().isHolyday(date) || DateUtils.isWeekend(date));
    }

    /**
     * 이번달의 첫 번째 평일 반환
     */
    public Date getFirstBsnsDayInCurrMnth() throws Exception {
        Date keyDt = DateUtils.asDate(DateUtils.getCurrYyMnthStr() + "01");
        while (true) {
            if (!this.getSelf().isHolydayOrWeekend(keyDt)) return keyDt;
            keyDt = DateUtils.addDays(keyDt, 1);
        }
    }

    /**
     * 이번달의 첫 번째 평일 여부 반환
     */
    public boolean isFirstBsnsDayInCurrMnth() throws Exception {
        final Date firstBsnsDayInCurrMnth = getFirstBsnsDayInCurrMnth();
        final Date today = DateUtils.getCurrDate();

        return DateUtils.isSameDay(firstBsnsDayInCurrMnth, today);
    }

    /**
     * 관련 캐시 삭제.
     *
     * @param rslt 캐시 처리할 엔티티
     */
    public void evictCache(final ScheduleEntity rslt) throws Exception {
        EhCacheUtils.clearCache("holydayEntityList");
        EhCacheUtils.clearCache("isHolyday");
        EhCacheUtils.clearCache("isHolydayOrWeekend");
    }

    /**
     * 공휴일 정보를 다시 동기화하여 캐시에 갱신한다.
     */
    public void resyncHolydayMap() throws Exception {
        final ScheduleSearchParam param = ScheduleSearchParam.builder().scheduleCd(Code.SCHEDULE_HOLYDAY).build();
        final List<ScheduleEntity> holydayList = this.getSelf().getListEntity(param);

        final Map<String, List<String>> holydayMap = holydayList.stream()
                .filter(entity -> entity.getBgnDt() != null)
                .collect(Collectors.groupingBy(
                        entity -> {
                            try {
                                return DateUtils.asStr(entity.getBgnDt(), DatePtn.DATE);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        },
                        Collectors.mapping(
                                ScheduleEntity::getScheduleNm,
                                Collectors.toList()
                        )
                ));

        final Cache cache = cacheManager.getCache("holydayMap");
        if (cache != null) cache.put(SimpleKey.EMPTY, holydayMap);
    }
}

