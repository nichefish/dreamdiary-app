<template>
  <div class="user-admin-page">
    <div class="user-admin-toolbar">
      <div class="user-admin-actions">
        <button type="button" class="btn btn-sm btn-light-primary" :disabled="store.loading" @click="store.fetchUsers(store.currentPage)">
          <i class="bi bi-arrow-clockwise"></i>
        </button>
        <button type="button" class="btn btn-sm btn-primary" @click="store.openCreate">
          <i class="bi bi-plus-lg"></i>
          계정 등록
        </button>
      </div>
    </div>

    <div class="card post">
      <div class="card-body">
        <div class="user-admin-listbar">
          <div class="user-admin-search">
            <input
              v-model.trim="store.keyword"
              type="search"
              class="form-control form-control-solid"
              maxlength="200"
              placeholder="아이디 검색"
              @keyup.enter="store.fetchUsers(0)"
            />
            <select v-model="store.roleKey" class="form-select form-select-solid user-admin-role-filter">
              <option value="">전체 권한</option>
              <option v-for="role in store.activeRoles" :key="role.roleKey" :value="role.roleKey">{{ role.roleName }}</option>
            </select>
            <button type="button" class="btn btn-sm btn-light-primary" :disabled="store.loading" @click="store.fetchUsers(0)">
              <i class="bi bi-search"></i>
            </button>
          </div>
          <select :value="store.pageSize" class="form-select form-select-solid user-admin-page-size" @change="onPageSizeChange">
            <option :value="10">10개</option>
            <option :value="25">25개</option>
            <option :value="50">50개</option>
          </select>
        </div>

        <div v-if="store.error" class="alert alert-warning py-2">{{ store.error }}</div>
        <div v-if="store.loading" class="user-admin-loading">
          <span class="spinner-border spinner-border-sm me-2"></span>
          불러오는 중
        </div>

        <div v-else class="table-responsive">
          <table class="table align-middle table-row-dashed fs-small gy-4 mb-0">
            <thead>
              <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 text-muted">
                <th class="text-center hidden-table">번호</th>
                <th>계정</th>
                <th class="hidden-table">권한</th>
                <th class="hidden-table">소속</th>
                <th class="hidden-table">직급</th>
                <th>이메일</th>
                <th class="text-center">상태</th>
                <th class="text-center user-admin-manage-col">관리</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!store.rows.length">
                <td colspan="8" class="text-center text-muted py-8">조회된 계정이 없습니다.</td>
              </tr>
              <tr v-for="row in store.rows" :key="row.id" class="cursor-pointer" :class="{ 'bg-light': row.isMe }" @click="openDetail(row.id)">
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
                    {{ isLocked(row) ? "잠김" : "사용" }}
                  </span>
                </td>
                <td class="text-center" @click.stop>
                  <div class="user-admin-actions justify-content-center">
                    <button type="button" class="btn btn-sm btn-icon btn-light-primary" title="수정" @click="openEdit(row.id)">
                      <i class="bi bi-pencil-square"></i>
                    </button>
                    <button type="button" class="btn btn-sm btn-icon btn-light-danger" title="삭제" :disabled="row.isMe" @click="deleteUser(row)">
                      <i class="bi bi-trash"></i>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div class="card-footer user-admin-footer">
        <span class="text-muted fs-8">총 {{ formatNumber(store.totalElements) }}건</span>
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

    <template v-if="store.detailOpen">
      <div class="modal fade show d-block" tabindex="-1" role="dialog" aria-modal="true">
        <div class="modal-dialog modal-xl">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">계정 상세</h5>
              <button type="button" class="btn-close" @click="store.closeDetail"></button>
            </div>
            <div class="modal-body">
              <div v-if="store.detailLoading" class="user-admin-loading">
                <span class="spinner-border spinner-border-sm me-2"></span>
                불러오는 중
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
                      비밀번호 초기화
                    </button>
                    <button type="button" class="btn btn-sm btn-light-primary" @click="openEdit(store.detail.id)">
                      <i class="bi bi-pencil-square"></i>
                      수정
                    </button>
                  </div>
                </div>

                <div class="user-admin-detail-grid">
                  <div>
                    <span>권한</span>
                    <strong>{{ roleNames(store.detail) }}</strong>
                  </div>
                  <div>
                    <span>이메일</span>
                    <strong>{{ store.detail.email || "-" }}</strong>
                  </div>
                  <div>
                    <span>연락처</span>
                    <strong>{{ store.detail.phoneNumber || "-" }}</strong>
                  </div>
                  <div>
                    <span>상태</span>
                    <strong>{{ isLocked(store.detail) ? "잠김" : "사용" }}</strong>
                  </div>
                  <div>
                    <span>소속</span>
                    <strong>{{ [store.detail.cmpyNm, store.detail.teamNm].filter(Boolean).join(" / ") || "-" }}</strong>
                  </div>
                  <div>
                    <span>직급</span>
                    <strong>{{ store.detail.rankNm || "-" }}</strong>
                  </div>
                  <div>
                    <span>접속 IP 제한</span>
                    <strong>{{ store.detail.useAllowedIp ? allowedIps(store.detail) || "사용" : "미사용" }}</strong>
                  </div>
                  <div>
                    <span>등록</span>
                    <strong>{{ [store.detail.createdBy, store.detail.createdAt].filter(Boolean).join(" / ") || "-" }}</strong>
                  </div>
                </div>
                <div class="user-admin-detail-block">
                  <h4>계정 설명</h4>
                  <pre>{{ store.detail.content || "-" }}</pre>
                </div>
                <div class="user-admin-detail-split">
                  <div class="user-admin-detail-block">
                    <h4>프로필</h4>
                    <pre>{{ profileText(store.detail) }}</pre>
                  </div>
                  <div class="user-admin-detail-block">
                    <h4>인사정보</h4>
                    <pre>{{ emplymText(store.detail) }}</pre>
                  </div>
                </div>
              </template>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-sm btn-light" @click="store.closeDetail">닫기</button>
            </div>
          </div>
        </div>
      </div>
      <div class="modal-backdrop fade show"></div>
    </template>

    <template v-if="store.formOpen">
      <div class="modal fade show d-block user-admin-modal-top" tabindex="-1" role="dialog" aria-modal="true">
        <div class="modal-dialog modal-xl">
          <div class="modal-content">
            <form @submit.prevent="submit">
              <div class="modal-header">
                <h5 class="modal-title">{{ store.isEdit ? "계정 수정" : "계정 등록" }}</h5>
                <button type="button" class="btn-close" @click="store.closeForm"></button>
              </div>
              <div class="modal-body">
                <div class="user-admin-form">
                  <div class="user-admin-form-row">
                    <label for="username" class="form-label required">아이디</label>
                    <div class="user-admin-inline">
                      <input id="username" v-model.trim="store.form.username" type="text" class="form-control form-control-solid" maxlength="16" :readonly="store.isEdit" required />
                      <button v-if="!store.isEdit" type="button" class="btn btn-sm btn-light-primary" @click="checkUsername">중복확인</button>
                    </div>
                  </div>

                  <div v-if="!store.isEdit" class="user-admin-form-row">
                    <label for="password" class="form-label required">비밀번호</label>
                    <input id="password" v-model="store.form.password" type="password" class="form-control form-control-solid" maxlength="20" autocomplete="new-password" required />
                  </div>

                  <div class="user-admin-form-row">
                    <label for="nickname" class="form-label required">표시이름</label>
                    <input id="nickname" v-model.trim="store.form.nickname" type="text" class="form-control form-control-solid" maxlength="20" required />
                  </div>

                  <div class="user-admin-form-row">
                    <label class="form-label required">권한</label>
                    <div class="user-admin-role-options">
                      <label v-for="role in store.activeRoles" :key="role.roleKey" class="form-check form-check-custom form-check-solid">
                        <input v-model="store.form.roleKeyList" class="form-check-input" type="checkbox" :value="role.roleKey" />
                        <span class="form-check-label">{{ role.roleName }}</span>
                      </label>
                    </div>
                  </div>

                  <div class="user-admin-form-row">
                    <label class="form-label required">이메일</label>
                    <div class="user-admin-email">
                      <input v-model.trim="store.form.emailId" type="text" class="form-control form-control-solid" maxlength="64" required />
                      <span>@</span>
                      <input v-model.trim="store.form.emailDomain" type="text" class="form-control form-control-solid" maxlength="100" required />
                      <button type="button" class="btn btn-sm btn-light-primary" @click="checkEmail">중복확인</button>
                    </div>
                  </div>

                  <div class="user-admin-form-row">
                    <label for="phoneNumber" class="form-label">연락처</label>
                    <input id="phoneNumber" v-model.trim="store.form.phoneNumber" type="text" class="form-control form-control-solid" maxlength="20" />
                  </div>

                  <div class="user-admin-form-row">
                    <label for="useAllowedIp" class="form-label">접속 IP 제한</label>
                    <div>
                      <div class="form-check form-switch form-check-custom form-check-solid">
                        <input id="useAllowedIp" v-model="store.form.useAllowedIp" class="form-check-input cursor-pointer" type="checkbox" />
                        <label class="form-check-label ms-3" for="useAllowedIp">{{ store.form.useAllowedIp ? "사용" : "미사용" }}</label>
                      </div>
                      <input
                        v-if="store.form.useAllowedIp"
                        v-model.trim="store.form.allowedIpListStr"
                        type="text"
                        class="form-control form-control-solid mt-3"
                        maxlength="500"
                        placeholder="쉼표로 구분"
                      />
                    </div>
                  </div>

                  <div class="user-admin-form-row">
                    <label for="content" class="form-label">계정 설명</label>
                    <textarea id="content" v-model.trim="store.form.content" class="form-control form-control-solid" rows="4" maxlength="1000"></textarea>
                  </div>

                  <div class="user-admin-form-row">
                    <label for="hasProfile" class="form-label">프로필</label>
                    <div>
                      <div class="form-check form-switch form-check-custom form-check-solid">
                        <input id="hasProfile" v-model="store.form.hasProfile" class="form-check-input cursor-pointer" type="checkbox" />
                        <label class="form-check-label ms-3" for="hasProfile">{{ store.form.hasProfile ? "입력" : "미입력" }}</label>
                      </div>
                      <div v-if="store.form.hasProfile" class="user-admin-subform mt-3">
                        <input v-model="store.form.profile.brthdy" type="date" class="form-control form-control-solid" aria-label="생년월일" />
                        <label class="form-check form-check-custom form-check-solid">
                          <input v-model="store.form.profile.lunarYn" class="form-check-input" type="checkbox" />
                          <span class="form-check-label">음력</span>
                        </label>
                        <textarea v-model.trim="store.form.profile.proflCn" class="form-control form-control-solid" rows="3" maxlength="1000" placeholder="프로필 설명"></textarea>
                      </div>
                    </div>
                  </div>

                  <div class="user-admin-form-row">
                    <label for="hasEmplym" class="form-label">인사정보</label>
                    <div>
                      <div class="form-check form-switch form-check-custom form-check-solid">
                        <input id="hasEmplym" v-model="store.form.hasEmplym" class="form-check-input cursor-pointer" type="checkbox" />
                        <label class="form-check-label ms-3" for="hasEmplym">{{ store.form.hasEmplym ? "입력" : "미입력" }}</label>
                      </div>
                      <div v-if="store.form.hasEmplym" class="user-admin-subform mt-3">
                        <input v-model.trim="store.form.emplym.userNm" type="text" class="form-control form-control-solid" maxlength="50" placeholder="이름" />
                        <select v-model="store.form.emplym.cmpyCd" class="form-select form-select-solid">
                          <option value="">회사 선택</option>
                          <option v-for="opt in store.cmpyOptions" :key="opt.code" :value="opt.code">{{ opt.codeName }}</option>
                        </select>
                        <select v-model="store.form.emplym.teamCd" class="form-select form-select-solid">
                          <option value="">팀 선택</option>
                          <option v-for="opt in store.teamOptions" :key="opt.code" :value="opt.code">{{ opt.codeName }}</option>
                        </select>
                        <select v-model="store.form.emplym.emplymCd" class="form-select form-select-solid">
                          <option value="">재직구분 선택</option>
                          <option v-for="opt in store.emplymOptions" :key="opt.code" :value="opt.code">{{ opt.codeName }}</option>
                        </select>
                        <select v-model="store.form.emplym.rankCd" class="form-select form-select-solid">
                          <option value="">직급 선택</option>
                          <option v-for="opt in store.rankOptions" :key="opt.code" :value="opt.code">{{ opt.codeName }}</option>
                        </select>
                        <div class="user-admin-email">
                          <input v-model.trim="store.form.emplym.emplymEmailId" type="text" class="form-control form-control-solid" maxlength="64" placeholder="업무 이메일" />
                          <span>@</span>
                          <input v-model.trim="store.form.emplym.emplymEmailDomain" type="text" class="form-control form-control-solid" maxlength="100" placeholder="domain" />
                        </div>
                        <input v-model.trim="store.form.emplym.emplymPhoneNumber" type="text" class="form-control form-control-solid" maxlength="20" placeholder="업무 연락처" />
                        <div class="user-admin-inline flex-wrap">
                          <input v-model="store.form.emplym.ecnyDt" type="date" class="form-control form-control-solid user-admin-date" aria-label="입사일" />
                          <label class="form-check form-check-custom form-check-solid">
                            <input v-model="store.form.emplym.apntcYn" class="form-check-input" type="checkbox" />
                            <span class="form-check-label">수습</span>
                          </label>
                          <label class="form-check form-check-custom form-check-solid">
                            <input v-model="store.form.emplym.retireYn" class="form-check-input" type="checkbox" />
                            <span class="form-check-label">퇴사</span>
                          </label>
                          <input v-if="store.form.emplym.retireYn" v-model="store.form.emplym.retireDt" type="date" class="form-control form-control-solid user-admin-date" aria-label="퇴사일" />
                        </div>
                        <div class="user-admin-inline">
                          <input v-model.trim="store.form.emplym.acntBank" type="text" class="form-control form-control-solid" maxlength="40" placeholder="은행" />
                          <input v-model.trim="store.form.emplym.acntNo" type="text" class="form-control form-control-solid" maxlength="40" placeholder="계좌번호" />
                        </div>
                        <textarea v-model.trim="store.form.emplym.emplymCn" class="form-control form-control-solid" rows="3" maxlength="1000" placeholder="인사정보 설명"></textarea>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-sm btn-light" @click="store.closeForm">닫기</button>
                <button type="submit" class="btn btn-sm btn-primary" :disabled="store.saving">
                  <span v-if="store.saving" class="spinner-border spinner-border-sm me-1"></span>
                  <i v-else class="bi bi-check-lg"></i>
                  저장
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
      <div class="modal-backdrop fade show user-admin-backdrop-top"></div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { swalConfirm, swalAlert } from "@/shared/utils/swal";
