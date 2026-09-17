<template>
  <div class="user-admin-page">
    <!--begin::뷰 탭 + 툴바 — AdminPage 와 동일 골격(nav-tabs-line + ps-5 mt-5). 등록 버튼은 계정 탭에서만 노출.-->
    <div class="user-admin-view-toolbar d-flex flex-column-fluid justify-content-between align-items-start align-items-xl-center gap-4 w-100">
      <ul class="nav nav-tabs nav-tabs-line ps-5 mt-5 mb-0 flex-grow-1" role="tablist" :aria-label="t('user.admin.tab.aria-label')">
        <li class="nav-item" role="presentation">
          <button
            type="button"
            class="nav-link px-6"
            :class="{ active: activeTab === 'accounts' }"
            role="tab"
            :aria-selected="activeTab === 'accounts'"
            @click="selectTab('accounts')"
          >
            {{ t('user.admin.tab.accounts') }}
          </button>
        </li>
        <li class="nav-item" role="presentation">
          <button
            type="button"
            class="nav-link px-6"
            :class="{ active: activeTab === 'signup' }"
            role="tab"
            :aria-selected="activeTab === 'signup'"
            @click="selectTab('signup')"
          >
            {{ t('user.admin.tab.signup') }}
            <!--미승인 건수 배지 — 메뉴가 분리돼 있을 땐 눈에 띄던 대기 건수가 탭 안으로 들어가며 묻히지 않도록 노출-->
            <span v-if="pendingCount > 0" class="badge badge-circle badge-danger ms-2">{{ pendingCount }}</span>
          </button>
        </li>
      </ul>
      <div v-if="activeTab === 'accounts'" class="d-flex align-items-center flex-shrink-0 pe-5 mt-3 mb-1 gap-2">
        <button type="button" class="btn btn-sm btn-primary text-nowrap" @click="store.openCreate">
          <i class="bi bi-plus-lg"></i>
          {{ t('user.admin.register') }}
        </button>
      </div>
    </div>
    <!--end::뷰 탭 + 툴바-->

    <!--begin::계정 신청 승인 탭-->
    <UserSignupApprovalList v-if="activeTab === 'signup'" />
    <!--end::계정 신청 승인 탭-->

    <UserAdminAccountList v-else @detail="openDetail" @edit="openEdit" />
    <!--end::계정 목록 탭-->

    <!--모달은 탭과 무관하게 항상 마운트한다 (탭 전환 중 열려 있어도 유지)-->
    <UserAdminDetailModal v-if="store.detailOpen" @edit="openEdit" />

    <UserAdminFormModal v-if="store.formOpen" />
  </div>
</template>

<script setup lang="ts">
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalAlert } from "@/shared/utils/swal";
import { computed, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUserAdminStore } from "@/features/admin/stores/userAdmin";
import { useUserSignupStore } from "@/features/user/stores/userSignup";
import UserSignupApprovalList from "@/features/user/signup/UserSignupApprovalList.vue";
import UserAdminDetailModal from "@/features/admin/components/UserAdminDetailModal.vue";
import UserAdminFormModal from "@/features/admin/components/UserAdminFormModal.vue";
import UserAdminAccountList from "@/features/admin/components/UserAdminAccountList.vue";
import { reinitMetronicAfterDom } from "@/shared/utils/metronicReinit";

const route = useRoute();
const router = useRouter();
const store = useUserAdminStore();
/**
 * 계정 신청 승인은 데이터 원천(신청 API)이 계정 관리(/api/users)와 완전히 분리돼 있어
 * store 를 합치지 않고 그대로 쓴다. 화면만 탭으로 흡수한다.
 */
const signupStore = useUserSignupStore();

/** 계정 관리 탭 — AdminPage 와 동일하게 `?tab=` query 로 상태를 유지한다 */
type UserAdminTab = "accounts" | "signup";
const activeTab = computed<UserAdminTab>(() => (route.query.tab === "signup" ? "signup" : "accounts"));

/** 승인 대기 건수 (탭 라벨 배지) */
const pendingCount = computed(() => signupStore.pendingList.length);

async function selectTab(tab: UserAdminTab) {
  await router.replace({ query: { ...route.query, tab } });
}
const { t } = useLocaleStore();

async function openDetail(id: number) {
  try {
    await store.openDetail(id);
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("user.admin.detail.load.failure"));
  }
}

async function openEdit(id: number) {
  try {
    await store.openEdit(id);
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("user.admin.detail.load.failure"));
  }
}

onMounted(async () => {
  await Promise.all([store.fetchBootstrap(), store.fetchUsers(0)]);
  /*
   * 승인 대기 건수 배지는 어느 탭에 있든 보여야 하므로 진입 시 함께 조회한다.
   * 승인 탭 자체는 UserSignupApprovalList 가 마운트될 때 다시 조회한다.
   */
  void signupStore.fetchApprovalList();
  const id = Number(route.query.id);
  if (!Number.isFinite(id) || id <= 0) return;
  if (route.query.mode === "edit") await openEdit(id);
  else await openDetail(id);
});

/** 계정 탭으로 돌아올 때 승인 처리 결과가 배지에 반영되도록 건수를 갱신한다. */
watch(activeTab, (tab) => {
  if (tab === "accounts") void signupStore.fetchApprovalList();
});

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
</script>

<style scoped>
@import "./components/userAdminShared.scss";
</style>
