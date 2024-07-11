package kr.ac.chungbuk.harmonize.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic musicAnalysis() {
        return new NewTopic("musicAnalysis", 1, (short) 1);
    }
}
