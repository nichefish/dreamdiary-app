package io.nicheblog.dreamdiary.feature.admin.web.controller;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * I18nCatalogController
 * <pre>
 *  로그인 화면 Vue i18n용 message catalog를 JSON으로 제공합니다.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@Log4j2
public class I18nCatalogController {

    private static final String MESSAGE_BUNDLE_BASENAME = "messages/messages";

    /**
     * locale별 메시지 번들 catalog를 JSON으로 반환합니다.
     *
     * @param localePath locale path variable (default|ko|en 등)
     * @return 메시지 key/value 맵
     */
    @GetMapping(value = "/i18n/{localePath}.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> getCatalog(
            final @PathVariable("localePath") String localePath
    ) {
        final Locale locale = resolveLocale(localePath);
        final Map<String, String> catalog = loadCatalog(locale);
        log.info("i18n catalog served. localePath={}, resolvedLocale={}, size={}", localePath, locale, catalog.size());
        return ResponseEntity.ok(catalog);
    }

    /**
     * 요청 locale 값을 서버 Locale 객체로 변환합니다.
     *
     * @param localePath 요청 locale
     * @return 변환된 Locale
     */
    private Locale resolveLocale(final String localePath) {
        if (StringUtils.isBlank(localePath) || "default".equalsIgnoreCase(localePath)) {
            return Locale.getDefault();
        }

        final String normalized = localePath.trim().replace('_', '-').toLowerCase(Locale.ROOT);
        if (normalized.startsWith("ko")) return Locale.KOREAN;
        if (normalized.startsWith("en")) return Locale.ENGLISH;

        final Locale parsedLocale = Locale.forLanguageTag(normalized);
        if (StringUtils.isBlank(parsedLocale.getLanguage())) {
            log.warn("Unsupported localePath detected. Fallback to default locale. localePath={}", localePath);
            return Locale.getDefault();
        }

        return parsedLocale;
    }

    /**
     * locale에 해당하는 메시지 번들을 로드해 맵으로 변환합니다.
     *
     * @param locale 메시지 조회 locale
     * @return 메시지 key/value 맵
     */
    private Map<String, String> loadCatalog(final Locale locale) {
        try {
            final ResourceBundle bundle = ResourceBundle.getBundle(MESSAGE_BUNDLE_BASENAME, locale);
            final Map<String, String> catalog = new HashMap<>();
            bundle.keySet().forEach((key) -> catalog.put(key, bundle.getString(key)));
            return catalog;
        } catch (final MissingResourceException e) {
            log.error("Message bundle not found for locale: {}", locale, e);
            return Map.of();
        } catch (final Exception e) {
            log.error("Unexpected error while loading i18n catalog. locale={}", locale, e);
            return Map.of();
        }
    }
}
