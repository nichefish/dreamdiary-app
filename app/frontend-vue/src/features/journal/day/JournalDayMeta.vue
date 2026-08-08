<template>
  <!--begin::저널 메타 페이지-->
  <div class="journal-day-meta-page">
    <JournalDayViewToolbar />

    <!--begin::카드-->
    <div class="card post" style="margin-top: 0 !important;">
      <!--begin::카드 헤더-->
      <div class="card-header min-h-auto mb-7">
        <div id="journal_meta_header" class="mb-6 ms-4 w-100">
          <div class="row align-items-center mb-4 ms-4 min-h-42px">
            <div class="col-auto d-none d-md-flex text-center fs-6">
              <b>{{ t("journal.day.meta.header-label") }}</b>
            </div>
            <!--begin::메타 헤더 목록-->
            <div class="col flex-grow-1">
              <!--begin::로딩-->
              <span v-if="store.metaLoading" class="spinner-border spinner-border-sm text-primary" role="status"></span>
              <!--end::로딩-->
              <div v-else class="journal-day-meta-header-list-vue d-flex flex-wrap align-items-center">
                <template v-if="store.metaError">
                  <span class="text-danger fs-7">{{ store.metaError }}</span>
                </template>
                <template v-else-if="store.metaList.length > 0">
                  <span
                    v-for="item in store.metaList"
                    :key="'meta-h-' + item.id"
                    class="d-inline-flex align-items-center me-3"
                  >
                    <button
                      type="button"
                      class="btn btn-link py-2 px-0 cursor-pointer opacity-hover text-decoration-none d-inline-flex align-items-center"
                      :title="t('journal.day.meta.menu.tooltip')"
                      @click.stop="openMetaContextMenu($event, item)"
                    >
                      <span
                        class="text-primary d-inline-flex align-items-center"
                        :class="store.isMetaSelected(item) ? 'fw-bold' : 'fw-lighter'"
                      >
                        <span>#</span>
                        <span v-if="item.ctgr" class="fs-7 text-noti">[{{ item.ctgr }}]</span>
                        <span>{{ item.name }}</span>
                      </span>
                      <span class="fs-9 text-noti fw-normal tag-count">{{ item.contentSize ?? 0 }}</span>
                    </button>
                    <i
                      v-if="store.isMetaSelected(item)"
                      class="bi bi-x-circle-fill text-primary cursor-pointer opacity-75 ms-1"
                      :title="t('journal.day.meta.remove-from-graph.tooltip').replace('{0}', item.name ?? '')"
                      @click.stop="removeMetaFromGraph(item)"
                    ></i>
                  </span>
                </template>
                <template v-else>-</template>
              </div>
            </div>
            <!--end::메타 헤더 목록-->
            <div class="col-auto d-none d-md-flex ms-4 pe-0 border-2 border-gray-300 border-end h-75 w-10px">&nbsp;</div>
            <div class="col-auto d-none d-md-flex ms-4 me-20 text-center fs-6 gap-3">&nbsp;</div>
          </div>
        </div>
      </div>
      <!--end::카드 헤더-->

      <!--begin::카드 바디-->
      <div class="card-body">
        <!--begin::그래프 영역-->
        <div id="journal_day_meta_graph_div" class="border border-1 border-primary" style="min-height: 200px;">

          <!--begin::메타 미선택-->
          <div v-if="store.selectedMetas.length === 0" class="d-flex justify-content-center align-items-center text-muted py-10">
            {{ t("journal.day.meta.selection.empty-guide") }}
          </div>
          <!--end::메타 미선택-->

          <template v-else>
            <!--begin::그래프 컨트롤바-->
            <div class="d-flex align-items-center gap-3 px-4 pt-3 flex-wrap">

              <!--begin::연도 선택-->
              <div class="d-flex align-items-center gap-2">
                <label class="fw-bold fs-7 mb-0">{{ t("common.year") }}</label>
                <select class="form-select form-select-sm w-auto" :value="graphYy" @change="onYyChange">
                  <option :value="GRAPH_YY_ALL">{{ t("journal.day.meta.graph.all-years") }}</option>
                  <option v-for="y in yyList" :key="y" :value="y">{{ y }}</option>
                </select>
              </div>
              <!--end::연도 선택-->

              <!--begin::임계값 입력-->
              <div class="d-flex align-items-center gap-2">
                <label class="fw-bold fs-7 mb-0">{{ t("journal.day.meta.graph.threshold") }}</label>
                <input
                  type="number"
                  class="form-control form-control-sm"
                  style="width: 90px;"
                  :value="threshold ?? ''"
                  :placeholder="t('journal.day.meta.graph.none')"
                  @change="onThresholdChange"
                />
              </div>
              <!--end::임계값 입력-->

              <div v-if="store.selectedMetas.length === 1" class="text-muted fs-8 ms-1">
                {{ t("journal.day.meta.graph.compare-guide") }}
              </div>

            </div>
            <!--end::그래프 컨트롤바-->

            <!--begin::메타별 통계 요약-->
            <div
              v-for="selMeta in store.selectedMetas"
              :key="'meta-stats-' + selMeta.id"
              class="px-4 pb-1"
              :class="{ 'border-top border-gray-200 pt-2 mt-1': store.selectedMetas.length > 1 }"
            >
              <div class="text-muted fs-7 mb-1">
                #
                <span class="text-dark fw-bold">
                  <span v-if="selMeta.ctgr" class="fs-8 text-noti">[{{ selMeta.ctgr }}]</span>
                  {{ selMeta.name }}
                </span>
                <span v-if="selMeta.unit" class="text-muted fs-8 ms-1">({{ selMeta.unit }})</span>
              </div>
              <div
                v-if="getStats(selMeta.id)"
                class="d-flex align-items-center gap-3 flex-wrap fs-7"
              >
                <span><span class="text-muted">{{ t("journal.day.meta.stats.sum") }}</span> <b>{{ getStats(selMeta.id)!.sum }}{{ selMeta.unit ?? "" }}</b></span>
                <span><span class="text-muted">{{ t("journal.day.meta.stats.avg") }}</span> <b>{{ getStats(selMeta.id)!.avg }}{{ selMeta.unit ?? "" }}</b></span>
                <span class="text-success">
                  <span class="text-muted">{{ t("journal.day.meta.stats.max") }}</span> <b>{{ getStats(selMeta.id)!.max }}{{ selMeta.unit ?? "" }}</b>
                  <span class="text-muted fs-8 ms-1">{{ getStats(selMeta.id)!.maxDt }}</span>
                </span>
                <span class="text-danger">
                  <span class="text-muted">{{ t("journal.day.meta.stats.min") }}</span> <b>{{ getStats(selMeta.id)!.min }}{{ selMeta.unit ?? "" }}</b>
                  <span class="text-muted fs-8 ms-1">{{ getStats(selMeta.id)!.minDt }}</span>
                </span>
              </div>
            </div>
            <!--end::메타별 통계 요약-->

            <!--begin::그래프 로딩-->
            <div v-if="anyGraphLoading" class="d-flex justify-content-center py-10">
              <span class="spinner-border text-primary" role="status"></span>
            </div>
            <!--end::그래프 로딩-->

            <!--begin::비교 라인 차트 (선택 메타 전체를 한 차트에)-->
            <apexchart
              v-else-if="hasChartData"
              type="line"
              height="300"
              :options="combinedChartOptions"
              :series="combinedChartSeries"
              class="px-2 pb-2"
            />
            <!--end::비교 라인 차트-->

            <div v-else class="d-flex justify-content-center text-muted py-8">
              {{ t("journal.day.meta.graph.empty") }}
            </div>

          </template>

        </div>
        <!--end::그래프 영역-->
      </div>
      <!--end::카드 바디-->
    </div>
    <!--end::카드-->

  </div>
  <!--end::저널 메타 페이지-->
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted } from "vue";
import axios from "axios";
import { useJournalStore } from "@/features/journal/stores/journal";
import { useMetaContextMenuStore } from "@/features/journal/stores/metaContextMenu";
import JournalDayViewToolbar from "./components/JournalDayViewToolbar.vue";
import type { JournalDayDto, MetaContentItem, MetaDto } from "@/features/journal/stores/journal";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const store = useJournalStore();
const metaContextMenuStore = useMetaContextMenuStore();
const { t } = useLocaleStore();

