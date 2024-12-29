package io.nicheblog.dreamdiary.domain.admin.menu.repository.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * MenuMapper
 * <pre>
 *  메뉴 관리 MyBatis 기반 Mapper 인터페이스
 * </pre>
 *
 * @author nichefish
 */
@Mapper
public interface MenuMapper {

    /**
     * 최상위(MAIN) 메뉴의 관리자 메뉴 여부 (Y/N) 조회
     * @param menuNo 체크할 메뉴 번호
     * @return {@link String} -- 관리자 메뉴 여부 (Y/N)
     */
    String getMngrYn(final @Param("menuNo") Integer menuNo);
}
