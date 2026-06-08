package io.nicheblog.dreamdiary.feature.journal.entitycatalog.service;

import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contract tests for the first-phase journal entity reference sync heuristics.
 */
class JournalEntryEntityRefSyncServiceTest {

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
                .title("\uC6D0\uBE48\uB2D8\uC744 \uB2E4\uC2DC \uBD24\uB2E4")
                .content("\uC800\uB141\uC5D0 \uC6D0\uBE48\uC740 \uACF5\uC5F0 \uC774\uC57C\uAE30\uB97C \uAED8 \uD588\uB2E4.")
                .elseDreamerNm("\uBBFC\uC9C0")
                .build();

        @SuppressWarnings("unchecked")
        final List<Object> mentionList = (List<Object>) method.invoke(service, entry);

        assertEquals(2, mentionList.size());
        final String text = mentionList.toString();
        assertTrue(text.contains("\uC6D0\uBE48"));
        assertTrue(text.contains("\uBBFC\uC9C0"));
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

        final String honorific = (String) canonicalizeMethod.invoke(service, "\uC6D0\uBE48\uB2D8");
        final String particle = (String) canonicalizeMethod.invoke(service, "\uC6D0\uBE48\uC740");

        assertEquals("\uC6D0\uBE48", honorific);
        assertEquals("\uC6D0\uBE48", particle);
    }

    /**
     * Mention contexts should yield at least one concrete role when collaboration/tension keywords appear nearby.
     */
    @Test
    void extractEntityRoles_shouldInferRoleFromMentionContext() throws Exception {
        final JournalEntryEntityRefSyncService service =
                new JournalEntryEntityRefSyncService(null, null, null, null);
        final Method mentionCtor = Class.forName(
                        "io.nicheblog.dreamdiary.feature.journal.entitycatalog.service.JournalEntryEntityRefSyncService$ExtractedPersonMention")
                .getDeclaredConstructor(String.class, String.class, io.nicheblog.dreamdiary.feature.journal.entitycatalog.type.JournalEntityMentionType.class, String.class, String.class, Double.class);
        mentionCtor.setAccessible(true);
        final Object mention = mentionCtor.newInstance(
                "\uC6D0\uBE48\uC740",
                "\uC6D0\uBE48",
                io.nicheblog.dreamdiary.feature.journal.entitycatalog.type.JournalEntityMentionType.DIRECT,
                "\uC6D0\uBE48\uC740 \uD568\uAED8 \uACF5\uC5F0 \uC774\uC57C\uAE30\uB97C \uD588\uB2E4.",
                "\uC800\uB141\uC5D0 \uC6D0\uBE48\uC740 \uD568\uAED8 \uACF5\uC5F0 \uC774\uC57C\uAE30\uB97C \uD558\uBA70 \uC870\uAE08 \uAE34\uC7A5\uD588\uB2E4.",
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
