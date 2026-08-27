package io.nicheblog.dreamdiary.feature.journal.reflection.mapstruct;

import io.nicheblog.dreamdiary.feature.attachable._shared.mapstruct.BaseAttachableMapstruct;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.reflection.entity.JournalReflectionEntity;
import io.nicheblog.dreamdiary.feature.journal.reflection.model.JournalReflectionPostDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;

/**
 * Reflection(Commentary) MapStruct.
 *
 * <p>쓰기는 {@link JournalReflectionPostDto} → {@link JournalReflectionEntity}, 읽기는
 * {@link JournalReflectionEntity} → {@link JournalEntryDto}(대상 엔트리 embed 표시용)로 변환한다.
 * Reflection 은 chapter·prefix 가 없으므로 말머리 매핑을 하지 않는다.</p>
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    imports = { MarkdownUtils.class, StringUtils.class },
    builder = @Builder(disableBuilder = true)
)
public abstract class JournalReflectionMapstruct
        implements BaseWriteMapstruct<JournalReflectionPostDto, JournalReflectionEntity>,
        BaseAttachableMapstruct<JournalEntryDto, JournalReflectionEntity> {

    /**
     * 등록 DTO를 Reflection 엔티티로 변환한다.
     *
     * @param dto 등록 DTO
     * @return 변환된 엔티티
     * @throws Exception 변환 중 예외
     */
    @Override
    @Mapping(target = "content", expression = "java(MarkdownUtils.normalize(dto.getContent()))")
    public abstract JournalReflectionEntity toEntity(final JournalReflectionPostDto dto) throws Exception;

    /**
     * 수정 DTO 값을 기존 Reflection 엔티티에 반영한다.
     * 형제 순번({@code sortOrder})은 {@code JournalReflectionOrderService}가 재배치하므로 여기서 덮어쓰지 않는다.
     *
     * @param dto 수정 DTO
     * @param entity 대상 엔티티
     * @throws Exception 변환 중 예외
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "content", expression = "java(MarkdownUtils.normalize(dto.getContent()))")
    @Mapping(target = "sortOrder", ignore = true)
    public abstract void updateFromDto(final JournalReflectionPostDto dto, final @MappingTarget JournalReflectionEntity entity) throws Exception;

    /**
     * Reflection 엔티티를 엔트리 상세 DTO 로 변환한다(대상 역참조 embed·수정 로드용).
     * Reflection 은 chapter·prefix 가 없으므로 말머리 매핑을 하지 않는다.
     * 화면 본문은 Entry 와 같이 {@code markdownContent}(MarkdownUtils HTML)를 쓴다.
     *
     * @param reflection Reflection 엔티티
     * @return 상세 DTO
     * @throws Exception 변환 중 예외
     */
    @Override
    @Mapping(target = "prefix", ignore = true)
    @Mapping(target = "prefixId", ignore = true)
    @Mapping(target = "prefixContentType", ignore = true)
    @Mapping(target = "markdownContent", expression = "java(StringUtils.isEmpty(reflection.getContent()) ? \"-\" : MarkdownUtils.markdown(reflection.getContent()))")
    public abstract JournalEntryDto toDto(final JournalReflectionEntity reflection) throws Exception;
}
