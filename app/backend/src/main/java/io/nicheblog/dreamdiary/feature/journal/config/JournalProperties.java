package io.nicheblog.dreamdiary.feature.journal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JournalProperties
 * <pre>
 *  Journal runtime policy settings.
 * </pre>
 *
 * @author nichefish
 */
@Component
@ConfigurationProperties(prefix = "app.journal")
@Getter
@Setter
public class JournalProperties {

    /** Journal embedding settings. */
    private Embedding embedding;
    /** Journal entity sync settings. */
    private Entity entity;

    /**
     * Journal embedding settings.
     */
    @Getter
    @Setter
    public static class Embedding {
        /** Whether to load persisted embedding vectors into the search cache on startup. */
        private Boolean cacheOnStartup = true;
        /** Whether to enqueue embedding sync on startup. */
        private Boolean syncOnStartup;
        /** Embedding worker settings. */
        private Worker worker;
    }

    /**
     * Journal entity sync settings.
     */
    @Getter
    @Setter
    public static class Entity {
        /** Entity worker settings. */
        private Worker worker;
    }

    /**
     * Journal worker settings.
     */
    @Getter
    @Setter
    public static class Worker {
        /** Worker batch size. */
        private Integer batchSize;
    }
}
