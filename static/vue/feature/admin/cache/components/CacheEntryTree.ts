type CacheEntryTreeComponent = {
    name: string;
    props: Record<string, unknown>;
    components?: Record<string, unknown>;
    computed: Record<string, () => unknown>;
    methods: Record<string, (value: unknown) => unknown>;
    template: string;
};

const CacheEntryTree = {
    name: "CacheEntryTree",
    props: {
        value: { required: true },
        label: { type: String, required: false, default: "" },
    },
    computed: {
        entries(): Array<[string, unknown]> {
            const value = this.value;
            if (value === null || value === undefined || typeof value !== "object" || Array.isArray(value)) return [];
            return Object.entries(value as Record<string, unknown>);
        },
        arrayItems(): unknown[] {
            return Array.isArray(this.value) ? this.value : [];
        },
        isObjectValue(): boolean {
            return this.value !== null && typeof this.value === "object" && !Array.isArray(this.value);
        },
        isArrayValue(): boolean {
            return Array.isArray(this.value);
        },
    },
    methods: {
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
    <div class="cache-entry-tree">
        <div v-if="label" class="row">
            <div class="col-3 col-md-2 fw-bold text-break">{{ label }}:</div>
            <div class="col flex-grow-1 text-break">
                <template v-if="isObjectValue">
                    <CacheEntryTree
                        v-for="entry in entries"
                        :key="entry[0]"
                        :label="entry[0]"
                        :value="entry[1]"
                    />
                </template>
                <template v-else-if="isArrayValue">
                    <div v-for="(item, index) in arrayItems" :key="index" class="mb-1">
                        <CacheEntryTree :label="String(index)" :value="item" />
                    </div>
                </template>
                <template v-else>{{ stringify(value) }}</template>
            </div>
        </div>
        <template v-else>
            <template v-if="isObjectValue">
                <CacheEntryTree
                    v-for="entry in entries"
                    :key="entry[0]"
                    :label="entry[0]"
                    :value="entry[1]"
                />
            </template>
            <template v-else-if="isArrayValue">
                <div v-for="(item, index) in arrayItems" :key="index" class="mb-1">
                    <CacheEntryTree :label="String(index)" :value="item" />
                </div>
            </template>
            <template v-else>{{ stringify(value) }}</template>
        </template>
    </div>
    `,
} as CacheEntryTreeComponent;

CacheEntryTree.components = { CacheEntryTree };

export default CacheEntryTree;
