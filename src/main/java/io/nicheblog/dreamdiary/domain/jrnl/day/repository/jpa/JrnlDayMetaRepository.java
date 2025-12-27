package io.nicheblog.dreamdiary.domain.jrnl.day.repository.jpa;

import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDayMetaEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * JrnlDayMetaRepository
 * <pre>
 *  저널 일자 메타 정보 repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("jrnlDayMetaRepository")
public interface JrnlDayMetaRepository
        extends BaseStreamRepository<JrnlDayMetaEntity, Integer> {

    //
}
