type MenuNode = {
    id: number;
    menuType: string;
    parentMenuType?: string;
    parentMenuId?: number;
    sortOrder?: number;
    useYn?: string;
    menuName?: string;
    menuLabel?: string;
    icon?: string;
    protectedYn?: string;
    requiredYn?: string;
    submenuExpandType?: string;
    subMenuList?: MenuNode[];
};

function isYn(value: string | undefined): boolean {
    return String(value || "N").toUpperCase() === "Y";
}

export default {
    name: "MenuTreeNode",
    props: {
        node: { type: Object, required: true },
        level: { type: Number, required: true },
        t: { type: Function, required: true },
    },
    emits: ["open-main-add", "open-sub-add", "open-modify", "toggle-use", "delete-node"],
    computed: {
        n(): MenuNode {
            return this.node as MenuNode;
        },
        isMain(): boolean {
            return this.level === 0;
        },
        canDrag(): boolean {
            return !isYn(this.n.protectedYn);
        },
        isMainType(): boolean {
            return String(this.n.menuType || "") === "MAIN";
        },
        isParentMainType(): boolean {
            return String(this.n.parentMenuType || "") === "MAIN";
        },
        useY(): boolean {
            return isYn(this.n.useYn);
        },
        requiredY(): boolean {
            return isYn(this.n.requiredYn);
        },
        protectedY(): boolean {
            return isYn(this.n.protectedYn);
        },
        children(): MenuNode[] {
            return Array.isArray(this.n.subMenuList) ? this.n.subMenuList : [];
        },
        hasChildren(): boolean {
            return this.children.length > 0;
        },
        childZoneClass(): string {
            if (this.protectedY)
                return "sub-menu-list custom-scroll";
            return this.n.submenuExpandType === "NO_SUB"
                ? "sub-menu-list custom-scroll"
                : "sub-menu-list custom-scroll draggable-zone-sub";
        },
        mainCardClass(): string {
            return `menu-item col-4 list-unstyled p-5 sortable-item draggable-main`;
        },
    },
    methods: {
        emitOpenSubAdd(): void {
            this.$emit("open-sub-add", this.n.id, this.n.menuName || "");
        },
        emitOpenSubAddForward(parentId: number, parentName: string): void {
            this.$emit("open-sub-add", parentId, parentName);
        },
        onToggleUseClick(event: Event): void {
            event.preventDefault();
            event.stopPropagation();
            this.$emit("toggle-use", this.n.id);
        },
    },
    template: `
<li
  class="menu-item list-unstyled sortable-item"
  :class="isMain ? 'col-4 p-5 draggable-main' : 'draggable-sub'"
  :style="n.borderColor || ''"
  :data-id="n.id"
  :data-type="n.menuType"
  :data-parent-menu-id="n.parentMenuId"
  :data-sort-order="n.sortOrder"
  :data-use-yn="n.useYn"
>
  <div :class="isMain ? 'card shadow' : ''">
    <div v-if="isMain" class="card-header pt-2">
      <div class="d-flex align-items-center">
        <div v-if="canDrag" class="draggable-handle-main cursor-move pt-2">
          <i class="ki-duotone ki-abstract-14 fs-2x"><span class="path1"></span><span class="path2"></span></i>
        </div>
        <i :class="(n.icon || '') + ' text-info fs-3 ms-4'"></i>
        <h2 class="mx-2 mb-0 text-info cursor-default text-underline-dotted">
          <span class="title" :class="!useY ? 'strikethrough' : ''">
            {{ n.menuName }}
            <i v-if="protectedY" class="bi bi-shield-lock text-muted"></i>
            <i v-if="requiredY" class="bi bi-exclamation-diamond text-warning"></i>
            <span class="badge bg-light-info text-info ms-1">{{ n.menuLabel }}</span>
          </span>
        </h2>
      </div>
      <div class="d-flex align-items-center justify-content-between">
        <div class="w-40px">
          <button class="btn btn-sm btn-icon btn-bg-light btn-active-color-primary"
                  data-kt-menu-trigger="click" data-kt-menu-placement="bottom-end">
            <i class="ki-solid ki-dots-horizontal fs-2x"></i>
          </button>
          <div class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold w-200px py-3" data-kt-menu="true">
            <div class="menu-item px-3">
              <div class="menu-content text-muted pb-2 px-3 fs-7 text-uppercase">{{ t('txt.menu') }}: {{ n.menuName }}</div>
            </div>
            <div v-if="isMainType" class="menu-item px-3 my-1 cursor-pointer">
              <div class="menu-link flex-stack px-3" @click.stop.prevent="emitOpenSubAdd">
                {{ t('txt.menu') }} {{ t('txt.comm.reg') }}
                <i class="bi bi-pencil-square fs-8"></i>
              </div>
            </div>
            <div class="menu-item px-3 my-1 cursor-pointer">
              <div class="menu-link flex-stack px-3" @click.stop.prevent="$emit('open-modify', n.id)">
                {{ t('txt.comm.edit') }}
                <i class="bi bi-pencil-square fs-8"></i>
              </div>
            </div>
            <div v-if="!requiredY" class="menu-item px-3" data-kt-menu-trigger="hover" data-kt-menu-placement="right-end">
              <a href="#" class="menu-link px-3">
                <span class="menu-title">{{ t('txt.comm.status') }}</span>
                <span class="menu-arrow"></span>
              </a>
              <div class="menu-sub menu-sub-dropdown w-175px py-4">
                <div class="menu-item px-3">
                  <div class="menu-content px-3">
                    <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
                      <input class="form-check-input w-30px h-20px cursor-pointer" type="checkbox" :checked="useY" @click="onToggleUseClick">
                      <span class="form-check-label text-muted fs-7">{{ t('txt.status.use') }}</span>
                    </label>
                  </div>
                </div>
              </div>
            </div>
            <div v-if="!protectedY" class="separator my-2"></div>
            <div v-if="!protectedY" class="menu-item px-3 my-1 cursor-pointer">
              <div class="menu-link flex-stack px-3 text-danger" @click.stop.prevent="$emit('delete-node', n.id)">
                {{ t('txt.comm.del') }}
                <i class="bi bi-trash text-danger p-0 fs-8"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template v-else>
      <div v-if="isParentMainType" class="menu-label fw-bolder text-muted fs-6 pb-1">{{ n.menuLabel || '-' }}</div>
      <div class="d-flex align-items-center justify-content-between rounded">
      <div class="d-flex align-items-center">
        <div v-if="canDrag" class="draggable-handle-sub cursor-move pt-1">
          <i class="ki-duotone ki-abstract-14 fs-2x"><span class="path1"></span><span class="path2"></span></i>
        </div>
        <span class="ps-3 pe-0" v-html="n.icon || '-'"></span>
        <div class="ms-3 mb-0 text-dark cursor-default text-underline-dotted">
          <span class="title fs-5" :class="!useY ? 'strikethrough' : ''">
            {{ n.menuName }}
            <i v-if="protectedY" class="bi bi-shield-lock text-muted"></i>
            <i v-if="requiredY" class="bi bi-exclamation-diamond text-warning"></i>
            <span class="badge bg-secondary ms-1" :class="isParentMainType ? 'text-dark' : 'text-muted'">{{ n.menuLabel || '-' }}</span>
          </span>
        </div>
      </div>
      <div class="d-flex align-items-center justify-content-end">
        <div class="w-40px">
          <button class="btn btn-sm btn-icon btn-bg-light btn-active-color-primary"
                  data-kt-menu-trigger="click" data-kt-menu-placement="bottom-end">
            <i class="ki-solid ki-dots-horizontal fs-2x"></i>
          </button>
          <div class="menu menu-sub menu-sub-dropdown menu-column menu-rounded menu-gray-800 menu-state-bg-light-primary fw-semibold w-200px py-3" data-kt-menu="true">
            <div class="menu-item px-3">
              <div class="menu-content text-muted pb-2 px-3 fs-7 text-uppercase">{{ t('txt.menu') }}: {{ n.menuName }}</div>
            </div>
            <div class="menu-item px-3 my-1 cursor-pointer">
              <div class="menu-link flex-stack px-3" @click.stop.prevent="$emit('open-modify', n.id)">
                {{ t('txt.comm.edit') }}
                <i class="bi bi-pencil-square fs-8"></i>
              </div>
            </div>
            <div v-if="!requiredY" class="menu-item px-3" data-kt-menu-trigger="hover" data-kt-menu-placement="right-end">
              <a href="#" class="menu-link px-3">
                <span class="menu-title">{{ t('txt.comm.status') }}</span>
                <span class="menu-arrow"></span>
              </a>
              <div class="menu-sub menu-sub-dropdown w-175px py-4">
                <div class="menu-item px-3">
                  <div class="menu-content px-3">
                    <label class="form-check form-switch form-check-custom form-check-solid cursor-pointer">
                      <input class="form-check-input w-30px h-20px cursor-pointer" type="checkbox" :checked="useY" @click="onToggleUseClick">
                      <span class="form-check-label text-muted fs-7">{{ t('txt.status.use') }}</span>
                    </label>
                  </div>
                </div>
              </div>
            </div>
            <div v-if="!protectedY" class="separator my-2"></div>
            <div v-if="!protectedY" class="menu-item px-3 my-1 cursor-pointer">
              <div class="menu-link flex-stack px-3 text-danger" @click.stop.prevent="$emit('delete-node', n.id)">
                {{ t('txt.comm.del') }}
                <i class="bi bi-trash text-danger p-0 fs-8"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
      </div>
    </template>

    <ul
      :id="'menu_sub_' + n.id"
      :class="isMain ? ('card-body ps-12 ' + childZoneClass) : childZoneClass"
      :data-parent-menu-id="n.id"
      :data-parent-menu-ty="n.menuType"
    >
      <MenuTreeNode
        v-for="child in children"
        :key="child.id"
        :node="child"
        :level="level + 1"
        :t="t"
        @open-sub-add="emitOpenSubAddForward"
        @open-modify="$emit('open-modify', $event)"
        @toggle-use="$emit('toggle-use', $event)"
        @delete-node="$emit('delete-node', $event)"
      />
    </ul>
  </div>
</li>
`,
};
