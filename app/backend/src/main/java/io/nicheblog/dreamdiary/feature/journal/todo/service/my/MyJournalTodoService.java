package io.nicheblog.dreamdiary.feature.journal.todo.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.journal.todo.model.JournalTodoDto;
import io.nicheblog.dreamdiary.feature.journal.todo.model.JournalTodoSearchParam;
import io.nicheblog.dreamdiary.feature.journal.todo.service.JournalTodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MyJournalTodoService
 * <pre>
 *  로그인 사용자 기준 저널 할 일 서비스
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MyJournalTodoService {

    private final JournalTodoService journalTodoService;

    /**
     * 활성 할일 목록 조회 (dto level).
     *
     * @param searchParam 검색조건을 담고 있는 파라미터 객체 (yy/mnth 는 무시)
     * @return {@link List} -- 활성 할일 목록
     */
    public List<JournalTodoDto> getMyActiveList(final JournalTodoSearchParam searchParam) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        return journalTodoService.getActiveListDtoByUser(username, searchParam);
    }

    /**
     * 전 범위 할일 목록 조회 (dto level).
     * <pre>
     *  keep 전수관리 화면용 -- 활성(OPEN·PENDING)뿐 아니라 RESOLVED 까지 포함해
     *  사용자의 모든 할일을 발생지 무관하게 반환한다.
     * </pre>
     *
     * @param searchParam 검색조건을 담고 있는 파라미터 객체 (yy/mnth 는 무시)
     * @return {@link List} -- 사용자 전 범위 할일 목록
     */
    public List<JournalTodoDto> getMyList(final JournalTodoSearchParam searchParam) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        return journalTodoService.getListDtoWithLifecycleByUser(username, searchParam);
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 일련번호
     * @return {@link JournalTodoDto} -- 조회된 객체
     */
    public JournalTodoDto getMyDetailDtoWithCache(final Integer key) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        return journalTodoService.getDetailDtoWithCacheByUser(username, key);
    }
}
