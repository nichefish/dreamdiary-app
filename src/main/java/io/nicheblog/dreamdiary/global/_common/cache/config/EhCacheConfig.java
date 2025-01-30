package io.nicheblog.dreamdiary.global._common.cache.config;

import org.ehcache.jsr107.EhcacheCachingProvider;
import org.ehcache.xml.XmlConfiguration;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.cache.Caching;
import java.net.URL;

/**
 * EhCacheConfig
 * <pre>
 *  EhCache 관련 Config
 * </pre>
 *
 * @author nichefish
 */
@Configuration
public class EhCacheConfig {

    /**
     * jCacheManager 빈 등록
     * (= EhCache를 Spring의 캐시 추상화에 맞게 변환)
     */
    @Bean(name = "jCacheManager")
    @Primary
    public JCacheCacheManager jCacheCacheManager() {
        return new JCacheCacheManager(ehCacheManager());
    }

    /**
     * ehCacheManager 빈 등록
     * (ehCache -> jCache로의 변환을 거쳐 사용)
     */
    @Bean(name = "ehCacheManager")
    @Primary
    public javax.cache.CacheManager ehCacheManager() {
        URL myUrl = getClass().getClassLoader().getResource("config/ehcache/ehcache.xml");
        XmlConfiguration xmlConfig = new XmlConfiguration(myUrl);
        EhcacheCachingProvider provider = (EhcacheCachingProvider) Caching.getCachingProvider("org.ehcache.jsr107.EhcacheCachingProvider");
        return provider.getCacheManager(provider.getDefaultURI(),  xmlConfig);
    }
}