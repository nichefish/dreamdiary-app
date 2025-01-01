/**
 * commons-pdf.js
 * @namespace: commons.pdf
 * @author: nichefish
 * @since: 2022-06-27
 * @depdendency: html2pdf
 * 공통 - html2pdf(라이브러리) 관련 함수 모듈
 * (노출식 모듈 패턴 적용 :: commons.pdf.fnPdf("#id") 이런식으로 사용)
 */
if (typeof commons === 'undefined') {
    var commons = {};
}
commons.pdf = (function () {
    /**
     * PDF 생성 기본 옵션
     */
    const defaultOptions = {
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
        fnPdf: function (elmtId) {
            const elmt = document.getElementById(elmtId);
            html2pdf().from(elmt)
                .set(defaultOptions)
                .save();
        }
    };
})();
