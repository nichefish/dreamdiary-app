<template>
  <!--begin::결산 목록-->
  <div class="journal-annual-list-vue">

    <!--begin::총 집계 카드-->
    <div class="card post mb-4">
      <div class="card-body">
        <div class="d-flex-between fs-5">
          <div class="d-flex fs-5">
            <div class="text-gray-700 d-flex-center me-5">
              <span class="fw-bold me-2">{{ t('journal.annual.total.dream-record') }}</span>
              <i class="bi bi-moon-stars fs-4 me-2"></i>
              <template v-if="store.totalLoading">
                <span class="spinner-border spinner-border-sm text-primary" role="status"></span>
              </template>
              <template v-else-if="store.totalAnnual">
                (<span class="text-info fw-bold mx-1">{{ store.totalAnnual.dreamDayCnt ?? 0 }}</span>{{ t('common.unit.day') }}
                /
                <span class="text-info fw-bold mx-1">{{ store.totalAnnual.dreamCnt ?? 0 }}</span>{{ t('common.unit.count') }})
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!--end::총 집계 카드-->

    <!--begin::로딩-->
    <div v-if="store.loading" class="d-flex justify-content-center py-10">
      <span class="spinner-border text-primary" role="status"></span>
    </div>
    <!--end::로딩-->

    <!--begin::에러-->
    <div v-else-if="store.error" class="alert alert-danger">{{ store.error }}</div>
    <!--end::에러-->

    <!--begin::빈 목록-->
    <div v-else-if="!store.annualList.length" class="text-center text-muted py-10 fs-6">
      {{ t('journal.annual.list.empty') }}
    </div>
    <!--end::빈 목록-->

    <!--begin::결산 카드 목록-->
    <template v-else>
      <div v-for="annual in store.annualList" :key="annual.yy ?? annual.id" class="card post mb-3">
        <div class="card-body py-5">
          <div class="row journal-sumry align-items-center">

            <!--begin::연도 + 제목 링크-->
            <div class="col-2 d-flex align-items-center">
              <i class="bi bi-calendar3 fs-6 me-2"></i>
              <a
                class="cursor-pointer text-underline-dotted text-dark"
                @click="gotoDetail(annual.yy!)"
              >
                <template v-if="annual.title">
                  <span class="fs-5 me-1">{{ annual.title }}</span>
                </template>
                <template v-else>
                  <span class="fs-5 fw-bolder me-0">{{ annual.yy }}</span>
                  {{ t('journal.closing-by-year') }}
                </template>
                <i class="bi bi-pencil-square fs-4 ms-1"></i>
              </a>
            </div>
            <!--end::연도 + 제목 링크-->

            <!--begin::꿈 통계 + 본문 + 태그-->
            <div class="col fs-5">
              <div class="d-flex justify-content-start">
                <div class="text-gray-700 d-flex-center me-5">
                  <span class="fw-bold me-2">{{ t('journal.annual.diary-count') }}</span>
                  <template v-if="annual.dreamComptYn === 'Y'">
                    <span class="cursor-help">
                      <i class="bi bi-moon-stars-fill fs-4 me-2 text-success"></i>
                      <i class="bi bi-check text-success" style="margin-left:-0.8rem"></i>
                    </span>
                  </template>
                  <template v-else>
                    <span><i class="bi bi-moon-stars fs-4 me-2"></i></span>
                  </template>
                  (<span class="text-info fw-bold mx-1">{{ annual.dreamDayCnt }}</span>{{ t('common.unit.day') }}
                  /
                  <span class="text-info fw-bold mx-1">{{ annual.dreamCnt }}</span>{{ t('common.unit.count') }})
                </div>
              </div>
              <!--begin::마크다운 본문-->
              <div class="mt-2">
                <div
                  class="fs-6 fw-normal text-gray-800 ps-2 pt-3 text-noti"
                  v-html="highlightedAnnualContent(annual)"
                ></div>
              </div>
              <!--end::마크다운 본문-->
              <!--begin::태그 목록-->
              <!-- TODO: 태그 클릭 → JournalDayTagService 연동 미구현 (Sub-2 범위 외) -->
              <div v-if="hasTags(annual)" class="mt-2 ms-5 mt-3">
                <i class="bi bi-tag"></i>
                <span
                  v-for="tag in annual.tag?.list"
                  :key="(tag.tagId as number | string) + ':' + tag.name"
                  class="text-muted pe-1"
                >
                  #<span class="border-bottom text-primary fw-lighter opacity-hover">
                    <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>
                    {{ tag.name }}
                  </span>
                </span>
              </div>
              <!--end::태그 목록-->
            </div>
            <!--end::꿈 통계 + 본문 + 태그-->

            <!--begin::구분선-->
            <div class="col-1 ms-4 d-none d-md-flex border-2 border-gray-300 border-end h-75 w-10px">&nbsp;</div>
            <!--end::구분선-->

            <!--begin::컨텍스트 메뉴-->
            <div class="col-1 py-3 d-none d-md-flex justify-content-end w-50px ps-2">
              <div class="me-0 d-flex align-items-center">
                <button
                  type="button"
                  class="btn btn-sm btn-icon journal-annual-action-btn"
                  data-kt-menu-trigger="click"
                  data-kt-menu-placement="bottom-end"
                  :title="t('journal.annual.menu.tooltip')"
                >
                  <i class="ki-solid ki-dots-horizontal fs-2x"></i>
                </button>
                <div class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold w-200px py-3" data-kt-menu="true">
                  <div class="menu-item px-3">
                    <div class="menu-content text-muted pb-2 px-3 fs-7 text-uppercase">{{ t('journal.annual.menu.category') }}</div>
                  </div>
                  <div class="menu-item px-3 my-1">
                    <div class="menu-link flex-stack px-3" @click="gotoDetail(annual.yy!)">
                      {{ t('journal.annual.go-to-detail') }}
                      <i class="bi bi-arrow-right fs-8"></i>
                    </div>
                  </div>
                  <div class="separator my-2"></div>
                  <div class="menu-item px-3 my-1">
                    <div class="menu-link flex-stack px-3" @click="openModify(annual.yy!)">
                      {{ t('common.mdf') }}
                      <i class="bi bi-pencil-square fs-8"></i>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <!--end::컨텍스트 메뉴-->

          </div>
        </div>
      </div>
    </template>
    <!--end::결산 카드 목록-->

  </div>
  <!--end::결산 목록-->
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useRouter } from "vue-router";
import { useJournalAnnualStore } from "@/features/journal/stores/journalAnnual";
import type { JournalAnnualDto } from "@/features/journal/stores/journalAnnual";
import { useLocaleStore } from "@/shared/i18n/stores/locale";

