/**
 * 이메일 도메인 선택 공통 컴포넌트
 *
 * UserFormPanel / UserSignupApp 공용.
 *
 * @author nichefish
 */
export default {
    name: "EmailDomainSelect",
    props: {
        id: { type: String, required: true },
        name: { type: String, required: true },
        options: { type: Array, required: true },
        customInputLabel: { type: String, required: true },
        modelValue: { type: String, required: false, default: undefined },
        selectedValue: { type: String, required: false, default: "" },
        cssClass: { type: String, required: false, default: "form-select form-select-solid" },
    },
    emits: ["update:modelValue", "change"],
    computed: {
        hasModelValue(): boolean {
            return typeof this.modelValue === "string";
        },
        selected(): string {
            if (this.hasModelValue)
                return this.modelValue as string;
            return (this.selectedValue as string) || "";
        },
        domainOptions(): string[] {
            return (this.options as string[]) || [];
        },
    },
    methods: {
        onChange(e: Event): void {
            const value = (e.target as HTMLSelectElement)?.value || "";
            this.$emit("update:modelValue", value);
            this.$emit("change", value);
        },
    },
    template: `
    <select :id="id" :name="name" :class="cssClass" :value="selected" @change="onChange">
        <option value="">{{ customInputLabel }}</option>
        <option v-for="domain in domainOptions" :key="id + '-' + domain" :value="domain">{{ domain }}</option>
    </select>
    `,
};
