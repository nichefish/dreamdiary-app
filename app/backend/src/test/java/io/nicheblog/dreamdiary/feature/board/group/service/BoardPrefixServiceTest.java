package io.nicheblog.dreamdiary.feature.board.group.service;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.model.PrefixDto;
import io.nicheblog.dreamdiary.feature.attachable.prefix.service.PrefixContentService;
import io.nicheblog.dreamdiary.feature.attachable.prefix.service.PrefixService;
import io.nicheblog.dreamdiary.feature.board.group.entity.BoardEntity;
import io.nicheblog.dreamdiary.feature.board.group.jpa.BoardRepository;
import io.nicheblog.dreamdiary.feature.board.group.model.BoardPrefixManagementDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 게시판 boardKey별 GLOBAL Prefix 조회·관리 경계 계약 테스트.
 *
 * @author nichefish
 */
@ExtendWith(MockitoExtension.class)
class BoardPrefixServiceTest {

    private static final String FIXTURE_BOARD_KEY = "FIXTURE_BOARD";
    private static final String FIXTURE_BOARD_NAME = "가상 게시판";
    private static final Integer FIXTURE_PREFIX_ID = 50;

    @Mock
    private BoardRepository boardRepository;
    @Mock
    private PrefixService prefixService;
    @Mock
    private PrefixContentService prefixContentService;
    @InjectMocks
    private BoardPrefixService service;

    @Test
    void getActiveDelegatesToBoardKeyGlobalScope() {
        when(boardRepository.findByBoardKey(FIXTURE_BOARD_KEY)).thenReturn(Optional.of(board()));
        when(prefixService.getActiveGlobal(FIXTURE_BOARD_KEY))
                .thenReturn(List.of(PrefixDto.builder().id(FIXTURE_PREFIX_ID).name("가상 말머리").build()));

        final List<PrefixDto> result = service.getActive(FIXTURE_BOARD_KEY);

        assertEquals(1, result.size());
        assertEquals(FIXTURE_PREFIX_ID, result.get(0).getId());
    }

    @Test
    void getManagementReturnsBoardKeyGlobalPrefixesWithoutSharingMetadata() {
        when(boardRepository.findById(1)).thenReturn(Optional.of(board()));
        when(prefixService.getAllGlobal(FIXTURE_BOARD_KEY))
                .thenReturn(List.of(PrefixDto.builder()
                        .id(FIXTURE_PREFIX_ID)
                        .name("비활성 가상 말머리")
                        .activeYn("N")
                        .build()));

        final BoardPrefixManagementDto result = service.getManagement(1);

        assertEquals(FIXTURE_BOARD_KEY, result.getBoardKey());
        assertEquals(1, result.getPrefixes().size());
        assertEquals("N", result.getPrefixes().get(0).getActiveYn());
    }

    @Test
    void createDelegatesGlobalLazyCreationByBoardKey() {
        final PrefixDto request = PrefixDto.builder()
                .name("신규 가상 말머리")
                .sortOrder(0)
                .build();
        final PrefixDto expected = request.toBuilder().id(FIXTURE_PREFIX_ID).build();
        when(boardRepository.findById(1)).thenReturn(Optional.of(board()));
        when(prefixService.createGlobal(
                FIXTURE_BOARD_KEY, request, "board-admin:1:" + FIXTURE_BOARD_KEY
        )).thenReturn(expected);

        assertSame(expected, service.create(1, request));
    }

    @Test
    void updateDelegatesGlobalScopeBoundary() {
        final PrefixDto request = PrefixDto.builder()
                .name("수정 가상 말머리")
                .sortOrder(1)
                .build();
        when(boardRepository.findById(1)).thenReturn(Optional.of(board()));

        service.update(1, FIXTURE_PREFIX_ID, request);

        verify(prefixService).updateGlobal(
                FIXTURE_BOARD_KEY,
                FIXTURE_PREFIX_ID,
                request,
                "board-admin:1:" + FIXTURE_BOARD_KEY
        );
    }

    @Test
    void applySelectionChecksBoardAndDelegatesGlobalContentConnection() {
        final BaseAttachableKey attachableKey = new BaseAttachableKey(70, FIXTURE_BOARD_KEY);
        final PrefixEntity expected = PrefixEntity.builder().id(FIXTURE_PREFIX_ID).build();
        when(boardRepository.findByBoardKey(FIXTURE_BOARD_KEY)).thenReturn(Optional.of(board()));
        when(prefixContentService.applyGlobalSelection(
                attachableKey,
                FIXTURE_BOARD_KEY,
                FIXTURE_PREFIX_ID,
                "board-post:70:" + FIXTURE_BOARD_KEY
        )).thenReturn(expected);

        final PrefixEntity result = service.applySelection(
                FIXTURE_BOARD_KEY, attachableKey, FIXTURE_PREFIX_ID
        );

        assertSame(expected, result);
    }

    @Test
    void getActiveRejectsMissingBoard() {
        when(boardRepository.findByBoardKey(FIXTURE_BOARD_KEY)).thenReturn(Optional.empty());

        assertThrows(javax.persistence.EntityNotFoundException.class, () -> service.getActive(FIXTURE_BOARD_KEY));
        verifyNoInteractions(prefixService);
    }

    private BoardEntity board() {
        return BoardEntity.builder()
                .id(1)
                .boardKey(FIXTURE_BOARD_KEY)
                .boardName(FIXTURE_BOARD_NAME)
                .build();
    }
}
