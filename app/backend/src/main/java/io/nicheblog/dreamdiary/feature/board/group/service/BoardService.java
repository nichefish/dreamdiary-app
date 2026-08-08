package io.nicheblog.dreamdiary.feature.board.group.service;

import io.nicheblog.dreamdiary.feature.admin.menu.model.SiteAcsInfo;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.board.group.entity.BoardEntity;
import io.nicheblog.dreamdiary.feature.board.group.jpa.BoardRepository;
import io.nicheblog.dreamdiary.feature.board.group.mapstruct.BoardMapstruct;
import io.nicheblog.dreamdiary.feature.board.group.model.BoardDto;
import io.nicheblog.dreamdiary.feature.board.group.spec.BoardSpec;
import io.nicheblog.dreamdiary.feature.board.post.repository.jpa.BoardPostRepository;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoWritableService;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseSortableService;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
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
     * 게시판 기본 정보를 등록한다.
     * <p>
     * 변경 전에는 nullable {@code prefixSourceBoardId}로 기존 게시판 Scope를 공유하거나
     * 신규 빈 Scope를 함께 생성했다. 변경 후 게시판은 Scope FK를 가지지 않으며 첫 Prefix
     * 등록 시 {@code GLOBAL + boardKey} Scope를 lazy 생성하므로 등록 트랜잭션은 게시판
     * 기본 정보만 저장한다.
     * </p>
     *
     * @param registDto 등록할 게시판 DTO
     * @return 등록 결과
     */
    @Override
    @Transactional
    public ServiceResponse regist(final BoardDto registDto) throws Exception {
        this.preRegist(registDto);
        final BoardEntity registEntity = getWriteMapstruct().toEntity(registDto);
        this.preRegist(registEntity);

        final BoardEntity updatedEntity = this.updt(registEntity);
        final BoardDto updatedDto = getReadMapstruct().toDto(updatedEntity);
        this.postRegist(updatedDto);

        return ServiceResponse.builder()
                .rslt(updatedDto.getKey() != null)
                .rsltObj(updatedDto)
                .build();
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
                .orElseThrow(() -> new EntityNotFoundException());
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
        if (isFixedContentTypeKey(boardKey)) {
            log.warn("[Board] 등록 거부: 고정 ContentType과 boardKey 충돌. boardKey={}", boardKey);
            throw new IllegalStateException("board.group.board-key.reserved-forbidden");
        }
        if (repository.findByBoardKey(boardKey).isEmpty()) {
            return;
        }
        log.warn("Board preRegist rejected: board_key already exists. key={}", boardKey);
        throw new IllegalStateException("board.group.board-key.duplicate");
    }

    /**
     * 신규 게시판 엔티티 등록 전 Prefix Scope를 사전 생성하지 않는다.
     * <p>
     * 변경 전에는 독립 빈 Scope 또는 명시적으로 선택한 공유 Scope를 게시판 FK에 연결했다.
     * 변경 후에는 첫 Prefix 등록 시 {@code GLOBAL + boardKey} Scope를 lazy 생성하므로
     * 이 훅에서는 소유 관계를 만들지 않고 해당 분기를 구조화 로그로 남긴다.
     * </p>
     */
    @Override
    public void preRegist(final BoardEntity registEntity) {
        log.info("[Board] 신규 게시판 등록: GLOBAL Prefix Scope는 첫 Prefix 등록까지 생성하지 않음. boardKey={}",
                registEntity.getBoardKey());
    }

    /**
     * 수정 전처리. 영속 content type 식별자인 {@code board_key} 변경을 차단한다.
     * <p>
     * 변경 전에는 예약 키·중복만 검사해 일반 boardKey 변경을 허용했다. 변경 후에는
     * 게시글·attachable 연결·라우팅·GLOBAL PrefixScope가 같은 키를 사용하므로 기존 키와
     * 다른 값은 모두 거부한다. 이미 DB에 예약 키를 갖는 시드 행도 동일 키 유지 수정은 허용한다.
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
        if (boardKey.equals(existingKey)) return;
        log.warn("[Board] 수정 거부: boardKey는 생성 후 변경할 수 없음. boardId={}, existingKey={}, requestedKey={}",
                modifyEntity.getId(), existingKey, boardKey);
        throw new IllegalStateException("board.group.board-key.immutable");
    }

    /** 신규 동적 boardKey가 고정 attachable ContentType 키와 충돌하는지 확인한다. */
    private boolean isFixedContentTypeKey(final String boardKey) {
        for (final ContentType contentType : ContentType.values()) {
            if (contentType.getKey().equalsIgnoreCase(boardKey)) return true;
        }
        return false;
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
        this.evictSidebarMenuCache();
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
        this.evictSidebarMenuCache();
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
        this.evictSidebarMenuCache();
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
        this.evictSidebarMenuCache();
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
        this.evictSidebarMenuCache();
        EhCacheUtils.clearCache("boardMenu");
    }

    /**
     * 사이드바 메뉴 캐시를 무효화한다.
     * <p>
     * 게시판은 {@code MenuSiteMenuService.attachBoardSubMenus} 가 BOARD 확장 메뉴의 하위 항목으로 주입하므로,
     * 주입 결과가 사이드바 메뉴 캐시에 함께 담긴다. 게시판이 바뀌면 이 캐시도 비워야
     * 사용자 화면에 반영된다. (locale 별 key 로 나뉘어 있어 리전 전체를 비운다)
     */
    private void evictSidebarMenuCache() {
        EhCacheUtils.clearCache("userMenuList");
        EhCacheUtils.clearCache("userMenuMetaList");
        EhCacheUtils.clearCache("mngrMenuList");
        EhCacheUtils.clearCache("mngrMenuMetaList");
    }
}
