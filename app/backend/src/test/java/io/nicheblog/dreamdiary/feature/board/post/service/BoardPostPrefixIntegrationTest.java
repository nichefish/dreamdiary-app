package io.nicheblog.dreamdiary.feature.board.post.service;

import io.nicheblog.dreamdiary.auth.security.config.TestAuditConfig;
import io.nicheblog.dreamdiary.auth.security.exception.NotAuthorizedException;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixContentEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.model.PrefixDto;
import io.nicheblog.dreamdiary.feature.attachable.prefix.repository.jpa.PrefixContentRepository;
import io.nicheblog.dreamdiary.feature.board.group.entity.BoardEntity;
import io.nicheblog.dreamdiary.feature.board.group.entity.BoardEntityTestFactory;
import io.nicheblog.dreamdiary.feature.board.group.jpa.BoardRepository;
import io.nicheblog.dreamdiary.feature.board.group.service.BoardPrefixService;
import io.nicheblog.dreamdiary.feature.board.post.model.BoardPostDto;
import io.nicheblog.dreamdiary.feature.board.post.model.BoardPostSearchParam;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 게시글과 게시판별 GLOBAL Prefix의 실제 저장·검색 Scope 계약을 검증한다.
 *
 * @author nichefish
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestAuditConfig.class)
@Transactional
class BoardPostPrefixIntegrationTest {

    private static final String FIXTURE_BOARD_A_KEY = "IT_PREFIX_BOARD_A";
    private static final String FIXTURE_BOARD_B_KEY = "IT_PREFIX_BOARD_B";
    private static final String FIXTURE_PREFIX_NAME = "Integration Board Prefix";
    private static final String FIXTURE_OTHER_PREFIX_NAME = "Integration Other Board Prefix";

    @Resource
    private BoardPostService boardPostService;
    @Resource
    private BoardPrefixService boardPrefixService;
    @Resource
    private BoardRepository boardRepository;
    @Resource
    private PrefixContentRepository prefixContentRepository;
    @PersistenceContext
    private EntityManager entityManager;

    private BoardEntity boardA;
    private BoardEntity boardB;

    /** 테스트마다 독립된 게시판 두 개를 트랜잭션 안에 준비한다. */
    @BeforeEach
    void setUp() throws Exception {
        boardA = boardRepository.saveAndFlush(BoardEntityTestFactory.create(FIXTURE_BOARD_A_KEY));
        boardB = boardRepository.saveAndFlush(BoardEntityTestFactory.create(FIXTURE_BOARD_B_KEY));
    }

    /** 게시글 등록은 (postId, boardKey)에 GLOBAL Scope Prefix 연결을 저장한다. */
    @Test
    void registPersistsPrefixConnectionWithBoardKeyRef() throws Exception {
        final PrefixDto prefix = createPrefix(boardA, FIXTURE_PREFIX_NAME);

        final BoardPostDto registered = registPost(boardA.getBoardKey(), prefix.getId(), "Selected post");
        entityManager.flush();
        entityManager.clear();
        final PrefixContentEntity connection = requireConnection(
                registered.getId(), boardA.getBoardKey());

        assertEquals(prefix.getId(), connection.getPrefixId());
        assertEquals(prefix.getId(), registered.getPrefixId());
        assertNotNull(registered.getPrefix());
        assertEquals(prefix.getId(), registered.getPrefix().getId());
    }

    /** 다른 게시판 GLOBAL Scope의 Prefix는 게시글에 선택할 수 없다. */
    @Test
    void registRejectsPrefixFromDifferentBoardScope() {
        final PrefixDto boardAPrefix = createPrefix(boardA, FIXTURE_PREFIX_NAME);
        createPrefix(boardB, FIXTURE_OTHER_PREFIX_NAME);
        final BoardPostDto request = postRequest(
                boardB.getBoardKey(), boardAPrefix.getId(), "Cross-scope post");

        assertThrows(NotAuthorizedException.class, () -> boardPostService.regist(request));
    }

    /** Prefix 검색은 같은 boardKey의 prefix_content 연결이 있는 게시글만 반환한다. */
    @Test
    void prefixSearchReturnsOnlyMatchingConnectionWithinBoard() throws Exception {
        final PrefixDto boardAPrefix = createPrefix(boardA, FIXTURE_PREFIX_NAME);
        final PrefixDto boardBPrefix = createPrefix(boardB, FIXTURE_OTHER_PREFIX_NAME);
        final BoardPostDto selected = registPost(
                boardA.getBoardKey(), boardAPrefix.getId(), "Selected post");
        registPost(boardA.getBoardKey(), null, "Unselected post");
        registPost(boardB.getBoardKey(), boardBPrefix.getId(), "Other board post");
        entityManager.flush();
        entityManager.clear();

        final List<BoardPostDto> result = boardPostService.getListDto(BoardPostSearchParam.builder()
                .contentType(boardA.getBoardKey())
                .prefixId(boardAPrefix.getId())
                .build());
        final List<BoardPostDto> otherScopeResult = boardPostService.getListDto(BoardPostSearchParam.builder()
                .contentType(boardA.getBoardKey())
                .prefixId(boardBPrefix.getId())
                .build());

        assertEquals(1, result.size());
        assertEquals(selected.getId(), result.get(0).getId());
        assertEquals(boardAPrefix.getId(), result.get(0).getPrefixId());
        assertTrue(otherScopeResult.isEmpty());
    }

