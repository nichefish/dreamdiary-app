package io.nicheblog.dreamdiary.auth.security.service.manager;

import io.nicheblog.dreamdiary.auth.security.handler.LoginSuccessHandler;
import lombok.extern.log4j.Log4j2;

import javax.servlet.http.HttpSessionListener;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DupIdLoginManager
 * <pre>
 *  중복 로그인 체크 관련 메모리 캐시 매니저
 *  로그인시 array에 username 저장, 로그아웃 및 세션만료시 username 제거
 * </pre>
 *
 * @author nichefish
 * @see LoginSuccessHandler ,LogoutHandler,SessionDestroyListener
 */
@Log4j2
public class DupIdLoginManager
        implements HttpSessionListener {

    /** 로그인 아이디 목록을 담을 Map. */
    private static final ConcurrentHashMap<String, Boolean> loginIdMap = new ConcurrentHashMap<>();

    /* ----- */

    /**
     * 중복 로그인 여부를 체크합니다.
     *
     * @param compareId 중복 여부를 확인할 사용자 ID (String)
     * @return {@link Boolean} -- 중복 로그인인 경우 true, 그렇지 않으면 false
     */
    public synchronized static boolean isDupIdLogin(final String compareId) {
        return loginIdMap.containsKey(compareId);
    }

    /**
     * 사용자 ID를 중복 로그인 리스트(loginIdList)에 추가합니다.
     *
     * @param loginId 추가할 사용자 ID (String)
     */
    public synchronized static void addKey(final String loginId) {
        loginIdMap.put(loginId, true);
        log.info("username {} added for dupIdLoginMap.", loginId);
    }

    /**
     * 사용자 ID를 중복 로그인 리스트(loginIdList)에서 제거합니다.
     *
     * @param compareId 제거할 사용자 ID (String)
     */
    public synchronized static void removeKey(final String compareId) {
        loginIdMap.remove(compareId);
        log.info("username {} removed from dupIdLoginMap.", compareId);
    }
}
