package io.nicheblog.dreamdiary.feature.board.post.mapstruct;

import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixContentEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixScopeEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.embed.PrefixEmbed;
import io.nicheblog.dreamdiary.feature.attachable.prefix.type.PrefixScopeType;
import io.nicheblog.dreamdiary.feature.board.post.entity.BoardPostEntity;
import io.nicheblog.dreamdiary.feature.board.post.model.BoardPostDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 게시글 Prefix 조회 DTO 매핑 계약 테스트.
 *
 * @author nichefish
 */
class BoardPostMapstructTest {

    @Test
    void toDtoMapsPrefixDisplayAndForeignKey() throws Exception {
        final PrefixEntity prefix = PrefixEntity.builder()
                .id(80)
                .scope(PrefixScopeEntity.builder().id(70).scopeType(PrefixScopeType.GLOBAL).build())
                .name("가상 말머리")
                .sortOrder(1)
                .activeYn("Y")
                .build();
        final BoardPostEntity entity = BoardPostEntity.builder()
                .id(90)
                .contentType("FIXTURE_BOARD")
                .title("가상 게시글")
                .prefix(PrefixEmbed.builder()
                        .list(List.of(PrefixContentEntity.builder()
                                .prefixId(prefix.getId())
                                .prefix(prefix)
                                .build()))
                        .build())
                .build();

        final BoardPostDto result = BoardPostMapstruct.INSTANCE.toDto(entity);

        assertEquals(80, result.getPrefixId());
        assertNotNull(result.getPrefix());
        assertEquals("가상 말머리", result.getPrefix().getName());
    }
}
