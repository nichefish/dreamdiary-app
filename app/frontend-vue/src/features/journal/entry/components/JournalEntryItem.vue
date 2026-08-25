<template>
  <!--begin::엔트리 행-->
  <div
    :id="domId ?? (entry.id ? 'journal-entry-' + entry.id : undefined)"
    :class="[itemClass, { 'is-collapsed': isCollapsed }, 'd-flex gap-2 py-1']"
    :data-id="entry.id"
    :data-imprtc="hasState('IMPRTC') ? 'Y' : 'N'"
    :data-refrnc="hasState('REFRNC') ? 'Y' : 'N'"
    :data-resolved="isResolved ? 'Y' : 'N'"
    :data-lifecycle="lcKey || 'OPEN'"
    :data-else-dream="isElseDream ? 'Y' : 'N'"
    :data-stdrd-dt="entry.stdrdDt"
    :data-yy="entryCacheYy"
    :data-mnth="entryCacheMnth"
  >
    <!--begin::순번-->
    <div v-if="!isSummary" class="d-none d-md-flex flex-column align-items-center pt-1 ps-2" style="width:56px; min-width:56px;">
      <span :class="['fw-bold fs-7', isResolved ? (isDreamEntry ? 'text-dream' : 'text-success') : 'text-muted']">#{{ entry.sortOrder }}</span>
      <span v-if="isPending" class="badge badge-light-secondary text-gray-600 fs-8 mt-1">{{ t("journal.entry.pending-badge") }}</span>
      <!--begin::클라이언트 임시 접힘/펼침 버튼-->
      <button
        type="button"
        :class="['btn btn-xs px-1 mt-1', { 'is-active': isCollapsed }]"
        :title="isCollapsed ? t('common.expand') : t('common.collapse')"
        @click="toggleEntry"
      >
        <i :class="['bi pe-0 fs-8', isCollapsed ? 'bi-arrows-expand' : 'bi-arrows-collapse']"></i>
      </button>
      <!--end::클라이언트 임시 접힘/펼침 버튼-->
    </div>
    <!--end::순번-->

    <!--begin::본문 영역-->
    <div :class="[contentClass, 'flex-grow-1', 'min-w-0', { 'is-summary-card': isSummary, 'd-flex flex-column': isCollapsed }]">
      <!--begin::본문+액션 head-row (본문 옆에 액션 정렬; 리플렉션 임베드 액션과 같은 오른쪽 열)-->
      <div class="d-flex gap-2" :class="isCollapsed ? 'flex-grow-1 align-items-stretch' : 'align-items-start'">
        <!--begin::head-main (배지·제목·본문) — collapsed 시 flex-column+세로 중앙으로 제목을 태그 위 공간 중앙에 둔다 (액션은 상단 유지) -->
        <div class="flex-grow-1 min-w-0" :class="{ 'd-flex flex-column justify-content-center': isCollapsed }">
          <!--begin::꿈 상태 배지 (꿈 엔트리 전용)-->
          <div v-if="isDream" class="d-flex align-items-center gap-1 mb-1 flex-wrap">
            <span v-if="hasState('NHTMR')" class="badge badge-light-danger">!{{ t('state.nightmare') }}</span>
            <span v-if="hasState('HALLUC')" class="badge badge-light-secondary">!{{ t('state.hallucination') }}</span>
          </div>
          <!--end::꿈 상태 배지-->

          <!--begin::엔트리 제목 (유형 무관, title 있을 때만)
            변경 전: 꿈 엔트리에서만 배지 행에 인라인(fs-7)으로 표시 → 일기·노트는 제목이 보이지 않았음
            변경 후: 모든 유형에서 배지 행 아래 독립 행으로 표시. 본문(.journal-content = 1rem) 대비
                     한 단계 위인 fs-5(1.15rem) + fw-bold.
            접힘(isCollapsed) 상태와 무관하게 항상 표시한다 (기존 꿈 제목 동작 유지 — 본문만 숨김).
            .journal-content 밖이라 유형별 본문 색상을 상속하지 않고 기본 텍스트색을 쓴다.
            Prefix 소비 추가 후: 말머리는 제목 앞의 색상 배지로 표시하며 제목이 없어도 말머리만 남긴다.
            접힘 시: 제목 + (collapsed)를 한 줄에 fs-7로 표시. -->
          <div v-if="entry.prefix || entry.title" class="d-flex align-items-center flex-wrap mb-1" :class="isCollapsed ? 'fs-7' : 'fw-bold fs-5'">
            <span
              v-if="entry.prefix"
              class="badge me-2 fs-8"
              :style="{ borderColor: entry.prefix.color || '', color: entry.prefix.color || '' }"
            >{{ entry.prefix.name }}</span>
            <span v-if="entry.title">{{ entry.title }}</span>
            <span v-if="isCollapsed" class="text-muted fst-italic ms-2">(collapsed)</span>
          </div>
          <!--end::엔트리 제목-->

          <!--begin::마크다운 본문-->
          <div v-if="debugCollapse" class="fs-9 text-danger px-2">
            [E#{{entry.id}}] isCollapsed={{isCollapsed}} | lc={{lcKey}} | force={{props.forceCollapsed}} | localOvr={{localCollapsedOverride}} | signal={{reflectionForceSignal}}
          </div>
          <div
            v-if="!isCollapsed && entry.markdownContent"
            class="journal-content p-2"
            v-html="displayMarkdownContent"
          ></div>
          <div v-else-if="isCollapsed && !entry.prefix && !entry.title" class="text-muted fs-8 fst-italic ps-2 d-flex align-items-center">(collapsed)</div>
          <!--end::마크다운 본문-->
        </div>
        <!--end::head-main-->
        <!--begin::우측 액션 영역-->
        <div v-if="entry.id" class="journal-entry-actions d-flex flex-row align-items-start pt-1 gap-1">
          <!--begin::댓글 등록 버튼-->
          <button
            v-if="axisWritable"
            type="button"
            class="btn btn-xs btn-icon journal-entry-action-btn"
            :title="t('comment.register')"
            @click="openCommentRegist"
          >
            <i class="bi bi-chat-dots fs-8"></i>
          </button>
          <!--end::댓글 등록 버튼-->

          <!--begin::복사 (split — 주 버튼=전체/해석 포함, ▾ 드롭다운=본문만/해석 제외)-->
          <div class="btn-group" role="group">
            <!--begin::주 버튼 (해석 포함)-->
            <button
              type="button"
              class="btn btn-xs btn-icon journal-entry-action-btn copy-split-main"
              :title="copyIncludeTitle"
              @click="copyEntry('full')"
            >
              <i class="bi bi-copy fs-8"></i>
            </button>
            <!--end::주 버튼-->
            <!--begin::본문만 드롭다운 (항상 노출 — 리플렉션 없으면 본문=전체라 결과 동일)-->
            <button
              type="button"
              class="btn btn-xs journal-entry-action-btn copy-split-caret"
              data-kt-menu-trigger="click"
              data-kt-menu-placement="bottom-end"
              :title="t('common.menu')"
            >
              <i class="bi bi-caret-down-fill fs-9 pe-0"></i>
            </button>
            <div
              class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold w-175px py-2"
              data-kt-menu="true"
            >
              <div class="menu-item px-3 my-1 cursor-pointer">
                <div class="menu-link flex-stack px-3" @click="copyEntry('no-pending')">
                  {{ t('journal.copy.no-pending.label') }}
                  <i class="bi bi-copy fs-8"></i>
                </div>
              </div>
              <div class="menu-item px-3 my-1 cursor-pointer">
                <div class="menu-link flex-stack px-3" @click="copyEntry('body')">
                  {{ t('journal.copy.body.label') }}
                  <i class="bi bi-clipboard fs-8"></i>
                </div>
              </div>
            </div>
            <!--end::본문만 드롭다운-->
          </div>
          <!--end::복사 (split)-->
          <!--begin::링크 복사 (외부에서 클릭 시 해당 일자 일간뷰로 이동해 이 엔트리로 스크롤)-->
          <button
            type="button"
            class="btn btn-xs btn-icon journal-entry-action-btn"
            :title="t('journal.entry.copy-link')"
            @click="copyEntryLink"
          >
            <i class="bi bi-link-45deg fs-8"></i>
          </button>
          <!--end::링크 복사-->

          <!--begin::컨텍스트 메뉴-->
          <div class="me-0">
            <button
              type="button"
              class="btn btn-xs btn-icon journal-entry-action-btn"
              data-kt-menu-trigger="click"
              data-kt-menu-placement="bottom-end"
              :title="t('common.menu')"
            >
              <i class="ki-solid ki-dots-horizontal fs-6"></i>
            </button>
            <div
              class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold w-200px py-3"
              data-kt-menu="true"
            >
              <!--begin::메뉴 헤더-->
              <div class="menu-item px-3">
                <div class="menu-content text-muted pb-2 px-3 fs-7 text-uppercase">{{ contentLabel }}</div>
              </div>
              <!--end::메뉴 헤더-->

              <!--begin::저장된 엔트리 새 창 보기-->
              <div class="menu-item px-3 my-1 cursor-pointer">
                <div class="menu-link flex-stack px-3" @click="openInNewWindow">
                  {{ t('common.open-in-new-window') }}
                  <i class="bi bi-box-arrow-up-right fs-8"></i>
                </div>
              </div>
              <!--end::저장된 엔트리 새 창 보기-->

              <!--begin::수정-->
              <div v-if="axisWritable" class="menu-item px-3 my-1 cursor-pointer">
                <div class="menu-link flex-stack px-3" @click="openModify">
                  {{ t('common.edit') }}
                  <i class="bi bi-pencil-square fs-8"></i>
                </div>
              </div>
              <!--end::수정-->

              <!--begin::해석 등록 — primary만. 1급 Reflection에서는 R→R을 열지 않는다(REFLECTION_ONE_TYPE §3.1).-->
              <div
                v-if="axisWritable && entry.contentType !== 'JOURNAL_REFLECTION'"
                class="menu-item px-3 my-1 cursor-pointer"
              >
                <div class="menu-link flex-stack px-3" @click="openReflectionRegist">
                  {{ t('journal.entry.reflection.register') }}
                  <i class="bi bi-lightbulb fs-8"></i>
                </div>
              </div>
              <!--end::해석 등록-->

              <!--begin::이력 (historyTriggeredAt 없으면 disabled)-->
              <div class="menu-item px-3 my-1 cursor-pointer">
                <div
                  :class="['menu-link flex-stack px-3', { 'disabled text-muted': !hasHistory }]"
                  @click="hasHistory ? openHistory() : undefined"
                >
                  {{ t('journal.entry.history') }}
                  <i class="bi bi-clock-history fs-8"></i>
                </div>
              </div>
              <!--end::이력-->

              <!--begin::관련 글 추가 (다른 사람 꿈 제외)-->
              <div v-if="axisWritable && !hasDreamerName(entry)" class="menu-item px-3 my-1 cursor-pointer">
                <div class="menu-link flex-stack px-3" @click="openRelated">
                  {{ t('journal.entry.related-content.add') }}
                  <i class="bi bi-link-45deg fs-8"></i>
                </div>
              </div>
              <!--end::관련 글 추가-->

              <!--begin::스레드에 추가 서브메뉴 (다른 사람 꿈·일기/꿈/노트 target Reflection 제외)-->
              <div
                v-if="axisWritable && !hasDreamerName(entry) && !isPrimaryContentTargetedReflection(entry)"
                class="menu-item px-3"
                data-kt-menu-trigger="hover"
                data-kt-menu-placement="right-start"
                @mouseenter="ensureThreadOptions"
              >
                <a href="#" class="menu-link px-3" @click.prevent>
                  <span class="menu-title">{{ t('journal.entry.thread.add') }}</span>
                  <span class="menu-arrow"></span>
                </a>
                <div class="menu-sub menu-sub-dropdown py-3" style="width: 280px;">
                  <!--begin::새 스레드로 시작-->
                  <div class="menu-item px-3 my-1 cursor-pointer">
                    <div class="menu-link flex-stack px-3 text-primary" @click="startNewThread">
                      {{ t('journal.entry.thread.new') }}
                      <i class="bi bi-plus-lg fs-8"></i>
                    </div>
                  </div>
                  <!--end::새 스레드로 시작-->

                  <div class="separator my-2"></div>

                  <!--begin::스레드 후보 검색·말머리-->
                  <div
                    class="menu-item px-3"
                    data-kt-menu-dismiss="false"
                    @click.stop
                    @keydown.stop
                  >
                    <div class="menu-content px-3 py-1 w-100">
                      <input
                        v-model="membershipStore.optionKeyword"
                        type="search"
                        class="form-control form-control-sm"
                        :placeholder="t('journal.thread.filter.keyword.placeholder')"
                        data-kt-menu-dismiss="false"
                        @input="scheduleThreadCandidateSearch"
                      />
                      <select
                        v-model="membershipStore.optionPrefix"
                        class="form-select form-select-sm mt-2"
                        :disabled="membershipStore.prefixesLoading"
                        data-kt-menu-dismiss="false"
                        @change="refreshThreadCandidates"
                      >
                        <option value="">{{ t("journal.thread.filter.all-prefixes") }}</option>
                        <option
                          v-for="item in membershipPrefixItems"
                          :key="'thread-prefix-' + item.id"
                          :value="String(item.id)"
                        >
                          {{ item.name }}
                        </option>
                      </select>
                      <div v-if="membershipStore.prefixError" class="text-danger fs-9 mt-1">
                        {{ membershipStore.prefixError }}
                      </div>
                      <label class="form-check form-check-custom form-check-sm form-check-solid mt-2 cursor-pointer">
                        <input
                          v-model="membershipStore.optionIncludeResolved"
                          class="form-check-input"
                          type="checkbox"
                          data-kt-menu-dismiss="false"
                          @change="refreshThreadCandidates"
                        />
                        <span class="form-check-label fs-8 text-gray-700">
                          {{ t("journal.entry.thread.candidates.include-resolved") }}
                        </span>
                      </label>
                    </div>
                  </div>
                  <!--end::스레드 후보 검색·분류-->

                  <div class="separator my-2"></div>

                  <!--begin::스레드 후보 목록-->
                  <div v-if="membershipStore.optionsLoading" class="menu-item px-3">
                    <span class="menu-link px-3 text-muted fs-8">{{ t('common.loading') }}</span>
                  </div>
                  <div v-if="membershipStore.optionsError" class="menu-item px-3">
                    <span class="menu-content px-3 text-danger fs-8">{{ membershipStore.optionsError }}</span>
                  </div>
                  <div
                    v-if="!membershipStore.optionsLoading
                      && !membershipStore.optionsError
                      && membershipStore.threadOptions.length === 0"
                    class="menu-item px-3"
                  >
                    <span class="menu-content px-3 text-muted fs-8">
                      {{ hasThreadCandidateFilter
                        ? t('journal.entry.thread.search.empty')
                        : t('journal.entry.thread.empty') }}
                    </span>
                  </div>
                  <div v-if="filteredThreadOptions.length > 0" class="thread-candidate-list">
                    <div
                      v-for="opt in filteredThreadOptions"
                      :key="'thread-opt-' + opt.id"
                      class="menu-item px-3 my-1 cursor-pointer"
                    >
                      <div class="menu-link flex-stack px-3" @click="toggleThread(opt)">
                        <span class="min-w-0">
                          <span class="d-block text-truncate">
                            {{ opt.title || t('journal.entry.thread.untitled') }}
                          </span>
                          <span class="d-block text-muted fs-9">
                            <span v-if="threadPrefixName(opt)">{{ threadPrefixName(opt) }}</span>
                            <span
                              v-if="opt.lifecycleKey && opt.lifecycleKey !== 'OPEN'"
                              :class="[
                                threadPrefixName(opt) ? 'ms-1' : '',
                                opt.lifecycleKey === 'PENDING' ? 'text-gray-600' : 'text-success',
                              ]"
                            >{{ threadLifecycleLabel(opt.lifecycleKey) }}</span>
                          </span>
                        </span>
                        <i v-if="opt.member" class="bi bi-check-lg fs-8 text-success"></i>
                      </div>
                    </div>
                  </div>
                  <!--end::스레드 후보 목록-->
                </div>
              </div>
              <!--end::스레드에 추가 서브메뉴-->

              <div v-if="axisWritable" class="separator my-2"></div>

              <!--begin::라이프사이클 서브메뉴-->
              <div v-if="axisWritable" class="menu-item px-3" data-kt-menu-trigger="hover" data-kt-menu-placement="right-end">
                <a href="#" class="menu-link px-3" @click.prevent>
                  <span class="menu-title">{{ t('common.lifecycle') }}</span>
                  <span class="menu-arrow"></span>
                </a>
                <div class="menu-sub menu-sub-dropdown w-175px py-4">
                  <div v-for="lc in lifecycleOptions" :key="'lc-' + lc.key" class="menu-item px-3">
                    <div class="menu-content px-3">
                      <label class="form-check form-check-custom form-check-solid cursor-pointer">
                        <input
                          class="form-check-input w-18px h-18px cursor-pointer"
                          type="radio"
                          :name="'entry-lc-' + entry.id"
                          :value="lc.key"
                          :checked="lcKey === lc.key"
                          @click="setLifecycle(lc.key)"
                        />
                        <span class="form-check-label fs-7" :class="lcKey === lc.key ? lc.activeClass : 'text-muted'">{{ lc.label }}</span>
                      </label>
                    </div>
                  </div>
                </div>
              </div>
              <!--end::라이프사이클 서브메뉴-->

              <!--begin::상태 서브메뉴-->
              <div class="menu-item px-3" data-kt-menu-trigger="hover" data-kt-menu-placement="right-end">
                <a href="#" class="menu-link px-3" @click.prevent>
                  <span class="menu-title">{{ t('common.status') }}</span>
                  <span class="menu-arrow"></span>
                </a>
                <div class="menu-sub menu-sub-dropdown w-175px py-4">
                  <!--begin::중요/참조 토글-->
                  <template v-if="axisWritable">
                  <div v-for="st in statusOptions" :key="'st-' + st.key" class="menu-item px-3">
                    <div class="menu-content px-3">
                      <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
                        <input
                          class="form-check-input w-30px h-20px cursor-pointer"
                          type="checkbox"
                          :checked="hasState(st.key)"
                          @click="toggleState(st.key)"
                        />
                        <span class="form-check-label fs-7" :class="hasState(st.key) ? st.activeClass : 'text-muted'">{{ st.label }}</span>
                      </label>
                    </div>
                  </div>
                  <!--end::중요/참조 토글-->

                  <!--begin::악몽/환각 토글 (꿈 전용)-->
                  <template v-if="isDream">
                    <div v-for="st in dreamStatusOptions" :key="'dst-' + st.key" class="menu-item px-3">
                      <div class="menu-content px-3">
                        <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
                          <input
                            class="form-check-input w-30px h-20px cursor-pointer"
                            type="checkbox"
                            :checked="hasState(st.key)"
                            @click="toggleState(st.key)"
                          />
                          <span class="form-check-label fs-7" :class="hasState(st.key) ? st.activeClass : 'text-muted'">{{ st.label }}</span>
                        </label>
                      </div>
                    </div>
                  </template>
                  <!--end::악몽/환각 토글-->
                  </template>

                  <!--begin::접기 토글-->
                  <div class="menu-item px-3">
                    <div class="menu-content px-3">
                      <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
                        <input
                          class="form-check-input w-30px h-20px cursor-pointer"
                          type="checkbox"
                          :checked="hasState('COLLAPSED')"
                          @click="toggleState('COLLAPSED')"
                        />
                        <span class="form-check-label fs-7" :class="hasState('COLLAPSED') ? 'text-gray-700' : 'text-muted'">{{ t('common.collapse') }}</span>
                      </label>
                    </div>
                  </div>
                  <!--end::접기 토글-->
                </div>
              </div>
              <!--end::상태 서브메뉴-->

              <div v-if="axisWritable" class="separator my-2"></div>

              <!--begin::삭제-->
              <div v-if="axisWritable" class="menu-item px-3 my-1 cursor-pointer">
                <div class="menu-link flex-stack px-3 text-danger" @click="deleteEntry">
                  {{ t('common.delete') }}
                  <i class="bi bi-trash text-danger p-0 fs-8"></i>
                </div>
              </div>
              <!--end::삭제-->
            </div>
          </div>
          <!--end::컨텍스트 메뉴-->
        </div>
        <!--end::우측 액션 영역-->
      </div>
      <!--end::본문+액션 head-row-->

      <!--begin::Reflection 슬림 임베드 (target=이 엔트리; 본문 아래·태그 위, 엔트리 접힘 시 함께 숨김)-->
      <template v-if="!isCollapsed">
        <JournalReflectionItem
          v-for="reflection in reflectionList"
          :key="reflection.id"
          :reflection="reflection"
          :force-collapsed-signal="reflectionForceSignal"
        />
      </template>
      <!--end::Reflection 슬림 임베드-->

      <!--begin::엔트리 태그-->
      <div v-if="tagList.length > 0" class="d-flex flex-wrap gap-1 mt-1 ps-2">
        <span
          v-for="tag in tagList"
          :key="tag.tagId"
          class="text-muted cursor-pointer pe-1"
          @click.stop="openTagContextMenu($event, tag)"
        >
          #<span class="border-bottom text-primary fw-lighter opacity-hover">
            <span v-if="tag.ctgr" class="fs-7 text-noti">[{{ tag.ctgr }}]</span>{{ tag.name }}
          </span>
        </span>
      </div>
      <!--end::엔트리 태그-->

      <!--begin::꿈 태그 프로필-->
      <div v-if="!isCollapsed && dreamTagProfileList.length > 0" class="journal-dream-tag-profiles">
        <div
          v-for="tag in dreamTagProfileList"
          :key="'profile-' + tag.tagId"
          class="journal-dream-tag-profile"
        >
          <span class="journal-dream-tag-profile__tag">#{{ tag.name }}</span>
          <span class="journal-dream-tag-profile__divider"></span>
          <span class="journal-dream-tag-profile__content">{{ tag.profileContent }}</span>
        </div>
      </div>
      <!--end::꿈 태그 프로필-->

      <!--begin::관련글-->
      <div v-if="relatedList.length > 0" class="d-flex flex-column gap-1 mt-2 ps-2">
        <div
          v-for="rel in relatedList"
          :key="rel.id"
          class="d-flex align-items-center flex-wrap gap-2 p-2 bg-light rounded fs-8 text-muted"
        >
          <i class="bi bi-link-45deg"></i>
          <span v-if="rel.relationType" class="badge badge-light-primary">{{ relationTypeLabel(rel.relationType) }}</span>
          <span v-if="rel.targetContentType" class="badge badge-light-secondary">{{ relatedContentTypeLabel(rel.targetContentType) }}</span>
          <button
            v-if="rel.targetId"
            type="button"
            class="btn btn-link p-0 fs-8 text-primary text-start"
            :title="t('related-content.open.tooltip')"
            @click.stop="openRelatedTarget(rel.targetId)"
          >
            {{ rel.targetTitle || '#' + rel.targetId }}
          </button>
          <span v-else>{{ rel.targetTitle }}</span>
          <span v-if="rel.reason" class="fst-italic">({{ rel.reason }})</span>
          <button
            v-if="axisWritable && rel.id"
            type="button"
            class="btn btn-xs btn-icon btn-light-danger ms-auto"
            :title="t('related-content.unlink.tooltip')"
            @click.stop="unlinkRelated(rel)"
          >
            <i class="bi bi-x-lg fs-9"></i>
          </button>
        </div>

      </div>
      <!--end::관련글-->

      <!--begin::소속 스레드-->
      <div v-if="entryThreadList.length > 0" class="d-flex flex-wrap align-items-center gap-1 mt-2 ps-2">
        <i class="bi bi-diagram-3 fs-8 text-muted"></i>
        <button
          v-for="th in entryThreadList"
          :key="'thread-' + th.id"
          type="button"
          class="badge badge-light-primary border-0 fs-8 cursor-pointer"
          :title="t('journal.entry.thread.open.tooltip')"
          @click.stop="openThreadDetail(th.threadId)"
        >
          {{ th.threadTitle || ('#' + th.threadId) }}
        </button>
      </div>
      <!--end::소속 스레드-->

      <!--begin::댓글-->
      <div v-if="commentList.length > 0" class="d-flex flex-column gap-1 mt-2 ps-2">
        <div v-for="cmt in commentList" :key="cmt.id" class="d-flex align-items-start gap-1">
          <div
            class="fs-8 text-muted ps-2 border-start border-2 border-gray-300 flex-grow-1 min-w-0"
            v-html="cmt.markdownContent || cmt.content || ''"
          ></div>
          <div v-if="axisWritable" class="d-flex flex-shrink-0 gap-1">
            <button type="button" class="btn btn-xs btn-icon btn-active-light-primary" :title="t('comment.modify')" @click.stop="onEditComment(cmt.id)">
              <i class="bi bi-pencil fs-9"></i>
            </button>
            <button type="button" class="btn btn-xs btn-icon btn-active-light-danger" :title="t('comment.delete')" @click.stop="onDeleteComment(cmt.id)">
              <i class="bi bi-trash fs-9"></i>
            </button>
          </div>
        </div>
      </div>
      <!--end::댓글-->
    </div>
    <!--end::본문 영역-->
  </div>
  <!--end::엔트리 행-->
