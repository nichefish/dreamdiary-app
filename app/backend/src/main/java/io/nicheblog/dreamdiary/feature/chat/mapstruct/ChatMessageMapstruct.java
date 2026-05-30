package io.nicheblog.dreamdiary.feature.chat.mapstruct;

import io.nicheblog.dreamdiary.feature.attachable._shared.mapstruct.BaseAttachableMapstruct;
import io.nicheblog.dreamdiary.feature.chat.entity.ChatMessageEntity;
import io.nicheblog.dreamdiary.feature.chat.model.ChatMessageDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * ChatMessageMapstruct
 * <pre>
 *  채팅 메시지 Entity와 DTO 사이의 변환을 담당하는 MapStruct Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, MarkdownUtils.class}, builder = @Builder(disableBuilder = true))
public interface ChatMessageMapstruct
        extends BaseWriteMapstruct<ChatMessageDto, ChatMessageEntity>, BaseAttachableMapstruct<ChatMessageDto, ChatMessageEntity> {

    /** 직접 참조가 필요한 서비스에서 사용하는 MapStruct 매퍼 인스턴스입니다. */
    ChatMessageMapstruct INSTANCE = Mappers.getMapper(ChatMessageMapstruct.class);

    /**
     * 채팅 메시지 Entity를 화면 응답용 DTO로 변환한다.
     *
     * @param entity 변환할 Entity 객체
     * @return 변환된 채팅 메시지 DTO
     * @throws Exception 마크다운 변환 또는 공통 매핑 중 예외가 발생한 경우
     */
    @Override
    @Named("toDto")
    @Mapping(target = "markdownContent", expression = "java(StringUtils.isEmpty(entity.getContent()) ? \"-\" : MarkdownUtils.markdown(entity.getContent()))")
    @Mapping(target = "isCreatedBy", expression = "java(!\"ASSISTANT\".equalsIgnoreCase(entity.getRole()) && entity.isCreatedBy())")
    ChatMessageDto toDto(final ChatMessageEntity entity) throws Exception;

    /**
     * 채팅 메시지 DTO를 저장용 Entity로 변환한다.
     *
     * @param dto 변환할 Dto 객체
     * @return 변환된 채팅 메시지 Entity
     * @throws Exception 내용 정규화 또는 공통 매핑 중 예외가 발생한 경우
     */
    @Override
    @Mapping(target = "content", expression = "java(MarkdownUtils.normalize(dto.getContent()))")
    ChatMessageEntity toEntity(final ChatMessageDto dto) throws Exception;

    /**
     * DTO에서 null이 아닌 값만 기존 채팅 메시지 Entity에 반영한다.
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     * @throws Exception 내용 정규화 또는 공통 매핑 중 예외가 발생한 경우
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "content", expression = "java(MarkdownUtils.normalize(dto.getContent()))")
    void updateFromDto(final ChatMessageDto dto, final @MappingTarget ChatMessageEntity entity) throws Exception;
}
