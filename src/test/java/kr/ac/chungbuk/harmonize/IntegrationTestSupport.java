package kr.ac.chungbuk.harmonize;

import kr.ac.chungbuk.harmonize.config.KafkaTopicConfig;
import kr.ac.chungbuk.harmonize.config.ScheduledTask;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

@SpringBootTest
@MockBeans({
        @MockBean(KafkaTopicConfig.class),
        @MockBean(ReplyingKafkaTemplate.class),
        @MockBean(ScheduledTask.class)
})
public class IntegrationTestSupport {

    @MockBean
    protected ReplyingKafkaTemplate<String, String, String> kafkaTemplate;

}
