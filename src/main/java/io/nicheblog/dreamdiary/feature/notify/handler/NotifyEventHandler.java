package io.nicheblog.dreamdiary.feature.notify.handler;

import io.nicheblog.dreamdiary.feature.board.post.model.BoardPostDto;
import io.nicheblog.dreamdiary.feature.calendar.schedule.model.ScheduleDto;
import io.nicheblog.dreamdiary.feature.calendar.schedule.service.ScheduleService;
import io.nicheblog.dreamdiary.feature.user.account.service.UserService;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import io.nicheblog.dreamdiary.infrastructure.code.service.CodeLookupService;
import io.nicheblog.dreamdiary.infrastructure.log.event.LogEvent;
import io.nicheblog.dreamdiary.infrastructure.log.model.LogParam;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.messaging.jandi.service.JandiApiService;
import io.nicheblog.dreamdiary.infrastructure.messaging.jandi.type.JandiTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * NotifyService
 * <pre>
 *  알림 관리 서비스 모듈
 *  (중앙통제 Wrapper, 여기저기로 알림 분배)
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class NotifyEventHandler {

    private final ScheduleService scheduleService;
    private final UserService userService;
    private final CodeLookupService codeLookupService;
    private final JandiApiService jandiApiService;
    private final ApplicationEventPublisherWrapper publisher;

    /**
     * 게시판 등록 잔디 알림 메시지 발송
     *
     * @see io.nicheblog.dreamdiary.infrastructure.log.handler.LogEventListener
     */
    public String notifyBoardPostReg(
            final JandiTopic trgetTopic,
            final BoardPostDto result,
            final LogParam logParam
    ) throws Exception {
        String jandiRsltMsg;
        try {
            // title
            final String title = result.getTitle();
            // msg
            final String msg = "새로운 글이 등록되었습니다.";
            // url
            final String param = "id=" + result.getId() + "&contentType=" + result.getContentType() + "&" + Code.UTM_SOURCE + "=jandi";
            final String fullUrl = Url.DOMAIN + Url.BOARD_POST_DETAIL + "?" + param;
            // 메세지 발송
            jandiApiService.sendMsg(trgetTopic, msg, title, fullUrl);
            jandiRsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_JANDI_SUCCESS);
        } catch (final Exception e) {
            logParam.setExceptionInfo(e);
            jandiRsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_JANDI_FAILURE);;
            publisher.publishAsyncEvent(new LogEvent(this, logParam));
        }
        return jandiRsltMsg;
    }

    /**
     * 일정 등록 잔디 알림 메시지 발송
     *
     * @see io.nicheblog.dreamdiary.infrastructure.log.handler.LogEventListener
     */
    public String notifyScheduleReg(
            final JandiTopic trgetTopic,
            final ScheduleDto result,
            final LogParam logParam
    ) {
        String jandiRsltMsg;
        try {
            // title
            final String scheduleTyNm = codeLookupService.getCodeName(Code.SCHEDULE_CD, result.getScheduleCd());
            String title = "[" + scheduleTyNm + "] " + result.getBgnDt() + " / " + result.getScheduleNm();
            String prtcpntStr = result.getPrtcpntListStr();
            if (StringUtils.isNotEmpty(prtcpntStr)) {
                title = "[" + scheduleTyNm + "] " + result.getBgnDt() + " / " + prtcpntStr + " : " + result.getScheduleNm();
            }
            // msg
            final String msg = "새로운 일정이 등록되었습니다.";
            // url
            final String param = Code.UTM_SOURCE + "=jandi";
            final String fullUrl = Url.DOMAIN + Url.SCHEDULE_CAL + "?" + param;
            // 메세지 발송
            jandiApiService.sendMsg(trgetTopic, msg, title, fullUrl);
            jandiRsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_JANDI_SUCCESS);
        } catch (final Exception e) {
            jandiRsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_JANDI_FAILURE);;
            logParam.setResult(false, MessageUtils.getExceptionMsg(e), ActvtyCtgr.JANDI);
            publisher.publishAsyncEvent(new LogEvent(this, logParam));
        }
        return jandiRsltMsg;
    }

    /**
     * 일정 > 생일인 현재 직원에 대하여 알림 발송
     */
    // public Boolean notifyCrdtUserBrthdy(
    //         final LogParam logParam
    // ) throws Exception {
    //     // 생일인 직원 목록 조회
    //     List<UserDto> brthdyUserList = userService.getBrthdyCrdtUser();
    //     if (CollectionUtils.isEmpty(brthdyUserList)) return true;
    //     String jandiRsltMsg = "";
    //     boolean isSuccess = false;
    //     try {
    //         JandiTopic trgetTopic = JandiTopic.SCHEDULE;
    //         for (UserDto user : brthdyUserList) {
    //             // title
    //             String title = "[생일] " + user.getUserNm();
    //             // msg
    //             String msg = "오늘은 " + user.getUserNm() + "님의 생일입니다.";
    //             boolean isLunar = "Y".equals(user.getLunarYn());
    //             String brthdyStr = user.getBrthdy();
    //             if (isLunar) brthdyStr = DateUtils.ChineseCal.solToLunStr(brthdyStr, DatePtn.DATE);
    //             // url
    //             String url = DateUtils.asStr(brthdyStr, DatePtn.BRTHDY) + (isLunar ? "음력" : "");
    //             // 메세지 발송
    //             isSuccess = jandiApiService.sendMsg(trgetTopic, msg, title, url);
    //             jandiRsltMsg = MessageUtils.getMessage(isSuccess ? MessageUtils.RSLT_JANDI_SUCCESS : MessageUtils.getMessage(MessageUtils.RSLT_JANDI_FAILURE););
    //         }
    //         isSuccess = true;
    //     } catch (final Exception e) {
    //         jandiRsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_JANDI_FAILURE);;
    //         logParam.setResult(false, MessageUtils.getExceptionMsg(e), ActvtyCtgr.JANDI);
    //         publisher.publishAsyncEvent(new LogEvent(this, logParam));
    //     }
    //     log.account("{}", jandiRsltMsg);
    //     return isSuccess;
    // }

}
