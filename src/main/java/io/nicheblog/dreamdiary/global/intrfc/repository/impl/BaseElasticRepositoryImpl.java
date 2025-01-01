/*
package io.nicheblog.dreamdiary.global.intrfc.repository.impl;

import io.nicheblog.dreamdiary.global.intrfc.repository.BaseElasticRepository;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchEntityInformation;
import org.springframework.data.elasticsearch.repository.support.SimpleElasticsearchRepository;
import org.springframework.data.repository.NoRepositoryBean;

import javax.annotation.Resource;
import java.io.Serializable;

*/
/**
 * BaseElasticRepositoryImpl
 * <pre>
 *  (공통/상속) 커스텀 ElasticSearch Repository 구현체
 * </pre>
 *
 * @author nichefish
 *//*

@NoRepositoryBean
public class BaseElasticRepositoryImpl<T, ID extends Serializable>
        extends SimpleElasticsearchRepository<T, ID>
        implements BaseElasticRepository<T, ID> {

    @Resource
    protected ElasticsearchRestTemplate elasticsearchRestTemplate;

    private final ElasticsearchOperations elasticsearchOperations;

    */
/**
     * constructor
     *//*

    public BaseElasticRepositoryImpl(ElasticsearchEntityInformation<T, ID> metadata, ElasticsearchOperations operations) {
        super(metadata, operations);
        this.elasticsearchOperations = operations;
    }

    */
/**
     * Refresh an entity (reload the entity from Elasticsearch)
     *//*


    public T refresh(ID id, Class<T> clazz) {
        return elasticsearchRestTemplate.get(id.toString(), clazz);
    }
}
*/
