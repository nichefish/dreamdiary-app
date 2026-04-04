package io.nicheblog.dreamdiary.feature.board.def.service;

import io.nicheblog.dreamdiary.feature.admin.menu.model.SiteAcsInfo;
import io.nicheblog.dreamdiary.feature.board.def.entity.BoardDefEntity;
import io.nicheblog.dreamdiary.feature.board.def.mapstruct.BoardDefMapstruct;
import io.nicheblog.dreamdiary.feature.board.def.model.BoardDefDto;
import io.nicheblog.dreamdiary.feature.board.def.repository.jpa.BoardDefRepository;
import io.nicheblog.dreamdiary.feature.board.def.spec.BoardDefSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoWritableService;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * BoardDefService
 * <pre>
 *  게시판 정의 서비스 모듈.
 *  ※게시판 정의(board_def) = 게시판 분류. 게시판 게시물(board_post)을 1:N으로 관리한다.
 * </pre>
 *
 * @author nichefish
 */
@Service("boardDefService")
@RequiredArgsConstructor
@Log4j2
public class BoardDefService
        implements BaseDtoWritableService<BoardDefDto, BoardDefDto, String, BoardDefEntity> {

    @Getter
    private final BoardDefRepository repository;
    @Getter
    private final BoardDefSpec spec;
    @Getter
    private final BoardDefMapstruct mapstruct = BoardDefMapstruct.INSTANCE;
    public BoardDefMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public BoardDefMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    /**
     * boardDef 목록 메뉴 조회 (SiteAcsInfo 목록 반환)
     *
     * @return {@link List} -- 게시판 정의 목록을 메뉴 정보로 변환한 리스트
     */
    @Transactional(readOnly = true)
    @Cacheable(value="boardDefMenuList")
    public List<SiteAcsInfo> boardDefMenuList() throws Exception {
        final Map<String, Object> searchParamMap = new HashMap<>() {{
            put("useYn", "Y");
        }};
        final List<BoardDefEntity> boardDefPage = this.getListEntity(searchParamMap);

        return boardDefPage.stream()
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
     * boardDef로 단일 메뉴 조회 (SiteAcsInfo 반환)
     *
     * @return {@link SiteAcsInfo} -- 게시판 정의를 메뉴 정보로 변환하여 반환
     */
    @Transactional(readOnly = true)
    @Cacheable(value="boardMenu", key="#boardDef")
    public SiteAcsInfo getMenuByBoardDef(final String boardDef) throws Exception {
        final BoardDefEntity retrievedEntity = this.getDtlEntity(boardDef);

        return mapstruct.toMenu(retrievedEntity);
    }

    /**
     * 등록 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postRegist(final BoardDefDto updatedDto) throws Exception {
        EhCacheUtils.clearCache("boardDefMenuList");
    }

    /**
     * 수정 후처리. (override)
     *
     * @param updatedDto - 등록된 객체
     */
    @Override
    public void postModify(final BoardDefDto postDto, final BoardDefDto updatedDto) throws Exception {
        EhCacheUtils.clearCache("boardDefMenuList");
        EhCacheUtils.evictCacheByKey("boardMenu", updatedDto.getBoardDef());
    }

    /**
     * 삭제 후처리. (override)
     *
     * @param deletedDto - 삭제된 객체
     */
    @Override
    public void postDelete(final BoardDefDto deletedDto) throws Exception {
        EhCacheUtils.clearCache("boardDefMenuList");
        EhCacheUtils.evictCacheByKey("boardMenu", deletedDto.getBoardDef());
    }
}