import { computed, onMounted } from "vue";
import { useRoute } from "vue-router";
import { useUserAdminStore, type UserRow } from "@/features/admin/stores/userAdmin";

const route = useRoute();
const store = useUserAdminStore();

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

function roleNames(row: UserRow): string {
  return (row.userRoles ?? []).map((role) => role.roleName || role.roleKey).join(", ") || "-";
}

function allowedIps(row: UserRow): string {
  return (row.allowedIpList ?? []).map((item) => item.allowedIp).join(", ");
}

function profileText(row: UserRow): string {
  if (!row.profile) return "-";
  return [
    row.profile.brthdy ? `생년월일: ${row.profile.brthdy}${row.profile.lunarYn === "Y" ? " (음력)" : ""}` : "",
    row.profile.proflCn || "",
  ].filter(Boolean).join("\n") || "-";
}

function emplymText(row: UserRow): string {
  if (!row.emplym) return "-";
  return [
    row.emplym.userNm ? `이름: ${row.emplym.userNm}` : "",
    [row.emplym.cmpyNm, row.emplym.teamNm, row.emplym.rankNm].filter(Boolean).join(" / "),
    row.emplym.emplymEmail ? `업무 이메일: ${row.emplym.emplymEmail}` : "",
    row.emplym.emplymPhoneNumber ? `업무 연락처: ${row.emplym.emplymPhoneNumber}` : "",
    row.emplym.ecnyDt ? `입사일: ${row.emplym.ecnyDt}` : "",
    row.emplym.retireYn === "Y" ? `퇴사일: ${row.emplym.retireDt || "-"}` : "",
    row.emplym.acntBank || row.emplym.acntNo ? `급여계좌: ${[row.emplym.acntBank, row.emplym.acntNo].filter(Boolean).join(" ")}` : "",
    row.emplym.emplymCn || "",
  ].filter(Boolean).join("\n") || "-";
}

