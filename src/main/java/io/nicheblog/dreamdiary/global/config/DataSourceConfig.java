package io.nicheblog.dreamdiary.global.config;

import com.p6spy.engine.spy.P6DataSource;
import com.zaxxer.hikari.HikariDataSource;
import io.nicheblog.dreamdiary.global.intrfc.repository.impl.BaseRepositoryImpl;
import org.apache.ibatis.session.SqlSessionFactory;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.jetbrains.annotations.NotNull;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;

/**
 * DataSourceConfig
 * <pre>
 *  멀티 데이터소스 설정시 Primary DataSource Config.
 *  JPA, MyBatis 각각에 대해 설정해준다.
 * </pre>
 *
 * @author nichefish
 */
@Configuration
@EnableJpaRepositories(
        basePackages = {"io.nicheblog.dreamdiary.**.repository.jpa", "io.nicheblog.dreamdiary.**.repository.querydsl" },
        repositoryBaseClass = BaseRepositoryImpl.class
)
@MapperScan(
        basePackages = { "io.nicheblog.dreamdiary.**.repository.mybatis" },
        sqlSessionTemplateRef = "sqlSessionTemplate"
)
public class DataSourceConfig
        implements EnvironmentAware {

    @Resource
    private Environment env;

    /**
     * 빈 등록 :: dataSource
     * `@ConfigurationProperties`룰 통해 application.yml 속성을 자동 매핑한다.
     *
     * @return {@link DataSource} -- DataSource 객체
     */
    @Bean(name = "primaryOriginalDataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.primary")    // application.properties에서 사용한 이름
    public DataSource dataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    /**
     * 빈 등록 :: P6Spy를 적용한 DataSource 생성
     * 주어진 {@link DataSource}가 이미 P6Spy로 래핑되어 있지 않다면, P6Spy를 적용하여 반환한다.
     *
     * @param dataSource 원본 HikariCP {@link DataSource}
     * @return {@link DataSource} P6Spy가 적용된 DataSource 객체
     */
    @Bean(name = "primaryDataSource")
    public DataSource createP6SpyDataSource(
            final @Qualifier("primaryOriginalDataSource") DataSource dataSource
    ) {
        if (!(dataSource instanceof P6DataSource)) return new P6DataSource(dataSource);
        return dataSource;
    }

    /**
     * 빈 등록 :: (JPA) EntityManagerFactory
     *
     * @param dataSource 주 데이터 소스 (dataSource)
     * @param jpaProperties JPA 설정 정보 (from application.yml)
     * @return {@link EntityManagerFactory} -- EntityManagerFactory 객체
     */
    @Bean(name = "entityManagerFactory")
    @Primary
    public EntityManagerFactory entityManagerFactory(
            final @Qualifier("primaryDataSource") DataSource dataSource,
            final JpaProperties jpaProperties
    ) {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("io.nicheblog.dreamdiary.**.entity");

        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        final HashMap<String, Object> properties = new HashMap<>(jpaProperties.getProperties());
        properties.put("hibernate.physical_naming_strategy", CamelCaseToUnderscoresNamingStrategy.class.getName()); // 네이밍
        properties.put("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName()); // 네이밍
        properties.put("hibernate.enable_lazy_load_no_trans", "true");
        em.setJpaPropertyMap(properties);
        em.afterPropertiesSet();

        return em.getObject();
    }

    /**
     * 빈 등록 :: (JPA) TransactionManager (JPA 트랜잭션 관리)
     * "@Transactional을 사용할 때, 어떤 데이터 소스를 사용할지 명확하게 지정해야 한다."
     *
     * @param entityManagerFactory JPA EntityManagerFactory 객체
     * @return PlatformTransactionManager 객체
     */
    @Bean(name = "transactionManager")
    @Primary
    public PlatformTransactionManager transactionManager(
            final @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        final JpaTransactionManager jtm = new JpaTransactionManager();
        jtm.setEntityManagerFactory(entityManagerFactory);
        return jtm;
    }

    /**
     * 빈 등록 :: Hibernate SessionFactory
     *
     * @param entityManagerFactory JPA {@link EntityManagerFactory} 객체
     * @return {@link SessionFactory} Hibernate SessionFactory
     */
    @Bean(name = "hibernateSessionFactory")  // Hibernate용 SessionFactory
    public SessionFactory hibernateSessionFactory(
            final @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        return entityManagerFactory.unwrap(SessionFactory.class);
    }

    /**
     * 빈 등록 :: (MyBatis) sessionFactory
     *
     * @param dataSource 주 데이터 소스 (dataSource)
     * @param applicationContext 애플리케이션 컨텍스트 (ApplicationContext), MyBatis 매퍼 파일의 위치를 가져오기 위해 사용
     * @return {@link SqlSessionFactory} -- SqlSessionFactory 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Bean(name = "sqlSessionFactory")
    @Primary
    public SqlSessionFactory sqlSessionFactory(
            final @Qualifier("primaryDataSource") DataSource dataSource,
            final ApplicationContext applicationContext
    ) throws Exception {

        final SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setMapperLocations(applicationContext.getResources("classpath*:**/repository/mybatis/*Mapper.xml"));
        bean.setDataSource(dataSource);
        bean.setVfs(SpringBootVFS.class);
        bean.setTypeAliasesPackage("io.nicheblog.dreamdiary.**.model");
        return bean.getObject();
    }

    /**
     * 빈 등록 :: (MyBatis) sqlSessionTemplate
     *
     * @param sqlSessionFactory MyBatis의 SqlSessionFactory 객체
     * @return {@link SqlSessionTemplate} -- SqlSessionTemplate 객체
     */
    @Bean(name = "sqlSessionTemplate")
    @Primary
    public SqlSessionTemplate sqlSessionTemplate(
            final @Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory
    ) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    /**
     * 환경 설정을 주입합니다. (환경 변수에 직접 접근하느 클래스)
     *
     * @param environment Spring의 Environment 객체, 애플리케이션의 환경 설정 정보를 포함
     */
    @Override
    public void setEnvironment(final @NotNull Environment environment) {
        env = environment;
    }
}