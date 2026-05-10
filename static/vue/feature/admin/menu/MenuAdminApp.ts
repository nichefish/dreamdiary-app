/**
 * MenuAdminApp.ts
 * 메뉴 관리 화면 Vue 앱 (오케스트레이션)
 *
 * @author nichefish
 */
import { createScopedI18n } from "../../../global/services/scopedI18nService.js";
import createMenuAdminActions from "./services/menuAdminActionService.js";
import MenuTreeNode from "./components/MenuTreeNode.js";
import MenuRegistForm from "./components/MenuRegistForm.js";

type MenuNode = {
    id: number;
    subMenuList?: MenuNode[];
};

const i18n = createScopedI18n();
const state = Vue.reactive({
    rows: [] as MenuNode[],
    submenuExpandOptions: [] as Array<{ code: string; codeName: string }>,
    menuForm: {
        id: "",
        menuType: "",
        parentMenuId: "",
        upperMenuNm: "",
        adminYn: "N",
        menuName: "",
        menuLabel: "",
        icon: "",
        unreadCntNm: "",
        submenuExpandType: "",
        url: "",
        protectedYn: "N",
    } as Record<string, any>,
});
const actions = createMenuAdminActions({
    renderForm: function(obj: Record<string, any>): void {
        state.menuForm.id = obj.id || "";
        state.menuForm.menuType = obj.menuType || "MAIN";
        state.menuForm.parentMenuId = obj.parentMenuId || "";
        state.menuForm.upperMenuNm = obj.upperMenuNm || "";
        state.menuForm.adminYn = String(obj.adminYn || "N");
        state.menuForm.menuName = obj.menuName || "";
        state.menuForm.menuLabel = obj.menuLabel || "";
        state.menuForm.icon = obj.icon || "";
        state.menuForm.unreadCntNm = obj.unreadCntNm || "";
        state.menuForm.submenuExpandType = obj.submenuExpandType || "";
        state.menuForm.url = obj.url || "";
        state.menuForm.protectedYn = String(obj.protectedYn || "N");
    },
    afterFormRendered: function(): void {
        Vue.nextTick(function(): void {
            cF.validate.validateForm("#menuRegistForm", actions.registAjax);
            cF.validate.toUpperCase("#menuRegistForm #menuLabel");
            initBootstrapTooltips();
            openMenuRegistModal();
        });
    },
});

function initBootstrapTooltips(): void {
    const b = (window as any).bootstrap;
    if (!b?.Tooltip)
        return;
    const els = Array.from(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    els.forEach(function(el: Element): void {
        const existed = b.Tooltip.getInstance(el);
        if (existed)
            existed.dispose();
        new b.Tooltip(el);
    });
}

function openMenuRegistModal(): void {
    const modalEl = document.getElementById("menu_regist_modal");
    if (!modalEl)
        return;
    const b = (window as any).bootstrap;
    if (b?.Modal) {
        b.Modal.getOrCreateInstance(modalEl).show();
        return;
    }
    $("#menu_regist_modal").modal("show");
}

function runWhenDomReady(fn: () => void): void {
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", fn);
        return;
    }
    fn();
}

function resolveMenuAdminPageLocale(): string {
    const w = typeof window !== "undefined" ? (window as Window & { Model?: { locale?: string } }) : undefined;
    const loc = w?.Model?.locale;
    if (loc) return loc;
    return (document.documentElement.lang || "ko").replace(/_/g, "-");
}

function loadMenuTree(): void {
    const url: string = Url.MENU_MAIN_LIST_AJAX;
    cF.ajax.get(url, null, function(res: AjaxResponse): void {
        if (!res.rslt) {
            if (cF.util.isNotEmpty(res.message))
                Swal.fire({ text: res.message });
            return;
        }

        state.rows = (res.rsltList || []) as MenuNode[];
        Vue.nextTick(function(): void {
            KTMenu.createInstances();
            initBootstrapTooltips();
            actions.initSubDraggable();
            actions.initMainDraggable();
        });
    });
}

const MenuAdminRootApp = {
    name: "MenuAdminRootApp",
    components: {
        MenuTreeNode,
        MenuRegistForm,
    },
    data(): { state: typeof state } {
        return { state };
    },
    methods: {
        t(key: string): string { return i18n.t(key); },
        onOpenMainReg(): void { actions.regModal("MAIN", 0, ""); },
        onOpenSubAdd(id: number, menuName: string): void { actions.regModal("SUB", id, menuName); },
        onOpenModify(id: number): void { actions.modifyModal(id); },
        onToggleUse(id: number): void { actions.toggleUseAjax(id); },
        onDeleteNode(id: number): void { actions.deleteAjax(id); },
    },
    template: `
<teleport to="#menu_main_card_div">
    <MenuTreeNode
      v-for="row in state.rows"
      :key="row.id"
      :node="row"
      :level="0"
      :t="t"
      @open-sub-add="onOpenSubAdd"
      @open-modify="onOpenModify"
      @toggle-use="onToggleUse"
      @delete-node="onDeleteNode"
    />
</teleport>
<teleport to="#menu_admin_reg_btn_slot">
    <button type="button" class="btn btn-sm btn-primary" @click="onOpenMainReg"
            data-bs-toggle="tooltip" data-bs-placement="top" data-bs-dismiss="click" :title="t('bs.tooltip.modal.reg')">
        <i class="fas fa-plus"></i>{{ t('txt.comm.reg') }}
    </button>
</teleport>
<teleport to="#menu_reg_div">
    <MenuRegistForm :form="state.menuForm" :submenu-expand-options="state.submenuExpandOptions" :t="t" />
</teleport>
`,
};

runWhenDomReady(async function(): Promise<void> {
    await i18n.load(resolveMenuAdminPageLocale());

    if (!document.getElementById("menu_admin_app")
        || !document.getElementById("menu_main_card_div")) {
        console.error("[MenuAdminApp] Vue mount root not found.");
        return;
    }

    const submenuTypesEl = document.getElementById("menu_submenu_expand_types_data");
    if (submenuTypesEl) {
        try {
            state.submenuExpandOptions = JSON.parse(submenuTypesEl.textContent || "[]");
        } catch (e) {
            console.error("[MenuAdminApp] failed to parse submenu expand options.", e);
            state.submenuExpandOptions = [];
        }
    }

    const app = Vue.createApp(MenuAdminRootApp);
    app.mount("#menu_admin_app");

    actions.init();
    initBootstrapTooltips();
    loadMenuTree();
});
