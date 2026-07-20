package io.nicheblog.dreamdiary.feature.journal.entitycatalog.service;

import io.nicheblog.dreamdiary.feature.journal.entitycatalog.type.JournalEntityRoleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link JournalEntityRoleExtractor} 휴리스틱 역할 축 회귀 테스트.
 *
 * <p>가상 픽스처만 사용한다 ({@code 민수}/{@code 지연}).</p>
 */
class JournalEntityRoleExtractorTest {

    private static final String FIXTURE_PERSON_A = "민수";
    private static final String FIXTURE_PERSON_B = "지연";

    @Test
    @DisplayName("협업·긴장 단서가 함께 있으면 COLLABORATION+TENSION")
    void extract_shouldDetectCollaborationAndTension() {
        final String ctx = "저녁에 " + FIXTURE_PERSON_A + "는 함께 공연 이야기를 하며 긴장했다.";
        final Set<JournalEntityRoleType> roles = roleTypes(ctx);
        assertTrue(roles.contains(JournalEntityRoleType.COLLABORATION));
        assertTrue(roles.contains(JournalEntityRoleType.TENSION));
        assertFalse(roles.contains(JournalEntityRoleType.UNKNOWN));
    }

    @Test
    @DisplayName("갈등 단서는 CONFLICT")
    void extract_shouldDetectConflict() {
        final Set<JournalEntityRoleType> roles = roleTypes(FIXTURE_PERSON_B + "와 말다툼이 있었고 대립이 남았다.");
        assertTrue(roles.contains(JournalEntityRoleType.CONFLICT));
    }

    @Test
    @DisplayName("위로·챙김 단서는 CARE")
    void extract_shouldDetectCare() {
        final Set<JournalEntityRoleType> roles = roleTypes(FIXTURE_PERSON_A + "가 나를 위로하고 챙겨 줬다.");
        assertTrue(roles.contains(JournalEntityRoleType.CARE));
    }

    @Test
    @DisplayName("노이즈 단서(조금/작업/처럼)만으로는 역할 축을 만들지 않는다")
    void extract_shouldIgnoreFormerNoiseKeywords() {
        assertEquals(Set.of(JournalEntityRoleType.UNKNOWN), roleTypes("오늘은 조금 쉬고 작업만 했다."));
        assertEquals(Set.of(JournalEntityRoleType.UNKNOWN), roleTypes(FIXTURE_PERSON_A + "처럼 보였다."));
    }

    @Test
    @DisplayName("다중 키워드면 confidence가 기본값보다 높다")
    void extract_shouldBoostConfidenceOnMultiHit() {
        final var single = JournalEntityRoleExtractor.extract(FIXTURE_PERSON_A + "와 함께 있었다.").get(0);
        final var multi = JournalEntityRoleExtractor.extract(
                FIXTURE_PERSON_A + "와 함께 협업하고 의논했다."
        ).stream().filter(r -> r.roleType() == JournalEntityRoleType.COLLABORATION).findFirst().orElseThrow();
        assertTrue(multi.confidence() > single.confidence());
    }

    private static Set<JournalEntityRoleType> roleTypes(final String context) {
        return JournalEntityRoleExtractor.extract(context).stream()
                .map(JournalEntityRoleExtractor.ExtractedRole::roleType)
                .collect(Collectors.toSet());
    }
}
