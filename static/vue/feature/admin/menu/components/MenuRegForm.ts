type SubmenuExpandOption = {
    code: string;
    codeName: string;
};

export default {
    name: "MenuRegForm",
    props: {
        form: { type: Object, required: true },
        submenuExpandOptions: { type: Array, required: true },
        t: { type: Function, required: true },
    },
    computed: {
        options(): SubmenuExpandOption[] {
            return this.submenuExpandOptions as SubmenuExpandOption[];
        },
        isMain(): boolean {
            return String((this.form as any).menuType || "") === "MAIN";
        },
        showUrl(): boolean {
            return String((this.form as any).submenuExpandType || "") === "NO_SUB";
        },
        iconPreview(): string {
            const icon = String((this.form as any).icon || "");
            return icon || "-";
        },
    },
    template: `
<div>
    <input type="hidden" name="id" id="id" :value="form.id || ''">
    <input type="hidden" name="menuType" id="menuType" :value="form.menuType || ''">
    <input type="hidden" name="parentMenuId" id="parentMenuId" :value="form.parentMenuId || ''">

    <div class="row mb-4">
        <template v-if="isMain">
            <div class="col-2 text-center">
                <label for="useYn" class="col-form-label fw-bold">{{ t('txt.admin.menu.form.manager-menu-yn') }}</label>
            </div>
            <div class="col-10">
                <div class="form-check form-switch mt-2 form-check-custom form-check-solid">
                    <input type="checkbox" name="adminYn" id="useYn" class="form-check-input cursor-pointer"
                           value="Y" :checked="form.adminYn === 'Y'" @change="form.adminYn = $event.target && $event.target.checked ? 'Y' : 'N'" />
                    <label class="form-check-label ms-3" for="useYn">
                        {{ form.adminYn === 'Y' ? t('txt.status.use') : t('txt.status.unuse') }}
                    </label>
                </div>
            </div>
        </template>
        <template v-else>
            <div class="col-2 text-center">
                <label class="col-form-label fw-bold">{{ t('txt.admin.menu.form.upper-menu-name') }}</label>
            </div>
            <div class="col-10 col-form-label">
                <div class="ps-2 text-gray-600 ls-1">{{ form.upperMenuNm || '' }}</div>
            </div>
        </template>
    </div>

    <div class="row mb-4">
        <div class="col-2 text-center"><label for="menuName" class="col-form-label fw-bold required">{{ t('txt.admin.menu.form.menu-name') }}</label></div>
        <div class="col-4">
            <input type="text" name="menuName" id="menuName" class="form-control d-inline-block required"
                   v-model="form.menuName" maxlength="200" autocomplete="off">
            <span id="menuName_validate_span"></span>
        </div>
        <div class="col-2 text-center"><label for="menuLabel" class="col-form-label fw-bold">{{ t('txt.admin.menu.form.menu-label') }}</label></div>
        <div class="col-4">
            <input type="text" name="menuLabel" id="menuLabel" class="form-control d-inline-block"
                   :readonly="form.protectedYn === 'Y'" :class="form.protectedYn === 'Y' ? 'cursor-not-allowed form-control-solid' : ''"
                   v-model="form.menuLabel" maxlength="200" autocomplete="off">
            <span id="menuLabel_validate_span"></span>
        </div>
    </div>

    <div class="row mb-4">
        <div class="col-2 text-center"><label for="icon" class="col-form-label fw-bold">{{ t('txt.admin.menu.form.menu-icon') }}</label></div>
        <div class="col-1 d-flex-center flex-column gap-3">
            <div id="menu_icon_div" class="cursor-pointer" v-html="iconPreview"></div>
        </div>
        <div class="col-9">
            <textarea name="icon" id="icon" class="form-control form-control-solid d-block"
                      v-model="form.icon" maxlength="1000" rows="5"></textarea>
            <div id="icon_validate_span"></div>
        </div>
    </div>

    <div class="row mb-4">
        <div class="col-2 text-center"><label for="unreadCntNm" class="col-form-label fw-bold">{{ t('txt.admin.menu.form.unread-count-name') }}</label></div>
        <div class="col-10">
            <input type="text" name="unreadCntNm" id="unreadCntNm" class="form-control" v-model="form.unreadCntNm" maxlength="1000">
        </div>
    </div>

    <div class="row mb-4">
        <div class="col-2 text-center"><label for="submenuExpandType" class="cursor-help fw-bold required" v-html="t('txt.admin.menu.form.submenu-display-type')"></label></div>
        <div class="col-3">
            <select name="submenuExpandType" id="submenuExpandType" class="form-select form-select-solid required" v-model="form.submenuExpandType">
                <option value="">----</option>
                <option v-for="opt in options" :key="opt.code" :value="opt.code">{{ opt.codeName }}</option>
            </select>
            <div id="submenuExpandType_validate_span"></div>
        </div>
    </div>

    <div class="row mb-4" id="url_div" :class="showUrl ? '' : 'd-none'">
        <div class="col-2 text-center"><label for="url" class="col-form-label fw-bold">{{ t('txt.admin.menu.form.menu-url') }}</label></div>
        <div class="col-10">
            <input type="text" name="url" id="url" class="form-control" v-model="form.url" maxlength="1000">
            <div id="url_validate_span"></div>
        </div>
    </div>
</div>
`,
};
