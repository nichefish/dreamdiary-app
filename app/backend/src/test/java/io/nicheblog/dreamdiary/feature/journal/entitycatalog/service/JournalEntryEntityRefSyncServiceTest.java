package io.nicheblog.dreamdiary.feature.journal.entitycatalog.service;

import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contract tests for the first-phase journal entity reference sync heuristics.
 *
 * <p>가상 픽스처만 사용한다 ({@code 민수}/{@code 지연}).</p>
 */
class JournalEntryEntityRefSyncServiceTest {

    /** 가상 인물 A */
    private static final String FIXTURE_PERSON_A = "민수";
    /** 가상 인물 B (dreamer 필드) */
    private static final String FIXTURE_PERSON_B = "지연";

    /**
     * Direct person mentions with particles and honorifics should survive as canonical names.
     */
    @Test
    void extractPersonMentions_shouldCaptureDirectPersonNames() throws Exception {
        final JournalEntryEntityRefSyncService service =
                new JournalEntryEntityRefSyncService(null, null, null, null);
        final Method method = JournalEntryEntityRefSyncService.class.getDeclaredMethod(
                "extractPersonMentions",
                JournalEntryEntity.class
        );
        method.setAccessible(true);

        final JournalEntryEntity entry = JournalEntryEntity.builder()
                .title(FIXTURE_PERSON_A + "님을 다시 봤다")
                .content("저녁에 " + FIXTURE_PERSON_A + "는 공연 이야기를 했다.")
                .elseDreamerNm(FIXTURE_PERSON_B)
                .build();

        @SuppressWarnings("unchecked")
        final List<Object> mentionList = (List<Object>) method.invoke(service, entry);

        // honorific(title) + particle(content) + dreamer field may yield 2~3 rows for two people
        assertTrue(mentionList.size() >= 2);
        final String text = mentionList.toString();
        assertTrue(text.contains(FIXTURE_PERSON_A));
        assertTrue(text.contains(FIXTURE_PERSON_B));
    }

    /**
     * Canonical labels should remove trailing particles and honorifics, but keep the display form.
     */
    @Test
    void canonicalizePersonLabel_shouldStripHonorificAndParticle() throws Exception {
        final JournalEntryEntityRefSyncService service =
                new JournalEntryEntityRefSyncService(null, null, null, null);
        final Method canonicalizeMethod = JournalEntryEntityRefSyncService.class.getDeclaredMethod(
                "canonicalizePersonLabel",
                String.class
        );
        canonicalizeMethod.setAccessible(true);

        final String honorific = (String) canonicalizeMethod.invoke(service, FIXTURE_PERSON_A + "님");
        final String particle = (String) canonicalizeMethod.invoke(service, FIXTURE_PERSON_A + "는");

        assertEquals(FIXTURE_PERSON_A, honorific);
        assertEquals(FIXTURE_PERSON_A, particle);
    }

    /**
     * Mention contexts should yield concrete roles via {@link JournalEntityRoleExtractor}.
     */
    @Test
    void extractEntityRoles_shouldInferRoleFromMentionContext() throws Exception {
        final JournalEntryEntityRefSyncService service =
                new JournalEntryEntityRefSyncService(null, null, null, null);
        final Constructor<?> mentionCtor = Class.forName(
                        "io.nicheblog.dreamdiary.feature.journal.entitycatalog.service.JournalEntryEntityRefSyncService$ExtractedPersonMention")
                .getDeclaredConstructor(String.class, String.class, io.nicheblog.dreamdiary.feature.journal.entitycatalog.type.JournalEntityMentionType.class, String.class, String.class, Double.class);
        mentionCtor.setAccessible(true);
        final Object mention = mentionCtor.newInstance(
                FIXTURE_PERSON_A + "은",
                FIXTURE_PERSON_A,
                io.nicheblog.dreamdiary.feature.journal.entitycatalog.type.JournalEntityMentionType.DIRECT,
                FIXTURE_PERSON_A + "은 함께 공연 이야기를 했다.",
                "저녁에 " + FIXTURE_PERSON_A + "은 함께 공연 이야기를 하며 긴장했다.",
                0.82D
        );

        final Method method = JournalEntryEntityRefSyncService.class.getDeclaredMethod("extractEntityRoles", mention.getClass());
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        final List<Object> roleList = (List<Object>) method.invoke(service, mention);

        final String roleText = roleList.toString();
        assertTrue(roleText.contains("COLLABORATION"));
        assertTrue(roleText.contains("TENSION"));
    }
}
