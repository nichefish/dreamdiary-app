package io.nicheblog.dreamdiary.infrastructure.freemarker.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * FreemarkerModelContext
 * <pre>
 *  Freemarker 모델 구성 과정에서 사용하는 요청 단위 컨텍스트 객체.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Builder
public class FreemarkerModelContext {

    private final HttpServletRequest request;
    private final HttpSession session;
    private final ModelAndView mav;

    /** 현재 로그인한 사용자 ID */
    private final String userId;

    /**
     * 모델에서 지정한 키에 해당하는 값을 조회한다.
     *
     * @param key 모델 속성 키
     * @return 해당 키에 매핑된 값 (없을 경우 null)
     */
    public Object getModelAttr(final String key) {
        return mav.getModel().get(key);
    }

    /**
     * 모델에 새로운 속성을 추가한다.
     *
     * @param key 모델 속성 키
     * @param value 저장할 값
     */
    public void addObject(final String key, final Object value) {
        mav.addObject(key, value);
    }
}
