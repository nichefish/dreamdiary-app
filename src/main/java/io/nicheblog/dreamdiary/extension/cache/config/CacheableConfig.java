package io.nicheblog.dreamdiary.extension.cache.config;

import java.lang.annotation.*;

/**
 * CacheableConfig
 * <pre>
 *  CacheableConfig 관련 커스텀 annotation 추가.
 * </pre>
 *
 * @author nichefish
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CacheableConfig {
    enum CacheTarget {
        MEMORY,
        SHARED,
        MEMORY_AND_SHARED
    }

    CacheTarget cacheTarget() default CacheTarget.MEMORY_AND_SHARED;
}