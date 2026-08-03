<template>
  <div class="user-group-page">
    <div class="d-flex flex-column-fluid justify-content-end align-items-start align-items-xl-center gap-4 w-100">
      <div class="d-flex align-items-center flex-shrink-0 pe-5 mt-3 mb-1 gap-2">
        <button type="button" class="btn btn-sm btn-primary text-nowrap" @click="store.openCreate">
          <i class="bi bi-plus-lg"></i>
          {{ t("common.register") }}
        </button>
      </div>
    </div>

    <div class="card post" style="margin-top: 0 !important">
      <div class="card-body">
        <div class="d-flex flex-wrap gap-2 mb-4">
          <input
            v-model.trim="store.keyword"
            type="search"
            class="form-control form-control-solid w-auto"
            maxlength="200"
            :placeholder="t('admin.user-group.search.placeholder')"
            @keyup.enter="store.fetchList(0)"
          />
          <button type="button" class="btn btn-sm btn-light-primary" :disabled="store.loading" @click="store.fetchList(0)">
            <i class="bi bi-search"></i>
          </button>
        </div>

        <div v-if="store.error" class="alert alert-warning py-2">{{ store.error }}</div>
        <div v-if="store.loading" class="py-6 text-muted">
          <span class="spinner-border spinner-border-sm me-2"></span>
          {{ t("common.loading") }}
        </div>

        <div v-else class="table-responsive">
          <table class="table align-middle table-row-dashed fs-small gy-4 mb-0">
            <thead>
              <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                <th>{{ t("admin.user-group.list.key") }}</th>
                <th>{{ t("admin.user-group.list.name") }}</th>
                <th class="text-center">{{ t("admin.user-group.list.members") }}</th>
                <th class="text-center">{{ t("common.use") }}</th>
                <th class="text-center">{{ t("common.manage") }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!store.rows.length">
                <td colspan="5" class="text-center text-muted py-8">{{ t("admin.user-group.empty") }}</td>
              </tr>
              <tr v-for="row in store.rows" :key="row.id">
                <td>{{ row.groupKey }}</td>
                <td>
                  <div>{{ row.groupName }}</div>
                  <div v-if="row.description" class="text-muted fs-8">{{ row.description }}</div>
                </td>
                <td class="text-center">{{ row.memberCount ?? 0 }}</td>
                <td class="text-center">{{ row.useYn === "Y" ? t("common.use.y") : t("common.use.n") }}</td>
                <td class="text-center">
                  <button type="button" class="btn btn-sm btn-light-primary me-1" @click="store.openEdit(row.id)">
                    {{ t("common.edit") }}
                  </button>
                  <button type="button" class="btn btn-sm btn-light-danger" @click="store.remove(row.id)">
                    {{ t("common.delete") }}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-if="store.totalPages > 1" class="d-flex justify-content-end gap-2 mt-4">
          <button
            type="button"
            class="btn btn-sm btn-light"
            :disabled="store.currentPage <= 0 || store.loading"
            @click="store.fetchList(store.currentPage - 1)"
          >
            {{ t("common.prev") }}
          </button>
          <span class="align-self-center fs-7 text-muted">{{ store.currentPage + 1 }} / {{ store.totalPages }}</span>
          <button
            type="button"
            class="btn btn-sm btn-light"
            :disabled="store.currentPage + 1 >= store.totalPages || store.loading"
            @click="store.fetchList(store.currentPage + 1)"
          >
            {{ t("common.next") }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="store.modalOpen" class="modal fade show d-block" tabindex="-1" role="dialog" aria-modal="true">
      <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              {{ store.isEdit ? t("admin.user-group.modal.edit") : t("admin.user-group.modal.create") }}
            </h5>
            <button type="button" class="btn-close" @click="store.closeModal"></button>
          </div>
          <div class="modal-body">
            <div v-if="store.detailLoading" class="py-6 text-muted">{{ t("common.loading") }}</div>
            <template v-else>
              <div class="mb-3">
                <label class="form-label required">{{ t("admin.user-group.form.key") }}</label>
                <input v-model.trim="store.form.groupKey" type="text" class="form-control" maxlength="50" :disabled="store.isEdit" />
              </div>
              <div class="mb-3">
                <label class="form-label required">{{ t("admin.user-group.form.name") }}</label>
                <input v-model.trim="store.form.groupName" type="text" class="form-control" maxlength="100" />
              </div>
              <div class="mb-3">
                <label class="form-label">{{ t("admin.user-group.form.description") }}</label>
                <textarea v-model.trim="store.form.description" class="form-control" rows="2" maxlength="500"></textarea>
              </div>
              <div class="mb-3 d-flex gap-3 align-items-center">
                <label class="form-label mb-0">{{ t("common.use") }}</label>
                <div class="form-check form-switch">
                  <input
                    class="form-check-input"
                    type="checkbox"
                    :checked="store.form.useYn === 'Y'"
                    @change="store.form.useYn = ($event.target as HTMLInputElement).checked ? 'Y' : 'N'"
                  />
                </div>
              </div>

              <div class="mb-4">
                <label class="form-label">{{ t("admin.user-group.form.permissions") }}</label>
                <div class="border rounded p-3" style="max-height: 220px; overflow: auto">
                  <div v-for="perm in store.permissions" :key="perm.permKey" class="form-check">
                    <input
                      class="form-check-input"
                      type="checkbox"
                      :id="'perm-' + perm.permKey"
                      :checked="store.form.permissionKeys.includes(perm.permKey)"
                      @change="store.togglePermission(perm.permKey)"
                    />
                    <label class="form-check-label" :for="'perm-' + perm.permKey">
                      <span class="fw-semibold">{{ perm.permKey }}</span>
                      <span v-if="perm.permName" class="text-muted ms-2">{{ perm.permName }}</span>
                    </label>
                  </div>
                </div>
              </div>

              <div class="mb-2">
                <label class="form-label">{{ t("admin.user-group.form.members") }}</label>
                <div class="d-flex gap-2">
                  <input
                    v-model.trim="store.form.memberInput"
                    type="text"
                    class="form-control"
                    maxlength="20"
                    :placeholder="t('admin.user-group.form.member-placeholder')"
                    @keyup.enter="store.addMember"
                  />
                  <button type="button" class="btn btn-sm btn-light-primary text-nowrap" @click="store.addMember">
                    {{ t("common.add") }}
                  </button>
                </div>
                <div class="d-flex flex-wrap gap-2 mt-2">
                  <span v-for="username in store.form.memberUsernames" :key="username" class="badge badge-light-primary">
                    {{ username }}
                    <button type="button" class="btn btn-sm p-0 ms-1" @click="store.removeMember(username)">×</button>
                  </span>
                </div>
              </div>
            </template>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-light" @click="store.closeModal">{{ t("common.cancel") }}</button>
            <button type="button" class="btn btn-primary" :disabled="store.saving || store.detailLoading" @click="store.save">
              <span v-if="store.saving" class="spinner-border spinner-border-sm me-1"></span>
              {{ t("common.save") }}
            </button>
          </div>
        </div>
      </div>
    </div>
    <div v-if="store.modalOpen" class="modal-backdrop fade show"></div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { useUserGroupStore } from "@/features/admin/stores/userGroup";

const { t } = useLocaleStore();
const store = useUserGroupStore();

onMounted(() => {
  void store.fetchList(0);
});
</script>
