package io.nicheblog.dreamdiary.adapter._common.handler;

import io.nicheblog.dreamdiary.adapter.jandi.JandiTopic;
import io.nicheblog.dreamdiary.adapter.jandi.service.JandiApiService;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.domain.board.post.model.BoardPostDto;
import io.nicheblog.dreamdiary.domain.board.notice.model.NoticeDto;
import io.nicheblog.dreamdiary.domain.schdul.model.SchdulDto;
import io.nicheblog.dreamdiary.domain.schdul.service.SchdulService;
import io.nicheblog.dreamdiary.domain.user.info.service.UserService;
import io.nicheblog.dreamdiary.domain.vcatn.papr.model.VcatnPaprDto;
import io.nicheblog.dreamdiary.extension.cd.service.DtlCdService;
import io.nicheblog.dreamdiary.extension.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.extension.log.sys.event.LogSysEvent;
import io.nicheblog.dreamdiary.extension.log.sys.handler.LogSysEventListener;
import io.nicheblog.dreamdiary.extension.log.sys.model.LogSysParam;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
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
    private final DtlCdService dtlCdService;
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
            final String param = "postNo=" + result.getPostNo() + "&boardDef=" + result.getContentType() + "&" + Constant.UTM_SOURCE + "=jandi";
            final String fullUrl = Url.DOMAIN + Url.NOTICE_DTL + "?" + param;
            // 메세지 발송
            jandiApiService.sendMsg(trgetTopic, msg, title, fullUrl);
            jandiRsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_JANDI_SUCCESS);
        } catch (final Exception e) {
            jandiRsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_JANDI_FAILURE);
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
            final String param = "postNo=" + result.getPostNo() + "&boardDef=" + result.getBoardDef() + "&" + Constant.UTM_SOURCE + "=jandi";
            final String fullUrl = Url.DOMAIN + Url.BOARD_POST_DTL + "?" + param;
            // 메세지 발송
            jandiApiService.sendMsg(trgetTopic, msg, title, fullUrl);
            jandiRsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_JANDI_SUCCESS);
        } catch (final Exception e) {
            logParam.setExceptionInfo(e);
            jandiRsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_JANDI_FAILURE);
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
            final String schdulTyNm = dtlCdService.getDtlCdNm(Constant.SCHDUL_CD, result.getSchdulCd());
            String title = "[" + schdulTyNm + "] " + result.getBgnDt() + " / " + result.getSchdulNm();
            String prtcpntStr = result.getPrtcpntListStr();
            if (StringUtils.isNotEmpty(prtcpntStr)) {
                title = "[" + schdulTyNm + "] " + result.getBgnDt() + " / " + prtcpntStr + " : " + result.getSchdulNm();
            }
            // msg
            final String msg = "새로운 일정이 등록되었습니다.";
            // url
            final String param = Constant.UTM_SOURCE + "=jandi";
            final String fullUrl = Url.DOMAIN + Url.SCHDUL_CAL + "?" + param;
            // 메세지 발송
            jandiApiService.sendMsg(trgetTopic, msg, title, fullUrl);
            jandiRsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_JANDI_SUCCESS);
        } catch (final Exception e) {
            jandiRsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_JANDI_FAILURE);
            logParam.setResult(false, MessageUtils.getExceptionMsg(e), ActvtyCtgr.JANDI);
            publisher.publishAsyncEvent(new LogSysEvent(this, logParam));
        }
        return jandiRsltMsg;
    }

    /**
     * 휴가계획서 등록 잔디 알림 메시지 발송
     *
     * @see LogSysEventListener
     */
    public String notifyVcatnPaprReg(
            final JandiTopic trgetTopic,
            final VcatnPaprDto result,
            final LogSysParam logParam
    ) {
        String jandiRsltMsg;
        try {
            // title
            String userNm = AuthUtils.getLgnUserNm();
            String title = userNm + " " + result.getTitle();
            // msg
            // String msg = isReg ? "새로운 휴가 계획서가 등록되었습니다." : "휴가 계획서가 수정되었습니다.";
            String msg = "새로운 휴가 계획서가 등록되었습니다.";
            // url
            final String param = "postNo=" + result.getPostNo() + "&boardDef=" + result.getContentType() + "&" + Constant.UTM_SOURCE + "=jandi";
            final String fullUrl = Url.DOMAIN + Url.VCATN_PAPR_DTL + "?" + param;
            // 메세지 발송
            jandiApiService.sendMsg(trgetTopic, msg, title, fullUrl);
            jandiRsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_JANDI_SUCCESS);
        } catch (final Exception e) {
            jandiRsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_JANDI_FAILURE);
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
    //     List<UserDto.LIST> brthdyUserList = userService.getBrthdyCrdtUser();
    //     if (CollectionUtils.isEmpty(brthdyUserList)) return true;
    //     String jandiRsltMsg = "";
    //     boolean isSuccess = false;
    //     try {
    //         JandiTopic trgetTopic = JandiTopic.SCHDUL;
    //         for (UserDto.LIST user : brthdyUserList) {
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
    //             jandiRsltMsg = MessageUtils.getMessage(isSuccess ? MessageUtils.RSLT_JANDI_SUCCESS : MessageUtils.RSLT_JANDI_FAILURE);
    //         }
    //         isSuccess = true;
    //     } catch (final Exception e) {
    //         jandiRsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_JANDI_FAILURE);
    //         logParam.setResult(false, MessageUtils.getExceptionMsg(e), ActvtyCtgr.JANDI);
    //         publisher.publishAsyncEvent(new LogSysEvent(this, logParam));
    //     }
    //     log.info("{}", jandiRsltMsg);
    //     return isSuccess;
    // }

}