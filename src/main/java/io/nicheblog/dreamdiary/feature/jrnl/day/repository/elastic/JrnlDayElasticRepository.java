/*
package io.nicheblog.dreamdiary.web.repository.jrnl.day.elastic;

import io.nicheblog.dreamdiary.web.feature.jrnl.day.entity.JrnlDayEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

*/
/**
 * JrnlDayElasticRepository
 * <pre>
 *  저널 일자 ElasticSearch (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 *//*

@Repository("jrnlDayElasticRepository")
public interface JrnlDayElasticRepository {

    // @Query("{ \"nested\": { \"path\": \"tag.list\", \"query\": { \"bool\": { \"must\": [{ \"nested\": { \"path\": \"tag.list.tag\", \"query\": { \"match\": { \"tag.list.tag.tagNm\": \"달리기     \" } } } }] } } } }")
    List<JrnlDayEntity> findByTag_TagNm(final String tagNm);
}*/