type GraphPoint = { dt: string; value: number };
type GraphStats = { sum: number; avg: number; max: number; min: number; maxDt: string; minDt: string };
type MetaGraphBundle = { points: GraphPoint[]; loading: boolean };

/** 그래프 공통 상태 */
const graphYy = ref<string>(String(new Date().getFullYear()));
const yyList = ref<string[]>([]);
const threshold = ref<number | null>(null);
/** metaId 별 그래프 데이터 */
const graphByMetaId = ref<Record<string, MetaGraphBundle>>({});

onMounted(() => {
  void store.fetchMetas();
});

/** 메타 헤더 클릭 시 컨텍스트 메뉴를 연다. */
function openMetaContextMenu(event: MouseEvent, item: MetaDto) {
  if (item.id == null) return;
  metaContextMenuStore.open(event, {
    metaId: item.id,
    name: item.name ?? "",
    ctgr: item.ctgr ?? "",
    unit: item.unit,
    contentSize: item.contentSize,
  });
}

/** 그래프 선택에서 메타를 제거한다. */
function removeMetaFromGraph(item: MetaDto) {
  if (item.id == null) return;
  store.removeMetaFromGraph(item.id);
}

function metaKey(metaId: number | string | undefined): string {
  return String(metaId ?? "");
}

function setGraphBundle(metaId: number | string, bundle: MetaGraphBundle) {
  const key = metaKey(metaId);
  graphByMetaId.value = { ...graphByMetaId.value, [key]: bundle };
}

