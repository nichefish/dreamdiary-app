import CacheEntryTree from "./CacheEntryTree.js";
import { CacheDetail } from "../types.js";

export default {
    name: "CacheDetailModalBody",
    components: {
        CacheEntryTree,
    },
    props: {
        detail: { required: false, default: null },
    },
    computed: {
        value(): CacheDetail {
            return this.detail as CacheDetail;
        },
    },
    template: `
    <div>
        <div v-if="value === null || value === undefined" class="text-center text-muted py-10">캐시 상세가 없습니다.</div>
        <CacheEntryTree v-else :value="value" />
    </div>
    `,
};
