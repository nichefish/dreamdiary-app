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
              <b>메타 : </b>
            </div>
            <!--begin::메타 헤더 목록-->
            <div class="col flex-grow-1">
              <!--begin::로딩-->
              <span v-if="store.metaLoading" class="spinner-border spinner-border-sm text-primary" role="status"></span>
              <!--end::로딩-->
              <div v-else class="journal-day-meta-header-list-vue">
                <template v-if="store.metaList.length > 0">
                  <span
                    v-for="item in store.metaList"
                    :key="'meta-h-' + item.id"
                    class="text-muted cursor-pointer pe-1"
                    @click="store.selectMeta(item)"
                  >
                    #
                    <span class="border-bottom text-primary fw-lighter opacity-hover">
                      <span v-if="item.ctgr" class="fs-7 text-noti">[{{ item.ctgr }}]</span>
                      {{ item.name }}
                    </span>
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
        <!--begin::설정 스트립-->
        <div class="d-flex-align-center mb-2 ps-2 min-h-42px">
          <div class="journal-day-meta-config-strip-vue d-flex-align-center flex-wrap gap-2">
            <template v-if="store.selectedMeta">
              <span class="text-muted fs-4 pe-1">
                #
                <span class="text-dark fw-bold opacity-hover">
                  <span v-if="store.selectedMeta.ctgr" class="fs-7 text-noti">[{{ store.selectedMeta.ctgr }}]</span>
                  {{ store.selectedMeta.name }}
                </span>
              </span>
              <div class="col-1 d-none d-md-flex border-2 border-gray-300 border-end h-75 me-3 w-10px">&nbsp;</div>
              <div class="gap-1 d-flex align-items-center">
                <!--TODO: 메타 설정 모달 미구현-->
                <button
                  type="button"
                  class="btn btn-sm btn-icon btn-outline btn-bg-light btn-active-color-primary"
                  title="메타 설정"
                >
                  <i class="bi bi-gear"></i>
                </button>
                <button
                  type="button"
                  class="btn btn-sm btn-icon btn-outline btn-bg-light btn-active-color-primary"
                  title="메타 컨텐츠 목록"
                  @click="openMetaModal"
                >
                  <i class="bi bi-bar-chart"></i>
                </button>
              </div>
            </template>
          </div>
        </div>
        <!--end::설정 스트립-->

        <!--begin::그래프 영역-->
        <div id="journal_day_meta_graph_div" class="border border-1 border-primary" style="min-height: 200px;">

          <!--begin::메타 미선택-->
          <div v-if="!store.selectedMeta" class="d-flex justify-content-center align-items-center text-muted py-10">
            메타를 선택하세요.
          </div>
          <!--end::메타 미선택-->

          <template v-else>
            <!--begin::그래프 컨트롤바-->
            <div class="d-flex align-items-center gap-3 px-4 pt-3 flex-wrap">

              <!--begin::연도 선택-->
              <div class="d-flex align-items-center gap-2">
                <label class="fw-bold fs-7 mb-0">연도</label>
                <select class="form-select form-select-sm w-auto" :value="graphYy" @change="onYyChange">
                  <option v-for="y in yyList" :key="y" :value="y">{{ y }}</option>
                </select>
              </div>
              <!--end::연도 선택-->

              <!--begin::임계값 입력-->
              <div class="d-flex align-items-center gap-2">
                <label class="fw-bold fs-7 mb-0">임계값</label>
                <input
                  type="number"
                  class="form-control form-control-sm"
                  style="width: 90px;"
                  :value="threshold ?? ''"
                  placeholder="없음"
                  @change="onThresholdChange"
                />
              </div>
              <!--end::임계값 입력-->

              <!--begin::통계 요약-->
              <div v-if="stats" class="d-flex align-items-center gap-3 ms-auto fs-7">
                <span><span class="text-muted">합계</span> <b>{{ stats.sum }}{{ unit }}</b></span>
                <span><span class="text-muted">평균</span> <b>{{ stats.avg }}{{ unit }}</b></span>
                <span class="text-success">
                  <span class="text-muted">최고</span> <b>{{ stats.max }}{{ unit }}</b>
                  <span class="text-muted fs-8 ms-1">{{ stats.maxDt }}</span>
                </span>
                <span class="text-danger">
                  <span class="text-muted">최저</span> <b>{{ stats.min }}{{ unit }}</b>
                  <span class="text-muted fs-8 ms-1">{{ stats.minDt }}</span>
                </span>
              </div>
              <!--end::통계 요약-->

            </div>
            <!--end::그래프 컨트롤바-->

            <!--begin::그래프 로딩-->
            <div v-if="graphLoading" class="d-flex justify-content-center py-10">
              <span class="spinner-border text-primary" role="status"></span>
            </div>
            <!--end::그래프 로딩-->

            <!--begin::라인 차트-->
            <apexchart
              v-else-if="chartSeries.length > 0"
              type="line"
              height="280"
              :options="chartOptions"
              :series="chartSeries"
              class="px-2 pb-2"
            />
            <!--end::라인 차트-->

            <div v-else-if="!graphLoading" class="d-flex justify-content-center text-muted py-10">
              데이터가 없습니다.
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
import { useJournalStore } from "@/stores/journal";
import { useJournalModalStore } from "@/stores/journalModal";
import JournalDayViewToolbar from "./components/JournalDayViewToolbar.vue";
import type { JournalDayDto, MetaContentItem } from "@/stores/journal";

