package io.nicheblog.dreamdiary.feature.admin.tmplat.repository.jpa;

import io.nicheblog.dreamdiary.feature.admin.tmplat.entity.TmplatTxtEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.QueryHint;
import java.util.Optional;

/**
 * TmplatTxtRepository
 * <pre>
 *  입력내용(텍스트에디터) 템플릿 관리 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Repository("tmplatTxtRepository")
public interface TmplatTxtRepository
        extends BaseStreamRepository<TmplatTxtEntity, Integer> {

    /**
     * 템플릿 정의 코드, 카테고리 코드, 그리고 기본 여부에 따라 템플릿 텍스트를 검색합니다.
     *
     * @param tmplatDefCd 템플릿 정의 코드
     * @param ctgrCd 카테고리 코드
     * @param defaultYn 기본 템플릿 여부 ("Y" 또는 "N")
     * @return {@link Optional} - 결과를 담은 Optional 객체
     */
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Optional<TmplatTxtEntity> findByTmplatDefCdAndCtgrCdAndDefaultYn(
            final String tmplatDefCd,
            final String ctgrCd,
            final String defaultYn
    );
}
