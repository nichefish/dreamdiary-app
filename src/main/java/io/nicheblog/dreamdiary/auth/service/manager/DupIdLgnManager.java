package io.nicheblog.dreamdiary.auth.service.manager;

import io.nicheblog.dreamdiary.auth.handler.LgnSuccessHandler;
import io.nicheblog.dreamdiary.auth.handler.LgoutHandler;
import io.nicheblog.dreamdiary.auth.handler.SessionDestroyListener;
import lombok.extern.log4j.Log4j2;

import javax.servlet.http.HttpSessionListener;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DupIdLgnManager
 * <pre>
 *  중복 로그인 체크 관련 메모리 캐시 매니저
 *  로그인시 array에 userId 저장, 로그아웃 및 세션만료시 userId 제거
 * </pre>
 *
 * @author nichefish
 * @see LgnSuccessHandler,LgoutHandler,SessionDestroyListener
 */
@Log4j2
public class DupIdLgnManager
        implements HttpSessionListener {

    /** 로그인 아이디 목록을 담을 Map. */
    private static final ConcurrentHashMap<String, Boolean> lgnIdMap = new ConcurrentHashMap<>();

    /* ----- */

    /**
     * 중복 로그인 여부를 체크합니다.
     *
     * @param compareId 중복 여부를 확인할 사용자 ID (String)
     * @return {@link Boolean} -- 중복 로그인인 경우 true, 그렇지 않으면 false
     */
    public synchronized static boolean isDupIdLgn(final String compareId) {
        return lgnIdMap.containsKey(compareId);
    }

    /**
     * 사용자 ID를 중복 로그인 리스트(lgnIdList)에 추가합니다.
     *
     * @param lgnId 추가할 사용자 ID (String)
     */
    public synchronized static void addKey(final String lgnId) {
        lgnIdMap.put(lgnId, true);
        log.info("userId {} added for dupIdLgnMap.", lgnId);
    }

    /**
     * 사용자 ID를 중복 로그인 리스트(lgnIdList)에서 제거합니다.
     *
     * @param compareId 제거할 사용자 ID (String)
     */
    public synchronized static void removeKey(final String compareId) {
        lgnIdMap.remove(compareId);
        log.info("userId {} removed from dupIdLgnMap.", compareId);
    }
}