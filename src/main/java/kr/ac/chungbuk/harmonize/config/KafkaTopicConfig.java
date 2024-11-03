package kr.ac.chungbuk.harmonize.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic musicAnalysis() {
        return new NewTopic("musicAnalysis", 1, (short) 1);
    }

    @Bean
    public NewTopic musicAnalysisReply() {
        return new NewTopic("musicAnalysisReply", 1, (short) 1);
    }

    @Bean
    public NewTopic musicRecSys() {
        return new NewTopic("musicRecSys", 1, (short) 1);
    }

    @Bean
    public NewTopic musicRecSysReply() {
        return new NewTopic("musicRecSysReply", 1, (short) 1);
    }

    @Bean
    public ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate(
            ProducerFactory<String, String> producerFactory,
            ConcurrentMessageListenerContainer<String, String> replyContainer) {
        return new ReplyingKafkaTemplate<>(producerFactory, replyContainer);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, String> replyContainer(
            ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory.createContainer("musicRecSysReply", "musicAnalysisReply");
    }
}
