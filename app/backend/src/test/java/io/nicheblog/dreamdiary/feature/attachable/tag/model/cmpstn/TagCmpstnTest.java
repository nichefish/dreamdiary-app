package io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn;

import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagContentDto;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * TagCmpstnTest
 * <pre>
 *  표시 이름 SSOT(flat name / nested tag.name)와 직렬화용 문자열 목록 NPE 방지 계약.
 * </pre>
 */
class TagCmpstnTest {

    @Test
    void getTagStrList_usesFlatNameWhenNestedTagMissing() {
        final TagContentDto item = TagContentDto.builder().name("alpha").build();
        final TagCmpstn cmpstn = TagCmpstn.builder().list(List.of(item)).build();

        assertEquals(List.of("alpha"), cmpstn.getTagStrList());
    }

    @Test
    void getTagStrList_fallsBackToNestedTagName() {
        final TagContentDto item = TagContentDto.builder()
                .tag(TagDto.builder().name("nested").build())
                .build();
        final TagCmpstn cmpstn = TagCmpstn.builder().list(List.of(item)).build();

        assertEquals(List.of("nested"), cmpstn.getTagStrList());
    }

    @Test
    void getTagStrList_skipsBlankNamesWithoutNpe() {
        final TagContentDto item = TagContentDto.builder().build();
        final TagCmpstn cmpstn = TagCmpstn.builder().list(List.of(item)).build();

        assertEquals(List.of(), cmpstn.getTagStrList());
    }

    @Test
    void resolveDisplayName_prefersFlatOverNested() {
        final TagContentDto item = TagContentDto.builder()
                .name("flat")
                .tag(TagDto.builder().name("nested").build())
                .build();

        assertEquals("flat", item.resolveDisplayName());
        assertEquals(0, item.compareTo(TagContentDto.builder().name("flat").build()));
    }

    @Test
    void compareTo_nullNamesDoNotThrow() {
        final TagContentDto a = TagContentDto.builder().build();
        final TagContentDto b = TagContentDto.builder().name("b").build();

        assertEquals(-1, Integer.signum(a.compareTo(b)));
        assertNull(a.resolveDisplayName());
    }
}