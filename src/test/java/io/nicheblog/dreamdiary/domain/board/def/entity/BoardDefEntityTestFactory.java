package io.nicheblog.dreamdiary.domain.board.def.entity;

import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * BoardDefEntityTestFactory
 * <pre>
 *  게시판 정의 (JPA) 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class BoardDefEntityTestFactory {

    /**
     * 테스트용 게시판 정의 Entity 생성
     */
    public static BoardDefEntity create(final String boardDef) throws Exception {
        return BoardDefEntity.builder()
                .boardDef(boardDef)
                .build();
    }
}
