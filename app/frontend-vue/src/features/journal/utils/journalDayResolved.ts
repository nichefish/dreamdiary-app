import {
  inject,
  provide,
  computed,
  type ComputedRef,
  type InjectionKey,
  type MaybeRefOrGetter,
  toValue,
} from "vue";

export interface JournalDayResolvedAxis {
  diaryWritable: boolean;
  dreamWritable: boolean;
}

export const JOURNAL_DAY_RESOLVED_KEY: InjectionKey<ComputedRef<JournalDayResolvedAxis>> =
  Symbol("journalDayResolved");

type DayResolvedSource =
  | {
      diaryResolvedYn?: string;
      dreamResolvedYn?: string;
    }
  | null
  | undefined;

export function isResolvedYn(yn?: string | null): boolean {
  return String(yn ?? "").toUpperCase() === "Y";
}

/**
 * 일자 diaryResolvedYn / dreamResolvedYn 에서 축별 쓰기 가능 여부를 provide 한다.
 * day 는 ComputedRef 또는 getter 로 넘기면 일자 데이터 갱신에 반응한다.
 */
export function provideJournalDayResolved(
  day: MaybeRefOrGetter<DayResolvedSource>,
): ComputedRef<JournalDayResolvedAxis> {
  const axis = computed<JournalDayResolvedAxis>(() => {
    const d = toValue(day) as DayResolvedSource;
    return {
      diaryWritable: !isResolvedYn(d?.diaryResolvedYn),
      dreamWritable: !isResolvedYn(d?.dreamResolvedYn),
    };
  });
  provide(JOURNAL_DAY_RESOLVED_KEY, axis);
  return axis;
}


/**
 * parent provide 축과 엔트리 DTO 투영 플래그를 병합한다.
 * provide 가 없는 화면(검색·상세·뷰 모달 등)에서는 엔트리 {@link JournalEntryDto} 의
 * diaryResolvedYn / dreamResolvedYn 이 해당 축 쓰기 잠금 SSOT 이다.
 * 엔트리 플래그가 없으면 엔트리 쪽에서는 잠금을 적용하지 않는다.
 */
export function mergeDayResolvedAxis(
  parent: JournalDayResolvedAxis,
  entry?: { diaryResolvedYn?: string; dreamResolvedYn?: string } | null,
): JournalDayResolvedAxis {
  return {
    diaryWritable: parent.diaryWritable && !isResolvedYn(entry?.diaryResolvedYn),
    dreamWritable: parent.dreamWritable && !isResolvedYn(entry?.dreamResolvedYn),
  };
}
/**
 * 일자 provide 축을 inject 한다. 상위 provide 가 없으면 전 축 writable 기본값.
 * 검색·뷰 모달 등 provide 없는 화면에서는 {@link mergeDayResolvedAxis} 로
 * 엔트리 DTO 투영 플래그를 병합해 SSOT 를 맞춘다.
 */
export function useJournalDayResolved(): ComputedRef<JournalDayResolvedAxis> {
  return inject(
    JOURNAL_DAY_RESOLVED_KEY,
    computed(() => ({ diaryWritable: true, dreamWritable: true })),
  );
}

export function useAxisWritable(isDream: boolean): ComputedRef<boolean> {
  const axis = useJournalDayResolved();
  return computed(() => (isDream ? axis.value.dreamWritable : axis.value.diaryWritable));
}
