package io.nicheblog.dreamdiary.global.intrfc.service;

import io.nicheblog.dreamdiary.global._common.file.exception.AtchFileUploadException;
import io.nicheblog.dreamdiary.global._common.file.utils.FileUtils;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseAtchEntity;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseCrudMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseAtchDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import io.nicheblog.dreamdiary.global.intrfc.spec.BaseSpec;
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
public interface BaseMultiCrudService<Dto extends BaseAtchDto & Identifiable<Key>, ListDto extends BaseAtchDto, Key extends Serializable, Entity extends BaseAtchEntity, Repository extends BaseStreamRepository<Entity, Key>, Spec extends BaseSpec<Entity>, Mapstruct extends BaseCrudMapstruct<Dto, ListDto, Entity>>
        extends BaseCrudService<Dto, ListDto, Key, Entity, Repository, Spec, Mapstruct> {

    /**
     * default: 게시물 등록 (Multipart)
     *
     * @param registDto 등록할 DTO 객체
     * @param request Multipart 요청
     * @return {@link Dto} -- 처리된 DTO 객체
     * @throws Exception 파일 업로드 또는 등록 중 예외 발생 시
     */
    @Transactional
    default Dto regist(final Dto registDto, final MultipartHttpServletRequest request) throws Exception {
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
     * @param modifyDto 수정할 DTO 객체
     * @param request Multipart 요청
     * @return {@link Dto} -- 처리된 DTO 객체
     * @throws Exception 파일 업로드 또는 수정 중 예외 발생 시
     */
    @Transactional
    default Dto modify(final Dto modifyDto, final MultipartHttpServletRequest request) throws Exception {
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
