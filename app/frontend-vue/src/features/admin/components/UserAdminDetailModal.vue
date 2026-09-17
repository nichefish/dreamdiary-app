<template>
  <div class="modal fade show d-block" tabindex="-1" role="dialog" aria-modal="true">
    <div class="modal-dialog modal-xl">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">{{ t('user.admin.detail.title') }}</h5>
          <button type="button" class="btn-close" @click="store.closeDetail"></button>
        </div>
        <div class="modal-body">
          <div v-if="store.detailLoading" class="user-admin-loading">
            <span class="spinner-border spinner-border-sm me-2"></span>
            {{ t('common.loading') }}
          </div>
          <template v-else-if="store.detail">
            <div class="user-admin-detail-head">
              <div class="user-admin-account">
                <div class="user-admin-avatar lg">
                  <img v-if="store.detail.profileImageUrl" :src="store.detail.profileImageUrl" alt="" />
                  <i v-else class="bi bi-person-circle"></i>
                </div>
                <div>
                  <h3>{{ store.detail.nickname || store.detail.userNm || store.detail.username }}</h3>
                  <span>{{ store.detail.username }}</span>
                </div>
              </div>
              <div class="user-admin-actions">
                <button type="button" class="btn btn-sm btn-light-warning" @click="passwordReset(store.detail.id)">
                  <i class="bi bi-key"></i>
                  {{ t('user.admin.detail.reset-password') }}
                </button>
                <button type="button" class="btn btn-sm btn-light-primary" @click="emit('edit', store.detail.id)">
                  <i class="bi bi-pencil-square"></i>
                  {{ t('common.mdf') }}
                </button>
              </div>
            </div>

            <div class="user-admin-detail-grid">
              <div>
                <span>{{ t('log.col.role') }}</span>
                <strong>{{ roleNames(store.detail) }}</strong>
              </div>
              <div>
                <span>{{ t('user.admin.list.col.email') }}</span>
                <strong>{{ store.detail.email || "-" }}</strong>
              </div>
              <div>
                <span>{{ t('user.admin.detail.col.contact') }}</span>
                <strong>{{ store.detail.phoneNumber || "-" }}</strong>
              </div>
              <div>
                <span>{{ t('common.status') }}</span>
                <strong>{{ isLocked(store.detail) ? t('user.list.locked') : t('status.use') }}</strong>
              </div>
              <div>
                <span>{{ t('user.emplym.affiliation') }}</span>
                <strong>{{ [store.detail.cmpyNm, store.detail.teamNm].filter(Boolean).join(" / ") || "-" }}</strong>
              </div>
              <div>
                <span>{{ t('user.emplym.rank') }}</span>
                <strong>{{ store.detail.rankNm || "-" }}</strong>
              </div>
              <div>
                <span>{{ t('user.admin.detail.col.allowed-ip') }}</span>
                <strong>{{ store.detail.useAllowedIp ? allowedIps(store.detail) || t('status.use') : t('status.unuse') }}</strong>
              </div>
              <div>
                <span>{{ t('common.reg') }}</span>
                <strong>{{ [store.detail.createdBy, store.detail.createdAt].filter(Boolean).join(" / ") || "-" }}</strong>
              </div>
            </div>
            <div class="user-admin-detail-block">
              <h4>{{ t('user.form.account-description') }}</h4>
              <pre>{{ store.detail.content || "-" }}</pre>
            </div>
            <div class="user-admin-detail-split">
              <div class="user-admin-detail-block">
                <h4>{{ t('user.admin.detail.section.profile') }}</h4>
                <pre>{{ profileText(store.detail) }}</pre>
              </div>
              <div class="user-admin-detail-block">
                <h4>{{ t('user.admin.detail.section.employment') }}</h4>
                <pre>{{ emplymText(store.detail) }}</pre>
              </div>
            </div>
          </template>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-sm btn-light" @click="store.closeDetail">{{ t('common.close') }}</button>
        </div>
      </div>
    </div>
  </div>
  <div class="modal-backdrop fade show"></div>
</template>

<script setup lang="ts">
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalConfirm, swalAlert, swalFire } from "@/shared/utils/swal";
import { useUserAdminStore, type UserRow } from "@/features/admin/stores/userAdmin";

/**
 * 사용자 상세 모달: 계정 프로필·소속·권한·허용 IP 등을 읽기 전용으로 표시한다(store.detailOpen 구동).
 * 렌더 게이트(store.detailOpen)는 상위 UserAdminPage 가 소유하고, 수정 진입은 edit 이벤트로 위임한다.
 */
const store = useUserAdminStore();
const { t } = useLocaleStore();
const emit = defineEmits<{ (e: "edit", id: number): void }>();

function isLocked(row: UserRow): boolean {
  return row.isLocked === true || String(row.lockedYn ?? "N").toUpperCase() === "Y";
}

function roleNames(row: UserRow): string {
  return (row.userRoles ?? []).map((role) => role.roleName || role.roleKey).join(", ") || "-";
}

function allowedIps(row: UserRow): string {
  return (row.allowedIpList ?? []).map((item) => item.allowedIp).join(", ");
}

function profileText(row: UserRow): string {
  if (!row.profile) return "-";
  return [
    row.profile.brthdy ? `${t("user.profile.birth-date")}: ${row.profile.brthdy}${row.profile.lunarYn === "Y" ? ` (${t("user.profile.lunar")})` : ""}` : "",
    row.profile.proflCn || "",
  ].filter(Boolean).join("\n") || "-";
}

function emplymText(row: UserRow): string {
  if (!row.emplym) return "-";
  return [
    row.emplym.userNm ? `${t("user.emplym.name-placeholder")}: ${row.emplym.userNm}` : "",
    [row.emplym.cmpyNm, row.emplym.teamNm, row.emplym.rankNm].filter(Boolean).join(" / "),
    row.emplym.emplymEmail ? `${t("user.signup.work-email")}: ${row.emplym.emplymEmail}` : "",
    row.emplym.emplymPhoneNumber ? `${t("user.admin.form.emplym.phone.label")}: ${row.emplym.emplymPhoneNumber}` : "",
    row.emplym.ecnyDt ? `${t("user.emplym.join-date")}: ${row.emplym.ecnyDt}` : "",
    row.emplym.retireYn === "Y" ? `${t("user.emplym.retired-date")}: ${row.emplym.retireDt || "-"}` : "",
    row.emplym.acntBank || row.emplym.acntNo ? `${t("user.admin.info.payroll-account")}: ${[row.emplym.acntBank, row.emplym.acntNo].filter(Boolean).join(" ")}` : "",
    row.emplym.emplymCn || "",
  ].filter(Boolean).join("\n") || "-";
}

async function passwordReset(id: number) {
  if (!await swalConfirm(t("user.admin.reset-password.confirm"))) return;
  try {
    void swalFire({ icon: "success", text: await store.passwordReset(id) });
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("user.admin.reset-password.failure"));
  }
}
</script>

<style scoped>
@import "./userAdminShared.scss";
</style>