</template>

<script setup lang="ts">
import { swalAlert, swalFire, swalConfirm } from "@/shared/utils/swal";
import { joinAppBasePath } from "@/shared/utils/appPath";
import { computed, watch, nextTick, provide } from "vue";
import { useRoute } from "vue-router";
import { useJournalModalStore } from "@/features/journal/stores/journalModal";
import { useAuthStore } from "@/shared/auth/stores/auth";
import { useAttachableModalStore } from "@/features/attachable/stores/attachableModal";
import { useEntryThreadMembership } from "@/features/journal/entry/composables/useEntryThreadMembership";
import { useEntryLifecycleState } from "@/features/journal/entry/composables/useEntryLifecycleState";
import { useEntryRelatedContent } from "@/features/journal/entry/composables/useEntryRelatedContent";
import { useEntryCollapse } from "@/features/journal/entry/composables/useEntryCollapse";
import { useJournalThreadStore } from "@/features/journal/stores/journalThread";
import { useTagContextMenuStore } from "@/features/journal/stores/tagContextMenu";
import { useJournalStore } from "@/features/journal/stores/journal";
import { refreshJournalEntryHostForRoute } from "@/features/journal/utils/journalEntryHostRefresh";
import type { JournalEntryDto } from "@/features/journal/stores/journal";
import { getWeekDayStr } from "@/features/journal/utils/journalDate";
import { hasDreamerName } from "@/features/journal/utils/journalDream";
import { isPrimaryContentTargetedReflection } from "@/features/journal/utils/journalReflectionThread";
import { htmlToPlainText } from "@/features/journal/utils/htmlToPlainText";
import {
  type CopyReflectionMode,
  copySuccessKey,
  includeReflectionInCopy,
} from "@/features/journal/utils/journalCopyReflection";
import { highlightKeywordsInHtml } from "@/features/journal/utils/highlightKeywords";
import { openJournalEntryViewPopup } from "@/features/journal/utils/journalEntryViewPopup";
import { reinitMetronicAfterDom } from "@/shared/utils/metronicReinit";
import { useLocaleStore } from "@/shared/i18n/stores/locale";
import JournalReflectionItem from "../../reflection/components/JournalReflectionItem.vue";
import {
  useJournalDayResolved,
  mergeDayResolvedAxis,
  JOURNAL_DAY_RESOLVED_KEY,
} from "@/features/journal/utils/journalDayResolved";

