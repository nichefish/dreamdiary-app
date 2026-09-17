<template>
  <div class="modal fade show d-block user-admin-modal-top" tabindex="-1" role="dialog" aria-modal="true">
    <div class="modal-dialog modal-xl">
      <div class="modal-content">
        <form @submit.prevent="submit">
          <div class="modal-header">
            <h5 class="modal-title">{{ store.isEdit ? t('user.admin.modal.title.edit') : t('user.admin.register') }}</h5>
            <button type="button" class="btn-close" @click="store.closeForm"></button>
          </div>
          <div class="modal-body">
            <div class="user-admin-form">
              <div class="user-admin-form-row">
                <label for="username" class="form-label required">{{ t('user.form.username') }}</label>
                <div class="user-admin-inline">
                  <input id="username" v-model.trim="store.form.username" type="text" class="form-control form-control-solid" maxlength="16" :readonly="store.isEdit" required />
                  <button v-if="!store.isEdit" type="button" class="btn btn-sm btn-light-primary" @click="checkUsername">{{ t('user.form.dup-check') }}</button>
                </div>
              </div>

              <div v-if="!store.isEdit" class="user-admin-form-row">
                <label for="password" class="form-label required">{{ t('user.form.password') }}</label>
                <input id="password" v-model="store.form.password" type="password" class="form-control form-control-solid" maxlength="20" autocomplete="new-password" required />
              </div>

              <div class="user-admin-form-row">
                <label for="nickname" class="form-label required">{{ t('user.form.nickname') }}</label>
                <input id="nickname" v-model.trim="store.form.nickname" type="text" class="form-control form-control-solid" maxlength="20" required />
              </div>

              <div class="user-admin-form-row">
                <label class="form-label required">{{ t('user.form.role') }}</label>
                <div class="user-admin-role-options">
                  <label v-for="role in store.activeRoles" :key="role.roleKey" class="form-check form-check-custom form-check-solid">
                    <input v-model="store.form.roleKeyList" class="form-check-input" type="checkbox" :value="role.roleKey" />
                    <span class="form-check-label">{{ role.roleName }}</span>
                  </label>
                </div>
              </div>

              <div class="user-admin-form-row">
                <label class="form-label required">{{ t('user.form.email') }}</label>
                <div class="user-admin-email">
                  <input v-model.trim="store.form.emailId" type="text" class="form-control form-control-solid" maxlength="64" required />
                  <span>@</span>
                  <input v-model.trim="store.form.emailDomain" type="text" class="form-control form-control-solid" maxlength="100" required />
                  <button type="button" class="btn btn-sm btn-light-primary" @click="checkEmail">{{ t('user.form.dup-check') }}</button>
                </div>
              </div>

              <div class="user-admin-form-row">
                <label for="phoneNumber" class="form-label">{{ t('user.form.contact') }}</label>
                <input id="phoneNumber" v-model.trim="store.form.phoneNumber" type="text" class="form-control form-control-solid" maxlength="20" />
              </div>

              <div class="user-admin-form-row">
                <label for="useAllowedIp" class="form-label">{{ t('user.form.allowed-ip-restrict') }}</label>
                <div>
                  <div class="form-check form-switch form-check-custom form-check-solid">
                    <input id="useAllowedIp" v-model="store.form.useAllowedIp" class="form-check-input cursor-pointer" type="checkbox" />
                    <label class="form-check-label ms-3" for="useAllowedIp">{{ store.form.useAllowedIp ? t('status.use') : t('status.unuse') }}</label>
                  </div>
                  <input
                    v-if="store.form.useAllowedIp"
                    v-model.trim="store.form.allowedIpListStr"
                    type="text"
                    class="form-control form-control-solid mt-3"
                    maxlength="500"
                    :placeholder="t('user.admin.form.ip-placeholder')"
                  />
                </div>
              </div>

              <div class="user-admin-form-row">
                <label for="content" class="form-label">{{ t('user.form.account-description') }}</label>
                <textarea id="content" v-model.trim="store.form.content" class="form-control form-control-solid" rows="4" maxlength="1000"></textarea>
              </div>

              <div class="user-admin-form-row">
                <label for="hasProfile" class="form-label">{{ t('user.admin.form.profile') }}</label>
                <div>
                  <div class="form-check form-switch form-check-custom form-check-solid">
                    <input id="hasProfile" v-model="store.form.hasProfile" class="form-check-input cursor-pointer" type="checkbox" />
                    <label class="form-check-label ms-3" for="hasProfile">{{ store.form.hasProfile ? t('user.admin.form.input.yes') : t('user.admin.form.input.no') }}</label>
                  </div>
                  <div v-if="store.form.hasProfile" class="user-admin-subform mt-3">
                    <input v-model="store.form.profile.brthdy" type="date" class="form-control form-control-solid" :aria-label="t('user.profile.birth-date')" />
                    <label class="form-check form-check-custom form-check-solid">
                      <input v-model="store.form.profile.lunarYn" class="form-check-input" type="checkbox" />
                      <span class="form-check-label">{{ t('user.profile.lunar') }}</span>
                    </label>
                    <textarea v-model.trim="store.form.profile.proflCn" class="form-control form-control-solid" rows="3" maxlength="1000" :placeholder="t('user.admin.form.profile.content.placeholder')"></textarea>
                  </div>
                </div>
              </div>

              <div class="user-admin-form-row">
                <label for="hasEmplym" class="form-label">{{ t('user.admin.form.employment') }}</label>
                <div>
                  <div class="form-check form-switch form-check-custom form-check-solid">
                    <input id="hasEmplym" v-model="store.form.hasEmplym" class="form-check-input cursor-pointer" type="checkbox" />
                    <label class="form-check-label ms-3" for="hasEmplym">{{ store.form.hasEmplym ? t('user.admin.form.input.yes') : t('user.admin.form.input.no') }}</label>
                  </div>
                  <div v-if="store.form.hasEmplym" class="user-admin-subform mt-3">
                    <input v-model.trim="store.form.emplym.userNm" type="text" class="form-control form-control-solid" maxlength="50" :placeholder="t('user.emplym.name-placeholder')" />
                    <select v-model="store.form.emplym.cmpyCd" class="form-select form-select-solid">
                      <option value="">{{ t('user.admin.form.select.company') }}</option>
                      <option v-for="opt in store.cmpyOptions" :key="opt.code" :value="opt.code">{{ opt.codeName }}</option>
                    </select>
                    <select v-model="store.form.emplym.teamCd" class="form-select form-select-solid">
                      <option value="">{{ t('user.admin.form.select.team') }}</option>
                      <option v-for="opt in store.teamOptions" :key="opt.code" :value="opt.code">{{ opt.codeName }}</option>
                    </select>
                    <select v-model="store.form.emplym.emplymCd" class="form-select form-select-solid">
                      <option value="">{{ t('user.admin.form.select.employment-type') }}</option>
                      <option v-for="opt in store.emplymOptions" :key="opt.code" :value="opt.code">{{ opt.codeName }}</option>
                    </select>
                    <select v-model="store.form.emplym.rankCd" class="form-select form-select-solid">
                      <option value="">{{ t('user.admin.form.select.rank') }}</option>
                      <option v-for="opt in store.rankOptions" :key="opt.code" :value="opt.code">{{ opt.codeName }}</option>
                    </select>
                    <div class="user-admin-email">
                      <input v-model.trim="store.form.emplym.emplymEmailId" type="text" class="form-control form-control-solid" maxlength="64" :placeholder="t('user.signup.work-email')" />
                      <span>@</span>
                      <input v-model.trim="store.form.emplym.emplymEmailDomain" type="text" class="form-control form-control-solid" maxlength="100" :placeholder="t('user.form.email-domain-placeholder')" />
                    </div>
                    <input v-model.trim="store.form.emplym.emplymPhoneNumber" type="text" class="form-control form-control-solid" maxlength="20" :placeholder="t('user.admin.form.emplym.phone.label')" />
                    <div class="user-admin-inline flex-wrap">
                      <input v-model="store.form.emplym.ecnyDt" type="date" class="form-control form-control-solid user-admin-date" :aria-label="t('user.emplym.join-date')" />
                      <label class="form-check form-check-custom form-check-solid">
                        <input v-model="store.form.emplym.apntcYn" class="form-check-input" type="checkbox" />
                        <span class="form-check-label">{{ t('user.emplym.probation.active') }}</span>
                      </label>
                      <label class="form-check form-check-custom form-check-solid">
                        <input v-model="store.form.emplym.retireYn" class="form-check-input" type="checkbox" />
                        <span class="form-check-label">{{ t('user.emplym.retired') }}</span>
                      </label>
                      <input v-if="store.form.emplym.retireYn" v-model="store.form.emplym.retireDt" type="date" class="form-control form-control-solid user-admin-date" :aria-label="t('user.emplym.retired-date')" />
                    </div>
                    <div class="user-admin-inline">
                      <input v-model.trim="store.form.emplym.acntBank" type="text" class="form-control form-control-solid" maxlength="40" :placeholder="t('user.emplym.bank')" />
                      <input v-model.trim="store.form.emplym.acntNo" type="text" class="form-control form-control-solid" maxlength="40" :placeholder="t('user.emplym.account-number')" />
                    </div>
                    <textarea v-model.trim="store.form.emplym.emplymCn" class="form-control form-control-solid" rows="3" maxlength="1000" :placeholder="t('user.admin.form.emplym.content.placeholder')"></textarea>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-sm btn-light" @click="store.closeForm">{{ t('common.close') }}</button>
            <button type="submit" class="btn btn-sm btn-primary" :disabled="store.saving">
              <span v-if="store.saving" class="spinner-border spinner-border-sm me-1"></span>
              <i v-else class="bi bi-check-lg"></i>
              {{ t('common.save') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
  <div class="modal-backdrop fade show user-admin-backdrop-top"></div>
</template>

<script setup lang="ts">
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalAlert, swalAjaxResult } from "@/shared/utils/swal";
import { useUserAdminStore } from "@/features/admin/stores/userAdmin";

/**
 * 사용자 등록/수정 폼 모달(store.formOpen 구동). 필드는 store.form 에 직접 v-model 바인딩하고
 * 저장·아이디/이메일 중복확인은 store 액션을 호출한다. 렌더 게이트(store.formOpen)와 닫기
 * (store.closeForm)는 store 를 통해 처리하므로 props/emit 없이 자기완결이다.
 */
const store = useUserAdminStore();
const { t } = useLocaleStore();

async function submit() {
  if (!store.form.username.trim() || !store.form.nickname.trim()) {
    void swalAlert(t("user.admin.validate.username-nickname.required"));
    return;
  }
  if (!store.isEdit && !store.form.password.trim()) {
    void swalAlert(t("user.admin.validate.password.required"));
    return;
  }
  if (!store.form.emailId.trim() || !store.form.emailDomain.trim()) {
    void swalAlert(t("user.admin.validate.email.required"));
    return;
  }
  if (!store.form.roleKeyList.length) {
    void swalAlert(t("user.admin.validate.role.required"));
    return;
  }
  try {
    await store.submit();
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("user.admin.save.failure"));
  }
}

async function checkUsername() {
  if (!store.form.username.trim()) {
    void swalAlert(t("user.admin.validate.username.required"));
    return;
  }
  const result = await store.usernameDuplicateCheck(store.form.username.trim());
  void swalAjaxResult({
    rslt: result.ok,
    message: result.message,
    successFallback: t("user.admin.dup-check.username.usable"),
    failureFallback: t("user.admin.dup-check.username.duplicated"),
  });
}

async function checkEmail() {
  const email = `${store.form.emailId.trim()}@${store.form.emailDomain.trim()}`;
  if (!store.form.emailId.trim() || !store.form.emailDomain.trim()) {
    void swalAlert(t("user.admin.validate.email.required"));
    return;
  }
  const result = await store.emailDuplicateCheck(email);
  void swalAjaxResult({
    rslt: result.ok,
    message: result.message,
    successFallback: t("user.admin.dup-check.email.usable"),
    failureFallback: t("user.admin.dup-check.email.duplicated"),
  });
}
</script>

<style scoped>
@import "./userAdminShared.scss";
</style>
