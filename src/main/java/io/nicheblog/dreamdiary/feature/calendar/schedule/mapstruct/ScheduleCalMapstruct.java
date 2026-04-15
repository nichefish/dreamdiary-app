package io.nicheblog.dreamdiary.feature.calendar.schedule.mapstruct;

import io.nicheblog.dreamdiary.feature.calendar.schedule.entity.ScheduleEntity;
import io.nicheblog.dreamdiary.feature.calendar.schedule.model.ScheduleCalDto;
import io.nicheblog.dreamdiary.feature.calendar.schedule.type.ScheduleType;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseReadMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * ScheduleCalMapstruct
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        imports = {Constant.class, Code.class, DateUtils.class, StringUtils.class, DatePtn.class},
        builder = @Builder(disableBuilder = true)
)
public interface ScheduleCalMapstruct
        extends BaseWriteMapstruct<ScheduleCalDto, ScheduleEntity>, BaseReadMapstruct<ScheduleCalDto, ScheduleEntity> {

    ScheduleCalMapstruct INSTANCE = Mappers.getMapper(ScheduleCalMapstruct.class);

    /**
     * ScheduleEntity -> Dto 변환
     * 달력에선 종료일자에 시간 데이터(23:59:59)를 붙여줘야 한다.
     * 하루짜리 이벤트일 때만 allDay=true를 붙여준다.
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Mapping(target = "display", expression = "java(Code.SCHEDULE_HOLYDAY.equals(entity.getScheduleCd()) ? \"background\" : null)")
    @Mapping(target = "color", expression = "java(Code.SCHEDULE_HOLYDAY.equals(entity.getScheduleCd()) ? \"red\" : null)")
    @Mapping(target = "bgnDt", expression = "java(DateUtils.asStr(entity.getBgnDt(), DatePtn.DATE))")
    @Mapping(target = "endDt", expression = "java(DateUtils.asStr(entity.getEndDt(), DatePtn.ZDATETIME))")
    @Mapping(target = "start", expression = "java(DateUtils.asStr(entity.getBgnDt(), DatePtn.DATE))")
    @Mapping(target = "end", expression = "java(DateUtils.asStr(entity.getBgnDt(), DatePtn.ZDATETIME))")
    @Mapping(target = "allDay", expression = "java(entity.getEndDt() == null || DateUtils.isSameDay(entity.getBgnDt(), entity.getEndDt()))")
    ScheduleCalDto toCalDto(final ScheduleEntity entity) throws Exception;

    /** 
     * 일정분류에 따른 FullCalender 표시 설정 세팅
     *
     * @param entity 일정 엔티티 (ScheduleEntity)
     * @param dto FullCalendar에 표시할 일정 데이터 (ScheduleCalDto)
     */
    @AfterMapping
    default void mapCalFields(final ScheduleEntity entity, @MappingTarget ScheduleCalDto dto) throws Exception {
        ScheduleType scheduleTypeTy = ScheduleType.valueOf(dto.getScheduleCd());
        String title = dto.getTitle();
        switch (scheduleTypeTy) {
            case HOLYDAY:
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