const props = defineProps<{
  entry: JournalEntryDto;
  isDream?: boolean;
  /** 챕터 토글이 전파하는 강제 접힘 여부. null=챕터 미개입, true/false=챕터 강제 */
  forceCollapsed?: boolean | null;
  /** 스레드 상세 등에서 lifecycle 자동 접힘을 억제한다. 상태 표시는 유지한다. 기본 false. */
  disableLifecycleCollapse?: boolean;
  /** Parent-provided DOM id, used by popup/search contexts that render the same entry component. */
  domId?: string;
  /** Search-only keyword highlights. Empty by default so monthly/weekly/chapter renders stay unchanged. */
  highlightKeywords?: string[];
  /** 시스템 요약 챕터의 첫 엔트리(그날 전체 요약)를 상태 언어가 아닌 별개 '카드'로 분리 표시한다(#순번 숨김·본문 박스 카드화). 기본 false — 챕터 외 호출처 무영향. */
  isSummary?: boolean;
}>();

const modalStore = useJournalModalStore();
const authStore = useAuthStore();
const attachableStore = useAttachableModalStore();
const tagContextMenuStore = useTagContextMenuStore();
const journalStore = useJournalStore();
const threadStore = useJournalThreadStore();
const route = useRoute();
const { t } = useLocaleStore();

