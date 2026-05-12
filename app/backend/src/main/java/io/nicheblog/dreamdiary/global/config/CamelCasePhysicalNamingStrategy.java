package io.nicheblog.dreamdiary.global.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * CamelCasePhysicalNamingStrategy
 * <pre>
 *  JPA에서 컬럼명 규칙 전략 수동 주입
 * </pre>
 *
 * @author nichefish
 */
public class CamelCasePhysicalNamingStrategy
        extends PhysicalNamingStrategyStandardImpl {

    /**
     * 논리적 테이블 이름을 물리적 테이블 이름으로 변환하는 메서드입니다.
     *
     * @param name 논리적 테이블 이름
     * @param context JDBC 환경 정보
     * @return 변환된 물리적 테이블 이름
     */
    @Override
    public Identifier toPhysicalTableName(final Identifier name, final JdbcEnvironment context) {
        // 테이블 이름 변환 로직을 여기에 구현
        return new Identifier(name.getText(), name.isQuoted());
    }

    /**
     * 논리적 컬럼 이름을 물리적 컬럼 이름으로 변환하는 메서드입니다.
     *
     * @param name 논리적 컬럼 이름
     * @param context JDBC 환경 정보
     * @return 변환된 물리적 컬럼 이름
     */
    @Override
    public Identifier toPhysicalColumnName(final Identifier name, final JdbcEnvironment context) {
        // 컬럼 이름 변환 로직을 여기에 구현
        return new Identifier(name.getText(), name.isQuoted());
    }
}
