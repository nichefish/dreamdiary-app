package io.nicheblog.dreamdiary.infrastructure.log.p6spy;

import com.p6spy.engine.logging.Category;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * p6spy quiet 구간의 스레드 표시와 statement DEBUG 분기 테스트.
 *
 * @author nichefish
 */
class P6SpySqlLogQuietScopeTest {

    /** quiet 구간 밖에서는 statement를 INFO로 유지한다. */
    @Test
    void statementStaysInfoOutsideQuietScope() {
        assertThat(P6SpySqlLogQuietScope.isQuiet()).isFalse();
        assertThat(P6SpySlf4JLogger.shouldLogStatementAtDebug(Category.STATEMENT)).isFalse();
    }

    /** quiet 구간에서는 statement를 DEBUG로 내린다. ERROR/WARN은 유지한다. */
    @Test
    void statementGoesDebugInsideQuietScope() {
        P6SpySqlLogQuietScope.run(() -> {
            assertThat(P6SpySqlLogQuietScope.isQuiet()).isTrue();
            assertThat(P6SpySlf4JLogger.shouldLogStatementAtDebug(Category.STATEMENT)).isTrue();
            assertThat(P6SpySlf4JLogger.shouldLogStatementAtDebug(Category.ERROR)).isFalse();
            assertThat(P6SpySlf4JLogger.shouldLogStatementAtDebug(Category.WARN)).isFalse();
        });
        assertThat(P6SpySqlLogQuietScope.isQuiet()).isFalse();
    }

    /** quiet 구간이 예외로 끝나도 표시를 해제한다. */
    @Test
    void quietScopeClearsAfterException() {
        assertThatThrownBy(() -> P6SpySqlLogQuietScope.run(() -> {
            throw new IllegalStateException("fixture-quiet-scope-failure");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(P6SpySqlLogQuietScope.isQuiet()).isFalse();
    }
}
