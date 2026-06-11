<template>
  <!--begin::계정 신청 승인관리-->
  <div class="user-signup-approval-vue">

    <!--begin::로딩-->
    <div v-if="store.loading" class="d-flex justify-content-center py-10">
      <span class="spinner-border text-primary" role="status"></span>
    </div>
    <!--end::로딩-->

    <template v-else>
      <!--begin::승인 대기 목록-->
      <div class="card post mb-5">
        <div class="card-header">
          <h3 class="card-title">승인 대기 신청</h3>
        </div>
        <div class="card-body">
          <table class="table align-middle table-row-dashed fs-small gy-3 table-fixed mb-0">
            <thead>
              <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                <th class="text-center w-10">ID</th>
                <th class="text-center w-25">이름 / 아이디</th>
                <th class="text-center w-25">E-MAIL</th>
                <th class="text-center w-15">신청일시</th>
                <th class="text-center w-25">처리</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!store.pendingList.length">
                <td colspan="5" class="text-center text-muted py-6 fs-7">대기 중인 신청이 없습니다.</td>
              </tr>
              <tr v-for="req in store.pendingList" :key="'p-' + req.id">
                <td class="text-center">{{ req.id }}</td>
                <td class="text-start">{{ req.nickname || '-' }} ({{ req.username || '-' }})</td>
                <td class="text-start">{{ req.email || '-' }}</td>
                <td class="text-center">{{ req.createdAt || '-' }}</td>
                <td class="text-center">
                  <button type="button" class="btn btn-sm btn-primary me-2" @click="store.approve(req.id)">승인</button>
                  <button type="button" class="btn btn-sm btn-light-danger" @click="store.reject(req.id)">반려</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <!--end::승인 대기 목록-->

      <!--begin::최근 신청 목록-->
      <div class="card post">
        <div class="card-header">
          <h3 class="card-title">최근 신청 내역 (최근 30건)</h3>
        </div>
        <div class="card-body">
          <table class="table align-middle table-row-dashed fs-small gy-3 table-fixed mb-0">
            <thead>
              <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                <th class="text-center w-10">ID</th>
                <th class="text-center w-25">이름 / 아이디</th>
                <th class="text-center w-25">E-MAIL</th>
                <th class="text-center w-15">신청일시</th>
                <th class="text-center w-25">상태</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!store.recentList.length">
                <td colspan="5" class="text-center text-muted py-6 fs-7">최근 신청 내역이 없습니다.</td>
              </tr>
              <tr v-for="req in store.recentList" :key="'r-' + req.id">
                <td class="text-center">{{ req.id }}</td>
                <td class="text-start">{{ req.nickname || '-' }} ({{ req.username || '-' }})</td>
                <td class="text-start">{{ req.email || '-' }}</td>
                <td class="text-center">{{ req.createdAt || '-' }}</td>
                <td class="text-center">
                  <span :class="statusBadge(req.status)">{{ req.status || '-' }}</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <!--end::최근 신청 목록-->
    </template>

  </div>
  <!--end::계정 신청 승인관리-->
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useUserSignupStore } from "@/stores/userSignup";

const store = useUserSignupStore();

onMounted(() => {
  void store.fetchApprovalList();
});

/** 상태별 뱃지 CSS 클래스 */
function statusBadge(status: string): string {
  if (status === "APPROVED") return "badge badge-light-success";
  if (status === "REJECTED") return "badge badge-light-danger";
  return "badge badge-light-warning";
}
</script>
