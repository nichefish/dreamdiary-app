package io.nicheblog.dreamdiary.feature.jrnl.day.mapstruct;

import io.nicheblog.dreamdiary.feature.jrnl.day.entity.JrnlDayEntity;
import io.nicheblog.dreamdiary.feature.jrnl.day.entity.JrnlDayEntityTestFactory;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDayDtoTestFactory;
import io.nicheblog.dreamdiary.feature.jrnl.dream.entity.JrnlDreamEntity;
import io.nicheblog.dreamdiary.feature.jrnl.dream.mapstruct.JrnlDreamMapstruct;
import io.nicheblog.dreamdiary.feature.jrnl.dream.model.JrnlDreamDto;
import io.nicheblog.dreamdiary.feature.jrnl.entry.entity.JrnlEntryEntity;
import io.nicheblog.dreamdiary.feature.jrnl.entry.mapstruct.JrnlEntryMapstruct;
import io.nicheblog.dreamdiary.feature.jrnl.entry.model.JrnlEntryDto;
import io.nicheblog.dreamdiary.feature.jrnl.entry.model.JrnlEntrySmpDto;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JrnlDayMapstruct 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JrnlDayMapstruct")
class JrnlDayMapstructTest {

    private static final String JRNL_DT = "2026-03-06";
    private static final String APRXMT_DT = "2026-03-01";

    @Mock
    private JrnlEntryMapstruct jrnlEntryMapstruct;

    @Mock
    private JrnlDreamMapstruct jrnlDreamMapstruct;

    private JrnlDayMapstruct sut;

    @BeforeEach
    void setUp() {
        // MapStruct가 생성한 구현체를 직접 생성하여 테스트한다.
        // Spring 컨텍스트를 띄우지 않기 위해 필드 주입은 ReflectionTestUtils 사용
        sut = new JrnlDayMapstructImpl();
        ReflectionTestUtils.setField(sut, "jrnlEntryMapstruct", jrnlEntryMapstruct);
        ReflectionTestUtils.setField(sut, "jrnlDreamMapstruct", jrnlDreamMapstruct);
    }

    /**
     * Entity → DTO 매핑 검증
     *  - Date → String 변환
     *  - stdrdDt 계산 로직
     *  - 요일 계산 로직
     *  - 하위 리스트 매핑 위임
     */
    @Nested
    @DisplayName("toDto")
    class ToDto {

        /**
         * 일반적인 날짜 케이스
         * dtUnknownYn = N 인 경우 stdrdDt 는 jrnlDt 를 사용해야 한다.
         * 또한 하위 리스트 매핑은 JrnlEntryMapstruct / JrnlDreamMapstruct 로 위임되는지 검증한다.
         */
        @Test
        @DisplayName("maps date fields and delegates nested list mappings")
        void mapsDateFieldsAndDelegatesNestedLists() throws Exception {
            final JrnlDayEntity entity = createEntity("N");

            final List<JrnlEntryEntity> entryEntities = List.of(
                    JrnlEntryEntity.builder().postNo(11).title("entry").idx(1).build()
            );
            final List<JrnlDreamEntity> dreamEntities = List.of(
                    JrnlDreamEntity.builder().postNo(21).title("dream").idx(1).build()
            );
            final List<JrnlDreamEntity> elseDreamEntities = List.of(
                    JrnlDreamEntity.builder().postNo(31).title("else-dream").idx(1).build()
            );
            entity.setJrnlEntryList(entryEntities);
            entity.setJrnlDreamList(dreamEntities);
            entity.setJrnlElseDreamList(elseDreamEntities);

            final List<JrnlEntrySmpDto> expectedEntrySmp = List.of(JrnlEntrySmpDto.builder().postNo(11).title("entry").idx(1).build());
            final List<JrnlEntryDto> expectedEntry = List.of(JrnlEntryDto.builder().postNo(11).title("entry").idx(1).build());
            final List<JrnlDreamDto> expectedDream = List.of(JrnlDreamDto.builder().postNo(21).title("dream").idx(1).build());
            final List<JrnlDreamDto> expectedElseDream = List.of(JrnlDreamDto.builder().postNo(31).title("else-dream").idx(1).build());

            when(jrnlEntryMapstruct.toSmpDtoList(entryEntities)).thenReturn(expectedEntrySmp);
            when(jrnlEntryMapstruct.toDtoList(entryEntities)).thenReturn(expectedEntry);
            when(jrnlDreamMapstruct.toDtoList(dreamEntities)).thenReturn(expectedDream);
            when(jrnlDreamMapstruct.toDtoList(elseDreamEntities)).thenReturn(expectedElseDream);

            final JrnlDayDto dto = sut.toDto(entity);

            assertAll(
                    () -> assertNotNull(dto),
                    () -> assertEquals(JRNL_DT, dto.getJrnlDt()),
                    () -> assertEquals(APRXMT_DT, dto.getAprxmtDt()),
                    () -> assertEquals(JRNL_DT, dto.getStdrdDt()),
                    () -> assertEquals(DateUtils.getDayOfWeekChinese(DateUtils.asDate(JRNL_DT)), dto.getJrnlDtWeekDay()),
                    () -> assertSame(expectedEntrySmp, dto.getEntryList()),
                    () -> assertSame(expectedEntry, dto.getJrnlEntryList()),
                    () -> assertSame(expectedDream, dto.getJrnlDreamList()),
                    () -> assertSame(expectedElseDream, dto.getJrnlElseDreamList())
            );

            verify(jrnlEntryMapstruct).toSmpDtoList(entryEntities);
            verify(jrnlEntryMapstruct).toDtoList(entryEntities);
            verify(jrnlDreamMapstruct).toDtoList(dreamEntities);
            verify(jrnlDreamMapstruct).toDtoList(elseDreamEntities);
            verifyNoMoreInteractions(jrnlEntryMapstruct, jrnlDreamMapstruct);
        }

