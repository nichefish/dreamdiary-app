package io.nicheblog.dreamdiary.feature.file.service;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditEntity;
import io.nicheblog.dreamdiary.auth.intrfc.model.BaseAuditDto;
import io.nicheblog.dreamdiary.feature.file.entity.embed.FileEmbedModule;
import io.nicheblog.dreamdiary.feature.file.exception.FileUploadException;
import io.nicheblog.dreamdiary.feature.file.model.cmpstn.FileCmpstn;
import io.nicheblog.dreamdiary.feature.file.model.cmpstn.FileCmpstnModule;
import io.nicheblog.dreamdiary.feature.file.utils.FileUtils;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.intrfc.service.BaseDtoWritableService;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.transaction.Transactional;
import java.io.Serializable;

/**
 * BaseMultiCrudInterface
 * <pre>
 *  (공통/상속) MultipartRequest(파일 업로드)를 사용하는 CRUD 공통 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface BaseMultipartWritableService<PostDto extends BaseAuditDto & FileCmpstnModule & Identifiable<Key>, Dto extends BaseAuditDto & FileCmpstnModule & Identifiable<Key>, Key extends Serializable, Entity extends BaseAuditEntity & FileEmbedModule>
        extends BaseDtoWritableService<PostDto, Dto, Key, Entity> {

    /**
     * default: 게시물 등록 (Multipart)
     *
     * @param registDto 등록할 Dto 객체
     * @param request Multipart 요청
     * @return {@link ServiceResponse} -- 처리된 Dto 객체
     */
    @Transactional
    default ServiceResponse regist(final PostDto registDto, final MultipartHttpServletRequest request) throws Exception {
        try {
            // 파일 영역 처리
            if (registDto.getFile() == null) registDto.setFile(new FileCmpstn());
            final Integer existingFileGroupId = registDto.getFile().getFileGroupId();
            final Integer processedFileGroupId = FileUtils.uploadFile(request, existingFileGroupId);
            registDto.getFile().setFileGroupId(processedFileGroupId);    // 등록된 파일 마스터ID를 가져온다.
        } catch (final Exception e) {
            throw new FileUploadException("msg.file.upload.error", e);
        }
        // 나머지 처리
        return this.regist(registDto);
    }

    /**
     * default: 게시물 수정 (Multipart)
     *
     * @param modifyDto 수정할 Dto 객체
     * @param request Multipart 요청
     * @return {@link ServiceResponse} -- 처리된 Dto 객체
     */
    @Transactional
    default ServiceResponse modify(final PostDto modifyDto, final MultipartHttpServletRequest request) throws Exception {
        try {
            // 파일 영역 처리
            if (modifyDto.getFile() == null) modifyDto.setFile(new FileCmpstn());
            final Integer existingFileGroupId = modifyDto.getFile().getFileGroupId();
            final Integer processedFileGroupId = FileUtils.uploadFile(request, existingFileGroupId);
            modifyDto.getFile().setFileGroupId(processedFileGroupId);    // 등록된 파일 마스터ID를 가져온다.
        } catch (final Exception e) {
            throw new FileUploadException("msg.file.upload.error", e);
        }
        // 나머지 처리
        return this.modify(modifyDto);
    }
}
