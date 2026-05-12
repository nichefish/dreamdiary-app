package io.nicheblog.dreamdiary.feature.calendar.schedule.repository.jpa;

import io.nicheblog.dreamdiary.feature.calendar.schedule.entity.ScheduleEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.util.Date;
import java.util.Optional;

/**
 * ScheduleRepository
 * <pre>
 *  일정 상세 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface ScheduleRepository
        extends BaseStreamRepository<ScheduleEntity, Integer> {

    /**
     * 날짜(date)에 대하여 휴일 해당 여부 조회
     *
     * @param groupId - 조회할 일정 코드 또는 그룹 ID
     * @param date - 조회할 시작 날짜
     * @return 휴일에 해당하는 일정 정보가 포함된 `ScheduleEntity` 객체의 Optional
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Optional<ScheduleEntity> findByScheduleCdAndBgnDt(final String groupId, final Date date);
}