/** 현재 엔트리가 꿈 유형인지 여부. 꿈 RESOLVED 전용 보라색 표시 계약에 사용한다. */
const isDreamEntry = computed(() => props.isDream || props.entry.contentType === "JOURNAL_DREAM");

const parentDayResolvedAxis = useJournalDayResolved();
const mergedDayResolvedAxis = computed(() =>
  mergeDayResolvedAxis(parentDayResolvedAxis.value, props.entry),
);
provide(JOURNAL_DAY_RESOLVED_KEY, mergedDayResolvedAxis);
const axisWritable = computed(() =>
  isDreamEntry.value ? mergedDayResolvedAxis.value.dreamWritable : mergedDayResolvedAxis.value.diaryWritable,
);

function guardAxisWrite(): boolean {
  if (axisWritable.value) return true;
  void swalAlert(
    t(isDreamEntry.value ? "journal.day.dream-resolved-locked" : "journal.day.diary-resolved-locked"),
  );
  return false;
}

/** 엔트리 타입별 외부 item 클래스 (journal.scss 의 data-* 셀렉터 연동) */
const itemClass = computed(() => {
  if (props.isDream || props.entry.contentType === 'JOURNAL_DREAM') return 'journal-dream-item';
  if (props.entry.contentType === 'JOURNAL_NOTE') return 'journal-note-item';
  return 'journal-diary-item';
});

