package io.nicheblog.dreamdiary.adapter.kafka.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * KafkaUtils
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
@Profile({"dev", "prod"})
public class KafkaUtils {

    private static final String TOPIC = "dreamdiary";

    private final KafkaTemplate<String, String> template;

    private static KafkaTemplate<String, String> kafkaTemplate;
    private static HttpServletResponse response;

    /** static 맥락에서 사용할 수 있도록 bean 주입 */
    @PostConstruct
    private void init() {
        kafkaTemplate = template;
    }

    /**
     * 메세지 전송
     */
    public static void sendMessage(final String message) {
        log.info("Produce message : {}", message);
        kafkaTemplate.send(TOPIC, message);
    }

    /**
     * 메세지 처리
     */
    @KafkaListener(topics = TOPIC, groupId = "dreamdiary")
    public static void consume(final String message) throws IOException {
        log.info("Consumed message : {}", message);
    }

}
