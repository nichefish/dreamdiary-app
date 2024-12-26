package io.nicheblog.dreamdiary.domain.board.def.service.impl;

import io.nicheblog.dreamdiary.domain.admin.menu.SiteMenu;
import io.nicheblog.dreamdiary.domain.board.def.entity.BoardDefEntity;
import io.nicheblog.dreamdiary.domain.board.def.mapstruct.BoardDefMapstruct;
import io.nicheblog.dreamdiary.domain.board.def.model.BoardDefDto;
import io.nicheblog.dreamdiary.domain.board.def.repository.jpa.BoardDefRepository;
import io.nicheblog.dreamdiary.domain.board.def.service.BoardDefService;
import io.nicheblog.dreamdiary.domain.board.def.spec.BoardDefSpec;
import io.nicheblog.dreamdiary.global._common._clsf.state.model.cmpstn.StateCmpstn;
import io.nicheblog.dreamdiary.global.model.SiteAcsInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

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
public class BoardDefServiceImpl
        implements BoardDefService {

    @Getter
    private final BoardDefRepository repository;
    @Getter
    private final BoardDefSpec spec;
    @Getter
    private final BoardDefMapstruct mapstruct = BoardDefMapstruct.INSTANCE;

    /**
     * boardDef 목록 메뉴 조회 (SiteAcsInfo 목록 반환)
     *
     * @return {@link List} -- 게시판 정의 목록을 메뉴 정보로 변환한 리스트
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    public List<SiteAcsInfo> boardDefMenuList() throws Exception {
        Map<String, Object> searchParamMap = new HashMap<>() {{
            put("useYn", "Y");
        }};
        List<BoardDefEntity> boardDefPage = this.getListEntity(searchParamMap);

        return boardDefPage.stream()
                .map(entity -> {
                    try {
                        return mapstruct.toMenu(entity);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * boardCd로 단일 메뉴 조회 (SiteAcsInfo 반환)
     *
     * @return {@link SiteAcsInfo} -- 게시판 정의를 메뉴 정보로 변환하여 반환
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Override
    public SiteAcsInfo getBoardMenu(final String boardCd) throws Exception {
        final BoardDefEntity retrievedEntity = this.getDtlEntity(boardCd);

        return mapstruct.toMenu(retrievedEntity);
    }

    /**
     * 등록 전처리. (override)
     *
     * @param registDto 등록할 객체
     */
    @Override
    public void preRegist(final BoardDefDto registDto) {
        if (registDto.getState() == null) {
            registDto.setState(new StateCmpstn());
        }
    }

    /**
     * 상태 변경 후처리. (override)
     */
    @Override
    public void postSetState(final String boardCd) throws Exception {
        SiteMenu.BOARD.setSubMenuList(this.boardDefMenuList());
    }
}