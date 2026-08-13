package io.nicheblog.dreamdiary.infrastructure.cache.config;

import io.nicheblog.dreamdiary.infrastructure.cache.service.RedisConnChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CustomCacheResolverTest {

    private static final String CACHE_NAME = "fixtureCache";

    private CacheManager memoryCacheManager;
    private CacheManager remoteCacheManager;
    private RedisConnChecker redisConnChecker;
    private CustomCacheResolver resolver;

    @BeforeEach
    void setUp() {
        memoryCacheManager = mock(CacheManager.class);
        remoteCacheManager = mock(CacheManager.class);
        redisConnChecker = mock(RedisConnChecker.class);
        resolver = new CustomCacheResolver(memoryCacheManager, remoteCacheManager, redisConnChecker);
    }

    @Test
    void unconfiguredMethodUsesColdMemoryCacheWithoutRedisCheck() throws Exception {
        final Cache memoryCache = new ConcurrentMapCache(CACHE_NAME);
        when(memoryCacheManager.getCache(CACHE_NAME)).thenReturn(memoryCache);

        final Collection<? extends Cache> caches = resolver.resolveCaches(context(new DefaultFixture(), "load"));

        assertThat(asList(caches)).containsExactly(memoryCache);
        verifyNoInteractions(remoteCacheManager, redisConnChecker);
    }

    @Test
    void methodLevelMemoryAndSharedUsesBothCachesWhenRedisIsAvailable() throws Exception {
        final Cache memoryCache = new ConcurrentMapCache(CACHE_NAME);
        final Cache remoteCache = new ConcurrentMapCache(CACHE_NAME);
        when(memoryCacheManager.getCache(CACHE_NAME)).thenReturn(memoryCache);
        when(remoteCacheManager.getCache(CACHE_NAME)).thenReturn(remoteCache);
        when(redisConnChecker.isAvailable()).thenReturn(true);

        final Collection<? extends Cache> caches = resolver.resolveCaches(context(new MethodConfiguredFixture(), "load"));

        assertThat(asList(caches)).containsExactly(memoryCache, remoteCache);
        verify(redisConnChecker).checkRedisConnection();
    }

    @Test
    void classLevelMemoryAndSharedKeepsMemoryCacheWhenRedisIsUnavailable() throws Exception {
        final Cache memoryCache = new ConcurrentMapCache(CACHE_NAME);
        when(memoryCacheManager.getCache(CACHE_NAME)).thenReturn(memoryCache);
        when(redisConnChecker.isAvailable()).thenReturn(false);

        final Collection<? extends Cache> caches = resolver.resolveCaches(context(new ClassConfiguredFixture(), "load"));

        assertThat(asList(caches)).containsExactly(memoryCache);
        verify(redisConnChecker).checkRedisConnection();
        verify(remoteCacheManager, never()).getCache(CACHE_NAME);
    }

    @Test
    void sharedOnlyFailsWhenRedisIsUnavailable() throws Exception {
        when(redisConnChecker.isAvailable()).thenReturn(false);

        assertThatThrownBy(() -> resolver.resolveCaches(context(new SharedOnlyFixture(), "load")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No cache could be resolved");
        verify(memoryCacheManager, never()).getCache(CACHE_NAME);
        verify(remoteCacheManager, never()).getCache(CACHE_NAME);
    }

    @Test
    void missingMemoryNamespaceFailsInsteadOfUsingNoOpCache() throws Exception {
        when(memoryCacheManager.getCache(CACHE_NAME)).thenReturn(null);

        assertThatThrownBy(() -> resolver.resolveCaches(context(new DefaultFixture(), "load")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No cache could be resolved");
        verifyNoInteractions(remoteCacheManager, redisConnChecker);
    }

    @SuppressWarnings("unchecked")
    private CacheOperationInvocationContext<CacheOperation> context(final Object target, final String methodName) throws Exception {
        final Method method = target.getClass().getDeclaredMethod(methodName);
        final CacheOperation operation = mock(CacheOperation.class);
        when(operation.getCacheNames()).thenReturn(Set.of(CACHE_NAME));

        final CacheOperationInvocationContext<CacheOperation> context = mock(CacheOperationInvocationContext.class);
        when(context.getTarget()).thenReturn(target);
        when(context.getMethod()).thenReturn(method);
        when(context.getOperation()).thenReturn(operation);
        return context;
    }

    private List<Cache> asList(final Collection<? extends Cache> caches) {
        return new ArrayList<>(caches);
    }

    private static class DefaultFixture {
        public void load() {}
    }

    private static class MethodConfiguredFixture {
        @CacheableConfig(cacheTarget = CacheableConfig.CacheTarget.MEMORY_AND_SHARED)
        public void load() {}
    }

    @CacheableConfig(cacheTarget = CacheableConfig.CacheTarget.MEMORY_AND_SHARED)
    private static class ClassConfiguredFixture {
        public void load() {}
    }

    private static class SharedOnlyFixture {
        @CacheableConfig(cacheTarget = CacheableConfig.CacheTarget.SHARED)
        public void load() {}
    }
}
