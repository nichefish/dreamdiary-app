package io.nicheblog.dreamdiary.extension.file.repository.jpa;

import io.nicheblog.dreamdiary.extension.file.entity.AtchFileDtlEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * AtchFileDtlRepository
 * <pre>
 *  첨부파일 상세 (JPA) Repository 인터페이스.
 *  ※첨부파일 상세(atch_file_dtl) = 실제 첨부파일 정보를 담고 있는 객체. 첨부파일(atch_file)에 N:1로 귀속된다.
 * </pre>
 *
 * @author nichefish
 */
@Repository("atchFileDtlRepository")
public interface AtchFileDtlRepository
        extends BaseStreamRepository<AtchFileDtlEntity, Integer> {
    //
}
