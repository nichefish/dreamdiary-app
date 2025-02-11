package io.nicheblog.dreamdiary.extension.cd.spec;

import io.nicheblog.dreamdiary.extension.cd.entity.DtlCdEntity;
import io.nicheblog.dreamdiary.global.intrfc.spec.embed.BaseStateSpec;
import org.springframework.stereotype.Component;

/**
 * DtlCdSpec
 * <pre>
 *  상세 코드 검색인자 세팅 Specification.
 *  ※상세 코드(dtl_cd) = 분류 코드 하위의 상세 코드. 분류 코드(cl_cd)에 N:1로 귀속된다.
 * </pre>
 *
 * @author nichefish
 */
@Component
public class DtlCdSpec
        implements BaseStateSpec<DtlCdEntity> {
    //
}
