package io.nicheblog.dreamdiary.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SwaggerConfig
 * <pre>
 *  Swagger SpringDoc 관련 설정
 * </pre>
 *
 * @author nichefish
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {

        // API 명세 상세 설정
        final Info apiInfo = new Info().title("Springdoc 테스트")
                                 .description("Springdoc을 사용한 API 명세서 Swagger UI")
                                 .version("v1.0");

        return new OpenAPI().components(new Components())
                            .info(apiInfo);
    }
}
