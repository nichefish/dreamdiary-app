package io.nicheblog.dreamdiary.feature.clsf.file.jpa;

import io.nicheblog.dreamdiary.feature.clsf.file.entity.AtchFileEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

/**
 * AtchFileRepository
 * <pre>
 *  첨부파일 (JPA) Repository 인터페이스.
 *  ※첨부파일(atch_file) = 여러 첨부파일을 하나의 단위로 묶어놓은 객체. 첨부파일 상세(atch_file_dtl)를 1:N 묶음으로 관리한다.
 * </pre>
 *
 * @author nichefish
 */
@Repository
public interface AtchFileRepository
        extends BaseStreamRepository<AtchFileEntity, Integer> {
    //
}

