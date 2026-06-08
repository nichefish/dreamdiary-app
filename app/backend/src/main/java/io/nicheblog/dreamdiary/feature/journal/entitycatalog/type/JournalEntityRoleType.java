package io.nicheblog.dreamdiary.feature.journal.entitycatalog.type;

/**
 * First-phase role/function axes inferred from one entity mention context.
 *
 * <p>The role list intentionally stays narrow so early extraction noise remains
 * reviewable before the system grows into richer relationship semantics.</p>
 */
public enum JournalEntityRoleType {
    COLLABORATION,
    TENSION,
    EVALUATION,
    CARE,
    CONFLICT,
    DESIRE,
    SYMBOLIC_FIGURE,
    UNKNOWN
}
