package io.nicheblog.dreamdiary.feature.board.group.service;

import io.nicheblog.dreamdiary.feature.admin.menu.model.SiteAcsInfo;
import io.nicheblog.dreamdiary.feature.board.group.entity.BoardEntity;
import io.nicheblog.dreamdiary.feature.board.group.jpa.BoardRepository;
import io.nicheblog.dreamdiary.feature.board.group.mapstruct.BoardMapstruct;
import io.nicheblog.dreamdiary.feature.board.group.model.BoardDto;
import io.nicheblog.dreamdiary.feature.board.group.spec.BoardSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoWritableService;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseSortableService;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class BoardService
        implements BaseDtoWritableService<BoardDto, BoardDto, Integer, BoardEntity>,
                   BaseSortableService<BoardDto, Integer, BoardEntity> {

    @Getter
    private final BoardRepository repository;
    @Getter
    private final BoardSpec spec;
    @Getter
    private final BoardMapstruct mapstruct = BoardMapstruct.INSTANCE;

    private final ApplicationContext context;

    private BoardService getSelf() {
        return context.getBean(this.getClass());
    }

    public BoardMapstruct getReadMapstruct() {
        return this.mapstruct;
    }

    public BoardMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

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

    @Transactional(readOnly = true)
    @Cacheable(value = "boardMenu", key = "#boardKey")
    public SiteAcsInfo getMenuByBoardKey(final String boardKey) throws Exception {
        final BoardEntity retrievedEntity = this.getDtlEntityByBoardKey(boardKey);
        return mapstruct.toMenu(retrievedEntity);
    }

    @Transactional(readOnly = true)
    public BoardDto getDtlDtoByBoardKey(final String boardKey) throws Exception {
        return mapstruct.toDto(this.getDtlEntityByBoardKey(boardKey));
    }

    @Transactional(readOnly = true)
    public BoardEntity getDtlEntityByBoardKey(final String boardKey) {
        return repository.findByBoardKey(boardKey)
                .orElseThrow(() -> new EntityNotFoundException("exception.EntityNotFoundException"));
    }

    @Override
    public void postRegist(final BoardDto updatedDto) {
        EhCacheUtils.clearCache("boardMenuList");
    }

    @Override
    public void postModify(final BoardDto postDto, final BoardDto updatedDto) {
        EhCacheUtils.clearCache("boardMenuList");
        EhCacheUtils.evictCacheByKey("boardMenu", updatedDto.getBoardKey());
    }

    @Override
    public void postDelete(final BoardDto deletedDto) {
        EhCacheUtils.clearCache("boardMenuList");
        EhCacheUtils.evictCacheByKey("boardMenu", deletedDto.getBoardKey());
    }

    @Override
    public void postSetUse(final BoardEntity updatedEntity) {
        EhCacheUtils.clearCache("boardMenuList");
        EhCacheUtils.evictCacheByKey("boardMenu", updatedEntity.getBoardKey());
    }

    @Override
    public void postSortOrder(final List<BoardDto> sortOrders) {
        EhCacheUtils.clearCache("boardMenuList");
        EhCacheUtils.clearCache("boardMenu");
    }
}
