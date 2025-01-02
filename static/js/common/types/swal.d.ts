/**
 * swal.d.ts
 * '
 * @author nichefish
 */
declare const Swal: {
    fire: Function;
};
declare interface SwalResult<T = any> {
    readonly isConfirmed: boolean
    readonly isDenied: boolean
    readonly isDismissed: boolean
    readonly value?: T
    readonly dismiss?: Swal.DismissReason
}