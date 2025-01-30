package io.nicheblog.dreamdiary.global.handler;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolver;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * UTF8DecodeResourceResolver
 * <pre>
 *  리소스 이름에 한글 사용시 깨짐 방지 위한 처리
 * </pre>
 *
 * @author nichefish
 */
public class UTF8DecodeResourceResolver
        extends PathResourceResolver
        implements ResourceResolver {

    /**
     * 주어진 리소스 경로를 UTF-8로 디코딩하여 반환한다.
     *
     * @param resourcePathParam 요청된 리소스의 경로 (인코딩된 상태)
     * @param location 기본 리소스 위치
     * @return {@link Resource} 디코딩된 리소스를 반환
     * @throws IOException 리소스를 찾을 수 없거나 접근할 수 없는 경우 발생
     */
    @Override
    protected Resource getResource(
            final @NotNull String resourcePathParam,
            final @NotNull Resource location
    ) throws IOException {
        final String resourcePath = URLDecoder.decode(resourcePathParam, StandardCharsets.UTF_8);
        return super.getResource(resourcePath, location);
    }
}