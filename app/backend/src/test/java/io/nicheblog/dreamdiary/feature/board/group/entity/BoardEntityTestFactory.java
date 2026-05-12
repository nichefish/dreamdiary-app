package io.nicheblog.dreamdiary.feature.board.group.entity;

import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

@UtilityClass
@ActiveProfiles("test")
public class BoardEntityTestFactory {

    public static BoardEntity create(final String boardKey) throws Exception {
        return BoardEntity.builder()
                .boardKey(boardKey)
                .boardName(boardKey + " name")
                .categoryGroupCode("BOARD")
                .description("test board")
                .sortOrder(1)
                .useYn("Y")
                .build();
    }
}
