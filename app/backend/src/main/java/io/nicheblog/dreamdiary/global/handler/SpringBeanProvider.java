package io.nicheblog.dreamdiary.global.handler;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * SpringBeanProvider
 * <pre>
 *  정적 컨텍스트에서 Spring Bean 조회를 위한 Provider.
 * </pre>
 *
 * @author nichefish
 */
@Component
public class SpringBeanProvider implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static <T> T getBean(final Class<T> clazz) {
        if (context == null) {
            throw new IllegalStateException("ApplicationContext is not initialized.");
        }
        return context.getBean(clazz);
    }
}