        @Test
        @DisplayName("returns jrnlDt as standard date when dtUnknownYn is Y (dto getter contract)")
        void usesApproximateDateWhenUnknownDate() throws Exception {
            final JrnlDayEntity entity = createEntity("Y");
            final List<JrnlEntryEntity> entryEntities = List.of(JrnlEntryEntity.builder().postNo(11).title("entry").idx(1).build());
            final List<JrnlDreamEntity> dreamEntities = List.of(JrnlDreamEntity.builder().postNo(21).title("dream").idx(1).build());
            final List<JrnlDreamEntity> elseDreamEntities = List.of(JrnlDreamEntity.builder().postNo(31).title("else-dream").idx(1).build());
            entity.setJrnlEntryList(entryEntities);
            entity.setJrnlDreamList(dreamEntities);
            entity.setJrnlElseDreamList(elseDreamEntities);

            when(jrnlEntryMapstruct.toSmpDtoList(entryEntities)).thenReturn(List.of());
            when(jrnlEntryMapstruct.toDtoList(entryEntities)).thenReturn(List.of());
            when(jrnlDreamMapstruct.toDtoList(dreamEntities)).thenReturn(List.of());
            when(jrnlDreamMapstruct.toDtoList(elseDreamEntities)).thenReturn(List.of());

            final JrnlDayDto dto = sut.toDto(entity);

            assertAll(
                    () -> assertNotNull(dto),
                    () -> assertEquals(JRNL_DT, dto.getJrnlDt()),
                    () -> assertEquals(APRXMT_DT, dto.getAprxmtDt()),
                    () -> assertEquals(JRNL_DT, dto.getStdrdDt())
            );

            verify(jrnlEntryMapstruct).toSmpDtoList(entryEntities);
            verify(jrnlEntryMapstruct).toDtoList(entryEntities);
            verify(jrnlDreamMapstruct).toDtoList(dreamEntities);
            verify(jrnlDreamMapstruct).toDtoList(elseDreamEntities);
            verifyNoMoreInteractions(jrnlEntryMapstruct, jrnlDreamMapstruct);
        }
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("parses date strings and maps scalars/lists")
        void parsesDateStringsAndMapsScalarsAndLists() throws Exception {
            final JrnlDayDto dto = JrnlDayDtoTestFactory.createWithJrnlDt(JRNL_DT);
            dto.setAprxmtDt(APRXMT_DT);
            dto.setDtUnknownYn("Y");
            dto.setWeather("RAIN");
            dto.setYy(2026);
            dto.setMnth(3);

            final List<JrnlEntryDto> entryDtos = List.of(JrnlEntryDto.builder().postNo(11).title("entry").idx(1).build());
            dto.setJrnlEntryList(entryDtos);
            dto.setJrnlDreamList(List.of(JrnlDreamDto.builder().postNo(21).title("dream").idx(1).build()));
            dto.setJrnlElseDreamList(List.of(JrnlDreamDto.builder().postNo(31).title("else-dream").idx(1).build()));

            final List<JrnlEntryEntity> mappedEntryEntities = List.of(JrnlEntryEntity.builder().postNo(11).title("entry").idx(1).build());
            when(jrnlEntryMapstruct.toEntityList(entryDtos)).thenReturn(mappedEntryEntities);

            final JrnlDayEntity entity = sut.toEntity(dto);

            assertAll(
                    () -> assertNotNull(entity),
                    () -> assertEquals(DateUtils.asDate(JRNL_DT), entity.getJrnlDt()),
                    () -> assertEquals(DateUtils.asDate(APRXMT_DT), entity.getAprxmtDt()),
                    () -> assertEquals("Y", entity.getDtUnknownYn()),
                    () -> assertEquals("RAIN", entity.getWeather()),
                    () -> assertEquals(2026, entity.getYy()),
                    () -> assertEquals(3, entity.getMnth()),
                    () -> assertSame(mappedEntryEntities, entity.getJrnlEntryList()),
                    () -> assertEquals(1, entity.getJrnlDreamList().size()),
                    () -> assertEquals(1, entity.getJrnlElseDreamList().size())
            );

            verify(jrnlEntryMapstruct).toEntityList(entryDtos);
            verifyNoMoreInteractions(jrnlEntryMapstruct);
            verifyNoInteractions(jrnlDreamMapstruct);
        }
    }

