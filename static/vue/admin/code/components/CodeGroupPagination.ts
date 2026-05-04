import { PaginationState } from "../types.js";

export default {
    name: "CodeGroupPagination",
    props: {
        pagination: { type: Object, required: true },
    },
    emits: ["go-page", "change-size"],
    computed: {
        p(): PaginationState {
            return this.pagination as PaginationState;
        },
        totalText(): string {
            return this.$t("txt.admin.code.group.pagination.total-format").replace("{0}", String(this.p.totalCnt || 0));
        },
        pageOptions(): number[] {
            return [10, 25, 50];
        },
    },
    methods: {
        pageSizeLabel(size: number): string {
            if (size === 25) return this.$t("txt.admin.code.group.pagination.page-size-25");
            if (size === 50) return this.$t("txt.admin.code.group.pagination.page-size-50");
            return this.$t("txt.admin.code.group.pagination.page-size-10");
        },
    },
    template: `
    <div id="pagination" class="mt-10">
        <div class="row">
            <div class="col-6">
                <span class="float-start">
                    <select
                        name="pageSizeSelect"
                        id="pageSizeSelect"
                        data-hide-search="true"
                        class="form-select form-select-solid"
                        :value="p.pageSize"
                        @change="$emit('change-size', Number($event.target.value))"
                        data-bs-toggle="tooltip"
                        data-bs-placement="top"
                        data-bs-dismiss="click"
                        title="목록에서 한 페이지에 표시할&#10;데이터 개수를 변경합니다."
                    >
                        <option v-for="size in pageOptions" :key="size" :value="size">
                            {{ pageSizeLabel(size) }}
                        </option>
                    </select>
                </span>
            </div>
            <div class="col-6 pt-3 px-5">
                <span class="float-end">{{ totalText }}</span>
            </div>
        </div>
        <div class="row paging">
            <ul class="pagination">
                <li class="page-item previous" :class="{ disabled: p.isFirstPage }">
                    <a href="javascript:void(0);" class="page-link" @click="$emit('go-page', 1)"><i class="previous"></i></a>
                </li>
                <li class="page-item previous" :class="{ disabled: p.isFirstPage }">
                    <a href="javascript:void(0);" class="page-link" @click="$emit('go-page', p.prevPageNo || 1)">{{ p.prevPageNo || 1 }}</a>
                </li>
                <li class="page-item active">
                    <a href="javascript:void(0);" class="page-link">{{ p.currPageNo }}</a>
                </li>
                <li class="page-item" :class="{ disabled: p.isLastPage }">
                    <a href="javascript:void(0);" class="page-link" @click="$emit('go-page', p.nextPageNo || p.lastPageNo)">{{ p.nextPageNo || p.lastPageNo }}</a>
                </li>
                <li class="page-item next" :class="{ disabled: p.isLastPage }">
                    <a href="javascript:void(0);" class="page-link" @click="$emit('go-page', p.lastPageNo)"><i class="next"></i></a>
                </li>
            </ul>
        </div>
    </div>
    `,
};

