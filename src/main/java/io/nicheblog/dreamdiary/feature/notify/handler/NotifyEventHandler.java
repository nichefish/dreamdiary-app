package io.nicheblog.dreamdiary.feature.notify.handler;

import io.nicheblog.dreamdiary.feature.board.notice.model.NoticeDto;
import io.nicheblog.dreamdiary.feature.board.post.model.BoardPostDto;
import io.nicheblog.dreamdiary.feature.schdul.model.SchdulDto;
import io.nicheblog.dreamdiary.feature.schdul.service.SchdulService;
import io.nicheblog.dreamdiary.feature.user.info.service.UserService;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.cd.Code;
import io.nicheblog.dreamdiary.infrastructure.cd.service.CdLookupService;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.log.sys.event.LogSysEvent;
import io.nicheblog.dreamdiary.infrastructure.log.sys.handler.LogSysEventListener;
import io.nicheblog.dreamdiary.infrastructure.log.sys.model.LogSysParam;
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
@Service("notifyService")
@RequiredArgsConstructor
@Log4j2
public class NotifyEventHandler {

    private final SchdulService schdulService;
    private final UserService userService;
    private final CdLookupService cdLookupService;
    private final JandiApiService jandiApiService;
    private final ApplicationEventPublisherWrapper publisher;

    /**
     * 공지사항 등록 잔디 알림 메시지 발송
     *
     * @see LogSysEventListener
     */
    public String notifyNoticeReg(
            final JandiTopic trgetTopic,
            final NoticeDto result,
            final LogSysParam logParam
    ) throws Exception {
        String jandiRsltMsg;
        try {
            // title
            final String title = result.getFullTitle();
            // msg
            final String msg = "새로운 공지사항이 등록되었습니다.";
            // url
            final String param = "postNo=" + result.getPostNo() + "&boardDef=" + result.getContentType() + "&" + Code.UTM_SOURCE + "=jandi";
            final String fullUrl = Url.DOMAIN + Url.NOTICE_DTL + "?" + param;
            // 메세지 발송
            jandiApiService.sendMsg(trgetTopic, msg, title, fullUrl);
            jandiRsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_JANDI_SUCCESS);
        } catch (final Exception e) {
            jandiRsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_JANDI_FAILURE);;
            logParam.setResult(false, MessageUtils.getExceptionMsg(e), ActvtyCtgr.JANDI);
            publisher.publishAsyncEvent(new LogSysEvent(this, logParam));
        }
        return jandiRsltMsg;
    }

    /**
     * 게시판 등록 잔디 알림 메시지 발송
     *
     * @see LogSysEventListener
     */
    public String notifyBoardPostReg(
            final JandiTopic trgetTopic,
            final BoardPostDto result,
            final LogSysParam logParam
    ) throws Exception {
        String jandiRsltMsg;
        try {
            // title
            final String title = result.getTitle();
            // msg
            final String msg = "새로운 글이 등록되었습니다.";
            // url
            final String param = "postNo=" + result.getPostNo() + "&boardDef=" + result.getBoardDef() + "&" + Code.UTM_SOURCE + "=jandi";
            final String fullUrl = Url.DOMAIN + Url.BOARD_POST_DTL + "?" + param;
            // 메세지 발송
            jandiApiService.sendMsg(trgetTopic, msg, title, fullUrl);
            jandiRsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_JANDI_SUCCESS);
        } catch (final Exception e) {
            logParam.setExceptionInfo(e);
            jandiRsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_JANDI_FAILURE);;
            publisher.publishAsyncEvent(new LogSysEvent(this, logParam));
        }
        return jandiRsltMsg;
    }

    /**
     * 일정 등록 잔디 알림 메시지 발송
     *
     * @see LogSysEventListener
     */
    public String notifySchdulReg(
            final JandiTopic trgetTopic,
            final SchdulDto result,
            final LogSysParam logParam
    ) {
        String jandiRsltMsg;
        try {
            // title
            final String schdulTyNm = cdLookupService.getDtlCdNm(Code.SCHDUL_CD, result.getSchdulCd());
            String title = "[" + schdulTyNm + "] " + result.getBgnDt() + " / " + result.getSchdulNm();
            String prtcpntStr = result.getPrtcpntListStr();
            if (StringUtils.isNotEmpty(prtcpntStr)) {
                title = "[" + schdulTyNm + "] " + result.getBgnDt() + " / " + prtcpntStr + " : " + result.getSchdulNm();
            }
            // msg
            final String msg = "새로운 일정이 등록되었습니다.";
            // url
            final String param = Code.UTM_SOURCE + "=jandi";
            final String fullUrl = Url.DOMAIN + Url.SCHDUL_CAL + "?" + param;
            // 메세지 발송
            jandiApiService.sendMsg(trgetTopic, msg, title, fullUrl);
            jandiRsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_JANDI_SUCCESS);
        } catch (final Exception e) {
            jandiRsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_JANDI_FAILURE);;
            logParam.setResult(false, MessageUtils.getExceptionMsg(e), ActvtyCtgr.JANDI);
            publisher.publishAsyncEvent(new LogSysEvent(this, logParam));
        }
        return jandiRsltMsg;
    }

    /**
     * 일정 > 생일인 현재 직원에 대하여 알림 발송
     */
    // public Boolean notifyCrdtUserBrthdy(
    //         final LogSysParam logParam
    // ) throws Exception {
    //     // 생일인 직원 목록 조회
    //     List<UserDto> brthdyUserList = userService.getBrthdyCrdtUser();
    //     if (CollectionUtils.isEmpty(brthdyUserList)) return true;
    //     String jandiRsltMsg = "";
    //     boolean isSuccess = false;
    //     try {
    //         JandiTopic trgetTopic = JandiTopic.SCHDUL;
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
    //         publisher.publishAsyncEvent(new LogSysEvent(this, logParam));
    //     }
    //     log.info("{}", jandiRsltMsg);
    //     return isSuccess;
    // }

}
