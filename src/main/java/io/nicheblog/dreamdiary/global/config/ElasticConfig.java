/*
package io.nicheblog.dreamdiary.global.config;

import io.nicheblog.dreamdiary.global.intrfc.repository.impl.BaseElasticRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

*/
/**
 * ElasticConfig
 * <pre>
 *  ElasticSearch 관련 설정 커스터마이즈.
 * </pre>
 *
 * @author nichefish
 *//*

@Configuration
@EnableElasticsearchRepositories(
        basePackages = {"io.nicheblog.dreamdiary.**.repository.elastic"},
        repositoryBaseClass = BaseElasticRepositoryImpl.class
)
@RequiredArgsConstructor
public class ElasticConfig extends ElasticsearchConfiguration {

    private final ElasticProperty elasticProperty;

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(elasticProperty.getUris())
                .withBasicAuth(elasticProperty.getUsername(), elasticProperty.getPassword())
                .build();
    }
}*/
