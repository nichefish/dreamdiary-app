/*
package io.nicheblog.dreamdiary.web.repository.journal.day.elastic;

import io.nicheblog.dreamdiary.web.feature.journal.day.entity.JournalDayEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

*/
/**
 * JournalDayElasticRepository
 * <pre>
 *  저널 일자 ElasticSearch (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 *//*

@Repository
public interface JournalDayElasticRepository {

    // @Query("{ \"nested\": { \"path\": \"tag.list\", \"query\": { \"bool\": { \"must\": [{ \"nested\": { \"path\": \"tag.list.tag\", \"query\": { \"match\": { \"tag.list.tag.name\": \"달리기     \" } } } }] } } } }")
    List<JournalDayEntity> findByTag_Name(final String name);
}*/


