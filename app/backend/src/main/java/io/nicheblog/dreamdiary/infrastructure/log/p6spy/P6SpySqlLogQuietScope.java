package io.nicheblog.dreamdiary.infrastructure.log.p6spy;

/**
 * 현재 스레드의 p6spy statement SQL을 DEBUG로 내릴지 표시한다.
 *
 * <p>요청 스레드 SQL은 INFO를 유지하고, {@code JOURNAL_ENTRY_EMBEDDING_SYNC} 전수 순회처럼
 * 가치가 낮은 대량 statement만 DEBUG로 내린다. p6spy 로거 레벨은 바꾸지 않는다.</p>
 *
 * @author nichefish
 */
public final class P6SpySqlLogQuietScope {

    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    private P6SpySqlLogQuietScope() {
        // utility
    }

    /**
     * 현재 스레드가 quiet 구간이면 {@code true}를 반환한다.
     *
     * @return quiet 구간 여부
     */
    public static boolean isQuiet() {
        return DEPTH.get() > 0;
    }

    /**
     * 전달한 작업을 quiet 구간에서 실행하고, 종료 시 표시를 해제한다.
     *
     * @param action quiet 구간에서 실행할 작업
     */
    public static void run(final Runnable action) {
        if (action == null) {
            return;
        }
        DEPTH.set(DEPTH.get() + 1);
        try {
            action.run();
        } finally {
            final int next = DEPTH.get() - 1;
            if (next <= 0) {
                DEPTH.remove();
            } else {
                DEPTH.set(next);
            }
        }
    }
}
