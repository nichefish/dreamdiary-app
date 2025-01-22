package io.nicheblog.dreamdiary.global.handler;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Properties;

/**
 * SchemaHandler
 * <pre>
 *  .sql 실행 관련 핸들러
 * </pre>
 *
 * @author nichefish
 */
@Component
@Log4j2
public class SchemaHandler {

    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private DataSource datasource;
    @Resource
    private BuildProperties buildProperties;
    @Resource
    @Qualifier("vendorProperties")
    private Properties vendorProperties;

    /**
     * sql 실행 전 사전 준비
     */
    public boolean execute() throws Exception {

        log.info("Product Name: " + buildProperties.getName());
        log.info("Product Version: " + buildProperties.getVersion());
        log.info("Build Date: " + buildProperties.getTime());

        // sql script 실행:: 메소드 분리
        return this.execSqlScript();
    }

    /**
     * sql script 실행:: 메소드 분리
     */
    public boolean execSqlScript() throws Exception {

        // database vendor 판별
        String databaseProductName = this.getDatabaseProductName();
        String vendor = (String) vendorProperties.getOrDefault(databaseProductName, "");
        log.info(vendor);

        // database vendor에 따라 .sql 파일 로드
        // TODO: 상황에 맞게 로직 보완하기 (releaseDate 등)
        String releaseDate = "240224";
        String location = String.format("classpath:schema/" + releaseDate + "/data-%s.sql", vendor);
        org.springframework.core.io.Resource schemaSql = applicationContext.getResource(location);
        if (!schemaSql.exists()) return true;

        EncodedResource encodedResource = new EncodedResource(schemaSql, "UTF-8");
        try {
            ScriptUtils.executeSqlScript(
                    datasource.getConnection(),
                    encodedResource,
                    false,
                    false,
                    "--",
                    ";;",
                    "##/*",
                    "*/##"
            );
        } catch (final Exception e) {
            log.warn(e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * databaseProductName 얻기:: 메소드 분리
     */
    public String getDatabaseProductName() throws SQLException {
        Connection connection = datasource.getConnection();
        DatabaseMetaData databaseMetaData = connection.getMetaData();

        return databaseMetaData.getDatabaseProductName();
    }

}
