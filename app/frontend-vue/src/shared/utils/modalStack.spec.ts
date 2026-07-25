import { describe, expect, it } from "vitest";
import { MODAL_BASE_Z, MODAL_MAX_Z, SWAL_Z } from "./overlayZIndex";
import { resolveStackedModalZ } from "./modalStack";

describe("중첩 모달 z-index 스택", () => {
  it("첫 모달은 base, 두 번째부터 step 만큼 올린다", () => {
    expect(resolveStackedModalZ(0)).toBe(MODAL_BASE_Z);
    expect(resolveStackedModalZ(1)).toBe(MODAL_BASE_Z + 2);
    expect(resolveStackedModalZ(2)).toBe(MODAL_BASE_Z + 4);
  });

  it("스택이 깊어도 SweetAlert z-index 아래로 캡한다", () => {
    expect(resolveStackedModalZ(100)).toBe(MODAL_MAX_Z);
    expect(resolveStackedModalZ(100)).toBeLessThan(SWAL_Z);
  });
});