const store = useJournalStore();
const modalStore = useJournalModalStore();

/** 그래프용 상태 */
const graphYy = ref<string>(String(new Date().getFullYear()));
const yyList = ref<string[]>([]);
const graphPoints = ref<Array<{ dt: string; value: number }>>([]);
const graphLoading = ref(false);
const threshold = ref<number | null>(null);

onMounted(() => {
  void store.fetchMetas();
});

/** 선택된 메타의 컨텐츠 목록 모달을 연다. */
function openMetaModal() {
  if (!store.selectedMeta?.id) return;
  void modalStore.openMetaModal(store.selectedMeta.id, undefined, store.selectedMeta.name);
}

/** 선택된 메타의 단위 */
const unit = computed(() => store.selectedMeta?.unit ?? "");

/** metaId + yy 기준으로 일자별 메타 수치를 조회한다. */
async function fetchGraphData(metaId: number | string, yy: string) {
  graphLoading.value = true;
  graphPoints.value = [];
  try {
    const res = await axios.get<{ rsltList?: JournalDayDto[] }>("/api/journal/days", {
      params: { viewType: "SEARCH", metaId, yy },
    });
    const list: JournalDayDto[] = res.data?.rsltList ?? [];
    const points: Array<{ dt: string; value: number }> = [];
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
    graphPoints.value = points;
  } catch {
    graphPoints.value = [];
  } finally {
    graphLoading.value = false;
  }
}

/** selectedMeta 변경 시 연도 목록 조회 후 그래프 데이터를 로드한다. */
watch(
  () => store.selectedMeta,
  async (meta) => {
    if (!meta?.id) { graphPoints.value = []; return; }
    try {
      const res = await axios.get<{ rsltList?: unknown[] }>(`/api/journal/day/metas/${meta.id}/years`);
      const years: string[] = (res.data?.rsltList ?? []).map(String);
      yyList.value = years;
      const currentYy = String(new Date().getFullYear());
      graphYy.value = years.includes(currentYy) ? currentYy : (years[0] ?? currentYy);
    } catch {
      yyList.value = [];
    }
    await fetchGraphData(meta.id, graphYy.value);
  },
);

/** 연도 변경 처리 */
async function onYyChange(event: Event) {
  const target = event.target as HTMLSelectElement | null;
  if (!target || !store.selectedMeta?.id) return;
  graphYy.value = target.value;
  await fetchGraphData(store.selectedMeta.id, target.value);
}

/** 임계값 변경 처리 */
function onThresholdChange(event: Event) {
  const target = event.target as HTMLInputElement | null;
  if (!target) return;
  const val = parseFloat(target.value);
  threshold.value = isNaN(val) ? null : val;
}

/** 통계 (합계/평균/최고/최저) */
const stats = computed(() => {
  const pts = graphPoints.value;
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
});

/** ApexCharts 시리즈 */
const chartSeries = computed(() => {
  if (graphPoints.value.length === 0) return [];
  return [{ name: store.selectedMeta?.name ?? "값", data: graphPoints.value.map((p) => p.value) }];
});

/** ApexCharts 옵션 */
const chartOptions = computed(() => {
  const yaxis: ApexAnnotations["yaxis"] = [];

  /* threshold 수평 기준선 */
  if (threshold.value !== null) {
    yaxis.push({
      y: threshold.value,
      borderColor: "#f1416c",
      strokeDashArray: 4,
      label: { text: `임계 ${threshold.value}${unit.value}`, style: { color: "#fff", background: "#f1416c" } },
    });
  }

  /* 최고/최저 수평 표시선 */
  if (stats.value) {
    yaxis.push({
      y: stats.value.max,
      borderColor: "#50cd89",
      strokeDashArray: 2,
      label: { text: `최고 ${stats.value.max}${unit.value}`, position: "right", style: { color: "#fff", background: "#50cd89" } },
    });
    yaxis.push({
      y: stats.value.min,
      borderColor: "#f1416c",
      strokeDashArray: 2,
      label: { text: `최저 ${stats.value.min}${unit.value}`, position: "right", style: { color: "#fff", background: "#f1416c" } },
    });
  }

  return {
    chart: { type: "line", toolbar: { show: false }, zoom: { enabled: false } },
    stroke: { curve: "smooth", width: 2 },
    markers: { size: 4 },
    xaxis: {
      categories: graphPoints.value.map((p) => p.dt),
      labels: { rotate: -45, style: { fontSize: "11px" } },
    },
    yaxis: { labels: { formatter: (v: number) => `${v}${unit.value}` } },
    tooltip: { y: { formatter: (v: number) => `${v}${unit.value}` } },
    annotations: { yaxis },
    grid: { borderColor: "#e0e0e0" },
  };
});
</script>