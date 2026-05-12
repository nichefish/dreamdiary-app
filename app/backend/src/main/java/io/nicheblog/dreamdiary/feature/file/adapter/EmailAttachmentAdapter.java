package io.nicheblog.dreamdiary.feature.file.adapter;

import io.nicheblog.dreamdiary.feature.file.model.FileRecordDto;
import io.nicheblog.dreamdiary.infrastructure.messaging.email.model.EmailAttachment;
import io.nicheblog.dreamdiary.infrastructure.messaging.email.service.EmailService;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * EmailAttachmentAdapter
 * <pre>
 *  feature 계층의 파일 첨부 DTO를 인프라 이메일 모듈에서 사용하는 EmailAttachment 모델로 변환하는 어댑터.
 * </pre>
 *
 * @author nichefish
 * @see EmailService
 */
public final class EmailAttachmentAdapter {

    private EmailAttachmentAdapter() {
        // 인스턴스 생성 방지
    }

    /**
     * 첨부파일 상세 DTO를 EmailAttachment로 변환한다.
     *
     * @param dto 첨부파일 상세 DTO
     * @return EmailAttachment 변환 결과 (dto가 null인 경우 null 반환)
     */
    public static EmailAttachment toEmailAttachment(final FileRecordDto dto) {
        if (dto == null) return null;

        return EmailAttachment.builder()
                .fileNm(dto.getOrgnFileNm())
                .filePath(dto.getFileStrePath())
                .build();
    }

    /**
     * 첨부파일 상세 DTO 리스트를 EmailAttachment 리스트로 변환한다.
     *
     * @param dtoList 첨부파일 상세 DTO 리스트
     * @return EmailAttachment 리스트 (비어있을 경우 빈 리스트 반환)
     */
    public static List<EmailAttachment> toEmailAttachmentList(final List<FileRecordDto> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) return Collections.emptyList();

        return dtoList.stream()
                .map(EmailAttachmentAdapter::toEmailAttachment)
                .collect(Collectors.toList());
    }
}
