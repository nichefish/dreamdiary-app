package io.nicheblog.dreamdiary.domain.schdul.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.domain.schdul.entity.SchdulEntity;
import io.nicheblog.dreamdiary.domain.schdul.mapstruct.SchdulMapstruct;
import io.nicheblog.dreamdiary.domain.schdul.model.SchdulDto;
import io.nicheblog.dreamdiary.domain.schdul.model.SchdulPrtcpntDto;
import io.nicheblog.dreamdiary.domain.schdul.repository.jpa.SchdulRepository;
import io.nicheblog.dreamdiary.domain.schdul.spec.SchdulSpec;
import io.nicheblog.dreamdiary.extension.cache.util.EhCacheUtils;
import io.nicheblog.dreamdiary.extension.clsf.tag.event.TagProcEvent;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseClsfService;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * SchdulService
 * <pre>
 *  일정 관리 서비스 모듈
 * </pre>
 *
 * @author nichefish
 */
@Service("schdulService")
@RequiredArgsConstructor
@Log4j2
public class SchdulService
        implements BaseClsfService<SchdulDto, SchdulDto, Integer, SchdulEntity, SchdulRepository, SchdulSpec, SchdulMapstruct> {

    @Getter
    private final SchdulRepository repository;
    @Getter
    private final SchdulSpec spec;
    @Getter
    private final SchdulMapstruct mapstruct = SchdulMapstruct.INSTANCE;

    private final ApplicationEventPublisherWrapper publisher;

    private final ApplicationContext context;
    private SchdulService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 등록 전처리. (override)
     *
     * @param registDto 등록할 객체
     */
    @Override
    public void preRegist(final SchdulDto registDto) throws Exception {
        // 종료일자 없을시 자동으로 시작일자와 같게 처리
        if (StringUtils.isEmpty(registDto.getEndDt())) registDto.setEndDt(registDto.getBgnDt());

        // 개인 일정시 = '나' 자동으로 넣어줌 :: 메소드 분리
        if (registDto.getIsPrvt()) this.setMeToSchdul(registDto);
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void postRegist(final SchdulDto updatedDto) throws Exception {
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishAsyncEventAndWait(new TagProcEvent(this, updatedDto.getClsfKey(), updatedDto.tag));
        // 잔디 메세지 발송 :: 메인 로직과 분리
        // if (isSuccess && "Y".equals(jandiYn)) {
        //     String jandiRsltMsg = notifyService.notifySchdulReg(trgetTopic, result, logParam);
        //     rsltMsg = rsltMsg + "\n" + jandiRsltMsg;
        // }
    }

    /**
     * 수정 전처리. (override)
     *
     * @param modifyDto 수정할 객체
     */
    @Override
    public void preModify(final SchdulDto modifyDto) throws Exception {
        // 종료일자 없을시 자동으로 시작일자와 같게 처리
        if (StringUtils.isEmpty(modifyDto.getEndDt())) modifyDto.setEndDt(modifyDto.getBgnDt());

        // 개인 일정시 = '나' 자동으로 넣어줌 :: 메소드 분리
        if (modifyDto.getIsPrvt()) this.setMeToSchdul(modifyDto);
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     * @throws Exception 후처리 중 발생할 수 있는 예외
     */
    @Override
    public void postModify(final SchdulDto updatedDto) throws Exception {
        // 태그 처리 :: 메인 로직과 분리
        publisher.publishAsyncEventAndWait(new TagProcEvent(this, updatedDto.getClsfKey(), updatedDto.tag));
        // 잔디 메세지 발송 :: 메인 로직과 분리
        // if (isSuccess && "Y".equals(jandiYn)) {
        //     String jandiRsltMsg = notifyService.notifySchdulReg(trgetTopic, result, logParam);
        //     rsltMsg = rsltMsg + "\n" + jandiRsltMsg;
        // }
    }

    /**
     * 스케줄에 '나' 포함시키기 :: 메소드 분리
     *
     * @param schdulDto 적용할 객체
     */
    public void setMeToSchdul(final SchdulDto schdulDto) {
        List<SchdulPrtcpntDto> prtcpntList = schdulDto.getPrtcpntList();
        if (CollectionUtils.isEmpty(prtcpntList)) prtcpntList = new ArrayList<>();
        // 내이름 있는지 체크
        final SchdulPrtcpntDto isMe = new SchdulPrtcpntDto(AuthUtils.getLgnUserId());
        if (!prtcpntList.contains(isMe)) prtcpntList.add(isMe);
        schdulDto.setPrtcpntList(prtcpntList);
    }

    /**
     * 일정관리 > 일정 수정
     * Clears Cache : hldyEntityList, isHldy, isHldyOrWeekend
     */
    @Override
    @Transactional
    public ServiceResponse modify(final SchdulDto modifyDto) throws Exception {
        // 수정 전처리
        this.preModify(modifyDto);

        final SchdulEntity modifyEntity = this.getDtlEntity(modifyDto);       // Entity 레벨 조회
        final boolean wasSingleDate = DateUtils.isSameDay(modifyEntity.getBgnDt(), modifyEntity.getEndDt());
        final boolean isInvalidEndDate = modifyDto.getBgnDt()
                                            .compareTo(modifyDto.getEndDt()) > 0;
        if (wasSingleDate || isInvalidEndDate) modifyDto.setEndDt(modifyDto.getBgnDt());
        mapstruct.updateFromDto(modifyDto, modifyEntity);
        // update
        final SchdulEntity updatedEntity = this.updt(modifyEntity);
        final SchdulDto updatedDto = mapstruct.toDto(updatedEntity);

        return ServiceResponse.builder()
                .rslt(updatedEntity.getPostNo() != null)
                .rsltObj(updatedDto)
                .build();
    }

    /**
     * DB에 저장된 공휴일 정보 조회
     */
    @Cacheable(cacheNames = "hldyEntityList")
    public List<SchdulEntity> getHldyEntityList() throws Exception {
        final Map<String, Object> searchParamMap = new HashMap<>() {{
            put("schdulCd", Constant.SCHDUL_HLDY);
        }};

        return this.getListEntity(searchParamMap);
    }

    /**
     * 공휴일여부 반환
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "isHldy", key = "#date")
    public Boolean isHldy(final Object date) throws Exception {
        // 공휴일 여부 체크
        final Date asDate = DateUtils.asDate(date);
        final Date sDate = DateUtils.Parser.sDateParse(asDate);
        final Optional<SchdulEntity> schdulDtlWrapper = repository.findBySchdulCdAndBgnDt(Constant.SCHDUL_HLDY, sDate);

        return schdulDtlWrapper.isPresent();
    }

    /**
     * 공휴일 또는 주말여부 반환
     */
    @Cacheable(cacheNames = "isHldyOrWeekend", key = "#date")
    public Boolean isHldyOrWeekend(final Object date) throws Exception {
        return (this.getSelf().isHldy(date) || DateUtils.isWeekend(date));
    }

    /**
     * 이번달의 첫 번째 평일 반환
     */
    public Date getFirstBsnsDayInCurrMnth() throws Exception {
        Date keyDt = DateUtils.asDate(DateUtils.getCurrYyMnthStr() + "01");
        while (true) {
            if (!this.getSelf().isHldyOrWeekend(keyDt)) return keyDt;
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
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    public void evictCache(final SchdulEntity rslt) throws Exception {
        EhCacheUtils.evictCacheAll("hldyEntityList");
        EhCacheUtils.evictCacheAll("isHldy");
        EhCacheUtils.evictCacheAll("isHldyOrWeekend");
    }
}