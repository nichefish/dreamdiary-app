<template>
  <!--begin::게시판 게시물 목록-->
  <div class="board-post-list-vue">

    <!--begin::목록 헤더 툴바-->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h4 class="fs-5 fw-bold mb-0">게시판</h4>
      <button
        type="button"
        class="btn btn-sm btn-primary"
        @click="store.openRegist()"
      >
        <i class="bi bi-plus fs-5 pe-1"></i>
        등록
      </button>
    </div>
    <!--end::목록 헤더 툴바-->

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
              <th class="text-center wb-keepall w-10 hidden-table">번호</th>
              <th class="col-lg-9 col-9 text-center wb-keepall">제목</th>
              <th class="col-lg-1 text-center wb-keepall hidden-table">첨부</th>
              <th class="col-lg-1 text-center wb-keepall hidden-table">관리</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!store.postList.length">
              <td colspan="4" class="text-center text-muted py-6 fs-7">등록된 게시물이 없습니다.</td>
            </tr>
            <tr
              v-for="post in store.postList"
              :key="post.id"
              class="cursor-pointer"
              @click="store.openDetail(post.id!)"
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
                  title="댓글 목록"
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
                <div class="d-flex justify-content-center gap-1">
                  <button
                    type="button"
                    class="btn btn-sm btn-icon btn-light-primary"
                    title="수정"
                    @click.stop="store.openModify(post.id!)"
                  >
                    <i class="bi bi-pencil fs-7"></i>
                  </button>
                  <button
                    type="button"
                    class="btn btn-sm btn-icon btn-light-danger"
                    title="삭제"
                    @click.stop="store.deletePost(post.id!)"
                  >
                    <i class="bi bi-trash fs-7"></i>
                  </button>
                </div>
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
import { onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import { useBoardPostStore } from "@/stores/boardPost";
import { useAttachableModalStore } from "@/stores/attachableModal";
import type { BoardPostDto } from "@/stores/boardPost";

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

onMounted(async () => {
  const key = route.params.boardKey as string;
  if (key) await store.setBoard(key);
});

/** 게시물 태그 보유 여부 */
function hasPostTags(post: BoardPostDto): boolean {
  return Array.isArray(post.tag?.list) && post.tag!.list!.length > 0;
}

function openCommentList(post: BoardPostDto): void {
  const contentType = post.contentType ?? store.boardKey;
  if (!post.id || !contentType) return;
  void attachableStore.openCommentList(post.id, contentType);
}
</script>
