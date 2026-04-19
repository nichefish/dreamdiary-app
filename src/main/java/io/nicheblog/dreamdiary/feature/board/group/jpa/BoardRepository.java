package io.nicheblog.dreamdiary.feature.board.group.jpa;

import io.nicheblog.dreamdiary.feature.board.group.entity.BoardEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoardRepository extends BaseStreamRepository<BoardEntity, Integer> {

    Optional<BoardEntity> findByBoardKey(String boardKey);
}
