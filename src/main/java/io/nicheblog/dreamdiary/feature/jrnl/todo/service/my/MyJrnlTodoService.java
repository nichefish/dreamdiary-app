package io.nicheblog.dreamdiary.feature.jrnl.todo.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.jrnl.todo.model.JrnlTodoDto;
import io.nicheblog.dreamdiary.feature.jrnl.todo.model.JrnlTodoSearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.todo.service.JrnlTodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MyJrnlTodoService
 * <pre>
 *  로그인 사용자 기준 저널 할 일 서비스
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MyJrnlTodoService {

    private final JrnlTodoService jrnlTodoService;

    /**
     * 목록 조회 (dto level) :: 캐시 처리
     *
     * @param searchParam 검색조건을 담고 있는 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    public List<JrnlTodoDto> getMyListDtoWithCache(final JrnlTodoSearchParam searchParam) throws Exception {
        final String userId = AuthUtils.requireLgnUserId();
        return jrnlTodoService.getListDtoWithCacheByUser(userId, searchParam);
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 일련번호
     * @return {@link JrnlTodoDto} -- 조회된 객체
     */
    public JrnlTodoDto getMyDtlDtoWithCache(final Integer key) throws Exception {
        final String userId = AuthUtils.requireLgnUserId();
        return jrnlTodoService.getDtlDtoWithCacheByUser(userId, key);
    }
}
