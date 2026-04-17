package io.nicheblog.dreamdiary.feature.journal.day.mapstruct;

import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.mapstruct.JournalChapterMapstruct;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterSmpDto;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntityTestFactory;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDtoTestFactory;
import io.nicheblog.dreamdiary.feature.journal.dream.entity.JournalDreamEntity;
import io.nicheblog.dreamdiary.feature.journal.dream.mapstruct.JournalDreamMapstruct;
import io.nicheblog.dreamdiary.feature.journal.dream.model.JournalDreamDto;
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
 * JournalDayMapstruct 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JournalDayMapstruct")
class JournalDayMapstructTest {

    private static final String JOURNAL_DT = "2026-03-06";
    private static final String APRXMT_DT = "2026-03-01";

    @Mock
    private JournalChapterMapstruct journalChapterMapstruct;

    @Mock
    private JournalDreamMapstruct journalDreamMapstruct;

    private JournalDayMapstruct sut;

    @BeforeEach
    void setUp() {
        // MapStruct가 생성한 구현체를 직접 생성하여 테스트한다.
        // Spring 컨텍스트를 띄우지 않기 위해 필드 주입은 ReflectionTestUtils 사용
        sut = new JournalDayMapstructImpl();
        ReflectionTestUtils.setField(sut, "journalChapterMapstruct", journalChapterMapstruct);
        ReflectionTestUtils.setField(sut, "journalDreamMapstruct", journalDreamMapstruct);
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
         * dtUnknownYn = N 인 경우 stdrdDt 는 journalDt 를 사용해야 한다.
         * 또한 하위 리스트 매핑은 JournalChapterMapstruct / JournalDreamMapstruct 로 위임되는지 검증한다.
         */
        @Test
        @DisplayName("maps date fields and delegates nested list mappings")
        void mapsDateFieldsAndDelegatesNestedLists() throws Exception {
            final JournalDayEntity entity = createEntity("N");

            final List<JournalChapterEntity> chapterEntities = List.of(
                    JournalChapterEntity.builder().id(11).title("chapter").sortOrder(1).build()
            );
            final List<JournalDreamEntity> dreamEntities = List.of(
                    JournalDreamEntity.builder().id(21).title("dream").sortOrder(1).build()
            );
            final List<JournalDreamEntity> elseDreamEntities = List.of(
                    JournalDreamEntity.builder().id(31).title("else-dream").sortOrder(1).build()
            );
            entity.setJournalChapterList(chapterEntities);
            entity.setJournalDreamList(dreamEntities);
            entity.setJournalElseDreamList(elseDreamEntities);

            final List<JournalChapterSmpDto> expectedChapterSmp = List.of(JournalChapterSmpDto.builder().id(11).title("chapter").sortOrder(1).build());
            final List<JournalChapterDto> expectedChapter = List.of(JournalChapterDto.builder().id(11).title("chapter").sortOrder(1).build());
            final List<JournalDreamDto> expectedDream = List.of(JournalDreamDto.builder().id(21).title("dream").sortOrder(1).build());
            final List<JournalDreamDto> expectedElseDream = List.of(JournalDreamDto.builder().id(31).title("else-dream").sortOrder(1).build());

            when(journalChapterMapstruct.toSmpDtoList(chapterEntities)).thenReturn(expectedChapterSmp);
            when(journalChapterMapstruct.toDtoList(chapterEntities)).thenReturn(expectedChapter);
            when(journalDreamMapstruct.toDtoList(dreamEntities)).thenReturn(expectedDream);
            when(journalDreamMapstruct.toDtoList(elseDreamEntities)).thenReturn(expectedElseDream);

            final JournalDayDto dto = sut.toDto(entity);

            assertAll(
                    () -> assertNotNull(dto),
                    () -> assertEquals(JOURNAL_DT, dto.getJournalDt()),
                    () -> assertEquals(APRXMT_DT, dto.getAprxmtDt()),
                    () -> assertEquals(JOURNAL_DT, dto.getStdrdDt()),
                    () -> assertEquals(DateUtils.getDayOfWeekChinese(DateUtils.asDate(JOURNAL_DT)), dto.getJournalDtWeekDay()),
                    () -> assertSame(expectedChapterSmp, dto.getChapterList()),
                    () -> assertSame(expectedChapter, dto.getJournalChapterList()),
                    () -> assertSame(expectedDream, dto.getJournalDreamList()),
                    () -> assertSame(expectedElseDream, dto.getJournalElseDreamList())
            );

            verify(journalChapterMapstruct).toSmpDtoList(chapterEntities);
            verify(journalChapterMapstruct).toDtoList(chapterEntities);
            verify(journalDreamMapstruct).toDtoList(dreamEntities);
            verify(journalDreamMapstruct).toDtoList(elseDreamEntities);
            verifyNoMoreInteractions(journalChapterMapstruct, journalDreamMapstruct);
        }

        @Test
        @DisplayName("returns journalDt as standard date when dtUnknownYn is Y (dto getter contract)")
        void usesApproximateDateWhenUnknownDate() throws Exception {
            final JournalDayEntity entity = createEntity("Y");
            final List<JournalChapterEntity> chapterEntities = List.of(JournalChapterEntity.builder().id(11).title("chapter").sortOrder(1).build());
            final List<JournalDreamEntity> dreamEntities = List.of(JournalDreamEntity.builder().id(21).title("dream").sortOrder(1).build());
            final List<JournalDreamEntity> elseDreamEntities = List.of(JournalDreamEntity.builder().id(31).title("else-dream").sortOrder(1).build());
            entity.setJournalChapterList(chapterEntities);
            entity.setJournalDreamList(dreamEntities);
            entity.setJournalElseDreamList(elseDreamEntities);

            when(journalChapterMapstruct.toSmpDtoList(chapterEntities)).thenReturn(List.of());
            when(journalChapterMapstruct.toDtoList(chapterEntities)).thenReturn(List.of());
            when(journalDreamMapstruct.toDtoList(dreamEntities)).thenReturn(List.of());
            when(journalDreamMapstruct.toDtoList(elseDreamEntities)).thenReturn(List.of());

            final JournalDayDto dto = sut.toDto(entity);

            assertAll(
                    () -> assertNotNull(dto),
                    () -> assertEquals(JOURNAL_DT, dto.getJournalDt()),
                    () -> assertEquals(APRXMT_DT, dto.getAprxmtDt()),
                    () -> assertEquals(JOURNAL_DT, dto.getStdrdDt())
            );

            verify(journalChapterMapstruct).toSmpDtoList(chapterEntities);
            verify(journalChapterMapstruct).toDtoList(chapterEntities);
            verify(journalDreamMapstruct).toDtoList(dreamEntities);
            verify(journalDreamMapstruct).toDtoList(elseDreamEntities);
            verifyNoMoreInteractions(journalChapterMapstruct, journalDreamMapstruct);
        }
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("parses date strings and maps scalars/lists")
        void parsesDateStringsAndMapsScalarsAndLists() throws Exception {
            final JournalDayDto dto = JournalDayDtoTestFactory.createWithJournalDt(JOURNAL_DT);
            dto.setAprxmtDt(APRXMT_DT);
            dto.setDtUnknownYn("Y");
            dto.setWeather("RAIN");
            dto.setYy(2026);
            dto.setMnth(3);

            final List<JournalChapterDto> chapterDtos = List.of(JournalChapterDto.builder().id(11).title("chapter").sortOrder(1).build());
            dto.setJournalChapterList(chapterDtos);
            dto.setJournalDreamList(List.of(JournalDreamDto.builder().id(21).title("dream").sortOrder(1).build()));
            dto.setJournalElseDreamList(List.of(JournalDreamDto.builder().id(31).title("else-dream").sortOrder(1).build()));

            final List<JournalChapterEntity> mappedChapterEntities = List.of(JournalChapterEntity.builder().id(11).title("chapter").sortOrder(1).build());
            when(journalChapterMapstruct.toEntityList(chapterDtos)).thenReturn(mappedChapterEntities);

            final JournalDayEntity entity = sut.toEntity(dto);

            assertAll(
                    () -> assertNotNull(entity),
                    () -> assertEquals(DateUtils.asDate(JOURNAL_DT), entity.getJournalDt()),
                    () -> assertEquals(DateUtils.asDate(APRXMT_DT), entity.getAprxmtDt()),
                    () -> assertEquals("Y", entity.getDtUnknownYn()),
                    () -> assertEquals("RAIN", entity.getWeather()),
                    () -> assertEquals(2026, entity.getYy()),
                    () -> assertEquals(3, entity.getMnth()),
                    () -> assertSame(mappedChapterEntities, entity.getJournalChapterList()),
                    () -> assertEquals(1, entity.getJournalDreamList().size()),
                    () -> assertEquals(1, entity.getJournalElseDreamList().size())
            );

            verify(journalChapterMapstruct).toEntityList(chapterDtos);
            verifyNoMoreInteractions(journalChapterMapstruct);
            verifyNoInteractions(journalDreamMapstruct);
        }
    }

