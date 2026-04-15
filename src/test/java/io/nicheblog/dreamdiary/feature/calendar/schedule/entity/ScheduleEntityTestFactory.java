package io.nicheblog.dreamdiary.feature.calendar.schedule.entity;

import io.nicheblog.dreamdiary.feature.calendar.schedule.entity.ScheduleEntity;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * ScheduleEntityTestFactory
 * <pre>
 *  일정 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class ScheduleEntityTestFactory {

    /**
     * 테스트용 일정 Entity 생성
     */
    public static ScheduleEntity create() throws Exception {
        return ScheduleEntity.builder()
                .contentType(ContentType.SCHEDULE.key)
                .title("test_title")
                .cn("test_cn")
                .build();
    }
}

