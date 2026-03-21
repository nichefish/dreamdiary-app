/*
package io.nicheblog.dreamdiary.web.repository.jrnl.day.elastic.impl;

import io.nicheblog.dreamdiary.web.feature.jrnl.day.entity.JrnlDayEntity;
import io.nicheblog.dreamdiary.web.repository.jrnl.day.elastic.JrnlDayElasticRepository;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

*/
/**
 * JrnlDayElasticRepositoryImpl
 *//*

@Repository
public class JrnlDayElasticRepositoryImpl
        implements JrnlDayElasticRepository {


    @Resource
    protected ElasticsearchRestTemplate elasticsearchRestTemplate;


    @Override
    public List<JrnlDayEntity> findByTag_TagNm(final String tagNm) {
        NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery(
                "tag.list",
                QueryBuilders.nestedQuery(
                        "tag.list.tag",
                        QueryBuilders.matchQuery("tag.list.tag.tagNm", tagNm),
                        ScoreMode.None
                ),
                ScoreMode.None
        );

        Query searchQuery = new NativeSearchQueryBuilder()
                .withQuery(nestedQuery)
                .build();

        SearchHits<JrnlDayEntity> searchHits = elasticsearchRestTemplate.search(searchQuery, JrnlDayEntity.class);
        return searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());
    }
}
*/
