<template>
  <section class="card post admin-role-card">
    <div class="card-body">
      <h3 class="admin-section-title">{{ t('admin.page.section.roles') }}</h3>
      <div class="table-responsive">
        <table class="table align-middle table-row-dashed fs-small gy-4 mb-0">
          <thead>
            <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
              <th>{{ t('admin.page.roles.col.code') }}</th>
              <th>{{ t('admin.page.roles.col.name') }}</th>
              <th class="text-center">{{ t('admin.page.roles.col.sort') }}</th>
              <th class="text-center">{{ t('admin.page.roles.col.use') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!store.roles.length">
              <td colspan="4" class="text-center text-muted py-8">{{ t('admin.page.roles.empty') }}</td>
            </tr>
            <tr v-for="role in store.roles" :key="role.id">
              <td class="fw-bold text-muted">{{ role.roleKey }}</td>
              <td>
                <div class="d-flex align-items-center">
                  <i :class="roleIcon(role)" class="fs-2 me-2"></i>
                  <span :class="roleNameClass(role)" class="fw-bold">
                    <template v-if="role.roleKey === store.meta.authDevKey && role.parentRoleId != null">({{ role.parentRoleId }}) </template>
                    {{ role.roleName }}
                  </span>
                  <span class="badge ms-3" :class="roleBadgeClass(role)">{{ role.authLevel ?? "-" }}</span>
                </div>
              </td>
              <td class="text-center">{{ role.sortOrder ?? "-" }}</td>
              <td class="text-center">{{ role.useYn }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { useAdminPageStore } from "@/features/admin/stores/adminPage";
import type { RoleRow } from "@/features/admin/types/adminPage.types";

/**
 * 권한(Role) 목록 카드: 역할 코드·이름·정렬·사용여부를 읽기 전용 표로 표시한다(general 탭).
 * 렌더 게이트(activeTab === "general")는 상위 AdminPage 가 소유한다.
 */
const store = useAdminPageStore();
const { t } = useLocaleStore();

function roleIcon(role: RoleRow): string {
  if (role.roleKey === store.meta.authMngrKey) return "bi bi-person-lines-fill text-info";
  if (role.roleKey === store.meta.authDevKey) return "bi bi-person-fill-gear text-info";
  return "bi bi-people-fill text-muted";
}

function roleNameClass(role: RoleRow): string {
  return role.roleKey === store.meta.authUserKey ? "text-muted" : "text-info";
}

function roleBadgeClass(role: RoleRow): string {
  return role.roleKey === store.meta.authUserKey ? "badge-dark opacity-50" : "badge-info";
}
</script>

<style scoped>
@import "./adminCards.scss";

.admin-role-card {
  grid-column: 2;
  grid-row: 1 / span 2;
}

@media (max-width: 1200px) {
  .admin-role-card {
    display: flex;
    flex-direction: column;
  }
}
</style>
