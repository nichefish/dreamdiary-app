package io.nicheblog.dreamdiary.infrastructure.log.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link io.nicheblog.dreamdiary.infrastructure.log.aspect.ServiceEntryLogAspect} 에서
 * 서비스 진입 로그를 남기지 않도록 할 때 클래스 또는 메서드에 붙인다.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoServiceEntryLog {
}
