/**
 * useSafeModalClose 컴포저블 단위 테스트.
 * 2-스텝 닫기 계약: 1회 요청 시 arm 만 하고, resetMs 내 재요청 시에만 onClose 를 실행한다.
 * setTimeout 의존 로직은 fake timer 로 검증한다.
 * 컴포넌트 인스턴스 밖 호출이라 Vue 의 onBeforeUnmount 경고가 발생하지만 no-op 이므로
 * 이 스위트에 한해 console.warn 을 무음 처리한다.
 */
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { useSafeModalClose } from "./safeModalClose";

describe("useSafeModalClose", () => {
  beforeEach(() => {
    vi.useFakeTimers();
    vi.spyOn(console, "warn").mockImplementation(() => undefined);
  });
  afterEach(() => {
    vi.useRealTimers();
    vi.restoreAllMocks();
  });

  it("첫 요청은 arm 만 하고 onClose 를 호출하지 않는다", () => {
    const onClose = vi.fn();
    const { closeArmed, requestSafeClose } = useSafeModalClose(onClose);

    requestSafeClose();

    expect(closeArmed.value).toBe(true);
    expect(onClose).not.toHaveBeenCalled();
  });

  it("arm 상태에서 재요청하면 onClose 를 호출하고 arm 을 해제한다", () => {
    const onClose = vi.fn();
    const { closeArmed, requestSafeClose } = useSafeModalClose(onClose);

    requestSafeClose();
    requestSafeClose();

    expect(onClose).toHaveBeenCalledTimes(1);
    expect(closeArmed.value).toBe(false);
  });

  it("resetMs(기본 2000ms) 경과 시 arm 이 자동 해제되어 다음 요청은 다시 arm 만 한다", () => {
    const onClose = vi.fn();
    const { closeArmed, requestSafeClose } = useSafeModalClose(onClose);

    requestSafeClose();
    vi.advanceTimersByTime(2000);
    expect(closeArmed.value).toBe(false);

    requestSafeClose();
    expect(onClose).not.toHaveBeenCalled();
    expect(closeArmed.value).toBe(true);
  });

  it("resetMs 직전까지는 arm 이 유지된다", () => {
    const onClose = vi.fn();
    const { closeArmed, requestSafeClose } = useSafeModalClose(onClose);

    requestSafeClose();
    vi.advanceTimersByTime(1999);

    expect(closeArmed.value).toBe(true);
  });

  it("커스텀 resetMs 를 적용한다", () => {
    const onClose = vi.fn();
    const { closeArmed, requestSafeClose } = useSafeModalClose(onClose, 500);

    requestSafeClose();
    vi.advanceTimersByTime(500);

    expect(closeArmed.value).toBe(false);
    expect(onClose).not.toHaveBeenCalled();
  });

  it("resetSafeClose 수동 호출 시 arm 과 타이머를 함께 해제한다", () => {
    const onClose = vi.fn();
    const { closeArmed, requestSafeClose, resetSafeClose } = useSafeModalClose(onClose);

    requestSafeClose();
    resetSafeClose();
    expect(closeArmed.value).toBe(false);

    // 해제 후 타이머 진행이 상태에 영향을 주지 않아야 한다
    vi.advanceTimersByTime(5000);
    requestSafeClose();
    expect(closeArmed.value).toBe(true);
    expect(onClose).not.toHaveBeenCalled();
  });

  it("확정(2회 요청) 후 잔여 타이머가 남지 않는다", () => {
    const onClose = vi.fn();
    const { closeArmed, requestSafeClose } = useSafeModalClose(onClose);

    requestSafeClose();
    requestSafeClose();
    vi.advanceTimersByTime(5000);

    expect(onClose).toHaveBeenCalledTimes(1);
    expect(closeArmed.value).toBe(false);
  });
});
