<template>
  <!--begin::저널 스레드 목록-->
  <div class="journal-thread-list-vue">

    <!--begin::목록 헤더 툴바-->
    <div class="d-flex justify-content-end align-items-center mb-4">
      <button
        type="button"
        class="btn btn-sm btn-primary"
        @click="openCreate()"
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
            <tr v-if="!store.threadList.length">
              <td colspan="4" class="text-center text-muted py-6 fs-7">등록된 스레드가 없습니다.</td>
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
                  title="댓글 목록"
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
                <div class="d-flex justify-content-center gap-1">
                  <button
                    type="button"
                    class="btn btn-sm btn-icon btn-light-primary"
                    title="수정"
                    @click.stop="openModify(thread.id!)"
                  >
                    <i class="bi bi-pencil fs-7"></i>
                  </button>
                  <button
                    type="button"
                    class="btn btn-sm btn-icon btn-light-danger"
                    title="삭제"
                    @click.stop="store.deleteThread(thread.id!)"
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
  <!--end::저널 스레드 목록-->
</template>

<script setup lang="ts">
  import { onMounted } from "vue";
  import { useRouter } from "vue-router";
  import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
  import { useAttachableModalStore } from "@/features/attachable/stores/attachableModal";
  import type { JournalThreadDto } from "@/features/journal/stores/journalThread";
  
  const store = useJournalThreadStore();
  const attachableStore = useAttachableModalStore();
  const router = useRouter();
  
  onMounted(() => {
    void store.fetchList(0);
  });

  function openCreate(): void {
    void router.push({ name: "thread-create" });
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
