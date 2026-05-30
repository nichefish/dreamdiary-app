package io.nicheblog.dreamdiary.feature.journal.day.repository.jpa;

import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayMetaEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * JournalDayMetaRepository
 * <pre>
 *  저널 일자 메타 정보 repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface JournalDayMetaRepository
        extends BaseStreamRepository<JournalDayMetaEntity, Integer> {

    //
}