/** 엔트리 타입별 내부 content 클래스 (텍스트 색상·left-border 스타일 연동) */
const contentClass = computed(() => {
  if (props.isDream || props.entry.contentType === 'JOURNAL_DREAM') return 'journal-dream-content';
  if (props.entry.contentType === 'JOURNAL_NOTE') return 'journal-note-content';
  return 'journal-diary-content';
});

/** 메뉴 헤더에 표시할 컨텐츠 유형 레이블 */
const contentLabel = computed(() => {
  if (props.isDream || props.entry.contentType === 'JOURNAL_DREAM') return t('common.dream');
  return t('common.diary');
});

const lcKey = computed(() => props.entry.lifecycle?.lifecycleKey ?? "");
const isResolved = computed(() => lcKey.value === "RESOLVED");
const isPending = computed(() => lcKey.value === "PENDING");
/** localStorage("debug_collapse")=true 일 때 접힘 메타정보를 표시한다. */
const debugCollapse = computed(() => localStorage.getItem("debug_collapse") === "true");
/** 지정 꿈꾼(타인 꿈) — journal.scss 좌측 회색 이중선·RESOLVED 색상과 별도 */
const isElseDream = computed(() => {
  if (!(props.isDream || props.entry.contentType === "JOURNAL_DREAM")) return false;
  return hasDreamerName(props.entry);
});
const hasHistory = computed(() => !!props.entry.history?.historyTriggeredAt);

