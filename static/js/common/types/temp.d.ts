/**
 * swal.d.ts
 * '
 * @author nichefish
 */
declare const Vue: any;
declare const moment: Function;
declare const Handlebars: {
    compile: Function;
    registerHelper: Function<string, Function>;
    registerPartial: Function<string, Function>;
};
declare const html2pdf: Function;
declare const tinymce: {
    [key: string]: any;
};
declare const Tagify: any;

declare const Pickr: any;
declare interface Pickr {
    any;
}
declare namespace FullCalendar {
    interface Calendar {
        [key: string]: any;
    }
}
declare const FullCalendar: {
    Calendar: any
};
declare const Draggable: any;
declare interface Draggable {
    any;
}
interface JQuery {
    daterangepicker(options: any, callback: Function): JQuery;
}
