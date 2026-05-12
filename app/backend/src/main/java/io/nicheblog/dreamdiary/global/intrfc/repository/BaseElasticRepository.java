package io.nicheblog.dreamdiary.global.intrfc.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * BaseElasticRepository
 * <pre>
 *  (공통/상속) 커스텀 ElasticSearch (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@NoRepositoryBean
public interface BaseElasticRepository<T, ID extends Serializable>
        extends ElasticsearchRepository<T, ID> {
    //
}
