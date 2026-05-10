package io.nicheblog.dreamdiary.feature.board.group.service;

import io.nicheblog.dreamdiary.feature.admin.menu.model.SiteAcsInfo;
import io.nicheblog.dreamdiary.feature.board.group.entity.BoardEntity;
import io.nicheblog.dreamdiary.feature.board.group.jpa.BoardRepository;
import io.nicheblog.dreamdiary.feature.board.group.mapstruct.BoardMapstruct;
import io.nicheblog.dreamdiary.feature.board.group.model.BoardDto;
import io.nicheblog.dreamdiary.feature.board.group.spec.BoardSpec;
import io.nicheblog.dreamdiary.feature.board.group.type.ReservedStructuralBoard;
import io.nicheblog.dreamdiary.feature.board.post.repository.jpa.BoardPostRepository;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoWritableService;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseSortableService;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * BoardService
 * <pre>
 *  게시판 그룹 도메인 서비스.
 *  게시판 그룹의 CRUD/정렬/사용여부 처리와 메뉴 캐시 갱신을 담당한다.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class BoardService
        implements BaseDtoWritableService<BoardDto, BoardDto, Integer, BoardEntity>,
                   BaseSortableService<BoardDto, Integer, BoardEntity> {

    /** 게시판 저장소 */
    @Getter
    private final BoardRepository repository;
    /** 게시판 검색 스펙 */
    @Getter
    private final BoardSpec spec;
    /** 게시판 매핑 도구 */
    @Getter
    private final BoardMapstruct mapstruct = BoardMapstruct.INSTANCE;

    /** 자기 자신 프록시 조회용 컨텍스트 */
    private final ApplicationContext context;

    /** 게시글 저장소 (게시판별 글 건수 집계용) */
    private final BoardPostRepository boardPostRepository;

    /**
     * 트랜잭션/캐시 AOP 적용을 위해 자기 자신 프록시를 반환한다.
     *
     * @return BoardService 프록시 빈
     */
    private BoardService getSelf() {
        return context.getBean(this.getClass());
    }

    /**
     * 읽기용 Mapstruct를 반환한다.
     *
     * @return BoardMapstruct 구현체
     */
    public BoardMapstruct getReadMapstruct() {
        return this.mapstruct;
    }

    /**
     * 쓰기용 Mapstruct를 반환한다.
     *
     * @return BoardMapstruct 구현체
     */
    public BoardMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    /**
     * 사이드바/헤더용 게시판 메뉴 목록을 조회한다.
     * <p>
     * 사용중(`useYn=Y`) 게시판만 조회하고 메뉴 접근 정보로 변환한다.
     * </p>
     *
     * @return 게시판 메뉴 접근 정보 목록
     * @throws Exception 조회/매핑 중 예외
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "boardMenuList")
    public List<SiteAcsInfo> boardMenuList() throws Exception {
        final Map<String, Object> searchParamMap = new HashMap<>() {{
            put("useYn", "Y");
        }};
        final List<BoardEntity> boardList = this.getSelf().getListEntity(searchParamMap);

        return boardList.stream()
                .map(entity -> {
                    try {
                        return mapstruct.toMenu(entity);
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 게시판 키로 메뉴 접근 정보를 조회한다.
     *
     * @param boardKey 게시판 키
     * @return 메뉴 접근 정보
     * @throws Exception 조회/매핑 중 예외
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "boardMenu", key = "#boardKey")
    public SiteAcsInfo getMenuByBoardKey(final String boardKey) throws Exception {
        final BoardEntity retrievedEntity = this.getDtlEntityByBoardKey(boardKey);
        return mapstruct.toMenu(retrievedEntity);
    }

    /**
     * 게시판 키로 상세 DTO를 조회한다.
     *
     * @param boardKey 게시판 키
     * @return 게시판 상세 DTO
     * @throws Exception 조회/매핑 중 예외
     */
    @Transactional(readOnly = true)
    public BoardDto getDtlDtoByBoardKey(final String boardKey) throws Exception {
        return mapstruct.toDto(this.getDtlEntityByBoardKey(boardKey));
    }

    /**
     * 게시판 키로 상세 엔티티를 조회한다.
     *
     * @param boardKey 게시판 키
     * @return 게시판 엔티티
     * @throws EntityNotFoundException 대상이 없을 때
     */
    @Transactional(readOnly = true)
    public BoardEntity getDtlEntityByBoardKey(final String boardKey) {
        return repository.findByBoardKey(boardKey)
                .orElseThrow(() -> new EntityNotFoundException("exception.EntityNotFoundException"));
    }

    /**
     * 게시판 관리 목록: 페이징 Dto 에 게시글 건수를 채운다.
     *
     * @param searchParam 검색 파라미터
     * @param pageable    페이징
     * @return 게시글 건수가 세팅된 페이지
     */
    @Override
    @Transactional(readOnly = true)
    public Page<BoardDto> getPageDto(final BaseSearchParam searchParam, final Pageable pageable) throws Exception {
        final Page<BoardDto> page = BaseDtoWritableService.super.getPageDto(searchParam, pageable);
        this.applyPostCounts(page.getContent());
        return page;
    }

    /**
     * {@code board_post.content_type} 기준으로 목록 행의 {@link BoardDto#getPostCount()} 를 채운다.
     *
     * @param boards 현재 페이지 행
     */
    private void applyPostCounts(final List<BoardDto> boards) {
        if (boards == null || boards.isEmpty()) {
            return;
        }
        final List<String> keys = boards.stream()
                .map(BoardDto::getBoardKey)
                .filter(k -> k != null && !k.isBlank())
                .distinct()
                .collect(Collectors.toList());
        if (keys.isEmpty()) {
            return;
        }
        final List<Object[]> rows = boardPostRepository.countGroupedByContentTypeIn(keys);
        final Map<String, Long> countMap = new HashMap<>();
        if (rows != null) {
            for (final Object[] row : rows) {
                if (row == null || row.length < 2 || row[0] == null || row[1] == null) {
                    continue;
                }
                countMap.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
            }
        }
        for (final BoardDto b : boards) {
            final String k = b.getBoardKey();
            if (k == null || k.isBlank()) {
                b.setPostCount(0L);
                continue;
            }
            b.setPostCount(countMap.getOrDefault(k, 0L));
        }
    }

    /**
     * 등록 전처리. {@code board_key} 중복 및 구조적 예약 키를 선검사한다.
     * <p>
     * 동작: (변경 전) DB unique 제약에만 의존해 {@link org.springframework.dao.DataIntegrityViolationException} 으로 실패할 수 있었음.
     * (변경 후) {@link ReservedStructuralBoard} 예약 키는 관리 화면 등록 API 에서 <strong>무조건</strong> 거부한다. (시드·스크립트는 별도)
     * 이어서 동일 키가 이미 있으면 {@link IllegalStateException}(메시지 키)으로 거부한다.
     * </p>
     *
     * @param registDto 등록 Dto
     */
    @Override
    public void preRegist(final BoardDto registDto) throws Exception {
        if (StringUtils.isBlank(registDto.getBoardKey())) {
            return;
        }
        final String boardKey = registDto.getBoardKey().trim();
        registDto.setBoardKey(boardKey);
        if (ReservedStructuralBoard.isStructuralReservedKey(boardKey)) {
            log.warn("Board preRegist rejected: structural reserved board_key not allowed via admin regist. key={}", boardKey);
            throw new IllegalStateException("msg.board.group.board-key.reserved-forbidden");
        }
        if (repository.findByBoardKey(boardKey).isEmpty()) {
            return;
        }
        log.warn("Board preRegist rejected: board_key already exists. key={}", boardKey);
        throw new IllegalStateException("msg.board.group.board-key.duplicate");
    }

    /**
     * 수정 전처리. 예약 키로의 신규 지정을 막고, {@code board_key} 가 다른 행과 중복되게 바꾸는 경우를 차단한다.
     * <p>
     * 이미 DB 에 예약 키를 갖는 행(시드)은 동일 키 유지 수정만 허용한다.
     * </p>
     *
     * @param postDto   수정 Dto
     * @param modifyEntity 수정 대상 엔티티
     */
    @Override
    public void preModify(final BoardDto postDto, final BoardEntity modifyEntity) throws Exception {
        if (StringUtils.isBlank(postDto.getBoardKey())) {
            return;
        }
        final String boardKey = postDto.getBoardKey().trim();
        postDto.setBoardKey(boardKey);
        final String existingKey = modifyEntity.getBoardKey() == null ? "" : modifyEntity.getBoardKey().trim();
        if (ReservedStructuralBoard.isStructuralReservedKey(boardKey) && !boardKey.equals(existingKey)) {
            log.warn("Board preModify rejected: cannot assign structural reserved board_key. key={}, existingKey={}", boardKey,
                    existingKey);
            throw new IllegalStateException("msg.board.group.board-key.reserved-forbidden");
        }
        repository.findByBoardKey(boardKey).ifPresent(other -> {
            if (!Objects.equals(other.getId(), postDto.getKey())) {
                final String msgKey = ReservedStructuralBoard.isStructuralReservedKey(boardKey)
                        ? "msg.board.group.board-key.reserved-in-use"
                        : "msg.board.group.board-key.duplicate";
                log.warn("Board preModify rejected: board_key conflicts with another row. key={}, thisId={}, otherId={}, reservedStructural={}",
                        boardKey, postDto.getKey(), other.getId(), ReservedStructuralBoard.isStructuralReservedKey(boardKey));
                throw new IllegalStateException(msgKey);
            }
        });
    }

    /**
     * 등록 후처리.
     * <p>
     * 게시판 메뉴 목록 캐시를 비운다.
     * </p>
     *
     * @param updatedDto 등록 완료 DTO
     */
    @Override
    public void postRegist(final BoardDto updatedDto) {
        EhCacheUtils.clearCache("boardMenuList");
    }

    /**
     * 수정 후처리.
     * <p>
     * 게시판 메뉴 목록 캐시와 해당 게시판 메뉴 캐시를 무효화한다.
     * </p>
     *
     * @param postDto 수정 요청 DTO
     * @param updatedDto 수정 완료 DTO
     */
    @Override
    public void postModify(final BoardDto postDto, final BoardDto updatedDto) {
        EhCacheUtils.clearCache("boardMenuList");
        EhCacheUtils.evictCacheByKey("boardMenu", updatedDto.getBoardKey());
    }

    /**
     * 삭제 후처리.
     * <p>
     * 게시판 메뉴 목록 캐시와 해당 게시판 메뉴 캐시를 무효화한다.
     * </p>
     *
     * @param deletedDto 삭제된 게시판 DTO
     */
    @Override
    public void postDelete(final BoardDto deletedDto) {
        EhCacheUtils.clearCache("boardMenuList");
        EhCacheUtils.evictCacheByKey("boardMenu", deletedDto.getBoardKey());
    }

    /**
     * 사용/미사용 변경 후처리.
     * <p>
     * 게시판 메뉴 목록 캐시와 해당 게시판 메뉴 캐시를 무효화한다.
     * </p>
     *
     * @param updatedEntity 사용여부가 갱신된 엔티티
     */
    @Override
    public void postSetUse(final BoardEntity updatedEntity) {
        EhCacheUtils.clearCache("boardMenuList");
        EhCacheUtils.evictCacheByKey("boardMenu", updatedEntity.getBoardKey());
    }

    /**
     * 정렬 변경 후처리.
     * <p>
     * 메뉴 목록/단건 캐시를 모두 비운다.
     * </p>
     *
     * @param sortOrders 정렬 순서 목록
     */
    @Override
    public void postSortOrder(final List<BoardDto> sortOrders) {
        EhCacheUtils.clearCache("boardMenuList");
        EhCacheUtils.clearCache("boardMenu");
    }
}
