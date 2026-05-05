export function formValue(formSelector: string, fieldId: string, fallback: string = ""): string {
    const input: HTMLInputElement | null = document.querySelector(`${formSelector} #${fieldId}`);
    return String(input?.value ?? fallback);
}

export function showAjaxError(res: AjaxResponse): boolean {
    if (res.rslt) return false;
    if (cF.util.isNotEmpty(res.message)) Swal.fire({ text: res.message });
    return true;
}

export function confirmThen(message: string, onConfirm: () => void): void {
    Swal.fire({ text: message, showCancelButton: true }).then((result: SwalResult): void => {
        if (!result.value) return;
        onConfirm();
    });
}