function isGraphLoading(metaId: number | string | undefined): boolean {
  return graphByMetaId.value[metaKey(metaId)]?.loading ?? false;
}

function getPoints(metaId: number | string | undefined): GraphPoint[] {
  return graphByMetaId.value[metaKey(metaId)]?.points ?? [];
}

/** 연도 미지정(전체) — yy 파라미터 생략 시 백엔드가 metaId 기준 전 기간 조회 */
const GRAPH_YY_ALL = "";

/** metaId + yy 기준으로 일자별 메타 수치를 조회한다. yy 가 빈 문자열이면 전체 연도. */
async function fetchGraphData(metaId: number | string, yy: string) {
  setGraphBundle(metaId, { points: [], loading: true });
  try {
    const params: Record<string, unknown> = { viewType: "SEARCH", metaId };
    if (yy) params.yy = yy;
    const res = await axios.get<{ rsltList?: JournalDayDto[] }>("/api/journal/days", { params });
    const list: JournalDayDto[] = res.data?.rsltList ?? [];
    const points: GraphPoint[] = [];
    for (const day of list) {
      const metaRows: MetaContentItem[] = day?.meta?.list ?? [];
      const row = metaRows.find((r) => String(r.metaId ?? "") === String(metaId));
      if (row?.value == null) continue;
      const num = parseFloat(row.value);
      if (isNaN(num)) continue;
      points.push({ dt: day.stdrdDt ?? day.journalDate ?? "", value: num });
    }
    /* 날짜 오름차순 정렬 */
    points.sort((a, b) => a.dt.localeCompare(b.dt));
    setGraphBundle(metaId, { points, loading: false });
  } catch (e: unknown) {
    console.error("[JournalDayMeta] fetchGraphData failed", { metaId, yy }, e);
    setGraphBundle(metaId, { points: [], loading: false });
  }
}

/** 메타별 연도 목록 조회 */
async function fetchMetaYears(metaId: number | string): Promise<string[]> {
  try {
    const res = await axios.get<{ rsltList?: unknown[] }>(`/api/journal/day/metas/${metaId}/years`);
    return (res.data?.rsltList ?? []).map(String);
  } catch (e: unknown) {
    console.error("[JournalDayMeta] fetchMetaYears failed", { metaId }, e);
    return [];
  }
}

/** 선택 메타들의 연도 목록 합집합을 갱신하고 graphYy 를 맞춘다. */
async function syncYearListAndReload() {
  const metas = store.selectedMetas;
  if (metas.length === 0) {
    graphByMetaId.value = {};
    yyList.value = [];
    return;
  }
  const yearLists = await Promise.all(
    metas.filter((m) => m.id != null).map((m) => fetchMetaYears(m.id!))
  );
  const union = [...new Set(yearLists.flat())].sort((a, b) => b.localeCompare(a));
  yyList.value = union;
  const currentYy = String(new Date().getFullYear());
  if (graphYy.value !== GRAPH_YY_ALL && !union.includes(graphYy.value)) {
    graphYy.value = union.includes(currentYy) ? currentYy : (union[0] ?? currentYy);
  }
  await Promise.all(
    metas.filter((m) => m.id != null).map((m) => fetchGraphData(m.id!, graphYy.value))
  );
}

watch(
  () => store.selectedMetas.map((m) => m.id).join(","),
  () => {
    void syncYearListAndReload();
  }
);

/** 연도 변경 처리 */
async function onYyChange(event: Event) {
  const target = event.target as HTMLSelectElement | null;
  if (!target) return;
  graphYy.value = target.value;
  await Promise.all(
    store.selectedMetas
      .filter((m) => m.id != null)
      .map((m) => fetchGraphData(m.id!, target.value))
  );
}

/** 임계값 변경 처리 */
function onThresholdChange(event: Event) {
  const target = event.target as HTMLInputElement | null;
  if (!target) return;
  const val = parseFloat(target.value);
  threshold.value = isNaN(val) ? null : val;
}

