<template>
  <form @submit.prevent="save">
    <div class="d-flex justify-content-end gap-2 mb-4">
      <button v-if="!editing" type="button" class="btn btn-sm btn-light-primary" @click="beginEdit">
        <i class="bi bi-pencil me-1"></i>{{ t("common.edit") }}
      </button>
      <template v-else>
        <button type="button" class="btn btn-sm btn-light" :disabled="store.saving" @click="cancelEdit">
          {{ t("common.cancel") }}
        </button>
        <button type="submit" class="btn btn-sm btn-primary" :disabled="store.saving">
          <span v-if="store.saving" class="spinner-border spinner-border-sm me-1"></span>{{ t("common.save") }}
        </button>
      </template>
    </div>

    <div class="user-my-grid">
      <section class="user-my-section">
        <h4>{{ t("user.my.section.account") }}</h4>
        <dl>
          <div>
            <dt>{{ t("user.form.nickname") }}</dt>
            <dd v-if="editing">
              <input v-model.trim="form.nickname" type="text" class="form-control form-control-sm" maxlength="20" required />
            </dd>
            <dd v-else>{{ fallback(user.nickname) }}</dd>
          </div>
          <div><dt>{{ t("user.admin.list.col.email") }}</dt><dd>{{ fallback(user.email) }}</dd></div>
          <div>
            <dt>{{ t("user.admin.detail.col.contact") }}</dt>
            <dd v-if="editing">
              <input v-model.trim="form.phoneNumber" type="text" class="form-control form-control-sm" maxlength="20" />
            </dd>
            <dd v-else>{{ fallback(user.phoneNumber) }}</dd>
          </div>
        </dl>
      </section>

      <section class="user-my-section">
        <h4>{{ t("user.admin.detail.section.profile") }}</h4>
        <dl>
          <div>
            <dt>{{ t("user.profile.birth-date") }}</dt>
            <dd v-if="editing">
              <div class="user-my-birth-field">
                <input v-model="form.brthdy" type="date" class="form-control form-control-sm" :max="maxBirthDate" />
                <div class="form-check form-switch form-check-custom form-check-solid">
                  <input
                    id="user-my-lunar-yn"
                    v-model="form.lunarYn"
                    type="checkbox"
                    role="switch"
                    class="form-check-input"
                    true-value="Y"
                    false-value="N"
                  />
                  <label class="form-check-label" for="user-my-lunar-yn">
                    {{ form.lunarYn === "Y" ? t("user.profile.lunar") : t("user.profile.solar") }}
                  </label>
                </div>
              </div>
            </dd>
            <dd v-else>
              <template v-if="user.profile?.brthdy">
                {{ user.profile.brthdy }}
                ({{ user.profile.lunarYn === "Y" ? t("user.profile.lunar") : t("user.profile.solar") }})
              </template>
              <template v-else>-</template>
            </dd>
          </div>
          <div>
            <dt>{{ t("user.my.detail.intro") }}</dt>
            <dd v-if="editing">
              <textarea v-model.trim="form.proflCn" class="form-control form-control-sm" rows="5" maxlength="4000"></textarea>
            </dd>
            <dd v-else class="user-my-preline">{{ fallback(user.profile?.proflCn) }}</dd>
          </div>
        </dl>
      </section>

      <section class="user-my-section user-my-section--wide">
        <h4>{{ t("user.my.section.employment") }}</h4>
        <dl>
          <div><dt>{{ t("user.emplym.name-placeholder") }}</dt><dd>{{ fallback(user.emplym?.userNm) }}</dd></div>
          <div><dt>{{ t("user.emplym.affiliation") }}</dt><dd>{{ affiliationText }}</dd></div>
          <div><dt>{{ t("user.emplym.rank") }}</dt><dd>{{ rankText }}</dd></div>
          <div><dt>{{ t("user.my.detail.join-retire") }}</dt><dd>{{ joinRetireText }}</dd></div>
          <div><dt>{{ t("user.signup.work-email") }}</dt><dd>{{ fallback(user.emplym?.emplymEmail) }}</dd></div>
          <div><dt>{{ t("user.admin.form.emplym.phone.label") }}</dt><dd>{{ fallback(user.emplym?.emplymPhoneNumber) }}</dd></div>
          <div><dt>{{ t("user.my.detail.payroll-account") }}</dt><dd>{{ payrollText }}</dd></div>
        </dl>
      </section>
    </div>
  </form>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { useAuthStore } from "@/shared/auth/stores/auth";
