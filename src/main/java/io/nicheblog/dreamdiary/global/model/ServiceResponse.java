package io.nicheblog.dreamdiary.global.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ServiceResponse
 * <pre>
 *  (공통) 서비스 Response Body
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceResponse {

    /** 결과 (성공여부) */
    @Builder.Default
    private Boolean rslt = false;

    /** message */
    private String message;

    /** 결과 (목록) */
    private List<?> rsltList;

    /** 결과 (목록) */
    private Page<?> rsltPage;

    /** 결과 (맵) */
    private HashMap<String, ?> rsltMap;

    /** 결과 (숫자) */
    private Integer rsltVal;

    /** 결과 (객체) */
    private Object rsltObj;

    /** 결과 (문자열) */
    private String rsltStr;

    /* ----- */

    /**
     * 생성자.
     *
     * @param rslt 서비스 처리 결과 (true: 성공, false: 실패)
     * @param message 요청에 대한 메시지
     */
    public ServiceResponse(final Boolean rslt, final String message) {
        this.rslt = rslt;
        this.message = message;
    }

    /**
     * 서비스 결과 세팅.
     *
     * @param rslt 서비스 처리 결과 (true: 성공, false: 실패)
     * @param message 요청에 대한 메시지
     */
    public void setResult(final Boolean rslt, final String message) {
        this.rslt = rslt;
        this.message = message;
    }

    /**
     * Map이 들어올 시 HashMap으로 변환.
     *
     * @param map 변환할 Map 객체
     */
    public void setRsltMap(final Map<String, ?> map) {
        this.rsltMap = new HashMap<>(map);
    }
}
