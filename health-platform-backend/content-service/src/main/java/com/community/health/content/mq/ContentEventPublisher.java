package com.community.health.content.mq;

import com.community.health.content.entity.Article;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "rocketmq", name = "name-server")
public class ContentEventPublisher {
  private static final Logger log = LoggerFactory.getLogger(ContentEventPublisher.class);
  private final RocketMQTemplate template;
  private final ObjectMapper mapper = new ObjectMapper();
  private final String topic;

  public ContentEventPublisher(RocketMQTemplate template,
                               @Value("${health.topics.content:content-events}") String topic) {
    this.template = template;
    this.topic = topic;
  }

  public void publishArticle(Article article) {
    if (article == null) {
      return;
    }
    Map<String, Object> payload = new HashMap<>();
    payload.put("articleId", article.getArticleId());
    payload.put("title", article.getTitle());
    payload.put("category", article.getCategory());
    payload.put("status", article.getStatus());
    if (article.getPublishDate() != null) {
      payload.put("publishDate", article.getPublishDate().format(DateTimeFormatter.ISO_DATE_TIME));
    }
    try {
      template.convertAndSend(topic, mapper.writeValueAsString(payload));
      log.info("Published content-event {}", payload);
    } catch (JsonProcessingException e) {
      log.warn("Failed to serialize content event for article {}", article.getArticleId(), e);
    }
  }
}
