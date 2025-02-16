package io.nicheblog.dreamdiary.global.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AjaxResponse
 * <pre>
 *  (공통) Ajax Response Body
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AjaxResponse
        extends ServiceResponse {

    /** status */
    private Integer status;

    /** TODO: url to replace? */
    private String url;

    /** 페이징 정보 */
    private PaginationInfo pagination;

    /* ----- */

    /**
     * 생성자.
     *
     * @param rslt AJAX 요청 결과 (true: 성공, false: 실패)
     * @param message 요청에 대한 메시지
     */
    public AjaxResponse(final Boolean rslt, final String message) {
        super(rslt, message);
        if (this.status == null) this.status = rslt ? 200 : 400;
    }

    /**
     * Ajax 결과 세팅.
     *
     * @param rslt AJAX 요청 결과 (true: 성공, false: 실패)
     * @param message 요청에 대한 메시지
     */
    public static AjaxResponse withAjaxResult(final Boolean rslt, final String message) {
        return AjaxResponse.builder()
                .rslt(rslt)
                .message(message)
                .status(rslt ? 200 : 400)
                .build();
    }

    /**
     * 정적 팩토리 메소드
     */
    public static AjaxResponse fromResponse(final ServiceResponse response, final String rsltMsg) {
        return AjaxResponse.builder()
                .rslt(response.getRslt())
                .message(rsltMsg)
                .build();
    }

    /**
     * 정적 팩토리 메소드
     */
    public static AjaxResponse fromResponseWithObj(final ServiceResponse response, final String rsltMsg) {
        return AjaxResponse.builder()
                .rslt(response.getRslt())
                .message(rsltMsg)
                .rsltObj(response.getRsltObj())
                .build();
    }

    /**
     * Ajax 결과 세팅.
     *
     * @param rslt AJAX 요청 결과 (true: 성공, false: 실패)
     * @param message 요청에 대한 메시지
     */
    public AjaxResponse setAjaxResult(final Boolean rslt, final String message) {
        setResult(rslt, message);
        if (this.status == null) this.status = rslt ? 200 : 400;
        return this;
    }

    /**
     * 체이닝 메소드
     */
    public AjaxResponse withObj(Object rsltObj) {
        this.setRsltObj(rsltObj);
        return this;
    }

    /**
     * 체이닝 메소드
     */
    public AjaxResponse withMap(HashMap<String, ?> rsltMap) {
        this.setRsltMap(rsltMap);
        return this;
    }
    /**
     * Map이 들어올 시 HashMap으로 변환.
     *
     * @param map 변환할 Map 객체
     */
    public AjaxResponse withMap(final Map<String, ?> map) {
        this.setRsltMap(new HashMap<>(map));
        return this;
    }

    /**
     * 체이닝 메소드
     */
    public AjaxResponse withList(List<?> rsltList) {
        this.setRsltList(rsltList);
        return this;
    }
}
