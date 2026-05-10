import codeAdminUiService from "../services/codeAdminUiService.js";
import { CodeItemRow } from "../types.js";

/** thead 6열과 tbody 열 개수를 맞춘다. sortOrder 전용 td 는 헤더가 없어 제거(정렬 순서는 드래그·서버 sortOrder 로 유지). */
export default {
    name: "CodeGroupDetail",
    props: {
        detail: { type: Object, required: true },
    },
    emits: ["regist-item", "modify-item", "delete-item"],
    methods: {
        isUseYn(item: CodeItemRow): boolean {
            return codeAdminUiService.isYn(item.useYn);
        },
    },
    updated(): void {
        this.$nextTick((): void => codeAdminUiService.syncTooltips("#code_group_detail_div"));
    },
    mounted(): void {
        this.$nextTick((): void => codeAdminUiService.syncTooltips("#code_group_detail_div"));
    },
    template: `
    <div class="mb-4">
        <div class="row mb-4">
            <h1 class="fs-2x fw-bolder text-gray-900 mb-0 me-1">
                <span class="vertical-middle">
                    <span class="fw-bold">{{ detail.groupName }}</span>
                    (<span class="border-bottom">{{ detail.groupCode }}</span>)
                </span>
            </h1>
        </div>
    </div>
    <div class="separator separator-dashed border-gray-300 my-8"></div>
    <div class="fs-4 fw-normal text-gray-800 px-5 py-1 pb-6 min-h-100px">{{ detail.description }}</div>
    <div class="separator separator-dashed border-gray-300 my-8"></div>
    <div class="d-flex justify-content-end mb-4">
        <button type="button" class="btn btn-sm btn-light-primary" @click="$emit('regist-item')">
            <i class="bi bi-plus-lg"></i> {{ $t('txt.admin.code.group.detail.reg-item') }}
        </button>
    </div>
    <table class="table align-middle table-row-dashed fs-small gy-5 table-fixed hoverTable mb-3">
        <thead>
            <tr class="text-start fw-bolder fs-7 text-uppercase gs-0 fw-bolder text-muted">
                <th class="col-1 text-center wb-keepall w-8">{{ $t('txt.admin.code.group.detail.number') }}</th>
                <th class="col-2 text-center wb-keepall">{{ $t('txt.admin.code.group.detail.code') }}</th>
                <th class="col-2 text-center wb-keepall">{{ $t('txt.admin.code.group.detail.code-name') }}</th>
                <th class="col-4 text-center wb-keepall">{{ $t('txt.admin.code.group.detail.code-description') }}</th>
                <th class="col-1 text-center wb-keepall">{{ $t('txt.admin.code.group.detail.use-yn') }}</th>
                <th class="col-1 text-center wb-keepall">{{ $t('txt.admin.code.group.detail.manage') }}</th>
            </tr>
        </thead>
        <tbody class="draggable-zone-code-item">
            <template v-if="!(detail.codeItems && detail.codeItems.length)">
                <tr><td colspan="6" class="text-center">{{ $t('view.list.empty') }}</td></tr>
            </template>
            <template v-else>
            <tr
                v-for="item in detail.codeItems"
                :key="item.id"
                class="bg-hover-secondary cursor-pointer sortable-item draggable-code-item"
                :id="'code-item-' + item.id"
                :data-id="item.id"
                @click="$emit('modify-item', item.id)"
            >
                <td class="text-center">
                    <div class="draggable-handle-code-item d-flex-center cursor-move px-2 py-1"
                        @click.stop
                        data-bs-toggle="tooltip"
                        data-bs-placement="top"
                        data-bs-dismiss="click"
                        :title="$t('bs.tooltip.admin.code.group.detail.reorder')">
                        <i class="ki-duotone ki-abstract-14 fs-1"><span class="path1"></span><span class="path2"></span></i>
                        <span class="ms-2">{{ item.rnum }}</span>
                    </div>
                </td>
                <td class="text-start ps-6"><span class="fw-bold border-bottom">{{ item.code }}</span></td>
                <td class="text-start ps-6"><span class="border-bottom">{{ item.codeName }}</span></td>
                <td class="text-start ps-6">{{ item.description }}</td>
                <td class="text-center">
                    <span :class="isUseYn(item) ? 'text-success' : 'text-muted'">
                        {{ isUseYn(item) ? $t('txt.comm.use') : $t('txt.status.unuse') }}
                        <i :class="isUseYn(item) ? 'bi bi-check text-success ms-1' : 'bi bi-dash text-muted ms-1'"></i>
                    </span>
                </td>
                <td class="text-center">
                    <div class="d-flex justify-content-center align-items-center gap-2">
                        <div class="badge badge-danger badge-outlined cursor-pointer"
                            @click.stop="$emit('delete-item', item.id)"
                            data-bs-toggle="tooltip"
                            data-bs-placement="top"
                            data-bs-dismiss="click"
                            :title="$t('bs.tooltip.del')">
                            <i class="bi bi-trash text-danger fs-7 blink-slow"></i>
                            {{ $t('txt.comm.del') }}
                        </div>
                    </div>
                </td>
            </tr>
            </template>
        </tbody>
    </table>
    `,
};

