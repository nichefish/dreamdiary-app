package io.nicheblog.dreamdiary.feature.journal.entitycatalog.service;

import io.nicheblog.dreamdiary.feature.journal.embedding.entity.JournalEntryEmbeddingEntity;
import io.nicheblog.dreamdiary.feature.journal.embedding.repository.jpa.JournalEntryEmbeddingRepository;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.entity.JournalEntityEntity;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.entity.JournalEntryEntityRefEntity;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.entity.JournalEntryEntityRoleEntity;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.repository.jpa.JournalEntityRepository;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.repository.jpa.JournalEntryEntityRefRepository;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.repository.jpa.JournalEntryEntityRoleRepository;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.type.JournalEntityRoleType;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.type.JournalEntityType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Resolves entity-catalog-backed focus summaries for AI chat.
 *
 * <p>The current phase exposes person summaries first, but the service name stays
 * entity-oriented so the same path can later grow into EVENT/PLACE/SYMBOL focus.</p>
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalEntityFocusService {

    private final JournalEntityRepository journalEntityRepository;
    private final JournalEntryEntityRefRepository journalEntryEntityRefRepository;
    private final JournalEntryEntityRoleRepository journalEntryEntityRoleRepository;
    private final JournalEntryEmbeddingRepository journalEntryEmbeddingRepository;

    /**
     * Resolve the strongest PERSON focus summary from query tokens.
     *
     * @param personTokens query tokens extracted from a person-meaning question
     * @return optional PERSON focus summary
     */
    public Optional<PersonEntityFocusSummary> resolvePersonFocusSummary(final List<String> personTokens) {
        if (personTokens == null || personTokens.isEmpty()) return Optional.empty();

        for (final String token : personTokens) {
            final String normalizedToken = normalizeLookupLabel(token);
            if (StringUtils.isBlank(normalizedToken)) continue;

            final Optional<JournalEntityEntity> entityOpt =
                    journalEntityRepository.findFirstByEntityTypeAndNormalizedLabel(JournalEntityType.PERSON, normalizedToken);
            if (entityOpt.isEmpty()) continue;

            final Optional<PersonEntityFocusSummary> summaryOpt = buildPersonEntityFocusSummary(entityOpt.get(), personTokens);
            if (summaryOpt.isPresent()) {
                log.info(
                        "Resolved journal entity focus summary. entityId={}, canonicalLabel={}, queryTokens={}",
                        entityOpt.get().getId(),
                        entityOpt.get().getCanonicalLabel(),
                        personTokens
                );
                return summaryOpt;
            }
        }

        log.info("No journal entity focus summary resolved. queryTokens={}", personTokens);
        return Optional.empty();
    }

    /**
     * Build one PERSON summary from the entity catalog and extracted mention refs.
     */
    private Optional<PersonEntityFocusSummary> buildPersonEntityFocusSummary(
            final JournalEntityEntity entity,
            final List<String> queryTokens
    ) {
        if (entity == null || entity.getId() == null) return Optional.empty();

        final List<JournalEntryEntityRefEntity> refList =
                journalEntryEntityRefRepository.findAllByJournalEntityIdOrderByCreatedAtAscIdAsc(entity.getId());
        if (refList.isEmpty()) return Optional.empty();

        final List<Integer> journalEntryIdList = refList.stream()
                .map(JournalEntryEntityRefEntity::getJournalEntryId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        final List<Integer> refIdList = refList.stream()
                .map(JournalEntryEntityRefEntity::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
        final Map<Integer, JournalEntryEmbeddingEntity> embeddingMap = loadEmbeddingMap(journalEntryIdList);
        final Map<JournalEntityRoleType, Integer> roleCountMap = loadRoleCountMap(refIdList);

        final Map<String, Integer> contentKindCountMap = new LinkedHashMap<>();
        final Map<String, Integer> surfaceFormCountMap = new LinkedHashMap<>();
        final List<Date> journalDateList = new ArrayList<>();
        final Set<Integer> linkedJournalEntryIdSet = new LinkedHashSet<>();

        for (final JournalEntryEntityRefEntity refEntity : refList) {
            if (refEntity == null) continue;

            if (refEntity.getJournalEntryId() != null) {
                linkedJournalEntryIdSet.add(refEntity.getJournalEntryId());
            }

            final String surfaceText = StringUtils.trimToEmpty(refEntity.getSurfaceText());
            if (StringUtils.isNotBlank(surfaceText)) {
                surfaceFormCountMap.merge(surfaceText, 1, Integer::sum);
            }

            final JournalEntryEmbeddingEntity embeddingEntity = embeddingMap.get(refEntity.getJournalEntryId());
            if (embeddingEntity == null) continue;

            final String contentKind = StringUtils.defaultIfBlank(embeddingEntity.getContentKind(), "UNKNOWN");
            contentKindCountMap.merge(contentKind, 1, Integer::sum);

            if (embeddingEntity.getJournalDate() != null) {
                journalDateList.add(embeddingEntity.getJournalDate());
            }
        }

        journalDateList.sort(Date::compareTo);
        final String firstDate = journalDateList.isEmpty() ? null : formatDate(journalDateList.get(0));
        final String lastDate = journalDateList.isEmpty() ? null : formatDate(journalDateList.get(journalDateList.size() - 1));

        return Optional.of(
                new PersonEntityFocusSummary(
                        entity.getId(),
                        entity.getCanonicalLabel(),
                        entity.getNormalizedLabel(),
                        distinctTokens(queryTokens),
                        refList.size(),
                        linkedJournalEntryIdSet.size(),
                        firstDate,
                        lastDate,
                        contentKindCountMap,
                        roleCountMap,
                        surfaceFormCountMap,
                        new ArrayList<>(linkedJournalEntryIdSet)
                )
        );
    }

    /**
     * Load embedding rows keyed by journal entry ID so timeline/kind summaries stay aligned
     * with the same retrieval vocabulary used by chat RAG.
     */
    private Map<Integer, JournalEntryEmbeddingEntity> loadEmbeddingMap(final Collection<Integer> journalEntryIdList) {
        if (journalEntryIdList == null || journalEntryIdList.isEmpty()) return Map.of();

        final List<JournalEntryEmbeddingEntity> embeddingList =
                journalEntryEmbeddingRepository.findAllByJournalEntryIdIn(journalEntryIdList);
        final Map<Integer, JournalEntryEmbeddingEntity> embeddingMap = new LinkedHashMap<>();
        for (final JournalEntryEmbeddingEntity embeddingEntity : embeddingList) {
            if (embeddingEntity == null || embeddingEntity.getJournalEntryId() == null) continue;

            embeddingMap.merge(
                    embeddingEntity.getJournalEntryId(),
                    embeddingEntity,
                    (left, right) -> pickPreferredEmbedding(left, right)
            );
        }
        return embeddingMap;
    }

    /**
     * Prefer rows with a journal date/content kind, then the newer one, when multiple embedding rows exist.
     */
    private JournalEntryEmbeddingEntity pickPreferredEmbedding(
            final JournalEntryEmbeddingEntity left,
            final JournalEntryEmbeddingEntity right
    ) {
        if (left == null) return right;
        if (right == null) return left;

        final int leftScore = scoreEmbeddingForFocus(left);
        final int rightScore = scoreEmbeddingForFocus(right);
        if (leftScore != rightScore) {
            return rightScore > leftScore ? right : left;
        }

        final Date leftUpdatedAt = left.getUpdatedAt();
        final Date rightUpdatedAt = right.getUpdatedAt();
        if (leftUpdatedAt == null) return right;
        if (rightUpdatedAt == null) return left;
        return rightUpdatedAt.after(leftUpdatedAt) ? right : left;
    }

    /**
     * Score embedding usefulness for entity focus summaries.
     */
    private int scoreEmbeddingForFocus(final JournalEntryEmbeddingEntity entity) {
        int score = 0;
        if (entity == null) return score;
        if (entity.getJournalDate() != null) score += 2;
        if (StringUtils.isNotBlank(entity.getContentKind())) score += 1;
        return score;
    }

    /**
     * Normalize query/entity lookup labels onto the same lowercase key.
     */
    private String normalizeLookupLabel(final String token) {
        return StringUtils.lowerCase(StringUtils.deleteWhitespace(StringUtils.trimToEmpty(token)));
    }

    /**
     * Keep token order stable while removing blanks and duplicates.
     */
    private List<String> distinctTokens(final List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) return List.of();
        final Set<String> seen = new LinkedHashSet<>();
        final List<String> distinctTokens = new ArrayList<>();
        for (final String token : tokens) {
            final String value = StringUtils.trimToEmpty(token);
            if (StringUtils.isBlank(value) || !seen.add(value)) continue;
            distinctTokens.add(value);
        }
        return distinctTokens;
    }

    /**
     * Format one journal date into the same yyyy-MM-dd shape used by RAG summaries.
     */
    private String formatDate(final Date date) {
        if (date == null) return null;
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    /**
     * Aggregate role counts from all mention-level role rows linked to the resolved refs.
     */
    private Map<JournalEntityRoleType, Integer> loadRoleCountMap(final Collection<Integer> refIdList) {
        if (refIdList == null || refIdList.isEmpty()) return Map.of();

        final List<JournalEntryEntityRoleEntity> roleList =
                journalEntryEntityRoleRepository.findAllByJournalEntryEntityRefIdIn(refIdList);
        final Map<JournalEntityRoleType, Integer> roleCountMap = new LinkedHashMap<>();
        for (final JournalEntryEntityRoleEntity roleEntity : roleList) {
            if (roleEntity == null || roleEntity.getRoleType() == null) continue;
            roleCountMap.merge(roleEntity.getRoleType(), 1, Integer::sum);
        }
        return roleCountMap;
    }

    /**
     * PERSON focus summary derived from the entity catalog.
     */
    public record PersonEntityFocusSummary(
            Integer journalEntityId,
            String canonicalLabel,
            String normalizedLabel,
            List<String> queryTokens,
            int mentionCount,
            int journalEntryCount,
            String firstDate,
            String lastDate,
            Map<String, Integer> contentKindCountMap,
            Map<JournalEntityRoleType, Integer> roleCountMap,
            Map<String, Integer> surfaceFormCountMap,
            List<Integer> journalEntryIds
    ) {

        /**
         * Return the most repeated surface forms first for prompt/metadata display.
         */
        public List<String> topSurfaceForms(final int limit) {
            if (surfaceFormCountMap == null || surfaceFormCountMap.isEmpty()) return List.of();
            return surfaceFormCountMap.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                    .limit(limit)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        }

        /**
         * Return the most repeated role types first for prompt/metadata display.
         */
        public List<String> topRoles(final int limit) {
            if (roleCountMap == null || roleCountMap.isEmpty()) return List.of();
            return roleCountMap.entrySet().stream()
                    .sorted(Map.Entry.<JournalEntityRoleType, Integer>comparingByValue(Comparator.reverseOrder()))
                    .limit(limit)
                    .map(entry -> entry.getKey().name() + "(" + entry.getValue() + ")")
                    .collect(Collectors.toList());
        }
    }
}
