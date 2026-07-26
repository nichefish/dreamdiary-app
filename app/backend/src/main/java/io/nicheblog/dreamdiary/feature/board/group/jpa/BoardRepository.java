package io.nicheblog.dreamdiary.feature.board.group.jpa;

import io.nicheblog.dreamdiary.feature.board.group.entity.BoardEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends BaseStreamRepository<BoardEntity, Integer> {

    Optional<BoardEntity> findByBoardKey(String boardKey);

    /** 사용중 게시판을 정렬순으로 조회. 사이드바 BOARD 확장 메뉴의 하위 항목 구성에 사용한다. */
    List<BoardEntity> findByUseYnOrderBySortOrderAscIdAsc(String useYn);
}
