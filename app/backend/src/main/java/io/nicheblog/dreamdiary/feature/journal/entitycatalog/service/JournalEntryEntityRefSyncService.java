package io.nicheblog.dreamdiary.feature.journal.entitycatalog.service;

import io.nicheblog.dreamdiary.feature.journal.entitycatalog.entity.JournalEntityEntity;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.entity.JournalEntryEntityRefEntity;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.entity.JournalEntryEntityRoleEntity;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.repository.jpa.JournalEntityRepository;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.repository.jpa.JournalEntryEntityRefRepository;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.type.JournalEntityMentionType;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.type.JournalEntityRoleType;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.type.JournalEntityType;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.repository.jpa.JournalEntryEntityRoleRepository;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Syncs direct person mentions from journal entries into the normalized entity catalog.
 *
 * <p>This first phase intentionally stays heuristic and person-only. The schema is
 * entity-oriented, but extraction currently opens {@code PERSON} direct mentions only.
 * Role axes are delegated to {@link JournalEntityRoleExtractor}.</p>
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalEntryEntityRefSyncService {

    private static final Pattern HONORIFIC_NAME_PATTERN = Pattern.compile("([\\p{IsHangul}A-Za-z]{2,20})(\uB2D8|\uC528|\uAD70|\uC591)");
    private static final Pattern PARTICLE_NAME_PATTERN = Pattern.compile("([\\p{IsHangul}A-Za-z]{2,20})(\uC740|\uB294|\uC774|\uAC00|\uC744|\uB97C|\uACFC|\uC640|\uB3C4|\uC5D0\uAC8C|\uD55C\uD14C|\uAED8)");
    private static final int SNIPPET_RADIUS = 18;
    private static final int ROLE_CONTEXT_RADIUS = 36;
    private static final Set<String> PERSON_STOPWORDS = Set.of(
            "\uADF8\uB0E5", "\uC815\uB9D0", "\uC624\uB298", "\uC5B4\uC81C", "\uB0B4\uC77C",
            "\uC0AC\uB78C", "\uAE30\uB85D", "\uAFC8", "\uC77C\uAE30", "\uB178\uD2B8",
            "\uC758\uBBF8", "\uD574\uC11D", "\uC874\uC7AC", "\uC5ED\uD560", "\uB4F1\uC7A5",
            "\uBC18\uBCF5", "\uAC10\uC815", "\uC0DD\uAC01", "\uB9C8\uC74C", "\uB290\uB08C",
            "\uAD00\uACC4", "\uD68C\uC0AC", "\uACF5\uC5F0", "\uBE44\uAD50", "\uBD88\uC548"
    );

    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntityRepository journalEntityRepository;
    private final JournalEntryEntityRefRepository journalEntryEntityRefRepository;
    private final JournalEntryEntityRoleRepository journalEntryEntityRoleRepository;

    /**
     * Sync all current journal entries into entity references.
     *
     * @return processed entry count
     * @throws Exception when sync fails
     */
    @Transactional
    public int syncWithJournalEntries() throws Exception {
        final List<JournalEntryEntity> entryList = journalEntryRepository.findAll();
        final Set<Integer> activeEntryIdSet = new HashSet<>();
        for (final JournalEntryEntity entry : entryList) {
            if (entry != null && entry.getId() != null) {
                activeEntryIdSet.add(entry.getId());
            }
        }

        final List<JournalEntryEntityRefEntity> existingRefList = journalEntryEntityRefRepository.findAll();
        final List<JournalEntryEntityRefEntity> staleRefList = new ArrayList<>();
        for (final JournalEntryEntityRefEntity refEntity : existingRefList) {
            if (refEntity == null || refEntity.getJournalEntryId() == null) continue;
            if (!activeEntryIdSet.contains(refEntity.getJournalEntryId())) {
                staleRefList.add(refEntity);
            }
        }
        if (!staleRefList.isEmpty()) {
            deleteRolesByRefs(staleRefList);
            journalEntryEntityRefRepository.deleteAll(staleRefList);
            log.info("Journal entry entity ref full sync removed stale refs. removedRefs={}", staleRefList.size());
        }

        int processed = 0;
        for (final JournalEntryEntity entry : entryList) {
            syncForEntry(entry);
            processed++;
        }
        log.info("Journal entry entity ref full sync completed. processed={}", processed);
        return processed;
    }

    /**
     * Sync one journal entry into entity references.
     *
     * @param journalEntryId journal entry ID
     * @throws Exception when sync fails
     */
    @Transactional
    public void syncForEntryId(final Integer journalEntryId) throws Exception {
        if (journalEntryId == null) return;

        final JournalEntryEntity entry = journalEntryRepository.findById(journalEntryId).orElse(null);
        if (entry == null) {
            log.info("Journal entry entity ref sync remove path. entryId={}", journalEntryId);
            removeByJournalEntryId(journalEntryId);
            return;
        }

        syncForEntry(entry);
    }

    /**
     * Remove entity references for one journal entry.
     *
     * @param journalEntryId journal entry ID
     */
    @Transactional
    public void removeByJournalEntryId(final Integer journalEntryId) {
        if (journalEntryId == null) return;

        final List<JournalEntryEntityRefEntity> existingRefList =
                journalEntryEntityRefRepository.findAllByJournalEntryIdOrderBySortOrderAscIdAsc(journalEntryId);
        if (existingRefList.isEmpty()) {
            log.info("Journal entry entity ref remove skipped. entryId={}, existingRefs=0", journalEntryId);
            return;
        }

        deleteRolesByRefs(existingRefList);
        journalEntryEntityRefRepository.deleteAll(existingRefList);
        log.info("Journal entry entity ref removed. entryId={}, removedRefs={}", journalEntryId, existingRefList.size());
    }

    /**
     * Sync one loaded journal entry into entity references.
     */
    private void syncForEntry(final JournalEntryEntity entry) throws Exception {
        if (entry == null || entry.getId() == null) return;

        final List<ExtractedPersonMention> mentionList = extractPersonMentions(entry);
        final List<JournalEntryEntityRefEntity> existingRefList =
                journalEntryEntityRefRepository.findAllByJournalEntryIdOrderBySortOrderAscIdAsc(entry.getId());
        if (!existingRefList.isEmpty()) {
            deleteRolesByRefs(existingRefList);
            journalEntryEntityRefRepository.deleteAll(existingRefList);
        }

        if (mentionList.isEmpty()) {
            log.info("Journal entry entity ref sync produced no direct person mentions. entryId={}", entry.getId());
            return;
        }

        int sortOrder = 1;
        for (final ExtractedPersonMention mention : mentionList) {
            final JournalEntityEntity entity = upsertPersonEntity(mention.canonicalLabel());
            final JournalEntryEntityRefEntity refEntity = JournalEntryEntityRefEntity.builder()
                    .journalEntryId(entry.getId())
                    .journalEntityId(entity.getId())
                    .surfaceText(mention.surfaceText())
                    .mentionType(mention.mentionType())
                    .evidenceSnippet(mention.evidenceSnippet())
                    .confidence(mention.confidence())
                    .sortOrder(sortOrder++)
                    .build();
            final JournalEntryEntityRefEntity savedRefEntity = journalEntryEntityRefRepository.save(refEntity);
            saveRoleRows(savedRefEntity, mention);
        }

        log.info(
                "Journal entry entity ref sync completed. entryId={}, existingRefsRemoved={}, createdRefs={}",
                entry.getId(),
                existingRefList.size(),
                mentionList.size()
        );
    }

    /**
     * Upsert the normalized person entity row.
     */
    private JournalEntityEntity upsertPersonEntity(final String canonicalLabel) {
        final String normalizedLabel = normalizePersonLabel(canonicalLabel);
        final Optional<JournalEntityEntity> existing = journalEntityRepository.findFirstByEntityTypeAndNormalizedLabel(
                JournalEntityType.PERSON,
                normalizedLabel
        );
        if (existing.isPresent()) {
            return existing.get();
        }

        return journalEntityRepository.saveAndFlush(
                JournalEntityEntity.builder()
                        .entityType(JournalEntityType.PERSON)
                        .canonicalLabel(canonicalLabel)
                        .normalizedLabel(normalizedLabel)
                        .build()
        );
    }

    /**
     * Extract direct person mentions from one journal entry.
     */
    private List<ExtractedPersonMention> extractPersonMentions(final JournalEntryEntity entry) {
        final Map<String, ExtractedPersonMention> mentionMap = new LinkedHashMap<>();
        collectMentionsFromText(mentionMap, entry == null ? null : entry.getTitle());
        collectMentionsFromText(mentionMap, entry == null ? null : entry.getContent());
        collectDreamerMention(mentionMap, entry == null ? null : entry.getElseDreamerNm());
        return new ArrayList<>(mentionMap.values());
    }

    /**
     * Collect direct name candidates from ordinary entry text.
     */
    private void collectMentionsFromText(final Map<String, ExtractedPersonMention> mentionMap, final String text) {
        if (StringUtils.isBlank(text)) return;

        collectByPattern(mentionMap, text, HONORIFIC_NAME_PATTERN, JournalEntityMentionType.HONORIFIC, 0.98D);
        collectByPattern(mentionMap, text, PARTICLE_NAME_PATTERN, JournalEntityMentionType.DIRECT, 0.82D);
    }

    /**
     * Collect the explicit dreamer field as a direct person reference.
     */
    private void collectDreamerMention(final Map<String, ExtractedPersonMention> mentionMap, final String dreamerName) {
        final String surfaceText = StringUtils.trimToEmpty(dreamerName);
        final String canonicalLabel = canonicalizePersonLabel(surfaceText);
        if (!isValidPersonCandidate(canonicalLabel)) return;

        final String dedupeKey = canonicalLabel + "|FIELD";
        mentionMap.putIfAbsent(
                dedupeKey,
                new ExtractedPersonMention(
                        surfaceText,
                        canonicalLabel,
                        JournalEntityMentionType.DIRECT,
                        "dreamer:" + surfaceText,
                        surfaceText,
                        1.0D
                )
        );
    }

    /**
     * Collect mention candidates matched by one regex pattern.
     */
    private void collectByPattern(
            final Map<String, ExtractedPersonMention> mentionMap,
            final String text,
            final Pattern pattern,
            final JournalEntityMentionType mentionType,
            final Double confidence
    ) {
        final Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            final String surfaceText = StringUtils.trimToEmpty(matcher.group());
            final String rawName = StringUtils.trimToEmpty(matcher.group(1));
            final String canonicalLabel = canonicalizePersonLabel(rawName);
            if (!isValidPersonCandidate(canonicalLabel)) continue;

            final String dedupeKey = canonicalLabel + "|" + mentionType.name();
            mentionMap.putIfAbsent(
                    dedupeKey,
                    new ExtractedPersonMention(
                            surfaceText,
                            canonicalLabel,
                            mentionType,
                            buildEvidenceSnippet(text, matcher.start(), matcher.end()),
                            buildRoleContextSnippet(text, matcher.start(), matcher.end()),
                            confidence
                    )
            );
        }
    }

    /**
     * Remove role rows linked to the given ref rows before the refs themselves are deleted.
     */
    private void deleteRolesByRefs(final List<JournalEntryEntityRefEntity> refEntityList) {
        if (refEntityList == null || refEntityList.isEmpty()) return;

        final List<Integer> refIdList = refEntityList.stream()
                .map(JournalEntryEntityRefEntity::getId)
                .filter(id -> id != null)
                .collect(java.util.stream.Collectors.toList());
        if (refIdList.isEmpty()) return;

        final List<JournalEntryEntityRoleEntity> existingRoleList =
                journalEntryEntityRoleRepository.findAllByJournalEntryEntityRefIdIn(refIdList);
        if (existingRoleList.isEmpty()) return;

        journalEntryEntityRoleRepository.deleteAll(existingRoleList);
        log.info("Journal entry entity role removed with refs. refCount={}, removedRoles={}", refIdList.size(), existingRoleList.size());
    }

    /**
     * Save first-phase role rows derived from one mention context.
     */
    private void saveRoleRows(final JournalEntryEntityRefEntity savedRefEntity, final ExtractedPersonMention mention) {
        if (savedRefEntity == null || savedRefEntity.getId() == null || mention == null) return;

        final List<ExtractedEntityRole> roleList = extractEntityRoles(mention);
        for (final ExtractedEntityRole role : roleList) {
            final JournalEntryEntityRoleEntity roleEntity = JournalEntryEntityRoleEntity.builder()
                    .journalEntryEntityRefId(savedRefEntity.getId())
                    .roleType(role.roleType())
                    .evidenceSnippet(role.evidenceSnippet())
                    .confidence(role.confidence())
                    .build();
            journalEntryEntityRoleRepository.save(roleEntity);
        }
    }

    /**
     * Extract a narrow set of first-phase roles from one mention context.
     *
     * <p>규칙 본문은 {@link JournalEntityRoleExtractor}에 위임한다.
     * UNKNOWN만 남을 때는 mention evidence를 근거로 보강한다.</p>
     */
    private List<ExtractedEntityRole> extractEntityRoles(final ExtractedPersonMention mention) {
        final List<JournalEntityRoleExtractor.ExtractedRole> extracted =
                JournalEntityRoleExtractor.extract(mention == null ? null : mention.roleContextSnippet());
        final List<ExtractedEntityRole> roles = new ArrayList<>(extracted.size());
        for (final JournalEntityRoleExtractor.ExtractedRole role : extracted) {
            String evidence = role.evidenceSnippet();
            if (role.roleType() == JournalEntityRoleType.UNKNOWN && mention != null) {
                evidence = StringUtils.defaultIfBlank(mention.evidenceSnippet(), mention.roleContextSnippet());
            }
            roles.add(new ExtractedEntityRole(role.roleType(), evidence, role.confidence()));
        }
        return roles;
    }

    /**
     * Normalize a person label for canonical/entity lookup use.
     */
    private String canonicalizePersonLabel(final String label) {
        String canonical = StringUtils.trimToEmpty(label);
        canonical = canonical.replaceAll("(\uB2D8|\uC528|\uAD70|\uC591)$", "");
        canonical = canonical.replaceAll("(\uC740|\uB294|\uC774|\uAC00|\uC744|\uB97C|\uACFC|\uC640|\uB3C4|\uC5D0\uAC8C|\uD55C\uD14C|\uAED8)$", "");
        canonical = canonical.replaceAll("\\s+", "");
        return canonical;
    }

    /**
     * Normalize a canonical person label for entity lookup use.
     */
    private String normalizePersonLabel(final String label) {
        return StringUtils.lowerCase(canonicalizePersonLabel(label));
    }

    /**
     * Decide whether one normalized token is valid enough for the first-phase person catalog.
     */
    private boolean isValidPersonCandidate(final String canonicalLabel) {
        if (StringUtils.length(canonicalLabel) < 2 || StringUtils.length(canonicalLabel) > 20) return false;
        if (PERSON_STOPWORDS.contains(normalizePersonLabel(canonicalLabel))) return false;
        return canonicalLabel.matches("[\\p{IsHangul}A-Za-z]+");
    }

    /**
     * Build a compact evidence snippet around one regex hit.
     */
    private String buildEvidenceSnippet(final String text, final int start, final int end) {
        if (StringUtils.isBlank(text)) return null;
        final int snippetStart = Math.max(0, start - SNIPPET_RADIUS);
        final int snippetEnd = Math.min(text.length(), end + SNIPPET_RADIUS);
        return StringUtils.normalizeSpace(text.substring(snippetStart, snippetEnd));
    }

    /**
     * Build a slightly wider context snippet for role/function inference.
     */
    private String buildRoleContextSnippet(final String text, final int start, final int end) {
        if (StringUtils.isBlank(text)) return null;
        final int snippetStart = Math.max(0, start - ROLE_CONTEXT_RADIUS);
        final int snippetEnd = Math.min(text.length(), end + ROLE_CONTEXT_RADIUS);
        return StringUtils.normalizeSpace(text.substring(snippetStart, snippetEnd));
    }

    /**
     * Extracted first-phase person mention.
     */
    private record ExtractedPersonMention(
            String surfaceText,
            String canonicalLabel,
            JournalEntityMentionType mentionType,
            String evidenceSnippet,
            String roleContextSnippet,
            Double confidence
    ) {}

    /**
     * Extracted first-phase entity role judgment.
     */
    private record ExtractedEntityRole(
            JournalEntityRoleType roleType,
            String evidenceSnippet,
            Double confidence
    ) {}
}
