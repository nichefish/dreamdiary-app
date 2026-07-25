<template>
  <!--begin::게시판 게시물 목록-->
  <div class="board-post-list-vue">

    <!--begin::헤더·검색 카드 — 등록은 BoardPostViewToolbar. 카드는 툴바에 붙인다(margin-top: 0).-->
    <div class="card mb-4" style="margin-top: 0 !important;">
      <div class="card-body px-4 py-3">
        <div class="d-flex justify-content-between align-items-start gap-3 mb-2">
          <!--begin::태그 클라우드
            게시판 태그는 tag_content.ref_content_type 에 boardKey 로 저장되므로
            /api/tags?contentType=<boardKey> 로 조회한다. 같은 태그를 다시 누르면 필터가 해제된다. -->
          <div class="d-flex flex-wrap align-items-center gap-1 flex-grow-1 py-1" style="min-width:0;">
            <i class="bi bi-tags text-muted me-1"></i>
            <span v-if="store.tagCloudLoading" class="text-muted fs-7">{{ t('common.loading') }}...</span>
            <span v-else-if="store.tagCloudError" class="text-danger fs-7">{{ store.tagCloudError }}</span>
            <span v-else-if="!store.tagCloud.length" class="text-muted fs-7">{{ t('board.post.tag-cloud.empty') }}</span>
            <template v-else>
              <button
                v-for="tag in store.tagCloud"
                :key="tag.id"
                type="button"
                class="btn btn-sm py-1 px-2"
                :class="store.filterTagId === tag.id ? 'btn-light-primary border border-primary' : 'btn-light text-gray-600'"
                @click="store.toggleTagFilter(tag.id)"
              >
                <span v-if="tag.ctgr" class="fs-9 text-muted me-1">[{{ tag.ctgr }}]</span>
                <span :class="[tag.tagClass, tag.textClass]">#{{ tag.name }}</span>
                <span class="fs-9 text-muted ms-1">{{ tag.contentSize ?? 0 }}</span>
              </button>
            </template>
          </div>
          <!--end::태그 클라우드-->

        </div>

        <!--begin::검색 폼
          store.filterKeyword/filterCategory 는 이미 fetchList 가 API(searchKeyword/categoryCode)로
          전송하고 있었으나 입력 UI 가 없어 값을 넣을 수단이 없었다. -->
        <form class="d-flex flex-wrap align-items-center gap-2 border-top pt-3" @submit.prevent="search">
          <!--분류 그룹이 지정되지 않은 게시판은 선택지가 비므로 select 자체를 숨긴다-->
          <select
            v-if="store.categoryOptions.length"
            v-model="store.filterCategory"
            class="form-select form-select-sm form-select-solid w-auto flex-shrink-0"
          >
            <option value="">{{ t('board.post.filter.all-categories') }}</option>
            <option v-for="category in store.categoryOptions" :key="category.code" :value="category.code">
              {{ category.codeName }}
            </option>
          </select>
          <input
            v-model.trim="store.filterKeyword"
            type="search"
            class="form-control form-control-sm form-control-solid flex-grow-1"
            style="min-width:200px;"
            :placeholder="t('board.post.filter.keyword.placeholder')"
          />
          <button type="submit" class="btn btn-sm btn-light-primary">{{ t('common.search') }}</button>
          <button type="button" class="btn btn-sm btn-light" @click="store.resetFilters()">{{ t('common.reset') }}</button>
        </form>
        <div v-if="store.categoryError" class="text-danger fs-8 mt-2">{{ store.categoryError }}</div>
        <!--end::검색 폼-->
      </div>
    </div>
    <!--end::헤더·검색 카드-->

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
              <th class="text-center wb-keepall w-10 hidden-table">{{ t('board.group.list.number') }}</th>
              <th class="col-lg-9 col-9 text-center wb-keepall">{{ t('common.title') }}</th>
              <th class="col-lg-1 text-center wb-keepall hidden-table">{{ t('board.post.list.col.attach') }}</th>
              <th class="col-lg-1 text-center wb-keepall hidden-table">{{ t('board.group.list.manage') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!store.postList.length">
              <td colspan="4" class="text-center text-muted py-6 fs-7">{{ t('board.post.list.empty') }}</td>
            </tr>
            <tr
              v-for="post in store.postList"
              :key="post.id"
              class="cursor-pointer"
              @click="onPostRowClick($event, post.id!)"
            >
              <td class="text-center text-gray-500 fs-7 hidden-table">{{ post.rnum }}</td>
              <td class="ps-3">
                <span v-if="post.ctgrNm" class="badge badge-light-primary me-2 fs-9">{{ post.ctgrNm }}</span>
                <span class="fs-6">{{ post.title }}</span>
                <span v-if="post.isNew" class="badge border-0 text-white bg-noti fs-8 ms-2">N</span>
                <button
                  v-if="post.comment && post.comment.cnt"
                  type="button"
                  class="btn btn-link text-muted ms-2 fs-8 p-0 align-baseline"
                  :title="t('board.post.list.comment-list.tooltip')"
                  @click.stop="openCommentList(post)"
                >[{{ post.comment.cnt }}]</button>
                <!--begin::태그-->
                <div v-if="hasPostTags(post)" class="mt-1">
                  <i class="bi bi-tag fs-8 me-1"></i>
                  <span
                    v-for="tag in post.tag?.list"
                    :key="String(tag.tagId)"
                    class="text-muted fs-9 pe-1"
                  >
                    #{{ tag.name }}
                  </span>
                </div>
                <!--end::태그-->
              </td>
              <td class="text-center hidden-table">
                <i v-if="post.hasFiles" class="bi bi-paperclip text-muted"></i>
              </td>
              <td class="text-center">
                <!--begin::컨텍스트 메뉴 (저널 스레드·일자와 동일 KTMenu SSOT)
                  트리거에 @click.stop 을 두면 body 위임 클릭이 막혀 메뉴가 열리지 않는다.
                  행 상세 이동은 isMetronicMenuEventTarget 가드로 막는다.
                  store.loading 종료 시 reinitMetronicAfterDom() 을 호출한다. -->
                <div class="d-flex justify-content-center">
                  <button
                    type="button"
                    class="btn btn-sm btn-icon btn-bg-light btn-active-color-primary"
                    data-kt-menu-trigger="click"
                    data-kt-menu-placement="bottom-end"
                    :title="t('common.menu')"
                  >
                    <i class="ki-solid ki-dots-horizontal fs-2x"></i>
                  </button>
                  <div
                    class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold w-200px py-3"
                    data-kt-menu="true"
                    @click.stop
                  >
                    <div class="menu-item px-3 my-1">
                      <div class="menu-link flex-stack px-3" @click="store.openModify(post.id!)">
                        {{ t('common.edit') }}
                        <i class="bi bi-pencil-square fs-8"></i>
                      </div>
                    </div>
                    <div class="separator my-2"></div>
                    <div class="menu-item px-3 my-1">
                      <div class="menu-link flex-stack px-3 text-danger" @click="store.deletePost(post.id!)">
                        {{ t('common.delete') }}
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
  <!--end::게시판 게시물 목록-->
</template>

<script setup lang="ts">
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import { useBoardPostStore } from "@/features/board/stores/boardPost";
import { useAttachableModalStore } from "@/features/attachable/stores/attachableModal";
import type { BoardPostDto } from "@/features/board/stores/boardPost";
import { isMetronicMenuEventTarget, reinitMetronicAfterDom } from "@/shared/utils/metronicReinit";

const { t } = useLocaleStore();
const route = useRoute();
const store = useBoardPostStore();
const attachableStore = useAttachableModalStore();

/** boardKey 변경 시 목록 재조회 */
watch(
  () => route.params.boardKey as string,
  async (key) => {
    if (key) await store.setBoard(key);
  },
  { immediate: true }
);

/**
 * 목록 렌더가 끝나면 Metronic 컨텍스트 메뉴를 재바인딩한다.
 * 행 액션이 `data-kt-menu` 드롭다운이라, 비동기로 교체된 DOM 에는 핸들러가 붙어 있지 않다.
 */
watch(
  () => store.loading,
  (loading, wasLoading) => {
    if (wasLoading && !loading) void reinitMetronicAfterDom();
  }
);

onMounted(async () => {
  const key = route.params.boardKey as string;
  if (key) await store.setBoard(key);
});

/** 게시물 태그 보유 여부 */
/** 검색 실행 — 조건이 바뀌었으므로 항상 첫 페이지부터 조회한다. */
function onPostRowClick(event: MouseEvent, id: number): void {
  if (isMetronicMenuEventTarget(event.target)) return;
  store.openDetail(id);
}

function search(): void {
  void store.fetchList(0);
}

function hasPostTags(post: BoardPostDto): boolean {
  return Array.isArray(post.tag?.list) && post.tag!.list!.length > 0;
}

function openCommentList(post: BoardPostDto): void {
  const contentType = post.contentType ?? store.boardKey;
  if (!post.id || !contentType) return;
  void attachableStore.openCommentList(post.id, contentType);
}
</script>
