package io.nicheblog.dreamdiary.global.util;

import lombok.experimental.UtilityClass;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * TransactionHookUtils
 * <pre>
 *  트랜잭션 상태에 따라 실행 시점을 제어하는 유틸리티.
 *  트랜잭션이 활성화된 경우 → commit 이후(afterCommit)에 콜백 실행
 *  트랜잭션이 없는 경우 → 즉시 실행
 *  주로 다음과 같은 상황에서 사용한다: DB commit 이후에만 안전하게 실행되어야 하는 후속 처리. (예: 이벤트 발행, 외부 시스템 호출, 캐시 반영 등)
 *  트랜잭션 롤백 시에는 afterCommit이 호출되지 않으므로, side-effect를 트랜잭션 성공 이후로 지연시키는 목적을 가진다.
 * </pre>
 *
 * @author nichefish
 */
@UtilityClass
public class TransactionHookUtils {

    /**
     * Checked Exception을 던질 수 있는 Runnable 인터페이스.
     *
     * 일반 Runnable은 checked exception을 허용하지 않기 때문에,
     * 트랜잭션 이후 실행 로직에서 예외 처리가 필요한 경우를 위해 정의됨.
     */
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    /**
     * 트랜잭션 상태에 따라 작업 실행 시점을 제어한다.
     * 트랜잭션이 활성화된 경우 → commit 이후(afterCommit)에 실행
     * 트랜잭션이 없는 경우 → 즉시 실행
     *
     * @param action 실행할 작업 (null 불가)
 */
    public static void runAfterCommitOrNow(final Runnable action) {
        Objects.requireNonNull(action, "action");

        if (isTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }

        action.run();
    }

    /**
     * 트랜잭션 상태에 따라 작업 실행 시점을 제어한다.
     * 트랜잭션이 활성화된 경우 → commit 이후(afterCommit)에 실행 → 실행 중 발생한 예외는 asyncExceptionHandler로 전달됨
     * 트랜잭션이 없는 경우 → 즉시 실행  → 예외는 호출자에게 그대로 전달됨
     *
     * @param action 실행할 작업 (checked exception 허용)
     * @param asyncExceptionHandler 비동기 실행 시 예외 처리 핸들러 (null 불가)
     * @throws Exception 트랜잭션이 없는 경우 action 실행 중 발생한 예외
     */
    public static void runAfterCommitOrNow(final ThrowingRunnable action, final Consumer<Exception> asyncExceptionHandler) throws Exception {
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(asyncExceptionHandler, "asyncExceptionHandler");

        if (isTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        action.run();
                    } catch (final Exception e) {
                        asyncExceptionHandler.accept(e);
                    }
                }
            });
            return;
        }

        action.run();
    }

    /**
     * 현재 스레드에 실제 트랜잭션이 활성화되어 있는지 확인한다.
     * - synchronizationActive: 트랜잭션 동기화가 활성화되어 있는지 여부
     * - actualTransactionActive: 실제 물리적 트랜잭션이 존재하는지 여부
     * 두 조건을 모두 만족해야 "진짜 트랜잭션 안"으로 판단한다.
     *
     * @return 트랜잭션 활성 여부
     */
    private static boolean isTransactionActive() {
        return TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive();
    }
}
