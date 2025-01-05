package io.nicheblog.dreamdiary.adapter.kafka.controller;

import io.nicheblog.dreamdiary.adapter.kafka.util.KafkaUtils;
import io.nicheblog.dreamdiary.global.aspect.log.LogActvtyRestControllerAspect;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

/**
 * KafkaApiController
 * <pre>
 *  Kafka:: 테스트 및 수동 처리 API 컨트롤러.
 * </pre>
 *
 * @see LogActvtyRestControllerAspect
 * @author nichefish
 */
@RestController
@Log4j2
public class KafkaApiController {

    /**
     * 카프카 메세지 발송 테스트 (Ajax)
     *
     * @param message 메세지
     * @return {@link String} -- 응답 메세지
     */
    @PostMapping(value = "/kafka")
    @ResponseBody
    public String sendMessage(
            final @RequestBody String message
    ) {

        log.info("message : {}", message);
        KafkaUtils.sendMessage(message);

        return "success";
    }
}