    @Nested
    @DisplayName("updateFromDto")
    class UpdateFromDto {

        @Test
        @DisplayName("applies non-null values and keeps existing entry list when source list is null")
        void appliesNonNullValuesAndKeepsExistingEntryListWhenSourceNull() throws Exception {
            final JrnlDayEntity target = JrnlDayEntityTestFactory.createWithJrnlDt("2024-01-01");
            target.setAprxmtDt(DateUtils.asDate("2024-01-02"));
            target.setWeather("SUNNY");
            target.setYy(2024);
            target.setMnth(1);
            final List<JrnlEntryEntity> originalEntryList = new ArrayList<>(
                    List.of(JrnlEntryEntity.builder().postNo(1).title("origin").idx(1).build())
            );
            target.setJrnlEntryList(originalEntryList);

            final JrnlDayDto dto = JrnlDayDtoTestFactory.create();
            dto.setJrnlDt(JRNL_DT);
            dto.setAprxmtDt(APRXMT_DT);
            dto.setWeather("RAIN");
            dto.setYy(2026);
            dto.setMnth(3);
            when(jrnlEntryMapstruct.toEntityList(null)).thenReturn(null);

            sut.updateFromDto(dto, target);

            assertAll(
                    () -> assertEquals(DateUtils.asDate(JRNL_DT), target.getJrnlDt()),
                    () -> assertEquals(DateUtils.asDate(APRXMT_DT), target.getAprxmtDt()),
                    () -> assertEquals("RAIN", target.getWeather()),
                    () -> assertEquals(2026, target.getYy()),
                    () -> assertEquals(3, target.getMnth()),
                    () -> assertSame(originalEntryList, target.getJrnlEntryList()),
                    () -> assertEquals(1, target.getJrnlEntryList().size())
            );

            verify(jrnlEntryMapstruct).toEntityList(null);
            verifyNoMoreInteractions(jrnlEntryMapstruct);
            verifyNoInteractions(jrnlDreamMapstruct);
        }

        @Test
        @DisplayName("clears date fields when date strings are explicitly null")
        void clearsDateFieldsWhenDateStringsAreNull() throws Exception {
            final JrnlDayEntity target = JrnlDayEntityTestFactory.createWithJrnlDt("2024-01-01");
            target.setAprxmtDt(DateUtils.asDate("2024-01-02"));

            final JrnlDayDto dto = JrnlDayDtoTestFactory.create();
            dto.setJrnlDt(null);
            dto.setAprxmtDt(null);
            when(jrnlEntryMapstruct.toEntityList(null)).thenReturn(null);

            sut.updateFromDto(dto, target);

            assertAll(
                    () -> assertNull(target.getJrnlDt()),
                    () -> assertNull(target.getAprxmtDt())
            );

            verify(jrnlEntryMapstruct).toEntityList(null);
            verifyNoMoreInteractions(jrnlEntryMapstruct);
            verifyNoInteractions(jrnlDreamMapstruct);
        }
    }

    @Nested
    @DisplayName("null contract")
    class NullContract {

        @Test
        @DisplayName("returns null for null sources")
        void returnsNullForNullSources() throws Exception {
            assertAll(
                    () -> assertNull(sut.toDto(null)),
                    () -> assertNull(sut.toEntity(null))
            );
            verifyNoInteractions(jrnlEntryMapstruct, jrnlDreamMapstruct);
        }

        @Test
        @DisplayName("updateFromDto: null source is no-op")
        void updateFromDtoNullSourceIsNoOp() throws Exception {
            final JrnlDayEntity target = JrnlDayEntityTestFactory.createWithJrnlDt("2024-01-01");
            final String beforeWeather = target.getWeather();
            final Integer beforeYy = target.getYy();

            sut.updateFromDto(null, target);

            assertAll(
                    () -> assertEquals(DateUtils.asDate("2024-01-01"), target.getJrnlDt()),
                    () -> assertEquals(beforeWeather, target.getWeather()),
                    () -> assertEquals(beforeYy, target.getYy())
            );
            verifyNoInteractions(jrnlEntryMapstruct, jrnlDreamMapstruct);
        }
    }

    private static JrnlDayEntity createEntity(final String dtUnknownYn) throws Exception {
        final JrnlDayEntity entity = JrnlDayEntityTestFactory.createWithJrnlDt(JRNL_DT);
        entity.setDtUnknownYn(dtUnknownYn);
        entity.setAprxmtDt(DateUtils.asDate(APRXMT_DT));
        entity.setWeather("RAIN");
        entity.setYy(2026);
        entity.setMnth(3);
        return entity;
    }
}
