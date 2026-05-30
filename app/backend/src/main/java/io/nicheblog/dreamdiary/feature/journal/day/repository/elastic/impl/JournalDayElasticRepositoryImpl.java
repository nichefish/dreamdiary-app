/*
package io.nicheblog.dreamdiary.web.repository.journal.day.elastic.impl;

import io.nicheblog.dreamdiary.web.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.web.repository.journal.day.elastic.JournalDayElasticRepository;
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
 * JournalDayElasticRepositoryImpl
 *//*

@Repository
public class JournalDayElasticRepositoryImpl
        implements JournalDayElasticRepository {


    @Resource
    protected ElasticsearchRestTemplate elasticsearchRestTemplate;


    @Override
    public List<JournalDayEntity> findByTag_Name(final String name) {
        NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery(
                "tag.list",
                QueryBuilders.nestedQuery(
                        "tag.list.tag",
                        QueryBuilders.matchQuery("tag.list.tag.name", name),
                        ScoreMode.None
                ),
                ScoreMode.None
        );

        Query searchQuery = new NativeSearchQueryBuilder()
                .withQuery(nestedQuery)
                .build();

        SearchHits<JournalDayEntity> searchHits = elasticsearchRestTemplate.search(searchQuery, JournalDayEntity.class);
        return searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());
    }
}
*/

