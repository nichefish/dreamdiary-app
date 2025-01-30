package io.nicheblog.dreamdiary.global;

import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * ReleaseInfo
 * <pre>
 *  배포 정보 조회
 *  버전 표기는 0.0.0 (메이저.마이너.패치) 형식을 가진다.
 * </pre>
 *
 * @author nichefish
 */
@Component
@ConfigurationProperties(prefix = "spring.flyway")
@Getter
@Setter
public class ReleaseInfo {

    /** 배포 활성화 여부 (flyway) */
    private Boolean enabled;
    /** 배포 대상 버전 */
    private String version;
    /** 배포일자 */
    private String releaseDate;

    /** 버전 히스토리 목록 */
    private List<String> schemaHistory;

    /* ----- */

    /**
     * 배포일자 (Date) 조회
     *
     * @return {@link Date}
     * @throws Exception 발생 가능한 예외
     */
    public Date getReleaseDate() throws Exception {
        return DateUtils.asDate(this.releaseDate);
    }

    /**
     * 배포일자 (문자열) 조회
     *
     * @return {@link String}
     * @throws Exception 발생 가능한 예외
     */
    public String getReleaseDateStr() throws Exception {
        if (StringUtils.isEmpty(this.releaseDate)) return "20010101";

        return this.releaseDate;
    }

    /**
     * 기존 버전에서 새로운 버전으로 업그레이드 가능한지 확인한다.
     *
     * @param existingVersion 현재 사용 중인 버전 (예: "1.2.3")
     * @return {@link Boolean} (인자로 받은) 기존 버전이 더 낮으면 {@code true}, 높거나 같으면 {@code false}를 반환한다.
     */
    public boolean isUpgradeViableFrom(final String existingVersion) {
        final int[] existing = parseVersion(existingVersion);
        final int[] target = parseVersion(this.version);

        for (int i = 0; i < 3; i++) { // Major → Minor → Patch 순서 비교
            if (target[i] > existing[i]) return true;  // 새로운 버전이 더 크면 업그레이드 가능
            if (target[i] < existing[i]) return false; // 새로운 버전이 더 낮으면 업그레이드 불가
        }
        return false; // 모든 값이 같으면 업그레이드 불가
    }

    /**
     * 기존 버전에서 새로운 버전으로 업그레이드 불가능한지 확인한다.
     *
     * @param existingVersion 현재 사용 중인 버전 (예: "1.2.3")
     * @return {@link Boolean} (인자로 받은) 기존 버전이 더 높거나 같으면 {@code true}, 낮으면 {@code false}를 반환한다.
     */
    public boolean isUpgradeNotViableFrom(final String existingVersion) {
        return !isUpgradeViableFrom(existingVersion);
    }

    /**
     * 버전 문자열("1.2.3")을 정수 배열([1,2,3])로 변환
     *
     * @param version 변환할 버전 문자열 (예: "1.2.3")
     * @return 정수 배열 ([메이저, 마이너, 패치])
     */
    private int[] parseVersion(final String version) {
        if (StringUtils.isEmpty(version)) return new int[]{0, 0, 0};

        final String[] parts = version.split("\\.");
        final int[] numbers = new int[3]; // Major, Minor, Patch

        for (int i = 0; i < 3; i++) {
            numbers[i] = (i < parts.length) ? Integer.parseInt(parts[i]) : 0;
        }
        return numbers;
    }
}
