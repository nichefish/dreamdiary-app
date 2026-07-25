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

    <template v-else>

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
              <tr v-if="!store.rows.length">
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
                        <div class="menu-link flex-stack px-3" @click="openEdit(row.id)">
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
    <!--end::계정 목록 탭-->

    <!--모달은 탭과 무관하게 항상 마운트한다 (탭 전환 중 열려 있어도 유지)-->
    <template v-if="store.detailOpen">
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
                    <button type="button" class="btn btn-sm btn-light-primary" @click="openEdit(store.detail.id)">
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

    <template v-if="store.formOpen">
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
  </div>
</template>

<script setup lang="ts">
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import { swalConfirm, swalAlert, swalFire, swalAjaxResult } from "@/shared/utils/swal";
import { computed, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUserAdminStore, type UserRow } from "@/features/admin/stores/userAdmin";
import { useUserSignupStore } from "@/features/user/stores/userSignup";
import UserSignupApprovalList from "@/features/user/signup/UserSignupApprovalList.vue";
import { isMetronicMenuEventTarget, reinitMetronicAfterDom } from "@/shared/utils/metronicReinit";

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

function onPageSizeChange(event: Event) {
  void store.changePageSize(Number((event.target as HTMLSelectElement).value));
}

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

async function passwordReset(id: number) {
  if (!await swalConfirm(t("user.admin.reset-password.confirm"))) return;
  try {
    void swalFire({ icon: "success", text: await store.passwordReset(id) });
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("user.admin.reset-password.failure"));
  }
}

async function deleteUser(row: UserRow) {
  if (!await swalConfirm(t("user.admin.delete.confirm").replace("{username}", row.username))) return;
  try {
    await store.deleteUser(row.id);
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("user.admin.delete.failure"));
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

function onUserRowClick(event: MouseEvent, id: number): void {
  if (isMetronicMenuEventTarget(event.target)) return;
  void openDetail(id);
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
.user-admin-page {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

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
