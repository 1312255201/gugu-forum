package cn.gugufish.repository;

import cn.gugufish.entity.es.TopicDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicRepository extends ElasticsearchRepository<TopicDocument, Integer> {
}