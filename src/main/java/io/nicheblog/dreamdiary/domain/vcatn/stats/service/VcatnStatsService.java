package io.nicheblog.dreamdiary.domain.vcatn.stats.service;

import io.nicheblog.dreamdiary.domain.schdul.service.SchdulService;
import io.nicheblog.dreamdiary.domain.user.info.mapstruct.UserMapstruct;
import io.nicheblog.dreamdiary.domain.user.info.model.UserDto;
import io.nicheblog.dreamdiary.domain.user.info.service.UserService;
import io.nicheblog.dreamdiary.domain.vcatn.papr.entity.VcatnSchdulEntity;
import io.nicheblog.dreamdiary.domain.vcatn.papr.service.VcatnSchdulService;
import io.nicheblog.dreamdiary.domain.vcatn.stats.entity.VcatnStatsEntity;
import io.nicheblog.dreamdiary.domain.vcatn.stats.entity.VcatnStatsKey;
import io.nicheblog.dreamdiary.domain.vcatn.stats.mapstruct.VcatnStatsMapstruct;
import io.nicheblog.dreamdiary.domain.vcatn.stats.model.VcatnStatsDto;
import io.nicheblog.dreamdiary.domain.vcatn.stats.model.VcatnStatsTotalDto;
import io.nicheblog.dreamdiary.domain.vcatn.stats.model.VcatnStatsYyDto;
import io.nicheblog.dreamdiary.domain.vcatn.stats.repository.jpa.VcatnStatsRepository;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * VcatnStatsService
 * <pre>
 *  휴가관리 > 년도별 휴가관리 통계 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service("vcatnStatsService")
@RequiredArgsConstructor
@Log4j2
public class VcatnStatsService {

    private final VcatnStatsMapstruct vcatnStatsMapstruct = VcatnStatsMapstruct.INSTANCE;
    private final UserMapstruct userMapstruct = UserMapstruct.INSTANCE;
    private final UserService userService;
    private final VcatnSchdulService vcatnSchdulService;
    private final VcatnStatsYyService vcatnStatsYyService;
    private final VcatnStatsRepository vcatnStatsRepository;
    private final SchdulService schdulService;

    /**
     * 휴가관리 > 년도별 휴가관리 통계 목록 조회
     * 해당년도에 근무이력이 있는(중도퇴사 포함) 모든 직원(재직+프리랜서) 전원에 대하여 산정
     *
     * @param statsYy 조회할 통계 기준 년도 정보를 담고 있는 VcatnStatsYyDto 객체
     * @return {@link List} -- 해당 연도에 대한 휴가 관리 통계 목록
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    public List<VcatnStatsDto> getVcatnStatsList(final VcatnStatsYyDto statsYy) throws Exception {
        final String yyStr = statsYy.getStatsYy();
        // 직원목록 조회 :: 해당년도에 근무이력이 있는(중도퇴사 포함) 모든 직원(재직+프리랜서) 전원에 대하여 산정
        final List<UserDto.LIST> userList = userService.getCrdtUserList(yyStr);

        final List<VcatnStatsDto> stats = new ArrayList<>();
        for (final UserDto.LIST user : userList) {
            // 각 직원에 대하여 휴가일자 산정하여 목록 반환 :: 메소드 분리
            final VcatnStatsDto vcantStats = this.getVcatnStats(user, statsYy);
            stats.add(vcantStats);
        }

        return stats;
    }

    /**
     * 휴가관리 > (개인) 휴가현황 조회
     *
     * @param statsYy 조회할 통계 기준 년도 정보를 담고 있는 VcatnStatsYyDto 객체
     * @param userId 조회할 사용자의 ID
     * @return 개인의 휴가 현황 정보를 담고 있는 VcatnStatsDto 객체
     * @throws Exception 조회 중 발생할 수 있는 예외
     * @throws NullPointerException 입사일 정보가 존재하지 않는 경우
     */
    public VcatnStatsDto getVcatnStatsDtl(
            final VcatnStatsYyDto statsYy,
            final String userId
    ) throws Exception {
        final UserDto.DTL user = userService.getDtlDto(userId);
        final UserDto.LIST userDtl = userMapstruct.dtlToList(user);
        if (StringUtils.isEmpty(userDtl.getEcnyDt())) throw new NullPointerException(MessageUtils.getMessage("user.emplym.ecny-dt.empty"));

        return this.getCalcVcatnStats(userDtl, statsYy);
    }

