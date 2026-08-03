package io.nicheblog.dreamdiary.feature.board.group.service;

import io.nicheblog.dreamdiary.feature.board.group.entity.BoardEntity;
import io.nicheblog.dreamdiary.feature.board.group.jpa.BoardRepository;
import io.nicheblog.dreamdiary.feature.board.group.model.BoardDto;
import io.nicheblog.dreamdiary.feature.board.group.spec.BoardSpec;
import io.nicheblog.dreamdiary.feature.board.post.repository.jpa.BoardPostRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 게시판 boardKey의 등록 충돌·수정 불변 계약 테스트.
 * <p>
 * 변경 전 Prefix Scope 등록 테스트는 게시판 FK 공유 계약을 검증했다. GLOBAL Scope는
 * 첫 Prefix 등록 시 lazy 생성하므로 게시판 서비스는 Scope를 만들지 않고, 이 테스트는
 * 영속 content type으로 승격된 boardKey 경계를 검증한다.
 * </p>
 *
 * @author nichefish
 */
class BoardServiceBoardKeyTest {

    private static final String FIXTURE_BOARD_KEY = "FIXTURE_BOARD";

    @Test
    void preRegistAllowsUniqueDynamicBoardKey() throws Exception {
        final BoardRepository repository = mock(BoardRepository.class);
        when(repository.findByBoardKey(FIXTURE_BOARD_KEY)).thenReturn(Optional.empty());
        final BoardDto request = BoardDto.builder().boardKey(FIXTURE_BOARD_KEY).build();

        service(repository).preRegist(request);

        assertEquals(FIXTURE_BOARD_KEY, request.getBoardKey());
    }

    @Test
    void preRegistRejectsFixedContentTypeKey() {
        final BoardDto request = BoardDto.builder().boardKey("journal_thread").build();

        assertThrows(IllegalStateException.class, () -> service().preRegist(request));
    }

    @Test
    void preModifyKeepsExistingBoardKey() throws Exception {
        final BoardDto request = BoardDto.builder().id(1).boardKey(FIXTURE_BOARD_KEY).build();
        final BoardEntity entity = BoardEntity.builder().id(1).boardKey(FIXTURE_BOARD_KEY).build();

        service().preModify(request, entity);

        assertEquals(FIXTURE_BOARD_KEY, request.getBoardKey());
    }

    @Test
    void preModifyRejectsBoardKeyChange() {
        final BoardDto request = BoardDto.builder().id(1).boardKey("FIXTURE_CHANGED").build();
        final BoardEntity entity = BoardEntity.builder().id(1).boardKey(FIXTURE_BOARD_KEY).build();

        assertThrows(IllegalStateException.class, () -> service().preModify(request, entity));
    }

    private BoardService service() {
        return service(mock(BoardRepository.class));
    }

    private BoardService service(final BoardRepository repository) {
        return new BoardService(
                repository,
                mock(BoardSpec.class),
                mock(ApplicationContext.class),
                mock(BoardPostRepository.class)
        );
    }
}
