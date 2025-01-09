/**
 * html2pdf.js
 * 공통 - html2pdf 관련 함수 모듈
 *
 * @namespace: cF.html2pdf (노출식 모듈 패턴)
 * @author: nichefish
 */
// @ts-ignore
if (typeof cF === 'undefined') { var cF = {} as any; }
cF.html2pdf = (function(): Module {
    /**
     * PDF 생성 기본 옵션
     */
    const defaultOptions: Record<string, any> = {
        margin: 0,
        filename: 'test.pdf',
        image: { type: 'jpeg', quality: 1 },
        html2canvas: { dpi: 300, letterRendering: true },
        jsPDF: { orientation: 'portrait', unit: 'mm', format: 'a4', compressPDF: true },
        pagebreak: {
            mode: ['avoid-all', 'css'],
            before: ['#pdfContent']
        }
    };

    return {
        /**
         * 특정 요소를 PDF로 변환하고 저장합니다.
         * @param {string} elmtId - PDF로 변환할 요소의 ID.
         */
        fnPdf: function(elmtId: string): void {
            const elmt = document.getElementById(elmtId);
            html2pdf().from(elmt)
                      .set(defaultOptions)
                      .save();
        }
    }
})();

