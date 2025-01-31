package io.nicheblog.dreamdiary.domain.board.post.service;

import io.nicheblog.dreamdiary.domain.board.post.entity.BoardPostEntity;
import io.nicheblog.dreamdiary.domain.board.post.mapstruct.BoardPostMapstruct;
import io.nicheblog.dreamdiary.domain.board.post.model.BoardPostDto;
import io.nicheblog.dreamdiary.domain.board.post.repository.jpa.BoardPostRepository;
import io.nicheblog.dreamdiary.domain.board.post.spec.BoardPostSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BasePostService;

import java.util.List;

/**
 * BoardPostService
 * <pre>
 *  게시판 게시물 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface BoardPostService
        extends BasePostService<BoardPostDto.DTL, BoardPostDto.LIST, Integer, BoardPostEntity, BoardPostRepository, BoardPostSpec, BoardPostMapstruct> {

    /**
     * 게시판 > 게시판 상단 고정 목록 조회
     *
     * @param contentType 조회할 컨텐츠 타입
     * @return {@link List} -- 상단 고정 게시물 목록
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    List<BoardPostDto.LIST> getFxdList(final String contentType) throws Exception;
}
