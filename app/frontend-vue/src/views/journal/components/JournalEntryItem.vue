<template>
  <!--begin::엔트리 행-->
  <div
    :class="['d-flex gap-2 py-2', isCollapsed ? 'is-collapsed' : '']"
    :data-id="entry.id"
  >
    <!--begin::순번 + 접힘 버튼-->
    <div class="d-none d-md-flex flex-column align-items-center pt-1 ps-2" style="width:56px; min-width:56px;">
      <span :class="['fw-bold fs-7', isResolved ? 'text-success' : 'text-muted']">#{{ entry.sortOrder }}</span>
      <span v-if="isResolved" class="badge badge-light-success fs-8 mt-1">DONE</span>
      <span v-else-if="lcKey === 'PENDING'" class="badge badge-light-warning fs-8 mt-1">PEND</span>
    </div>
    <!--end::순번 + 접힘 버튼-->

    <!--begin::본문 영역-->
    <div class="flex-grow-1">
      <!--begin::꿈 상태 배지 (꿈 엔트리 전용)-->
      <div v-if="isDream" class="d-flex align-items-center gap-1 mb-1 flex-wrap">
        <span v-if="hasState('NHTMR')" class="badge badge-light-danger">!악몽</span>
        <span v-if="hasState('HALLUC')" class="badge badge-light-secondary">!환각/현시</span>
        <span v-if="entry.elseDreamYn === 'Y'" class="badge badge-light-secondary">
          {{ entry.elseDreamerNm }} 꿈
        </span>
        <span v-if="entry.title" class="fw-bold fs-7">{{ entry.title }}</span>
      </div>
      <!--end::꿈 상태 배지-->

      <!--begin::상태 배지 (중요/참조)-->
      <div v-if="hasState('IMPRTC') || hasState('REFRNC')" class="d-flex gap-1 mb-1">
        <span v-if="hasState('IMPRTC')" class="badge badge-light-warning">중요</span>
        <span v-if="hasState('REFRNC')" class="badge badge-light-info">참조</span>
      </div>
      <!--end::상태 배지-->

      <!--begin::마크다운 본문-->
      <div
        v-if="!isCollapsed && entry.markdownContent"
        class="journal-content fs-7 p-2"
        v-html="entry.markdownContent"
      ></div>
      <div v-else-if="isCollapsed" class="text-muted fs-8 fst-italic ps-2">접힌 상태</div>
      <!--end::마크다운 본문-->

      <!--begin::엔트리 태그-->
      <div v-if="tagList.length > 0" class="d-flex flex-wrap gap-1 mt-1 ps-2">
        <span
          v-for="tag in tagList"
          :key="tag.tagId"
          class="text-muted fs-8 cursor-default"
        >
          #<span v-if="tag.ctgr" class="text-noti me-1 fs-8">[{{ tag.ctgr }}]</span>{{ tag.name }}
        </span>
      </div>
      <!--end::엔트리 태그-->

      <!--begin::관련글-->
      <div v-if="relatedList.length > 0" class="d-flex flex-column gap-1 mt-2 ps-2">
        <div
          v-for="rel in relatedList"
          :key="rel.id"
          class="d-flex align-items-center gap-2 p-2 bg-light rounded fs-8 text-muted"
        >
          <i class="bi bi-link-45deg"></i>
          <span v-if="rel.contentType" class="badge badge-light-secondary">{{ rel.contentType }}</span>
          <span v-if="rel.refTitle">{{ rel.refTitle }}</span>
          <span v-if="rel.relReason" class="fst-italic">({{ rel.relReason }})</span>
        </div>
      </div>
      <!--end::관련글-->

      <!--begin::댓글-->
      <div v-if="commentList.length > 0" class="d-flex flex-column gap-1 mt-2 ps-2">
        <div
          v-for="cmt in commentList"
          :key="cmt.id"
          class="fs-8 text-muted ps-2 border-start border-2 border-gray-300"
        >
          {{ cmt.content }}
        </div>
      </div>
      <!--end::댓글-->
      <!--begin::엔트리 액션 버튼-->
      <div v-if="entry.id" class="d-flex gap-2 mt-2 ps-2">
        <button type="button" class="btn btn-xs btn-icon btn-bg-light btn-active-color-primary" title="댓글 등록" @click="openCommentReg"><i class="bi bi-chat-dots fs-8"></i></button>
        <button type="button" class="btn btn-xs btn-icon btn-bg-light btn-active-color-primary" title="이력 보기" @click="openHistory"><i class="bi bi-clock-history fs-8"></i></button>
        <button type="button" class="btn btn-xs btn-icon btn-bg-light btn-active-color-primary" title="관련 글 추가" @click="openRelated"><i class="bi bi-link-45deg fs-8"></i></button>
      </div>
      <!--end::엔트리 액션 버튼-->
    </div>
    <!--end::본문 영역-->
  </div>
  <!--end::엔트리 행-->
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useJournalModalStore } from "@/stores/journalModal";
import { useAttachableModalStore } from "@/stores/attachableModal";
import type { JournalEntryDto } from "@/stores/journal";

const props = defineProps<{
  entry: JournalEntryDto;
  isDream?: boolean;
}>();

const modalStore = useJournalModalStore();
const attachableStore = useAttachableModalStore();

const lcKey = computed(() => props.entry.lifecycle?.lifecycleKey ?? "");
const isResolved = computed(() => lcKey.value === "RESOLVED");
const isCollapsed = computed(() => hasState("COLLAPSED"));

function hasState(key: string): boolean {
  return (props.entry.state?.list ?? []).some((s) => s.stateKey === key);
}

const tagList = computed(() => props.entry.tag?.list ?? []);
const relatedList = computed(() => props.entry.relatedContentList ?? []);
const commentList = computed(() => props.entry.comment?.list ?? []);

/** 엔트리 수정 모달 열기 */
function openMdf() {
  void modalStore.openEntryMdf(props.entry.id);
}

/** 댓글 등록 모달 열기 */
function openCommentReg() {
  if (!props.entry.id || !props.entry.contentType) return;
  attachableStore.openCommentReg(props.entry.id, props.entry.contentType);
}

/** 이력 모달 열기 */
function openHistory() {
  if (!props.entry.id || !props.entry.contentType) return;
  void attachableStore.openHistory(props.entry.contentType, props.entry.id);
}

/** 관련 글 추가 모달 열기 */
function openRelated() {
  if (!props.entry.id || !props.entry.contentType) return;
  attachableStore.openRelated(props.entry.contentType, props.entry.id);
}
</script>