import { swalAlert, swalConfirm } from "@/shared/utils/swal";
import {
  useUserMyStore,
  type UserMyUpdatePayload,
} from "@/features/user/stores/userMy";

const store = useUserMyStore();
const authStore = useAuthStore();
const { t } = useLocaleStore();
const user = computed(() => store.user);
const editing = ref(false);
type UserMyForm = Omit<UserMyUpdatePayload, "phoneNumber" | "brthdy" | "proflCn"> & {
  phoneNumber: string;
  brthdy: string;
  proflCn: string;
};
const form = reactive<UserMyForm>({
  nickname: "",
  phoneNumber: "",
  brthdy: "",
  lunarYn: "N",
  proflCn: "",
});
const today = new Date();
const maxBirthDate = [
  today.getFullYear(),
  String(today.getMonth() + 1).padStart(2, "0"),
  String(today.getDate()).padStart(2, "0"),
].join("-");
const compact = (values: Array<string | null | undefined>) => values.map((value) => (value ?? "").trim()).filter(Boolean);
const fallback = (value: string | null | undefined) => value?.trim() || "-";
const affiliationText = computed(() => compact([user.value.emplym?.cmpyNm, user.value.emplym?.teamNm, user.value.emplym?.emplymNm]).join(" / ") || "-");
const rankText = computed(() => {
  const rank = user.value.emplym?.rankNm || "-";
  return user.value.emplym?.apntcYn === "Y" ? `${rank} (${t("user.emplym.probation.active")})` : rank;
});
const joinRetireText = computed(() => user.value.emplym?.retireYn === "Y"
  ? `${user.value.emplym?.ecnyDt || "-"} / ${t("user.emplym.retired")} ${user.value.emplym?.retireDt || "-"}`
  : user.value.emplym?.ecnyDt || "-");
const payrollText = computed(() => compact([user.value.emplym?.acntBank, user.value.emplym?.acntNo]).join(" / ") || "-");

function resetForm() {
  const current: UserMyForm = {
    nickname: user.value.nickname ?? "",
    phoneNumber: user.value.phoneNumber ?? "",
    brthdy: user.value.profile?.brthdy ?? "",
    lunarYn: user.value.profile?.lunarYn === "Y" ? "Y" : "N",
    proflCn: user.value.profile?.proflCn ?? "",
  };
  Object.assign(form, current);
}

function beginEdit() {
  resetForm();
  editing.value = true;
}

function cancelEdit() {
  resetForm();
  editing.value = false;
}

async function save() {
  if (!form.nickname.trim()) {
    void swalAlert(t("user.signup.nickname.required"));
    return;
  }
  if (!await swalConfirm(t("user.my.profile.update.confirm"))) return;
  try {
    await store.updateMyInfo({
      nickname: form.nickname.trim(),
      phoneNumber: form.phoneNumber.trim() || null,
      brthdy: form.brthdy || null,
      lunarYn: form.lunarYn,
      proflCn: form.proflCn.trim() || null,
    });
    await authStore.verifyAuth({ force: true });
    resetForm();
    editing.value = false;
    void swalAlert(t("user.my.profile.update.success"));
  } catch (error) {
    void swalAlert(error instanceof Error ? error.message : t("user.my.profile.update.failure"));
  }
}
</script>

<style scoped>
.user-my-grid { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:1.5rem; }
.user-my-section--wide { grid-column:1/-1; }
.user-my-section h4 { margin-bottom:1rem; font-size:1rem; font-weight:700; }
.user-my-section dl { display:grid; gap:.75rem; margin:0; }
.user-my-section dl>div { display:grid; grid-template-columns:140px minmax(0,1fr); gap:1rem; padding-bottom:.75rem; border-bottom:1px dashed var(--bs-gray-300); }
.user-my-section dt { color:var(--bs-gray-700); font-weight:700; }
.user-my-section dd { margin:0; min-width:0; word-break:break-word; }
.user-my-preline { white-space:pre-wrap; }
.user-my-birth-field { display:flex; align-items:center; gap:1rem; }
.user-my-birth-field>.form-control { max-width:220px; }
@media (max-width:768px) {
  .user-my-grid,.user-my-section dl>div { grid-template-columns:1fr; }
  .user-my-birth-field { align-items:flex-start; flex-direction:column; }
}
</style>