function onPageSizeChange(event: Event) {
  void store.changePageSize(Number((event.target as HTMLSelectElement).value));
}

async function openDetail(id: number) {
  try {
    await store.openDetail(id);
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : "계정 상세를 불러오지 못했습니다.");
  }
}

async function openEdit(id: number) {
  try {
    await store.openEdit(id);
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : "계정 상세를 불러오지 못했습니다.");
  }
}

async function submit() {
  if (!store.form.username.trim() || !store.form.nickname.trim()) {
    void swalAlert("아이디와 표시이름을 입력해주세요.");
    return;
  }
  if (!store.isEdit && !store.form.password.trim()) {
    void swalAlert("비밀번호를 입력해주세요.");
    return;
  }
  if (!store.form.emailId.trim() || !store.form.emailDomain.trim()) {
    void swalAlert("이메일을 입력해주세요.");
    return;
  }
  if (!store.form.roleKeyList.length) {
    void swalAlert("권한을 하나 이상 선택해주세요.");
    return;
  }
  try {
    await store.submit();
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : "계정을 저장하지 못했습니다.");
  }
}

async function passwordReset(id: number) {
  if (!await swalConfirm("비밀번호를 초기화할까요?")) return;
  try {
    void swalAlert(await store.passwordReset(id));
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : "비밀번호를 초기화하지 못했습니다.");
  }
}

