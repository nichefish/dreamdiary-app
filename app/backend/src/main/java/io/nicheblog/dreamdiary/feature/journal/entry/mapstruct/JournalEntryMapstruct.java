package io.nicheblog.dreamdiary.feature.journal.entry.mapstruct;

import io.nicheblog.dreamdiary.feature.attachable._shared.mapstruct.BaseAttachableMapstruct;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixEntity;
import io.nicheblog.dreamdiary.feature.attachable.prefix.model.PrefixDto;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.feature.journal.day.type.JournalDatePrecision;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryPostDto;
import io.nicheblog.dreamdiary.feature.journal.interpretation.mapstruct.JournalInterpretationMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import io.nicheblog.dreamdiary.infrastructure.code.utils.CodeUtils;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    imports = { MarkdownUtils.class, CodeUtils.class, JournalDatePrecision.class },
    uses = { JournalInterpretationMapstruct.class },
    builder = @Builder(disableBuilder = true)
)
public abstract class JournalEntryMapstruct
        implements BaseWriteMapstruct<JournalEntryPostDto, JournalEntryEntity>,
        BaseAttachableMapstruct<JournalEntryDto, JournalEntryEntity>,
        JournalEntryReadMapstructSupport {

    /**
     * 등록 DTO를 엔티티로 변환한다.
     *
     * @param dto 등록 DTO
     * @return 변환된 엔티티
     * @throws Exception 변환 중 예외
     */
    @Override
    @Mapping(target = "journalChapter", ignore = true)
    @Mapping(target = "prefix", ignore = true)
    @Mapping(target = "content", expression = "java(MarkdownUtils.normalize(dto.getContent()))")
    public abstract JournalEntryEntity toEntity(final JournalEntryPostDto dto) throws Exception;

    /**
     * 수정 DTO 값을 기존 엔티티에 반영한다.
     *
     * @param dto 수정 DTO
     * @param entity 대상 엔티티
     * @throws Exception 변환 중 예외
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "journalChapter", ignore = true)
    @Mapping(target = "prefix", ignore = true)
    @Mapping(target = "content", expression = "java(MarkdownUtils.normalize(dto.getContent()))")
    public abstract void updateFromDto(final JournalEntryPostDto dto, final @MappingTarget JournalEntryEntity entity) throws Exception;

    /**
     * 엔티티를 상세 DTO로 변환한다.
     *
     * @param entity 원본 엔티티
     * @return 상세 DTO
     * @throws Exception 변환 중 예외
     */
    @Override
    @Mapping(target = "prefix", expression = "java(selectedPrefixToDto(entity))")
    @Mapping(target = "prefixId", expression = "java(entity.getSelectedPrefixId())")
    @Mapping(target = "prefixContentType", expression = "java(resolvePrefixContentType(entity))")
    public abstract JournalEntryDto toDto(final JournalEntryEntity entity) throws Exception;

    /**
     * 엔트리가 선택한 말머리(prefix_content 연결)를 표시용 DTO로 변환한다.
     *
     * @param entity 저널 엔트리 엔티티
     * @return 선택 말머리 DTO. 선택이 없으면 {@code null}
     */
    protected PrefixDto selectedPrefixToDto(final JournalEntryEntity entity) {
        final PrefixEntity prefix = entity.getSelectedPrefix();
        if (prefix == null) return null;
        return PrefixDto.builder()
                .id(prefix.getId())
                .name(prefix.getName())
                .color(prefix.getColor())
                .sortOrder(prefix.getSortOrder())
                .activeYn(prefix.getActiveYn())
                .build();
    }

    /**
     * 영속 contentType과 별개로 소속 챕터 유형이 사용하는 개인 Prefix 목록 키를 반환한다.
     *
     * @param entity 저널 엔트리 엔티티
     * @return JOURNAL_DIARY | JOURNAL_DREAM | JOURNAL_NOTE
     */
    protected String resolvePrefixContentType(final JournalEntryEntity entity) {
        if (entity.getJournalChapter() == null || entity.getJournalChapter().getChapterType() == null) return null;
        final ChapterType chapterType = entity.getJournalChapter().getChapterType();
        return switch (chapterType) {
            case DIARY -> ContentType.JOURNAL_DIARY.key;
            case NOTE -> ContentType.JOURNAL_NOTE.key;
            case DREAM -> ContentType.JOURNAL_DREAM.key;
        };
    }
}
