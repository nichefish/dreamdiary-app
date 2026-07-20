<template>
  <!--begin::저널 스레드 목록-->
  <div class="journal-thread-list-vue">

    <!--begin::태그 클라우드·검색 카드 — 등록은 JournalThreadViewToolbar. 카드는 툴바에 붙인다(margin-top: 0).-->
    <div class="card mb-4" style="margin-top: 0 !important;">
      <div class="card-body px-4 py-3">
        <div class="d-flex justify-content-between align-items-start gap-3 mb-2">
          <!--begin::태그 클라우드-->
          <div class="d-flex flex-wrap align-items-center gap-1 flex-grow-1 py-1" style="min-width:0;">
            <i class="bi bi-tags text-muted me-1"></i>
            <span v-if="store.tagCloudLoading" class="text-muted fs-7">{{ t("common.loading") }}...</span>
            <span v-else-if="store.tagCloudError" class="text-danger fs-7">{{ store.tagCloudError }}</span>
            <span v-else-if="!store.tagCloud.length" class="text-muted fs-7">{{ t("journal.thread.tag-cloud.empty") }}</span>
            <template v-else>
              <button
                v-for="tag in store.tagCloud"
                :key="tag.id"
                type="button"
                class="btn btn-sm py-1 px-2"
                :class="store.filterTagId === tag.id ? 'btn-light-primary border border-primary' : 'btn-light text-gray-600'"
                @click="toggleTagFilter(tag.id)"
              >
                <span v-if="tag.ctgr" class="fs-9 text-muted me-1">[{{ tag.ctgr }}]</span>
                <span :class="[tag.tagClass, tag.textClass]">#{{ tag.name }}</span>
                <span class="fs-9 text-muted ms-1">{{ tag.contentSize ?? 0 }}</span>
              </button>
            </template>
          </div>
          <!--end::태그 클라우드-->
        </div>

        <!--begin::검색 폼-->
        <form class="d-flex flex-wrap align-items-center gap-2 border-top pt-3" @submit.prevent="search">
          <select v-model="store.filterCategory" class="form-select form-select-sm form-select-solid w-auto flex-shrink-0">
            <option value="">{{ t("journal.thread.filter.all-categories") }}</option>
            <option v-for="category in store.categoryOptions" :key="category.code" :value="category.code">
              {{ category.codeName }}
            </option>
          </select>
          <input
            v-model.trim="store.filterKeyword"
            type="search"
            class="form-control form-control-sm form-control-solid flex-grow-1"
            style="min-width:200px;"
            :placeholder="t('journal.thread.filter.keyword.placeholder')"
          />
          <button type="submit" class="btn btn-sm btn-light-primary">{{ t("common.search") }}</button>
          <button type="button" class="btn btn-sm btn-light" @click="resetFilters">{{ t("common.reset") }}</button>
        </form>
        <div v-if="store.categoryError" class="text-danger fs-8 mt-2">{{ store.categoryError }}</div>
        <!--end::검색 폼-->
      </div>
    </div>
    <!--end::태그 클라우드·검색 카드-->

    <div class="card post">
      <div class="card-body">

        <!--begin::로딩-->
        <div v-if="store.loading" class="d-flex justify-content-center py-10">
          <span class="spinner-border text-primary" role="status"></span>
        </div>
        <!--end::로딩-->

        <table v-else class="table align-middle table-row-dashed fs-small gy-5 table-fixed hoverTable mb-3">
          <thead>
            <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
              <th class="text-center wb-keepall w-10 hidden-table">{{ t("common.number") }}</th>
              <th class="col-lg-9 col-9 text-center wb-keepall">{{ t("common.title") }}</th>
              <th class="col-lg-1 text-center wb-keepall hidden-table">{{ t("common.attachment") }}</th>
              <th class="col-lg-1 text-center wb-keepall hidden-table">{{ t("common.manage") }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!store.threadList.length">
              <td colspan="4" class="text-center text-muted py-6 fs-7">{{ t("journal.thread.empty") }}</td>
            </tr>
            <tr
              v-for="thread in store.threadList"
              :key="thread.id"
              class="cursor-pointer"
              @click="openDetail(thread.id!)"
            >
              <td class="text-center text-gray-500 fs-7 hidden-table">{{ thread.rnum }}</td>
              <td class="ps-3">
                <span v-if="thread.categoryName" class="badge badge-light-primary me-2 fs-9">{{ thread.categoryName }}</span>
                <span class="fs-6">{{ thread.title }}</span>
                <button
                  v-if="thread.comment && thread.comment.cnt"
                  type="button"
                  class="btn btn-link text-muted ms-2 fs-8 p-0 align-baseline"
                  :title="t('journal.thread.comments.tooltip')"
                  @click.stop="openCommentList(thread)"
                >[{{ thread.comment.cnt }}]</button>
                <!--begin::태그-->
                <div v-if="hasThreadTags(thread)" class="mt-1">
                  <i class="bi bi-tag fs-8 me-1"></i>
                  <span
                    v-for="tag in thread.tag?.list"
                    :key="String(tag.tagId)"
                    class="text-muted fs-9 pe-1"
                  >
                    #{{ tag.name }}
                  </span>
                </div>
                <!--end::태그-->
              </td>
              <td class="text-center hidden-table">
                <i v-if="thread.hasFiles" class="bi bi-paperclip text-muted"></i>
              </td>
              <td class="text-center">
                <!--begin::컨텍스트 메뉴-->
                <div class="d-flex justify-content-center">
                  <button
                    type="button"
                    class="btn btn-sm btn-icon btn-bg-light btn-active-color-primary"
                    data-kt-menu-trigger="click"
                    data-kt-menu-placement="bottom-end"
                    :title="t('common.menu')"
                    @click.stop
                  >
                    <i class="ki-solid ki-dots-horizontal fs-2x"></i>
                  </button>
                  <div
                    class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold w-200px py-3"
                    data-kt-menu="true"
                    @click.stop
                  >
                    <div class="menu-item px-3 my-1">
                      <div class="menu-link flex-stack px-3" @click="openModify(thread.id!)">
                        {{ t("common.edit") }}
                        <i class="bi bi-pencil-square fs-8"></i>
                      </div>
                    </div>
                    <div class="separator my-2"></div>
                    <div class="menu-item px-3 my-1">
                      <div class="menu-link flex-stack px-3 text-danger" @click="store.deleteThread(thread.id!)">
                        {{ t("common.delete") }}
                        <i class="bi bi-trash text-danger p-0 fs-8"></i>
                      </div>
                    </div>
                  </div>
                </div>
                <!--end::컨텍스트 메뉴-->
              </td>
            </tr>
          </tbody>
        </table>

      </div>
      <!--begin::페이지네이션-->
      <div v-if="store.totalPages > 1" class="card-footer">
        <div class="d-flex justify-content-center gap-1">
          <button
            v-for="p in store.totalPages"
            :key="p"
            type="button"
            class="btn btn-sm"
            :class="(p - 1) === store.currentPage ? 'btn-primary' : 'btn-light'"
            @click="store.fetchList(p - 1)"
          >
            {{ p }}
          </button>
        </div>
      </div>
      <!--end::페이지네이션-->
    </div>

  </div>
  <!--end::저널 스레드 목록-->
</template>

<script setup lang="ts">
  import { onMounted, watch } from "vue";
  import { useRouter } from "vue-router";
  import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
  import { useAttachableModalStore } from "@/features/attachable/stores/attachableModal";
  import type { JournalThreadDto } from "@/features/journal/stores/journalThread";
  import { useLocaleStore } from "@/shared/i18n/stores/locale";
  import { reinitMetronicAfterDom } from "@/shared/utils/metronicReinit";
  
  const store = useJournalThreadStore();
  const attachableStore = useAttachableModalStore();
  const { t } = useLocaleStore();
  const router = useRouter();
  
  onMounted(() => {
    void Promise.all([
      store.fetchList(0),
      store.fetchTagCloud(),
      store.fetchCategoryOptions(),
    ]);
  });

  watch(
    () => store.loading,
    (loading, wasLoading) => {
      if (wasLoading && !loading) void reinitMetronicAfterDom();
    }
  );

  function search(): void {
    void store.fetchList(0);
  }

  function resetFilters(): void {
    void store.resetFilters();
  }

  function toggleTagFilter(tagId: number): void {
    store.filterTagId = store.filterTagId === tagId ? null : tagId;
    void store.fetchList(0);
  }

  function openDetail(id: number): void {
    void router.push({ name: "thread-detail", params: { id } });
  }

  function openModify(id: number): void {
    void router.push({ name: "thread-edit", params: { id } });
  }

/** 스레드 태그 보유 여부 */
function hasThreadTags(thread: JournalThreadDto): boolean {
  return Array.isArray(thread.tag?.list) && thread.tag!.list!.length > 0;
}

function openCommentList(thread: JournalThreadDto): void {
  const contentType = thread.contentType ?? "JOURNAL_THREAD";
  if (!thread.id || !contentType) return;
  void attachableStore.openCommentList(thread.id, contentType);
}
</script>