/** composable 공통 entry ref */
const entryRef = computed(() => props.entry);

/** 접힘 상태 해석·토글·Reflection 신호 전파를 캡슐화. */
const {
  localCollapsedOverride,
  isCollapsed,
  reflectionForceSignal,
  hasState,
  toggleEntry,
} = useEntryCollapse({
  entry: entryRef,
  forceCollapsed: computed(() => props.forceCollapsed),
  disableLifecycleCollapse: computed(() => props.disableLifecycleCollapse),
  lcKey,
});

const tagList = computed(() => props.entry.tag?.list ?? []);
const displayMarkdownContent = computed(() => highlightKeywordsInHtml(props.entry.markdownContent ?? "", props.highlightKeywords ?? []));
/** 변경 전: 태그 프로필은 설정 모달에서만 보였음. 변경 후: 꿈 엔트리에서만 본문 아래에 프로필을 표시. */
const dreamTagProfileList = computed(() => {
  if (!(props.isDream || props.entry.contentType === "JOURNAL_DREAM")) return [];
  return tagList.value.filter((tag) => typeof tag.profileContent === "string" && tag.profileContent.trim() !== "");
});
/** 관련글 표시·해제·라벨 전체를 캡슐화. */
const {
  relatedList,
  relatedContentTypeLabel,
  relationTypeLabel,
  openRelatedTarget,
  unlinkRelated,
} = useEntryRelatedContent({
  entry: entryRef,
  guardAxisWrite,
  scrollAfterFetch,
  t,
});
const commentList = computed(() => props.entry.comment?.list ?? []);
const reflectionList = computed(() => props.entry.reflectionList ?? []);

/** 복사(해석 포함) 버튼 tooltip. 리플렉션이 있으면 "해석 포함"을 명시하고, 로컬 프로필은 id 를 덧붙인다. */
const copyIncludeTitle = computed(() => {
  const base = reflectionList.value.length > 0 ? t("journal.copy.full.tooltip") : t("common.copy");
  return authStore.isLocalProfile ? `${base} (id ${props.entry.id})` : base;
});

/**
 * 엔트리 접힘 시 임베드 Reflection 은 v-if 로 DOM 에서 제거된다.
 * 다시 펼치면 KTMenu(⋯) DOM 이 새로 마운트되므로 Metronic 핸들러를 재바인딩한다.
 */