    /**
     * 휴가관리 > 년도별 휴가관리 통계 > 개인별 휴가 현황 조회 (메소드 분리)
     *
     * @param user 조회할 사용자 정보
     * @param statsYy 조회할 통계 기준 년도 정보
     * @return {@link VcatnStatsDto} -- 개인별 휴가 현황 정보
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    private VcatnStatsDto getCalcVcatnStats(
            final UserDto.LIST user,
            final VcatnStatsYyDto statsYy
    ) throws Exception {

        // 사용자 ID로 검색한 휴가 일정을 불러온다.
        final VcatnStatsDto vcatnStats = this.getVcatnStats(user, statsYy);

        // 입사일, 기준일로 근속년수 산청
        final LocalDate ecnyDt = DateUtils.asLocalDate(user.getEcnyDt());
        final LocalDate stdrdDt = DateUtils.asLocalDate(DateUtils.getDateAddDay(statsYy.getBgnDt(), -1));
        final Period ecnyPeriod = Period.between(ecnyDt, stdrdDt);
        // 1. 입사일부터 기준일까지 산정하여 내년 연차 산정
        int bsYryc = 0, newbieMnthYryc = 0;
        final int basicBsYryc = 15;
        // 신입(~입사 1년차) = 입사 1년차까지 1달에 1개 휴가 발생
        // 신입 이외 = 기본연자 15개
        final int workYy = ecnyPeriod.getYears();
        final boolean isNewbie = (workYy == 0);
        if (!isNewbie) {
            bsYryc = basicBsYryc;
        } else {
            final boolean ecnyBeforeStdrdDt = ecnyDt.isBefore(stdrdDt);
            if (ecnyBeforeStdrdDt) {
                // 기준일 이전에 입사한 경우 = 기준일~입사일+1년까지는 1달에 1개, 그 이후로는 연차 계산 (기간에 따른 비율)
                final LocalDate newbieOutDt = DateUtils.asLocalDate(DateUtils.getDateAddYy(ecnyDt, 1));
                final Period newbiePeriod = Period.between(stdrdDt, newbieOutDt);
                newbieMnthYryc = Math.min(newbiePeriod.getMonths(), 11);
                int afterNewbieDay = Math.max((int) ChronoUnit.DAYS.between(newbieOutDt, DateUtils.asLocalDate(statsYy.getEndDt())), 0);
                bsYryc = (afterNewbieDay * 15) / 365;
            } else {
                // 기준일 이후에 입사한 경우 = 입사일~종료일까지 1달에 1개
                Period newbiePeriod = Period.between(ecnyDt, DateUtils.asLocalDate(statsYy.getEndDt()));
                int ecnyMnth = (int) newbiePeriod.toTotalMonths();
                newbieMnthYryc = Math.min(ecnyMnth, 11);
            }
        }
        vcatnStats.setCnwkYy(workYy);
        vcatnStats.setBsYryc(bsYryc);
        if (isNewbie) {
            vcatnStats.setIsNewbie(true);
            vcatnStats.setNewbieYryc(newbieMnthYryc);
        }

        // 총 소진 연차를 산정한다. :: 메소드 분리
        final double exhsYryc = this.getTotVcatnDy(statsYy, vcatnStats, user.getUserId());
        vcatnStats.setExhsYryc(exhsYryc);

        return vcatnStats;
    }

    /**
     * 총 소진 연차를 산정한다. :: 메소드 분리
     *
     * @param statsYy 조회할 통계 기준 년도 정보
     * @param vcatnStats 휴가 현황 정보
     * @param userId 조회할 사용자의 ID
     * @return 총 소진 연차를 나타내는 double 값
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    private double getTotVcatnDy(VcatnStatsYyDto statsYy, VcatnStatsDto vcatnStats, @NotEmpty String userId) throws Exception {
        // 1. 전체 휴가 현황 조회
        final Map<String, Object> searchParamMap = vcatnStatsYyService.getVcatnYyDtMap(statsYy);
        final Date statsYyBgnDt = DateUtils.asDate(searchParamMap.get("searchStartDt"));
        final Date statsYyEndDt = DateUtils.asDate(searchParamMap.get("searchEndDt"));
        vcatnStats.setStatsDt(DateUtils.getDateAddDayStr(statsYyBgnDt, DatePtn.DATE, -1));
        searchParamMap.put("userId", userId);
        final List<VcatnSchdulEntity> vcatnEntityList = vcatnSchdulService.getListEntity(searchParamMap);

        // 2. 전체 휴가 소진일자 산정
        double totVcatnDy = 0.0;
        for (final VcatnSchdulEntity vcatn : vcatnEntityList) {
            // 연차(반차)만 계산한다. 공가, 무급/생리휴가, 경조휴가 등은 패스
            final String vcatnTy = vcatn.getVcatnCd();
            final List<String> nonAnnuals = Arrays.asList(Constant.VCATN_PBLEN, Constant.VCATN_UNPAID, Constant.VCATN_MNSTR, Constant.VCATN_CTSNN);
            if (nonAnnuals.contains((vcatnTy))) continue;
            // 반차는 0.5일, 연차는 1일 소진
            final boolean isHalf = (Constant.VCATN_AM_HALF.equals(vcatnTy) || Constant.VCATN_PM_HALF.equals(vcatnTy));
            final double exhrDy = isHalf ? 0.5 : Constant.VCATN_ANNUAL.equals(vcatnTy) ? 1 : 0;
//
            // 휴가 시작일자/종료일자 산정 : 2월 28일 전- 또는 3월 1일 후 식으로 경계에 걸쳐있는 경우를 따진다. (딱 경계까지만 일수 계산하기)
            Date vcatnBgnDt = DateUtils.Parser.sDateParse(vcatn.getBgnDt());
            Date vcatnEndDt = DateUtils.Parser.sDateParse(vcatn.getEndDt());
            assert vcatnBgnDt != null;
            if (vcatnBgnDt.compareTo(statsYyBgnDt) < 0) vcatnBgnDt = statsYyBgnDt;
            vcatnEndDt = (vcatnEndDt == null) ? vcatnBgnDt : (vcatnEndDt.compareTo(statsYyEndDt) > 0) ? statsYyEndDt : vcatnEndDt;
//
            // 날짜 훑으면서 공휴일 또는 주말여부 체크
            Date keyDt = DateUtils.Parser.sDateParse(vcatnBgnDt);
            while (true) {
                assert keyDt != null;
                if (!(keyDt.compareTo(vcatnEndDt) <= 0)) break;
                if (schdulService.isHldyOrWeekend(keyDt)) {
                    keyDt = DateUtils.getDateAddDay(keyDt, 1);
                    continue;
                }
                totVcatnDy += exhrDy;
                keyDt = DateUtils.getDateAddDay(keyDt, 1);
            }
        }

        return totVcatnDy;
    }

    /**
     * 사용자 ID로 검색한 휴가일정을 불러온다.
     *
     * @param user 조회할 사용자 정보
     * @param statsYy 조회할 통계 기준 년도 정보
     * @return {@link VcatnStatsDto} -- 해당 사용자에 대한 휴가 현황 정보
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    private VcatnStatsDto getVcatnStats(UserDto.LIST user, VcatnStatsYyDto statsYy) throws Exception {
        // 사용자 ID로 검색한 휴가 일정을 불러온다.
        final String userId = user.getUserId();
        final VcatnStatsKey key = new VcatnStatsKey(statsYy.getStatsYy(), userId);
        final Optional<VcatnStatsEntity> entityWrapper = vcatnStatsRepository.findById(key);
        final VcatnStatsEntity rsEntity = entityWrapper.orElse(new VcatnStatsEntity());
        final VcatnStatsDto vcatnStats = vcatnStatsMapstruct.toDto(rsEntity);
        vcatnStats.setUser(user);

        return vcatnStats;
    }

    /**
     * 휴가관리 > 년도별 휴가관리 통계 > 휴가 현황 저장
     *
     * @param vcatnStatsTotal 저장할 통계 정보
     * @return {@link boolean} -- 저장 성공 여부
     * @throws Exception 저장 중 발생할 수 있는 예외
     */
    public boolean regStatsTotal(final VcatnStatsTotalDto vcatnStatsTotal) throws Exception {
        final List<VcatnStatsDto> statsList = vcatnStatsTotal.getStatsList();
        final List<VcatnStatsEntity> statsEntityList = new ArrayList<>();
        for (final VcatnStatsDto stats : statsList) {
            if (StringUtils.isEmpty(stats.getUserId())) continue;
            stats.setStatsYy(vcatnStatsTotal.getStatsYy());
            final VcatnStatsKey key = new VcatnStatsKey(stats.getStatsYy(), stats.getUserId());
            final Optional<VcatnStatsEntity> entityWrapper = vcatnStatsRepository.findById(key);
            final VcatnStatsEntity rsEntity = entityWrapper.orElse(new VcatnStatsEntity());
            vcatnStatsMapstruct.updateFromDto(stats, rsEntity);
            statsEntityList.add(rsEntity);
        }
        vcatnStatsRepository.saveAll(statsEntityList);

        return true;
    }

    /**
     * 휴가관리 > 년도별 휴가관리 통계 엑셀 다운로드 목록 조회
     * 해당년도에 근무이력이 있는(중도퇴사 포함) 모든 직원(재직+프리랜서) 전원에 대하여 산정
     *
     * @param statsYy 조회할 통계 기준 년도
     * @return {@link List} -- 엑셀 다운로드를 위한 통계 목록
     * @throws Exception 조회 중 발생할 수 있는 예외
     */
    public List<Object> getVcatnStatsListXlsx(final VcatnStatsYyDto statsYy) throws Exception {
        final List<VcatnStatsDto> statsList = this.getVcatnStatsList(statsYy);
        final List<Object> statsObjList = new ArrayList<>();
        for (final VcatnStatsDto stats : statsList) {
            statsObjList.add(vcatnStatsMapstruct.toListXlsxDto(stats));
        }

        return statsObjList;
    }
}
