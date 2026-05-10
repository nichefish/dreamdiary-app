import { CacheMap } from "../types.js";

export default {
    name: "CacheListModalBody",
    props: {
        cacheMap: { type: Object, required: true },
    },
    emits: ["clear-cache", "open-detail", "evict-entry"],
    computed: {
        map(): CacheMap {
            return this.cacheMap as CacheMap;
        },
        cacheNames(): string[] {
            return Object.keys(this.map || {});
        },
    },
    methods: {
        entries(cacheName: string): Array<[string, unknown]> {
            const cache = this.map[cacheName];
            if (!cache || typeof cache !== "object") return [];
            return Object.entries(cache);
        },
        displayKey(cacheKey: string): string {
            return cacheKey === "SimpleKey()" ? "-" : cacheKey;
        },
        stringify(value: unknown): string {
            if (value === undefined) return "undefined";
            if (value === null) return "null";
            if (typeof value === "string") return value;
            try {
                return JSON.stringify(value);
            } catch (_error) {
                return String(value);
            }
        },
    },
    template: `
    <div>
        <div v-if="!cacheNames.length" class="text-center text-muted py-10">활성 캐시가 없습니다.</div>
        <template v-for="(cacheName, cacheIndex) in cacheNames" :key="cacheName">
            <div class="row d-flex-align-center" :data-cache-name="cacheName">
                <div class="col-2 d-flex-center flex-column gap-1">
                    <div class="fw-bold text-break">"{{ cacheName }}"</div>
                    <div>
                        <button
                            type="button"
                            class="btn btn-sm btn-light-danger btn-outlined ms-2 py-1 px-2 cursor-pointer"
                            data-bs-toggle="tooltip"
                            data-bs-placement="top"
                            data-bs-dismiss="click"
                            title="캐시 전체 무효화"
                            @click="$emit('clear-cache', cacheName)"
                        >
                            <i class="bi bi-trash p-0"></i>전체 삭제
                        </button>
                    </div>
                </div>
                <div class="col-10">
                    <div
                        v-for="entry in entries(cacheName)"
                        :key="entry[0]"
                        class="row py-1"
                        :data-cache-key="entry[0]"
                    >
                        <div class="col d-flex-align-center">
                            <div class="text-start text-truncate">
                                <button
                                    type="button"
                                    class="btn btn-sm btn-light-primary btn-outlined ms-2 py-1 px-3 cursor-pointer"
                                    data-bs-toggle="tooltip"
                                    data-bs-placement="top"
                                    data-bs-dismiss="click"
                                    title="상세 열기"
                                    @click="$emit('open-detail', cacheName, entry[0])"
                                >
                                    {{ displayKey(entry[0]) }}
                                    <i class="bi bi-stickies ms-1 p-0"></i>
                                </button>
                                <button
                                    type="button"
                                    class="btn btn-sm btn-light-danger btn-outlined ms-2 py-1 px-0 cursor-pointer"
                                    data-bs-toggle="tooltip"
                                    data-bs-placement="top"
                                    data-bs-dismiss="click"
                                    title="캐시 항목 무효화"
                                    @click="$emit('evict-entry', cacheName, entry[0])"
                                >
                                    <i class="bi bi-trash p-0"></i>삭제
                                </button>
                                <span class="ms-4">{{ stringify(entry[1]) }}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div v-if="cacheIndex < cacheNames.length - 1" class="separator my-5"></div>
        </template>
    </div>
    `,
};