watch(isCollapsed, (collapsed, wasCollapsed) => {
  if (wasCollapsed !== true || collapsed) return;
  if (reflectionList.value.length === 0) return;
  void reinitMetronicAfterDom();
});

/** 라이프사이클·상태·삭제 전체를 캡슐화. */
const {
  entryCacheYy,
  entryCacheMnth,
  lifecycleOptions,
  statusOptions,
  dreamStatusOptions,
  setLifecycle,
  toggleState,
  deleteEntry,
} = useEntryLifecycleState({
  entry: entryRef,
  isDreamEntry,
  guardAxisWrite,
  scrollAfterFetch,
  refreshTagCloudAfterDelete,
  t,
});

/** 태그 클릭 컨텍스트 메뉴 열기 */
function openTagContextMenu(event: MouseEvent, tag: { tagId: number | string; name: string; ctgr?: string }): void {
  tagContextMenuStore.open(event, {
    tagId: tag.tagId,
    name: tag.name,
    ctgr: tag.ctgr ?? "",
    contentType: props.entry.contentType ?? "",
  });
}

/** 엔트리 내용을 클립보드에 복사한다. 형식: 날짜(요일) → 본문 → 그 엔트리를 문(target) 리플렉션 본문(원문·해석은 한 몸으로 함께 복사). */
async function copyEntry(mode: CopyReflectionMode = "full"): Promise<void> {
  const weekDay = getWeekDayStr(props.entry.stdrdDt, t);
  const dateLine = weekDay
    ? `${props.entry.stdrdDt} (${weekDay})`
    : (props.entry.stdrdDt ?? "");
  /* content = TinyMCE HTML 원문(마크다운 재처리 이전); markdownContent = MarkdownUtils 처리 후 HTML */
  const raw = htmlToPlainText(props.entry.content ?? props.entry.markdownContent ?? "");
  const parts = [dateLine, raw].filter(Boolean);
  /* 모드별로 이 엔트리를 target 으로 한 리플렉션 본문을 빈 줄로 이어 붙인다(포맷: 마커 없음). 보류(PENDING)는 no-pending 모드에서 제외. */
  for (const reflection of reflectionList.value) {
    if (!includeReflectionInCopy(mode, reflection.lifecycle?.lifecycleKey)) continue;
    const reflRaw = htmlToPlainText(reflection.content ?? reflection.markdownContent ?? "");
    if (reflRaw) parts.push("", reflRaw);
  }
  const text = parts.join("\n");
  try {
    await navigator.clipboard.writeText(text);
    /* 성공 토스트는 복사 범위를 명시한다: 전체/보류 제외/본문만, 리플렉션이 없으면 공용 문구. */
    const successKey = copySuccessKey(mode, reflectionList.value.length > 0);
    void swalFire({ icon: "success", text: t(successKey) });
  } catch (error: unknown) {
    console.error("[journal-entry] clipboard copy failed", error);
    void swalFire({ icon: "error", text: t("common.copy.failure") });
  }
}

/**
 * 이 엔트리로 가는 링크를 클립보드에 복사한다.
 * 외부(메신저·메모 등)에서 클릭하면 앱의 해당 일자 일간뷰(journal-daily-tab)로 진입하고,
 * entryId 로 이 엔트리(#journal-entry-{id})까지 스크롤한다. 절대 URL(origin + BASE_URL) 을 만든다.
 */
async function copyEntryLink(): Promise<void> {
  const stdrdDt = props.entry.stdrdDt;
  if (!stdrdDt || props.entry.id == null) return;
  const path = joinAppBasePath(`/journal/daily?stdrdDt=${encodeURIComponent(stdrdDt)}&entryId=${props.entry.id}`);
  const url = `${window.location.origin}${path}`;
  try {
    await navigator.clipboard.writeText(url);
    void swalFire({ icon: "success", text: t("common.copy.success") });
  } catch (error: unknown) {
    console.error("[journal-entry] link copy failed", error);
    void swalFire({ icon: "error", text: t("common.copy.failure") });
  }
}

/** 저장된 엔트리 한 건을 ID 기반 읽기 전용 새 창으로 연다. */
function openInNewWindow(): void {
  if (!openJournalEntryViewPopup(props.entry.id)) {
    void swalAlert(t("common.error.popup"));
  }
}

/** 엔트리 수정 모달 열기. 이 컴포넌트는 Primary 엔트리(일기·꿈·노트)만 렌더한다. */
function openModify() {
  if (!guardAxisWrite()) return;
  if (!props.entry.id) return;
  void modalStore.openEntryModify(props.entry.id);
}

/** 댓글 등록 모달 열기 */
function openCommentRegist() {
  if (!guardAxisWrite()) return;
  if (!props.entry.id || !props.entry.contentType) return;
  attachableStore.openCommentRegist(props.entry.id, props.entry.contentType);
}

/** 인라인 댓글 수정 — 기존 CommentRegistModal 수정 모드를 재사용한다. */
function onEditComment(id: number): void {
  if (!guardAxisWrite()) return;
  void attachableStore.openCommentModify(id);
}

/** 인라인 댓글 삭제 — 확인 후 삭제하고 스크롤 없이 호스트를 재조회한다. */
async function onDeleteComment(id: number): Promise<void> {
  if (!guardAxisWrite()) return;
  if (!await swalConfirm(t("comment.delete.confirm"))) return;
  try {
    if (await attachableStore.deleteComment(id)) {
      await swalAlert(t("common.result.deleted"));
      void scrollAfterFetch(undefined, { scroll: false });
    }
  } catch (e) {
    void swalAlert(e instanceof Error ? e.message : t("common.result.failure"));
  }
}

