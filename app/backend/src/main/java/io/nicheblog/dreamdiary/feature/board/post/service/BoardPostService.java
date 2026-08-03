package io.nicheblog.dreamdiary.feature.board.post.service;

import io.nicheblog.dreamdiary.feature.attachable._shared.service.BaseAttachableService;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.model.PrefixDto;
import io.nicheblog.dreamdiary.feature.board.group.service.BoardPrefixService;
import io.nicheblog.dreamdiary.feature.board.post.entity.BoardPostEntity;
import io.nicheblog.dreamdiary.feature.board.post.mapstruct.BoardPostMapstruct;
import io.nicheblog.dreamdiary.feature.board.post.model.BoardPostDto;
import io.nicheblog.dreamdiary.feature.board.post.repository.jpa.BoardPostRepository;
import io.nicheblog.dreamdiary.feature.board.post.spec.BoardPostSpec;
import io.nicheblog.dreamdiary.feature.file.service.BaseMultipartWritableService;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * BoardPostService
 * <pre>
 *  게시판 게시물 서비스 모듈.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class BoardPostService
        implements BaseAttachableService<BoardPostDto, BoardPostDto, Integer, BoardPostEntity>, BaseMultipartWritableService<BoardPostDto, BoardPostDto, Integer, BoardPostEntity> {

    @Getter
    private final BoardPostRepository repository;
    @Getter
    private final BoardPostSpec spec;
    @Getter
    private final BoardPostMapstruct mapstruct = BoardPostMapstruct.INSTANCE;

    public BoardPostMapstruct getReadMapstruct() {
        return this.mapstruct;
    }
    public BoardPostMapstruct getWriteMapstruct() {
        return this.mapstruct;
    }

    private final BoardPrefixService boardPrefixService;

    /**
     * 목록 {@code Page<Entity>} -> {@code Page<Dto>} 변환 (override)
     *
     * @param entityPage 페이징 처리된 Entity 목록
     * @return {@link Page} -- 변환된 페이징 처리된 Dto 목록
     */
    @Override
    public Page<BoardPostDto> pageEntityToDto(final Page<BoardPostEntity> entityPage) throws Exception {
        final List<BoardPostDto> dtoList = new ArrayList<>();
        int i = 0;
        for (BoardPostEntity entity : entityPage.getContent()) {
            final BoardPostDto listDto = mapstruct.toDto(entity);
            listDto.setRnum(CmmUtils.getPageRnum(entityPage, i));
            dtoList.add(listDto);
            i++;
        }

        return new PageImpl<>(dtoList, entityPage.getPageable(), entityPage.getTotalElements());
    }

    /**
     * default: 상세 페이지 조회
     *
     * @param key 조회수를 증가시킬 항목의 키
     * @return Dto -- 조회된 객체
     */
    @Transactional
    public BoardPostDto viewDtlPage(final Integer key) throws Exception {

        // 조회수 증가
        // this.hitCntUp(key);

        return this.getDtlDto(key);
    }

    /** 게시글 본문과 GLOBAL Scope 소속 단일 Prefix 연결을 같은 트랜잭션에서 등록한다. */
    @Override
    @Transactional
    public ServiceResponse regist(final BoardPostDto registDto) throws Exception {
        final ServiceResponse response = BaseAttachableService.super.regist(registDto);
        final BoardPostDto updatedDto = (BoardPostDto) response.getRsltObj();
        applyPrefixSelection(updatedDto, registDto.getPrefixId());
        return response;
    }

    /** 게시글 본문과 GLOBAL Scope 소속 단일 Prefix 연결을 같은 트랜잭션에서 수정한다. */
    @Override
    @Transactional
    public ServiceResponse modify(final BoardPostDto modifyDto) throws Exception {
        final ServiceResponse response = BaseAttachableService.super.modify(modifyDto);
        final BoardPostDto updatedDto = (BoardPostDto) response.getRsltObj();
        applyPrefixSelection(updatedDto, modifyDto.getPrefixId());
        return response;
    }

    /**
     * 선택한 Prefix의 GLOBAL Scope·활성 상태를 검증하고 prefix_content 연결을 반영한다.
     * 변경 전에는 게시글 엔티티의 직접 FK를 교체하고 다시 저장했다. 변경 후에는 게시글의
     * {@code (id, boardKey)} attachable 키를 사용해 공통 연결만 생성·교체·해제한다.
     */
    private void applyPrefixSelection(final BoardPostDto dto, final Integer prefixId) {
        final BoardPostEntity entity = repository.findById(dto.getId())
                .orElseThrow(() -> new javax.persistence.EntityNotFoundException("Board post not found."));
        final PrefixEntity prefix = boardPrefixService.applySelection(
                entity.getContentType(),
                entity.getAttachableKey(),
                prefixId
        );
        dto.setPrefix(prefix == null ? null : PrefixDto.builder()
                .id(prefix.getId())
                .name(prefix.getName())
                .color(prefix.getColor())
                .sortOrder(prefix.getSortOrder())
                .activeYn(prefix.getActiveYn())
                .build());
        dto.setPrefixId(prefixId);
        log.info("[BoardPost] Prefix 연결 반영. postId={}, boardKey={}, prefixId={}",
                entity.getId(), entity.getContentType(), prefixId);
    }
}
