import { PaginationState, UserLabels } from "../types.js";

export default {
    name: "UserPagination",
    props: {
        pagination: { type: Object, required: true },
        labels: { type: Object, required: true },
    },
    emits: ["go-page", "change-size"],
    computed: {
        p(): PaginationState {
            return this.pagination as PaginationState;
        },
        l(): UserLabels {
            return this.labels as UserLabels;
        },
        pageOptions(): number[] {
            return [10, 25, 50];
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
                    >
                        <option v-for="size in pageOptions" :key="size" :value="size">{{ size }}개씩 보기</option>
                    </select>
                </span>
            </div>
            <div class="col-6 pt-3 px-5">
                <span class="float-end">{{ l.totalPrefix }} {{ p.totalCnt || 0 }}</span>
            </div>
        </div>
        <div class="row paging">
            <ul class="pagination">
                <li class="page-item previous" :class="{ disabled: p.isFirstPage }">
                    <a href="javascript:void(0);" class="page-link" @click="$emit('go-page', 1)"><i class="previous"></i></a>
                </li>
                <li v-if="p.prevPageNo && p.prevPageNo < p.currPageNo" class="page-item previous">
                    <a href="javascript:void(0);" class="page-link" @click="$emit('go-page', p.prevPageNo)">{{ p.prevPageNo }}</a>
                </li>
                <li class="page-item active">
                    <a href="javascript:void(0);" class="page-link">{{ p.currPageNo }}</a>
                </li>
                <li v-if="p.nextPageNo && p.nextPageNo > p.currPageNo" class="page-item">
                    <a href="javascript:void(0);" class="page-link" @click="$emit('go-page', p.nextPageNo)">{{ p.nextPageNo }}</a>
                </li>
                <li class="page-item next" :class="{ disabled: p.isLastPage }">
                    <a href="javascript:void(0);" class="page-link" @click="$emit('go-page', p.lastPageNo)"><i class="next"></i></a>
                </li>
            </ul>
        </div>
    </div>
    `,
};
