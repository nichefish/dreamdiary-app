package io.nicheblog.dreamdiary.domain.board.post.entity;

import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * BoardPostEntityTestFactory
 * <pre>
 *  게시판 게시물 (JPA) 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class BoardPostEntityTestFactory {

    /**
     * 테스트용 게시물 정의 Entity 생성
     */
    public static BoardPostEntity create(final String boardDef) throws Exception {
        return BoardPostEntity.builder()
                .contentType(boardDef)
                .build();
    }
}
