/// <reference types="vite/client" />

declare module "@yaireo/tagify" {
  class Tagify {
    value: any[];
    settings: Record<string, any>;
    DOM: { input: HTMLElement };
    constructor(el: HTMLInputElement, settings?: Record<string, any>);
    on(event: string, callback: (e: any) => void): this;
    addTags(tags: any[]): void;
    removeTags(tag?: any): void;
    loadOriginalValues(value: string | any[]): void;
    destroy(): void;
    dropdown: { show: (value?: string) => void };
  }
  export default Tagify;
}
