package io.nicheblog.dreamdiary.feature.attachable.meta.model.cmpstn;

import io.nicheblog.dreamdiary.feature.attachable.meta.model.MetaContentDto;
import io.nicheblog.dreamdiary.feature.attachable.meta.model.MetaDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * MetaCmpstnTest
 * <pre>
 *  표시 이름 SSOT(flat name / nested meta.name)와 getMetaStrList NPE 방지 계약.
 * </pre>
 */
class MetaCmpstnTest {

    @Test
    void getMetaStrList_usesFlatNameWhenNestedMetaMissing() {
        final MetaContentDto item = MetaContentDto.builder().name("weight").build();
        final MetaCmpstn cmpstn = MetaCmpstn.builder().list(List.of(item)).build();

        assertEquals(List.of("weight"), cmpstn.getMetaStrList());
    }

    @Test
    void getMetaStrList_fallsBackToNestedMetaName() {
        final MetaContentDto item = MetaContentDto.builder()
                .meta(MetaDto.builder().name("nested-meta").build())
                .build();
        final MetaCmpstn cmpstn = MetaCmpstn.builder().list(List.of(item)).build();

        assertEquals(List.of("nested-meta"), cmpstn.getMetaStrList());
    }

    @Test
    void getMetaStrList_skipsBlankNamesWithoutNpe() {
        final MetaContentDto item = MetaContentDto.builder().build();
        final MetaCmpstn cmpstn = MetaCmpstn.builder().list(List.of(item)).build();

        assertEquals(List.of(), cmpstn.getMetaStrList());
    }

    @Test
    void getMetaListStr_nullValueUnitDoNotBecomeLiteralNull() {
        final MetaContentDto item = MetaContentDto.builder().name("only-name").build();
        final MetaCmpstn cmpstn = MetaCmpstn.builder().list(List.of(item)).build();

        final String json = cmpstn.getMetaListStr();
        assertEquals(true, json != null && !json.contains("nullnull"));
        assertEquals(true, json.contains("only-name"));
    }

    @Test
    void compareTo_nullNamesDoNotThrow() {
        final MetaContentDto a = MetaContentDto.builder().build();
        final MetaContentDto b = MetaContentDto.builder().name("b").build();

        assertEquals(-1, Integer.signum(a.compareTo(b)));
        assertNull(a.resolveDisplayName());
    }
}