    @Nested
    @DisplayName("updateFromDto")
    class UpdateFromDto {

        @Test
        @DisplayName("applies non-null values and keeps existing entry list when source list is null")
        void appliesNonNullValuesAndKeepsExistingChapterListWhenSourceNull() throws Exception {
            final JournalDayEntity target = JournalDayEntityTestFactory.createWithJournalDt("2024-01-01");
            target.setAprxmtDt(DateUtils.asDate("2024-01-02"));
            target.setWeather("SUNNY");
            target.setYy(2024);
            target.setMnth(1);
            final List<JournalChapterEntity> originalChapterList = new ArrayList<>(
                    List.of(JournalChapterEntity.builder().id(1).title("origin").sortOrder(1).build())
            );
            target.setJournalChapterList(originalChapterList);

            final JournalDayDto dto = JournalDayDtoTestFactory.create();
            dto.setJournalDt(JOURNAL_DT);
            dto.setAprxmtDt(APRXMT_DT);
            dto.setWeather("RAIN");
            dto.setYy(2026);
            dto.setMnth(3);
            when(journalChapterMapstruct.toEntityList(null)).thenReturn(null);

            sut.updateFromDto(dto, target);

            assertAll(
                    () -> assertEquals(DateUtils.asDate(JOURNAL_DT), target.getJournalDt()),
                    () -> assertEquals(DateUtils.asDate(APRXMT_DT), target.getAprxmtDt()),
                    () -> assertEquals("RAIN", target.getWeather()),
                    () -> assertEquals(2026, target.getYy()),
                    () -> assertEquals(3, target.getMnth()),
                    () -> assertSame(originalChapterList, target.getJournalChapterList()),
                    () -> assertEquals(1, target.getJournalChapterList().size())
            );

            verify(journalChapterMapstruct).toEntityList(null);
            verifyNoMoreInteractions(journalChapterMapstruct);
            verifyNoInteractions(journalDreamMapstruct);
        }

