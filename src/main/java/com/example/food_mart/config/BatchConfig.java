package com.example.food_mart.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JdbcJobRepositoryFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {
    private final DataSource dataSource;
    private final PlatformTransactionManager transactionManager;

    // Spring Batch 메타 테이블 자동 생성 — jar 내 공식 스키마 SQL 사용
    // setContinueOnError(true): 테이블이 이미 존재해도 오류 없이 넘어감
    @Bean
    public DataSourceInitializer batchSchemaInitializer() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("org/springframework/batch/core/schema-mysql.sql"));
        populator.setContinueOnError(true);

        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(populator);
        return initializer;
    }

    // 빈 이름을 "jdbcJobRepository"로 지정 → autoconfiguration의 "jobRepository"(in-memory)와 충돌 없음
    // @Primary → JobRepository 타입으로 주입받는 곳은 모두 이 JDBC 버전을 사용
    @Bean
    @Primary
    public JobRepository jdbcJobRepository() throws Exception {
        JdbcJobRepositoryFactoryBean factory = new JdbcJobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.setDatabaseType("MYSQL");
        factory.afterPropertiesSet();
        return factory.getObject();
    }
}
