<template>
  <div class="card post" style="margin-top: 0 !important;">
    <div class="card-body">
      <div class="user-admin-listbar">
        <div class="user-admin-search">
          <input
            v-model.trim="store.keyword"
            type="search"
            class="form-control form-control-solid"
            maxlength="200"
            :placeholder="t('user.admin.search.placeholder')"
            @keyup.enter="store.fetchUsers(0)"
          />
          <select v-model="store.roleKey" class="form-select form-select-solid user-admin-role-filter">
            <option value="">{{ t('user.admin.search.role.all') }}</option>
            <option v-for="role in store.activeRoles" :key="role.roleKey" :value="role.roleKey">{{ role.roleName }}</option>
          </select>
          <button type="button" class="btn btn-sm btn-light-primary" :disabled="store.loading" @click="store.fetchUsers(0)">
            <i class="bi bi-search"></i>
          </button>
        </div>
        <select :value="store.pageSize" class="form-select form-select-solid user-admin-page-size" @change="onPageSizeChange">
          <option :value="10">{{ t('common.page-size.10') }}</option>
          <option :value="25">{{ t('common.page-size.25') }}</option>
          <option :value="50">{{ t('common.page-size.50') }}</option>
        </select>
      </div>

      <div v-if="store.error" class="alert alert-warning py-2">{{ store.error }}</div>
      <div v-if="store.loading" class="user-admin-loading">
        <span class="spinner-border spinner-border-sm me-2"></span>
        {{ t('common.loading') }}
      </div>

      <div v-else class="table-responsive">
        <table class="table align-middle table-row-dashed fs-small gy-4 mb-0">
          <thead>
            <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
              <th class="text-center hidden-table">{{ t('board.group.list.number') }}</th>
              <th>{{ t('user.admin.list.col.account') }}</th>
              <th class="hidden-table">{{ t('log.col.role') }}</th>
              <th class="hidden-table">{{ t('user.emplym.affiliation') }}</th>
              <th class="hidden-table">{{ t('user.emplym.rank') }}</th>
              <th>{{ t('user.admin.list.col.email') }}</th>
              <th class="text-center">{{ t('common.status') }}</th>
              <th class="text-center user-admin-manage-col">{{ t('board.group.list.manage') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!store.error && !store.rows.length">
              <td colspan="8" class="text-center text-muted py-8">{{ t('user.admin.list.empty') }}</td>
            </tr>
            <tr v-for="row in store.rows" :key="row.id" class="cursor-pointer" :class="{ 'bg-light': row.isMe }" @click="onUserRowClick($event, row.id)">
              <td class="text-center hidden-table text-gray-600">{{ row.rnum }}</td>
              <td>
                <div class="user-admin-account">
                  <div class="user-admin-avatar">
                    <img v-if="row.profileImageUrl" :src="row.profileImageUrl" alt="" />
                    <i v-else class="bi bi-person-circle"></i>
                  </div>
                  <div>
                    <strong>{{ row.userNm || row.nickname || "-" }}</strong>
                    <span>{{ row.username }}</span>
                  </div>
                </div>
              </td>
              <td class="hidden-table">
                <span v-for="role in row.userRoles ?? []" :key="role.roleKey" class="badge badge-light-primary me-1">{{ role.roleName }}</span>
              </td>
              <td class="hidden-table">{{ row.teamNm || row.cmpyNm || "-" }}</td>
              <td class="hidden-table">{{ row.rankNm || "-" }}</td>
              <td>
                <div class="user-admin-ellipsis">{{ row.email || "-" }}</div>
              </td>
              <td class="text-center">
                <span class="badge" :class="isLocked(row) ? 'badge-light-danger' : 'badge-light-success'">
                  {{ isLocked(row) ? t('user.list.locked') : t('status.use') }}
                </span>
              </td>
              <td class="text-center">
                <!--begin::컨텍스트 메뉴
                  SSOT: 저널 일자·게시판 목록과 동일 Metronic data-kt-menu.
                  .table-responsive(overflow) 클리핑은 data-kt-menu-overflow="true"(body portal)로 해결한다.
                  변경 전(Bootstrap strategy:fixed): 메뉴가 여러 행에서 열린 채 겹쳤다.
                  본인 계정(row.isMe) 삭제는 disabled. 트리거 stop 금지(body 위임). 행 클릭은 메뉴 가드. 목록 렌더 후 reinit.
                -->
                <div class="d-flex justify-content-center">
                  <button
                    type="button"
                    class="btn btn-sm btn-icon btn-bg-light btn-active-color-primary"
                    data-kt-menu-trigger="click"
                    data-kt-menu-placement="bottom-end"
                    data-kt-menu-overflow="true"
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
                      <div class="menu-link flex-stack px-3" @click="emit('edit', row.id)">
                        {{ t('common.mdf') }}
                        <i class="bi bi-pencil-square fs-8"></i>
                      </div>
                    </div>
                    <div class="separator my-2"></div>
                    <div class="menu-item px-3 my-1">
                      <div
                        class="menu-link flex-stack px-3"
                        :class="row.isMe ? 'disabled text-muted' : 'text-danger'"
                        @click="!row.isMe && deleteUser(row)"
                      >
                        {{ t('common.del') }}
                        <i class="bi bi-trash p-0 fs-8" :class="row.isMe ? 'text-muted' : 'text-danger'"></i>
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
    </div>
    <div class="card-footer user-admin-footer">
      <span class="text-muted fs-8">{{ t('board.group.pagination.total-format').replace('{0}', formatNumber(store.totalElements)) }}</span>
      <div v-if="pageNumbers.length" class="pagination mb-0">
        <button type="button" class="page-link" :disabled="store.currentPage <= 0" @click="store.fetchUsers(0)">
          <i class="previous"></i>
        </button>
        <button
          v-for="page in pageNumbers"
          :key="page"
          type="button"
          class="page-link"
          :class="{ active: page === store.currentPage }"
          @click="store.fetchUsers(page)"
        >
          {{ page + 1 }}
        </button>
        <button
          type="button"
          class="page-link"
          :disabled="store.currentPage >= store.totalPages - 1"
          @click="store.fetchUsers(store.totalPages - 1)"
        >
          <i class="next"></i>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalConfirm, swalAlert } from "@/shared/utils/swal";
import { useUserAdminStore, type UserRow } from "@/features/admin/stores/userAdmin";
import { isMetronicMenuEventTarget } from "@/shared/utils/metronicReinit";

/**
 * 계정 목록 카드: 필터·정렬·페이지네이션·행 메뉴를 담당한다. 조회/페이징/페이지크기는 store 를
 * 직접 호출하고, 상세 열기·수정 진입은 detail·edit 이벤트로 상위(UserAdminPage)에 위임한다
 * (상위가 모달 상태를 소유). 삭제는 자체 처리한다. 렌더 게이트(activeTab)는 상위가 소유.
 */
const store = useUserAdminStore();
const { t } = useLocaleStore();
const emit = defineEmits<{ (e: "detail", id: number): void; (e: "edit", id: number): void }>();

const pageNumbers = computed(() => {
  if (store.totalPages <= 1) return [];
  const start = Math.max(0, store.currentPage - 2);
  const end = Math.min(store.totalPages - 1, store.currentPage + 2);
  const pages: number[] = [];
  for (let page = start; page <= end; page += 1) pages.push(page);
  return pages;
});

function formatNumber(value: number | undefined): string {
  return new Intl.NumberFormat().format(Number(value) || 0);
}

function isLocked(row: UserRow): boolean {
  return row.isLocked === true || String(row.lockedYn ?? "N").toUpperCase() === "Y";
}

function onPageSizeChange(event: Event) {
  void store.changePageSize(Number((event.target as HTMLSelectElement).value));
}

async function deleteUser(row: UserRow) {
  if (!await swalConfirm(t("user.admin.delete.confirm").replace("{username}", row.username))) return;
  try {
    await store.deleteUser(row.id);
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("user.admin.delete.failure"));
  }
}

function onUserRowClick(event: MouseEvent, id: number): void {
  if (isMetronicMenuEventTarget(event.target)) return;
  emit("detail", id);
}
</script>

<style scoped>
@import "./userAdminShared.scss";
</style>
