package io.nicheblog.dreamdiary.infrastructure.web.interceptor;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class VueAssetCacheControlInterceptorTest {

    private final VueAssetCacheControlInterceptor interceptor = new VueAssetCacheControlInterceptor();

    @Test
    void writesImmutableCacheContract() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/vue-app/assets/index-hash.js");
        final MockHttpServletResponse response = new MockHttpServletResponse();

        final boolean shouldContinue = interceptor.preHandle(request, response, new Object());

        assertThat(shouldContinue).isTrue();
        assertThat(response.getHeader(HttpHeaders.CACHE_CONTROL))
                .isEqualTo(VueAssetCacheControlInterceptor.CACHE_CONTROL_VALUE);
    }
}
