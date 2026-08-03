package io.nicheblog.dreamdiary.feature.board.group.service;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.model.PrefixDto;
import io.nicheblog.dreamdiary.feature.attachable.prefix.service.PrefixContentService;
import io.nicheblog.dreamdiary.feature.attachable.prefix.service.PrefixService;
import io.nicheblog.dreamdiary.feature.board.group.entity.BoardEntity;
import io.nicheblog.dreamdiary.feature.board.group.jpa.BoardRepository;
import io.nicheblog.dreamdiary.feature.board.group.model.BoardPrefixManagementDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;

/**
 * 게시판 boardKey를 GLOBAL Prefix Scope content type으로 해석하는 도메인 서비스.
 * <p>
 * 변경 전에는 {@code board.prefix_scope_id}로 Scope를 직접 참조하고 여러 게시판이 같은
 * Scope를 공유할 수 있었다. 변경 후에는 각 게시판이 {@code GLOBAL + boardKey}로 독립적인
 * 목록을 가지며 첫 Prefix 등록 시 Scope를 lazy 생성한다. 게시판 존재·관리 문맥만 이 서비스가
 * 확정하고 말머리 자체의 불변식은 {@link PrefixService}가 담당한다.
 * </p>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class BoardPrefixService {

    private final BoardRepository boardRepository;
    private final PrefixService prefixService;
    private final PrefixContentService prefixContentService;

    /** 게시판에서 신규 선택 가능한 활성 말머리를 정렬 조회한다. */
    @Transactional(readOnly = true)
    public List<PrefixDto> getActive(final String boardKey) {
        final BoardEntity board = requireBoard(boardKey);
        return prefixService.getActiveGlobal(board.getBoardKey());
    }

    /**
     * 관리자 화면에서 게시판 boardKey의 비활성 포함 GLOBAL Prefix를 조회한다.
     * Scope 식별자는 외부 관리 계약에 노출하지 않는다.
     */
    @Transactional(readOnly = true)
    public BoardPrefixManagementDto getManagement(final Integer boardId) {
        final BoardEntity board = requireBoard(boardId);
        return BoardPrefixManagementDto.builder()
                .boardId(board.getId())
                .boardKey(board.getBoardKey())
                .boardName(board.getBoardName())
                .prefixes(prefixService.getAllGlobal(board.getBoardKey()))
                .build();
    }

    /** 관리자 요청으로 게시판 boardKey의 GLOBAL Scope에 새 Prefix를 등록한다. */
    @Transactional
    public PrefixDto create(final Integer boardId, final PrefixDto request) {
        final BoardEntity board = requireBoard(boardId);
        return prefixService.createGlobal(board.getBoardKey(), request, managementContext(board));
    }

    /** 관리자 요청으로 게시판 boardKey의 GLOBAL Scope 소속 Prefix를 수정한다. */
    @Transactional
    public PrefixDto update(final Integer boardId, final Integer prefixId, final PrefixDto request) {
        final BoardEntity board = requireBoard(boardId);
        return prefixService.updateGlobal(
                board.getBoardKey(), prefixId, request, managementContext(board)
        );
    }

    /** 관리자 요청으로 게시판 boardKey의 GLOBAL Scope 소속 Prefix의 활성 상태를 변경한다. */
    @Transactional
    public void setActive(final Integer boardId, final Integer prefixId, final boolean active) {
        final BoardEntity board = requireBoard(boardId);
        prefixService.setActiveGlobal(
                board.getBoardKey(), prefixId, active, managementContext(board)
        );
    }

    /**
     * 활성 게시판 존재를 확인하고 게시글의 Prefix 선택을 GLOBAL attachable 연결로 반영한다.
     * 게시판 확인과 Scope content type 확정은 이 서비스가 맡고, 연결 upsert는 공통
     * {@link PrefixContentService}에 위임한다.
     *
     * @param boardKey 게시판 영속 키
     * @param attachableKey 게시글 attachable 키
     * @param prefixId 선택할 Prefix ID. null이면 선택 해제.
     * @return 반영된 Prefix 엔티티. 선택 해제 시 null.
     */
    @Transactional
    public PrefixEntity applySelection(
            final String boardKey,
            final BaseAttachableKey attachableKey,
            final Integer prefixId
    ) {
        final BoardEntity board = requireBoard(boardKey);
        return prefixContentService.applyGlobalSelection(
                attachableKey,
                board.getBoardKey(),
                prefixId,
                "board-post:" + attachableKey.getId() + ":" + board.getBoardKey()
        );
    }

    private BoardEntity requireBoard(final String boardKey) {
        return boardRepository.findByBoardKey(boardKey)
                .orElseThrow(() -> {
                    log.warn("[BoardPrefix] 게시판 조회 실패. boardKey={}", boardKey);
                    return new EntityNotFoundException("Board not found.");
                });
    }

    private BoardEntity requireBoard(final Integer boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> {
                    log.warn("[BoardPrefix] 관리자 게시판 조회 실패. boardId={}", boardId);
                    return new EntityNotFoundException("Board not found.");
                });
    }

    private String managementContext(final BoardEntity board) {
        return "board-admin:" + board.getId() + ":" + board.getBoardKey();
    }
}