        @Test
        @DisplayName("clears date fields when date strings are explicitly null")
        void clearsDateFieldsWhenDateStringsAreNull() throws Exception {
            final JournalDayEntity target = JournalDayEntityTestFactory.createWithJournalDt("2024-01-01");
            target.setAprxmtDt(DateUtils.asDate("2024-01-02"));

            final JournalDayDto dto = JournalDayDtoTestFactory.create();
            dto.setJournalDt(null);
            dto.setAprxmtDt(null);
            when(journalChapterMapstruct.toEntityList(null)).thenReturn(null);

            sut.updateFromDto(dto, target);

            assertAll(
                    () -> assertNull(target.getJournalDt()),
                    () -> assertNull(target.getAprxmtDt())
            );

            verify(journalChapterMapstruct).toEntityList(null);
            verifyNoMoreInteractions(journalChapterMapstruct);
            verifyNoInteractions(journalDreamMapstruct);
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
            verifyNoInteractions(journalChapterMapstruct, journalDreamMapstruct);
        }

        @Test
        @DisplayName("updateFromDto: null source is no-op")
        void updateFromDtoNullSourceIsNoOp() throws Exception {
            final JournalDayEntity target = JournalDayEntityTestFactory.createWithJournalDt("2024-01-01");
            final String beforeWeather = target.getWeather();
            final Integer beforeYy = target.getYy();

            sut.updateFromDto(null, target);

            assertAll(
                    () -> assertEquals(DateUtils.asDate("2024-01-01"), target.getJournalDt()),
                    () -> assertEquals(beforeWeather, target.getWeather()),
                    () -> assertEquals(beforeYy, target.getYy())
            );
            verifyNoInteractions(journalChapterMapstruct, journalDreamMapstruct);
        }
    }

    private static JournalDayEntity createEntity(final String dtUnknownYn) throws Exception {
        final JournalDayEntity entity = JournalDayEntityTestFactory.createWithJournalDt(JOURNAL_DT);
        entity.setDtUnknownYn(dtUnknownYn);
        entity.setAprxmtDt(DateUtils.asDate(APRXMT_DT));
        entity.setWeather("RAIN");
        entity.setYy(2026);
        entity.setMnth(3);
        return entity;
    }
}