async function deleteUser(row: UserRow) {
  if (!await swalConfirm(`${row.username} 계정을 삭제할까요?`)) return;
  try {
    await store.deleteUser(row.id);
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : "계정을 삭제하지 못했습니다.");
  }
}

async function checkUsername() {
  if (!store.form.username.trim()) {
    void swalAlert("아이디를 입력해주세요.");
    return;
  }
  const result = await store.usernameDuplicateCheck(store.form.username.trim());
  void swalAlert(result.message || (result.ok ? "사용 가능한 아이디입니다." : "이미 사용 중인 아이디입니다."));
}

async function checkEmail() {
  const email = `${store.form.emailId.trim()}@${store.form.emailDomain.trim()}`;
  if (!store.form.emailId.trim() || !store.form.emailDomain.trim()) {
    void swalAlert("이메일을 입력해주세요.");
    return;
  }
  const result = await store.emailDuplicateCheck(email);
  void swalAlert(result.message || (result.ok ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다."));
}

onMounted(async () => {
  await Promise.all([store.fetchBootstrap(), store.fetchUsers(0)]);
  const id = Number(route.query.id);
  if (!Number.isFinite(id) || id <= 0) return;
  if (route.query.mode === "edit") await openEdit(id);
  else await openDetail(id);
});
</script>

<style scoped>
.user-admin-page {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.user-admin-toolbar,
.user-admin-listbar,
.user-admin-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.user-admin-actions,
.user-admin-search,
.user-admin-account,
.user-admin-inline,
.user-admin-email {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.user-admin-toolbar {
  justify-content: flex-end;
}

.user-admin-search {
  min-width: min(560px, 100%);
  flex-wrap: wrap;
}

.user-admin-search .form-control {
  min-width: 220px;
}

.user-admin-role-filter {
  width: 150px;
}

.user-admin-page-size {
  width: 110px;
}

.user-admin-manage-col {
  width: 104px;
}

.user-admin-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  min-width: 32px;
  height: 32px;
  overflow: hidden;
  border-radius: 8px;
  background: var(--bs-light);
  color: var(--bs-gray-600);
}

.user-admin-avatar.lg {
  width: 48px;
  min-width: 48px;
  height: 48px;
}

.user-admin-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-admin-account strong,
.user-admin-detail-head h3 {
  display: block;
  margin: 0;
  font-size: 0.95rem;
}

.user-admin-account span,
.user-admin-detail-head span {
  color: var(--bs-gray-600);
  font-size: 0.8rem;
}

.user-admin-ellipsis {
  max-width: 260px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-admin-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 120px;
  color: var(--bs-gray-600);
}

.user-admin-detail-head {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.user-admin-detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.75rem;
  margin-top: 1rem;
}

.user-admin-detail-grid > div {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  padding: 0.75rem;
  border: 1px solid var(--bs-gray-200);
  border-radius: 8px;
}

.user-admin-detail-grid span {
  color: var(--bs-gray-600);
  font-size: 0.78rem;
}

.user-admin-detail-grid strong {
  overflow-wrap: anywhere;
}

.user-admin-detail-block {
  margin-top: 1rem;
}

.user-admin-detail-split {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1rem;
}

.user-admin-detail-block h4 {
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
  font-weight: 700;
}

.user-admin-detail-block pre {
  min-height: 72px;
  margin: 0;
  padding: 0.75rem;
  border-radius: 8px;
  background: var(--bs-light);
  color: var(--bs-gray-700);
  white-space: pre-wrap;
  word-break: break-word;
}

.user-admin-form {
  display: grid;
  gap: 1rem;
}

.user-admin-form-row {
  display: grid;
  grid-template-columns: 150px minmax(0, 1fr);
  gap: 1rem;
  align-items: start;
}

.user-admin-form-row > .form-label {
  padding-top: 0.75rem;
  font-weight: 700;
}

.user-admin-role-options {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
  padding-top: 0.75rem;
}

.user-admin-subform {
  display: grid;
  gap: 0.75rem;
}

.user-admin-date {
  width: 180px;
}

.user-admin-email {
  flex-wrap: wrap;
}

.user-admin-email .form-control {
  width: 180px;
}

.user-admin-modal-top {
  z-index: 1065;
}

.user-admin-backdrop-top {
  z-index: 1060;
}

.page-link.active {
  background: var(--bs-primary);
  border-color: var(--bs-primary);
  color: #fff;
}

@media (max-width: 768px) {
  .user-admin-toolbar,
  .user-admin-listbar,
  .user-admin-footer,
  .user-admin-search,
  .user-admin-actions {
    align-items: stretch;
    width: 100%;
  }

  .user-admin-search .form-control,
  .user-admin-role-filter,
  .user-admin-page-size,
  .user-admin-email .form-control {
    width: 100%;
    min-width: 0;
  }

  .user-admin-form-row,
  .user-admin-detail-grid,
  .user-admin-detail-split {
    grid-template-columns: 1fr;
  }

  .user-admin-form-row > .form-label {
    padding-top: 0;
  }
}
</style>
