package io.nicheblog.dreamdiary.global.intrfc.service;

import io.nicheblog.dreamdiary.extension.file.exception.AtchFileUploadException;
import io.nicheblog.dreamdiary.extension.file.utils.FileUtils;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseAtchEntity;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseAtchDto;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
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
public interface BaseMultiCrudService<PostDto extends BaseAtchDto & Identifiable<Key>, Dto extends BaseCrudDto & Identifiable<Key>, Key extends Serializable, Entity extends BaseAtchEntity>
        extends BaseCrudService<PostDto, Dto, Key, Entity> {

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
            final Integer atchFileNo = registDto.getAtchFileNo();
            registDto.setAtchFileNo(FileUtils.uploadFile(request, atchFileNo));    // 등록된 파일 마스터ID를 가져온다.
        } catch (final Exception e) {
            throw new AtchFileUploadException("파일 업로드 중 오류가 발생했습니다.", e);
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
            final Integer atchFileNo = modifyDto.getAtchFileNo();
            modifyDto.setAtchFileNo(FileUtils.uploadFile(request, atchFileNo));    // 등록된 파일 마스터ID를 가져온다.
        } catch (final Exception e) {
            throw new AtchFileUploadException("파일 업로드 중 오류가 발생했습니다.", e);
        }
        // 나머지 처리
        return this.modify(modifyDto);
    }
}
