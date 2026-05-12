package io.nicheblog.dreamdiary.infrastructure.freemarker.port;

import io.nicheblog.dreamdiary.infrastructure.freemarker.interceptor.FreemarkerInterceptor;
import io.nicheblog.dreamdiary.infrastructure.freemarker.model.FreemarkerModelContext;

/**
 * FreemarkerModelContributor
 * <pre>
 *  Freemarker 모델 구성 과정에 참여하는 확장 포인트(Contributor) 인터페이스.
 * </pre>
 *
 * @author nichefish
 * @see FreemarkerInterceptor
 */
public interface FreemarkerModelContributor {

    /**
     * Freemarker 모델에 feature별 데이터를 추가한다.
     *
     * @param context 요청 단위 Freemarker 모델 컨텍스트
     * @throws Exception 처리 중 예외 발생 시
     */
    void contribute(final FreemarkerModelContext context) throws Exception;
}
