package io.nicheblog.dreamdiary.feature.calendar.schedule.service;

import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
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
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import io.nicheblog.dreamdiary.infrastructure.code.service.CodeLookupService;
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
import java.time.LocalDate;
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
    private final CodeLookupService codeLookupService;

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
        this.normalizeAndValidateSchedule(registDto);

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
        this.normalizeAndValidateSchedule(modifyDto);

        // 개인 일정시 = '나' 자동으로 넣어줌 :: 메소드 분리
        if (modifyDto.getIsPrvt()) this.setMeToSchedule(modifyDto);
    }

    /**
     * 일정 저장 계약을 정규화하고 검증한다.
     * <p>종료일 미입력은 시작일과 같은 날짜로 정규화하며 공휴일은 단일 일자만 허용한다.</p>
     * <p>변경 전에는 종료일이 시작일보다 빠르면 시작일로 조용히 덮어썼고, 기존 단일 일정은
     * 수정 시 다일 일정으로 늘릴 수 없었다. 변경 후에는 역전된 기간을 명시적으로 거부하고
     * 요청에 담긴 유효한 종료일을 그대로 보존한다.</p>
     * <p>휴가 구분은 {@code scheduleCd=VCATN}일 때만 저장하며 활성 {@code VCATN_CD} 코드가 필수다.</p>
     *
     * @param scheduleDto 등록 또는 수정할 일정
     */
    private void normalizeAndValidateSchedule(final ScheduleDto scheduleDto) throws Exception {
        if (StringUtils.isBlank(scheduleDto.getScheduleCd()) || StringUtils.isBlank(scheduleDto.getBgnDt())) {
            log.warn("SCHEDULE_VALIDATION_FAILED reason=required id={} scheduleCd={} bgnDt={}",
                    scheduleDto.getId(), scheduleDto.getScheduleCd(), scheduleDto.getBgnDt());
            throw new IllegalArgumentException(MessageUtils.getMessage("schedule.validate.required"));
        }

        if (StringUtils.isBlank(scheduleDto.getEndDt())) scheduleDto.setEndDt(scheduleDto.getBgnDt());
        if (Code.SCHEDULE_HOLYDAY.equals(scheduleDto.getScheduleCd())) {
            log.debug("SCHEDULE_DATE_NORMALIZED reason=holyday-single-date id={} bgnDt={}",
                    scheduleDto.getId(), scheduleDto.getBgnDt());
            scheduleDto.setEndDt(scheduleDto.getBgnDt());
        }

        final LocalDate bgnDate;
        final LocalDate endDate;
        try {
            bgnDate = DateUtils.asLocalDate(scheduleDto.getBgnDt());
            endDate = DateUtils.asLocalDate(scheduleDto.getEndDt());
        } catch (final Exception e) {
            log.warn("SCHEDULE_VALIDATION_FAILED reason=date-format id={} bgnDt={} endDt={}",
                    scheduleDto.getId(), scheduleDto.getBgnDt(), scheduleDto.getEndDt());
            throw new IllegalArgumentException(MessageUtils.getMessage("schedule.validate.date-format"), e);
        }
        if (bgnDate == null || endDate == null) {
            log.warn("SCHEDULE_VALIDATION_FAILED reason=date-required id={} bgnDt={} endDt={}",
                    scheduleDto.getId(), scheduleDto.getBgnDt(), scheduleDto.getEndDt());
            throw new IllegalArgumentException(MessageUtils.getMessage("schedule.validate.required"));
        }
        if (endDate.isBefore(bgnDate)) {
            log.warn("SCHEDULE_VALIDATION_FAILED reason=end-before-start id={} bgnDt={} endDt={}",
                    scheduleDto.getId(), scheduleDto.getBgnDt(), scheduleDto.getEndDt());
            throw new IllegalArgumentException(MessageUtils.getMessage("schedule.validate.date-range"));
        }

        if (Code.SCHEDULE_VCATN.equals(scheduleDto.getScheduleCd())) {
            final String vcatnCd = scheduleDto.getVcatnCd();
            if (StringUtils.isBlank(vcatnCd) || codeLookupService.getCodeName(Code.VCATN_CD, vcatnCd) == null) {
                log.warn("SCHEDULE_VALIDATION_FAILED reason=invalid-vacation-type id={} vcatnCd={}",
                        scheduleDto.getId(), vcatnCd);
                throw new IllegalArgumentException(MessageUtils.getMessage("schedule.validate.vacation-type"));
            }
            return;
        }

        if (StringUtils.isNotBlank(scheduleDto.getVcatnCd())) {
            log.info("SCHEDULE_VACATION_TYPE_CLEARED reason=non-vacation id={} scheduleCd={} vcatnCd={}",
                    scheduleDto.getId(), scheduleDto.getScheduleCd(), scheduleDto.getVcatnCd());
            scheduleDto.setVcatnCd(null);
        }
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
     * 일정 상세를 현재 사용자 가시성 계약으로 조회한다.
     * 공개 일정은 모든 인증 사용자가 볼 수 있고, 개인 일정은 작성자 또는 참가자만 볼 수 있다.
     *
     * @param key 일정 식별자
     * @return 조회 가능한 일정 DTO
     */
    @Override
    @Transactional(readOnly = true)
    public ScheduleDto getDtlDto(final Integer key) throws Exception {
        final ScheduleEntity retrievedEntity = this.getDtlEntity(key);
        this.assertCanViewSchedule(retrievedEntity);
        return mapstruct.toDto(retrievedEntity);
    }

    /**
     * 개인 일정의 조회 권한을 검증한다.
     *
     * @param entity 조회 대상 일정
     */
    private void assertCanViewSchedule(final ScheduleEntity entity) {
        if (!"Y".equals(entity.getPrivateYn())) return;

        final String username = AuthUtils.requireLoginUsername();
        final boolean isOwner = username.equals(entity.getCreatedBy());
        final boolean isParticipant = CollectionUtils.isNotEmpty(entity.getPrtcpntList())
                && entity.getPrtcpntList().stream()
                .anyMatch(participant -> username.equals(participant.getUsername()));
        if (isOwner || isParticipant) return;

        log.warn("SCHEDULE_ACCESS_DENIED action=view id={} username={} createdBy={}",
                entity.getId(), username, entity.getCreatedBy());
        throw new NotAuthorizedException("common.result.access-not-authorized");
    }

    /**
     * 일정 수정 권한은 작성자에게만 부여한다.
     * 참가자는 개인 일정을 조회할 수 있지만 작성자 대신 수정·삭제할 수 없다.
     *
     * @param entity 변경 대상 일정
     * @param action 로그에 기록할 작업명
     */
    private void assertCanManageSchedule(final ScheduleEntity entity, final String action) {
        if (AuthUtils.isCreatedBy(entity.getCreatedBy())) return;

        log.warn("SCHEDULE_ACCESS_DENIED action={} id={} username={} createdBy={}",
                action, entity.getId(), AuthUtils.getLoginUsername(), entity.getCreatedBy());
        throw new NotAuthorizedException("common.result.access-not-authorized");
    }

    /**
     * 일정관리 > 일정 수정
     * Clears Cache : holydayEntityList, isHolyday, isHolydayOrWeekend
     */
    @Override
    @Transactional
    public ServiceResponse modify(final ScheduleDto modifyDto) throws Exception {
        final ScheduleEntity modifyEntity = this.getDtlEntity(modifyDto.getKey());       // Entity 레벨 조회
        this.assertCanManageSchedule(modifyEntity, "modify");
        // 수정 전처리
        this.preModify(modifyDto);
        mapstruct.updateFromDto(modifyDto, modifyEntity);
        // null 무시 update 계약에서도 VCATN → 비휴가 전환 시 고아 휴가 코드는 반드시 제거한다.
        if (!Code.SCHEDULE_VCATN.equals(modifyDto.getScheduleCd())) modifyEntity.setVcatnCd(null);
        // update
        final ScheduleEntity updatedEntity = this.updt(modifyEntity);
        final ScheduleDto updatedDto = mapstruct.toDto(updatedEntity);

        return ServiceResponse.builder()
                .rslt(updatedEntity.getId() != null)
                .rsltObj(updatedDto)
                .build();
    }

    /**
     * 일정 삭제 전 작성자 권한을 검증한다.
     *
     * @param deletedDto 삭제 대상 일정
     */
    @Override
    public void preDelete(final ScheduleDto deletedDto) {
        if (AuthUtils.isCreatedBy(deletedDto.getCreatedBy())) return;

        log.warn("SCHEDULE_ACCESS_DENIED action=delete id={} username={} createdBy={}",
                deletedDto.getId(), AuthUtils.getLoginUsername(), deletedDto.getCreatedBy());
        throw new NotAuthorizedException("common.result.access-not-authorized");
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
        final Optional<ScheduleEntity> scheduleDetailWrapper = repository.findByScheduleCdAndBgnDt(Code.SCHEDULE_HOLYDAY, DateUtils.asLocalDateTime(sDate));

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
        /*
         * holydayMap 은 저널 일자·엔트리 목록의 공휴일 표시(isHolyday) 원천이다.
         * 이걸 비우지 않으면 공휴일 일정을 등록·수정·삭제해도 목록 색상이 갱신되지 않았다.
         * 비운 뒤 재생성은 getHolydayMap() 이 미스 시 수행한다.
         */
        EhCacheUtils.clearCache("holydayMap");
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

    /**
     * 휴일 정보 캐시를 조회한다. 캐시가 비어 있으면 재생성 후 다시 읽는다.
     * <p>
     * {@code holydayMap} 은 {@code @Cacheable} 이 아니라 수동으로 put 하는 캐시라 자동 로딩되지 않는다.
     * 채우는 곳은 기동 시 워밍업({@code ScheduleCacheWarmupTask})과 공휴일 API 동기화·본 메서드 미스 처리다.
     * ehcache defaultTemplate TTL 이 1일이라, 미스 시 재생성하지 않으면 저널 일자·엔트리 검색의
     * {@code isHolyday} 가 비어 공휴일 빨간색이 사라진다.
     * </p>
     *
     * @return 휴일 맵 (재생성에 실패하면 null — 호출부는 휴일 정보를 채우지 않고 넘어간다)
     */
    @SuppressWarnings("unchecked")
    public Map<String, List<String>> getHolydayMap() {
        final Map<String, List<String>> cached = (Map<String, List<String>>) EhCacheUtils.getObjectFromCache("holydayMap");
        if (cached != null) return cached;

        log.info("[getHolydayMap] holydayMap 캐시 미스 — 재생성 시도");
        try {
            this.resyncHolydayMap();
        } catch (final Exception e) {
            log.error("[getHolydayMap] holydayMap 재생성 실패 — 휴일 정보 없이 조회를 계속한다", e);
            return null;
        }
        return (Map<String, List<String>>) EhCacheUtils.getObjectFromCache("holydayMap");
    }
}
