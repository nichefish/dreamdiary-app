package io.nicheblog.dreamdiary.domain.board.def.service;

import io.nicheblog.dreamdiary.domain.board.def.entity.BoardDefEntity;
import io.nicheblog.dreamdiary.domain.board.def.mapstruct.BoardDefMapstruct;
import io.nicheblog.dreamdiary.domain.board.def.model.BoardDefDto;
import io.nicheblog.dreamdiary.domain.board.def.repository.jpa.BoardDefRepository;
import io.nicheblog.dreamdiary.domain.board.def.spec.BoardDefSpec;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseCrudService;
import io.nicheblog.dreamdiary.global._common._clsf.state.service.BaseStateService;
import io.nicheblog.dreamdiary.global.model.SiteAcsInfo;

import java.util.List;

/**
 * BoardDefService
 * <pre>
 *  게시판 정의 서비스 인터페이스.
 *  ※게시판 정의(board_def) = 게시판 분류. 게시판 게시물(board_post)을 1:N으로 관리한다.
 * </pre>
 *
 * @author nichefish
 */
public interface BoardDefService
        extends BaseCrudService<BoardDefDto, BoardDefDto, String, BoardDefEntity, BoardDefRepository, BoardDefSpec, BoardDefMapstruct>,
                BaseStateService<BoardDefDto, BoardDefDto, String, BoardDefEntity, BoardDefRepository, BoardDefSpec, BoardDefMapstruct> {

    /**
     * boardDef 목록 메뉴 조회 (SiteAcsInfo 목록 반환)
     *
     * @return {@link List} -- 게시판 정의 목록을 메뉴 정보로 변환한 리스트
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    List<SiteAcsInfo> boardDefMenuList() throws Exception;

    /**
     * boardDef로 단일 메뉴 조회 (SiteAcsInfo 반환)
     *
     * @return {@link SiteAcsInfo} -- 게시판 정의를 메뉴 정보로 변환하여 반환
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    SiteAcsInfo getMenuByBoardDef(final String boardDef) throws Exception;
}