/** 통계 (합계/평균/최고/최저) */
function getStats(metaId: number | string | undefined): GraphStats | null {
  const pts = getPoints(metaId);
  if (pts.length === 0) return null;
  const values = pts.map((p) => p.value);
  const sum = values.reduce((a, b) => a + b, 0);
  const avg = sum / values.length;
  const maxVal = Math.max(...values);
  const minVal = Math.min(...values);
  return {
    sum: Math.round(sum * 100) / 100,
    avg: Math.round(avg * 100) / 100,
    max: maxVal,
    min: minVal,
    maxDt: pts.find((p) => p.value === maxVal)?.dt ?? "",
    minDt: pts.find((p) => p.value === minVal)?.dt ?? "",
  };
}

function metaSeriesLabel(meta: MetaDto): string {
  return meta.ctgr ? `[${meta.ctgr}] ${meta.name ?? ""}` : (meta.name ?? t("journal.day.meta.value-fallback"));
}

/** 비교 차트 X축: 선택된 모든 메타의 일자 합집합(오름차순) */
const chartCategories = computed(() => {
  const dates = new Set<string>();
  for (const meta of store.selectedMetas) {
    for (const p of getPoints(meta.id)) {
      if (p.dt) dates.add(p.dt);
    }
  }
  return [...dates].sort((a, b) => a.localeCompare(b));
});

const anyGraphLoading = computed(() =>
  store.selectedMetas.some((m) => isGraphLoading(m.id))
);

/** ApexCharts 시리즈 — 메타마다 한 줄, 동일 X축(categories)에 맞춤 */
const combinedChartSeries = computed(() => {
  const cats = chartCategories.value;
  return store.selectedMetas
    .map((meta) => {
      const byDt = new Map(getPoints(meta.id).map((p) => [p.dt, p.value]));
      return {
        name: metaSeriesLabel(meta),
        data: cats.map((dt) => byDt.get(dt) ?? null),
      };
    })
    .filter((s) => s.data.some((v) => v !== null));
});

const hasChartData = computed(() => combinedChartSeries.value.length > 0);

/** Y축 단위 라벨 — 단위가 하나면 붙이고, 여러 개면 범례·툴팁에서 구분 */
const yAxisUnitSuffix = computed(() => {
  const units = [...new Set(store.selectedMetas.map((m) => m.unit ?? "").filter(Boolean))];
  if (units.length === 1) return units[0];
  return "";
});

/** ApexCharts 옵션 — 선택 메타 전체를 한 차트에 */
const combinedChartOptions = computed(() => {
  const yaxis: ApexAnnotations["yaxis"] = [];

  if (threshold.value !== null) {
    const thrUnit = yAxisUnitSuffix.value;
    yaxis.push({
      y: threshold.value,
      borderColor: "#f1416c",
      strokeDashArray: 4,
      label: {
        text: `${t("journal.day.meta.graph.threshold-short")} ${threshold.value}${thrUnit}`,
        style: { color: "#fff", background: "#f1416c" },
      },
    });
  }

  /* 단일 메타일 때만 최고/최저 보조선 (비교 시 혼잡 방지) */
  if (store.selectedMetas.length === 1) {
    const meta = store.selectedMetas[0];
    const unit = meta.unit ?? "";
    const stats = getStats(meta.id);
    if (stats) {
      yaxis.push({
        y: stats.max,
        borderColor: "#50cd89",
        strokeDashArray: 2,
        label: { text: `${t("journal.day.meta.stats.max")} ${stats.max}${unit}`, position: "right", style: { color: "#fff", background: "#50cd89" } },
      });
      yaxis.push({
        y: stats.min,
        borderColor: "#f1416c",
        strokeDashArray: 2,
        label: { text: `${t("journal.day.meta.stats.min")} ${stats.min}${unit}`, position: "right", style: { color: "#fff", background: "#f1416c" } },
      });
    }
  }

  return {
    chart: { type: "line", toolbar: { show: false }, zoom: { enabled: false } },
    colors: ["#0b63ce", "#50cd89"],
    legend: { show: store.selectedMetas.length > 1, position: "top", horizontalAlign: "right" },
    stroke: { curve: "smooth", width: 2 },
    markers: { size: 4 },
    xaxis: {
      categories: chartCategories.value,
      labels: { rotate: -45, style: { fontSize: "11px" } },
    },
    yaxis: {
      labels: {
        formatter: (v: number) => (yAxisUnitSuffix.value ? `${v}${yAxisUnitSuffix.value}` : String(v)),
      },
    },
    tooltip: {
      shared: true,
      intersect: false,
      y: {
        formatter: (v: number, opts?: { seriesIndex?: number }) => {
          if (v == null) return "-";
          const meta = store.selectedMetas[opts?.seriesIndex ?? 0];
          const unit = meta?.unit ?? "";
          return unit ? `${v}${unit}` : String(v);
        },
      },
    },
    annotations: { yaxis },
    grid: { borderColor: "#e0e0e0" },
  };
});
</script>
