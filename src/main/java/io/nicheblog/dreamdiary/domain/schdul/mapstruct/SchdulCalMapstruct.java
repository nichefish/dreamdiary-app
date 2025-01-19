package io.nicheblog.dreamdiary.domain.schdul.mapstruct;

import io.nicheblog.dreamdiary.domain.schdul.entity.SchdulEntity;
import io.nicheblog.dreamdiary.domain.schdul.model.SchdulCalDto;
import io.nicheblog.dreamdiary.domain.vcatn.papr.entity.VcatnSchdulEntity;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseCrudMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * SchdulCalMapstruct
 * <pre>
 *  일정(달력) MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {Constant.class, DateUtils.class, StringUtils.class, DatePtn.class}, builder = @Builder(disableBuilder = true))
public interface SchdulCalMapstruct
        extends BaseCrudMapstruct<SchdulCalDto, SchdulCalDto, SchdulEntity> {

    SchdulCalMapstruct INSTANCE = Mappers.getMapper(SchdulCalMapstruct.class);

    /**
     * SchdulEntity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    // 달력에선 종료일자에 시간 데이터(23:59:59)를 붙여줘야 한다.
    // 하루짜리 이벤트일 때만 allDay=true를 붙여준다.
    @Mapping(target = "display", expression = "java(Constant.SCHDUL_HLDY.equals(entity.getSchdulCd()) ? \"background\" : null)")
    @Mapping(target = "color", expression = "java(Constant.SCHDUL_HLDY.equals(entity.getSchdulCd()) ? \"red\" : null)")
    // @Mapping(target = "prtcpnt", expression = "java(entity.getPrtcpntStr())")
    @Mapping(target = "bgnDt", expression = "java(DateUtils.asStr(entity.getBgnDt(), DatePtn.DATE))")
    @Mapping(target = "endDt", expression = "java(DateUtils.asStr(entity.getEndDt(), DatePtn.ZDATETIME))")
    @Mapping(target = "start", expression = "java(DateUtils.asStr(entity.getBgnDt(), DatePtn.DATE))")
    @Mapping(target = "end", expression = "java(DateUtils.asStr(entity.getBgnDt(), DatePtn.ZDATETIME))")
    @Mapping(target = "allDay", expression = "java(entity.getEndDt() == null ? true : DateUtils.isSameDay(entity.getBgnDt(), entity.getEndDt()) ? true : false)")
    SchdulCalDto toCalDto(final SchdulEntity entity) throws Exception;

    /**
     * VcatnEntity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    // 달력에선 종료일자에 시간 데이터(23:59:59)를 붙여줘야 한다.
    // 하루짜리 이벤트일 때만 allDay=true를 붙여준다.
    @Mapping(target = "title", expression = "java(entity.getUserNm() + \" \" + entity.getVcatnNm()) ")
    @Mapping(target = "bgnDt", expression = "java(DateUtils.asStr(entity.getBgnDt(), DatePtn.DATE))")
    @Mapping(target = "endDt", expression = "java(DateUtils.asStr(entity.getEndDt(), DatePtn.ZDATETIME))")
    @Mapping(target = "start", expression = "java(DateUtils.asStr(entity.getBgnDt(), DatePtn.DATE))")
    @Mapping(target = "end", expression = "java(DateUtils.asStr(entity.getBgnDt(), DatePtn.ZDATETIME))")
    @Mapping(target = "allDay", expression = "java(entity.getEndDt() == null ? true : DateUtils.isSameDay(entity.getBgnDt(), entity.getEndDt()) ? true : false)")
    SchdulCalDto toCalDto(final VcatnSchdulEntity entity) throws Exception;

    /** 
     * 일정분류에 따른 FullCalender 표시 설정 세팅
     *
     * @param entity 일정 엔티티 (SchdulEntity)
     * @param dto FullCalendar에 표시할 일정 데이터 (SchdulCalDto)
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @AfterMapping
    default void mapCalFields(final SchdulEntity entity, @MappingTarget SchdulCalDto dto) throws Exception {
        Constant.Schdul schdulTy = Constant.Schdul.valueOf(dto.getSchdulCd());
        String title = dto.getTitle();
        switch (schdulTy) {
            case HLDY:
                dto.setDisplay("background");
                dto.setColor("red");
                dto.setClassName("text-light text-end pe-8");
                break;
            case CEREMONY:
                dto.setColor("#e8a8ff");
                dto.setClassName("text-light");
                dto.setTitle("\uD83D\uDC4F" + title);
                break;
            case BRTHDY:
                dto.setColor("purple");
                break;
            case TLCMMT:
                dto.setColor("#d6edff");
                dto.setClassName("text-dark");
                dto.setTitle(entity.getPrtcpntStr() + "재택");
                break;
            case OUTDT:
            case INDT:
                dto.setColor("lightgray");
                break;
            case ETC:
                dto.setColor("lightgray");
                dto.setClassName("text-dark" + (!dto.hasPassed() ? " blink" : ""));
                break;
        }
        boolean isPrvt = "Y".equals(dto.getPrvtYn());
        if (isPrvt) title = "\uD83D\uDD07" + title;
        title += dto.hasPassed() ? " \uD83D\uDDF8" : " ⋯";
        dto.setTitle(title);
    }
}