    /** 게시글 수정은 기존 연결 행의 Prefix를 같은 게시판 Scope의 다른 활성 Prefix로 교체한다. */
    @Test
    void modifyReplacesExistingPrefixConnection() throws Exception {
        final PrefixDto originalPrefix = createPrefix(boardA, FIXTURE_PREFIX_NAME);
        final PrefixDto replacementPrefix = createPrefix(boardA, FIXTURE_OTHER_PREFIX_NAME);
        final BoardPostDto registered = registPost(
                boardA.getBoardKey(), originalPrefix.getId(), "Original post");

        final BoardPostDto modified = modifyPost(
                registered.getId(), boardA.getBoardKey(), replacementPrefix.getId(), "Modified post");
        entityManager.flush();
        entityManager.clear();
        final PrefixContentEntity connection = requireConnection(
                registered.getId(), boardA.getBoardKey());

        assertEquals(replacementPrefix.getId(), connection.getPrefixId());
        assertEquals(replacementPrefix.getId(), modified.getPrefixId());
        assertEquals(replacementPrefix.getId(), modified.getPrefix().getId());
    }

    /** 게시글 수정에서 Prefix 선택을 비우면 기존 prefix_content 연결을 삭제한다. */
    @Test
    void modifyClearsExistingPrefixConnection() throws Exception {
        final PrefixDto prefix = createPrefix(boardA, FIXTURE_PREFIX_NAME);
        final BoardPostDto registered = registPost(
                boardA.getBoardKey(), prefix.getId(), "Selected post");

        final BoardPostDto modified = modifyPost(
                registered.getId(), boardA.getBoardKey(), null, "Cleared post");
        entityManager.flush();
        entityManager.clear();

        assertFalse(prefixContentRepository.findByRefIdAndRefContentType(
                registered.getId(), boardA.getBoardKey()).isPresent());
        assertNull(modified.getPrefixId());
        assertNull(modified.getPrefix());
    }

    /** 기존 게시글의 동일한 비활성 Prefix는 다른 필드 수정에서도 유지할 수 있다. */
    @Test
    void modifyKeepsSameInactiveHistoricalPrefix() throws Exception {
        final PrefixDto prefix = createPrefix(boardA, FIXTURE_PREFIX_NAME);
        final BoardPostDto registered = registPost(
                boardA.getBoardKey(), prefix.getId(), "Selected post");
        boardPrefixService.setActive(boardA.getId(), prefix.getId(), false);

        final BoardPostDto modified = modifyPost(
                registered.getId(), boardA.getBoardKey(), prefix.getId(), "Modified post");
        entityManager.flush();
        entityManager.clear();
        final PrefixContentEntity connection = requireConnection(
                registered.getId(), boardA.getBoardKey());

        assertEquals(prefix.getId(), connection.getPrefixId());
        assertEquals(prefix.getId(), modified.getPrefixId());
        assertEquals("N", modified.getPrefix().getActiveYn());
    }

    /** 게시글 수정에서도 다른 비활성 Prefix를 새로 선택할 수 없다. */
    @Test
    void modifyRejectsDifferentInactivePrefix() throws Exception {
        final PrefixDto originalPrefix = createPrefix(boardA, FIXTURE_PREFIX_NAME);
        final PrefixDto inactivePrefix = createPrefix(boardA, FIXTURE_OTHER_PREFIX_NAME);
        final BoardPostDto registered = registPost(
                boardA.getBoardKey(), originalPrefix.getId(), "Selected post");
        boardPrefixService.setActive(boardA.getId(), inactivePrefix.getId(), false);

        assertThrows(IllegalStateException.class, () -> modifyPost(
                registered.getId(), boardA.getBoardKey(), inactivePrefix.getId(), "Rejected post"));
    }

    private PrefixDto createPrefix(final BoardEntity board, final String name) {
        return boardPrefixService.create(board.getId(), PrefixDto.builder()
                .name(name)
                .color("#6B7280")
                .sortOrder(1)
                .build());
    }

    private BoardPostDto registPost(
            final String boardKey,
            final Integer prefixId,
            final String title
    ) throws Exception {
        final ServiceResponse response = boardPostService.regist(postRequest(boardKey, prefixId, title));
        return (BoardPostDto) response.getRsltObj();
    }

    private BoardPostDto modifyPost(
            final Integer postId,
            final String boardKey,
            final Integer prefixId,
            final String title
    ) throws Exception {
        final BoardPostDto request = postRequest(boardKey, prefixId, title);
        request.setId(postId);
        final ServiceResponse response = boardPostService.modify(request);
        return (BoardPostDto) response.getRsltObj();
    }

    private BoardPostDto postRequest(
            final String boardKey,
            final Integer prefixId,
            final String title
    ) {
        return BoardPostDto.builder()
                .contentType(boardKey)
                .title(title)
                .content("Integration board content")
                .prefixId(prefixId)
                .build();
    }

    private PrefixContentEntity requireConnection(final Integer refId, final String refContentType) {
        final PrefixContentEntity connection = prefixContentRepository
                .findByRefIdAndRefContentType(refId, refContentType)
                .orElse(null);
        assertNotNull(connection);
        return connection;
    }
}
