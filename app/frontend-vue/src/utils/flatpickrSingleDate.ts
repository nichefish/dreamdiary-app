import flatpickr from "flatpickr";
import type { Instance } from "flatpickr/dist/types/instance";
import { Korean } from "flatpickr/dist/l10n/ko.js";
import "flatpickr/dist/flatpickr.min.css";

export interface SingleDatePickerOptions {
  /** YYYY-MM-DD */
  initial?: string;
  onValue: (dateStr: string) => void;
  /** 필요한 경우에만 flatpickr 캘린더 부모를 지정한다. 모달 입력은 기본 body append가 위치 계산에 안전하다. */
  appendTo?: HTMLElement;
}

/**
 * 레거시 cF.datepicker.singleDatePicker("#journalDate", "yyyy-MM-DD", initDt) 동등.
 * @returns flatpickr 인스턴스 — 모달 닫을 때 destroy 필수
 */
export function bindSingleDatePicker(
  input: HTMLInputElement,
  options: SingleDatePickerOptions
): Instance {
  return flatpickr(input, {
    locale: Korean,
    dateFormat: "Y-m-d",
    defaultDate: options.initial?.trim() || undefined,
    allowInput: true,
    disableMobile: false,
    appendTo: options.appendTo,
    onChange: (_selectedDates, dateStr) => {
      options.onValue(dateStr);
    },
  });
}

/** @param fp bindSingleDatePicker 반환값 */
export function destroySingleDatePicker(fp: Instance | null | undefined): void {
  fp?.destroy();
}