/** 이력 모달 열기 */
function openHistory() {
  if (!props.entry.id || !props.entry.contentType) return;
  void attachableStore.openHistory(props.entry.contentType, props.entry.id, {
    writeLocked: !axisWritable.value,
  });
}

/** 관련 글 추가 모달 열기 */
function openRelated() {
  if (!guardAxisWrite()) return;
  if (!props.entry.id || !props.entry.contentType) return;
  attachableStore.openRelated(props.entry.contentType, props.entry.id);
}

/** 이 엔트리가 속한 스레드 목록 · 소속 토글 · 새 스레드 생성 · 상세 열기 전체를 캡슐화. */
const {
  membershipStore,
  entryThreadList,
  hasThreadCandidateFilter,
  membershipPrefixItems,
  filteredThreadOptions,
  ensureThreadOptions,
  scheduleThreadCandidateSearch,
  refreshThreadCandidates,
  threadLifecycleLabel,
  threadPrefixName,
  toggleThread,
  startNewThread,
  openThreadDetail,
} = useEntryThreadMembership({
  entry: entryRef,
  guardAxisWrite,
  scrollAfterFetch,
  t,
});




/** Reflection 등록 모달 열기. target = 이 엔트리, 기본 chapter = 이 엔트리의 chapter. */
function openReflectionRegist() {
  if (!guardAxisWrite()) return;
  if (!props.entry.id || !props.entry.contentType) return;
  modalStore.openReflectionRegist({
    refId: props.entry.id,
    refContentType: props.entry.contentType,
    journalDayId: props.entry.journalDayId,
    journalChapterId: props.entry.journalChapterId,
    stdrdDt: props.entry.stdrdDt,
  });
}

/**
 * 액션 성공 후 현재 표시 호스트를 재조회하고, 일자 목록을 갱신한 경우에만 해당 일자로 스크롤한다.
 * 변경 전에는 모든 라우트에서 fetchDays 완료 후 일자 DOM을 찾았으나, 스레드 상세에서는
 * 열린 스레드의 원본 엔트리·집계 태그를 다시 조회하고 모달 내부 읽기 위치를 유지한다.
 */
function scrollAfterFetch(stdrdDt = props.entry.stdrdDt, opts: { scroll?: boolean } = {}): Promise<void> {
  const dt = stdrdDt;
  return refreshJournalEntryHostForRoute(journalStore, threadStore, route, dt).then((scope) => {
    /* 검색·스레드 상세는 배경 일자 스크롤 대상이 아니다. opts.scroll === false 면 재조회만 하고 스크롤은 생략한다(스레드 소속 변경 등 사용자 위치 유지). */
    if (opts.scroll === false || scope === "thread-detail" || scope === "journal-entry-search" || !dt) return;
    void nextTick(() => {
      const el = document.getElementById(`journal-day-${dt}`);
      if (el) el.scrollIntoView({ behavior: "smooth", block: "start" });
    });
  });
}

/**
 * 엔트리 삭제 뒤 현재 기간의 유형별 태그 클라우드를 갱신한다.
 * 검색 화면은 태그 클라우드를 표시하지 않으며, NOTE는 전용 태그 클라우드 섹션을 갖지 않는다.
 */
function refreshTagCloudAfterDelete(contentType?: string): void {
  if (route.name === "journal-entry-search") {
    console.info("[JournalEntryItem] 엔트리 삭제 후 태그 클라우드 갱신 생략", {
      contentType,
      routeName: route.name,
      reason: "search-route",
    });
    return;
  }
  if (contentType === "JOURNAL_DIARY") {
    console.info("[JournalEntryItem] 엔트리 삭제 후 태그 클라우드 갱신", { contentType, section: "diary" });
    void journalStore.fetchTagCloud({ sections: ["diary"] });
    return;
  }
  if (contentType === "JOURNAL_DREAM") {
    console.info("[JournalEntryItem] 엔트리 삭제 후 태그 클라우드 갱신", { contentType, section: "dream" });
    void journalStore.fetchTagCloud({ sections: ["dream"] });
    return;
  }
  console.info("[JournalEntryItem] 엔트리 삭제 후 태그 클라우드 갱신 생략", {
    contentType,
    routeName: route.name,
    reason: "unsupported-content-type",
  });
}


</script>

<style scoped>
/* 스레드 소속 서브메뉴 후보 목록: 높이를 고정 상한으로 묶어 목록 변경 시 서브메뉴 재배치(→ hover 닫힘)를 막는다. */
.thread-candidate-list {
  max-height: 240px;
  overflow-y: auto;
}
:deep(.journal-entry-search-keyword-mark) {
  background-color: #fff3cd;
  border-radius: 0.25rem;
  box-shadow: inset 0 -0.35em 0 rgba(255, 193, 7, 0.35);
  color: inherit;
  font-weight: 700;
  padding: 0 0.12em;
}

/* 시스템 요약 챕터를 '그날 전체 요약' 카드로 분리 — 카드 재질은 챕터 전체(헤더+엔트리)를 감싼다(JournalChapterItem .is-summary-chapter).
   엔트리 레벨에서는 요약 엔트리의 크롬만 정리한다: #순번 거터 숨김(템플릿 v-if)·상태 좌측선(::before) 제거.
   변경 전: 배경 tint + 2px 좌측 바(=엔트리 상태 언어)라 요약이 '상태'로 오인됐고, 이후 본문 박스만 카드화했으나 챕터 헤더가 카드 밖이었다.
   변경 후: 카드는 챕터 래퍼로 올리고 엔트리는 카드 안에서 크롬만 뺀다. 접힘(is-collapsed) 시 챕터가 엔트리를 숨긴다(의도된 동작). */
.is-summary-card::before {
  /* base 좌측선(.journal-diary-item .journal-diary-content::before, 특이성 0,2,1)과 동률이라 순서 의존을 피해 강제 제거 */
  content: none !important;
}
</style>
