package io.nicheblog.dreamdiary.feature.attachable.history.mapstruct.embed;

import io.nicheblog.dreamdiary.feature.attachable.history.entity.embed.HistoryEmbed;
import io.nicheblog.dreamdiary.feature.attachable.history.mapstruct.HistoryMapstruct;
import io.nicheblog.dreamdiary.feature.attachable.history.model.cmpstn.HistoryCmpstn;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * HistoryEmbedMapstruct
 * <pre>
 *  조치 모듈 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, DatePtn.class, StringUtils.class, HistoryMapstruct.class}, builder = @Builder(disableBuilder = true))
public interface HistoryEmbedMapstruct
        extends BaseWriteMapstruct<HistoryCmpstn, HistoryEmbed>, BaseMapstruct<HistoryCmpstn, HistoryEmbed> {

    HistoryEmbedMapstruct INSTANCE = Mappers.getMapper(HistoryEmbedMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Mapping(target = "historyTriggeredByNm", expression = "java(entity.getHistoryTriggeredByInfo() != null ? entity.getHistoryTriggeredByInfo().getNickNm() : null)")
    @Mapping(target = "historyTriggeredAt", expression = "java(DateUtils.asStr(entity.getHistoryTriggeredAt(), DatePtn.DATETIME))")
    HistoryCmpstn toDto(final HistoryEmbed entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    @Mapping(target = "historyTriggeredAt", expression = "java(DateUtils.asDate(dto.getHistoryTriggeredAt()))")
    HistoryEmbed toEntity(final HistoryCmpstn dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final HistoryCmpstn dto, final @MappingTarget HistoryEmbed entity) throws Exception;
}
