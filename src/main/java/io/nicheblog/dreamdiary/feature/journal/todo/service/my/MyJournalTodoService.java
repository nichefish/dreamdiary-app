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
     * 목록 조회 (dto level) :: 캐시 처리
     *
     * @param searchParam 검색조건을 담고 있는 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    public List<JournalTodoDto> getMyListDtoWithCache(final JournalTodoSearchParam searchParam) throws Exception {
        final String username = AuthUtils.requireLgnUsername();
        return journalTodoService.getListDtoWithCacheByUser(username, searchParam);
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 일련번호
     * @return {@link JournalTodoDto} -- 조회된 객체
     */
    public JournalTodoDto getMyDtlDtoWithCache(final Integer key) throws Exception {
        final String username = AuthUtils.requireLgnUsername();
        return journalTodoService.getDtlDtoWithCacheByUser(username, key);
    }
}