const router = useRouter();
const store = useJournalAnnualStore();
const { t } = useLocaleStore();

onMounted(() => {
  void store.fetchList();
  void store.fetchTotal();
});

/** 결산 상세 페이지로 이동한다. */
function gotoDetail(yy: number) {
  void router.push({ name: "annual-detail", params: { yy: String(yy) } });
}

/** 결산 수정 모달을 연다. */
function openModify(yy: number) {
  void store.openModify(yy);
}

/** 태그 보유 여부 확인 */
function hasTags(annual: JournalAnnualDto): boolean {
  return Array.isArray(annual.tag?.list) && annual.tag!.list!.length > 0;
}

function highlightedAnnualContent(annual: JournalAnnualDto): string {
  return highlightKeywordInHtml(annual.markdownContent ?? annual.content ?? "", store.listKeyword);
}

function highlightKeywordInHtml(html: string, keyword: string): string {
  const trimmedKeyword = keyword.trim();
  if (!html || !trimmedKeyword || typeof document === "undefined") return html;

  const lowerKeyword = trimmedKeyword.toLowerCase();
  const template = document.createElement("template");
  template.innerHTML = html;

  const textNodes: Text[] = [];
  const skippedTags = new Set(["MARK", "SCRIPT", "STYLE", "TEXTAREA"]);
  const walker = document.createTreeWalker(
    template.content,
    NodeFilter.SHOW_TEXT,
    {
      acceptNode(node) {
        const parent = node.parentElement;
        if (!parent || skippedTags.has(parent.tagName)) return NodeFilter.FILTER_REJECT;
        return (node.nodeValue ?? "").toLowerCase().includes(lowerKeyword)
          ? NodeFilter.FILTER_ACCEPT
          : NodeFilter.FILTER_REJECT;
      },
    }
  );

  while (walker.nextNode()) {
    textNodes.push(walker.currentNode as Text);
  }

  textNodes.forEach((node) => {
    const text = node.nodeValue ?? "";
    const lowerText = text.toLowerCase();
    const fragment = document.createDocumentFragment();
    let cursor = 0;
    let matchIndex = lowerText.indexOf(lowerKeyword, cursor);

    while (matchIndex !== -1) {
      if (matchIndex > cursor) {
        fragment.appendChild(document.createTextNode(text.slice(cursor, matchIndex)));
      }

      const mark = document.createElement("mark");
      mark.className = "journal-annual-list-vue__keyword-mark";
      mark.textContent = text.slice(matchIndex, matchIndex + trimmedKeyword.length);
      fragment.appendChild(mark);

      cursor = matchIndex + trimmedKeyword.length;
      matchIndex = lowerText.indexOf(lowerKeyword, cursor);
    }

    if (cursor < text.length) {
      fragment.appendChild(document.createTextNode(text.slice(cursor)));
    }

    node.parentNode?.replaceChild(fragment, node);
  });

  return template.innerHTML;
}
</script>

<style scoped>
.journal-annual-list-vue :deep(.journal-annual-list-vue__keyword-mark) {
  background-color: #fff3cd;
  border-radius: 0.25rem;
  box-shadow: inset 0 -0.35em 0 rgba(255, 193, 7, 0.35);
  color: inherit;
  font-weight: 700;
  padding: 0 0.12em;
}
</style>
