package ag.com.dbo.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static ag.com.dbo.config.Utils.checkProps;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "ag.com.dbo.repositories.queue",
        entityManagerFactoryRef = "queueEntityManagerFactory",
        transactionManagerRef = "queueTransactionManager"
)

public class QueueDataSourceConfiguration {
    @Autowired
    private Environment env;

    @Bean(name = "queueDataSource")
    public DataSource queueDataSource() {

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(Objects.requireNonNull(env.getProperty("spring.datasource.queue.driver-class-name")));
        dataSource.setUrl(env.getProperty("spring.datasource.queue.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.queue.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.queue.password"));

        return dataSource;
    }

    @Bean(name = "queueEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean queueEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("queueDataSource") DataSource queueDataSource) {
        Map<String,Object> property =  new HashMap<>();
        checkProps(property, "hibernate.hbm2ddl.auto.queue");
        checkProps(property, "hibernate.dialect.queue");

        return builder
                .dataSource(queueDataSource())
                .packages("ag.com.dbo.models.queue")
                .persistenceUnit("queue")
                .properties(property)
                .build();
    }

    @Bean(name = "queueTransactionManager")
    public PlatformTransactionManager queueTransactionManager(
            @Qualifier("queueEntityManagerFactory") EntityManagerFactory queueEntityManagerFactory) {
        return new JpaTransactionManager(queueEntityManagerFactory);
    }
}
