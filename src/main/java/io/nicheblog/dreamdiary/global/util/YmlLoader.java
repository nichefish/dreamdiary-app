package io.nicheblog.dreamdiary.global.util;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * YmlLoader
 * <pre>
 *  .yml 파일 괸련 유틸리티 모듈
 * </pre>
 *
 * @author nichefish
 */
@UtilityClass
@Log4j2
public class YmlLoader {

    /**
     * 파일 경로 강제로 잡아서 .yml 파일 물리적으로 읽어옴
     *
     * @return {@link String} spring.profiles.active 값
     */
    @SuppressWarnings("unchecked")
    public static String loadSpringProfile() {
        final Yaml yaml = new Yaml();
        final String yamlPath = "config/application.yml"; // ✅ 강제로 경로 지정

        try (final InputStream inputStream = new FileInputStream(yamlPath)) {

            final Map<String, Object> yamlMap = yaml.load(inputStream);
            final Map<String, Object> springMap = (Map<String, Object>) yamlMap.get("spring");
            if (springMap == null) return null;

            final Map<String, Object> profilesMap = (Map<String, Object>) springMap.get("profiles");
            if (profilesMap == null) return null;

            return (String) profilesMap.get("active"); // 기본값 local
        } catch (final Exception e) {
            log.error(MessageUtils.getExceptionMsg(e));
            return null;
        }
    }
}
