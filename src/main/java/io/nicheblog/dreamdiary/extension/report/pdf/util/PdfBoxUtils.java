package io.nicheblog.dreamdiary.extension.report.pdf.util;

import io.nicheblog.dreamdiary.extension.file.model.AtchFileDtlDto;
import io.nicheblog.dreamdiary.extension.file.utils.FileUtils;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * PdfBoxUtils
 * <pre>
 *  Image -> PDF 관련 처리 유틸리티 모듈
 * </pre>
 *
 * @author nichefish
 */
@Component
@Log4j2
public class PdfBoxUtils {

    /**
     * 이미지 파일 묶음을 PDF로 변환하여 다운로드
     *
     * @param fileNm 생성될 PDF 파일의 이름
     * @param fileList 변환할 이미지 파일들의 정보 리스트
     * @throws Exception 처리 중 발생한 예외
     */
    public static void imgCmbnPdfDownload(final String fileNm, final List<AtchFileDtlDto> fileList) throws Exception {
        try (final PDDocument doc = new PDDocument()) {
            // 폴더 생성
            final String pdfPath = createPdfPath();

            for (final AtchFileDtlDto atchFile : fileList) {
                // 이미지 파일 로드
                final BufferedImage awtImage = validateAndLoadImage(atchFile);
                final String imagePath = atchFile.getFileStrePath() + "/" + atchFile.getStreFileNm();
                addImageToPdf(doc, awtImage, imagePath);
                doc.save(pdfPath + fileNm);
            }

            // 응답 헤더 설정 및 한글 파일명 처리 (메소드 분리)
            FileUtils.setRespnsHeaderAndSuccessCookie(fileNm);

            final File pdfFile = new File(pdfPath, fileNm);
            FileUtils.downloadFile(pdfFile, fileNm);
        } catch (final Exception e) {
            final String rsltMsg = MessageUtils.getExceptionMsg(e);
            MessageUtils.alertMessage(rsltMsg);
            // TODO: 로그 관련 처리
        }
    }

    /**
     * PDF 파일을 저장할 경로를 생성합니다.
     *
     * @return {@link String} -- PDF 파일이 저장될 경로
     */
    private static String createPdfPath() {
        final String pdfPath = "file/_temp/pdf_temp/";
        final File file = new File(pdfPath);
        if (!file.exists()) file.mkdirs();
        return pdfPath;
    }

    /**
     * 이미지 파일을 검증한 후 BufferedImage 객체로 로드합니다.
     *
     * @param atchFile 이미지 파일 정보 객체
     * @return {@link BufferedImage} -- 로드된 이미지 객체
     * @throws Exception 이미지 로드 중 발생할 수 있는 예외
     */
    private static BufferedImage validateAndLoadImage(final AtchFileDtlDto atchFile) throws Exception {
        final File imageFile = new File(atchFile.getFileStrePath(), atchFile.getStreFileNm());
        if (!imageFile.exists()) throw new FileNotFoundException(MessageUtils.getExceptionMsg("FileNotFoundException"));
        if (!imageFile.canRead()) throw new SecurityException("파일을 읽을 권한이 없습니다.");
        return ImageIO.read(imageFile);
    }

    /**
     * 이미지 파일을 PDF 문서에 추가합니다.
     *
     * @param doc PDF 문서 객체
     * @param awtImage BufferedImage 객체
     * @param imagePath 이미지 경로
     * @throws Exception 이미지 추가 중 발생할 수 있는 예외
     */
    private static void addImageToPdf(
            final PDDocument doc,
            final BufferedImage awtImage,
            final String imagePath
    ) throws Exception {

        final PDPage pdPage = new PDPage();
        doc.addPage(pdPage);

        final PDImageXObject pdImage = PDImageXObject.createFromFile(imagePath, doc);
        try(final PDPageContentStream contents = new PDPageContentStream(doc, pdPage)) {
            // 이미지 크기 및 위치 설정
            final int pageWidth = Math.round(pdPage.getCropBox().getWidth());
            final int pageHeight = Math.round(pdPage.getCropBox().getHeight());
            final int[] imageDimensions = calculateImageDimensions(awtImage, pageWidth);
            final int pageWidthPosition = (pageWidth - imageDimensions[0]) / 2;
            final int pageHeightPosition = (pageHeight - imageDimensions[1]) / 2;

            //  이미지 회전 처리
            setPageRotation(pdPage);

            contents.drawImage(pdImage, pageWidthPosition, pageHeightPosition, imageDimensions[0], imageDimensions[1]);
        }
    }

    /**
     * 이미지 크기를 계산합니다.
     *
     * @param awtImage 이미지 객체
     * @param pageWidth 페이지 너비
     * @return {@link int[]} -- 이미지 너비와 높이
     */
    private static int[] calculateImageDimensions(final BufferedImage awtImage, final int pageWidth) {

        float imgRatio = 1;
        final int orgnlImgWidth = awtImage.getWidth(null);
        final int orgnlImgHeight = awtImage.getHeight(null);

        if (pageWidth < orgnlImgWidth) imgRatio = (float) orgnlImgWidth / pageWidth;

        final int imgWidth = Math.round(orgnlImgWidth / imgRatio);
        final int imgHeight = Math.round(orgnlImgHeight / imgRatio);
        return new int[]{imgWidth, imgHeight};
    }

    /**
     * 페이지 회전을 설정합니다.
     *
     * @param pdPage PDF 페이지 객체
     */
    private static void setPageRotation(final PDPage pdPage) {
        final PDRectangle pageSize = pdPage.getMediaBox();
        if ((pageSize.getWidth() <= pageSize.getHeight()) && (pdPage.getRotation() == 90 || pdPage.getRotation() == 270)) {
            pdPage.setRotation(0);  // 가로 이미지 처리
        } else {
            pdPage.setRotation(90); // 세로 이미지 처리
        }
    }
}
